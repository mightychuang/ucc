package com.hhly.user.web;


import com.hhly.user.api.constant.UserConstant;
import com.hhly.user.api.dto.request.*;
import com.hhly.common.dto.ResultObject;
import com.hhly.user.api.UserURL;
import com.hhly.user.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
* @author wangxianchen
* @create 2017-09-27
* @desc 资源菜单
*/
@RestController
public class ResourceController extends BaseController{

    @Autowired
    private ResourceService resourceService;

    /**
     * @desc 添加菜单
     * @author wangxianchen
     * @create 2017-08-24
     * @param req 请求数据
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.RESOURCE_ADD,method = RequestMethod.POST)
    public ResultObject add(@RequestBody @Validated ResourceReq req) {
        resourceService.add(req,super.getUserId());
        return new ResultObject();

    }



    /**
     * @desc 菜单更新
     * @author wangxianchen
     * @create 2017-08-24
     * @param req
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.RESOURCE_UPDATE,method = RequestMethod.POST)
    public ResultObject update(@RequestBody @Validated ResourceUpdateReq req) {
        resourceService.update(req,super.getUserId());
        return new ResultObject();

    }

    /**
     * @desc 删除资源
     * @author wangxianchen
     * @create 2017-09-27
     * @param selfId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.RESOURCE_DELETE,method = RequestMethod.GET)
    public ResultObject delete(@RequestParam(value="selfId") Integer selfId){
        resourceService.delete(selfId,super.getUserId());
        return new ResultObject();
    }

    /**
     * @desc 根据code 查询资源详细信息
     * @author wangxianchen
     * @create 2017-09-22
     * @param selfId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.RESOURCE_DETAIL,method = RequestMethod.GET)
    public ResultObject detail(@RequestParam(value="selfId") Integer selfId){
        return resourceService.detail(selfId);
    }


    /**
     * @desc 移动菜单,改变菜单链接顺序,父菜单更改
     * @author zhangshuqing
     * @create 2017-08-24
     * @param req
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.RESOURCE_MOVE,method = RequestMethod.POST)
    public ResultObject move(@RequestBody @Validated  ResourceMoveReq req) {
        resourceService.move(req,super.getUserId());
        return new ResultObject();
    }

    /**
     * @desc 菜单树
     * @author wangxianchen
     * @create 2017-09-27
     * @param selfId
     * @param appCode
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.RESOURCE_TREE,method = RequestMethod.GET)
    public ResultObject tree(@RequestParam(value="selfId",required = false) Integer selfId,
                             @RequestParam(value="appCode",required = false) String appCode) {
        MenuTreeReq menuTreeReq = new MenuTreeReq();
        menuTreeReq.setMenuId(selfId == null ? UserConstant.TOP_MENU_TREE_ID : selfId);
        menuTreeReq.setAppCode(appCode);
        return resourceService.queryTree(menuTreeReq);
    }

    /**
     * @desc 主页导航树
     * @author wangxianchen
     * @create 2017-09-27
     * @param request
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.RESOURCE_NAVIGATION_TREE)
    public ResultObject navigationTree(HttpServletRequest request){
        HeaderReq header = super.getHeaders(request,true);
        MenuTreeReq menuTreeReq = new MenuTreeReq();
        menuTreeReq.setMenuId(UserConstant.NAVIGATION_MENU_TREE_ID);
        menuTreeReq.setUserId(super.getUserId());
        menuTreeReq.setAppCode(header.getAppCode());
        return resourceService.queryNavigationTree(menuTreeReq);
    }

    /**
     * @desc 刷新白名单
     * @author wangxianchen
     * @create 2017-09-27
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.WHITE_REFRESH)
    public ResultObject refreshWhiteList(){
        resourceService.refreshWhiteList();
        return new ResultObject();
    }
}
