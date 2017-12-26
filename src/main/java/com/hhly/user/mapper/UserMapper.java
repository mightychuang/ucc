package com.hhly.user.mapper;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.common.dao.BaseMapper;
import com.hhly.user.api.dto.request.CountRegUserReq;
import com.hhly.user.api.dto.request.UserQueryReq;
import com.hhly.user.api.dto.response.CountRegUserResp;
import com.hhly.user.api.dto.response.UserDetailResp;
import com.hhly.user.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author wangxianchen
* @create 2017-08-24
* @desc 可以自定义方法
*/
public interface UserMapper extends BaseMapper<User>{

    int updateByUserIdSelective(User user);

    List<UserDetailResp> selectUserWithPage(@Param("condition") UserQueryReq condition, PageBounds pageBounds);

    List<CountRegUserResp> countRegUserByDays(@Param("condition") CountRegUserReq condition);

    Integer countRegUser(@Param("accType") String accType);

}