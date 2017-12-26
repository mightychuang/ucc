package com.hhly.user.service;

import com.alibaba.fastjson.JSON;
import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;
import com.hhly.common.exception.ValidationException;
import com.hhly.common.util.LogUtil;
import com.hhly.user.api.dto.request.*;
import com.hhly.user.api.dto.response.RoleBindResQueryResp;
import com.hhly.user.api.dto.response.UserRoleQueryResp;
import com.hhly.user.api.enums.RoleEnum;
import com.hhly.user.api.enums.UserEnums;
import com.hhly.user.dao.*;
import com.hhly.user.entity.Role;
import com.hhly.user.entity.RoleResource;
import com.hhly.user.entity.User;
import com.hhly.user.entity.UserRole;
import com.hhly.common.dto.ErrorCodeEnum;
import com.hhly.common.dto.ResultObject;
import com.hhly.common.enums.DeleteFlagEnum;
import com.hhly.common.util.U;
import com.hhly.user.api.dto.response.RoleShortInfoResp;
import com.hhly.user.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* @author wangxianchen
* @create 2017-11-27
* @desc 角色处理
*/
@Service
public class RoleService {

    private Logger logger = LoggerFactory.getLogger(RoleService.class);

    @Autowired
    private ApplicationDao appDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private RoleResourceDao roleResourceDao;

    @Autowired
    private ResourceDao resourceDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRoleDao userRoleDao;


    /**
     * @desc 角色添加
     * @author wangxianchen
     * @create 2017-09-27
     * @param roleAddReq
     * @param operatorId
     */
    @Transactional(propagation= Propagation.REQUIRED,rollbackFor=Exception.class)
    public void addRole(RoleAddReq roleAddReq,String operatorId){
        Application app = appDao.getAvailableAppByCode(roleAddReq.getAppCode());
        if(app == null){
            throw new ValidationException("应用编码不存在");
        }
        //真正的角色编码 = appCode +"_"+ 输入的编码
        String roleCode = roleAddReq.getAppCode()+"_"+ roleAddReq.getCode();
        Role role = roleDao.selectByCode(roleCode);
        if(role != null){
            if(role.getIsDelete().equals(DeleteFlagEnum.YES.getCode())){ //如果有删除标记
                BeanUtils.copyProperties(roleAddReq,role);
                role.setCode(roleCode);
                role.setState(Byte.valueOf(roleAddReq.getState()));
                role.setIsDelete(DeleteFlagEnum.NO.getCode());
                role.setUpdateUser(operatorId);
                roleDao.updateByPrimaryKeySelective(role);
            }else{
                throw new ValidationException("角色编码已存在,请重新输入");
            }
        }else{
            role = new Role();
            BeanUtils.copyProperties(roleAddReq,role);
            role.setCode(roleCode);
            role.setState(Byte.valueOf(roleAddReq.getState()));
            role.setCreateUser(operatorId);
            roleDao.insertSelective(role);
        }
        logger.info("操作人:{},新增了一个角色:{}",operatorId,JSON.toJSONString(role));

    }

    /**
     * 更新角色
     * @author zhangshuqing
     * @create 2017-08-24
     * @param req  角色更新请求参数
     * @return
     */
    @Transactional(propagation= Propagation.REQUIRED,rollbackFor=Exception.class)
    public void updateRole(RoleUpdateReq req,String operatorId){
        //查找出Role
        Role role = roleDao.selectByCode(req.getCode());
        if(role == null || DeleteFlagEnum.YES.getCode().equals(role.getIsDelete())){
            throw new ValidationException("角色不存在");
        }
        BeanUtils.copyProperties(req,role);
        if(!StringUtils.isEmpty(req.getState())){
            role.setState(Byte.valueOf(req.getState()));
        }
        role.setUpdateUser(operatorId);
        roleDao.updateByCode(role);
        logger.info("操作人:{},更新一个角色:{}",operatorId,JSON.toJSONString(role));
    }


    /**
     * 角色资源绑定
     * @author zhangshuqing
     * @create 2017-08-24
     * @param req 资源绑定请求参数
     * @param operatorId 用户id
     * @return
     */
    @Transactional(propagation= Propagation.REQUIRED,rollbackFor=Exception.class)
    public void bindRes(RoleResourceReq req, String operatorId){
        Role role = roleDao.getAvailableRole(req.getRoleCode());
        if(role == null){
            throw new ValidationException("角色不存在或已删除");
        }
        //2.判断resource对象是否存在，不存在则提示错误
        List<String> resCodeList = req.getResCodeArray();
        int existCount = resourceDao.selectExistCodeCount(resCodeList,role.getAppCode());
        if(existCount != resCodeList.size()){
            throw new ValidationException("资源不存在或已删除,请刷新列表重试");
        }
        //都存在
        //操作类型(1:绑定 2:取消绑定)
        if(req.getOpType() == 1){
            List<RoleResource> batchList = new ArrayList<>();
            for(String resCode : resCodeList){
                RoleResource rr = new RoleResource();
                rr.setUuid(U.uuid());
                rr.setRoleCode(req.getRoleCode());
                rr.setResCode(resCode);
                rr.setCreateUser(operatorId);
                batchList.add(rr);
            }
            roleResourceDao.insertBatch(batchList);
            logger.info("操作人:{},为角色绑定了资源:{}",operatorId, JSON.toJSON(batchList));
        }else if(req.getOpType() == 2){
            roleResourceDao.deleteByRoleResourceCode(req.getRoleCode(),resCodeList);
            logger.info("操作人:{},为角色解绑了资源:{}",operatorId, JSON.toJSON(resCodeList));
        }
    }


    /**
     * 为角色添加或删除关联用户
     * @author zhangshuqing
     * @create 2017-08-24
     * @param req  请求参数
     * @param operatorId 操作id
     * @return
     */
    @Transactional(propagation= Propagation.REQUIRED,rollbackFor=Exception.class)
    public void bindUser(RoleUserReq req, String operatorId) {
        //获取user
        User user = userDao.selectByUserId(req.getUserId());
        //不存在或已删除
        if(user == null || DeleteFlagEnum.YES.getCode().equals(user.getIsDelete())){
            throw new ValidationException("用户不存在");
        }
        //获取 role
        List<String> roleCodeArray = req.getRoleCodeArray();
        int count = roleDao.selectExistCodeCount(roleCodeArray);
        if(count != roleCodeArray.size()){
            throw new ValidationException("角色不存在或已删除,请刷新列表重试");
        }
        //都存在
        //操作类型(1:绑定 2:取消绑定)
        if(req.getOpType() == 1){
            List<UserRole> batchList = new ArrayList<>();
            for(String roleCode : roleCodeArray){
                UserRole ur = new UserRole();
                ur.setUuid(U.uuid());
                ur.setUserId(req.getUserId());
                ur.setRoleCode(roleCode);
                ur.setCreateUser(operatorId);
                batchList.add(ur);
            }
            try {
                userRoleDao.insertBatch(batchList);
            } catch (DuplicateKeyException e) {
                throw new ValidationException("重复绑定");
            }
            logger.info("操作人:{},为用户绑定了角色:{}",operatorId, JSON.toJSON(batchList));
        }else if(req.getOpType() == 2){
            userRoleDao.deleteByUserRoleCodes(req.getUserId(),roleCodeArray);
            logger.info("操作人:{},为用户解绑了角色:{}",operatorId, JSON.toJSON(roleCodeArray));
        }
    }

    /**
     * @desc 分页查询权限列表
     * @author zhangshuqing
     * @create 2017-08-28
     * @param roleQueryReq
     * @return
     */
    @Transactional(readOnly=true)
    public ResultObject roleListByPage(RoleQueryReq roleQueryReq, PageBounds pageBounds) {
        ResultObject ro = new ResultObject();
        List<RoleShortInfoResp> list =  roleDao.fuzzySelect(roleQueryReq,pageBounds);
        ro.setData(list);
        return ro;
}


    /**
     * @desc 删除角色
     * @author wangxianchen
     * @create 2017-09-27
     * @param code
     * @param operatorId
     * @return
     */
    @Transactional(propagation= Propagation.REQUIRED,rollbackFor=Exception.class)
    public void delete(String code, String operatorId) {
        Role role = roleDao.getAvailableRole(code);
        if(role != null){
            if (RoleEnum.contains(role.getCode())){
                throw new ValidationException("系统默认角色不可以删除");
            }
            roleDao.deleteByCode(role.getCode());
            roleResourceDao.deleteByRoleCode(code);
            userRoleDao.deleteByRoleCode(code);
            logger.info("操作人:{},删除了一个角色:{}",operatorId,code);
        }
    }

    /**
     * @desc 用户角色列表
     * @author wangxianchen
     * @create 2017-09-21
     * @param req
     * @param pageBounds
     * @return
     */
    @Transactional(readOnly=true)
    public ResultObject userRoleList(UserRoleQueryReq req,PageBounds pageBounds) {
        ResultObject ro = new ResultObject();
        if(U.isBlank(req.getUserId())){
            throw new ValidationException("参数用户ID不能为空");
        }
        User user = userDao.selectByUserId(req.getUserId());
        if(user == null || DeleteFlagEnum.YES.getCode().equals(user.getIsDelete())){
            throw new ValidationException("用户不存在或已删除,请刷新列表重试");
        }
        //获取用户所属APP
        String userBlongAppCode = UserEnums.ACC_TYPE.getAppCodeByCode(user.getAccType());
        List<UserRoleQueryResp> list = null;
        String[] appCodes = new String[]{userBlongAppCode};
        //是否选择 1:已选择  2:未选择
        if(req.getType().equals("1")){
            list = userRoleDao.selectBindByUserId(req,appCodes,pageBounds);
        }else if(req.getType().equals("2")){
            list = userRoleDao.selectNotBindByUserId(req,appCodes,pageBounds);
        }
        ro.setData(list);
        return ro;
    }


    /**
     * @desc 查询角色 拥有/未拥有的资源
     * @author zhangshuqing
     * @create 2017-09-22
     * @param req
     * @param pageBounds
     * @return
     */
    @Transactional(readOnly=true)
    public ResultObject roleResList(RoleResQueryReq req, PageBounds pageBounds){
        ResultObject ro = new ResultObject();
        Role role = roleDao.getAvailableRole(req.getRoleCode());
        if(role == null){
            throw new ValidationException("角色不存在或已删除");
        }
        List<RoleBindResQueryResp> list = null;
        req.setAppCode(role.getAppCode());
        //是否选择 1:已选择  2:未选择
        if(req.getType().equals("1")){
            list = roleResourceDao.selectBindResource(req,pageBounds);
        }else if(req.getType().equals("2")){
            list = roleResourceDao.selectNotBindResource(req,pageBounds);
        }
        ro.setData(list);
        return ro;
    }

    /**
     * @desc 获取详细信息
     * @author wangxianchen
     * @create 2017-09-26
     * @param code
     * @return
     */
    @Transactional(readOnly=true)
    public ResultObject detail(String code) {
        ResultObject resultObject = new ResultObject();
        Role role = roleDao.selectByCode(code);
        if(role == null){
            throw new ValidationException("角色不存在");
        }
        role.setId(null);
        resultObject.setData(role);
        return resultObject;
    }

}
