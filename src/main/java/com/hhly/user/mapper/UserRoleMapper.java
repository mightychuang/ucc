package com.hhly.user.mapper;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.user.api.dto.request.UserRoleQueryReq;
import com.hhly.user.api.dto.response.UserRoleQueryResp;
import com.hhly.user.entity.UserRole;
import com.hhly.common.dao.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserRoleMapper extends BaseMapper<UserRole>{

    int insertBatch(List<UserRole> list);

    int deleteByUserId(String userId);

    int deleteByRoleCodes(@Param("list") List<String> roleCodes);

    int deleteByUserRoleCodes(@Param("userId") String userId,@Param("list") List<String> roleCodes);

    List<String> selectExistRoleCodes(@Param("userId") String userId,@Param("list") List<String> roleCodes);

    int selectExistRoleCount(@Param("userId") String userId,@Param("list") List<String> roleCodes);

    List<UserRoleQueryResp> selectBindByUserId(@Param("condition") UserRoleQueryReq req, @Param("array") String[] appCodes,PageBounds pageBounds);

    List<UserRoleQueryResp> selectNotBindByUserId(@Param("condition") UserRoleQueryReq req,@Param("array") String[] appCodes,PageBounds pageBounds);



}