package com.hhly.user.dao;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.common.util.A;
import com.hhly.user.api.constant.UserConstant;
import com.hhly.user.api.dto.request.UserRoleQueryReq;
import com.hhly.user.api.dto.response.UserRoleQueryResp;
import com.hhly.user.entity.UserRole;
import com.hhly.common.dao.AbstractBaseDao;
import com.hhly.user.mapper.UserRoleMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* @author wangxianchen
* @create 2017-11-27
* @desc 用户角色处理
*/
@Repository
public class UserRoleDao extends AbstractBaseDao<UserRoleMapper,UserRole> {


    /**
     * @desc 批量插入角色
     * @author wangxianchen
     * @create 2017-08-31
     * @param list
     * @return
     */
    public int insertBatch(List<UserRole> list){
        return baseMapper.insertBatch(list);
    }


    public int deleteByUserRoleCodes(String userId,List<String> roleCodeList){
        return baseMapper.deleteByUserRoleCodes(userId,roleCodeList);
    }

    public int deleteByRoleCodes(List<String> roleCodeList){
        return baseMapper.deleteByRoleCodes(roleCodeList);
    }

    public int deleteByRoleCode(String roleCode){
        List<String> roleCodeList = new ArrayList<>();
        roleCodeList.add(roleCode);
        return this.deleteByRoleCodes(roleCodeList);
    }


    public List<String> selectExistRoleCodes(String userId,List<String> roleCodeList){
        return baseMapper.selectExistRoleCodes(userId,roleCodeList);
    }


    /**
     * @desc 查询已绑定的角色信息
     * @author wangxianchen
     * @create 2017-09-21
     * @param req
     * @return
     */
    public List<UserRoleQueryResp> selectBindByUserId(UserRoleQueryReq req,String[] appCodes, PageBounds pageBounds){
        return baseMapper.selectBindByUserId(req,appCodes,pageBounds);
    }

    /**
     * @desc 查询未绑定的角色信息
     * @author wangxianchen
     * @create 2017-09-21
     * @param req
     * @param pageBounds
     * @return
     */
    public List<UserRoleQueryResp> selectNotBindByUserId(UserRoleQueryReq req,String[] appCodes,PageBounds pageBounds){
        return baseMapper.selectNotBindByUserId(req,appCodes,pageBounds);
    }

    /**
     * 查询用户所有角色编码
     * @param userId
     * @return
     */
    public List<String> selectRoleCodesByUserId(String userId){
        return selectExistRoleCodes(userId,null);
    }
}
