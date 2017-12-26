package com.hhly.user.mapper;

import com.hhly.user.entity.Resource;
import com.hhly.common.dao.BaseMapper;
import com.hhly.user.api.dto.response.MenuTree;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ResourceMapper extends BaseMapper<Resource> {


    List<MenuTree> queryNavigationTree(@Param("list")List<String> list,
                                       @Param("array") String[] array,
                                       @Param("state")Byte state,
                                       @Param("parentId")Integer  parentId,
                                       @Param("depth")Byte depth);


    MenuTree findTreeBySelfId(@Param("selfId")int selfId);


    int updateByCode(Resource resource);

    int updateByList(List<Resource> list);

    int selectExistCodeCount(@Param("list") List<String> list,@Param("appCode") String appCode);

    List<MenuTree> getAllMenus(@Param("appCode") String appCode,@Param("parentId") Integer parentId);

    int deleteByCodes(List<String> codes);

    List<Resource> selectByCodes(List<String> codes);

    int selectExistByAppCodeAndUrl(@Param("appCode") String appCode,@Param("url") String url);

    int getMaxSelfId();
}

