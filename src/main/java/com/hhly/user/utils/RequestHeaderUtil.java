package com.hhly.user.utils;

import com.hhly.common.constant.Constant;
import com.hhly.common.exception.ValidationException;
import com.hhly.common.util.*;
import com.hhly.user.api.constant.UserConstant;
import com.hhly.user.api.dto.request.HeaderReq;
import com.hhly.user.api.enums.UserErrorCodeEnum;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wangxianchen
 * @create 2017-09-28
 * @desc 获取请求头
 */
public class RequestHeaderUtil {

    public static HeaderReq getHeaders(HttpServletRequest request, boolean checkNull) throws ValidationException {
        HeaderReq header = new HeaderReq();
        header.setUserId(UserUtil.getCurrentUserId(request));
        header.setAppCode(request.getHeader(Constant.REQUEST_HEADER_KEY_APP));
        header.setPlatform(request.getHeader(Constant.REQUEST_HEADER_KEY_OS));
        header.setImei(request.getHeader(Constant.REQUEST_HEADER_KEY_IMEI));
        header.setUserAgent(request.getHeader(UserConstant.REQUEST_HEADER_OF_USER_AGENT));
        header.setIp(RequestUtils.getRealIp());
        if(checkNull){
            if(U.isBlank(header.getUserId()) ||
                    U.isBlank(header.getAppCode())||
                    U.isBlank(header.getPlatform())){
                throw new ValidationException(UserErrorCodeEnum.REQUEST_HEADER_PARAMETER_MISSING);
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append(header.getAppCode());
        sb.append(header.getPlatform());
        sb.append(U.isBlank(header.getImei()) ? "unkown" : header.getImei());
        sb.append(header.getUserAgent());
        header.setSecretKey(":"+ MD5.encode(sb.toString()));

        //LogUtil.ROOT_LOG.info("headerInfo={},MD5={}",sb.toString(),header.getSecretKey());

        return header;
    }
}
