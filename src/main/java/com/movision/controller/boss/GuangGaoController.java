package com.movision.controller.boss;

import com.movision.common.Response;
import com.movision.facade.boss.HomepageManageFacade;
import com.movision.mybatis.homepageManage.entity.HomepageLinkage;
import com.movision.mybatis.homepageManage.entity.HomepageManage;
import com.movision.mybatis.homepageManage.entity.HomepageManageVo;
import com.movision.mybatis.homepageManage.service.HomepageManageService;
import com.movision.mybatis.manageType.entity.ManageType;
import com.movision.utils.file.FileUtil;
import com.movision.utils.oss.MovisionOssClient;
import com.movision.utils.pagination.model.Paging;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author zhurui
 * @Date 2017/3/4 17:21
 */
@RestController
@RequestMapping("/boss/guang_gao")
public class GuangGaoController {

    @Autowired
    HomepageManageFacade homepageManageFacade;

    @Autowired
    MovisionOssClient movisionOssClient;


    /**
     * 查询广告列表
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "查询广告列表", notes = "用于查询广告列表接口", response = Response.class)
    @RequestMapping(value = "query_guang_gao_list", method = RequestMethod.POST)
    public Response queryAdvertisementList(@ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                           @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<HomepageManageVo> pager = new Paging(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<HomepageManageVo> list = homepageManageFacade.queryAdvertisementList(pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }


    /**
     * 根据条件查询广告列表
     *
     * @param name
     * @param type
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "模糊查询广告列表", notes = "用于条件查询广告接口", response = Response.class)
    @RequestMapping(value = "query_guang_gao_like", method = RequestMethod.POST)
    public Response queryAdvertisementLike(@ApiParam(value = "广告位置") @RequestParam(required = false) String name,
                                           @ApiParam(value = "排序（传1按时间正序，默认倒叙)") @RequestParam(required = false) String type,
                                           @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                           @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<HomepageManageVo> pager = new Paging(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<HomepageManageVo> list = homepageManageFacade.queryAdvertisementLike(name, type, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }

    /**
     * 根据广告类型查询排序
     *
     * @return
     */
    @ApiOperation(value = "根据广告位置类型查询排序", notes = "根据广告位置类型查询排序", response = Response.class)
    @RequestMapping(value = "query_guang_gao_location", method = RequestMethod.POST)
    public Response queryAdvertisementLocation(@ApiParam(value = "广告位置类型") @RequestParam String type,
                                               @ApiParam(value = "排序id") @RequestParam(required = false) String orderid) {
        Response response = new Response();
        List<Integer> list = homepageManageFacade.queryAdvertisementLocation(type, orderid);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(list);
        return response;
    }

    /**
     * 操作广告排序 （0删除 1添加）
     *
     * @return
     */
    @ApiOperation(value = "操作广告排序位置", notes = "用于操作广告排序位置接口", response = Response.class)
    @RequestMapping(value = "operation_guang_gao_orderid", method = RequestMethod.POST)
    public Response operationAdvertisementOrderid(@ApiParam(value = "操作方式0 删除 1添加") @RequestParam String type,
                                                  @ApiParam(value = "广告id") @RequestParam String id,
                                                  @ApiParam(value = "排序id") @RequestParam(required = false) String orderid) {
        Response response = new Response();
        int map = homepageManageFacade.operationAdvertisementOrderid(type, id, orderid);
        if (response.getCode() == 200) {
            response.setMessage("操作成功");
        }
        response.setData(map);
        return response;
    }

    /**
     * 添加广告
     *
     * @param topictype
     * @param orderid
     * @param content
     * @param subcontent
     * @param url
     * @param transurl
     * @return
     */
    @ApiOperation(value = "添加广告", notes = "用于添加广告接口", response = Response.class)
    @RequestMapping(value = "add_guang_gao", method = RequestMethod.POST)
    public Response addAdvertisement(@ApiParam(value = "广告位置（传位置code）") @RequestParam String topictype,
                                     @ApiParam(value = "排序") @RequestParam(required = false) String orderid,
                                     @ApiParam(value = "主标题") @RequestParam String content,
                                     @ApiParam(value = "副标题") @RequestParam String subcontent,
                                     @ApiParam(value = "广告类型 0 帖子 1 活动 2 H5") String type,
                                     @ApiParam(value = "帖子或活动id") @RequestParam String postid,
                                     @ApiParam(value = "图片URL") @RequestParam String url,
                                     @ApiParam(value = "跳转链接URL") @RequestParam(required = false) String transurl) {
        Response response = new Response();
        int map = homepageManageFacade.addAdvertisement(topictype, orderid, content, subcontent, url, transurl, type, postid);
        if (response.getCode() == 200) {
            response.setMessage("操作成功");
        }
        response.setData(map);
        return response;
    }

    /**
     * 查询广告详情
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "查询广告详情", notes = "查询广告详情", response = Response.class)
    @RequestMapping(value = "query_guang_gao_particulars", method = RequestMethod.POST)
    public Response queryAvertisementById(@ApiParam(value = "广告id") @RequestParam String id) {
        Response response = new Response();
        HomepageManageVo particulars = homepageManageFacade.queryAvertisementById(id);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(particulars);
        return response;
    }

    /**
     * 编辑广告
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "编辑广告", notes = "用于编辑广告接口", response = Response.class)
    @RequestMapping(value = "update_guang_gao", method = RequestMethod.POST)
    public Response updateAdvertisement(@ApiParam(value = "广告id") @RequestParam String id,
                                        @ApiParam(value = "排序") @RequestParam(required = false) String orderid,
                                        @ApiParam(value = "主标题") @RequestParam String content,
                                        @ApiParam(value = "副标题") @RequestParam String subcontent,
                                        @ApiParam(value = "图片URL") @RequestParam String url,
                                        @ApiParam(value = "跳转链接URL") @RequestParam(required = false) String transurl) {
        Response response = new Response();
        int i = homepageManageFacade.updateAdvertisement(id, orderid, content, subcontent, url, transurl);
        if (response.getCode() == 200) {
            response.setMessage("操作成功");
        }
        response.setData(i);
        return response;
    }

    /**
     * 根据id删除广告
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "删除广告", notes = "用于删除广告接口", response = Response.class)
    @RequestMapping(value = "delete_guang_gao", method = RequestMethod.POST)
    public Response deleteAdvertisement(@ApiParam(value = "广告id") @RequestParam String id) {
        Response response = new Response();
        Map map = homepageManageFacade.deleteAdvertisement(id);
        if (response.getCode() == 200) {
            response.setMessage("操作成功");
        }
        response.setData(map);
        return response;
    }


    /**
     * 查询广告类型列表
     *
     * @return
     */
    @ApiOperation(value = "查询广告类型列表", notes = "用于查询广告类型列表接口", response = Response.class)
    @RequestMapping(value = "query_guang_gao_type", method = RequestMethod.POST)
    public Response queryAdvertisementTypeList(@ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                               @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<ManageType> pager = new Paging<ManageType>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<ManageType> type = homepageManageFacade.queryAdvertisementTypeList(pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(type);
        response.setData(pager);
        return response;
    }


    /**
     * 根据条件查询广告类型列表
     *
     * @param name
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "根据条件查询广告类型列表", notes = "根据条件查询广告类型列表", response = Response.class)
    @RequestMapping(value = "query_type_like_name", method = RequestMethod.POST)
    public Response queryAdvertisementTypeLikeName(@ApiParam(value = "位置名称") @RequestParam(required = false) String name,
                                                   @ApiParam(value = "排序(传1按时间正序，否则倒叙)") @RequestParam(required = false) String type,
                                                   @ApiParam(value = "当前第几页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                                   @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<ManageType> pager = new Paging<ManageType>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<ManageType> list = homepageManageFacade.queryAdvertisementTypeLikeName(name, type, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }


    /**
     * 添加广告类型
     *
     * @param name
     * @param wide
     * @param high
     * @param quantity
     * @return
     */
    @ApiOperation(value = "添加广告类型", notes = "用于添加广告类型接口", response = Response.class)
    @RequestMapping(value = "add_guang_gao_type", method = RequestMethod.POST)
    public Response addAdvertisementType(@ApiParam(value = "广告位置名称") @RequestParam String name,
                                         @ApiParam(value = "广告宽度") @RequestParam String wide,
                                         @ApiParam(value = "广告高度") @RequestParam String high,
                                         @ApiParam(value = "广告数量") @RequestParam String quantity) {
        Response response = new Response();
        int i = homepageManageFacade.addAdvertisementType(name, wide, high, quantity);
        if (response.getCode() == 200) {
            response.setMessage("操作成功");
        }
        response.setData(i);
        return response;
    }

    /**
     * 根据id查询广告类型详情
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "查询广告类型详情", notes = "用于根据广告id查询广告类型详情", response = Response.class)
    @RequestMapping(value = "query_type_id", method = RequestMethod.POST)
    public Response queryAdvertisementTypeById(@ApiParam(value = "类型id") @RequestParam String id) {
        ManageType manageType = homepageManageFacade.queryAdvertisementTypeById(id);
        Response response = new Response();
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(manageType);
        return response;
    }

    /**
     * 编辑广告类型
     *
     * @param id
     * @param name
     * @param wide
     * @param high
     * @return
     */
    @ApiOperation(value = "编辑广告类型", notes = "编辑广告类型", response = Response.class)
    @RequestMapping(value = "update_guang_gao_type", method = RequestMethod.POST)
    public Response updateAdvertisementType(@ApiParam(value = "广告位置id") @RequestParam String id,
                                            @ApiParam(value = "广告位置名称") @RequestParam(required = false) String name,
                                            @ApiParam(value = "广告宽度") @RequestParam(required = false) String wide,
                                            @ApiParam(value = "广告高度") @RequestParam(required = false) String high) {
        Response response = new Response();
        Map map = homepageManageFacade.updateAdvertisementType(id, name, wide, high);
        if (response.getCode() == 200) {
            response.setMessage("操作成功");
        }
        response.setData(map);
        return response;
    }


    /**
     * 上传广告相关图片
     *
     * @param file
     * @return
     */
    @ApiOperation(value = "上传广告相关图片", notes = "上传广告相关图片", response = Response.class)
    @RequestMapping(value = {"/upload_guang_gao_img"}, method = RequestMethod.POST)
    public Response updatePostImg(@RequestParam(value = "file", required = false) MultipartFile file) {

        Map m = movisionOssClient.uploadObject(file, "img", "advertisement");
        String url = String.valueOf(m.get("url"));
        Map<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("name", FileUtil.getFileNameByUrl(url));
        map.put("width", m.get("width"));
        map.put("height", m.get("height"));
        return new Response(map);
    }
}
