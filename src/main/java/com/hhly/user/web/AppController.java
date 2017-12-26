package com.hhly.user.web;


import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.common.dto.ResultObject;
import com.hhly.user.api.UserURL;
import com.hhly.user.api.dto.request.AppAddReq;
import com.hhly.user.api.dto.request.AppQueryReq;
import com.hhly.user.api.dto.request.AppUpdateReq;
import com.hhly.user.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
* @author wangxianchen
* @create 2017-09-27
* @desc app
*/
@RestController
public class AppController extends BaseController{

    @Autowired
    AppService appService;


    /**
     * @desc 应用查询
     * @author wangxianchen
     * @create 2017-09-27
     * @param req <Br>
     *          req.appCode  N <Br>
     *          req.appName  N <Br>
     *          req.state =  1（默认1）<Br><Br>
     *
     *          分页查询，但结果没有返回分页的页码和总条数
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.APP_QUERY,method = RequestMethod.GET)
    public ResultObject query(@Validated AppQueryReq req, PageBounds pageBounds) {
        return appService.selectList(req,pageBounds);
    }

    /**
     * @desc 查询单个明细
     * @author wangxianchen
     * @create 2017-09-27
     * @param appCode
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.APP_DETAIL,method = RequestMethod.GET)
    public ResultObject detail(@RequestParam(value="appCode") String appCode){
        return appService.detail(appCode);
    }

    /**
     * @desc 新增
     * @author wangxianchen
     * @create 2017-09-27
     * @param req
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.APP_ADD,method = RequestMethod.POST)
    public ResultObject add(@RequestBody @Validated AppAddReq req) {
         appService.add(req,super.getUserId());
         return new ResultObject();
    }

    /**
     * @desc 更新
     * @author wangxianchen
     * @create 2017-09-27
     * @param req
     * @return
     */
    @ResponseBody
    @RequestMapping(value = UserURL.APP_UPDATE,method = RequestMethod.POST)
    public ResultObject update(@RequestBody @Validated AppUpdateReq req) {
        appService.update(req,super.getUserId());
        return new ResultObject();
    }

}
