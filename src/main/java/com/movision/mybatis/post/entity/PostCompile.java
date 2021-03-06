package com.movision.mybatis.post.entity;

import com.movision.mybatis.goods.entity.Goods;
import com.movision.mybatis.goods.entity.GoodsVo;
import com.movision.mybatis.postLabel.entity.PostLabel;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用于帖子编辑数据回显
 *
 * @Author zhurui
 * @Date 2017/2/16 15:34
 */
public class PostCompile implements Serializable {

    private Integer id;//帖子id

    private String title;//帖子标题

    private String subtitle;//帖子副标题

    private Integer type;//帖子类型

    private Integer circleid;//圈子id

    private Integer category;//圈子分类

    private String circleidCN;//圈子中文

    private Integer ishot;//是否加精

    private Integer userid;//发帖人id

    private String nickname;//发帖人名

    private String coverimg;//首页图片

    private String vid;//视频文件

    private String bannerimgurl;//视频图片url

    private Integer isessence;//首页加精

    private Date time;//精选日期

    private Integer orderid;//精选排序

    private String postcontent;//帖子内容

    private List<GoodsVo> goodses;//商品

    private List<PostLabel> postLabels;//帖子标签

    public List<PostLabel> getPostLabels() {
        return postLabels;
    }

    public void setPostLabels(List<PostLabel> postLabels) {
        this.postLabels = postLabels;
    }

    public List<GoodsVo> getGoodses() {
        return goodses;
    }

    public void setGoodses(List<GoodsVo> goodses) {
        this.goodses = goodses;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getBannerimgurl() {
        return bannerimgurl;
    }

    public void setBannerimgurl(String bannerimgurl) {
        this.bannerimgurl = bannerimgurl;
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public Integer getIshot() {
        return ishot;
    }

    public void setIshot(Integer ishot) {
        this.ishot = ishot;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getCircleid() {
        return circleid;
    }

    public void setCircleid(Integer circleid) {
        this.circleid = circleid;
    }

    public String getCircleidCN() {
        return circleidCN;
    }

    public void setCircleidCN(String circleidCN) {
        this.circleidCN = circleidCN;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCoverimg() {
        return coverimg;
    }

    public void setCoverimg(String coverimg) {
        this.coverimg = coverimg;
    }

    public Integer getIsessence() {
        return isessence;
    }

    public void setIsessence(Integer isessence) {
        this.isessence = isessence;
    }

    public Integer getOrderid() {
        return orderid;
    }

    public void setOrderid(Integer orderid) {
        this.orderid = orderid;
    }

    public String getPostcontent() {
        return postcontent;
    }

    public void setPostcontent(String postcontent) {
        this.postcontent = postcontent;
    }
}
