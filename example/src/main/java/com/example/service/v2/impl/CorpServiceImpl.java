package com.example.service.v2.impl;

import com.example.constant.MenuMessage;
import com.example.constant.Message;
import com.example.entity.CorpMstr;
import com.example.entity.RoleMstr;
import com.example.entity.UserMstr;
import com.example.exception.CustomHttpException;
import com.example.service.feign.DbHelperService;
import com.example.service.v2.CorpService;
import com.example.utils.*;
import net.sf.json.JSONArray;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;


@Service("CorpV2Service")
public class CorpServiceImpl implements CorpService {
    public static final String CODE = "code";
    public static final String SUCCESS_CODE = "10000";
    Logger logger = LoggerFactory.getLogger(CorpServiceImpl.class);
    private static final String DATASOURCE_POSTGRES = "postgres";
    @Autowired
    @Lazy
    DbHelperService dbHelperService;

    /**
     * 分页获取公司信息
     *
     * @return 结果封装类
     */
    @Override
    public ResultUtil getCorpInfoList(int pageIndex, int pageSize, String corp, List<Map<String, Object>> criteriaList) {
        String sortString = getSortString(criteriaList);
        String sql = "select" +
                " corp_id as \"corp\", corp_name as \"corpName\", corp_sname as \"corpSname\", corp_type as \"corpType\", corp_max_users as \"corpMaxUsers\", corp_admin as \"corpAdmin\", " +
                " corp_db as \"corpDb\", corp_host as \"corpHost\", corp_os as \"corpOs\", corp_port as \"corpPort\", " +
                " corp_scrpt_timeout as \"corpScrptTimeout\", corp_idle_timeout as \"corpIdleTimeout\", " +
                " corp_mod_date as \"corpId\", corp_mod_prog as \"corpModProg\", corp_mod_user as \"corpModUser\",corp_mod_time as \"corpModTime\"" +
                " from corp_mstr where corp_id ilike '%25"+corp+"%25' " +
                "order by "+sortString+";";
        String message;
        ResultUtil result = dbHelperService.selectPage(sql, DATASOURCE_POSTGRES, pageIndex, pageSize);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(Message.DELIVERY_GET_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        ArrayList list = (ArrayList) result.get("result");
        Map<String, Object> dataMap = new HashMap<>(2);
        //获取总条数
        int count = (int) result.get("count");
        int pageCount = (int) result.get("pageCount");
        dataMap.put("list", list);
        dataMap.put("count", count);
        message = MessageUtil.getMessage(Message.DELIVERY_GET_SUCCESS.getCode());
        logger.info(message);
        return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(dataMap);
    }

    /**
     * 处理排序列表
     *
     * @param criteriaList 排序列表
     * @return 处理后的排序条件
     */
    private String getSortString(List<Map<String, Object>> criteriaList) {
        StringBuilder criteriaBuilder = new StringBuilder();
        if (criteriaList.size() > 0) {
            for (int i = 0; i < criteriaList.size(); i++) {
                Map<String, Object> listMap = (Map<String, Object>) criteriaList.get(i);
                Optional<Object> sort = Optional.ofNullable(listMap.get("sort"));
                Optional<Object> criteria = Optional.ofNullable(listMap.get("criteria"));
                criteriaBuilder.append(criteria.orElse("corp"));
                if (!"0".equals(sort.orElse("0"))) {
                    criteriaBuilder.append(" desc");
                }
                criteriaBuilder.append(" ,");
            }
        } else {
            criteriaBuilder.append("corp_id,");
        }
        criteriaBuilder.setLength(criteriaBuilder.length()-1);
        return criteriaBuilder.toString();
    }


    /**
     * 单条添加公司信息
     *
     * @param corpMstr
     * @Return 结果封装类
     * @author
     * @date
     */
    @Override
    public ResultUtil insertCorpInfo(CorpMstr corpMstr) {
        String message;
        boolean corpExist = isCorpExist(corpMstr.getCorp());
        if (corpExist) {
            message = MessageUtil.getMessage(Message.DELIVERY_IS_EXIST.getCode());
            logger.warn(corpMstr.getCorp() + "：" + message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String sql = "insert into corp_mstr " +
                "(corp_id,corp_name, corp_sname, corp_type, corp_max_users, corp_admin, corp_db, corp_host, corp_os, corp_port) " +
                "values('" + corpMstr.getCorp() + "','" + corpMstr.getCorpName()+ "','"+ corpMstr.getCorpSname()+"','"+corpMstr.getCorpType()+"','"+
                corpMstr.getCorpMaxUsers()+"','"+corpMstr.getCorpAdmin()+"','"+corpMstr.getCorpDb()+"','"+corpMstr.getCorpHost()+"','"+corpMstr.getCorpOs()+"','"+corpMstr.getCorpPort()+"');";
        ResultUtil result = dbHelperService.insert(sql, DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(Message.DELIVERY_ADD_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message = MessageUtil.getMessage(Message.DELIVERY_ADD_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    /**
     * 批量添加公司信息
     *
     * @param corpMstrList 公司信息列表
     * @Return 结果封装类
     * @author
     * @date
     */
    @Override
    public ResultUtil insertCorpInfoList(List<CorpMstr> corpMstrList) {
        String message;
        for (CorpMstr corpMstr : corpMstrList) {
            ResultUtil result = insertCorpInfo(corpMstr);
            corpMstr.setResult(result.get("msg").toString());
            corpMstr.setCode(result.get("code").toString());
        }
        message=MessageUtil.getMessage(Message.DELIVERY_ADD_SUCCESS.getCode());
        return  ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(corpMstrList);
    }

    /**
     * 单条删除公司信息
     *
     * @param corpMstr 公司信息
     * @return 结果封装类
     * @author
     * @date
     */
    @Override
    public ResultUtil deleteCorpInfo(CorpMstr corpMstr) {
        String message;
        boolean corpExist = isCorpExist(corpMstr.getCorp());
        if (!corpExist) {
            message = MessageUtil.getMessage(Message.DELIVERY_NOT_EXIST.getCode());
            logger.warn(corpMstr.getCorp() + "：" + message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String sql ="delete from corp_mstr where lower(corp_id)=lower('"+corpMstr.getCorp()+"')";
        ResultUtil result=dbHelperService.delete(sql,DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(Message.DELIVERY_DELETE_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message = MessageUtil.getMessage(Message.DELIVERY_DELETE_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    /**
     * 批量删除公司信息
     *
     * @param corpMstrList 公司信息列表
     * @return 结果封装类
     * @author
     * @date
     */
    @Override
    public ResultUtil deleteCorpInfolist(List<CorpMstr> corpMstrList) {
        String message;
        for (CorpMstr corpMstr : corpMstrList) {
            ResultUtil result = deleteCorpInfo(corpMstr);
            corpMstr.setResult(result.get("msg").toString());
            corpMstr.setCode(result.get("code").toString());
        }
        message=MessageUtil.getMessage(Message.DELIVERY_DELETE_SUCCESS.getCode());
        return  ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(corpMstrList);
    }

    /**
     * 单条更新公司信息
     *
     * @param corpMstr 公司信息
     * @return 结果封装类
     * @author
     * @date
     */
    @Override
    public ResultUtil updateCorpInfo(CorpMstr corpMstr) {
        String message;
        boolean corpExist = isCorpExist(corpMstr.getCorp());
        if (!corpExist) {
            message = MessageUtil.getMessage(Message.DELIVERY_NOT_EXIST.getCode());
            logger.warn(corpMstr.getCorp() + "：" + message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String sql = "update corp_mstr set corp_name= '"+corpMstr.getCorpName()+"',corp_sname= '"+corpMstr.getCorpSname()+"',corp_type= '"+corpMstr.getCorpType()+"',corp_max_users= '"+corpMstr.getCorpMaxUsers()+
                "',corp_admin= '"+corpMstr.getCorpAdmin()+ "',corp_db= '"+corpMstr.getCorpDb()+"',corp_host= '"+corpMstr.getCorpHost()+"',corp_os= '"+corpMstr.getCorpOs()+"',corp_port= '"+corpMstr.getCorpPort()+"' "+
                "where lower(corp_id) = lower('"+corpMstr.getCorp()+"') ;";
        ResultUtil result=dbHelperService.update(sql,DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(Message.DELIVERY_UPDATE_ERROR.getCode());
            logger.error(message);

            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message = MessageUtil.getMessage(Message.DELIVERY_UPDATE_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName());
    }


    /**
     * 批量更新公司信息
     *
     * @param corpMstrList 公司信息列表
     * @return 结果封装类
     * @author
     * @date
     */
    @Override
    public ResultUtil updateCorpInfolist(List<CorpMstr> corpMstrList) {
        String message;
        for (CorpMstr corpMstr : corpMstrList) {
            ResultUtil result = updateCorpInfo(corpMstr);
            corpMstr.setResult(result.get("msg").toString());
            corpMstr.setCode(result.get("code").toString());
        }
        message=MessageUtil.getMessage(Message.DELIVERY_UPDATE_SUCCESS.getCode());
        return  ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(corpMstrList);
    }

    /**
     * 导入公司信息
     *
     * @param workbook
     * @return 结果封装类
     * @author
     * @date
     */
    @Override
    public ResultUtil exportCorp(Workbook workbook) {
        String defaultPassword= "123456";
        Sheet sheet=workbook.getSheetAt(0);
        //获取表格标题列表
        List titleList= PoiUtils.getTitleList(PoiUtils.getRow(sheet,0));
        //请求结果列表
        List<Map<String,Object>> resultList=new ArrayList<>();
        Map<String,Object> corpInfo;

        Optional<Object> corp;
        Optional<Object> corpName;
        Optional<Object> corpSname;
        Optional<Object> corpType;
        Optional<Object> corpMaxUsers;
        Optional<Object> corpAdmin;
        Optional<Object> corpDb;
        Optional<Object> corpHost;
        Optional<Object> corpOs;
        Optional<Object> corpPort;
        //循环遍历用户信息写入列表
        for (int i = 1; i <=sheet.getLastRowNum() ; i++) {
            //获取相应行的数据，转换为list
            corpInfo=PoiUtils.getRowData(PoiUtils.getRow(sheet,i),titleList);
            corp= Optional.ofNullable(corpInfo.get("corpId"));
            corpName = Optional.ofNullable(corpInfo.get("corpName"));
            corpSname = Optional.ofNullable(corpInfo.get("corpSname"));
            corpType = Optional.ofNullable(corpInfo.get("corpType"));
            corpMaxUsers = Optional.ofNullable(corpInfo.get("corpMaxUsers"));
            corpAdmin =Optional.ofNullable( corpInfo.get("corpAdmin"));
            corpDb = Optional.ofNullable(corpInfo.get("corpDb"));
            corpHost = Optional.ofNullable(corpInfo.get("corpHost"));
            corpOs = Optional.ofNullable(corpInfo.get("corpOs"));
            corpPort = Optional.ofNullable(corpInfo.get("corpPort"));
            //实体类赋值
            CorpMstr corpMstr=new CorpMstr();
            corpMstr.setCorp(corp.orElse("").toString());
            corpMstr.setCorpName(corpName.orElse("").toString());
            corpMstr.setCorpType(corpType.orElse("").toString());
            corpMstr.setCorpMaxUsers(Integer.parseInt(corpMaxUsers.orElse("0").toString()));
            corpMstr.setCorpAdmin(corpAdmin.orElse("").toString());
            corpMstr.setCorpDb(corpDb.orElse("").toString());
            corpMstr.setCorpHost(corpHost.orElse("").toString());
            corpMstr.setCorpPort(corpPort.orElse("").toString());
            corpMstr.setCorpOs(corpOs.orElse("").toString());
            corpMstr.setCorpSname(corpSname.orElse("").toString());

            //公司运行结果Map
        Map<String,Object> resultMap=new HashMap<>(2);
        //查看该角色是否存在
        if (!isCorpExist(corpMstr.getCorp())){
            ResultUtil insertResult= insertCorpInfo(corpMstr);
            resultMap.put(corpMstr.getCorp(),insertResult);
        }
        else {
            ResultUtil updateResult=updateCorpInfo(corpMstr);
            resultMap.put(corpMstr.getCorp(),updateResult);
        }
        resultList.add(resultMap);
    }
        return ResultUtil.ok().setData(resultList);
}


    /**
     * 导出公司信息
     *
     * @param isDelete 是否删除
     * @param corp
     * @return 结果封装类
     * @author
     * @date
     */
    @Override
    public ResultUtil deriveCorp(String corp, boolean isDelete) {
        String sql = "select\n" +
                "         corp_id as  \"corpId\", corp_name as \"corpName\", corp_sname as \"corpSname\"," +
                "         corp_type as \"corpType\", corp_max_users as \"corpMaxUsers\", corp_admin as \"corpAdmin\"," +
                "         corp_db as \"corpDb\", corp_host as \"corpHost\", corp_os as \"corpOs\", corp_port as \"corpPort\" " +
                "        from public.corp_mstr\n" +
                "        where corp_id ilike '%25"+corp+"%25' ";
        String message;
        ResultUtil result=dbHelperService.select(sql,DATASOURCE_POSTGRES);
        if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
            message=MessageUtil.getMessage(Message.QUERY_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message);
        }
        ArrayList list= (ArrayList) result.get("result");
        //判断导出记录是否存在
        if (list.size()==0){
            message=MessageUtil.getMessage(Message.QUERY_ERROR.getCode());
            logger.error(message);
            return ResultUtil.ok(message);
        }
        List<String> titleList=new ArrayList<>();
        titleList.add("corpId");
        titleList.add("corpName");
        titleList.add("corpSname");
        titleList.add("corpType");
        titleList.add("corpMaxUsers");
        titleList.add("corpAdmin");
        titleList.add("corpDb");
        titleList.add("corpHost");
        titleList.add("corpOs");
        titleList.add("corpPort");
        String diskPath="E:/test/";
        String path= ExcelUtils.createMapListExcel(list,diskPath,titleList);
        FileUtils fileUtils=new FileUtils();
        fileUtils.downLoad(path);
        //判断是否删除
        if(isDelete){
            String 	deleteSql = "delete  from corp_mstr " +
                    "where corp_id = "+corp+" " ;

            ResultUtil deleteResult=dbHelperService.delete(deleteSql,DATASOURCE_POSTGRES);
            if(HttpStatus.OK.value()!= (int)deleteResult.get(CODE)){
                message=MessageUtil.getMessage(Message.QUERY_ERROR.getCode());
                logger.error(message);
                return ResultUtil.error(message);
            }
        }
        message=MessageUtil.getMessage(Message.DERIVE_DELIVERY_SUCCESS.getCode());
        logger.info(message);
        return ResultUtil.ok().put("msg",message).put("result",path);
    }

    /**
     * 判断公司是否存在
     *
     * @param corp 公司名
     * @return 结果封装类
     */
    private boolean isCorpExist(String corp) {
        String message;
        String sql = "select 1 from corp_mstr where corp_id='" + corp + "';";
        ResultUtil result = dbHelperService.select(sql, DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(MenuMessage.QUERY_ERROR.getCode());
            logger.error(message);
            throw new CustomHttpException(message);
        }
        ArrayList list = (ArrayList) result.get("result");
        if (list.size() > 0) {
            return true;
        }
        return false;
    }
}
