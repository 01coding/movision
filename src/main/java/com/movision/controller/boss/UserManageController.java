package com.movision.controller.boss;

import com.movision.common.Response;
import com.movision.facade.boss.GoodsListFacade;
import com.movision.facade.boss.PostFacade;
import com.movision.facade.boss.UserManageFacade;
import com.movision.facade.coupon.CouponFacade;
import com.movision.mybatis.collection.entity.Collection;
import com.movision.mybatis.comment.entity.CommentVo;
import com.movision.mybatis.coupon.entity.Coupon;
import com.movision.mybatis.coupon.entity.CouponVo;
import com.movision.mybatis.goods.entity.GoodsVo;
import com.movision.mybatis.pageHelper.entity.Datagrid;
import com.movision.mybatis.post.entity.PostList;
import com.movision.mybatis.record.entity.RecordVo;
import com.movision.mybatis.share.entity.Shares;
import com.movision.mybatis.share.entity.SharesVo;
import com.movision.mybatis.submission.entity.Submission;
import com.movision.mybatis.submission.entity.SubmissionVo;
import com.movision.mybatis.user.entity.UserAll;
import com.movision.mybatis.user.entity.UserParticulars;
import com.movision.mybatis.user.entity.UserVo;
import com.movision.mybatis.user.service.UserService;
import com.movision.utils.pagination.model.Paging;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @Author zhurui
 * @Date 2017/2/24 11:09
 */
@RestController
@RequestMapping(value = "/boss/userManage")
public class UserManageController {

    @Autowired
    UserManageFacade userManageFacade;

    @Autowired
    CouponFacade couponFacade;

    @Autowired
    PostFacade postFacade;

    @Autowired
    GoodsListFacade goodsListFacade;

    /**
     * 查看vip申请列表
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "查看vip申请列表", notes = "用于查看VIP申请列表", response = Response.class)
    @RequestMapping(value = "query_apply_vip_list", method = RequestMethod.POST)
    public Response queryApplyVipList(@RequestParam(required = false, defaultValue = "1") String pageNo,
                                      @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<UserVo> pager = new Paging(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<UserVo> list = userManageFacade.queryApplyVipList(pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;

    }

    /**
     * 查看vip列表
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "查看VIP列表", notes = "用于查看VIP用户列表", response = Response.class)
    @RequestMapping(value = "query_vip_list", method = RequestMethod.POST)
    public Response queryVipList(@RequestParam(required = false, defaultValue = "1") String pageNo,
                                 @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<UserVo> pager = new Paging<UserVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<UserVo> list = userManageFacade.queryVipList(pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }

    /**
     * 根据条件查看VIP列表
     *
     * @param name
     * @param phone
     * @param authstatus
     * @param begintime
     * @param endtime
     * @param pai
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "根据条件查看VIP列表", notes = "根据条件用于查看VIP用户列表", response = Response.class)
    @RequestMapping(value = "query_condition_vip_list", method = RequestMethod.POST)
    public Response queryByConditionvipList(@ApiParam(value = "用户名") @RequestParam(required = false) String name,
                                            @ApiParam(value = "手机号") @RequestParam(required = false) String phone,
                                            @ApiParam(value = "实名认证0失败1成功2驳回3未认证") @RequestParam(required = false) String authstatus,
                                            @ApiParam(value = "加V日期-开始时间") @RequestParam(required = false) String begintime,
                                            @ApiParam(value = "加V日期-结束时间") @RequestParam(required = false) String endtime,
                                            @ApiParam(value = "排序方式1 按加V时间拍，2按会员等级排") @RequestParam(required = false) String pai,
                                            @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                            @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<UserVo> pager = new Paging<UserVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<UserVo> list = userManageFacade.queryByConditionvipList(name, phone, authstatus, begintime, endtime, pai, pager);
        pager.result(list);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(pager);
        return response;
    }

    /**
     * 投稿说明
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "投稿说明", notes = "用于投稿说明接口", response = Response.class)
    @RequestMapping(value = "query_contribute_bounce", method = RequestMethod.POST)
    public Response queryContributeBounce(@ApiParam(value = "投稿id") @RequestParam String id) {
        Response response = new Response();
        Submission sub = userManageFacade.queryContributeBounce(id);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(sub);
        return response;
    }


    /**
     * 对VIP列表排序
     *
     * @param time
     * @param grade
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "VIP用户列表排序", notes = "用于VIP用户列表按加V时间和会员等级排序", response = Response.class)
    @RequestMapping(value = "query_add_v_sort", method = RequestMethod.POST)
    public Response queryAddVSortUser(@ApiParam(value = "按加V时间(传数值1)") @RequestParam(required = false) String time,
                                      @ApiParam(value = "按会员等级（传数值1）") @RequestParam(required = false) String grade,
                                      @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                      @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<UserVo> pager = new Paging<UserVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<UserVo> list = userManageFacade.queryAddVSortUser(time, grade, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }


    /**
     * 条件查询VIP申请用户列表
     *
     * @param username
     * @param phone
     * @param begintime
     * @param endtime
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "条件查询VIP申请用户列表", notes = "用于根据条件查询VIP申请用户列表接口", response = Response.class)
    @RequestMapping(value = "query_unite_condition_apply", method = RequestMethod.POST)
    public Response queryUniteConditionByApply(@ApiParam(value = "用户名") @RequestParam(required = false) String username,
                                               @ApiParam(value = "手机号") @RequestParam(required = false) String phone,
                                               @ApiParam(value = "实名认证") @RequestParam(required = false) String authstatus,
                                               @ApiParam(value = "开始时间") @RequestParam(required = false) String begintime,
                                               @ApiParam(value = "结束时间") @RequestParam(required = false) String endtime,
                                               @ApiParam(value = "排序方式 1按加V时间拍 2按会员等级排") @RequestParam(required = false) String type,
                                               @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                               @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<UserVo> pager = new Paging<UserVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<UserVo> list = userManageFacade.queryUniteConditionByApply(username, phone, authstatus, begintime, endtime, type, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }

    /**
     * 条件查询用户VIP审核列表
     *
     * @return
     */
    @ApiOperation(value = "条件查询用户VIP审核列表", notes = "用于查询用户VIP审核状态列表", response = Response.class)
    @RequestMapping(value = "query_user_examine_and_verify", method = RequestMethod.POST)
    public Response queryUserExamineAndVerify(@ApiParam(value = "用户名") @RequestParam(required = false) String username,
                                              @ApiParam(value = "手机号") @RequestParam(required = false) String phone,
                                              @ApiParam(value = "实名认证") @RequestParam(required = false) String authstatus,
                                              @ApiParam(value = "开始时间") @RequestParam(required = false) String begintime,
                                              @ApiParam(value = "结束时间") @RequestParam(required = false) String endtime,
                                              @ApiParam(value = "排序方式 1按加V时间拍 2按会员等级排") @RequestParam(required = false) String type,
                                              @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                              @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<UserVo> pager = new Paging<UserVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<UserVo> list = userManageFacade.queryUserExamineAndVerify(username, phone, authstatus, begintime, endtime, type, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }

    /**
     * 投稿列表审核
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "对投稿做审核操作", notes = "用于对某个投稿审核", response = Response.class)
    @RequestMapping(value = "update_contribute_audit", method = RequestMethod.POST)
    public Response updateContributeAudit(@ApiParam(value = "投稿id") @RequestParam String id, @ApiParam(value = "审核状态（0 待审核 1 审核通过 2 审核未通过）") @RequestParam String status) {
        Response response = new Response();
        int i = userManageFacade.update_contribute_audit(id, status);
        if (response.getCode() == 200) {
            response.setMessage("操作成功");
        }
        response.setData(i);
        return response;
    }

    /**
     * 逻辑删除投稿
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "逻辑删除投稿", notes = "用于删除投稿列表中指定投稿", response = Response.class)
    @RequestMapping(value = "delete_contribute", method = RequestMethod.POST)
    public Response deleteContributeById(@ApiParam(value = "投稿id") @RequestParam String id) {
        Response response = new Response();
        int i = userManageFacade.deleteContributeById(id);
        if (response.getCode() == 200) {
            response.setMessage("操作成功");
        }
        response.setData(i);
        return response;
    }

    /**
     * 查询所有用户列表
     * @param nickname
     * @param phone
     * @param authentication
     * @param vip
     * @param seal
     * @param begintime
     * @param endtime
     * @param pointsSort
     * @param postsumSort
     * @param isessenceSort
     * @param fansSort
     * @param conditionon
     * @param conditiontwo
     * @param price
     * @param login
     * @param pai
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "查询所有用户列表", notes = "用于查询所有用户列表接口", response = Response.class)
    @RequestMapping(value = "query_all_user", method = RequestMethod.POST)
    public Response queryAllUserList(@ApiParam(value = "用户名") @RequestParam(required = false) String nickname,
                                     @ApiParam(value = "手机号") @RequestParam(required = false) String phone,
                                     @ApiParam(value = "实名认证") @RequestParam(required = false) String authentication,
                                     @ApiParam(value = "是否是vip(1是0不是)") @RequestParam(required = false) String vip,
                                     @ApiParam(value = "是否封号") @RequestParam(required = false) String seal,
                                     @ApiParam(value = "注册开始时间") @RequestParam(required = false) String begintime,
                                     @ApiParam(value = "注册结束时间") @RequestParam(required = false) String endtime,
                                     @ApiParam(value = "积分排序0升序1降序") @RequestParam(required = false) String pointsSort,
                                     @ApiParam(value = "帖子数量排序0升序1降序") @RequestParam(required = false) String postsumSort,
                                     @ApiParam(value = "精贴排序0升序1降序") @RequestParam(required = false) String isessenceSort,
                                     @ApiParam(value = "关注排序0升序1降序") @RequestParam(required = false) String fansSort,
                                     @ApiParam(value = "条件1(积分：1，优惠券2，帖子3，精贴4，关注5)") @RequestParam(required = false) String conditionon,
                                     @ApiParam(value = "条件2(大于：3，等于：2，小于：1)") @RequestParam(required = false) String conditiontwo,
                                     @ApiParam(value = "值") @RequestParam(required = false) String price,
                                     @ApiParam(value = "登录方式") @RequestParam(required = false) String login,
                                     @ApiParam(value = "排序方式  1按注册时间拍 2按会员等级排") @RequestParam(required = false) String pai,
                                     @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                     @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<UserAll> pag = new Paging<UserAll>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        Map list = userManageFacade.queryAllUserList(nickname, phone, authentication, vip, seal, begintime, endtime, pointsSort,
                postsumSort, isessenceSort, fansSort, conditionon, conditiontwo, price, login, pai, pag);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(list);
        return response;
    }

    /**
     * 对用户进行封号处理
     *
     * @param userid
     * @return
     */
    @ApiOperation(value = "账号封号/解封", notes = "用于对用户账号做封号处理", response = Response.class)
    @RequestMapping(value = "delete_user_id", method = RequestMethod.POST)
    public Response deleteUserByid(@ApiParam(value = "用户id") @RequestParam String userid,
                                   @ApiParam(value = "(传1封号，0解封)") @RequestParam String type) {
        Response response = new Response();
        int i = userManageFacade.deleteUserByid(userid, type);
        if (response.getCode() == 200) {
            response.setMessage("操作成功");
        }
        response.setData(i);
        return response;
    }

    /**
     * 用于对用户进行加V去V 操作
     *
     * @param userid
     * @param type
     * @return
     */
    @ApiOperation(value = "账号加V/去V", notes = "用于账号加V/去V接口", response = Response.class)
    @RequestMapping(value = "delete_user_levl", method = RequestMethod.POST)
    public Response deleteUserLevl(@ApiParam(value = "用户id") @RequestParam String userid,
                                   @ApiParam(value = "(传1加V，0去V)") @RequestParam String type) {
        Response response = new Response();
        int i = userManageFacade.deleteUserLevl(userid, type);
        if (response.getCode() == 200) {
            response.setMessage("操作成功");
        }
        response.setData(i);
        return response;
    }

    /**
     * 用户加V审核
     *
     * @param userid
     * @param status
     * @param cause
     * @return
     */
    @ApiOperation(value = "用户加V审核", notes = "用于用户加V审核接口", response = Response.class)
    @RequestMapping(value = "update_user_vip_audit", method = RequestMethod.POST)
    public Response updateVipAudit(@ApiParam(value = "用户id") @RequestParam String userid,
                                   @ApiParam(value = "登录用户") @RequestParam String loginid,
                                   @ApiParam(value = "申请表id") @RequestParam String applyid,
                                   @ApiParam(value = "审核状态 0通过 1未通过") @RequestParam String status,
                                   @ApiParam(value = "驳回原因") @RequestParam(required = false) String cause) {
        Response response = new Response();
        int i = userManageFacade.updateVipAudit(userid, loginid, applyid, status, cause);
        if (response.getCode() == 200) {
            response.setMessage("操作成功");
        }
        response.setData(i);
        return response;
    }

    /**
     * 对作者推荐、取消推荐
     * @param id
     * @return
     */
    @ApiOperation(value = "对作者推荐、取消推荐", notes = "用于对作者推荐、取消推荐操作接口", response = Response.class)
    @RequestMapping(value = "updateUserByIsrecommend", method = RequestMethod.POST)
    public Response updateUserByIsrecommend(@ApiParam(value = "用户id") @RequestParam String id) {
        Response response = new Response();
        Map map = userManageFacade.updateUserByIsrecommend(id);
        response.setMessage("操作成功");
        response.setData(map);
        return response;
    }

    /**
     * 查询用户积分流水列表
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "查询用户积分列表", notes = "用于查询用户积分记录列表接口", response = Response.class)
    @RequestMapping(value = "query_integral_list", method = RequestMethod.POST)
    public Response queryIntegralList(@ApiParam(value = "用户id") @RequestParam String userid,
                                      @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                      @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<RecordVo> pager = new Paging(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<RecordVo> list = userManageFacade.queryIntegralList(userid, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }

    /**
     * 查询用户优惠券列表
     *
     * @param userid
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "查询用户优惠券列表", notes = "用于查询用户优惠券列表接口", response = Response.class)
    @RequestMapping(value = "query_discount_coupon", method = RequestMethod.POST)
    public Response queryDiscountCouponList(@ApiParam(value = "用户id") @RequestParam String userid,
                                            @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                            @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<CouponVo> pager = new Paging(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<CouponVo> list = couponFacade.queryDiscountCouponList(userid, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }

    /**
     * 查询用户详情
     *
     * @param userid
     * @return
     */
    @ApiOperation(value = "查询用户详情", notes = "用于插卡用户详情页面数据接口", response = Response.class)
    @RequestMapping(value = "query_user_particulars", method = RequestMethod.POST)
    public Response queryUserParticulars(@ApiParam(value = "用户id") @RequestParam String userid) {
        Response response = new Response();
        UserParticulars user = userManageFacade.queryUserParticulars(userid);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(user);
        return response;
    }

    /**
     * 根据用户id查询用户列表
     *
     * @param userids
     * @return
     */
    @ApiOperation(value = "根据用户id查询用户列表", notes = "用于根据多个用户id查询用户列表", response = Response.class)
    @RequestMapping(value = "query_user_ids", method = RequestMethod.POST)
    public Response queryUserByIds(@ApiParam(value = "用户id(以逗号分隔)") @RequestParam String userids) {
        Response response = new Response();
        List<UserAll> list = userManageFacade.queryUserByIds(userids);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(list);
        return response;
    }

    /**
     * 根据用户id查询帖子列表
     *
     * @param userid
     * @param type
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "根据用户id查询帖子列表", notes = "用于根据用户id查询帖子列表接口", response = Response.class)
    @RequestMapping(value = "query_post_userid", method = RequestMethod.POST)
    public Response queryPostByUserid(@ApiParam(value = "用户id") @RequestParam String userid,
                                      @ApiParam(value = "查询帖子：不用填，查询精贴：2") @RequestParam(required = false) String type,
                                      @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                      @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<PostList> pager = new Paging<PostList>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<PostList> list = postFacade.queryPostListByUserid(userid, type, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }

    //------------------------------收藏------------------------------------

    /**
     * 根据用户id查询用户被收藏的帖子列表
     *
     * @param userid
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "根据用户id查询用户被收藏的帖子列表", notes = "用于根据用户id查询用户被收藏的帖子列表接口", response = Response.class)
    @RequestMapping(value = "query_by_collection_userid", method = RequestMethod.POST)
    public Response queryByCollectionListByUserid(@ApiParam(value = "用户id") @RequestParam String userid,
                                                @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                                @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<PostList> pager = new Paging<PostList>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<PostList> list = postFacade.queryCollectionListByUserid(userid, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }


    /**
     * 根据用户id查询用户收藏的商品列表
     *
     * @param goodsid
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "根据用户id查询用户收藏的商品列表", notes = "根据用户id查询用户收藏的商品列表", response = Response.class)
    @RequestMapping(value = "query_collection_goods_userid", method = RequestMethod.POST)
    public Response queryCollectionGoodsListByUserid(@ApiParam(value = "商品id") @RequestParam String goodsid,
                                                     @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                                     @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<GoodsVo> pager = new Paging<GoodsVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<GoodsVo> list = goodsListFacade.queryCollectionGoodsListByUserid(goodsid, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }


    /**
     * 查询收藏的帖子列表
     *
     * @param userid
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "查询收藏帖子列表", notes = "用于根据用户id查询收藏的帖子列表", response = Response.class)
    @RequestMapping(value = "query_collect_post_list", method = RequestMethod.POST)
    public Response queryCollectPostList(@ApiParam(value = "用户id") @RequestParam String userid,
                                         @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                         @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<PostList> pager = new Paging<PostList>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<PostList> list = postFacade.queryCollectPostList(userid, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }

    //---------------------------------------分享-------------------------------------------

    /**
     * 根据用户id查询用户帖子被分享的分享列表
     *
     * @param userid
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "根据用户id查询用户帖子被分享的分享列表", notes = "用于根据用户id查询用户帖子被分享的分享列表接口", response = Response.class)
    @RequestMapping(value = "query_share_by_post_list", method = RequestMethod.POST)
    public Response queryBySharePostList(@ApiParam(value = "用户id") @RequestParam String userid,
                                       @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                       @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<SharesVo> pager = new Paging<SharesVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<SharesVo> list = postFacade.querySharePostList(userid, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }

    /**
     * 查询用户分享帖子的分享列表
     *
     * @param userid
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "查询用户分享帖子的分享列表", notes = "用于查询用户分享帖子的分享列表接口", response = Response.class)
    @RequestMapping(value = "query_user_share_post_list", method = RequestMethod.POST)
    public Response queryUsersSharePostsList(@ApiParam(value = "用户id") @RequestParam String userid,
                                             @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                             @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<SharesVo> pager = new Paging<SharesVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<SharesVo> list = postFacade.queryUsersSharePostsList(userid, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }


    //--------------------------------------------评论-----------------------------------------------

    /**
     * 根据用户id查询用户帖子被评论的评论列表
     *
     * @param userid
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "根据用户id查询用户帖子被评论的评论列表", notes = "用于根据用户id查询用户帖子被评论的评论列表接口", response = Response.class)
    @RequestMapping(value = "query_comment_by_userid", method = RequestMethod.POST)
    public Response queryByCommentListByUserid(@ApiParam(value = "用户id") @RequestParam String userid,
                                             @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                             @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<CommentVo> pager = new Paging<CommentVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<CommentVo> list = userManageFacade.queryCommentListByUserid(userid, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }

    /**
     * 查询用户评论帖子的评论列表
     *
     * @param userid
     * @param pageNo
     * @param pageSize
     * @return
     */
    @ApiOperation(value = "查询用户评论帖子的评论列表", notes = "用于根据用户id查询评论的帖子评论列表接口", response = Response.class)
    @RequestMapping(value = "query_the_user_comments", method = RequestMethod.POST)
    public Response queryTheUserComments(@ApiParam(value = "用户id") @RequestParam String userid,
                                         @ApiParam(value = "当前页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                         @ApiParam(value = "每页几条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();
        Paging<CommentVo> pager = new Paging<CommentVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<CommentVo> list = userManageFacade.queryTheUserComments(userid, pager);
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        pager.result(list);
        response.setData(pager);
        return response;
    }


}
