package com.zhuhuibao.business.oms.tech;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.zhuhuibao.common.Response;
import com.zhuhuibao.common.constant.Constants;
import com.zhuhuibao.common.constant.MsgCodeConstant;
import com.zhuhuibao.common.constant.PayConstants;
import com.zhuhuibao.common.constant.TechConstant;
import com.zhuhuibao.common.util.ShiroUtil;
import com.zhuhuibao.exception.AuthException;
import com.zhuhuibao.exception.BusinessException;
import com.zhuhuibao.mybatis.memCenter.service.UploadService;
import com.zhuhuibao.mybatis.order.entity.Order;
import com.zhuhuibao.mybatis.order.entity.OrderGoods;
import com.zhuhuibao.mybatis.order.service.OrderGoodsService;
import com.zhuhuibao.mybatis.order.service.OrderService;
import com.zhuhuibao.mybatis.tech.entity.TrainPublishCourse;
import com.zhuhuibao.mybatis.tech.service.PublishTCourseService;
import com.zhuhuibao.service.course.CourseService;
import com.zhuhuibao.utils.MsgPropertiesUtils;
import com.zhuhuibao.utils.oss.ZhbOssClient;
import com.zhuhuibao.utils.pagination.model.Paging;
import com.zhuhuibao.utils.pagination.util.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运营管理平台技术培训课程管理
 *
 * @author Administrator
 * @version 2016/6/2 0002
 */
@RestController
@RequestMapping("/rest/tech/oms/publish")
@Api(value = "trainCourseOms", description = "发布技术培训课程")
public class TrainCourseOmsController {
    @Autowired
    PublishTCourseService ptCourseService;

    @Autowired
    ZhbOssClient zhbOssClient;

    @Autowired
    CourseService courseServie;

    @Autowired
    OrderService orderService;

    @RequestMapping(value = "add_course", method = RequestMethod.POST)
    @ApiOperation(value = "更新未发布的培训课程", notes = "插入发布的培训课程", response = Response.class)
    public Response insertTrainCourse(@ApiParam(value = "培训课程") @ModelAttribute TrainPublishCourse course) {
        Long omsOperateId = ShiroUtil.getOmsCreateID();
        if (omsOperateId != null) {
            //插入发布者Id
            course.setPublisherid(omsOperateId);
            int result = ptCourseService.insertPublishCourse(course);
        } else {
            throw new AuthException(MsgCodeConstant.un_login, MsgPropertiesUtils.getValue(String.valueOf(MsgCodeConstant.un_login)));
        }
        Response response = new Response();
        return response;
    }

    @RequestMapping(value = "sel_course_info", method = RequestMethod.GET)
    @ApiOperation(value = "查看发布的培训课程信息", notes = "查看发布的培训课程信息", response = Response.class)
    public Response selectTrainCourseInfo(@ApiParam(value = "培训课程ID") @RequestParam Long courseId) {
        Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("courseid", courseId);
        TrainPublishCourse course = ptCourseService.selectTrainCourseInfo(condition);
        Response response = new Response();
        response.setData(course);
        return response;
    }

    @RequestMapping(value = "upd_course", method = RequestMethod.POST)
    @ApiOperation(value = "更新未发布的培训课程", notes = "更新发布的培训课程", response = Response.class)
    public Response updateTrainCourse(@ApiParam(value = "培训课程") @ModelAttribute TrainPublishCourse course) {
        Long omsOperateId = ShiroUtil.getOmsCreateID();
        if (omsOperateId != null) {
            //更新发布者ID
            course.setPublisherid(omsOperateId);
            int result = ptCourseService.updatePublishCourse(course);
        } else {
            throw new AuthException(MsgCodeConstant.un_login, MsgPropertiesUtils.getValue(String.valueOf(MsgCodeConstant.un_login)));
        }
        Response response = new Response();
        return response;
    }

    @RequestMapping(value = "upd_publishCourse", method = RequestMethod.POST)
    @ApiOperation(value = "上架", notes = "上架", response = Response.class)
    public Response publishTrainCourse(@ApiParam(value = "课程ID") @RequestParam String courseId) {
        Long omsOperateId = ShiroUtil.getOmsCreateID();
        if (omsOperateId != null) {
            int result = ptCourseService.publishCourse(courseId, omsOperateId);
        } else {
            throw new AuthException(MsgCodeConstant.un_login, MsgPropertiesUtils.getValue(String.valueOf(MsgCodeConstant.un_login)));
        }
        Response response = new Response();
        return response;
    }

    @RequestMapping(value = "upd_beginCourse", method = RequestMethod.POST)
    @ApiOperation(value = "开始上课", notes = "开始上课", response = Response.class)
    public Response beginCourse(@ApiParam(value = "课程ID") @RequestParam String courseId) throws Exception {
        //查看该课程是否存在 已支付订单
        List<Order> orderList = orderService.findListByCourseIdAndStatus(courseId, PayConstants.OrderStatus.YZF.toString());
        if(orderList.size() > 0){
            courseServie.begin(courseId);
            return new Response();
        } else{
            throw new BusinessException(MsgCodeConstant.SYSTEM_ERROR,"该课程不存在已支付订单,无法开课");
        }
    }

    @RequestMapping(value = "upd_stopCourse", method = RequestMethod.POST)
    @ApiOperation(value = "终止课程", notes = "终止课程", response = Response.class)
    public Response stopCourse(@ApiParam(value = "课程ID") @RequestParam String courseId) throws Exception {

        courseServie.stop(courseId);
        Response response = new Response();
        return response;
    }

    @RequestMapping(value = "upd_completeCourse", method = RequestMethod.POST)
    @ApiOperation(value = "完成课程", notes = "完成课程", response = Response.class)
    public Response completeCourse(@ApiParam(value = "课程ID") @RequestParam String courseId) throws Exception {
        courseServie.complete(courseId);
        Response response = new Response();
        return response;
    }

    @RequestMapping(value = "sel_publish_course", method = RequestMethod.GET)
    @ApiOperation(value = "运营管理平台搜索技术的发布课程", notes = "运营管理平台搜索技术的发布课程", response = Response.class)
    public Response findAllTechDataPager(@ApiParam(value = "课程名称") @RequestParam(required = false) String title,
                                         @ApiParam(value = "状态：1未上架，2销售中，3待开课，4上课中，5已终止，6已完成") @RequestParam(required = false) String status,
                                         @ApiParam(value = "课程类型：1：技术培训，2专家培训") @RequestParam String type,
                                         @ApiParam(value = "省") @RequestParam(required = false) String province,
                                         @ApiParam(value = "市") @RequestParam(required = false) String city,
                                         @ApiParam(value = "页码") @RequestParam(required = false) String pageNo,
                                         @ApiParam(value = "每页显示的数目") @RequestParam(required = false) String pageSize) {
        Response response = new Response();
        Map<String, Object> condition = new HashMap<String, Object>();
        condition.put("courseType", type);
        condition.put("mobile", province);
        condition.put("city", city);
        if (title != null && !title.equals("")) {
            condition.put("title", title.replaceAll("_", "\\_"));
        }
        if (StringUtils.isEmpty(pageNo)) {
            pageNo = "1";
        }
        if (StringUtils.isEmpty(pageSize)) {
            pageSize = "10";
        }
        Paging<Map<String, String>> pager = new Paging<Map<String, String>>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        condition.put("status", status);
        List<Map<String, String>> techList = ptCourseService.findAllTrainCoursePager(pager, condition);
        pager.result(techList);
        response.setData(pager);
        return response;
    }

    @ApiOperation(value = "上传培训轮播图", notes = "上传培训轮播图", response = Response.class)
    @RequestMapping(value = "upload_img", method = RequestMethod.POST)
    public Response uploadImg(@RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
        Response result = new Response();
        Subject currentUser = SecurityUtils.getSubject();
        Session session = currentUser.getSession(false);
        if (null != session) {
            String url = zhbOssClient.uploadObject(file, "img", "tech");
            result.setData(url);
            result.setCode(200);
        } else {
            throw new AuthException(MsgCodeConstant.un_login, MsgPropertiesUtils.getValue(String.valueOf(MsgCodeConstant.un_login)));
        }
        return result;
    }

}
