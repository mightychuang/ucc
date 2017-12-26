package com.hhly.user.service;

import com.alibaba.fastjson.JSON;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.common.dto.ResultObject;
import com.hhly.common.exception.ValidationException;
import com.hhly.common.util.LogUtil;
import com.hhly.common.util.U;
import com.hhly.user.api.dto.request.AppAddReq;
import com.hhly.user.api.dto.request.AppQueryReq;
import com.hhly.user.api.dto.request.AppUpdateReq;
import com.hhly.user.api.dto.response.ApplicationResp;
import com.hhly.user.dao.ApplicationDao;
import com.hhly.user.entity.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wangxianchen
 * @create 2017-09-27
 * @desc application操作服务
 */
@Service
public class AppService {

    private Logger logger = LoggerFactory.getLogger(AppService.class);

    @Autowired
    private ApplicationDao appDao;

    /**
     * @desc 分页列表查询
     * @author wangxianchen
     * @create 2017-09-27
     * @param req
     * @param pageBounds
     * @return
     */
    @Transactional(readOnly=true)
    public ResultObject selectList(AppQueryReq req, PageBounds pageBounds){
        ResultObject ro = new ResultObject();
        Application temp = new Application();
        temp.setCode(req.getAppCode());
        temp.setName(req.getAppName());
        temp.setState(req.getState());
        List<ApplicationResp> list = appDao.selectList(temp,pageBounds);
        ro.setData(list);
        return ro;
    }

    /**
     * @desc 查询单个明细
     * @author wangxianchen
     * @create 2017-09-27
     * @param appCode
     * @return
     */
    @Transactional(readOnly=true)
    public ResultObject detail(String appCode){
        ResultObject ro = new ResultObject();
        Application app = appDao.getAvailableAppByCode(appCode);
        app.setId(null);
        ro.setData(app);
        return ro;
    }

    /**
     * @desc 新增
     * @author wangxianchen
     * @create 2017-09-27
     * @param req
     * @param operatorId
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void add(AppAddReq req,String operatorId){
        Application app = appDao.selectByCode(req.getAppCode());
        if(app != null){
            throw new ValidationException("应用编码已存在,请重新编辑");
        }
        Application temp = new Application();
        temp.setUuid(U.uuid());
        temp.setCode(req.getAppCode());
        temp.setName(req.getAppName());
        temp.setState(Byte.valueOf(req.getState()));
        temp.setCreateUser(operatorId);
        appDao.insertSelective(temp);
        logger.info("操作人:{},新增了一个应用:{}",operatorId, JSON.toJSON(temp));
    }

    /**
     * @desc 更新
     * @author wangxianchen
     * @create 2017-09-27
     * @param req
     * @param operatorId
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void update(AppUpdateReq req, String operatorId){
        Application temp = new Application();
        temp.setCode(req.getAppCode());
        temp.setName(req.getAppName());
        temp.setState(Byte.valueOf(req.getState()));
        temp.setUpdateUser(operatorId);
        appDao.updateByCode(temp);
        logger.info("操作人:{},更新了一个应用:{}",operatorId, JSON.toJSON(temp));
    }

}
