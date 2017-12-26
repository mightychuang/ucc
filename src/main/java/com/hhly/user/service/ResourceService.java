package com.hhly.user.service;

import com.alibaba.fastjson.JSON;
import com.hhly.cache.service.RedisService;
import com.hhly.common.exception.ValidationException;
import com.hhly.common.util.A;
import com.hhly.user.api.constant.UserConstant;
import com.hhly.user.api.dto.request.*;
import com.hhly.user.dao.*;
import com.hhly.user.entity.Application;
import com.hhly.user.entity.Resource;
import com.hhly.common.constant.Constant;
import com.hhly.common.dto.ResultObject;
import com.hhly.common.util.LogUtil;
import com.hhly.user.api.enums.ResEnums;
import com.hhly.user.api.enums.StateEnum;
import com.hhly.user.api.dto.response.MenuTree;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
* @author wangxianchen
* @create 2017-09-27
* @desc 资源操作
*/
@Service
public class ResourceService {

    private Logger logger = LoggerFactory.getLogger(ResourceService.class);
    
    @Autowired
    ResourceDao resourceDao;

    @Autowired
    ApplicationDao applicationDao;

    @Autowired
    RoleResourceDao roleResourceDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    RedisService redisService;

    /**
     * @desc 新增资源
     * @author wangxianchen
     * @create 2017-09-27
     * @param req
     * @param operatorId
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void add(ResourceReq req, String operatorId) {
        //插入前先查找数据库中是否已经存在
        String resCode = req.getAppCode()+"_"+req.getCode();
        Resource resource = resourceDao.selectByCode(resCode);
        if (resource != null) {
           throw new ValidationException("资源已存在");
        }
        Application app = applicationDao.selectByCode(req.getAppCode());
        if(app == null){
            throw new ValidationException("应用不存在或不可用,请刷新重试");
        }
        Resource parent = null;
        //若插入的不是一级菜单，先判断父ID是否存在
        if (!isTopMenu(req.getParentId())) {
            parent = resourceDao.selectBySelfId(req.getParentId());
            if(parent == null){
                throw new ValidationException("父资源不存在或不可用,请刷新重试");
            }
            //父资源与当前资源不属于同一个应用
            if(!parent.getAppCode().equals(req.getAppCode())){
                throw new ValidationException("当前资源与父资源不同属一个应用");
            }
        }
        int selfId = resourceDao.generateSelfId();
        Resource temp = new Resource();
        BeanUtils.copyProperties(req, temp);
        temp.setSelfId(selfId);
        temp.setCreateUser(operatorId);
        temp.setCode(resCode);
        temp.setAppCode(req.getAppCode());
        temp.setPath(parent == null ? "0" : parent.getPath()+"|"+selfId);
        temp.setDepth(parent == null ? 1 : (byte) (1+parent.getDepth()));
        resourceDao.insertSelective(temp);
        //是否加入白名单
        if(ResEnums.JOIN_WHITE_LIST.YES.getCode().equals(temp.getJoinWhiteList())){
            insertRedis(temp.getUrl());
        }
        logger.info("操作人:{},新增了一个资源:{}",operatorId,JSON.toJSONString(temp));
    }

    /**
     * @desc 更新
     * @author wangxianchen
     * @create 2017-09-27
     * @param req
     * @param operatorId
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void update(ResourceUpdateReq req, String operatorId) {
        Resource oldRes = resourceDao.selectByCode(req.getCode());
        if (oldRes == null) {
            throw new ValidationException("资源不存在,请刷新重试");
        }
        Resource newResource = new Resource();
        BeanUtils.copyProperties(req, newResource);
        newResource.setUpdateUser(operatorId);
        //是否加入白名单
        if(ResEnums.JOIN_WHITE_LIST.YES.getCode().equals(newResource.getJoinWhiteList())){
            insertRedis(newResource.getUrl());
        }else{
            removeRedis(newResource.getUrl());
        }
        resourceDao.updateByCode(newResource);
        logger.info("操作人:{},更新了一个资源:{}",operatorId,JSON.toJSONString(newResource));
    }

    /**
     * @desc 移动资源,改变资源之间的关系
     * @author wangxianchen
     * @create 2017-09-26
     * @param req
     * @param operatorId
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void move(ResourceMoveReq req,String operatorId){
        Resource parent = resourceDao.selectBySelfId(req.getParentId());
        if(parent == null){
            throw new ValidationException("父资源不存在");
        }
        Resource self = resourceDao.selectBySelfId(req.getSelfId());
        if(self == null){
            throw new ValidationException("资源不存在");
        }
        if(!self.getAppCode().equals(parent.getAppCode())){
            throw new ValidationException("当前资源与父资源不同属一个应用");
        }
        MenuTree tree = resourceDao.findTreeBySelfId(req.getSelfId());
        if(tree == null){
            throw new ValidationException("资源不存在");
        }
        List<Resource> list = new ArrayList<>();
        resetResPathAndDepth(tree,parent.getSelfId(),parent.getPath(),parent.getDepth(),operatorId,list);
        if(A.isNotEmpty(list)){
            /// 开始批量更新
            resourceDao.updateByList(list);
        }
        logger.info("操作人:{},改变了资源的关联关系:{}",operatorId,JSON.toJSONString(req));
    }

    /**
     * @desc 重设资源的路径及深度
     * @author wangxianchen
     * @create 2017-09-26
     * @param tree
     * @param parentId
     * @param parentPath
     * @param parentDepth
     * @param operatorId
     * @param list
     */
    private void resetResPathAndDepth(MenuTree tree,int parentId,String parentPath, byte parentDepth,String operatorId,List<Resource> list){
        Resource temp = new Resource();
        temp.setSelfId(tree.getSelfId());
        temp.setParentId(parentId);
        temp.setPath(parentPath+"|"+tree.getSelfId());
        temp.setDepth((byte) (parentDepth+1));
        temp.setUpdateUser(operatorId);
        list.add(temp);
        List<MenuTree> children = tree.getChildren();
        if(A.isNotEmpty(children)){
            for(MenuTree menuTree : children){
                resetResPathAndDepth(menuTree,temp.getSelfId(),temp.getPath(),temp.getDepth(),operatorId,list);
            }
        }
    }


    /**
     * @desc 导航菜单查询
     * @author wangxianchen
     * @create 2017-09-27
     * @param menuTreeReq
     * @return
     */
    @Transactional(readOnly=true)
    public ResultObject queryNavigationTree(MenuTreeReq menuTreeReq) {
        ResultObject resultObject = new ResultObject();
        String[] appCodes = new String[]{menuTreeReq.getAppCode()};
        //获取拥有对应应用的已启用角色
        List<String> belongAppRoleList = roleDao.selectByRoleCodesAndAppCodes(appCodes);
        //获取该用户包含的角色
        List<String> roleCodeList = userRoleDao. selectExistRoleCodes(menuTreeReq.getUserId(),belongAppRoleList);
        //获取当前用户角色拥有的资源
        List<String> resCodeList = roleResourceDao.selectResCodesByRoleCodes(roleCodeList);
        //获取一级菜单
        List<MenuTree> menuList = resourceDao.queryNavigationTree(resCodeList,appCodes, StateEnum.ENABLE.getCode(),UserConstant.NAVIGATION_MENU_TREE_ID,UserConstant.NAVIGATION_MENU_DEPTH);
        if(menuList != null && menuList.size() > 0){
            for(MenuTree menu : menuList){
                List<MenuTree> subList = resourceDao.queryNavigationTree(resCodeList,appCodes, StateEnum.ENABLE.getCode(),menu.getSelfId(),UserConstant.NAVIGATION_MENU_DEPTH);
                menu.setChildren(subList);
            }
        }
        resultObject.setData(menuList);
        return resultObject;
    }

    /**
     * @desc 菜单树
     * @author wangxianchen
     * @create 2017-09-27
     * @param menuTreeReq
     * @return
     */
    @Transactional(readOnly=true)
    public ResultObject queryTree(MenuTreeReq menuTreeReq) {
        ResultObject resultObject = new ResultObject();
        List<MenuTree> menuTreeList = resourceDao.getTree(menuTreeReq.getAppCode(),menuTreeReq.getMenuId());
        menuTreeList = (menuTreeList == null ? Collections.EMPTY_LIST : menuTreeList);
        resultObject.setData(menuTreeList);
        return resultObject;
    }

    /**
     * @desc 是否是 一级菜单
     * @author wangxianchen
     * @create 2017-09-27
     * @param menuId
     * @return
     */
    private boolean isTopMenu(int menuId) {
        return UserConstant.TOP_MENU_TREE_ID == menuId;
    }

    /**
     * @desc 删除
     * @author wangxianchen
     * @create 2017-09-27
     * @param selfId
     * @param operatorId
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void delete(Integer selfId,String operatorId) {
        if(selfId == null){
            throw new ValidationException("资源ID不能为空");
        }
        //查找出需要删除的菜单树,删除
        MenuTree menuTree = resourceDao.findTreeBySelfId(selfId);
        if (menuTree != null) {
            List<String> codeList = new ArrayList<String>();
            this.getCodeByMenuTree(menuTree, codeList);
            resourceDao.deleteByCodes(codeList);
            //删除与角色绑定的关系
            roleResourceDao.deleteByResourceCode(codeList);
            logger.info("操作人{},删除了以下资源:{}",operatorId, JSON.toJSONString(menuTree));
        }
    }

    /**
     * @desc 递归获取 菜单树里面的code
     * @author wangxianchen
     * @create 2017-09-27
     * @param menuTree
     * @param codeList
     */
    private void getCodeByMenuTree(MenuTree menuTree, List<String> codeList) {
        codeList.add(menuTree.getCode());
        List<MenuTree> child = menuTree.getChildren();
        if (child != null && child.size() != 0) {
            for (MenuTree tree : child) {
                getCodeByMenuTree(tree, codeList);
            }
        }
    }

    /**
     * @desc 刷新白名单
     * @author wangxianchen
     * @create 2017-09-27
     */
    @Transactional(readOnly=true)
    public void refreshWhiteList(){
        List<Resource> listResource = resourceDao.selectWhiteList();
        Set<String> whiteList = new HashSet<String>();
        for (Resource item : listResource) {
            if(!StringUtils.isEmpty(item.getUrl())){
                whiteList.add(item.getUrl());
            }
        }
        setRedis(whiteList);
    }

    private Set<String> getRedis() {
        Set<String> whiteSet = (Set<String>) redisService.get(Constant.CACHE_KEY_PREFIX_WHITELIST);
        if (whiteSet == null) {
            whiteSet = new HashSet<String>();
        }
        return whiteSet;
    }

    private void setRedis(Set<String> whiteSet) {
        redisService.set(Constant.CACHE_KEY_PREFIX_WHITELIST, whiteSet);
    }

    private void insertRedis(String url) {
        Set<String> blackSet = getRedis();
        blackSet.add(url);
        setRedis(blackSet);
    }

    private void removeRedis(String url) {
        Set<String> blackSet = getRedis();
        blackSet.remove(url);
        setRedis(blackSet);
    }

    private void updateRedis(String oldUrl, String newUrl) {
        Set<String> blackSet = getRedis();
        blackSet.add(newUrl);
        blackSet.remove(oldUrl);
        setRedis(blackSet);
    }

    /**
     * @desc 明细
     * @author wangxianchen
     * @create 2017-09-26
     * @param selfId
     * @return
     */
    @Transactional(readOnly=true)
    public ResultObject detail(Integer selfId) {
        if(selfId == null){
            throw new ValidationException("资源ID不能为空");
        }
        ResultObject ro = new ResultObject();
        Resource resource = resourceDao.selectBySelfId(selfId);
        ro.setData(resource);
        return ro;
    }
}
