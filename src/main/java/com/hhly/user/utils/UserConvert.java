package com.hhly.user.utils;

import com.hhly.user.entity.BlackList;
import com.hhly.common.util.DateUtil;
import com.hhly.user.api.dto.request.BlackReq;
import com.hhly.user.api.dto.response.BlackResp;

import java.util.Date;

/**
 * @author hhly-pc
 * @create 2017-09-06
 * @desc
 */
public class UserConvert {

    public static BlackList toBlack(BlackReq blackReq){
        BlackList blackList = new BlackList();
        blackList.setBlackName(blackReq.getBlackName());
        blackList.setBlackType(blackReq.getBlackType());
        //blackList.setForever(blackReq.getForever());
        blackList.setDescription(blackReq.getDescription());

        if(blackReq.getTimeOut() <=0){
            blackList.setExpirartionTime(DateUtil.converateDate("2037-12-31 23:59:59"));
            blackList.setForever(1);
        }else{
            blackList.setExpirartionTime(DateUtil.getDateAfterDays(new Date(),blackReq.getTimeOut()));
            blackList.setForever(0);
        }
        return blackList;
    }

    public static BlackResp toBlack(BlackList blackList){
        BlackResp blackResp = new BlackResp();
        blackResp.setBlackName(blackList.getBlackName());
        blackResp.setBlackType(blackList.getBlackType());
        blackResp.setForever(blackList.getForever());
        blackResp.setDescription(blackList.getDescription());

        if(blackList.getExpirartionTime() != null){
            blackResp.setExpirartionTime(blackList.getExpirartionTime());
        }
        return blackResp;
    }
}
