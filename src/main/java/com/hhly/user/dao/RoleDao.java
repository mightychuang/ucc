package com.hhly.user.dao;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.user.entity.Role;
import com.hhly.user.mapper.RoleMapper;
import com.hhly.common.dao.AbstractBaseDao;
import com.hhly.common.enums.DeleteFlagEnum;
import com.hhly.user.api.dto.request.RoleQueryReq;
import com.hhly.user.api.dto.response.RoleShortInfoResp;
import com.hhly.user.api.enums.StateEnum;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author wangxianchen
* @create 2017-11-27
* @desc 角色处理
*/
@Repository
public class RoleDao extends AbstractBaseDao<RoleMapper,Role> {

    public List<String> selectByRoleCodesAndAppCodes(String[] appCodes){
        return baseMapper.selectByRoleCodesAndAppCodes(appCodes);
    }

    public Role selectByCode(String code){
        Role role = new Role();
        role.setCode(code);
        return super.selectOneSelective(role);
    }

    /**
     * @desc 获取有效可用的角色
     * @author wangxianchen
     * @create 2017-09-18
     * @param roleCode
     * @return
     */
    public Role getAvailableRole(String roleCode){
        Role role = new Role();
        role.setCode(roleCode);
        role.setIsDelete(DeleteFlagEnum.NO.getCode());
        role.setState(StateEnum.ENABLE.getCode());
        return super.selectOneSelective(role);
    }

    public int updateByCode(Role role){
        return baseMapper.updateByCode(role);
    }

    /**
     * 根据 name 来模糊查询
     * @param roleQueryReq
     * @return
     */
    public List<RoleShortInfoResp> fuzzySelect(RoleQueryReq roleQueryReq, PageBounds pageBounds){
        return baseMapper.fuzzySelect(roleQueryReq,pageBounds);
    }

    public int selectExistCodeCount(List<String> codeArray){
        return baseMapper.selectExistCodeCount(codeArray,null,null);
    }

    /**
     * @desc 根据CODE删除角色
     * @author wangxianchen
     * @create 2017-09-27
     * @param code
     * @return
     */
    public int deleteByCode(String code){
        return baseMapper.deleteByCode(code);
    }

    public List<String> filterEnableRoleCodes(List<String> list){
        return baseMapper.selectEnableRoleCodes(list);
    }
}
