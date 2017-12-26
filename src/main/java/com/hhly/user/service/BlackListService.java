package com.hhly.user.service;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.cache.service.RedisService;
import com.hhly.user.dao.BlackListDao;
import com.hhly.user.entity.BlackList;
import com.hhly.user.utils.UserConvert;
import com.hhly.common.constant.Constant;
import com.hhly.common.dto.ErrorCodeEnum;
import com.hhly.common.dto.ResultObject;
import com.hhly.common.util.UserUtil;
import com.hhly.user.api.dto.request.BlackQueryReq;
import com.hhly.user.api.dto.request.BlackReq;
import com.hhly.user.api.dto.response.BlackResp;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author pengchao
 * @create 2017-09-06
 * @desc
 */
@Service
public class BlackListService {
    @Autowired
    BlackListDao blackListDao;

    @Autowired
    RedisService redisService;

    /**
     * @desc 获取黑名单
     */
    public ResultObject get(int id) {
        BlackList blackList = blackListDao.selectByPrimaryKey(id);
        if (blackList == null) {
            return new ResultObject(ErrorCodeEnum.QUERY_DATA_ERROR);
        } else {
            ResultObject result = new ResultObject();
            result.setData(UserConvert.toBlack(blackList));
            return result;
        }
    }

    /**
     * @desc 新增黑名单
     */
    public ResultObject add(BlackReq blackReq) {
        BlackList blackList_old = blackListDao.selectUniqueness(blackReq);
        if (blackList_old == null) {
            BlackList blackList = UserConvert.toBlack(blackReq);
            blackList.setCreateUser(UserUtil.getCurrentUserId());
            Boolean isSuccess = blackListDao.insertSelective(blackList);
            ResultObject result = isSuccess ?
                    new ResultObject() :
                    new ResultObject(ErrorCodeEnum.FAIL);
            if (isSuccess) {
                insertRedis(blackList.getBlackName());
            }
            return result;
        } else {
            return new ResultObject(ErrorCodeEnum.INPUT_DATE_HAS_EXIST_ERROR);
        }
    }

    /**
     * @desc 更新黑名单
     */
    public ResultObject update(BlackReq blackReq) {
        BlackList blackList_old = blackListDao.selectUniqueness(blackReq);
        if (blackList_old == null) {
            return new ResultObject(ErrorCodeEnum.UPDATE_RECORD_NOT_EXIST_ERROR);
        }else{
            if (blackList_old.getId() != blackReq.getId()) {
                return new ResultObject(ErrorCodeEnum.PARAM_EXCEPTION.format("数据不匹配"));
            }

            BlackList blackList = UserConvert.toBlack(blackReq);
            blackList.setUpdateUser(UserUtil.getCurrentUserId());
            blackList.setId(blackReq.getId());

            Boolean isSuccess = blackListDao.updateByPrimaryKeySelective(blackList);
            ResultObject result = isSuccess ?
                    new ResultObject() :
                    new ResultObject(ErrorCodeEnum.FAIL);
            if(!blackList_old.getBlackName().equals(blackList.getBlackName())){
                insertRedis(blackList.getBlackName());
            }else{
                updateRedis(blackList_old.getBlackName(),blackList.getBlackName());
            }
            return result;
        }
    }

    /**
     * @desc 删除黑名单
     */
    public ResultObject delete(int id) {
        BlackList blackList_old = blackListDao.selectByPrimaryKey(id);
        if (blackList_old == null) {
            return new ResultObject(ErrorCodeEnum.PARAM_EXCEPTION.format("数据不匹配"));
        }else{
            Boolean isSuccess = blackListDao.deleteByPrimaryKey(id);
            ResultObject result = isSuccess ?
                    new ResultObject() :
                    new ResultObject(ErrorCodeEnum.FAIL);
            if (isSuccess) {
                removeRedis(blackList_old.getBlackName());
            }
            return result;
        }
    }

    /**
     * @param blackQueryReq
     * @return
     * @desc 获取黑名单分页列表
     */
    public ResultObject blackListPage(BlackQueryReq blackQueryReq, PageBounds pageBounds) {
        ResultObject result = new ResultObject();
        List<BlackList> blackLists = blackListDao.selectWithPage(blackQueryReq, pageBounds);
        List<BlackResp> blackResps = new ArrayList<BlackResp>();
        for (BlackList black :
                blackLists) {
            blackResps.add(UserConvert.toBlack(black));
        }
        result.setData(blackResps);
        return result;
    }

    /**
     * 刷新白名单
     */
    public void refreshBlackList(){
        List<BlackList> listResource = blackListDao.selectBlackList();
        Set<String> blackList = new HashSet<String>();
        for (BlackList item:
                listResource) {
            if(!StringUtils.isEmpty(item.getBlackName())){
                blackList.add(item.getBlackName());
            }
        }
        setRedis(blackList);
    }
    private Set<String> getRedis(){
        Set<String> blackSet = (Set<String>) redisService.get(Constant.CACHE_KEY_PREFIX_BLACKLIST);
        if (blackSet == null) {
            blackSet = new HashSet<String>();
        }
        return blackSet;
    }
    private void setRedis(Set<String> blackSet){
        redisService.set(Constant.CACHE_KEY_PREFIX_BLACKLIST, blackSet);
    }
    private void insertRedis(String blackName) {
        Set<String> blackSet = getRedis();
        blackSet.add(blackName);
        setRedis(blackSet);
    }
    private void removeRedis(String blackName) {
        Set<String> blackSet = getRedis();
        blackSet.remove(blackName);
        setRedis(blackSet);
    }
    private void updateRedis(String oldName,String newName){
        Set<String> blackSet = getRedis();
        blackSet.add(newName);
        blackSet.remove(oldName);
        setRedis(blackSet);
    }
}