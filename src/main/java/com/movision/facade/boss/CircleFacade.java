package com.movision.facade.boss;

import com.movision.common.constant.JurisdictionConstants;
import com.movision.fsearch.utils.StringUtil;
import com.movision.mybatis.accusation.service.AccusationService;
import com.movision.mybatis.bossUser.entity.BossUser;
import com.movision.mybatis.bossUser.service.BossUserService;
import com.movision.mybatis.category.entity.Category;
import com.movision.mybatis.category.service.CategoryService;
import com.movision.mybatis.circle.entity.*;
import com.movision.mybatis.circle.service.CircleService;
import com.movision.mybatis.circleCategory.entity.CircleCategory;
import com.movision.mybatis.circleCategory.service.CircleCategoryService;
import com.movision.mybatis.manager.service.ManagerServcie;
import com.movision.mybatis.post.entity.PostList;
import com.movision.mybatis.post.service.PostService;
import com.movision.mybatis.rewarded.service.RewardedService;
import com.movision.mybatis.share.service.SharesService;
import com.movision.mybatis.user.entity.User;
import com.movision.mybatis.user.entity.UserLike;
import com.movision.mybatis.user.entity.UserRole;
import com.movision.mybatis.user.service.UserService;
import com.movision.utils.L;
import com.movision.utils.oss.MovisionOssClient;
import com.movision.utils.pagination.model.Paging;
import com.movision.utils.pagination.util.StringUtils;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author zhurui
 * @Date 2017/2/8 17:56
 */
@Service
public class CircleFacade {
    Logger logger = LoggerFactory.getLogger(CircleFacade.class);

    @Autowired
    private CircleService circleService;

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private ManagerServcie managerService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private commonalityFacade commonalityFacade;

    @Autowired
    private BossUserService bossUserService;

    @Autowired
    private CircleCategoryService circleCategoryService;


  /*  *//**
     * 圈子首页列表查询
     *List<CircleIndexList>
     * @return
     *//*
    public Map queryCircleByList(String loginid) {
        Map tm = new HashedMap();
        Integer id = Integer.parseInt(loginid);
        Map map1 = new HashMap();
        Map res = commonalityFacade.verifyUserByQueryMethod(id, JurisdictionConstants.JURISDICTION_TYPE.select.getCode(), JurisdictionConstants.JURISDICTION_TYPE.circle.getCode(), null);
        if (res.get("resault").equals(1) || res.get("resault").equals(2) || res.get("resault").equals(0)) {
            tm.put("categoryid", null);
            List<CircleIndexList> circlenum = circleService.queryListByCircleCategory(tm);//查询圈子所有分类
            BossUser logintype = bossUserService.queryUserByAdministrator(Integer.parseInt(loginid));//根据登录用户id查询当前用户有哪些权限
            for (int i = 0; i < circlenum.size(); i++) {
                /////////////////////分类列表////////////////////////
                Map map = new HashedMap();
                map.put("type", circlenum.get(i).getCategory());
                List<CircleVo> listt = new ArrayList<>();
                if (res.get("resault").equals(2)) {
                    listt = circleService.queryCircleByLikeList(map);//超管\普管
                } else if (res.get("resault").equals(1)) {//圈子管理员,圈主
                    map.put("userid", loginid);
                    listt = circleService.queryCircleManagementByLikeList(map);
                }
                List<CircleVo> circleVoslist = new ArrayList<>();
                List<Map> username = new ArrayList();//用于存放类型中所有圈主
                int posts = 0;//总帖子数
                int follows = 0;//关注数
                int follownews = 0;//今日关注
                int postnews = 0;//今日新增帖子
                int isessences = 0;//精贴数
                int heatvalue = 0;//热度
                map.put("category", circlenum.get(i).getCategory());
                List<User> userslist = userService.queryCircleMangerByUseridList(map);//根据用户id查询出圈子所有管理员列表
                if (listt.size() > 0) {
                    for (int e = 0; e < listt.size(); e++) {//根据圈子遍历
                        CircleVo vo = new CircleVo();
                        Integer cid = listt.get(e).getId();//圈子id
                        Map m = new HashedMap();
                        m.put("nickname", listt.get(e).getCirclename());
                        m.put("userid", listt.get(e).getCircleUserid());
                        username.add(m);//把圈子的圈主遍历出来临时存放
                        posts += listt.get(e).getPostnum();//圈子帖子和
                        postnews += listt.get(e).getPostnewnum();//新增帖子和
                        follows += listt.get(e).getFollownum();//关注和
                        follownews += listt.get(e).getFollownewnum();//新增关注和
                        isessences += listt.get(e).getIsessencenum();//精贴和
                        heatvalue += listt.get(e).getHeatvalue();//热度

                        //根据圈子id查询圈子的管理员列表
                        List<User> circleManage = userService.queryCircleManagerByCircleList(cid);
                        //内层圈子列表
                        vo.setCirclemanagerlist(circleManage);//圈子管理员列表
                        vo.setPostnum(listt.get(e).getPostnum());//帖子数量
                        vo.setPostnewnum(listt.get(e).getPostnewnum());//今日新增帖子数量
                        vo.setIsessencenum(listt.get(e).getIsessencenum());//精贴数量
                        vo.setOrderid(listt.get(e).getOrderid());//发现推荐
                        vo.setFollownum(listt.get(e).getFollownum());//关注数
                        vo.setFollownewnum(listt.get(e).getFollownewnum());//今日新增关注数
                        vo.setId(listt.get(e).getId());//圈子id
                        vo.setName(listt.get(e).getName());//圈子名称
                        vo.setCategory(listt.get(e).getCategory());//圈子分类
                        vo.setCategoryname(listt.get(e).getCategoryname());//圈子分类名
                        vo.setCirclename(listt.get(e).getCirclename());//圈主
                        vo.setCategorylevel(listt.get(e).getCategorylevel());//判断是否为大V
                        vo.setSupportnum(listt.get(e).getSupportnum());//支持数
                        vo.setStatus(listt.get(e).getStatus());//圈子状态：0 待审核 1 审核通过 2 审核不通过
                        vo.setIsrecommend(listt.get(e).getIsrecommend());//推荐首页
                        vo.setIsdiscover(listt.get(e).getIsdiscover());//推荐发现
                        vo.setCreatetime(listt.get(e).getCreatetime());//圈子创建时间
                        vo.setHeatvalue(listt.get(e).getHeatvalue());//圈子热度
                        circleVoslist.add(vo);
                    }
                    //外层圈子类型列表数据
                    circlenum.get(i).setCirclemanagerlist(userslist);
                    for (int n = 0; n < username.size() - 1; n++) {
                        for (int m = username.size() - 1; m > n; m--) {
                            if (username.get(m).equals(username.get(n))) {
                                username.remove(m);
                            }
                        }
                    }
                    circlenum.get(i).setCirclemaster(username);
                    circlenum.get(i).setPostnum(posts);
                    circlenum.get(i).setPostnewnum(postnews);
                    circlenum.get(i).setFollownum(follows);
                    circlenum.get(i).setFollownewnum(follownews);
                    circlenum.get(i).setIsessencenum(isessences);
                    circlenum.get(i).setClassify(circleVoslist);
                    circlenum.get(i).setHeatvaluenum(heatvalue);
                }
            }
            for (int n = 0; n < circlenum.size(); ) {
                if (circlenum.get(n).getClassify() == null) {
                    circlenum.remove(n);
                } else {
                    n++;
                }
            }
            map1.put("resault", circlenum);
            return map1;
        } else {
            map1.put("resault", -1);
            map1.put("message", "权限不足");
            return map1;
        }
    }
*/

    /**
     * 后台管理--查询精贴列表
     * @param pager
     * @return
     */
    public List<PostList> queryPostIsessenceByList(String cirid, String categoryid, Paging<PostList> pager) {
        Map map = new HashedMap();
        map.put("circleid", cirid);
        map.put("categoryid", categoryid);
        return postService.queryPostIsessenceByList(map, pager);
    }


    /**
     * 后台管理-圈子列表-查询圈子可推荐到发现页的排序
     *
     * @return
     */
    public Map<String, List> queryDiscoverList() {
        List<Circle> list = circleService.queryDiscoverList();
        List<Integer> tem = new ArrayList<>();
        Map<String, List> map = new HashedMap();
        for (int i = 1; i < 10; i++) {
            tem.add(i);
        }
        for (int k = 0; k < list.size(); k++) {//用于返回推荐页排序的剩余位置
            for (int l = 0; l < tem.size(); l++) {
                if (list.get(k).getOrderid() == tem.get(l)) {
                    tem.remove(l);
                }
            }
        }
        map.put("resault", tem);
        return map;
    }

    /**
     * 圈子推荐到发现页
     *
     * @param circleid
     * @return
     */
    public Map updateDiscover(String circleid) {
        Circle circle = new Circle();
        circle.setId(Integer.parseInt(circleid));
        Integer discover = circleService.queryCircleDiscover(Integer.parseInt(circleid));//查询圈子是否推荐发现页
        Map map = new HashedMap();
        if (discover == 0) {//没有
            circle.setIsdiscover(1);
            Integer resault = circleService.updateDiscover(circle);
            map.put("resault", resault);
        } else {//是
            circle.setIsdiscover(0);
            Integer resault = circleService.updateDiscover(circle);
            map.put("resault", resault);
        }
        return map;
    }

    /**
     * 圈子推荐到首页
     *
     * @param circleid
     * @return
     */
    public Map updateCircleIndex(String circleid, String loginid) {
        Integer recommend = circleService.queryCircleRecommendIndex(circleid);
        Map map = new HashedMap();
        Map res = commonalityFacade.verifyUserJurisdiction(Integer.parseInt(loginid), JurisdictionConstants.JURISDICTION_TYPE.update.getCode(), JurisdictionConstants.JURISDICTION_TYPE.circle.getCode(), Integer.parseInt(circleid));
        if (res.get("resault").equals(1)) {
            if (recommend == 0) {
                Integer l = circleService.updateCircleIndex(Integer.parseInt(circleid));
                map.put("resault", 1);
            } else if (recommend == 1) {
                Integer n = circleService.updateCircleIndexDel(circleid);
                map.put("resault", 2);
            }
            return map;
        } else {
            map.put("resault", -1);
            map.put("message", "权限不足");
            return map;
        }
    }

    /**
     * 查看圈子详情
     * @param circleid
     * @return
     */
    public CircleDetails quryCircleDetails(String circleid) {
        return circleService.quryCircleDetails(Integer.parseInt(circleid));
    }


    /**
     * 圈子编辑
     *
     * @param id
     * @param name
     * @param category
     * @param circleadmin
     * @param photo
     * @param introduction
     * @param scope
     * @return
     */
    public Map updateCircle(String id, String name, String category, String circlemanid,
                            String circleadmin, String photo, String introduction, String notice, String maylikeimg, String scope, String loginuser) {
        CircleDetails circleDetails = new CircleDetails();
        Map map = new HashedMap();
        Integer circleid = null;
        Map res = commonalityFacade.verifyUserJurisdiction(Integer.parseInt(loginuser), JurisdictionConstants.JURISDICTION_TYPE.update.getCode(), JurisdictionConstants.JURISDICTION_TYPE.circle.getCode(), Integer.parseInt(id));
        if (res.get("resault").equals(1)) {
            if (!StringUtils.isEmpty(id)) {
                circleid = Integer.parseInt(id);
                circleDetails.setId(Integer.parseInt(id));
            }
            if (!StringUtils.isEmpty(name)) {
                circleDetails.setName(name);
            }
            if (!StringUtils.isEmpty(category)) {
                circleDetails.setCategory(Integer.parseInt(category));
            }
            if (StringUtil.isNotEmpty(notice)) {
                circleDetails.setNotice(notice);
            }
            if (!StringUtils.isEmpty(circleadmin)) {//管理员列表
                //待定
                String[] ary = circleadmin.split(",");//以逗号分隔接收数据
                managerService.deleteManagerToCircleid(circleid);//删除圈子的所有管理员
                for (String itm : ary) {//循环添加
                    Map<String, Integer> mapd = new HashedMap();
                    mapd.put("circleid", circleid);
                    mapd.put("userid", Integer.parseInt(itm));
                    managerService.addManagerToCircleAndUserid(mapd);//添加圈子所用管理员
                }
            }
            if (!StringUtils.isEmpty(circlemanid)) {
                //查询圈主
                String pon = userService.queryUserbyPhoneByUserid(Integer.parseInt(circlemanid));
                circleDetails.setPhone(pon);
            }
            if (!StringUtils.isEmpty(photo)) {
                circleDetails.setPhoto(photo);
            }
            if (!StringUtils.isEmpty(introduction)) {
                circleDetails.setIntroduction(introduction);
            }
            if (!StringUtils.isEmpty(maylikeimg)) {
                circleDetails.setMaylikeimg(maylikeimg);
            }
            if (!StringUtils.isEmpty(scope)) {
                circleDetails.setScope(Integer.parseInt(scope));
            }
            Integer s = circleService.updateCircle(circleDetails);
            if (s == 1) {
                map.put("resault", s);
            } else {
                Integer t = 0;
                map.put("resault", t);
            }
            return map;
        } else {
            map.put("resault", -1);
            map.put("message", "权限不足");
            return map;
        }
    }

    /**
     * 圈子编辑时数据回显
     *
     * @param circleid
     * @return
     */
    public Map<String, CircleDetails> queryCircleByShow(String circleid) {
        Map<String, CircleDetails> map = new HashedMap();
        CircleDetails circleDetails = circleService.queryCircleByShow(Integer.parseInt(circleid));//查询出圈子信息
        List<UserLike> list = userService.queryCircleaAministratorListById(Integer.parseInt(circleid));//根据圈子id查询出管理员列表
        if (list != null) {
            circleDetails.setAdmin(list);
        }
        List<Integer> ords = circleService.queryCircleByOrderidList();//查询圈子推荐发现页排序
        List<Integer> h = new ArrayList<>();
        for (int k = 1; k < 10; k++) {
            h.add(k);
        }
        for (int i = 0; i < ords.size(); i++) {
            for (int j = 0; j < h.size(); j++) {
                if (ords.get(i) == h.get(j) && h.get(j) != circleDetails.getOrderid()) {//比较排序并返回空余排序位置
                    h.remove(j);
                }
            }
            circleDetails.setOrderids(h);
        }
        map.put("resault", circleDetails);
        return map;
    }


    /**
     * 圈子添加
     *
     * @param name
     * @param category
     * @param userid
     * @param circleadmin
     * @param circlemanid
     * @param photo
     * @param introduction
     * @param maylikeimg
     * @return
     */
    @Transactional
    public Map addCircle(String name, String category, String userid, String circleadmin,
                         String circlemanid, String photo, String maylikeimg, String introduction, String notice, String loginid) {
        CircleDetails circleDetails = new CircleDetails();
        Map map = new HashedMap();
        //登录用户，操作，操作种类，种类id
        Map res = commonalityFacade.verifyUserJurisdiction(Integer.parseInt(loginid), JurisdictionConstants.JURISDICTION_TYPE.add.getCode(), JurisdictionConstants.JURISDICTION_TYPE.circle.getCode(), null);
        if (res.get("resault").equals(1)) {
            if (!StringUtils.isEmpty(category)) {
                circleDetails.setName(name);
            }
            if (!StringUtils.isEmpty(category)) {
                circleDetails.setCategory(Integer.parseInt(category));
            }

            if (!StringUtils.isEmpty(circlemanid)) {//添加创建人
                circleDetails.setUserid(circlemanid);
            }
            if (StringUtil.isNotEmpty(notice)) {//圈子公告
                circleDetails.setNotice(notice);
            }
            if (!StringUtils.isEmpty(userid)) {
                //查询圈主手机号
                String pon = userService.queryUserbyPhoneByUserid(Integer.parseInt(userid));
                circleDetails.setPhone(pon);//设置圈主手机号
            }
            circleDetails.setCreatetime(new Date());
            if (!StringUtils.isEmpty(photo)) {
                circleDetails.setPhoto(photo);
            }
            if (!StringUtils.isEmpty(introduction)) {
                circleDetails.setIntroduction(introduction);
            }
            if (!StringUtils.isEmpty(maylikeimg)) {//圈子首页展示小方块
                circleDetails.setMaylikeimg(maylikeimg);
            }
            circleDetails.setScope(2);//设置默认圈子贷方范围，对所有人开放
            circleDetails.setStatus(0);//设置审核状态，初始值为待审核：0
            circleDetails.setOrderid(0);//设置精选排序，初始值：0
            circleDetails.setIsrecommend(0);//设置是否被推荐，初始值0
            circleDetails.setPermission(1);//设置其他用户是否可以发帖，初始值：1是
            circleDetails.setHeatvalue(0);//圈子热度
            Integer s = circleService.insertCircle(circleDetails);//创建圈子
            Integer cirid = circleDetails.getId();
            if (!StringUtils.isEmpty(circleadmin)) {//管理员列表
                //待定
                String ary[] = circleadmin.split(",");//以逗号分隔接收数据
                for (int ad = 0; ad < ary.length; ad++) {//循环添加
                    Map<String, Integer> mapd = new HashedMap();
                    String ads = ary[ad];
                    mapd.put("circleid", cirid);
                    mapd.put("userid", Integer.parseInt(ads));
                    managerService.addManagerToCircleAndUserid(mapd);//添加圈子所用管理员
                }
            }
            if (s == 1) {
                map.put("resault", s);
            } else {
                Integer t = 0;
                map.put("resault", t);
            }
            return map;
        } else {
            map.put("resault", -1);
            map.put("message", "权限不足");
            return map;
        }
    }


    /**
     * 根据圈子id查询帖子列表
     *
     * @param circleid
     * @param pager
     * @return
     */
    public List<PostList> queryPostByCircleId(String circleid, String type, Paging<PostList> pager) {
        Map map = new HashedMap();
        map.put("circleid", Integer.parseInt(circleid));
        map.put("type", type);
        return postService.queryPostByCircleId(map, pager);
    }

    /**
     * 查询圈子分类
     *
     * @return
     */
    public List<CircleCategory> queryCircleTypeList(String loginid) {
        Integer loid = Integer.parseInt(loginid);
        Map res = commonalityFacade.verifyUserByQueryMethod(loid, JurisdictionConstants.JURISDICTION_TYPE.select.getCode(), JurisdictionConstants.JURISDICTION_TYPE.circleType.getCode(), null);
        if (res.get("resault").equals(1)) {//圈主或者管理员登录
            List<CircleCategory> list = circleCategoryService.queryCircleTytpeListByUserid(loid);
            return list;
        } else if (res.get("resault").equals(2)) {
            List<CircleCategory> list = categoryService.queryCircleTypeList(null);
            return list;
        }
        return null;
    }

    /**
     * 查询圈子名称
     * @param categoryid
     * @return
     */
    public List<Circle> queryListByCircleType(String categoryid, String loginid) {
        Integer loid = Integer.parseInt(loginid);
        Map res = commonalityFacade.verifyUserByQueryMethod(loid, JurisdictionConstants.JURISDICTION_TYPE.select.getCode(), JurisdictionConstants.JURISDICTION_TYPE.circleType.getCode(), null);
        if (res.get("resault").equals(1)) {
                Map map = new HashedMap();
                map.put("categoryid", categoryid);
                map.put("userid", loid);
            List<Circle> circle = circleService.queryListByCircleListByUserid(map);//圈主。圈子管理员
                return circle;
        } else if (res.get("resault").equals(2) || res.get("resault").equals(0)) {//超管，普管，特邀嘉宾
            Map map = new HashMap();
            map.put("categoryid", categoryid);
            List<Circle> circle = circleService.queryListByCircleList(map);
            return circle;
        }
        return null;
    }

    /**
     * 用于首页查询圈子列表
     * @return
     */
    public List<CircleVo> queryCircleList(String loginid, String categoryid, Integer type) {
        List<CircleVo> circleList = null;
        //查询当前用户角色
        Integer vip = userService.queryUserIsVip(loginid);//是否为大V
        UserRole ur = userService.queryUserRole(loginid);//用户角色
        ur.setUserid(Integer.parseInt(loginid));
        if (categoryid != null) {
            ur.setCategoryid(Integer.parseInt(categoryid));
        }
        ur.setType(type);
        if (vip > 0) {
            ur.setVip(1);
        }
        if (ur.getIscircle() != null && ur.getCirclemanagement() != null && ur.getCommon() != null) {
            if (ur.getIscircle() == 1 || ur.getCirclemanagement() == 1) {
                circleList = circleService.queryCircleListByUserRole(ur);//查询用户可发帖的圈子列表
            }/* else if (ur.getCommon() == 1 || loginid == "-1") {
                circleList = circleService.queryCircleList();//查询所有圈子列表
            } */ else {
                Map map = new HashMap();
                map.put("vip", vip);
                map.put("type", type);
                map.put("categoryid", categoryid);
                circleList = circleService.queryCircleListTo(map);
            }
        } else {
            Map map = new HashMap();
            map.put("vip", vip);
            map.put("type", type);
            map.put("categoryid", categoryid);
            circleList = circleService.queryCircleListTo(map);
        }
        return circleList;
    }

    /**
     * 查询圈子类型
     *
     * @param loginid
     * @return
     */
    public List<CircleVo> queryCategoryList(String loginid) {
        return queryCircleList(loginid, null, 5);
    }

    /**
     * 添加圈子分类
     *
     * @param typename
     * @param discoverpageurl
     * @return
     */
    public Map addCircleType(String typename, String discoverpageurl, String loginid) {
        Map map = new HashedMap();
        /*Map packaging = new HashedMap();*/
        CircleCategory category = new CircleCategory();
        //登录用户，操作，操作类型，类型id
        Map res = commonalityFacade.verifyUserJurisdiction(Integer.parseInt(loginid), JurisdictionConstants.JURISDICTION_TYPE.add.getCode(), JurisdictionConstants.JURISDICTION_TYPE.circleType.getCode(), null);
        if (res.get("resault").equals(1)) {
            category.setCategoryname(typename);
            category.setIntime(new Date());
            category.setDiscoverpageurl(discoverpageurl);
            circleCategoryService.addCircleType(category);
            map.put("resault", 200);
            return map;
        } else {
            map.put("resault", -1);
            map.put("message", "权限不足");
            return map;
        }
    }

    /**
     * 回显圈子类型详情接口
     *
     * @param category
     * @return
     */
    public Category queryCircleCategory(String category) {
        Map map = new HashMap();
        map.put("categoryid", Integer.parseInt(category));
        return circleCategoryService.queryCircleCategory(map);
    }


    /**
     * 编辑圈子类型接口
     *
     * @param categoryid
     * @param
     * @param discoverpageurl
     * @return
     */
    public Map updateCircleCategory(String categoryid, String categoryname, String discoverpageurl, String loginid) {
        Map map = new HashedMap();
        //登录用户，操作，操作类型，操作id
        Map ma = commonalityFacade.verifyUserJurisdiction(Integer.parseInt(loginid), JurisdictionConstants.JURISDICTION_TYPE.update.getCode(), JurisdictionConstants.JURISDICTION_TYPE.circleType.getCode(), null);
        if (ma.get("resault").equals(1)) {
            /*map.put("categoryid", categoryid);
            map.put("categoryname", categoryname);
            map.put("intime", new Date());
            map.put("imgurl", discoverpageurl);*/
            CircleCategory circleCategory = new CircleCategory();
            circleCategory.setDiscoverpageurl(discoverpageurl);
            circleCategory.setCategoryname(categoryname);
            circleCategory.setIntime(new Date());
            circleCategory.setId(Integer.parseInt(categoryid));
            int i = circleCategoryService.updateCircleCategory(circleCategory);
            Map m = new HashedMap();
            if (i == 1) {
                m.put("resault", i);
                return m;
            } else {
                m.put("resault", 0);
                return m;
            }
        } else {
            map.put("resault", -1);
            map.put("message", "权限不足");
            return map;
        }
    }

    /**
     * 查询圈子下的帖子列表
     *
     * @param circleid
     * @param pager
     * @return
     */
    public List<PostList> findAllQueryCircleByPostList(String categoryid, String circleid, Paging<PostList> pager) {
        Map map = new HashedMap();
        map.put("circleid", circleid);
        map.put("categoryid", categoryid);
        return postService.findAllQueryCircleByPostList(map, pager);
    }

    public List<User> queryCircleManList() {
        return userService.queryCircleManList();
    }

   /* *//**
     * 查询发帖人
     *
     * @param
     * @return
     *//*
    public List<BossUser> queryIssuePostManList() {
        return userService.queryIssuePostManList();
    }*/

/*

*/
/**
     * 根据条件查询圈子列表
     *
     * @param circle
     * @param circleman
     * @param type
     * @param begintime
     * @param endtime
     * @return
 *//*


    public Map queryCircleByCondition(String pai, String circle, String circleman, String type, String status, String begintime, String endtime, String loginid) {
        Map map = new HashedMap();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date beg = null;
        Date end = null;
        Map map1 = new HashMap();
        Map res = commonalityFacade.verifyUserByQueryMethod(Integer.parseInt(loginid), JurisdictionConstants.JURISDICTION_TYPE.select.getCode(), JurisdictionConstants.JURISDICTION_TYPE.circle.getCode(), null);
        if (res.get("resault").equals(1) || res.get("resault").equals(2)) {
            if ((!StringUtils.isEmpty(begintime)) && (!StringUtils.isEmpty(endtime))) {
                try {
                    beg = format.parse(begintime);
                    end = format.parse(endtime);
                } catch (ParseException e) {
                    logger.error("时间格式转换异常", e);
                }
            }
            if (!StringUtils.isEmpty(pai)) {
                map.put("pai", pai);
            }
            if (!StringUtils.isEmpty(circle)) {
                map.put("circleid", circle);
            }
            if (!StringUtils.isEmpty(type)) {
                map.put("type", type);
            }
            if (!StringUtils.isEmpty(status)) {
                map.put("status", status);
            }
            if (!StringUtils.isEmpty(circleman)) {
                map.put("circleman", circleman);
            }
            map.put("begintime", beg);
            map.put("endtime", end);
            List<CircleIndexList> circlenum = new ArrayList<>();
            if (StringUtils.isEmpty(type)) {
                Map tm = new HashedMap();
                tm.put("categoryid", null);
                circlenum = circleService.queryListByCircleCategory(tm);//查询圈子所有分类
                BossUser logintype = bossUserService.queryUserByAdministrator(Integer.parseInt(loginid));//根据登录用户id查询当前用户有哪些权限
                for (int i = 0; i < circlenum.size(); i++) {
                    /////////////////////分类列表////////////////////////
                    map.put("type", circlenum.get(i).getCategory());
                    List<User> adminlist = new ArrayList();//用于存储类型中所有圈子的管理员
                    List username = new ArrayList<>();//用于存放类型中所有圈主
                    List<CircleVo> listt = new ArrayList<>();
                    if (res.get("resault").equals(2) || res.get("resault").equals(0)) {
                        listt = circleService.queryCircleByLikeList(map);//获取圈子分类列表的圈子列表
                    } else if (res.get("resault").equals(1)) {//圈子管理员,圈主
                            map.put("userid", loginid);
                            listt = circleService.queryCircleManagementByLikeList(map);
                    }
                    List<CircleVo> circleVoslist = new ArrayList<>();
                    int posts = 0;//总帖子数
                    int follows = 0;//关注数
                    int follownews = 0;//今日关注
                    int postnews = 0;//今日新增帖子
                    int isessences = 0;//精贴数
                    map.put("category", circlenum.get(i).getCategory());
                    List<User> userslist = userService.queryCircleMangerByUseridList(map);//查询出圈子所有管理员列表
                    //List<User> userslist = userService.queryCircleManagerList(circlenum.get(i).getCategory());//查询出圈子管理员列表
                    if (listt.size() > 0) {
                        for (int e = 0; e < listt.size(); e++) {//遍历所有圈子
                            CircleVo vo = new CircleVo();
                            Integer circleid = listt.get(e).getId();
                            Map m = new HashedMap();
                            m.put("nickname", listt.get(e).getCirclename());
                            m.put("userid", listt.get(e).getCircleUserid());
                            username.add(m);//把圈子的圈主遍历出来临时存放
                            posts += listt.get(e).getPostnum();//圈子帖子和
                            postnews += listt.get(e).getPostnewnum();//新增帖子和
                            follows += listt.get(e).getFollownum();//关注和
                            follownews += listt.get(e).getFollownewnum();//新增关注和
                            isessences += listt.get(e).getIsessencenum();//精贴和


                            //根据圈子id查询圈子的管理员列表
                            List<User> circleManage = userService.queryCircleManagerByCircleList(circleid);
                            vo.setCirclemanagerlist(circleManage);
                            vo.setPostnum(listt.get(e).getPostnum());//帖子数量
                            vo.setPostnewnum(listt.get(e).getPostnewnum());//今日新增帖子数量
                            vo.setIsessencenum(listt.get(e).getIsessencenum());//精贴数量
                            vo.setOrderid(listt.get(e).getOrderid());//发现推荐
                            vo.setFollownum(listt.get(e).getFollownum());//关注数
                            vo.setFollownewnum(listt.get(e).getFollownewnum());//今日新增关注数
                            vo.setId(listt.get(e).getId());//圈子id
                            vo.setName(listt.get(e).getName());//圈子名称
                            vo.setCategory(listt.get(e).getCategory());//圈子分类
                            vo.setCategoryname(listt.get(e).getCategoryname());//圈子分类名
                            vo.setCirclename(listt.get(e).getCirclename());//圈主
                            vo.setCategorylevel(listt.get(e).getCategorylevel().toString());//判断是否为大V
                            vo.setSupportnum(listt.get(e).getSupportnum());//支持数
                            vo.setStatus(listt.get(e).getStatus());//圈子状态：0 待审核 1 审核通过 2 审核不通过
                            vo.setIsrecommend(listt.get(e).getIsrecommend());//推荐首页
                            vo.setIsdiscover(listt.get(e).getIsdiscover());//推荐发现
                            vo.setCreatetime(listt.get(e).getCreatetime());//圈子创建时间
                            circleVoslist.add(vo);
                        }
                        circlenum.get(i).setCirclemanagerlist(userslist);
                        for (int n = 0; n < username.size() - 1; n++) {
                            for (int m = username.size() - 1; m > n; m--) {
                                if (username.get(m).equals(username.get(n))) {
                                    username.remove(m);
                                }
                            }
                        }
                        circlenum.get(i).setCirclemaster(username);
                        circlenum.get(i).setPostnum(posts);
                        circlenum.get(i).setPostnewnum(postnews);
                        circlenum.get(i).setFollownum(follows);
                        circlenum.get(i).setFollownewnum(follownews);
                        circlenum.get(i).setIsessencenum(isessences);
                        circlenum.get(i).setClassify(circleVoslist);
                    }
                }
                for (int n = 0; n < circlenum.size(); ) {
                    if (circlenum.get(n).getClassify() == null) {
                        circlenum.remove(n);
                    } else {
                        n++;
                    }
                }
                map1.put("resault", circlenum);
                return map1;
            } else {
                Map tm = new HashedMap();
                tm.put("categoryid", type);
                /////////////////////分类列表////////////////////////
                circlenum = circleService.queryListByCircleCategory(tm);//查询指定圈子分类
                BossUser logintype = bossUserService.queryUserByAdministrator(Integer.parseInt(loginid));//根据登录用户id查询当前用户有哪些权限
                for (int f = 0; f < circlenum.size(); f++) {
                    //List<CircleVo> listt = circleService.queryCircleByLikeList(map);//获取圈子分类列表的圈子列表
                    List<CircleVo> listt = new ArrayList<>();
                    if (res.get("resault").equals(2) || res.get("resault").equals(0)) {
                        listt = circleService.queryCircleByLikeList(map);//获取圈子分类列表的圈子列表
                    } else if (res.get("resault").equals(1)) {//圈子管理员,圈主
                            map.put("userid", loginid);
                            listt = circleService.queryCircleManagementByLikeList(map);
                    }
                    List<CircleVo> circleVoslist = new ArrayList<>();
                    List username = new ArrayList();//用于存放类型中所有圈主
                    int posts = 0;//总帖子数
                    int follows = 0;//关注数
                    int follownews = 0;//今日关注
                    int postnews = 0;//今日新增帖子
                    int isessences = 0;//精贴数
                    List<User> userslist = userService.queryCircleMangerByUseridList(map);//查询出圈子所有管理员列表
                    //List<User> userslist = userService.queryCircleManagerList(circlenum.get(f).getCategory());//查询出圈子管理员列表
                    if (listt.size() > 0) {
                        for (int e = 0; e < listt.size(); e++) {
                            CircleVo vo = new CircleVo();
                            Integer circleid = listt.get(e).getId();
                            Map m = new HashedMap();
                            m.put("nickname", listt.get(e).getCirclename());
                            m.put("userid", listt.get(e).getCircleUserid());
                            username.add(m);//把圈子的圈主遍历出来临时存放
                            posts += listt.get(e).getPostnum();//圈子帖子和
                            postnews += listt.get(e).getPostnewnum();//新增帖子和
                            follows += listt.get(e).getFollownum();//关注和
                            follownews += listt.get(e).getFollownewnum();//新增关注和
                            isessences += listt.get(e).getIsessencenum();//精贴和

                            //根据圈子id查询圈子的管理员列表
                            List<User> circleManage = userService.queryCircleManagerByCircleList(circleid);
                            vo.setCirclemanagerlist(circleManage);
                            vo.setPostnum(listt.get(e).getPostnum());//帖子数量
                            vo.setPostnewnum(listt.get(e).getPostnewnum());//今日新增帖子数量
                            vo.setIsessencenum(listt.get(e).getIsessencenum());//精贴数量
                            vo.setOrderid(listt.get(e).getOrderid());//发现推荐
                            vo.setFollownum(listt.get(e).getFollownum());//关注数
                            vo.setFollownewnum(listt.get(e).getFollownewnum());//今日新增关注数
                            vo.setId(listt.get(e).getId());//圈子id
                            vo.setName(listt.get(e).getName());//圈子名称
                            vo.setCategory(listt.get(e).getCategory());//圈子分类
                            vo.setCategoryname(listt.get(e).getCategoryname());//圈子分类名
                            vo.setCirclename(listt.get(e).getCirclename());//圈主
                            vo.setCategorylevel(listt.get(e).getCategorylevel().toString());//判断是否为大V
                            vo.setSupportnum(listt.get(e).getSupportnum());//支持数
                            vo.setStatus(listt.get(e).getStatus());//圈子状态：0 待审核 1 审核通过 2 审核不通过
                            vo.setIsrecommend(listt.get(e).getIsrecommend());//推荐首页
                            vo.setIsdiscover(listt.get(e).getIsdiscover());//推荐发现
                            vo.setCreatetime(listt.get(e).getCreatetime());//圈子创建时间
                            circleVoslist.add(vo);
                        }

                        circlenum.get(f).setCirclemanagerlist(userslist);
                        for (int n = 0; n < username.size() - 1; n++) {
                            for (int m = username.size() - 1; m > n; m--) {
                                if (username.get(m).equals(username.get(n))) {
                                    username.remove(m);
                                }
                            }
                        }
                        circlenum.get(f).setCirclemaster(username);
                        circlenum.get(f).setPostnum(posts);
                        circlenum.get(f).setPostnewnum(postnews);
                        circlenum.get(f).setFollownum(follows);
                        circlenum.get(f).setFollownewnum(follownews);
                        circlenum.get(f).setIsessencenum(isessences);
                        circlenum.get(f).setClassify(circleVoslist);
                    }
                }
                for (int n = 0; n < circlenum.size(); ) {
                    if (circlenum.get(n).getClassify() == null) {
                        circlenum.remove(n);
                    } else {
                        n++;
                    }
                }
                map1.put("resault", circlenum);
                return map1;
            }
        } else {
            map1.put("resault", -1);
            map1.put("resault", "权限不足");
            return map1;
        }
    }
*/


    /**
     * 根据条件查询圈子列表
     *
     * @param circle
     * @param circleman
     * @param type
     * @return
     */
    public Map queryCircleByConditionNew(String pai, String circle, String circleman, String type, String status, String intime, String loginid) {
        String begintime = null;
        String endtime = null;
        if (StringUtil.isNotEmpty(intime)) {
            String[] strings = intime.split(",");
            begintime = strings[0];
            endtime = strings[1];
        }
        Map map = new HashedMap();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date beg = null;
        Date end = null;
        Map map1 = new HashMap();
        Map res = commonalityFacade.verifyUserByQueryMethod(Integer.parseInt(loginid), JurisdictionConstants.JURISDICTION_TYPE.select.getCode(), JurisdictionConstants.JURISDICTION_TYPE.circle.getCode(), null);
        if (res.get("resault").equals(1) || res.get("resault").equals(2)) {
            if (StringUtil.isNotEmpty(begintime) && StringUtil.isNotEmpty(endtime)) {
                try {
                    beg = format.parse(begintime);
                    end = format.parse(endtime);
                } catch (ParseException e) {
                    logger.error("时间格式转换异常", e);
                }
            }
            if (!StringUtils.isEmpty(pai)) {
                map.put("pai", pai);
            }
            if (!StringUtils.isEmpty(circle)) {
                map.put("circleid", circle);
            }
            if (!StringUtils.isEmpty(type)) {
                map.put("type", type);
            }
            if (!StringUtils.isEmpty(status)) {
                map.put("status", status);
            }
            if (!StringUtils.isEmpty(circleman)) {
                map.put("circleman", circleman);
            }
            map.put("begintime", beg);
            map.put("endtime", end);

            List<CircleList> circlenum = circleService.queryListByCircleCategory(map);//查询圈子分类统计汇总
            //循环圈子分类
            for (int i = 0; i < circlenum.size(); i++) {
                //圈主或者圈子管理员
                if (res.get("resault").equals(1)) {
                    map.put("userid", Integer.parseInt(loginid));
                    map.put("category", circlenum.get(i).getCategory());
                    //根据圈子类型查询圈主列表
                    List<String> circlemaster = circleService.queryCircleEmaster(map);
                    circlenum.get(i).setCirclemaster(circlemaster);
                    //根据圈子类型查询管理员列表
                    List<String> userslist = userService.queryCircleMangerByUseridList(map);
                    circlenum.get(i).setCirclemanagerlist(userslist);
                } else {
                    //后台管理员
                    map.put("userid", null);
                    map.put("category", circlenum.get(i).getCategory());
                    //根据圈子类型查询圈主列表
                    List<String> circlemaster = circleService.queryCircleEmaster(map);
                    circlenum.get(i).setCirclemaster(circlemaster);
                    //根据圈子类型查询管理员列表
                    List<String> userslist = userService.queryCircleMangerByUseridList(map);
                    circlenum.get(i).setCirclemanagerlist(userslist);
                }
                map.put("type", circlenum.get(i).getCategory());
                List<CircleList> listt = new ArrayList<>();
                if (res.get("resault").equals(2) || res.get("resault").equals(0)) {
                    //根据条件查询所有圈子列表
                    listt = circleService.queryCircleByLikeList(map);
                    //圈子管理员,圈主
                } else if (res.get("resault").equals(1)) {
                    map.put("userid", loginid);
                    //根据条件查询 圈主活管理员管理的圈子列表
                    listt = circleService.queryCircleManagementByLikeList(map);
                }
                //遍历所有圈子
                for (int e = 0; e < listt.size(); e++) {
                    Integer circleid = listt.get(e).getId();
                    //根据圈子id查询圈子的管理员列表
                    List<String> circleManage = userService.queryCircleManagerByCircleList(circleid);
                    listt.get(e).setCirclemanagerlist(circleManage);
                }

                circlenum.get(i).setFoldCircle(listt);
            }
            //去除圈子为空的圈子类型
            List<CircleList> resault = new ArrayList<>();
            for (int k = 0; k < circlenum.size(); k++) {
                if (circlenum.get(k).getFoldCircle().size() > 0) {
                    resault.add(circlenum.get(k));
                }
            }

            map1.put("resault", resault);
            return map1;

        } else {
            map1.put("resault", -1);
            map1.put("resault", "权限不足");
            return map1;
        }
    }

    /**
     * 圈子审核
     *
     * @param circleid
     * @param status
     * @return
     */
    public Map updateAuditCircle(String circleid, String status, String loginid) {
        Map map = new HashedMap();
        Map m = new HashedMap();
        //登录用户，操作，操作类型，类型id
        Map res = commonalityFacade.verifyUserJurisdiction(Integer.parseInt(loginid), JurisdictionConstants.JURISDICTION_TYPE.update.getCode(), JurisdictionConstants.JURISDICTION_TYPE.circleAudit.getCode(), null);
        if (res.get("resault").equals(1)) {
            map.put("circleid", circleid);
            map.put("status", status);
            int i = circleService.updateAuditCircle(map);
            m.put("resault", i);
            return m;
        } else {
            map.put("resault", -1);
            map.put("message", "权限不足");
            return map;
        }
    }

    /**
     * 修改圈子分类
     *
     * @param categoryid
     * @param categoryname
     * @param discoverpageurl
     * @return
     */
/*    public int updateCircleCategoryClassify(String categoryid, String categoryname, String discoverpageurl) {
        Map map = new HashedMap();
        map.put("categoryid", categoryid);
        map.put("categoryname", categoryname);
        map.put("discoverpageurl", discoverpageurl);
        return circleService.updateCircleCategoryClassify(map);
    }*/

    /**
     * 根据类型id查询圈子类型
     *
     * @param categoryid
     * @return
     */
    public Category queryCircleCategoryClassify(String categoryid) {
        return circleService.queryCircleCategoryClassify(categoryid);
    }
}
