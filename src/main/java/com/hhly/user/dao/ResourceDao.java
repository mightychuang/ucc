package com.hhly.user.dao;

import com.hhly.common.enums.DeleteFlagEnum;
import com.hhly.common.util.A;
import com.hhly.user.api.enums.ResEnums;
import com.hhly.user.api.enums.StateEnum;
import com.hhly.user.entity.Resource;
import com.hhly.user.mapper.ResourceMapper;
import com.hhly.common.dao.AbstractBaseDao;
import com.hhly.user.api.dto.response.MenuTree;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
* @author wangxianchen
* @create 2017-11-27
* @desc 资源操作
*/
@Repository
public class ResourceDao extends AbstractBaseDao<ResourceMapper,Resource> {

    public List<MenuTree> queryNavigationTree(List<String> list, String[] array,Byte state,Integer parentId,Byte depth){
        return baseMapper.queryNavigationTree(list,array,state,parentId,depth);
    }
    public Resource selectByCode(String resCode){
        Resource res = new Resource();
        res.setCode(resCode);
        return  super.selectOneSelective(res);
    }
    public Resource selectBySelfId(int selfId){
        Resource res = new Resource();
        res.setSelfId(selfId);
        return  super.selectOneSelective(res);
    }

    public boolean selectExistByAppCodeAndUrl(String appCode,String url){
        int count = baseMapper.selectExistByAppCodeAndUrl(appCode,url);
        return count > 0 ? true : false;
    }

    public int updateByCode(Resource resource){
        return baseMapper.updateByCode(resource);
    }
    public int updateByList(List<Resource> list){
        return baseMapper.updateByList(list);
    }

    public MenuTree findTreeBySelfId(int selfId){
        return baseMapper.findTreeBySelfId(selfId);
    }

    public int selectExistCodeCount(List<String> codeArray,String appCode){
        return  baseMapper.selectExistCodeCount(codeArray,appCode);
    }

    public  List<Resource> selectWhiteList(){
        //IS_DELETE=0 AND TYPE_='API' AND JOIN_WHITE_LIST=1 AND STATE=1
        Resource temp = new Resource();
        temp.setIsDelete(DeleteFlagEnum.NO.getCode());
        temp.setJoinWhiteList(ResEnums.JOIN_WHITE_LIST.YES.getCode());
        temp.setState(StateEnum.ENABLE.getCode());
        return super.selectManySelective(temp);
    }


    public List<MenuTree> getTree(String appCode,Integer parentId) {
        return baseMapper.getAllMenus(appCode,parentId);
    }

    public int deleteByCodes(List<String> codeList){
        return baseMapper.deleteByCodes(codeList);
    }

    public List<Resource> selectByCodes(List<String> codeList){
        if(A.isEmpty(codeList)){
            return null;
        }
        return baseMapper.selectByCodes(codeList);
    }


    /**
     * @desc 获取下一个可用SELF_ID
     * @author wangxianchen
     * @create 2017-09-26
     * @return
     */
    public int generateSelfId(){
        return baseMapper.getMaxSelfId()+1;
    }
}
