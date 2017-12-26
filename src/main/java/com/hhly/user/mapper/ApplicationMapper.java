package com.hhly.user.mapper;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.user.entity.Application;
import com.hhly.common.dao.BaseMapper;
import com.hhly.user.api.dto.response.ApplicationResp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApplicationMapper extends BaseMapper<Application> {

    int updateByCode(Application record);

    List<ApplicationResp> selectList(@Param("condition")Application condition, PageBounds pageBounds);
}