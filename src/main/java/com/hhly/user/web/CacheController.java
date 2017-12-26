package com.hhly.user.web;

import com.hhly.cache.config.RedisConfig;
import com.hhly.common.dto.ResultObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

/**
 * @author wangxianchen
 * @create 2017-09-28
 * @desc 清除缓存
 */

@RestController
@RequestMapping("/usercenter")
public class CacheController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(CacheController.class);

    @Value("${spring.application.name}")
    private String appServiceName;

    @ResponseBody
    @RequestMapping(value = "/delCache/{key}",method = RequestMethod.GET)
    public ResultObject delete(@PathVariable String key){
        ResultObject ro = new ResultObject();
        RedisTemplate<String, Object> redisTemplate = super.redisService.getRedisTemplate();
        Set<String> keySet = redisTemplate.keys(key);
        if (!CollectionUtils.isEmpty(keySet)) {
            redisTemplate.delete(keySet);
        }
        ro.setData(keySet);
        return ro;
    }


    /**
     * @desc 清除本服务下myBatis缓存
     * @author wangxianchen
     * @create 2017-11-08
     * @return
     */
    @RequestMapping(value = "/clearMyBatisCache",method = RequestMethod.GET)
    public Object clearMyBatisCache(){
        String keyPattern = RedisConfig.MYBATIS_CACHE_PREFIX + appServiceName + ":*";
        logger.info("清除缓存,key={}",keyPattern);
        super.redisService.removePattern(keyPattern);
        return "success";
    }

    /**
     * @desc 清除本服务下spring注解缓存
     * @author wangxianchen
     * @create 2017-11-08
     * @return
     */
    @RequestMapping(value = "/clearAnnotaionCache",method = RequestMethod.GET)
    public Object clearAnnotaionCache(){
        String keyPattern = RedisConfig.ANNOTATION_CACHE_PREFIX + appServiceName + ":*";
        logger.info("清除缓存,key={}",keyPattern);
        super.redisService.removePattern(keyPattern);
        return "success";
    }

}
