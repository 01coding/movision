package com.movision.facade.index;

import com.movision.mybatis.circle.entity.CircleVo;
import com.movision.mybatis.circle.service.CircleService;
import com.movision.mybatis.circleCategory.entity.CircleCategory;
import com.movision.mybatis.circleCategory.service.CircleCategoryService;
import com.movision.mybatis.homepageManage.entity.HomepageManage;
import com.movision.mybatis.homepageManage.service.HomepageManageService;
import com.movision.mybatis.post.entity.Post;
import com.movision.mybatis.post.entity.PostVo;
import com.movision.mybatis.post.service.PostService;
import com.movision.mybatis.user.entity.Author;
import com.movision.mybatis.user.entity.User;
import com.movision.mybatis.user.entity.UserVo;
import com.movision.mybatis.user.service.UserService;
import com.movision.utils.pagination.model.Paging;
import javafx.geometry.Pos;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author shuxf
 * @Date 2017/1/18 17:51
 */
@Service
public class FacadeDiscover {

    @Autowired
    private HomepageManageService homepageManageService;

    @Autowired
    private CircleCategoryService circleCategoryService;

    @Autowired
    private PostService postService;

    @Autowired
    private CircleService circleService;

    @Autowired
    private UserService userService;

    public Map<String, Object> queryDiscoverIndexData(String userid) {

        HashMap<String, Object> pmap = new HashMap();
        List<HomepageManage> homepageManageList = homepageManageService.queryBannerList(1);//查询发现页顶部banner轮播图 type=1
        List<CircleCategory> circleCategoryList = circleCategoryService.queryCircleCategoryList();//查询发现页次banner所有圈子类别轮播图
        List<Post> hotActiveList = postService.queryHotActiveList();//查询发现页热门活动列表

        List<CircleVo> hotCircleList;
        hotCircleList = circleService.queryHotCircleList();//查询发现页被设置为发现页展示的圈子

        for (int i = 0; i < hotCircleList.size(); i++) {
            //设置发现页最下面推荐的10个圈子下只推荐最新的5个热帖
            Map<String, Object> map = new HashMap<>();
            int circleid = hotCircleList.get(i).getId();
            map.put("circleid", circleid);
            map.put("sum", 5);//设置为5
            List<Post> postlist = postService.queryCircleSubPost(map);
            hotCircleList.get(i).setHotPostList(postlist);

            //这里需要判断这个圈子是否可以已被当前用户关注
            if (!StringUtils.isEmpty(userid)) {
                //userid不为空，不为空时需要查询该用户有没有关注当前圈子
                HashMap<String, Object> parammap = new HashMap();
                parammap.put("userid", Integer.parseInt(userid));
                parammap.put("circleid", hotCircleList.get(i).getId());
                int count = circleService.queryCountByFollow(parammap);
                if (count == 0) {//未关注
                    hotCircleList.get(i).setIsfollow(0);
                } else {//已关注
                    hotCircleList.get(i).setIsfollow(1);
                }
            } else {
                //未登录时userid为空,全部按钮均显示为可关注（点击跳转到登录页）
                hotCircleList.get(i).setIsfollow(0);
            }
        }

        pmap.put("homepageManageList", homepageManageList);
        pmap.put("circleCategoryList", circleCategoryList);
        pmap.put("hotActiveList", hotActiveList);
        pmap.put("hotCircleList", hotCircleList);
        return pmap;
    }

    public Map<String, Object> queryDiscoverIndexData2Up(){

        Map<String, Object> map = new HashMap<>();
        List<HomepageManage> homepageManageList = homepageManageService.queryBannerList(1);//查询发现页顶部banner轮播图
        List<Post> hotActiveList = postService.queryHotActiveList();//查询发现页热门活动列表
        List<UserVo> hotUserList = userService.queryHotUserList();//查询发现页热门作者列表

        //循环查询作者的发帖数
        for (int i = 0; i < hotUserList.size(); i++){
            UserVo vo = hotUserList.get(i);
            int userid = vo.getId();
            //根据userid查询该用户的总发帖数
            int postsum = postService.queryPostNumByUserid(userid);
            vo.setPostsum(postsum);
            hotUserList.set(i, vo);
        }

        map.put("homepageManageList", homepageManageList);
        map.put("hotActiveList", hotActiveList);
        map.put("hotUserList", hotUserList);
        return map;
    }

    public List<PostVo> searchHotCommentPostInAll(Paging<PostVo> pager) {


        return null;
    }



}
