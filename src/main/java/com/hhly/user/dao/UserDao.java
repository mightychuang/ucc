package com.hhly.user.dao;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.common.dao.AbstractBaseDao;
import com.hhly.user.api.dto.request.CountRegUserReq;
import com.hhly.user.api.dto.request.UserQueryReq;
import com.hhly.user.api.dto.response.CountRegUserResp;
import com.hhly.user.api.dto.response.UserDetailResp;
import com.hhly.user.entity.User;
import com.hhly.user.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author wangxianchen
 * @create 2017-08-24
 * @desc 用户处理
 */
@Repository
public class UserDao extends AbstractBaseDao<UserMapper,User>{


    public User selectByUserId(String userId){
        User temp = new User();
        temp.setUuid(userId);
        return super.selectOneSelective(temp);
    }

    public boolean updateByUserIdSelective(User user){
       return baseMapper.updateByUserIdSelective(user) == 1 ? true : false;
    }

    public List<UserDetailResp> selectUserWithPage(UserQueryReq condition, PageBounds pageBounds){
        return baseMapper.selectUserWithPage(condition, pageBounds);
    }

    public List<CountRegUserResp> countRegUserByDays(CountRegUserReq condition){
        return baseMapper.countRegUserByDays(condition);
    }

    public Integer countRegUser(String accType){
        return baseMapper.countRegUser(accType);
    }

}
