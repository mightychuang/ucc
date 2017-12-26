package com.hhly.user.mapper;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.user.entity.BlackList;
import com.hhly.common.dao.BaseMapper;
import com.hhly.user.api.dto.request.BlackQueryReq;
import com.hhly.user.api.dto.request.BlackReq;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface BlackListMapper extends BaseMapper<BlackList> {
    List<BlackList> selectWithPage(@Param("condition") BlackQueryReq condition, PageBounds pageBounds);

    /**
     * 获取唯一实体
     * @param blackReq
     * @return
     */
    BlackList selectUniqueness(BlackReq blackReq);

    /**
     * 获取黑名单名单列表
     * @return
     */
    List<BlackList> selectBlackList();
}