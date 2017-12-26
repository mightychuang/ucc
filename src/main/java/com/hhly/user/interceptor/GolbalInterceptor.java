package com.hhly.user.interceptor;

import com.hhly.cache.service.RedisService;
import com.hhly.common.spring.ApplicationContextHolder;
import com.hhly.common.util.LogUtil;
import com.hhly.common.util.U;
import com.hhly.user.api.UserURL;
import com.hhly.user.api.constant.UserConstant;
import com.hhly.user.api.dto.request.HeaderReq;
import com.hhly.user.utils.RequestHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wangxianchen
 * @create 2017-09-28
 * @desc 用户中心拦截器
 */
public class GolbalInterceptor implements HandlerInterceptor {

    private final RedisService redisService = ApplicationContextHolder.context.getBean(RedisService.class);
    private Logger logger = LoggerFactory.getLogger(GolbalInterceptor.class);

    /**
     * 在请求处理之前进行调用（Controller方法调用之前）
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getRequestURI();
        //非权限验证的就不做日志记录了
        HeaderReq header = RequestHeaderUtil.getHeaders(request,false);
        if(!url.equals(UserURL.AUTH_CHECK)){
            if(U.isNotBlank(header.getUserId())){
                //每一次访问后,刷新session存活时间
                String sessionUserKey = UserConstant.SESSION_USER_PREFIX + header.getUserId() + header.getSecretKey();
                redisService.resetExpire(sessionUserKey,UserConstant.LOGIN_SESSION_EXPIR_SECONDS);
            }
        }
        logger.info("intercept request:{},url={}",header,url);
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) throws Exception {
    }
}
