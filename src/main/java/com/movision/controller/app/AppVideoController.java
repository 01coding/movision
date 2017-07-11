package com.movision.controller.app;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;
import com.movision.common.Response;
import com.movision.common.constant.AliVideoConstant;
import com.movision.facade.apsaraVideo.AliVideoFacade;

import com.movision.utils.VideoUploadUtil;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.apache.commons.collections.map.HashedMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @Author zhuangyuhao
 * @Date 2017/5/19 16:41
 */
@RestController
@RequestMapping("app/video")
public class AppVideoController {

    @Autowired
    private AliVideoFacade aliVideoFacade;

    @Autowired
    private VideoUploadUtil videoUploadUtil;



    @ApiOperation(value = "获取视频播放凭证", notes = "获取视频播放凭证", response = Response.class)
    @RequestMapping(value = "get_video_play_auth", method = RequestMethod.POST)
    public Response getVideoPlayAuth(@ApiParam("vid, ali视频id") @RequestParam String vid) throws Exception {
        Response response = new Response();
        String url = aliVideoFacade.generateRequestUrl(AliVideoConstant.GetVideoPlayAuth, vid);
        Map<String, String> reMap = aliVideoFacade.doGet(url);
        Map result = new HashedMap();
        if (!reMap.isEmpty()) {
            if ("200".equals(reMap.get("status"))) {
                Gson gson = new Gson();
                result = gson.fromJson(reMap.get("result"), Map.class);
            }
        }
        response.setData(result);
        return response;
    }

    /**
     * 废弃不用
     * 原因：上传文件所在的绝对路径，获取不到
     *
     * @param fileName
     * @param title
     * @param description
     * @param coverimg
     * @param tatges
     * @return
     */
    @ApiOperation(value = "视频上传接口", notes = "视频上传接口", response = Response.class)
    @RequestMapping(value = "get_video_upload", method = RequestMethod.POST)
    public Response getVideoUpload(@ApiParam("上传文件所在的绝对路径(必须包含扩展名)") @RequestParam String fileName,
                                   @ApiParam("视频标题") @RequestParam String title,
                                   @ApiParam("视频描述") @RequestParam String description,
                                   @ApiParam("封面URL") @RequestParam String coverimg,
                                   @ApiParam("视频标签,多个用逗号分隔") @RequestParam String tatges) {
        Response response = new Response();
        String videoid = videoUploadUtil.videoUpload(fileName, title, description, coverimg, tatges);
        if (response.getCode() == 200) {
            response.setData(videoid);
            response.setMessage("调用成功");
        }
        return response;
    }


    @ApiOperation(value = "获取视频上传凭证和地址", notes = "获取视频上传凭证和地址", response = Response.class)
    @RequestMapping(value = "get_video_address", method = RequestMethod.POST)
    public Response createUploadVideo(
            @ApiParam("必选，视频源文件名称（必须带后缀, 支持 \"3GP\",\"AVI\",\"FLV\",\"MP4\",\"M3U8\",\"MPG\",\"ASF\",\"WMV\",\"MKV\",\"MOV\",\"TS\",    \"WebM\",\"MPEG\",\"RM\",\"RMVB\",\"DAT\",\"ASX\",\"WVX\",\"MPE\",\"MPA\",\"F4V\",\"MTS\",\"VOB\",\"GIF\"）") @RequestParam String fileName,
            @ApiParam("视频标题") @RequestParam String title,
            @ApiParam("视频描述") @RequestParam(required = false) String description,
            @ApiParam("视频标签,多个用逗号分隔") @RequestParam(required = false) String tatges,
            @ApiParam("必选，视频源文件字节数") @RequestParam long filesize) {
        Response response = new Response();
        DefaultAcsClient aliyunClient;
        aliyunClient = new DefaultAcsClient(
                DefaultProfile.getProfile("cn-shanghai", VideoUploadUtil.accessKeyId, VideoUploadUtil.accessKeySecret));

        Map videoid = VideoUploadUtil.createUploadVideo(aliyunClient, fileName, description, tatges, title, filesize);
        if (response.getCode() == 200) {
            response.setMessage("调用成功");
        }
        response.setData(videoid);
        return response;
    }


    @ApiOperation(value = " 刷新视频上传凭证", notes = " 刷新视频上传凭证", response = Response.class)
    @RequestMapping(value = "get_video_refresh", method = RequestMethod.POST)
    public Response refreshUploadVideo(
            @ApiParam("视频唯一id") @RequestParam String videoid) {
        Response response = new Response();
        DefaultAcsClient aliyunClient;
        aliyunClient = new DefaultAcsClient(
                DefaultProfile.getProfile("cn-shanghai", VideoUploadUtil.accessKeyId, VideoUploadUtil.accessKeySecret));
        VideoUploadUtil.refreshUploadVideo(aliyunClient, videoid);
        return response;
    }


    /**
     * 删除视频
     *
     * @param videoid
     * @return
     */
    @ApiOperation(value = " 删除视频", notes = " 删除视频", response = Response.class)
    @RequestMapping(value = "get_delete_video", method = RequestMethod.POST)
    public Response deleteVideo(@ApiParam("视频唯一id") @RequestParam String videoid) {
        Response response = new Response();
        String result = VideoUploadUtil.deleteVideo(videoid);
        if (response.getCode() == 200) {
            response.setMessage("删除成功");
        }
        response.setData(result);
        return response;
    }

    /**
     * 获取微信acctoken
     *
     * @param code
     * @return
     */
    @ApiOperation(value = " 获取微信acctoken", notes = " 获取微信acctoken", response = Response.class)
    @RequestMapping(value = "get_weixin_registic", method = RequestMethod.POST)
    public Response weixinRegistic(@ApiParam("code") @RequestParam String code) {
        Response response = new Response();
        Map result = VideoUploadUtil.weixinRegistic(code);
        if (response.getCode() == 200) {
            response.setMessage("获取成功");
        }
        response.setData(result);
        return response;
    }


    /**
     * 获取用户信息
     *
     * @param
     * @return
     */
    @ApiOperation(value = " 获取用户信息", notes = " 获取用户信息", response = Response.class)
    @RequestMapping(value = "get_user_information", method = RequestMethod.POST)
    public Response getUserInformation(@ApiParam("acctoken") @RequestParam String acctoken,
                                       @ApiParam("openid") @RequestParam String openid) {
        Response response = new Response();
        Map result = VideoUploadUtil.getUserInformation(acctoken, openid);
        if (response.getCode() == 200) {
            response.setMessage("获取成功");
        }
        response.setData(result);
        return response;
    }


    /**
     * 获取fuflshtoken
     *
     * @param
     * @return
     */
    @ApiOperation(value = " 获取fuflshtoken", notes = " 获取fuflshtoken", response = Response.class)
    @RequestMapping(value = "get_fulshtoken", method = RequestMethod.POST)
    public Response getrefulshtoken(@ApiParam("acctoken") @RequestParam String fuflshtoken,
                                    @ApiParam("openid") @RequestParam String openid) {
        Response response = new Response();
        Map result = VideoUploadUtil.getrefulshtoken(fuflshtoken, openid);
        if (response.getCode() == 200) {
            response.setMessage("获取成功");
        }
        response.setData(result);
        return response;
    }


    /**
     * 微信分享获取签名
     *
     * @param
     * @return
     */
    @ApiOperation(value = " 微信分享获取签名", notes = " 微信分享获取签名", response = Response.class)
    @RequestMapping(value = "get_ticket", method = RequestMethod.POST)
    public Response getticket(@ApiParam("acctoken") @RequestParam String acctoken) {
        Response response = new Response();
        Map result = VideoUploadUtil.getticket(acctoken);
        if (response.getCode() == 200) {
            response.setMessage("获取成功");
        }
        response.setData(result);
        return response;
    }
}
