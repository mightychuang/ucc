package com.hhly.user.web;


import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.user.api.dto.request.*;
import com.hhly.common.dto.ResultObject;
import com.hhly.user.api.UserURL;
import com.hhly.user.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* @author wangxianchen
* @create 2017-09-27
* @desc 角色控制器
*/
@RestController
public class RoleController  extends BaseController{

    @Autowired
    RoleService roleService;


    /**
     * @desc 角色添加
     * @author wangxianchen
     * @create 2017-09-27
     * @param roleAddReq
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.ROLE_ADD,method = RequestMethod.POST)
    public ResultObject add(@RequestBody @Validated RoleAddReq roleAddReq) {
        ResultObject ro = new ResultObject();
        roleService.addRole(roleAddReq,super.getUserId());
        return ro;
    }


    /**
     * @desc 角色更新
     * @author wangxianchen
     * @create 2017-09-27
     * @param roleUpdateReq
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.ROLE_UPDATE,method = RequestMethod.POST)
    public ResultObject update(@RequestBody @Validated RoleUpdateReq roleUpdateReq) {
        ResultObject ro = new ResultObject();
        roleService.updateRole(roleUpdateReq,super.getUserId());
        return ro;
    }

    /**
     * @desc 角色删除
     * @author wangxianchen
     * @create 2017-09-27
     * @param code
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.ROLE_DELETE,method = RequestMethod.GET)
    public ResultObject delete(@RequestParam(value="code") String code){
        ResultObject ro = new ResultObject();
        roleService.delete(code,super.getUserId());
        return ro;

    }


    /**
     * @desc 角色列表查询
     * @author wangxianchen
     * @create 2017-09-27
     * @param req
     * @param pageBounds
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.ROLE_LIST,method = RequestMethod.GET)
    public ResultObject roleList(@Validated RoleQueryReq req, PageBounds pageBounds) {
        return roleService.roleListByPage(req,pageBounds);
    }

    /**
     * @desc 角色资源绑定
     * @author wangxianchen
     * @create 2017-09-27
     * @param req
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.ROLE_BIND_RES,method = RequestMethod.POST)
    public ResultObject bindRes(@RequestBody @Validated RoleResourceReq req) {
        ResultObject ro = new ResultObject();
        roleService.bindRes(req,super.getUserId());
        return ro;

    }

    /**
     * @desc 角色用户绑定
     * @author wangxianchen
     * @create 2017-09-27
     * @param req
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.ROLE_BIND_USER,method = RequestMethod.POST)
    public ResultObject bindUser(@RequestBody @Validated RoleUserReq req) {
        ResultObject ro = new ResultObject();
        roleService.bindUser(req,super.getUserId());
        return ro;
    }



    /**
     * @desc 获取用户角色列表
     * @author wangxianchen
     * @create 2017-09-21
     * @param req
     * @param pageBounds
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.USER_ROLE_LIST,method = RequestMethod.GET)
    public ResultObject userRoleList(UserRoleQueryReq req, PageBounds pageBounds) {
        return roleService.userRoleList(req,pageBounds);
    }


    /**
     * @desc 查询角色 拥有/未拥有的资源
     * @author wangxianchen
     * @create 2017-09-27
     * @param req
     * @param pageBounds
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.ROLE_RES_LIST,method = RequestMethod.GET)
    public ResultObject roleResList(@Validated RoleResQueryReq req, PageBounds pageBounds){
        return roleService.roleResList(req,pageBounds);
    }

    /**
     * @desc 明细
     * @author wangxianchen
     * @create 2017-09-27
     * @param code
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.ROLE_DETAIL,method = RequestMethod.GET)
    public ResultObject detail(@RequestParam(value="code") String code){
        return roleService.detail(code);
    }

}
