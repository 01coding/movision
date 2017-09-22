package com.movision.facade.voteH5;

import com.movision.facade.paging.PageFacade;
import com.movision.fsearch.utils.StringUtil;
import com.movision.mybatis.activeH5.entity.ActiveH5;
import com.movision.mybatis.activeH5.entity.ActiveH5Vo;
import com.movision.mybatis.activeH5.service.ActiveH5Service;
import com.movision.mybatis.take.entity.Take;
import com.movision.mybatis.take.entity.TakeVo;
import com.movision.mybatis.take.service.TakeService;
import com.movision.mybatis.votingrecords.entity.Votingrecords;
import com.movision.mybatis.votingrecords.service.VotingrecordsService;
import com.movision.utils.JsoupCompressImg;
import com.movision.utils.file.FileUtil;
import com.movision.utils.oss.MovisionOssClient;
import com.movision.utils.pagination.model.Paging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author zhanglei
 * @Date 2017/8/29 15:01
 */
@Service
public class VoteFacade {

    @Autowired
    private ActiveH5Service activeH5Service;

    @Autowired
    private TakeService takeService;

    @Autowired
    private VotingrecordsService votingrecordsService;

    @Autowired
    private JsoupCompressImg jsoupCompressImg;

    @Autowired
    private MovisionOssClient movisionOssClient;

    @Autowired
    private PageFacade pageFacade;
    private static Logger logger = LoggerFactory.getLogger(VoteFacade.class);


    /**
     * 插入活动
     *
     * @param name
     * @param photo
     * @param begintime
     * @param endtime
     * @param
     * @param activitydescription
     * @param
     * @return
     */
    public int insertSelective(HttpServletRequest request, String name, String photo, String begintime, String endtime, String activitydescription) {
        ActiveH5 activeH5 = new ActiveH5();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if (StringUtil.isNotEmpty(name)) {
            activeH5.setName(name);
        }
        if (StringUtil.isNotEmpty(photo)) {
            activeH5.setPhoto(photo);
        }
        Date begin = null;//开始时间
        if (StringUtil.isNotEmpty(begintime)) {
            try {
                begin = format.parse(begintime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Date end = null;
        if (StringUtil.isNotEmpty(endtime)) {
            try {
                end = format.parse(endtime);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        activeH5.setBegintime(begin);
        activeH5.setEndtime(end);
        if (StringUtil.isNotEmpty(activitydescription)) {
            activeH5.setActivitydescription(activitydescription);
        }
        activeH5.setIsdel(0);
        activeH5.setIntime(new Date());
        int result = activeH5Service.insertSelective(activeH5);
        return result;
    }

    /**
     * 根据id查询活动详情
     * @param id
     * @return
     */
    public ActiveH5 queryActivityById(Integer id) {
        return activeH5Service.queryActivityById(id);
    }

    /**
     * 删除活动
     *
     * @param id
     * @return
     */
    public int deleteActive(int id) {
        return activeH5Service.deleteActive(id);
    }

    /**
     * 更新活动
     * @param id
     * @param name
     * @param photo
     * @param explain
     * @param bigintime
     * @param endtime
     */
    public void updateActivity(HttpServletRequest request, Integer id, String name, String photo,
                               String explain, String bigintime, String endtime) {
        ActiveH5 activeH5 = new ActiveH5();
        activeH5.setId(id);
        if (StringUtil.isNotEmpty(name)) {
            activeH5.setName(name);
        }
        if (StringUtil.isNotEmpty(photo)) {
            activeH5.setPhoto(photo);
        }
        if (StringUtil.isNotEmpty(explain)) {
            //内容转换
            Map con = jsoupCompressImg.compressImg(request, explain);
            System.out.println(con);
            if ((int) con.get("code") == 200) {
                String str = con.get("content").toString();
                str = str.replace("\\", "");
                activeH5.setActivitydescription(str);
            } else {
                logger.error("帖子内容转换异常");
                activeH5.setActivitydescription(explain);
            }
            //activeH5.setActivitydescription(explain);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date big = null;
        if (StringUtil.isNotEmpty(bigintime)) {
            try {
                big = format.parse(bigintime);
                activeH5.setBegintime(big);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Date end = null;
        if (StringUtil.isNotEmpty(endtime)) {
            try {
                end = format.parse(endtime);
                activeH5.setEndtime(end);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        activeH5Service.updateActivity(activeH5);
    }

    /**
     * 查询所有活动
     *
     * @param paging
     * @return
     */
    public List<ActiveH5Vo> findAllActive(String name, String bigintime, String endtime, Paging<ActiveH5Vo> paging) {
        ActiveH5 activeH5 = new ActiveH5();
        if (StringUtil.isNotEmpty(name)) {
            activeH5.setName(name);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date beg = null;
        if (StringUtil.isNotEmpty(bigintime)) {
            try {
                beg = format.parse(bigintime);
                activeH5.setBegintime(beg);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        Date end = null;
        if (StringUtil.isNotEmpty(endtime)) {
            try {
                end = format.parse(endtime);
                activeH5.setEndtime(end);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return activeH5Service.findAllActive(activeH5, paging);
    }

    public List<ActiveH5Vo> queryAllActive(String name) {
        return activeH5Service.queryAllActive(name);
    }



    /**
     * 添加参赛人员
     *
     * @param
     * @param name
     * @param
     * @return
     */
    public int insertSelectiveTP(HttpServletRequest request, String activeid, String name, String phone, String photo,
                                 String describe, String nickname, String banner) {
        Take take = new Take();
        if (StringUtil.isNotEmpty(activeid)) {
            take.setActiveid(Integer.parseInt(activeid));
        }
        if (StringUtil.isNotEmpty(name)) {
            take.setName(name);
        }
        take.setIsdel(0);
        take.setIntime(new Date());
        if (StringUtil.isNotEmpty(phone)) {
            take.setPhone(phone);
        }
        if (StringUtil.isNotEmpty(photo)) {
            take.setPhoto(photo);
        }
        if (StringUtil.isNotEmpty(describe)) {
            take.setDescribes(describe);
        }
        if (StringUtil.isNotEmpty(nickname)) {
            take.setNickname(nickname);
        }
        if (StringUtil.isNotEmpty(banner)) {
            take.setBanner(banner);
        }
        int result = takeService.insertSelectiveTP(take);
        return result;
    }

    /**
     * 编辑投稿
     *
     * @param id
     * @param activeid
     * @param name
     * @param phone
     * @param photo
     * @param describe
     * @param nickname
     * @param banner
     * @param audit
     * @param mark
     */
    public void updateTakeById(HttpServletRequest request, String id, String activeid, String name, String phone,
                               String photo, String describe, String nickname, String banner, String audit, String mark) {
        Take take = new Take();
        take.setId(Integer.parseInt(id));
        if (StringUtil.isNotEmpty(activeid)) {
            take.setActiveid(Integer.parseInt(activeid));
        }
        if (StringUtil.isNotEmpty(name)) {
            take.setName(name);
        }
        if (StringUtil.isNotEmpty(phone)) {
            take.setPhone(phone);
        }
        if (StringUtil.isNotEmpty(photo)) {

            take.setPhoto(photo);
        }
        if (StringUtil.isNotEmpty(describe)) {
            take.setDescribes(describe);
        }
        if (StringUtil.isNotEmpty(nickname)) {
            take.setNickname(nickname);
        }
        if (StringUtil.isNotEmpty(banner)) {
            take.setBanner(banner);
        }
        if (StringUtil.isNotEmpty(audit)) {
            take.setAudit(Integer.parseInt(audit));
        }
        if (StringUtil.isNotEmpty(mark)) {
            take.setMark(Integer.parseInt(mark));
        }
        takeService.updateTakeById(take);
    }


    /**
     * 投稿审核
     *
     */
    public void updateTakeByAudit(String id, String type) {
        Take take = new Take();
        take.setId(Integer.parseInt(id));
        if (type.equals("1")) {
            take.setAudit(1);
        } else if (type.equals("2")) {
            take.setAudit(2);
        }
        //把当前序号之后的投稿序号+1
        //takeService.updateTakeByNumber(take);
        takeService.updateTakeByAudit(take);
    }


    /**
     * 查询投稿详情
     * @param id
     * @return
     */
    public TakeVo queryTakeById(Integer id) {
        return takeService.queryTakeById(id);
    }


    /**
     * 删除参赛人员
     *
     * @param id
     * @return
     */
    public int deleteTakePeople(int id) {
        return takeService.deleteTakePeople(id);
    }

    /**
     * 查询所有参赛人员
     *
     * @param paging
     * @return
     */
    public List<TakeVo> findAllTake(Paging<TakeVo> paging, String name, String audit, String activityid) {
        Take take = new Take();
        if (StringUtil.isNotEmpty(name)) {
            take.setName(name);
        }
        if (StringUtil.isNotEmpty(audit)) {
            take.setAudit(Integer.parseInt(audit));
        }
        if (StringUtil.isNotEmpty(activityid)) {
            take.setActiveid(Integer.parseInt(activityid));
        }
        return takeService.findAllTake(paging, take);
    }

    public List<TakeVo> findAll(int activeid) {
        List<TakeVo> list = takeService.findAll(activeid);
        list = pageFacade.getPageList(list, 1, 10);
        return list;
    }
    /**
     * 根据编号或名字查询
     *
     * @param paging
     * @param mark
     * @param nickname
     * @return
     */
    public List<TakeVo> findAllTakeCondition(Paging<TakeVo> paging, String mark, String nickname, String activeid) {
        Map map = new HashMap();
        if (StringUtil.isNotEmpty(mark)) {
            map.put("mark", mark);
        }
        if (StringUtil.isNotEmpty(nickname)) {
            map.put("nickname", nickname);
        }
        if (StringUtil.isNotEmpty(activeid)) {
            map.put("activeid", activeid);
        }
        return takeService.findAllTakeCondition(paging, map);
    }


    /**
     * 投票排行
     * @param paging
     * @return
     */
    public List<TakeVo> voteDesc(Paging<TakeVo> paging, int activeid) {

        return takeService.voteDesc(paging, activeid);
    }

    /**
     * 修改访问量
     *
     * @param activeid
     * @return
     */
    public int updatePageView(int activeid) {
        return activeH5Service.updatePageView(activeid);
    }

    /**
     * 首页数据
     *
     * @param activeid
     * @return
     */
    public ActiveH5Vo querySum(int activeid) {
        return activeH5Service.querySum(activeid);
    }

    /**
     * 查询活动说明
     *
     * @param activeid
     * @return
     */
    public ActiveH5 queryH5Describe(int activeid) {
        return activeH5Service.queryH5Describe(activeid);
    }

    /**
     * 时间到期
     *
     * @param activeid
     * @return
     */
    public int isTake(int activeid) {
        //查询活动的时间
        ActiveH5 activeH5 = activeH5Service.queryActivityById(activeid);
        Date beg = activeH5.getBegintime();
        Date end = activeH5.getEndtime();
        long begintime = beg.getTime();
        long endtime = end.getTime();
        int result = 0;
        Date date = new Date();
        long str = date.getTime();
        if (str < begintime || str > endtime) {
            return result;//灰色
        } else if (str > begintime && str < endtime) {
            result = 1;//正常
            return result;
        }
        return result;
    }

    /**
     * 投票记录
     *
     * @param
     * @return
     */
    public int insertSelectiveV(String activeid, String name, String takeid, String takenumber) {
        Votingrecords votingrecords = new Votingrecords();
        //在投票记录里面有没有此用户
        int count = votingrecordsService.queryHave(name);
        int result = 0;
        if (count == 0) {
            votingrecords.setIntime(new Date());
            if (StringUtil.isNotEmpty(name)) {
                votingrecords.setName(name);
            }
            if (StringUtil.isNotEmpty(activeid)) {
                votingrecords.setActiveid(Integer.parseInt(activeid));
            }
            if (StringUtil.isNotEmpty(takeid)) {
                votingrecords.setTakeid(Integer.parseInt(takeid));
            }
            if (StringUtil.isNotEmpty(takenumber)) {
                votingrecords.setTakenumber(Integer.parseInt(takenumber));
            }
            result = votingrecordsService.insertSelective(votingrecords);
        } else {
            result = -1;
        }
        return result;
    }


    /**
     * 投票多图上传
     *
     * @param file
     * @return
     */
    public List updatePostImgTest(MultipartFile[] file) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < file.length; i++) {
            Map m = movisionOssClient.uploadMultipartFile(file[i], 2);
            String url = String.valueOf(m.get("url"));
            Map map = new HashMap();
            map.put("name", FileUtil.getFileNameByUrl(url));
            list.add(map);
        }
        return list;
    }
}
