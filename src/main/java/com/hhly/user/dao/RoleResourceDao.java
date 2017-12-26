package com.hhly.user.dao;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.common.dao.AbstractBaseDao;
import com.hhly.common.util.A;
import com.hhly.user.api.dto.request.RoleResQueryReq;
import com.hhly.user.api.dto.response.RoleBindResQueryResp;
import com.hhly.user.entity.RoleResource;
import com.hhly.user.mapper.RoleResourceMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
* @author wangxianchen
* @create 2017-11-27
* @desc 角色资源处理
*/
@Repository
public class RoleResourceDao extends AbstractBaseDao<RoleResourceMapper,RoleResource> {

    public int deleteByRoleCode(String code){
        List<String> list = new ArrayList<>();
        list.add(code);
        return baseMapper.deleteByRoleCodes(list);
    }

    public int deleteByRoleResourceCode(String roleCode,List<String> codeList){
        return baseMapper.deleteByRoleResourceCode(roleCode,codeList);
    }

    public int deleteByResourceCode(List<String> codeList){
        return baseMapper.deleteByResourceCode(codeList);
    }

    public List<String> selectResCodesByRoleCodes(List<String> list){
        if(A.isEmpty(list)){
            return null;
        }
        return baseMapper.selectResCodesByRoleCodes(list);
    }

    /**
     * @desc 查询角色 拥有/未拥有的资源
     * @author zhangshuqing
     * @create 2017-09-22
     * @param req
     * @param pageBounds
     * @return
     */
    public List<RoleBindResQueryResp> selectBindResource(RoleResQueryReq req, PageBounds pageBounds) {
        return baseMapper.selectBindResource(req,pageBounds);
    }

    public List<RoleBindResQueryResp> selectNotBindResource(RoleResQueryReq req, PageBounds pageBounds) {
        return baseMapper.selectNotBindResource(req,pageBounds);
    }

    /**
     * @desc 批量插入
     * @author wangxianchen
     * @create 2017-09-27
     * @param list
     * @return
     */
    public int insertBatch(List<RoleResource> list){
        return baseMapper.insertBatch(list);
    }
}
