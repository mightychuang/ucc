package com.hhly.user.mapper;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.user.entity.Role;
import com.hhly.common.dao.BaseMapper;
import com.hhly.user.api.dto.request.RoleQueryReq;
import com.hhly.user.api.dto.response.RoleShortInfoResp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleMapper extends BaseMapper<Role> {

    List<String> selectByRoleCodesAndAppCodes(@Param("array") String[] appCodes);

    List<RoleShortInfoResp> fuzzySelect(@Param("condition") RoleQueryReq condition, PageBounds pageBounds);

    int updateByCode(Role role);

    int deleteByCode(String code);

    int selectExistCodeCount(@Param("list") List<String> codeArray,@Param("isDelete")Byte isDelete,@Param("state")Byte state);

    List<String> selectEnableRoleCodes(@Param("list") List<String> list);

}