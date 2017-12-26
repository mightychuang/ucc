package com.hhly.user.web;


import com.alibaba.fastjson.JSON;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.user.api.constant.UserConstant;
import com.hhly.user.api.dto.request.*;
import com.hhly.user.api.enums.*;
import com.hhly.common.dto.ErrorCodeEnum;
import com.hhly.common.dto.ResultObject;
import com.hhly.common.dto.SessionUser;
import com.hhly.common.exception.ValidationException;
import com.hhly.common.util.*;
import com.hhly.user.api.UserURL;
import com.hhly.user.service.SMSCheckCodeService;
import com.hhly.user.service.impl.AbstractUserService;
import com.hhly.user.service.impl.GeneralUserService;
import com.hhly.user.service.impl.LawyerUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
* @author wangxianchen
* @create 2017-08-24
* @desc 帐户基本控制入口
*/
@RestController
public class UserController extends BaseController{

    @Autowired
    private SMSCheckCodeService smsCheckCodeService;

    @Autowired
    LawyerUserService lawyerUserService;

    @Autowired
    GeneralUserService generalUserService;


    /**
     * @desc 登录
     * @author wangxianchen
     * @create 2017-08-24
     * @param loginReq
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_LOGIN,method = RequestMethod.POST)
    public ResultObject login(@RequestBody  @Validated LoginReq loginReq, HttpServletRequest request) throws Exception{
        HeaderReq headerReq = super.getHeaders(request,false);
        return invokingUserService(headerReq.getAppCode()).login(loginReq,headerReq);
    }


    /**
     * @desc 登出
     * @author wangxianchen
     * @create 2017-08-24
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_LOGOUT,method = RequestMethod.POST)
    public ResultObject logout(HttpServletRequest request) {
         HeaderReq header = super.getHeaders(request,false);
         invokingUserService(header.getAppCode()).updateUserOnLineState(header);
         return new ResultObject();
    }


    /**
     * @desc 注册
     * @author wangxianchen
     * @create 2017-08-24
     * @param regReq
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_REGISTER,method = RequestMethod.POST)
    public ResultObject register(@RequestBody @Validated RegReq regReq, HttpServletRequest request) throws Exception {
        HeaderReq header = super.getHeaders(request,false);
        //如果不是从用户端或律师端注册
        if(!AppEnum.LAWYER.name().equals(header.getAppCode()) &&
                !AppEnum.USER.name().equals(header.getAppCode())){
            throw new ValidationException(UserErrorCodeEnum.ILLEGAL_OPERATION, JSON.toJSON(regReq));
        }
        if(regReq.getRegType().equals(UserEnums.REG_TYPE.MOBILE_PHONE_NO.getCode().toString())){
            //验证码失败
            if(!smsCheckCodeService.checkCode(regReq.getRegName(), BizTypeEnum.REGIST.name(),regReq.getCheckCode())){
                throw new ValidationException(UserErrorCodeEnum.CHECK_CODE_ERR,regReq);
            }
        }else{
            throw new ValidationException(UserErrorCodeEnum.UNSUPPORT_REG_TYPE,regReq);
        }
        ResultObject ro = invokingUserService(header.getAppCode()).register(regReq,header);
        if(ro.isSuccess()){
            smsCheckCodeService.deleteCheckCode();
        }
        return ro;
    }

    /**
     * @desc 发送验证码
     * @author wangxianchen
     * @create 2017-08-24
     * @param checkCodeReq
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_SEND_CHECK_CODE,method = RequestMethod.POST)
    public ResultObject sendCheckCode(@RequestBody @Validated CheckCodeReq checkCodeReq){
        smsCheckCodeService.option(checkCodeReq);
        return new ResultObject();
    }

    /**
     * @desc 获取图片验证码
     * @author wangxianchen
     * @create 2017-09-12
     * @param request
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_GET_PIC_CHECK_CODE,method = RequestMethod.GET)
    public void getPicCheckCode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pid = request.getParameter("pid");
        if(StringUtils.isEmpty(pid)){
            throw new ValidationException(ErrorCodeEnum.FAIL,"pid不能为空");
        }
        SecurityCodeUtil.Code code = SecurityCodeUtil.generateCode("4","n","100","30");
        String content = code.getContent();
        ImageIO.write(code.getImage(),"png",response.getOutputStream());
        redisService.set(UserConstant.PIC_CHECK_CODE_PREFIX+pid,content,Long.valueOf(UserConstant.PIC_CHECK_CODE_EXPIR_SECONDS));
    }

    /**
     * @desc 更新用户信息
     * @author wangxianchen
     * @create 2017-08-24
     * @param req
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_UPDATE,method = RequestMethod.POST)
    public ResultObject update(@RequestBody @Validated UpdateUserReq req,HttpServletRequest request) throws Exception{
        SessionUser sessionUser = super.getSessionUser(request,false);
        HeaderReq header = super.getHeaders(request,false);
        return invokingUserService(header.getAppCode()).updateAccount(req,sessionUser.getUserId());
    }

    /**
     * @desc 更新用户认证状态
     * @author wangxianchen
     * @create 2017-11-24
     * @param req
     * @param request
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_UPDATE_AUTH,method = RequestMethod.POST)
    public ResultObject updateAuthState(@RequestBody @Validated UpdateUserAuthReq req,HttpServletRequest request) throws Exception{
        SessionUser sessionUser = super.getSessionUser(request,false);
        HeaderReq header = super.getHeaders(request,false);
        return invokingUserService(header.getAppCode()).updateAuthState(req,sessionUser.getUserId());
    }


    /**
     * @desc 变更密码
     * @author wangxianchen
     * @create 2017-08-24
     * @param modifyPwdReq
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_CHANGE_PWD,method = RequestMethod.POST)
    public ResultObject changePwd(@RequestBody @Validated ModifyPwdReq modifyPwdReq, HttpServletRequest request) {
        SessionUser sessionUser = super.getSessionUser(request,false);
        //验证码失败
        //if(!smsCheckCodeService.checkCode(sessionUser.getPhoneNo(), BizTypeEnum.CHANGE_PWD.name(), modifyPwdReq.getCheckCode())){
        //    throw new ValidationException(UserErrorCodeEnum.CHECK_CODE_ERR,modifyPwdReq);
        //}
        HeaderReq header = super.getHeaders(request,false);
        ResultObject ro = invokingUserService(header.getAppCode()).changePwd(sessionUser, modifyPwdReq);
/*        if(ro.isSuccess()){
            smsCheckCodeService.deleteCheckCode();
        }*/
        return ro;
    }

    /**
     * @desc 验证用户是否存在
     * @author wangxianchen
     * @create 2017-08-24
     * @param loginName
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_CHECK_EXIST,method = RequestMethod.GET)
    public ResultObject checkExist(@RequestParam String loginName, HttpServletRequest request) {
        HeaderReq header = super.getHeaders(request,false);
        return invokingUserService(header.getAppCode()).checkUserExist(loginName);
    }

    /**
     * @desc 密码重置
     * @author wangxianchen
     * @create 2017-08-24
     * @param resetPwdReq
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_RESET_PWD,method = RequestMethod.POST)
    public ResultObject resetPwd(@RequestBody @Validated ResetPwdReq resetPwdReq, HttpServletRequest request) {
        //验证码失败
        if(!smsCheckCodeService.checkCode(resetPwdReq.getPhoneNo(), BizTypeEnum.FORGET_PWD.name(), resetPwdReq.getCheckCode())){
            throw new ValidationException(UserErrorCodeEnum.CHECK_CODE_ERR,resetPwdReq);
        }
        HeaderReq header = super.getHeaders(request,false);
        //核对密码成功,可以进行重置操作
        ResultObject ro = invokingUserService(header.getAppCode()).resetPwd(resetPwdReq);
        if(ro.isSuccess()){
            smsCheckCodeService.deleteCheckCode();
        }
        return ro;
    }


    /**
     * @desc 根据ID获取用户信息
     * @author wangxianchen
     * @create 2017-08-24
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_DETAIL,method = RequestMethod.POST)
    public ResultObject detail(HttpServletRequest request) {
        HeaderReq header = super.getHeaders(request,false);
        SessionUser sessionUser = super.getSessionUser(request,false);
        return invokingUserService(header.getAppCode()).getDetailByUserId(sessionUser.getUserId());
    }


    /**
     * @desc 根据条件分页查询用户信息
     * @author wangxianchen
     * @create 2017-08-24
     * @param condition
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_LIST,method = RequestMethod.GET)
    public ResultObject list(@Validated UserQueryReq condition, PageBounds pageBounds,HttpServletRequest request) {
        HeaderReq header = super.getHeaders(request,false);
        return invokingUserService(header.getAppCode()).getListByCondition(condition,pageBounds);
    }

    /**
     * @desc 用户状态变更
     * @author wangxianchen
     * @create 2017-08-31
     * @param changeReq
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_CHANGE_STATE,method = RequestMethod.POST)
    public ResultObject changeState(@RequestBody @Validated UserStateChangeReq changeReq,HttpServletRequest request) {
        HeaderReq header = super.getHeaders(request,false);
        return invokingUserService(header.getAppCode()).updateUserState(changeReq);
    }


    /**
     * 权限验证
     * @param userId
     * @param platform
     * @param appCode
     * @param targetUrl
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.AUTH_CHECK,method = RequestMethod.GET)
    public ResultObject  authCheck(@RequestParam(value="userId") String userId,
                                   @RequestParam(value="platform") String platform,
                                   @RequestParam(value="appCode") String appCode,
                                   @RequestParam(value="targetUrl") String targetUrl,
                                   @RequestParam(value="method") String method
                                   ){
        ResultObject ro = new ResultObject();
        boolean flag = invokingUserService(appCode).authCheck(userId,platform,appCode,targetUrl,method);
        ro.setData(flag);
        return ro;
    }

    /**
     * @desc 根据条件分页查询用户信息
     * @author wangxianchen
     * @create 2017-08-24
     * @param condition
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.COUNT_EVERYDAY_REG_USERS, method = RequestMethod.GET)
    public ResultObject countRegUserByDays(@Validated CountRegUserReq condition,HttpServletRequest request) {
        HeaderReq header = super.getHeaders(request,false);
        return invokingUserService(header.getAppCode()).countRegUserByDays(condition);
    }

    /**
     * @desc 根据条件分页查询用户信息
     * @author wangxianchen
     * @create 2017-08-24
     * @param accType
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.COUNT_REG_USERS, method = RequestMethod.GET)
    public ResultObject countRegUser(@RequestParam(value="accType",required = false) String accType,HttpServletRequest request) {
        HeaderReq header = super.getHeaders(request,false);
        return invokingUserService(header.getAppCode()).countRegUser(accType);
    }

    /**
     * @desc 手动注入 IUserService 的实现
     * @author wangxianchen
     * @create 2017-09-25
     * @throws ValidationException
     */
    private AbstractUserService invokingUserService(String appCode){
        if(AppEnum.LAWYER.name().equals(appCode)){
            return lawyerUserService;
        }else if(AppEnum.USER.name().equals(appCode)){
            return generalUserService;
        }else if(AppEnum.PLATFORM.name().equals(appCode)){
            return generalUserService;
        } else{
            throw new ValidationException("不存在的应用");
        }
    }

}
