package com.movision.controller.app;

import com.movision.common.Response;
import com.movision.common.util.ShiroUtil;
import com.movision.facade.boss.PostFacade;
import com.movision.facade.index.FacadeHeatValue;
import com.movision.facade.index.FacadePost;
import com.movision.mybatis.accusation.service.AccusationService;
import com.movision.mybatis.compressImg.entity.CompressImg;
import com.movision.mybatis.goods.entity.Goods;
import com.movision.mybatis.post.entity.ActiveVo;
import com.movision.mybatis.post.entity.Post;
import com.movision.mybatis.post.entity.PostVo;
import com.movision.utils.CoverImgCompressUtil;
import com.movision.utils.file.FileUtil;
import com.movision.utils.oss.AliOSSClient;
import com.movision.utils.oss.MovisionOssClient;
import com.movision.utils.pagination.model.Paging;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.omg.CORBA.PRIVATE_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @Author shuxf
 * @Date 2017/1/19 15:42
 */
@RestController
@RequestMapping("/app/post/")
public class AppPostController {

    @Autowired
    private FacadePost facadePost;


    @Autowired
    private MovisionOssClient movisionOssClient;


    @Autowired
    private PostFacade postFacade;

    @Autowired
    private FacadeHeatValue facadeHeatValue;

    @ApiOperation(value = "帖子详情数据返回接口", notes = "用于返回请求帖子详情内容", response = Response.class)
    @RequestMapping(value = "detail", method = RequestMethod.POST)
    public Response queryPostDetail(@ApiParam(value = "帖子id") @RequestParam String postid,
                                    @ApiParam(value = "用户id(登录状态下不可为空)") @RequestParam(required = false) String userid) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        Response response = new Response();

        PostVo post = facadePost.queryPostDetail(postid, userid);


        if (null != post) {
            response.setCode(200);
            response.setMessage("查询成功");
        }else if (null == post){
            response.setCode(300);
            response.setMessage("该帖已删除");
        }
        response.setData(post);
        return response;
    }

    /*@ApiOperation(value = "帖子详情数据返回接口（老版,只用于改版数据割接使用）", notes = "用于返回请求帖子详情内容（老版,只用于改版数据割接使用）", response = Response.class)
    @RequestMapping(value = "olddetail", method = RequestMethod.POST)
    public Response queryOldPostDetail(@ApiParam(value = "帖子id") @RequestParam String postid,
                                    @ApiParam(value = "用户id(登录状态下不可为空)") @RequestParam(required = false) String userid) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        Response response = new Response();

        PostVo post = facadePost.queryOldPostDetail(postid, userid);

        if (null != post) {
            response.setCode(200);
            response.setMessage("查询成功");
        }else if (null == post){
            response.setCode(300);
            response.setMessage("该帖已删除");
        }
        response.setData(post);
        return response;
    }*/

    @ApiOperation(value = "活动详情数据返回接口", notes = "用于返回请求活动详情内容", response = Response.class)
    @RequestMapping(value = "activeDetail", method = RequestMethod.POST)
    public Response queryActiveDetail(@ApiParam(value = "活动id") @RequestParam String postid,
                                      @ApiParam(value = "用户id(登录状态下不可为空)") @RequestParam(required = false) String userid,
                                      @ApiParam(value = "活动类型:0 告知类活动 1 商城促销类活动") @RequestParam String activetype) {

        Response response = new Response();

        ActiveVo active = facadePost.queryActiveDetail(postid, userid, activetype);

        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(active);
        return response;
    }

    @ApiOperation(value = "往期精选右上角点击选择的日期枚举", notes = "列出所有有内容的精选日期", response = Response.class)
    @RequestMapping(value = "queryDateSelect", method = RequestMethod.POST)
    public Response queryDateSelect(@ApiParam(value = "第几页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                    @ApiParam(value = "每页多少条") @RequestParam(required = false, defaultValue = "15") String pageSize) {
        Response response = new Response();

        Paging<Date> pager = new Paging<>(Integer.parseInt(pageNo), Integer.parseInt(pageSize));
        List<Date> dateList = facadePost.queryDateSelect(pager);
        pager.result(dateList);

        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(pager);
        return response;
    }

    @ApiOperation(value = "查询往期3天的精选帖子(含活动)列表", notes = "用于返回往期3天的精选帖子(含活动)列表的接口", response = Response.class)
    @RequestMapping(value = "pastPost", method = RequestMethod.POST)
    public Response queryPastPostDetail(@ApiParam(value = "查询日期") @RequestParam(required = false) String date) {
        Response response = new Response();


        Map<String, Object> postmap = facadePost.queryPastPostDetail(date);

        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(postmap);
        return response;
    }

    @ApiOperation(value = "查询某个圈子往期所有热帖列表", notes = "从圈子中点击“最受欢迎的内容”查询该圈子往期所有的热门帖子列表")
    @RequestMapping(value = "pastHotPostList", method = RequestMethod.POST)
    public Response pastHotPostList(@ApiParam(value = "圈子id") @RequestParam String circleid,
                                    @ApiParam(value = "第几页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                    @ApiParam(value = "每页多少条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();

        Paging<PostVo> pager = new Paging<>(Integer.parseInt(pageNo), Integer.parseInt(pageSize));
        List<PostVo> postlist = facadePost.pastHotPostList(pager, circleid);
        pager.result(postlist);

        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(pager);
        return response;
    }

    @ApiOperation(value = "APP端发帖前先判断权限", notes = "用于APP端发布帖子时判断用户权限的接口", response = Response.class)
    @RequestMapping(value = "checkReleasePostPermission", method = RequestMethod.POST)
    public Response checkReleasePostPermission(@ApiParam(value = "用户id") @RequestParam String userid,
                                               @ApiParam(value = "所属圈子id") @RequestParam String circleid) {
        Response response = new Response();

        int count = facadePost.checkReleasePostPermission(userid, circleid);

        if (count == 1) {
            response.setCode(200);
            response.setMessage("用户拥有发帖权限");
        } else if (count == -1) {
            response.setCode(300);
            response.setMessage("用户不具备发帖权限");
        }
        return response;
    }


    /*@ApiOperation(value = "APP端发布帖子", notes = "用于APP端发布帖子的接口", response = Response.class)
    @RequestMapping(value = "releasePost", method = RequestMethod.POST)
    public Response releasePost(HttpServletRequest request,
                                @ApiParam(value = "用户id") @RequestParam String userid,
                                @ApiParam(value = "帖子类型：0 普通图文帖 1 原生视频帖 2 分享视频贴( isactive为0时该字段不为空)") @RequestParam String type,
                                @ApiParam(value = "所属圈子id") @RequestParam String circleid,
                                @ApiParam(value = "帖子主标题(限18个字以内)") @RequestParam String title,
                                @ApiParam(value = "帖子内容") @RequestParam String postcontent,
                                @ApiParam(value = "是否为活动：0 帖子 1 活动") @RequestParam String isactive,
                                @ApiParam(value = "帖子封面图片") @RequestParam MultipartFile coverimg,
                                @ApiParam(value = "原生视频上传到阿里云后的vid（type为1时必填）") @RequestParam(required = false) String vid,
                                @ApiParam(value = "第三方视频地址url（type为2时必填，直接把分享的第三方视频网址传到这里）") @RequestParam(required = false) String videourl,
                                @ApiParam(value = "分享的产品id(多个商品用英文逗号,隔开)") @RequestParam(required = false) String proids
    ) {
        Response response = new Response();

        Map count = facadePost.releasePost(request, userid, type, circleid, title, postcontent, isactive, coverimg, vid, videourl, proids);

        if (count.get("flag").equals(-2)) {
            response.setCode(300);
            response.setMessage("系统异常，APP发帖失败");
        } else if (count.get("flag").equals(-1)) {
            response.setCode(201);
            response.setMessage("用户不具备发帖权限");
        }else{
            response.setCode(200);
            response.setMessage("发帖成功");
        }
        return response;
    }*/

    /*@ApiOperation(value = "PC官网发布帖子", notes = "用于官网发布帖子的接口", response = Response.class)
    @RequestMapping(value = "releasePostByPC", method = RequestMethod.POST)
    public Response releasePostByPC(HttpServletRequest request,
                                    @ApiParam(value = "用户id") @RequestParam String userid,
                                    @ApiParam(value = "帖子类型：0 普通图文帖 1 原生视频帖 2 分享视频贴( isactive为0时该字段不为空)") @RequestParam String type,
                                    @ApiParam(value = "所属圈子id") @RequestParam String circleid,
                                    @ApiParam(value = "帖子主标题(限18个字以内)") @RequestParam String title,
                                    @ApiParam(value = "帖子内容") @RequestParam String postcontent,
                                    @ApiParam(value = "是否为活动：0 帖子 1 活动") @RequestParam String isactive,
                                    @ApiParam(value = "原生视频上传到阿里云后的vid（type为1时必填）") @RequestParam(required = false) String vid,
                                    @ApiParam(value = "第三方视频地址url（type为2时必填，直接把分享的第三方视频网址传到这里）") @RequestParam(required = false) String videourl,
                                    @ApiParam(value = "分享的产品id(多个商品用英文逗号,隔开)") @RequestParam(required = false) String proids,
                                    @RequestParam(value = "file", required = false) MultipartFile file,
                                    @ApiParam(value = "X坐标") @RequestParam String x,
                                    @ApiParam(value = "Y坐标") @RequestParam String y,
                                    @ApiParam(value = "宽") @RequestParam String w,
                                    @ApiParam(value = "高") @RequestParam String h) {
        Response response = new Response();

        Map count = facadePost.releasePostByPC(request, userid, type, circleid, title, postcontent, isactive, file, vid, videourl, proids, x, y, w, h);

        if (count.get("flag").equals(-2)) {
            response.setCode(300);
            response.setMessage("系统异常，APP发帖失败");
        } else if (count.get("flag").equals(-1)) {
            response.setCode(201);
            response.setMessage("用户不具备发帖权限");
        }else{
            response.setCode(200);
            response.setMessage("发帖成功");
        }
        return response;
    }*/


    @ApiOperation(value = "PC官网发布帖子(改版)", notes = "用于官网发布帖子的接口（改版）", response = Response.class)
    @RequestMapping(value = "releasePostByPC_Test", method = RequestMethod.POST)
    public Response releasePostByPCTest(HttpServletRequest request,
                                        @ApiParam(value = "用户id") @RequestParam String userid,
                                        @ApiParam(value = "所属圈子id") @RequestParam String circleid,
                                        @ApiParam(value = "帖子主标题(限18个字以内)") @RequestParam String title,
                                        @ApiParam(value = "帖子内容") @RequestParam String postcontent,
                                        @ApiParam(value = "帖子封面") @RequestParam String coverimg,
                                        @ApiParam(value = "分享的产品id(多个商品用英文逗号,隔开)") @RequestParam(required = false) String proids) {
        Response response = new Response();

        Map count = facadePost.releasePostByPCTest(request, userid, circleid, title, postcontent, coverimg, proids);

        if (count.get("flag").equals(-2)) {
            response.setCode(300);
            response.setMessage("系统异常，APP发帖失败");
        } else if (count.get("flag").equals(-1)) {
            response.setCode(201);
            response.setMessage("用户不具备发帖权限");
        } else {
            response.setCode(200);
            response.setMessage("发帖成功");
        }
        return response;
    }

    /**
     * PC官网上传帖子封面图片
     *
     * @param file
     * @return
     */
    @ApiOperation(value = "PC官网上传帖子封面图片（改版）", notes = "PC官网上传帖子封面图片（改版）", response = Response.class)
    @RequestMapping(value = {"/updateCoverImgByPC"}, method = RequestMethod.POST)
    public Response updateCoverImgByPC(@RequestParam(value = "file", required = false) MultipartFile file,
                                       @ApiParam(value = "X坐标") @RequestParam(required = false) String x,
                                       @ApiParam(value = "Y坐标") @RequestParam(required = false) String y,
                                       @ApiParam(value = "宽") @RequestParam String w,
                                       @ApiParam(value = "高") @RequestParam String h,
                                       @ApiParam(value = "1帖子封面 2活动方形图") @RequestParam String type) {
        Map map = facadePost.updateCoverImgByPC(file, x, y, w, h, type);
        return new Response(map);
    }

    /**
     * 上传帖子相关图片
     *
     * @param file
     * @return
     */
    @ApiOperation(value = "PC官网上传帖子相关图片（改版）", notes = "上传帖子相关图片", response = Response.class)
    @RequestMapping(value = {"/upload_post_img_test"}, method = RequestMethod.POST)
    public Response updatePostImgTest(@RequestParam(value = "file", required = false) MultipartFile[] file) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < file.length; i++) {
            Map map = postFacade.updatePostImgTest(file[i]);
            list.add(map);
        }
        return new Response(list);
    }


/*    @ApiOperation(value = "图片压缩", notes = "用于图片压缩", response = Response.class)
    @RequestMapping(value = "coverImgCompressUtil", method = RequestMethod.POST)
    public Response coverImgCompressUtil(@ApiParam(value = "上传文件") @RequestParam MultipartFile file,
                                         @ApiParam(value = "要求文件宽") @RequestParam String w,
                                         @ApiParam(value = "要求文件高") @RequestParam String h) {
        Response response = new Response();
        *//*int w = 750;//图片压缩后的宽度
        int h = 440;//图片压缩后的高度440*//*
        String str = coverImgCompressUtil.ImgCompress(file, Integer.parseInt(w), Integer.parseInt(h));
        response.setMessage("操作成功");
        response.setData(str);
        return response;
    }*/


    /**
     * 修改上架
     *
     * @return
     */
    @ApiOperation(value = "修改上架", notes = "修改上架", response = Response.class)
    @RequestMapping(value = {"/update_post_isdel"}, method = RequestMethod.POST)
    public Response updatePostIsdel(@ApiParam(value = "返回的vid") @RequestParam String vid) {
        Response response = new Response();
        int isdel = facadePost.updatePostIsdel(vid);
        if (response.getCode() == 200) {
            response.setMessage("修改成功");
            response.setData(isdel);
        }
        return response;
    }

    /**
     * 上传帖子内容相关图片
     *
     * @param file
     * @return
     */
    @ApiOperation(value = "上传帖子内容相关图片", notes = "上传帖子内容相关图片（上传帖子内容中的图片）", response = Response.class)
    @RequestMapping(value = {"/upload_post_img"}, method = RequestMethod.POST)
    public Response updatePostImg(@RequestParam(value = "file", required = false) MultipartFile file) {
        Map m = movisionOssClient.uploadObject(file, "img", "post");
        String url = String.valueOf(m.get("url"));
        Map<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("name", FileUtil.getFileNameByUrl(url));
        map.put("width", m.get("width"));
        map.put("height", m.get("height"));
        return new Response(map);

        /**Map m = new HashMap();
        String url = "";
        Map<String, Object> map = new HashMap<>();
        Map compressmap = null;
        if (type.equals("1")) {
            /**  m = movisionOssClient.uploadObject(file, "img", "postCover");
             url = String.valueOf(m.get("url"));
             //4对本地服务器中切割好的图片进行压缩处理
             int wt = 750;//图片压缩后的宽度
             int ht = 440;//图片压缩后的高度440
             String compressUrl = coverImgCompressUtil.ImgCompress(url, wt, ht);
             System.out.println("压缩完的切割图片url==" + compressUrl);
             //5对压缩完的图片上传到阿里云
         compressmap = aliOSSClient.uploadInciseStream(compressUrl, "img", "coverIncise");
            compressmap = facadePost.uploadPostFacePic(file);
            map.put("compressmap", compressmap);
        } else if (type.equals("2")) {
            m = movisionOssClient.uploadObject(file, "img", "post");
            url = String.valueOf(m.get("url"));
            map.put("url", url);
            map.put("name", FileUtil.getFileNameByUrl(url));
            map.put("width", m.get("width"));
            map.put("height", m.get("height"));
        }
         return new Response(map);*/
    }

    /**
     * 上传帖子封面相关图片
     *
     * @param file
     * @return
     */
    @ApiOperation(value = "App上传帖子封面相关图片", notes = "上传帖子封面相关图片（上传帖子封面相关图片）", response = Response.class)
    @RequestMapping(value = {"/upload_postface_img"}, method = RequestMethod.POST)
    public Response updatePostImgFace(@RequestParam(value = "file", required = false) MultipartFile file
    ) {

        Map<String, Object> map = new HashMap<>();
        Map compressmap = facadePost.uploadPostFacePic(file);
        map.put("compressmap", compressmap);

        return new Response(map);
    }

    @ApiOperation(value = "APP端删帖", notes = "用于APP端删除帖子的接口", response = Response.class)
    @RequestMapping(value = "delPost", method = RequestMethod.POST)
    public Response delPost(@ApiParam(value = "当前登录的用户id") @RequestParam String userid,
                            @ApiParam(value = "帖子id") @RequestParam String postid) {
        Response response = new Response();

        int flag = facadePost.delPost(userid, postid);

        if (flag == 1) {
            response.setCode(200);
            response.setMessage("删除成功");
        } else if (flag == 0) {
            response.setCode(300);
            response.setMessage("用户不具备删除权限");
        }
        return response;
    }


    @ApiOperation(value = "发帖选择推荐商品接口", notes = "APP发布普通帖选择推荐商品时调用此接口，选择收藏的商品列表和全部商品列表", response = Response.class)
    @RequestMapping(value = "recommendGoodsList", method = RequestMethod.POST)
    public Response recommendGoodsList(@ApiParam(value = "用户id(如果调用该接口，说明已经登录，如果未登录是不会调转到发帖编辑页的，所以必填)") @RequestParam String userid,
                                       @ApiParam(value = "第几页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                       @ApiParam(value = "每页条数") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();

        Map<String, Object> map = facadePost.queryRecommendGoodsList(userid, pageNo, pageSize);

        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(map);
        return response;
    }

    @ApiOperation(value = "APP端通过缩略图url请求原图url和图片大小接口", notes = "APP端通过缩略图url请求原图url和图片大小接口", response = Response.class)
    @RequestMapping(value = "getProtoImg", method = RequestMethod.POST)
    public Response getProtoImg(@ApiParam(value = "帖子中图片的缩略图url") @RequestParam String imgurl) {
        Response response = new Response();

        CompressImg compressImg = facadePost.getProtoImg(imgurl);

        if (response.getCode() == 200) {
            response.setMessage("查询成功");
            response.setData(compressImg);
        } else {
            response.setMessage("查询失败");
        }
        return response;
    }

    /**
     * 帖子点赞接口
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "更新帖子点赞次数", notes = "用于帖子点赞接口", response = Response.class)
    @RequestMapping(value = "updateZan", method = RequestMethod.POST)
    public Response updatePostByZanSum(@ApiParam(value = "帖子id") @RequestParam String id,
                                       @ApiParam(value = "用户id") @RequestParam String userid) {
        Response response = new Response();
        int sum = facadePost.updatePostByZanSum(id, userid);
        if (response.getCode() == 200) {
            response.setMessage("点赞成功");
            response.setData(sum);
        }
        if (sum == -1) {
            response.setCode(300);
            response.setMessage("已点赞，请刷新重试");
        }
        return response;
    }

    /**
     * 帖子举报接口
     *
     * @param userid
     * @param postid
     * @return
     */
    @ApiOperation(value = "帖子举报", notes = "用于举报帖子接口", response = Response.class)
    @RequestMapping(value = "inAccusation", method = RequestMethod.POST)
    public Response insertPostByAccusation(@ApiParam(value = "用户id") @RequestParam String userid,
                                           @ApiParam(value = "帖子id") @RequestParam String postid) {
        Response response = new Response();
        int type = facadePost.insertPostByAccusation(userid, postid);

        if (response.getCode() == 200 || type == 200) {
            response.setMessage("操作成功");
        }
        return response;
    }

    /**
     * 发现页热门活动————查看全部活动接口
     *
     * @param pageNo
     * @param pageSize
     * @return Response
     */
    @ApiOperation(value = "查看全部活动", notes = "用于返回发现页热门活动————查看全部活动接口(enddays为-1活动还未开始 为0活动已结束 为其他时为距离结束的剩余天数)", response = Response.class)
    @RequestMapping(value = "queryAllActive", method = RequestMethod.POST)
    public Response queryAllActive(@ApiParam(value = "第几页") @RequestParam(required = false, defaultValue = "1") String pageNo,
                                   @ApiParam(value = "每页多少条") @RequestParam(required = false, defaultValue = "10") String pageSize) {
        Response response = new Response();

        Paging<PostVo> pager = new Paging<>(Integer.parseInt(pageNo), Integer.parseInt(pageSize));
        List<PostVo> activeList = facadePost.queryAllActive(pager);
        pager.result(activeList);

        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(pager);

        return response;
    }

    /**
     * 告知类活动————点击参与活动接口
     *
     * @param postid
     * @param userid
     * @return Response
     */
    @ApiOperation(value = "点击参与活动接口（告知类）", notes = "用于告知类活动点击参与活动", response = Response.class)
    @RequestMapping(value = "partActive", method = RequestMethod.POST)
    public Response partActive(@ApiParam(value = "活动id") @RequestParam String postid,
                               @ApiParam(value = "参与用户id") @RequestParam String userid,
                               @ApiParam(value = "参与活动的主题") @RequestParam(required = false) String title,
                               @ApiParam(value = "邮箱") @RequestParam(required = false) String email,
                               @ApiParam(value = "作品简介") @RequestParam(required = false) String introduction) {
        Response response = new Response();
        int flag = facadePost.partActive(postid, userid, title, email, introduction);

        if (flag == 1) {
            response.setCode(200);
            response.setMessage("活动参与成功");
        } else if (flag == -1) {
            response.setCode(201);
            response.setMessage("已参与过该活动");
        }

        return response;
    }

    /**
     * 模块式发布帖子
     *
     * @param request
     * @param userid
     * @param circleid
     * @param title
     * @param postcontent
     * @param isactive
     * @param coverimg
     * @param proids
     * @return
     */
    @ApiOperation(value = "模块式发布帖子", notes = "模块式发布帖子", response = Response.class)
    @RequestMapping(value = "releaseModularPost", method = RequestMethod.POST)
    public Response releaseModularPost(HttpServletRequest request,
                                       @ApiParam(value = "用户id") @RequestParam String userid,
                                       @ApiParam(value = "所属圈子id") @RequestParam String circleid,
                                       @ApiParam(value = "帖子主标题(限18个字以内)") @RequestParam String title,
                                       @ApiParam(value = "帖子内容") @RequestParam String postcontent,
                                       @ApiParam(value = "是否为活动：0 帖子 1 活动") @RequestParam String isactive,
                                       @ApiParam(value = "帖子封面图片url字符串") @RequestParam String coverimg,
                                       @ApiParam(value = "标签实体集合，json字符串形式") @RequestParam String labellist,
                                       @ApiParam(value = "分享的产品id(多个商品用英文逗号,隔开)") @RequestParam(required = false) String proids) {


        Response response = new Response();

        Map count = facadePost.releaseModularPost(request, userid, circleid, title, postcontent, isactive, coverimg, proids, labellist);

        if (count.get("flag").equals(-2)) {
            response.setCode(300);
            response.setMessage("系统异常，APP发帖失败");
        } else if (count.get("flag").equals(-1)) {
            response.setCode(201);
            response.setMessage("用户不具备发帖权限");
        } else {
            response.setCode(200);
            response.setMessage("发帖成功");
        }
        return response;
    }


    @ApiOperation(value = "查询帖子中所有图片url", response = Response.class)
    @RequestMapping(value = "queryPostImgById", method = RequestMethod.POST)
    public Response queryPostImgById(@RequestParam String postid) {
        Response response = new Response();
        List<Map> resault = facadePost.queryPostImgById(postid);
        response.setData(resault);
        return response;
    }

    @ApiOperation(value = "发帖-查询圈子目录", notes = "发帖-查询圈子目录", response = Response.class)
    @RequestMapping(value = "get_circle_category_when_post", method = RequestMethod.GET)
    public Response getCircleCategoryWhenPost() {
        Response response = new Response();
        List<Map> resault = facadePost.getCircleInCatagory();
        response.setData(resault);
        return response;
    }


}
