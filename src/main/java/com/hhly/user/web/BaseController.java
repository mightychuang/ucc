package com.hhly.user.web;

import com.hhly.cache.service.RedisService;
import com.hhly.common.dto.ErrorCodeEnum;
import com.hhly.common.dto.ResultObject;
import com.hhly.common.dto.SessionUser;
import com.hhly.common.exception.ValidationException;
import com.hhly.common.util.*;
import com.hhly.user.api.constant.UserConstant;
import com.hhly.user.api.dto.request.HeaderReq;
import com.hhly.user.api.enums.UserErrorCodeEnum;
import com.hhly.user.utils.RequestHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
* @author wangxianchen
* @create 2017-08-24
* @desc 基础controller
*/
public class BaseController {

    private Logger logger = LoggerFactory.getLogger(BaseController.class);
    
    @Autowired
    RedisService redisService;

    /**
     * 线程变量
     */
    //public static ThreadLocal<HashMap<String,Object>> localMap = new ThreadLocal<HashMap<String,Object>>();


    /**
     * @desc
     * @author wangxianchen
     * @create 2017-09-23
     * @param request
     * @param isAllowNull 是否允许null,默认false不允许null当为null时抛异常,为ture 如果为null,则返回null
     * @return
     */
    public SessionUser getSessionUser(HttpServletRequest request,boolean isAllowNull){
        String key = getSessionUserKey(request);
        Object obj = redisService.get(key);
        SessionUser sessionUser = obj == null ? null : (SessionUser)obj;
        if(!isAllowNull && sessionUser == null){ //不允许null
            throw new ValidationException(UserErrorCodeEnum.NO_LOGIN);
        }
        return sessionUser;
    }


    /**
     * @desc 对请求数据体进行校验
     * @author wangxianchen
     * @create 2017-08-24
     * @param obj
     * @return
     */
    @Deprecated
    public ResultObject validate(Object obj){
        String validInfo = ValidatorUtil.validate(obj);
        ResultObject ro = new ResultObject();
        if(U.isNotBlank(validInfo)){ //未通过校验
            logger.error(ErrorCodeEnum.VERIFY_DATA_FAIL.message()+"parameters:{}\r\nvalidInfo:{}",validInfo,obj.toString());
            ro = new ResultObject();
            ro.fail(ErrorCodeEnum.VERIFY_DATA_FAIL);
            ro.setData(validInfo);
        }
        return ro;
    }


    /**
     * @desc 获取request header里的指定内容
     * @author wangxianchen
     * @create 2017-08-28
     * @param request
     * @return
     */
    public HeaderReq getHeaders(HttpServletRequest request,boolean checkNull) throws ValidationException {
       return RequestHeaderUtil.getHeaders(request,checkNull);
    }

    /**
     * @desc 获取当前登录的用户ID
     * @author wangxianchen
     * @create 2017-09-26
     * @return
     */
    public String getUserId(){
        String userId = UserUtil.getCurrentUserId();
        if(U.isBlank(userId)){
            throw new ValidationException(UserErrorCodeEnum.NO_LOGIN);
        }

        return userId;
    }

    /**
     * @desc 获取session 的key
     * @author wangxianchen
     * @create 2017-09-25
     * @param request
     * @return
     */
    public String getSessionUserKey(HttpServletRequest request){
        String secretKey = getHeaders(request,false).getSecretKey();
        return UserConstant.SESSION_USER_PREFIX + getUserId() +secretKey;
    }


}
