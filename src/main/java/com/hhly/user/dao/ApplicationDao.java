package com.hhly.user.dao;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.user.entity.Application;
import com.hhly.common.dao.AbstractBaseDao;
import com.hhly.common.enums.DeleteFlagEnum;
import com.hhly.user.api.dto.response.ApplicationResp;
import com.hhly.user.api.enums.StateEnum;
import com.hhly.user.mapper.ApplicationMapper;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
* @author wangxianchen
* @create 2017-11-27
* @desc 应用处理
*/
@Repository
public class ApplicationDao extends AbstractBaseDao<ApplicationMapper,Application> {


    public Application selectByCode(String code){
        Application app = new Application();
        app.setCode(code);
        return super.selectOneSelective(app);
    }

    public int updateByCode(Application record){
        return  baseMapper.updateByCode(record);
    }

    /**
     * 模糊查询
     * @param application
     * @return
     */
    public List<ApplicationResp> selectList(Application application, PageBounds pageBounds){
        return  baseMapper.selectList(application,pageBounds);
    }
    /**
     * @desc 根据appCode获取可用的app
     * @author wangxianchen
     * @create 2017-08-28
     * @param code
     * @return
     */
    public Application getAvailableAppByCode(String code){
        Application app = new Application();
        app.setCode(code);
        app.setIsDelete(DeleteFlagEnum.NO.getCode());
        app.setState(StateEnum.ENABLE.getCode());
        return super.selectOneSelective(app);
    }

}
