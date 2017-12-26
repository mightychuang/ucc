package com.hhly.user.service;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.common.dto.ResultObject;
import com.hhly.common.dto.SessionUser;
import com.hhly.user.api.dto.request.*;

/**
 * @author wangxianchen
 * @create 2017-09-25
 * @desc
 */
public interface IUserService {

    /**
     * @desc 登录
     * @author wangxianchen
     * @create 2017-09-25
     * @param loginReq
     * @param headerReq
     * @return
     */
    ResultObject login(LoginReq loginReq, HeaderReq headerReq)throws Exception;

    /**
     * @desc 注册
     * @author wangxianchen
     * @create 2017-09-25
     * @param regReq
     * @param header
     * @return
     */
    ResultObject register(RegReq regReq, HeaderReq header)throws Exception;

    /**
     * @desc 权限验证
     * @author wangxianchen
     * @create 2017-09-25
     * @param userId
     * @param platform
     * @param appCode
     * @param targetUrl
     * @return
     */
    boolean authCheck(String userId,String platform,String appCode,String targetUrl,String method);

    /**
     * @desc 更新帐户信息
     * @author wangxianchen
     * @create 2017-09-25
     * @param req
     * @param userId
     * @return
     * @throws Exception
     */
    ResultObject updateAccount(UpdateUserReq req, String userId) throws Exception;

    /**
     * @desc 更新帐户认证状态
     * @author wangxianchen
     * @create 2017-11-24
     * @param req
     * @param operatorId
     * @return
     */
    ResultObject updateAuthState(UpdateUserAuthReq req,String operatorId);

    /**
     * @desc 修改密码
     * @author wangxianchen
     * @create 2017-09-25
     * @param sessionUser
     * @param modifyPwdReq
     * @return
     */
    ResultObject changePwd(SessionUser sessionUser,ModifyPwdReq modifyPwdReq);

    /**
     * @desc 检查用户是否存在
     * @author wangxianchen
     * @create 2017-09-25
     * @param loginName
     * @return
     */
    ResultObject checkUserExist(String loginName);

    /**
     * @desc 重置密码
     * @author wangxianchen
     * @create 2017-09-25
     * @param resetPwdReq
     * @return
     */
    ResultObject resetPwd(ResetPwdReq resetPwdReq);

    /**
     * @desc 获取用户详细信息
     * @author wangxianchen
     * @create 2017-09-25
     * @param userId
     * @return
     */
    ResultObject getDetailByUserId(String userId);

    /**
     * @desc 用户列表查询
     * @author wangxianchen
     * @create 2017-09-25
     * @param condition
     * @param pageBounds
     * @return
     */
    ResultObject getListByCondition(UserQueryReq condition,PageBounds pageBounds);

    /**
     * @desc 更新用户状态
     * @author wangxianchen
     * @create 2017-09-25
     * @param changeReq
     * @return
     */
    ResultObject updateUserState(UserStateChangeReq changeReq);

    /**
     * @desc 更新用户在线状态
     * @author wangxianchen
     * @create 2017-09-27
     * @param header
     */
    void updateUserOnLineState(HeaderReq header);
}
