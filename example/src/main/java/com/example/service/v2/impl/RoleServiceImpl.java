package com.example.service.v2.impl;

import com.example.constant.MenuMessage;
import com.example.entity.CorpMstr;
import com.example.entity.RoleMstr;
import com.example.exception.CustomHttpException;
import com.example.service.feign.DbHelperService;
import com.example.service.v2.RoleService;
import com.example.utils.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("RoleV2Service")
public class RoleServiceImpl implements RoleService {
    public static final String CODE = "code";
    public static final String SUCCESS_CODE="10000";
    Logger logger= LoggerFactory.getLogger(RoleServiceImpl.class);
    private  static final String DATASOURCE_POSTGRES="postgres";
    @Autowired
    @Lazy
    DbHelperService dbHelperService;
    @Override
    public ResultUtil getRoleInfoList(int pageIndex, int pageSize, String roleName,List<Map<String, Object>> criteriaList) {
        String sortString=getSortString(criteriaList);
        String sql="select role_name as \"roleName\",role_desc as \"roleDesc\"" +
                " from role_mstr where role_name ilike '%25"+roleName+"%25' " +
                "order by "+sortString+";";
        String message;
        ResultUtil result=dbHelperService.selectPage(sql,DATASOURCE_POSTGRES,pageIndex,pageSize);
        if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
            message=MessageUtil.getMessage(MenuMessage. ROLE_GET_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        ArrayList list= (ArrayList) result.get("result");
        Map<String,Object> dataMap=new HashMap<>(2);
        //获取总条数
        int count= (int) result.get("count");
        dataMap.put("list",list);
        dataMap.put("count",count);
        message=MessageUtil.getMessage(MenuMessage.ROLE_GET_SUCCESS.getCode());
        logger.info(message);
        return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(dataMap);
    }

    /**
     * 处理排序列表
     * @param criteriaList 排序列表
     * @return 处理后的排序条件
     */
    private String getSortString(List<Map<String, Object>> criteriaList){
        StringBuilder criteriaBuilder=new StringBuilder();
        if(criteriaList.size()>0){
            for (int i = 0; i < criteriaList.size(); i++) {
                Map<String,Object> listMap= (Map<String, Object>) criteriaList.get(i);
                Optional<Object> sort=Optional.ofNullable(listMap.get("sort"));
                Optional<Object> criteria=Optional.ofNullable(listMap.get("criteria"));
                criteriaBuilder.append(criteria.orElse("roleName"));
                if (!"0".equals(sort.orElse("0"))){
                    criteriaBuilder.append(" desc");
                }
                criteriaBuilder.append(" ,");
            }
        }
        else {
            //设置默认排序项
            criteriaBuilder.append("role_name,");
        }
        criteriaBuilder.setLength(criteriaBuilder.length()-1);
        return  criteriaBuilder.toString();
    }

    /**
     * 单条添加角色
     * @param roleMstr 角色实体类
     * @return 结果封装类
     */
    @Override
    public ResultUtil insertRoleInfo(RoleMstr roleMstr) {
        String message;
        String GUID= GUIDUtils.create();
        boolean roleExist=isRoleExist(roleMstr.getRoleName());
        if(roleExist){
            message=MessageUtil.getMessage(MenuMessage.ROLE_IS_EXIST.getCode());
            logger.warn(roleMstr.getRoleName()+"："+message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String sql="insert into role_mstr "+
                "(role_name,role_desc) " +
                "values('"+roleMstr.getRoleName()+"','"+roleMstr.getRoleDesc()+"');";
        ResultUtil result=dbHelperService.insert(sql,DATASOURCE_POSTGRES);
        if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
            message=MessageUtil.getMessage(MenuMessage.ROLE_ADD_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message=MessageUtil.getMessage(MenuMessage.ROLE_ADD_SUCCESS.getCode());
        return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    /**
     * 单条删除角色
     * @param roleMstr 角色实体类
     * @return 结果封装类
     */
    @Override
    public ResultUtil deleteRoleInfo(RoleMstr roleMstr) {
        String message;
        boolean roleExist=isRoleExist(roleMstr.getRoleName());
        if(!roleExist){
            message=MessageUtil.getMessage(MenuMessage.ROLE_NOT_EXIST.getCode());
            logger.warn(roleMstr.getRoleName()+"："+message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String sql ="delete from role_mstr where lower(role_name)=lower('"+roleMstr.getRoleName()+"')";
        ResultUtil result=dbHelperService.delete(sql,DATASOURCE_POSTGRES);
        if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
            message=MessageUtil.getMessage(MenuMessage.ROLE_DELETE_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message=MessageUtil.getMessage(MenuMessage.ROLE_DELETE_SUCCESS.getCode());
        return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName());

    }
    /**
     * 单条更新角色
     * @param roleMstr 角色实体类
     * @return 结果封装类
     */
    @Override
    public ResultUtil updateRoleInfo(RoleMstr roleMstr) {
        String message;
        boolean roleExist=isRoleExist(roleMstr.getRoleName());
        if(!roleExist){
            message=MessageUtil.getMessage(MenuMessage.ROLE_NOT_EXIST.getCode());
            logger.warn(roleMstr.getRoleName()+"："+message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String sql = "update role_mstr set role_desc = '"+roleMstr.getRoleDesc()+"' "+
                "where lower(role_name)=lower('"+roleMstr.getRoleName()+"') ;";
        ResultUtil result=dbHelperService.update(sql,DATASOURCE_POSTGRES);
        if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
            message=MessageUtil.getMessage(MenuMessage.ROLE_UPDATE_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message=MessageUtil.getMessage(MenuMessage.ROLE_UPDATE_SUCCESS.getCode());
        return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    /**
     * 批量插入角色信息
     * @param roleMstrList 实体类列表
     * @return 结果封装类
     */
    @Override
    public ResultUtil insertRoleInfoList(List<RoleMstr> roleMstrList) {
        String message;
        for (RoleMstr roleMstr : roleMstrList) {
            ResultUtil result = insertRoleInfo(roleMstr);
            roleMstr.setResult(result.get("msg").toString());
            roleMstr.setCode(result.get("code").toString());
        }
        message=MessageUtil.getMessage(MenuMessage.ROLE_ADD_SUCCESS.getCode());
        return  ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(roleMstrList);
    }

    /**
     * 批量删除角色信息
     * @param roleMstrList 实体类列表
     * @return 结果封装类
     */
    @Override
    public ResultUtil deleteRoleInfolist(List<RoleMstr> roleMstrList) {
        String message;
        for (RoleMstr roleMstr : roleMstrList) {
            ResultUtil result = deleteRoleInfo(roleMstr);
            roleMstr.setResult(result.get("msg").toString());
            roleMstr.setCode(result.get("code").toString());
        }
        message=MessageUtil.getMessage(MenuMessage.ROLE_DELETE_SUCCESS.getCode());
        return  ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(roleMstrList);
    }


    /**
     * 批量更新角色
     * @param roleMstrList 菜单实体类列表
     * @return 结果封装类
     */
    @Override
    public ResultUtil updateRoleInfolist(List<RoleMstr> roleMstrList) {
        String message;
        for (RoleMstr roleMstr : roleMstrList) {
            ResultUtil result = updateRoleInfo(roleMstr);
            roleMstr.setResult(result.get("msg").toString());
            roleMstr.setCode(result.get("code").toString());
        }
        message=MessageUtil.getMessage(MenuMessage.ROLE_UPDATE_SUCCESS.getCode());
        return  ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(roleMstrList);
    }


    /**
     * 获取用户角色信息
     * @param userUserId
     * @return
     */
    @Override
    public ResultUtil getUserRoleInfoList(String userUserId) {
        String sql = "select  rm.role_name as \"roleName\", role_desc as \"roleDesc\" from userd_det ud left join role_mstr rm on ud.userd_role = rm.role_name \n" +
                "where lower(ud.userd_userid) = lower('" + userUserId + "')";

        String message;
        ResultUtil result = dbHelperService.select(sql, DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(MenuMessage.ROLE_GET_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        ArrayList list = (ArrayList) result.get("result");
        Map<String, Object> dataMap = new HashMap<>(2);
        dataMap.put("list", list);
        message = MessageUtil.getMessage(MenuMessage.ROLE_GET_SUCCESS.getCode());
        logger.info(message);
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(dataMap);
    }
    /**
     * 批量更新用户角色
     * @param roleMstrList
     * @param userUserId
     * @return
     * @author john.xiao
     * @date 2020-10-13 17:06
     */
   @Override
    public ResultUtil updateUserRoleInfoList(List<RoleMstr> roleMstrList, String userUserId) {
       List<Map<String,Object>> resultList=new ArrayList<>(roleMstrList.size());
       String message;
       String deleteSql = "delete from userd_det where lower(userd_userid) = lower('"+userUserId+"')";
       ResultUtil resultupdate=dbHelperService.update(deleteSql,DATASOURCE_POSTGRES);
       if(!SUCCESS_CODE.equals(resultupdate.get(CODE).toString())){
           message=MessageUtil.getMessage(MenuMessage.ROLE_UPDATE_ERROR.getCode());
           logger.error(message);
           return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
       }

        for (int i = 0; i < roleMstrList.size(); i++) {
            ResultUtil result=updateUserRoleInfo(roleMstrList.get(i),userUserId);
            Map<String,Object> resultMap=new HashMap<>();
            resultMap.put(roleMstrList.get(i).getRoleName(),result);
            resultList.add(resultMap);
        }
        message=MessageUtil.getMessage(MenuMessage.ROLE_UPDATE_SUCCESS.getCode());
        return  ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(resultList);
    }


    /**
     * 更新用户角色
     * @param roleMstr
     * @param userUserId
     * @return
     * @author john.xiao
     * @date 2020-10-13 17:06
     */

   @Override
    public ResultUtil updateUserRoleInfo(RoleMstr roleMstr, String userUserId) {
        String message;
        boolean roleExist=isRoleExist(roleMstr.getRoleName());
        if(!roleExist){
            message=MessageUtil.getMessage(MenuMessage.ROLE_NOT_EXIST.getCode());
            logger.warn(roleMstr.getRoleName()+"："+message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String addSql = "insert into userd_det(userd_userid,userd_role) values ('"+userUserId+"','"+roleMstr.getRoleName()+"') ";
        ResultUtil resultadd=dbHelperService.update(addSql,DATASOURCE_POSTGRES);
        if(!SUCCESS_CODE.equals(resultadd.get(CODE).toString())){
            message=MessageUtil.getMessage(MenuMessage.ROLE_UPDATE_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message=MessageUtil.getMessage(MenuMessage.ROLE_UPDATE_SUCCESS.getCode());
        return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName());
    }


    /**
     * 导入Excel
     * @param  workbook
     * @return 结果封装类
     */
    @Override
    public ResultUtil batchRoleInfoInsertOrUpdate(Workbook workbook) {
        String defaultPassword= "123456";
        //获取Excel文档第一个表格
        Sheet sheet=workbook.getSheetAt(0);
        //获取表格标题列表
        List titleList= PoiUtils.getTitleList(PoiUtils.getRow(sheet,0));
        //请求结果列表
        List<Map<String,Object>> resultList=new ArrayList<>();
        Map<String,Object> roleInfo;
        Optional<Object> roleName;
        Optional<Object> roleDesc;
        //循环遍历用户信息写入列表
        for (int i = 1; i <=sheet.getLastRowNum() ; i++) {
            //获取相应行的数据，转换为list
            roleInfo=PoiUtils.getRowData(PoiUtils.getRow(sheet,i),titleList);
            roleName=Optional.ofNullable(roleInfo.get("roleName"));
            roleDesc=Optional.ofNullable(roleInfo.get("roleDesc"));

            //实体类赋值
            RoleMstr roleMstr=new RoleMstr();
            roleMstr.setRoleName(roleName.orElse("").toString());
            roleMstr.setRoleDesc(roleDesc.orElse("").toString());

            //角色运行结果Map
            Map<String,Object> resultMap=new HashMap<>(2);
            //查看该角色是否存在
            if (!isRoleExist(roleMstr.getRoleName())){
                ResultUtil insertResult= insertRoleInfo(roleMstr);
                resultMap.put(roleMstr.getRoleName(),insertResult);
            }
            else {
                ResultUtil updateResult=updateRoleInfo(roleMstr);
                resultMap.put(roleMstr.getRoleName(),updateResult);
            }
            resultList.add(resultMap);
        }
        return ResultUtil.ok().setData(resultList);
    }

    /**
     * 导出信息
     * @param roleName 角色名
     * @return
     */
    @Override
    public void exportRoleInfo(String roleName) {
        String sql="select role_name as \"roleName\" ,role_desc as \"roleDesc\" " +
                "from role_mstr where role_name ilike '%25"+roleName+"%25';";
        String message;
        ResultUtil result=dbHelperService.select(sql,DATASOURCE_POSTGRES);
        if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
            message=MessageUtil.getMessage(MenuMessage.ROLE_GET_ERROR.getCode());
            logger.error(message);
        }
        ArrayList list= (ArrayList) result.get("result");
        //判断角色是否存在
        if (list.size()==0){
            message=MessageUtil.getMessage(MenuMessage.ROLE_NOT_EXIST.getCode());
            logger.error(message);
        }
        List<String> titleList=new ArrayList<>();
        titleList.add("roleName");
        titleList.add("roleDesc");
        String diskPath="E:/test/";
        String path= ExcelUtils.createMapListExcel(list,diskPath,titleList);
        FileUtils fileUtils=new FileUtils();
        fileUtils.downLoad(path);
    }


    /**
     * 判断角色是否存在
     * @param roleName 角色名
     * @return 结果封装类
     */
    private boolean isRoleExist(String roleName){
        String message;
        String sql = "select 1 from  role_mstr where  lower(role_name)=lower('"+roleName+"')";
        ResultUtil result=dbHelperService.select(sql,DATASOURCE_POSTGRES);
        if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
            message=MessageUtil.getMessage(MenuMessage.QUERY_ERROR.getCode());
            logger.error(message);
            throw new CustomHttpException(message);
        }
        ArrayList<HashMap> list= (ArrayList) result.get("result");
        return list.size() > 0;
    }

}

