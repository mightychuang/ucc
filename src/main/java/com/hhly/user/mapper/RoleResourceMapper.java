package com.hhly.user.mapper;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.user.api.dto.request.RoleResQueryReq;
import com.hhly.user.api.dto.response.RoleBindResQueryResp;
import com.hhly.user.entity.RoleResource;
import com.hhly.common.dao.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleResourceMapper extends BaseMapper<RoleResource> {

    int deleteByRoleResourceCode(@Param("roleCode") String roleCode,@Param("list") List<String> list);

    int deleteByResourceCode(List<String> list);

    int deleteByRoleCodes(List<String> list);

    List<String> selectResCodesByRoleCodes(@Param("list")List<String> list);

    List<String> selectRoleCodesByResCodes(@Param("list")List<String> list);

    List<RoleBindResQueryResp> selectBindResource(@Param("condition")RoleResQueryReq req, PageBounds pageBounds);

    List<RoleBindResQueryResp> selectNotBindResource(@Param("condition")RoleResQueryReq req, PageBounds pageBounds);

    int insertBatch(List<RoleResource> list);
}