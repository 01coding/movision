package com.movision.controller.app;

import com.movision.common.Response;
import com.movision.common.util.ShiroUtil;
import com.movision.facade.index.FacadeIndex;
import com.movision.fsearch.pojo.spec.GoodsSearchSpec;
import com.movision.fsearch.pojo.spec.PostSearchSpec;
import com.movision.fsearch.service.impl.PostSearchService;
import com.movision.mybatis.opularSearchTerms.service.OpularSearchTermsService;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * @Author shuxf
 * @Date 2017/1/17 15:25
 */
@RestController
@RequestMapping("/app/index/")
public class AppIndexController {

    private static Logger log = LoggerFactory.getLogger(AppIndexController.class);

    @Autowired
    private FacadeIndex facadeIndex;

    @Autowired
    private PostSearchService postSearchService;
    @Autowired
    private OpularSearchTermsService opularSearchTermsService;



    @ApiOperation(value = "首页数据返回接口", notes = "用户返回首页整版数据(活动贴的话 enddays为-1活动还未开始 为0活动已结束 为其他时为距离结束的剩余天数)", response = Response.class)
    @RequestMapping(value = "index", method = RequestMethod.POST)
    public Response queryIndexData(@ApiParam(value = "用户id") @RequestParam(required = false) String userid) {
        Response response = new Response();

        Map<String, Object> map = facadeIndex.queryIndexData(userid);

        if (response.getCode() == 200) {
            response.setMessage("查询成功");
        }
        response.setData(map);
        return response;
    }

    /*@ApiOperation(value = "意见反馈接口", notes = "用户对平台的意见建议", response = Response.class)
    @RequestMapping(value = "suggestion", method = RequestMethod.POST)
    public Response insertSuggestion(@ApiParam(value = "用户id") @RequestParam String userid,
                                     @ApiParam(value = "选填手机号，qq号等") @RequestParam(required = false) String phone,
                                     @ApiParam(value = "反馈内容") @RequestParam String content) {
        Response response = new Response();
        facadeSuggestion.insertSuggestion(userid, phone, content);//用户反馈信息
        if (response.getCode() == 200) {
            response.setMessage("操作成功");
        }
        return response;
    }*/

    @RequestMapping(value = {"searchPostAndActivity"}, method = RequestMethod.GET)
    @ApiOperation(value = "搜索帖子/活动", notes = "搜索帖子/活动", response = Response.class)
    public Response search_post_and_activity(@ApiParam @ModelAttribute PostSearchSpec spec) throws IOException {

        if (spec.getLimit() <= 0 || spec.getLimit() > 100) {
            spec.setLimit(12);
        }
        Response response = new Response();
        response.setCode(200);
        Map<String, Object> ret;
        try {
            log.debug("测试搜索词汉字是否乱码>>>>>>"+spec.getQ());
            ret = postSearchService.search(spec);
            response.setMsgCode(1);
            response.setMessage("OK!");
            response.setData(ret);
        } catch (Exception e) {
            response.setMsgCode(0);
            response.setMessage("search error!");
            log.error("searchProducts error >>>", e);
        }

        return response;
    }

    @RequestMapping(value = {"get_post_hot_search_word_and_history"}, method = RequestMethod.GET)
    @ApiOperation(value = "查询帖子热门搜索词和搜索历史记录", notes = "查询热门搜索词和搜索历史记录", response = Response.class)
    public Response getHotSearchWordAndHistory() {

        Response response = new Response();
        response.setData(postSearchService.getHotwordAndHistory());
        return response;
    }
    @RequestMapping(value = {"get_userlookhistory"}, method = RequestMethod.GET)
    @ApiOperation(value = "查询用户浏览历史", notes = "查询用户浏览历史", response = Response.class)
    public Response getUserLookingHistory(@RequestParam(required = false, defaultValue = "1") int page,
                                          @RequestParam(required = false, defaultValue = "10") int pageSize) {

        Response response = new Response();
        response.setData(postSearchService.getUserLookingHistory(page,pageSize));
        return response;
    }


    @RequestMapping(value = {"get_post_hot_search_word_and_history_isdel"}, method = RequestMethod.GET)
    @ApiOperation(value = "清除搜索记录", notes = "清除搜索记录", response = Response.class)
    public Response UpdateSearchIsdel() {
        Response response = new Response();
        postSearchService.UpdateSearchIsdel(ShiroUtil.getAppUserID());
        return response;
    }

}
