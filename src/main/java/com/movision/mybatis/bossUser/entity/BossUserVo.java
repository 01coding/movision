package com.movision.mybatis.bossUser.entity;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 用于封装boss用户 【新增/修改】 接口中的传参
 * @Author zhuangyuhao
 * @Date 2017/2/8 15:57
 */
@ApiModel(value = "编辑用户的接口对象", description = "编辑用户的接口对象")
public class BossUserVo implements Serializable {
    @ApiModelProperty(value = "用户id")
    private Integer id;

    @ApiModelProperty(value = "真实姓名")
    private String name;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "手机号")
    private String phone;

    @ApiModelProperty(value = "是否是超级管理员：0 否 1 是")
    private Integer issuper;

    @ApiModelProperty(value = "新设置的密码")
    private String newPassword;

    @ApiModelProperty(value = "用户对应的角色id")
    private String roleid;

    @ApiModelProperty(value = "是否是圈主 0否 1是")
    private Integer iscircle;

    @ApiModelProperty(value = "是否是圈子管理员 0否 1是")
    private Integer circlemanagement;

    @ApiModelProperty(value = "是否是特约嘉宾 0否 1是")
    private Integer contributing;
    
    @ApiModelProperty(value = "是否是普通管理员 0否 1是")
    private Integer common;

    public Integer getIscircle() {
        return iscircle;
    }

    public void setIscircle(Integer iscircle) {
        this.iscircle = iscircle;
    }

    public Integer getCirclemanagement() {
        return circlemanagement;
    }

    public void setCirclemanagement(Integer circlemanagement) {
        this.circlemanagement = circlemanagement;
    }

    public Integer getContributing() {
        return contributing;
    }

    public void setContributing(Integer contributing) {
        this.contributing = contributing;
    }

    public Integer getCommon() {
        return common;
    }

    public void setCommon(Integer common) {
        this.common = common;
    }

    public void setIssuper(Integer issuper) {
        this.issuper = issuper;
    }

    public Integer getIssuper() {

        return issuper;
    }

    public void setRoleid(String roleid) {
        this.roleid = roleid;
    }

    public String getRoleid() {

        return roleid;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {

        return name;
    }

    public Integer getId() {

        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPhone() {
        return phone;
    }

    public String getNewPassword() {
        return newPassword;
    }

    @Override
    public String toString() {
        return "BossUserVo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", phone='" + phone + '\'' +
                ", issuper=" + issuper +
                ", newPassword='" + newPassword + '\'' +
                ", roleid='" + roleid + '\'' +
                ", iscircle=" + iscircle +
                ", circlemanagement=" + circlemanagement +
                ", contributing=" + contributing +
                ", common=" + common +
                '}';
    }
}
