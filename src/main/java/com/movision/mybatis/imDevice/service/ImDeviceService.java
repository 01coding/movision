package com.movision.mybatis.imDevice.service;

import com.movision.mybatis.imDevice.entity.ImDevice;
import com.movision.mybatis.imDevice.mapper.ImDeviceMapper;
import com.movision.mybatis.imuser.entity.ImdeviceAppuser;
import com.movision.utils.ListUtil;
import org.apache.xpath.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author zhuangyuhao
 * @Date 2017/5/3 9:30
 */
@Service
@Transactional
public class ImDeviceService {

    private static Logger log = LoggerFactory.getLogger(ImDeviceService.class);

    @Autowired
    private ImDeviceMapper imDeviceMapper;

    public Boolean isExistDevice(String deviceid) {
        try {
            log.info("判断是否存该设备号的accid");
            Integer count = imDeviceMapper.isExistDevice(deviceid);
            Boolean flag = count > 0;
            return flag;
        } catch (Exception e) {
            log.error("判断是否存该设备号的accid失败", e);
            throw e;
        }
    }

    public ImDevice selectByDevice(String deviceid) {
        try {
            log.info("根据设备号查找对应的云信账户");
            return imDeviceMapper.selectByDevice(deviceid);
        } catch (Exception e) {
            log.error("根据设备号查找对应的云信账户失败", e);
            throw e;
        }
    }

    public Integer add(ImDevice imDevice) {
        try {
            log.info("新增设备im用户");
            return imDeviceMapper.insertSelective(imDevice);
        } catch (Exception e) {
            log.error("新增设备im用户失败", e);
            throw e;
        }
    }

    public int delete(Integer id) {
        try {
            log.info("删除设备im用户");
            return imDeviceMapper.deleteByPrimaryKey(id);
        } catch (Exception e) {
            log.error("删除设备im用户失败", e);
            throw e;
        }
    }

    public int updateImDevice(ImDevice imDevice) {
        try {
            log.info("更新im和设备号的关系");
            return imDeviceMapper.updateImDevice(imDevice);
        } catch (Exception e) {
            log.error("更新im和设备号的关系失败", e);
            throw e;
        }
    }

    public List<ImdeviceAppuser> selectRelatedAppuserAndImdevice() {
        try {
            log.info("查询相关联的app用户和设备im账号");
            return imDeviceMapper.selectRelatedAppuserAndImdevice();
        } catch (Exception e) {
            log.error("查询相关联的app用户和设备im账号失败", e);
            throw e;
        }
    }


}
