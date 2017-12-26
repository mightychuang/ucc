package com.hhly.user.web;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.common.dto.ErrorCodeEnum;
import com.hhly.common.dto.ResultObject;
import com.hhly.common.exception.ValidationException;
import com.hhly.common.util.LogUtil;
import com.hhly.user.api.UserURL;
import com.hhly.user.api.dto.request.BlackReq;
import com.hhly.user.api.dto.request.BlackQueryReq;
import com.hhly.user.service.BlackListService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
* @author pengchao
* @create 2017-09-06
* @desc 黑名单基本控制入口
*/
@RestController
public class BlackListController extends BaseController{

    private Logger logger = LoggerFactory.getLogger(BlackListController.class);
    
    @Autowired
    private BlackListService blackService;

    /**
     * 根据ID获取用户信息
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.BLACK_GET,method = RequestMethod.GET)
    public ResultObject get(Integer id) {
        if(id<=0 ){
            return new ResultObject(ErrorCodeEnum.PARAM_EXCEPTION.format("id错误"));
        }

        ResultObject result = new ResultObject();
        try {
            result = blackService.get(id);
        } catch (Exception e) {
            logger.error(ErrorCodeEnum.QUERY_DATA_ERROR.message(),e);
            result.fail(ErrorCodeEnum.QUERY_DATA_ERROR);
        }
        return result;
    }

    /**
     * @desc 新增
     * @param blackReq
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.BLACK_ADD, method = RequestMethod.POST)
    public ResultObject add(@RequestBody @Validated BlackReq blackReq) {
        ResultObject result = new ResultObject();
        try {
            result = blackService.add(blackReq);
        }catch(ValidationException e){
            logger.error(e.getErrorCodeEnum().message()+e.getParam(),e);
            result.fail(e.getErrorCodeEnum());
        }catch (Exception e) {
            logger.error(ErrorCodeEnum.SAVE_DATA_ERROR.message(),e);
            result.fail(ErrorCodeEnum.SAVE_DATA_ERROR);
        }
        return result;
    }

   /**
     * @desc 更新
     * @param blackReq
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.BLACK_UPDATE, method = RequestMethod.PUT)
    public ResultObject update(@RequestBody @Validated BlackReq blackReq) {
        if(blackReq.getId()<=0 ){
            return new ResultObject(ErrorCodeEnum.PARAM_EXCEPTION.format("id错误"));
        }

        ResultObject result = new ResultObject();
        try {
            result = blackService.update(blackReq);
        } catch(ValidationException e){
            logger.error(e.getErrorCodeEnum().message()+e.getParam(),e);
            result.fail(e.getErrorCodeEnum());
        }catch (Exception e) {
            logger.error(ErrorCodeEnum.SAVE_DATA_ERROR.message(),e);
            result.fail(ErrorCodeEnum.UPDATE_DATA_ERROR);
        }
        return result;
    }

    /**
     * @desc 删除
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.BLACK_DELETE, method = RequestMethod.DELETE)
    public ResultObject delete(Integer id) {
        if(id<=0 ){
            return new ResultObject(ErrorCodeEnum.PARAM_EXCEPTION.format("id错误"));
        }
        ResultObject result = new ResultObject();
        try {
            result = blackService.delete(id);
        }catch (Exception e) {
            logger.error(ErrorCodeEnum.SAVE_DATA_ERROR.message(),e);
            result.fail(ErrorCodeEnum.DELETE_DATA_ERROR);
        }
        return result;
    }

    /**
     * @desc 获取黑名单分页列表
     * @param blackQueryReq
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.BLACK_PAGE,method = RequestMethod.GET)
    public ResultObject page(BlackQueryReq blackQueryReq, PageBounds pageBounds) {
        return blackService.blackListPage(blackQueryReq,pageBounds);
    }

    @ResponseBody
    @RequestMapping(value = UserURL.BLACK_REFRESH)
    public ResultObject refreshWhiteList(){
        ResultObject result = new ResultObject();
        try {
            blackService.refreshBlackList();
        }catch (Exception e) {
            result = new ResultObject(ErrorCodeEnum.PARAM_EXCEPTION.format("黑名单"));
            logger.error(ErrorCodeEnum.REFRESH_DATA_ERROR.format("黑名单").message(),e);
        }
        return result;
    }

    /**
     * @desc 根据ID获取用户信息
    @ResponseBody
    @RequestMapping(value = UserURL.BLACK_GET,method = RequestMethod.GET)
    public ResultObject get(Integer id,HttpServletRequest request) {
        ResultObject ro = new ResultObject();
        try {
            SessionUser sessionUser = super.getNotNullSessionUser(request);
            ro = userService.getDetailByUserId(sessionUser.getUserId());
        } catch (ValidationException e) {
            ro.fail(e.getErrorCodeEnum());
        }
        return ro;
    }
    */
}