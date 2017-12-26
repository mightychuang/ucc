package com.hhly.user.component;

import com.hhly.cache.component.MybatisRedisCache;
import org.springframework.stereotype.Component;

/**
 * @author wangxianchen
 * @create 2017-09-15
 * @desc redis实现mybatis的二级缓存
 */
@Component
public class UserMybatisRedisCache extends MybatisRedisCache {

    public UserMybatisRedisCache(){
        super();
    }
    public UserMybatisRedisCache(String id) {
        super(id);
    }
}