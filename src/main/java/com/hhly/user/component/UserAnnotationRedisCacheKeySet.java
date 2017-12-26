package com.hhly.user.component;

import com.hhly.cache.component.AnnotationRedisCacheKeySet;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangxianchen
 * @create 2017-11-03
 * @desc 注解形式缓存key的失效时间设置
 */
@Component
public class UserAnnotationRedisCacheKeySet extends AnnotationRedisCacheKeySet {

    @Override
    public Map<String, Long> getExpiresMap() {
        Map<String, Long> map = new HashMap<>();
        return map;
    }
}
