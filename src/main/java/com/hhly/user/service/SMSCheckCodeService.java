package com.hhly.user.service;

import com.hhly.cache.service.RedisService;
import com.hhly.user.api.constant.UserConstant;
import com.hhly.user.api.enums.UserErrorCodeEnum;
import com.hhly.common.dto.ErrorCodeEnum;
import com.hhly.common.exception.ValidationException;
import com.hhly.common.util.*;
import com.hhly.user.api.dto.CheckCode;
import com.hhly.user.api.dto.request.CheckCodeReq;
import com.hhly.user.api.enums.BizTypeEnum;
import com.hhly.user.api.enums.SmsTemplateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedList;

/**
* @author wangxianchen
* @create 2017-08-24
* @desc 手机短信验证码实现
*/
@Service
public class SMSCheckCodeService{

    private Logger logger = LoggerFactory.getLogger(SMSCheckCodeService.class);
    
    @Autowired
    RedisService redisService;

    private ThreadLocal<String> atomicKey = new ThreadLocal<String>();
    /**
     * @desc 发送验证码,判断两次之间是否超过间隔时间
     * @author wangxianchen
     * @create 2017-08-24
     * @param checkCodeReq
     * @throws ValidationException
     */
    public void option(CheckCodeReq checkCodeReq) throws ValidationException {
        String bizType = checkCodeReq.getBizType();
        String redisKey = A.joinStr(":", UserConstant.SMS_CHECK_CODE_PREFIX,checkCodeReq.getPhoneNo(),bizType);
        Object obj = redisService.get(redisKey);
        if(obj != null){
            CheckCode ccrv = null;
            try {
                ccrv = (CheckCode)obj;
                long seconds = DateUtil.getMillisBetweenSecod(ccrv.convertGeneraTime(),new Date());
                if(seconds < UserConstant.SMS_CHECK_CODE_INTERVAL_SECONDS){ //两次获取验证码时间间隔小于默认间隔期
                    throw new ValidationException(UserErrorCodeEnum.NOT_EXCEEDING_TIME_LIMIT,ccrv);
                }
            } catch (NumberFormatException e) {
                logger.error("解析redis里的验证码时出错",e);
                throw new ValidationException(ErrorCodeEnum.PARSE_DATA_ERROR,ccrv);
            }
        }

        generateCode(redisKey,checkCodeReq.getPhoneNo(),bizType);
    }


    /**
     * @desc 产生验证码,发送并保存至redis
     * @author wangxianchen
     * @create 2017-08-24
     * @param redisKey
     * @param mobile
     * @param bizType
     * @return
     * @throws ValidationException
     */
    public String generateCode(String redisKey,String mobile,String bizType) throws ValidationException{
        //产生验证码,并发送,再将其保存至redis
        String checkCode = RandomCodeUtil.generateRandomNum(RandomCodeUtil.RANDOM_CODE_LENGTH);
        logger.error("验证码:{}={}",redisKey,checkCode);
        String tempId = null;
        LinkedList<String> params = new LinkedList<>();
        params.add(checkCode);
        params.add(String.valueOf(UserConstant.SMS_CHECK_CODE_EXPIR_SECONDS/60));
        if(bizType.equals(BizTypeEnum.LOGIN.name())){
            tempId = SmsTemplateEnum.LOGIN.getId();
        }else if(bizType.equals(BizTypeEnum.REGIST.name())){
            tempId = SmsTemplateEnum.REGIST.getId();
        }else if(bizType.equals(BizTypeEnum.FORGET_PWD.name())){
            tempId = SmsTemplateEnum.FORGET_PWD.getId();
        }else{
            throw new ValidationException("业务类型不存在");
        }

        try {
            //短信是否真正发送出去,并不知道,此处暂不作处理
            String result = SendSmsUtil.sendTemplateSms(mobile, tempId, params);
            String status = U.getStrFromXml(result,"status");
            if(!"0".equals(status)){
                throw new ValidationException(UserErrorCodeEnum.CHECK_CODE_SEND_ERROR);
            }
        } catch (Exception e) {
            logger.error(UserErrorCodeEnum.CHECK_CODE_SEND_ERROR.message(),e);
            throw new ValidationException(UserErrorCodeEnum.CHECK_CODE_SEND_ERROR);
        }
        CheckCode ccrv = new CheckCode();
        ccrv.setCode(checkCode);
        ccrv.setGeneraTime(new Date());
        if(!redisService.set(redisKey,ccrv,UserConstant.SMS_CHECK_CODE_EXPIR_SECONDS)){
            logger.error("保存验证码到redis异常 key={} value={}",redisKey,ccrv.toJSONString());
            throw new ValidationException(ErrorCodeEnum.SAVE_DATA_ERROR,"保存验证码出错");
        }
     return checkCode;
    }

    /**
     * @desc 核对验证码,并记录错误输入次数
     * @author wangxianchen
     * @create 2017-08-24
     * @param phoneNo
     * @param bizType
     * @param checkCode
     * @return
     * @throws ValidationException
     */
    public boolean  checkCode(String phoneNo,String bizType, String checkCode)throws ValidationException {
        String redisKey = A.joinStr(":",UserConstant.SMS_CHECK_CODE_PREFIX,phoneNo,bizType);
        atomicKey.set(redisKey);
        Object obj = redisService.get(redisKey);
        if(obj != null && checkCode != null){
            CheckCode ccrv = (CheckCode)obj;
            if(checkCode.equalsIgnoreCase(ccrv.getCode())){
                return true;
            }
            ccrv.setWrongCount(ccrv.getWrongCount()+1);
            redisService.set(redisKey,ccrv);
        }
        logger.error("验证码核对不上. parameters:{}",redisKey);
        return false;
    }

    /**
     * @desc 删除验证码,在同一个业务中，上一步验证了验证码，待业务成功后需要删除验证码。失败则不删除。
     * @author wangxianchen
     * @create 2017-11-23
     */
    public void deleteCheckCode(){
        redisService.remove(atomicKey.get());
    }


}
