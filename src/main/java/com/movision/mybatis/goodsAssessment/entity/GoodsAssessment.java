package com.movision.mybatis.goodsAssessment.entity;

import java.io.Serializable;
import java.util.Date;

public class GoodsAssessment implements Serializable {
    private Integer id;

    private Integer userid;

    private Integer goodid;

    private Integer suborderid;

    private Integer pid;

    private Integer goodpoint;

    private Integer logisticpoint;

    private Integer servicepoint;

    private String content;

    private Integer isimage;

    private Integer isanonymity;

    private Date createtime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getGoodid() {
        return goodid;
    }

    public void setGoodid(Integer goodid) {
        this.goodid = goodid;
    }

    public Integer getSuborderid() {
        return suborderid;
    }

    public void setSuborderid(Integer suborderid) {
        this.suborderid = suborderid;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getGoodpoint() {
        return goodpoint;
    }

    public void setGoodpoint(Integer goodpoint) {
        this.goodpoint = goodpoint;
    }

    public Integer getLogisticpoint() {
        return logisticpoint;
    }

    public void setLogisticpoint(Integer logisticpoint) {
        this.logisticpoint = logisticpoint;
    }

    public Integer getServicepoint() {
        return servicepoint;
    }

    public void setServicepoint(Integer servicepoint) {
        this.servicepoint = servicepoint;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content == null ? null : content.trim();
    }

    public Integer getIsimage() {
        return isimage;
    }

    public void setIsimage(Integer isimage) {
        this.isimage = isimage;
    }

    public Integer getIsanonymity() {
        return isanonymity;
    }

    public void setIsanonymity(Integer isanonymity) {
        this.isanonymity = isanonymity;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }
}