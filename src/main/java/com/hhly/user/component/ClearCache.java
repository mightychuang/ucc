package com.hhly.user.component;

import com.hhly.cache.config.RedisConfig;
import com.hhly.cache.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @author wangxianchen
 * @create 2017-11-27
 * @desc 清除缓存在应用启动后执行
 */
@Component
public class ClearCache implements CommandLineRunner {
    private Logger logger = LoggerFactory.getLogger(ClearCache.class);

    @Autowired
    RedisService redisService;

    @Value("${spring.application.name}")
    private String appServiceName;

    @Override
    public void run(String... strings) throws Exception {
        String keyPattern = RedisConfig.MYBATIS_CACHE_PREFIX + appServiceName + ":*";
        logger.info("清除缓存,key={}",keyPattern);
        redisService.removePattern(keyPattern);
    }
}
