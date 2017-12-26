package com.hhly.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.cache.service.RedisService;
import com.hhly.common.dto.ErrorCodeEnum;
import com.hhly.common.dto.ResultObject;
import com.hhly.common.dto.SessionUser;
import com.hhly.common.enums.DeleteFlagEnum;
import com.hhly.common.exception.ValidationException;
import com.hhly.common.util.A;
import com.hhly.common.util.DateUtil;
import com.hhly.common.util.U;
import com.hhly.user.api.constant.UserConstant;
import com.hhly.user.api.dto.request.*;
import com.hhly.user.api.dto.response.UserDetailResp;
import com.hhly.user.api.dto.response.UserLoginResp;
import com.hhly.user.api.enums.*;
import com.hhly.user.dao.*;
import com.hhly.user.entity.Application;
import com.hhly.user.entity.Resource;
import com.hhly.user.entity.User;
import com.hhly.user.entity.UserRole;
import com.hhly.user.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author wangxianchen
 * @create 2017-09-25
 * @desc
 */
public abstract class AbstractUserService implements IUserService{

    private Logger logger = LoggerFactory.getLogger(AbstractUserService.class);
    
    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private RoleResourceDao roleResourceDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private RedisService redisService;


    /**
     * @desc 登录
     * @author wangxianchen
     * @create 2017-08-24
     * @param loginReq
     * @return
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public ResultObject login(LoginReq loginReq, HeaderReq headerReq){
        logger.info("用户:{},开始登录",loginReq.getLoginName());
        ResultObject ro = new ResultObject();
        User user = this.getUserByLoginName(loginReq.getLoginName());
        if(user == null){
            throw new ValidationException(UserErrorCodeEnum.NO_USER,loginReq); //帐户不存在
        }
        if(!loginReq.getPassword().equals(user.getPassword())){ //密码不对
            logger.error("用户:{},登录密码不对",loginReq.getLoginName());
            throw new ValidationException(UserErrorCodeEnum.PWD_ERR,loginReq);
        }

        //String nowKey = UserConstant.SESSION_USER_PREFIX+user.getUuid()+headerReq.getSecretKey();
        String keyPattern = UserConstant.SESSION_USER_PREFIX+user.getUuid()+"*";
        Set<String> keySet = redisService.getRedisTemplate().keys(keyPattern);
        Iterator<String> it = keySet.iterator();
        while(it.hasNext()){
            String key = it.next();
            //暂时禁掉重复登录的判断
/*            if(nowKey.equals(key)){
                logger.error("用户:{},重复登录",loginReq.getLoginName());
                throw new ValidationException(UserErrorCodeEnum.NON_REPEATABLE_LOGIN,loginReq);
            }*/
            Object obj = redisService.get(key);
            if(obj != null){
                SessionUser his = (SessionUser) obj;
                //上一次登录是PC,这次登录的还是PC,且同属一个APP,就把上次登录的session干掉
                if(his.getLoginDeviceType().equals(ResEnums.APPLY_TO.PC.name()) &&
                        headerReq.getPlatform().equals(ResEnums.APPLY_TO.PC.name()) &&
                        his.getLoginAppCode().equals(headerReq.getAppCode())){
                        redisService.remove(his.getKey());
                }
            }
        }

        Application app = applicationDao.getAvailableAppByCode(headerReq.getAppCode());
        //无此应用 或状态不在启用状态时
        if(app == null){
            throw new ValidationException(ErrorCodeEnum.FAIL,"无此应用:"+JSON.toJSON(app));
        }


        //登录已成功,创建session
        SessionUser tempSessionUser = createSessionUserInfo(user,headerReq);
        logger.info("用户:{},登录后创建session成功",loginReq.getLoginName());

        //生成返回数据
        UserLoginResp userLoginResp = new UserLoginResp();
        userLoginResp.setUserId(user.getUuid());
        userLoginResp.setLoginName(user.getLoginName());
        userLoginResp.setEmail(user.getEmail());
        userLoginResp.setPhone(user.getPhone());
        userLoginResp.setSessionKey(tempSessionUser.getKey());
        ro.setData(userLoginResp.toString());
        return ro;
    }

    /**
     * @desc 创建session信息,并保存至redis
     * @author wangxianchen
     * @create 2017-09-13
     * @param user
     * @param headerReq
     * @return
     */
    private SessionUser createSessionUserInfo(User user,HeaderReq headerReq){
        SessionUser sessionUser = new SessionUser();
        sessionUser.setUserId(user.getUuid());
        sessionUser.setUserName(user.getLoginName());
        sessionUser.setPhoneNo(user.getPhone());
        sessionUser.setLoginAppCode(headerReq.getAppCode());
        sessionUser.setLoginIP(headerReq.getIp());
        sessionUser.setLoginDate(DateUtil.formatDayOfDate_yyyy_MM_dd_HH_mm_ss(new Date()));
        sessionUser.setLoginDeviceType(headerReq.getPlatform());
        sessionUser.setLoginDeviceId(headerReq.getImei());
        sessionUser.setUserAgent(headerReq.getUserAgent());
        sessionUser.setKey(UserConstant.SESSION_USER_PREFIX+user.getUuid()+headerReq.getSecretKey());
        if(!redisService.set(sessionUser.getKey(), sessionUser, UserConstant.LOGIN_SESSION_EXPIR_SECONDS)){
            logger.error("保存用户信息到Redis失败,userId={}",JSON.toJSON(sessionUser));
            throw new ValidationException("保存用户信息到缓存失败");
        }
        return sessionUser;
    }

    /**
     * @desc 验证当前用户是否有访问权限
     * @author wangxianchen
     * @create 2017-09-22
     * @param userId
     * @param platform
     * @param appCode
     * @param targetUrl
     * @return
     */
    @Override
    @Transactional(readOnly=true)
    public boolean authCheck(String userId,String platform,String appCode,String targetUrl,String method){
        boolean exist = resourceDao.selectExistByAppCodeAndUrl(appCode,targetUrl);
        if(!exist){
            logger.info("资源未配置,签权通过. appCode={},targetUrl={}",appCode,targetUrl);
            return true;
        }
        //获取用户所有角色
        List<String> roleCodes = userRoleDao.selectRoleCodesByUserId(userId);
        //获取启用的角色
        List<String> enableRoleCodes = roleDao.filterEnableRoleCodes(roleCodes);
        //获取角色对应的所有资源编码
        List<String> resCodes = roleResourceDao.selectResCodesByRoleCodes(enableRoleCodes);
        //获取对应所有资源
        List<Resource> resList = resourceDao.selectByCodes(resCodes);
        boolean flag = false;
        if(A.isNotEmpty(resList)){
            for(Resource res : resList){
                if(res.getAppCode().equals(appCode)
                        && res.getState().equals(StateEnum.ENABLE.getCode())
                        && res.getUrl().equals(targetUrl)
                        && res.getApplyTo().indexOf(platform) != -1
                        && res.getMethod().indexOf(method) != -1){
                    flag = true;
                    break;
                }
            }
        }
        if(!flag){
            logger.info("用户:{},终端平台:{},应用:{},目标URL:{},请求类型:{} 签权未通过",userId,platform,appCode,targetUrl,method);
        }
        return flag;
    }

    /**
     * @desc 用户注册
     * @author wangxianchen
     * @create 2017-08-24
     * @param regReq
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public ResultObject register(RegReq regReq, HeaderReq header) throws Exception {
        logger.info("用户:{},开始注册",regReq.getRegName());
        ResultObject ro = new ResultObject();
        //检查用户名是否注册过
        User user = new User();
        user.setLoginName(regReq.getRegName());
        user = userDao.selectOneSelective(user);
        if(user != null){
            throw new ValidationException(UserErrorCodeEnum.USER_EXIST,regReq);
        }
        Application app = applicationDao.getAvailableAppByCode(header.getAppCode());
        if(app == null){
            throw new ValidationException(UserErrorCodeEnum.APP_NOT_EXIST,regReq);
        }
        user = new User();
        user.setUuid(U.uuid());
        user.setLoginName(regReq.getRegName());
        user.setRegType(Byte.valueOf(regReq.getRegType()));
        //user.setPassword(Encrypt.toMd5(regReq.getRegPwd())); //加密密码
        user.setPassword(regReq.getRegPwd()); //前端传过来的已是加密密码
        user.setAppCode(app.getCode());
        user.setAccType(UserEnums.ACC_TYPE.getCodeByAppCode(app.getCode()));
        user.setCreateUser(user.getUuid());
        user.setRegDeviceType(header.getPlatform());
        user.setRegDeviceId(header.getImei());


        if(user.getRegType().equals(UserEnums.REG_TYPE.MOBILE_PHONE_NO.getCode())){
            user.setPhone(regReq.getRegName());
        }else{
            user.setEmail(regReq.getRegName());
        }
        user.setState(UserEnums.STATE.ENABLE.getCode());
        if(!userDao.insertSelective(user)){
            logger.error("保存用户数据失败{}",JSON.toJSON(user));
            throw new ValidationException(ErrorCodeEnum.SAVE_DATA_ERROR);
        }


        //初始化角色 默认用户角色 = 普通用户默认权限 + 律师(未认证)权限
        String[] initRoleCodes = new String[]{RoleEnum.LAWYER_USER_UNAUTHORIZED.name(),RoleEnum.GENERAL_USER.name()};
        List<UserRole> userRoleList = new ArrayList<>(initRoleCodes.length);
        for(String roleCode : initRoleCodes){
            //插入成功,初始化用色信息
            UserRole userRole = new UserRole();
            userRole.setUuid(U.uuid());
            userRole.setRoleCode(roleCode);
            userRole.setUserId(user.getUuid());
            userRole.setCreateUser(user.getUuid());
            userRoleList.add(userRole);
        }

        if(userRoleDao.insertBatch(userRoleList) != initRoleCodes.length){
            logger.error("新增用户角色数据失败{}",JSON.toJSON(userRoleList));
            throw new ValidationException(ErrorCodeEnum.SAVE_DATA_ERROR);
        }

        //注册已成功,创建session
        SessionUser tempSessionUser = this.createSessionUserInfo(user,header);
        logger.info("用户:{},注册后创建session成功",regReq.getRegName());
        //生成返回数据
        UserLoginResp userLoginResp = new UserLoginResp();
        userLoginResp.setUserId(user.getUuid());
        userLoginResp.setLoginName(user.getLoginName());
        userLoginResp.setEmail(user.getEmail());
        userLoginResp.setPhone(user.getPhone());
        userLoginResp.setSessionKey(tempSessionUser.getKey());
        ro.setData(userLoginResp.toString());
        return ro;
    }

    /**
     * @desc 修改密码
     * @author wangxianchen
     * @create 2017-08-24
     * @param sessionUser
     * @param modifyPwdReq
     * @return
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public ResultObject changePwd(SessionUser sessionUser,ModifyPwdReq modifyPwdReq){
        ResultObject ro = new ResultObject();
        User user = this.getUserByLoginName(sessionUser.getUserName());
        //核对旧密码
        if(!user.getPassword().equals(modifyPwdReq.getOldPwd())){
            throw new ValidationException(UserErrorCodeEnum.PWD_ERR);
        }
        User temp = new User();
        temp.setId(user.getId());
        temp.setUpdateUser(user.getUuid());
        temp.setPassword(modifyPwdReq.getNewPwd());
        if(!userDao.updateByPrimaryKeySelective(temp)){
            logger.error("用户:{},在修改密码时,数据更新失败{}",user.getLoginName(),JSON.toJSON(modifyPwdReq));
            throw new ValidationException(ErrorCodeEnum.UPDATE_DATA_ERROR);
        }
        return ro;
    }

    /**
     * @desc 重置密码
     * @author wangxianchen
     * @create 2017-08-24
     * @param resetPwdReq
     * @return
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public ResultObject resetPwd(ResetPwdReq resetPwdReq){
        ResultObject ro = new ResultObject();
        User temp = new User();
        User user = this.getUserByLoginName(resetPwdReq.getLoginName());
        temp.setId(user.getId());
        temp.setUpdateUser(user.getUuid());
        temp.setPassword(resetPwdReq.getNewPwd());
        if(!userDao.updateByPrimaryKeySelective(temp)){
            logger.error("用户:{},在重置密码时,数据更新失败{}",user.getLoginName(),JSON.toJSONString(temp));
            throw new ValidationException(ErrorCodeEnum.UPDATE_DATA_ERROR);
        }
        return ro;
    }

    /**
     * @desc 根据登录名称获取用户信息
     * @author wangxianchen
     * @create 2017-08-24
     * @param loginName
     * @return
     * @throws ValidationException
     */
    private User getUserByLoginName(String loginName) throws ValidationException{
        User user = new User();
        user.setLoginName(loginName);
        user.setState(UserEnums.STATE.ENABLE.getCode());
        user.setIsDelete(DeleteFlagEnum.NO.getCode());
        user = userDao.selectOneSelective(user);
        if(user == null){
            throw new ValidationException(UserErrorCodeEnum.NO_USER);
        }
        return user;
    }

    /**
     * @desc 验证用户是否存在
     * @author wangxianchen
     * @create 2017-08-24
     * @param loginName
     * @return
     */
    @Override
    @Transactional(readOnly=true)
    public ResultObject checkUserExist(String loginName){
        ResultObject ro = new ResultObject();
        User user = this.getUserByLoginName(loginName);
        Map<String,String> retMap = new HashMap<>();
        retMap.put("phoneNo",user.getPhone());
        retMap.put("userId",user.getUuid());
        //如果存在此用户,则返回手机号码
        ro.setData(retMap);
        return ro;
    }

    /**
     * @desc 更新帐户信息
     * @author wangxianchen
     * @create 2017-08-24
     * @param req
     * @param operatorId
     * @return
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public ResultObject updateAccount(UpdateUserReq req,String operatorId) throws Exception{
        if(req.getPhone() == null && req.getEmail() == null && req.getRealName() == null){
            throw new ValidationException("更新的内容不能为空");
        }
        ResultObject ro = new ResultObject();
        User user = userDao.selectByUserId(operatorId);
        if(user == null){
            logger.error("不存在当前登录用户.userId={}",operatorId);
            throw new ValidationException("不存在当前登录用户");
        }
        User temp = new User();
        temp.setUuid(operatorId);
        temp.setPhone(req.getPhone());
        temp.setEmail(req.getEmail());
        temp.setRealName(req.getRealName());
        temp.setUpdateUser(operatorId);
        if(!userDao.updateByUserIdSelective(temp)){
            logger.error("更新帐户信息失败",JSON.toJSON(temp));
            throw new ValidationException(ErrorCodeEnum.UPDATE_DATA_ERROR);
        }
        return ro;
    }

    /**
     * @desc 更新帐户认证状态
     * @author wangxianchen
     * @create 2017-11-24
     * @param req
     * @param operatorId
     * @return
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public ResultObject updateAuthState(UpdateUserAuthReq req,String operatorId){
        ResultObject ro = new ResultObject();
        User user = userDao.selectByUserId(req.getUserId());
        if(user == null){
            logger.error("无此用户.userId={}",req.getUserId());
            throw new ValidationException("无此用户");
        }
        User temp = new User();
        temp.setUuid(req.getUserId());
        temp.setUpdateUser(operatorId);
        //如果是律师
        if(user.getAccType().equals(UserEnums.ACC_TYPE.LAWYER.getCode())){
            if(user.getIsAuth().equals(UserEnums.IS_AUTH.UNAUTHORIZED)){ //未认证 时才能认证
                temp.setIsAuth(req.getIsAuth());
            }else if(user.getIsAuth().equals(UserEnums.IS_AUTH.AUTHORIZED_LAWYER.getCode()) &&
                    req.getIsAuth().equals(UserEnums.IS_AUTH.AUTHORIZED_AGENT.getCode())){
                throw new ValidationException("已认证为律师,不可再认证为知识产权代理人");
            }else if(user.getIsAuth().equals(UserEnums.IS_AUTH.AUTHORIZED_AGENT.getCode()) &&
                    req.getIsAuth().equals(UserEnums.IS_AUTH.AUTHORIZED_LAWYER.getCode())){
                throw new ValidationException("已认证为知识产权代理人,不可再认证为律师");
            }else{
                throw new ValidationException("用户状态已认证,请不要重复提交");
            }
        }else if(user.getAccType().equals(UserEnums.ACC_TYPE.USER.getCode())){ //普通用户
            temp.setIsAuth(req.getIsAuth());
            temp.setAccType(UserEnums.ACC_TYPE.LAWYER.getCode()); //成为律师
        }else{
            throw new ValidationException("系统用户不可认证");
        }

        if(!userDao.updateByUserIdSelective(temp)){
            logger.error("更新认证状态失败,userId={}",req.getUserId());
            throw new ValidationException(ErrorCodeEnum.UPDATE_DATA_ERROR);
        }
        return ro;
    }


    /**
     * @desc 获取帐户基本信息
     * @author wangxianchen
     * @create 2017-08-24
     * @param userId
     * @return
     */
    @Override
    @Transactional(readOnly=true)
    public ResultObject getDetailByUserId(String userId){
        ResultObject ro = new ResultObject();
        User user = new User();
        user.setUuid(userId);
        user.setIsDelete(DeleteFlagEnum.NO.getCode());
        user = userDao.selectOneSelective(user);
        if(user != null){
            UserDetailResp detail = new UserDetailResp();
            detail.setUserId(user.getUuid());
            detail.setLoginName(user.getLoginName());
            detail.setRealName(user.getRealName());
            detail.setPhone(user.getPhone());
            detail.setEmail(user.getEmail());
            detail.setState(user.getState());
            detail.setStateDesc(user.getStateDesc());
            detail.setAccType(user.getAccType());
            detail.setRegType(user.getRegType());
            detail.setIsAuth(user.getIsAuth());
            detail.setAppCode(user.getAppCode());
            detail.setRegDeviceType(user.getRegDeviceType());
            detail.setRegDeviceId(user.getRegDeviceId());
            ro.setData(detail);
        }
        return ro;
    }

    /**
     * @desc 按条件分页查询用户信息
     * @author wangxianchen
     * @create 2017-08-30
     * @param condition
     * @return
     */
    @Override
    @Transactional(readOnly=true)
    public ResultObject getListByCondition(UserQueryReq condition,PageBounds pageBounds){
        ResultObject ro = new ResultObject();
        ro.setData(userDao.selectUserWithPage(condition,pageBounds));
        return ro;
    }

    /**
     * @desc 更新用户状态
     * @author wangxianchen
     * @create 2017-09-25
     * @param changeReq
     * @return
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public ResultObject updateUserState(UserStateChangeReq changeReq){
        ResultObject ro = new ResultObject();
        User user = new User();
        user.setUuid(changeReq.getUserId());
        user.setState(Byte.valueOf(changeReq.getState()));
        user.setStateDesc(changeReq.getStateDesc());
        if(!userDao.updateByUserIdSelective(user)){
            logger.error("更新用户状态失败{}",JSON.toJSON(changeReq));
            throw new ValidationException(ErrorCodeEnum.UPDATE_DATA_ERROR);
        }
        return ro;
    }

    /**
     * @desc 更新用户在线状态
     * @author wangxianchen
     * @create 2017-09-27
     * @param header
     */
    @Override
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
    public void updateUserOnLineState(HeaderReq header){
        if(U.isBlank(header.getUserId())){
            throw new ValidationException(UserErrorCodeEnum.NO_USER);
        }
        //先删除当前session信息
        String nowKey = UserConstant.SESSION_USER_PREFIX+header.getUserId()+header.getSecretKey();
        redisService.remove(nowKey);
        //多个应用在线,只有全部下线,在线状态才为下线
        String keyPattern = UserConstant.SESSION_USER_PREFIX+header.getUserId()+"*";
        Set<String> keySet = redisService.getRedisTemplate().keys(keyPattern); //查找该用户还存在的session
/*        if(A.isEmpty(keySet)){ //如果为空,则说明当前用户已全部下线,此时更新其在线状态
            UserExt ext = new UserExt();
            ext.setUuid(header.getUserId());
            ext.setOnline(UserEnums.ONLINE.NO.getCode());
            ext.setUpdateUser(header.getUserId());
            userExtDao.updateByUserIdSelective(ext);
            logger.info("用户:{},的在线状态被更新为已下线",header.getUserId());
        }*/
    }

    /**
     * @desc 按天统计每天注册用户数
     * @author wangxianchen
     * @create 2017-11-09
     * @param condition
     * @return
     */
    @Transactional(readOnly = true)
    public ResultObject countRegUserByDays(CountRegUserReq condition){
        ResultObject ro = new ResultObject();
        ro.setData(userDao.countRegUserByDays(condition));
        return ro;
    }

    /**
     * @desc 按类型统计用户数
     * @author wangxianchen
     * @create 2017-11-09
     * @param accType
     * @return
     */
    @Transactional(readOnly = true)
    public ResultObject countRegUser(String accType){
        ResultObject ro = new ResultObject();
        ro.setData(userDao.countRegUser(accType));
        return ro;
    }




}
