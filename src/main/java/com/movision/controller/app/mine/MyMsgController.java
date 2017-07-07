package com.movision.controller.app.mine;

import com.movision.common.Response;
import com.movision.common.util.ShiroUtil;
import com.movision.facade.msgCenter.MsgCenterFacade;
import com.movision.mybatis.PostZanRecord.entity.ZanRecordVo;
import com.movision.mybatis.comment.entity.CommentVo;
import com.movision.mybatis.imFirstDialogue.entity.ImFirstDialogueVo;
import com.movision.mybatis.imSystemInform.entity.ImSystemInform;
import com.movision.mybatis.imSystemInform.entity.ImSystemInformVo;
import com.movision.mybatis.rewarded.entity.RewardedVo;
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
 * APP我的消息中心
 *
 * @Author zhuangyuhao
 * @Date 2017/4/5 13:46
 */
@RestController
@RequestMapping("app/mine/msg")
public class MyMsgController {

    @Autowired
    private MsgCenterFacade msgCenterFacade;

    @ApiOperation(value = "获取我的消息中心列表", notes = "获取我的消息中心列表", response = Response.class)
    @RequestMapping(value = {"/get_my_msg_center_list"}, method = RequestMethod.GET)
    public Response getMyMsgCenterList() {

        Response response = new Response();
        Map map = msgCenterFacade.getMsgCenterList(ShiroUtil.getAppUserID());
        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(map);
        return response;
    }

    @ApiOperation(value = "获取我的消息中心的系统通知列表", notes = "获取我的消息中心的系统通知列表", response = Response.class)
    @RequestMapping(value = {"/get_my_msg_center_system_list"}, method = RequestMethod.GET)
    public Response getMyMsgCenterInformationList(@ApiParam(value = "第几页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                                  @ApiParam(value = "每页多少条") @RequestParam(required = false, defaultValue = "10") String pageSize
    ) {
        Response response = new Response();
        Paging<ImSystemInformVo> paging = new Paging<ImSystemInformVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<ImSystemInformVo> list = msgCenterFacade.getMsgInformationList(ShiroUtil.getAppUserID(), ShiroUtil.getAppUser().getRegisterTime(), paging);
        paging.result(list);
        response.setData(paging);
        return response;
    }

    @ApiOperation(value = "获取我的消息中心的评论列表", notes = "获取我的消息中心的评论列表", response = Response.class)
    @RequestMapping(value = {"/get_my_msg_center_comment_list"}, method = RequestMethod.GET)
    public Response getMyMsgCenterCommentList(@ApiParam(value = "第几页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                              @ApiParam(value = "每页多少条") @RequestParam(required = false, defaultValue = "10") String pageSize
    ) {
        Response response = new Response();
        Paging<CommentVo> paging = new Paging<CommentVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<CommentVo> list = msgCenterFacade.getMsgCommentList(ShiroUtil.getAppUserID(), paging);
        paging.result(list);
        response.setData(paging);
        return response;
    }

    @ApiOperation(value = "获取我的消息中心的打赏列表", notes = "获取我的消息中心的打赏列表", response = Response.class)
    @RequestMapping(value = {"/get_my_msg_center_rewarded_list"}, method = RequestMethod.GET)
    public Response getMyMsgCenterRewardedList(@ApiParam(value = "第几页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                               @ApiParam(value = "每页多少条") @RequestParam(required = false, defaultValue = "10") String pageSize
    ) {
        Response response = new Response();
        Paging<RewardedVo> paging = new Paging<RewardedVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<RewardedVo> list = msgCenterFacade.getMsgRewardedList(ShiroUtil.getAppUserID(), paging);
        paging.result(list);
        response.setData(paging);
        return response;
    }


    @ApiOperation(value = "获取我的消息中心的赞列表", notes = "获取我的消息中心的赞列表", response = Response.class)
    @RequestMapping(value = {"/get_my_msg_center_zan_list"}, method = RequestMethod.GET)
    public Response findAllZan(@ApiParam(value = "第几页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                               @ApiParam(value = "每页多少条") @RequestParam(required = false, defaultValue = "10") String pageSize
    ) {
        Response response = new Response();
        Paging<ZanRecordVo> paging = new Paging<ZanRecordVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<ZanRecordVo> list = msgCenterFacade.findAllZan(ShiroUtil.getAppUserID(), paging);
        paging.result(list);
        response.setData(paging);
        return response;
    }


    @ApiOperation(value = "获取我的消息中心的打招呼列表", notes = "获取我的消息中心的打招呼列表", response = Response.class)
    @RequestMapping(value = {"/get_my_msg_center_dialogue_list"}, method = RequestMethod.GET)
    public Response findAllDialogue(@ApiParam(value = "第几页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                    @ApiParam(value = "每页多少条") @RequestParam(required = false, defaultValue = "10") String pageSize
    ) {
        Response response = new Response();
        Paging<ImFirstDialogueVo> paging = new Paging<ImFirstDialogueVo>(Integer.valueOf(pageNo), Integer.valueOf(pageSize));
        List<ImFirstDialogueVo> list = msgCenterFacade.findAllDialogue(ShiroUtil.getAppUserID(), paging);
        paging.result(list);
        response.setData(paging);
        return response;
    }


    @ApiOperation(value = "判断已读未读", notes = "判断已读未读", response = Response.class)
    @RequestMapping(value = {"/get_my_msg_center_isread_list"}, method = RequestMethod.GET)
    public Response queryIsread(
            @ApiParam(value = "消息id") @RequestParam(required = false) Integer id
    ) {
        Response response = new Response();
        msgCenterFacade.queryIsread(ShiroUtil.getAppUserID(), id);
        return response;
    }

    /**
     * 更新已读/未读状态
     *
     * @param type
     * @return
     */
    @ApiOperation(value = "更新已读/未读状态", notes = "更新已读/未读状态", response = Response.class)
    @RequestMapping(value = {"/get_my_msg_center_update_read"}, method = RequestMethod.GET)
    public Response updateisread(@ApiParam(value = "更新类型 1：赞 2：打赏 3：评论 4：系统 5：打招呼") @RequestParam String type,
                                 @ApiParam(value = "用户id") @RequestParam String userid,
                                 @ApiParam(value = "推送唯一标识（类型为4时不为空）") @RequestParam(required = false) String informidentity) {
        Response response = new Response();
        Integer resault = msgCenterFacade.updateisread(type, Integer.parseInt(userid), informidentity);
        response.setData(resault);
        return response;
    }

    /**
     * 系统消息全部标记已读
     *
     * @return
     */
    @ApiOperation(value = "系统消息全部标记已读", notes = "用于系统消息全部标记已读接口", response = Response.class)
    @RequestMapping(value = "whole_Sign_Read", method = RequestMethod.GET)
    public Response wholeSignRead() {
        Response response = new Response();
        Integer resault = msgCenterFacade.wholeSignRead(ShiroUtil.getAppUserID());
        if (response.getCode() == 200) {
            response.setData("操作成功");
        }
        response.setData(resault);
        return response;
    }
}
