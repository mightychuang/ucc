package com.hhly.user.dao;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.user.entity.BlackList;
import com.hhly.user.mapper.BlackListMapper;
import com.hhly.common.dao.AbstractBaseDao;
import com.hhly.user.api.dto.request.BlackQueryReq;
import com.hhly.user.api.dto.request.BlackReq;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author pengchao
 * @create 2017-09-06
 * @desc 黑名单处理
 */
@Repository
public class BlackListDao extends AbstractBaseDao<BlackListMapper,BlackList>{
    public List<BlackList> selectWithPage(BlackQueryReq condition, PageBounds pageBounds){
        return baseMapper.selectWithPage(condition, pageBounds);
    }

    public BlackList selectUniqueness(BlackReq blackReq){
        return baseMapper.selectUniqueness(blackReq);
    }

    /**
     * 获取黑名单名单列表
     * @return
     */
    public List<BlackList> selectBlackList(){
        return baseMapper.selectBlackList();
    }
}