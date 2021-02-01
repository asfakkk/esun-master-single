package com.example.service.v2.impl;

import com.example.constant.DomainMessage;
import com.example.constant.MenuMessage;
import com.example.entity.DomainMstr;
import com.example.exception.CustomHttpException;
import com.example.service.feign.DbHelperService;
import com.example.service.v2.DomainService;
import com.example.utils.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;


@Service("DomainV2Service")
public  class DomainServiceImpl implements DomainService {
    private static Logger logger = LoggerFactory.getLogger(DomainServiceImpl.class);

    @Autowired
    @Lazy
    DbHelperService dbHelperService;
    /**
     * Postgres数据源
     */
    private final static String DATASOURCE_POSTGRES = "postgres";
    /**
     * Mysql数据源
     */
    private final static String DATASOURCE_MYSQL = "mysql";
    @Value("${file.diskPath}")
    String diskPath;

    @Value("${ftp.url}")
    String ftpUrl;

    @Value("${ftp.port}")
    int ftpPort;

    @Value("${ftp.username}")
    String ftpUsername;

    @Value("${ftp.password}")
    String ftpPassword;

    @Value("${ftp.ftpPath}")
    String ftpPath;
    public static final String CODE = "code";
    public static final String SUCCESS_CODE = "10000";

    @Override
    public ResultUtil getDomainInfoList(int pageIndex, int pageSize, String domainDomain, List<Map<String, Object>> criteriaList) {
        String sortString = getSortString(criteriaList);
        String sql = "select domain_domain as \"domainDomain\", domain_name as \"domainName\", domain_corp as \"domainCorp\", domain_sname as \"domainSname\", " +
                "domain_db as \"domainDb\", domain_active as \"domainActive\", domain_propath as \"domainPropath\", " +
                "domain_type as \"domainType\", domain_max_users as \"domainMaxUsers\", domain_admin as  \"domainAdmin\" " +
                "from domain_mstr " +
                "where domain_domain ilike '%25" + domainDomain + "%25' " +
                "order by " + sortString + ";";
        String message;
        ResultUtil result = dbHelperService.selectPage(sql, DATASOURCE_POSTGRES, pageIndex, pageSize);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(DomainMessage.DOMAIN_GET_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        ArrayList list = (ArrayList) result.get("result");
        Map<String, Object> dataMap = new HashMap<>(10);
        int pageCount = (int) result.get("pageCount");
        int count = (int) result.get("count");
        dataMap.put("list", list);
        dataMap.put("pageCount", pageCount);
        dataMap.put("count", count);
        message = MessageUtil.getMessage(DomainMessage.DOMAIN_GET_SUCCESS.getCode());
        logger.info(message);
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(dataMap);
    }

    /**
     * 获取排序条件字符串
     *
     * @param criteriaList 排序列表
     * @return 排序后的字符
     * @author john.xiao
     * @date 2020-12-17 11-27
     */
    private String getSortString(List<Map<String, Object>> criteriaList) {
        StringBuilder criteriaBuilder = new StringBuilder();
        if (criteriaList.size() > 0) {
            for (int i = 0; i < criteriaList.size(); i++) {
                Map<String, Object> listMap = (Map<String, Object>) criteriaList.get(i);
                Optional<Object> sort = Optional.ofNullable(listMap.get("sort"));
                Optional<Object> criteria = Optional.ofNullable(listMap.get("criteria"));
                criteriaBuilder.append(criteria.orElse("domain"));
                if (!"0".equals(sort.orElse("0"))) {
                    criteriaBuilder.append(" desc");
                }
                criteriaBuilder.append(" ,");
            }
        } else {
            //设置默认排序项
            criteriaBuilder.append("domain_domain,");
        }
        criteriaBuilder.setLength(criteriaBuilder.length() - 1);
        return criteriaBuilder.toString();
    }

    @Override
    public ResultUtil insertDomainInfo(DomainMstr domainMstr) {
        String message;
        boolean domainExist = checkDomainExist(domainMstr.getDomainDomain());
        if (domainExist) {
            message = MessageUtil.getMessage(DomainMessage.DOMAIN_IS_EXIST.getCode());
            logger.warn(domainMstr.getDomainDomain() + ":" + message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String sql = "insert into domain_mstr " +
                "( domain_corp, domain_domain, domain_name, domain_sname, domain_db, domain_active, domain_propath, " +
                " domain_type, domain_max_users, domain_admin)" + " values('" + domainMstr.getDomainCorp() + "','" + domainMstr.getDomainDomain() + "','" + domainMstr.getDomainName() + "','"
                + domainMstr.getDomainSname() + "','" + domainMstr.getDomainDb() + "','" + domainMstr.getDomainActive() +
                "','" + domainMstr.getDomainPropath() + "','" + domainMstr.getDomainType() + "','"
                + domainMstr.getDomainMaxUsers() + "','" + domainMstr.getDomainAdmin() + "') ;";


        ResultUtil result = dbHelperService.insert(sql, DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(DomainMessage.DOMAIN_ADD_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message = MessageUtil.getMessage(DomainMessage.DOMAIN_ADD_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public ResultUtil updateDomainInfo(DomainMstr domainMstr) {
        String message;
        boolean domainExist = checkDomainExist(domainMstr.getDomainDomain());
        if (!domainExist) {
            message = MessageUtil.getMessage(DomainMessage.DOMAIN_NOT_EXIST.getCode());
            logger.warn(domainMstr.getDomainDomain() + ":" + message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String sql = "update domain_mstr set domain_domain = '" + domainMstr.getDomainDomain() + "', domain_corp = '" + domainMstr.getDomainCorp() + "', domain_name= '" + domainMstr.getDomainName() + "',domain_sname = '" +
                domainMstr.getDomainSname() + "',domain_db = '" + domainMstr.getDomainDb() + "', domain_active= '" + domainMstr.getDomainActive() + "', domain_propath= '" + domainMstr.getDomainPropath() + "'," +
                "domain_type = '" + domainMstr.getDomainType() + "', domain_max_users='" + domainMstr.getDomainMaxUsers() + "', domain_admin='"
                + domainMstr.getDomainAdmin() + "' " +
                "where lower(domain_domain)= lower('" + domainMstr.getDomainDomain() + "');";
        ResultUtil result = dbHelperService.update(sql, DATASOURCE_POSTGRES);

        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(DomainMessage.DOMAIN_UPDATE_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message = MessageUtil.getMessage(DomainMessage.DOMAIN_UPDATE_SUCCESS.getCode());

        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public ResultUtil deleteDomainInfo(DomainMstr domainMstr) {
        String message;
        boolean domainExist = checkDomainExist(domainMstr.getDomainDomain());
        if (!domainExist) {
            message = MessageUtil.getMessage(DomainMessage.DOMAIN_NOT_EXIST.getCode());
            logger.warn(domainMstr.getDomainDomain() + ":" + message);
            return ResultUtil.error(message);
        }
        String sql = "delete from domain_mstr where lower(domain_domain)=lower('" + domainMstr.getDomainDomain() + "')";
        ResultUtil result = dbHelperService.delete(sql, DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(DomainMessage.DOMAIN_DELETE_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message = MessageUtil.getMessage(DomainMessage.DOMAIN_DELETE_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    @Override
    public ResultUtil deleteDomainInfoList(List<DomainMstr> domainMstrList) {
        String message;
        for (DomainMstr domainMstr : domainMstrList) {
            ResultUtil result = deleteDomainInfo(domainMstr);
            domainMstr.setResult(result.get("msg").toString());
            domainMstr.setCode(result.get("code").toString());
        }
        message = MessageUtil.getMessage(DomainMessage.DOMAIN_DELETE_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(domainMstrList);
    }

    @Override
    public ResultUtil insertDomainInfoList(List<DomainMstr> domainMstrList) {
        String message;
        for (DomainMstr domainMstr : domainMstrList) {
            ResultUtil result = insertDomainInfo(domainMstr);
            domainMstr.setResult(result.get("msg").toString());
            domainMstr.setCode(result.get("code").toString());
        }
        message = MessageUtil.getMessage(DomainMessage.DOMAIN_ADD_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(domainMstrList);
    }


    @Override
    public ResultUtil updateDomainInfoList(List<DomainMstr> domainMstrList) {
        String message;
        for (DomainMstr domainMstr : domainMstrList) {
            ResultUtil result = updateDomainInfo(domainMstr);
            domainMstr.setResult(result.get("msg").toString());
            domainMstr.setCode(result.get("code").toString());
        }
        message = MessageUtil.getMessage(DomainMessage.DOMAIN_UPDATE_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(domainMstrList);
    }


    /**
     * 导入域信息
     * 批量插入或更新
     *
     * @param workbook Excel文件
     * @return
     */
    @Override
    public ResultUtil batchDomainInfoInsertOrUpdate(Workbook workbook) {
        Sheet sheet = workbook.getSheetAt(0);
        List titleList = PoiUtils.getTitleList(PoiUtils.getRow(sheet, 0));
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> Info;
        Optional<Object> domainCorp;
        Optional<Object> domainDomain;
        Optional<Object> domainName;
        Optional<Object> domainSName;
        Optional<Object> domainDb;
        Optional<Object> domainActive;
        Optional<Object> domainPropath;
        Optional<Object> domainType;
        Optional<Object> domainMaxUsers;
        Optional<Object> domainAdmin;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Info = PoiUtils.getRowData(PoiUtils.getRow(sheet, i), titleList);
            domainDomain = Optional.ofNullable(Info.get("domainDomain"));
            domainCorp = Optional.ofNullable(Info.get("domainCorp"));
            domainName = Optional.ofNullable(Info.get("domainName"));
            domainSName = Optional.ofNullable(Info.get("domainSname"));
            domainDb = Optional.ofNullable(Info.get("domainDb"));
            domainActive = Optional.ofNullable(Info.get("domainActive"));
            domainPropath = Optional.ofNullable(Info.get("domainPropath"));
            domainType = Optional.ofNullable(Info.get("domainType"));
            domainMaxUsers = Optional.ofNullable(Info.get("domainMaxUsers"));
            domainAdmin = Optional.ofNullable(Info.get("domainAdmain"));

            DomainMstr domainMstr = new DomainMstr();
            domainMstr.setDomainDomain(domainDomain.orElse("").toString());
            domainMstr.setDomainCorp(domainCorp.orElse("").toString());
            domainMstr.setDomainName(domainName.orElse("").toString());
            domainMstr.setDomainSname(domainSName.orElse("").toString());
            domainMstr.setDomainDb(domainDb.orElse("").toString());
            domainMstr.setDomainActive(Boolean.parseBoolean(domainActive.orElse("").toString()));
            domainMstr.setDomainPropath(domainPropath.orElse("").toString());
            domainMstr.setDomainType(domainType.orElse("").toString());
            domainMstr.setDomainMaxUsers(Integer.parseInt(domainMaxUsers.orElse("").toString()));
            domainMstr.setDomainAdmin(domainAdmin.orElse("").toString());

            Map<String, Object> resultMap = new HashMap<>(10);
            if (!checkDomainExist(domainMstr.getDomainDomain())) {
                ResultUtil insertResult = insertDomainInfo(domainMstr);
                resultMap.put(domainMstr.getDomainDomain(), insertResult);
            } else {
                ResultUtil updateResult = updateDomainInfo(domainMstr);
                resultMap.put(domainMstr.getDomainDomain(), updateResult);
            }
            resultList.add(resultMap);
        }
        return ResultUtil.ok().setData(resultList);

    }

    /**
     * 导出域信息
     *
     * @param domainDomain
     */
    @Override
    public ResultUtil exportDomainInfo(String domainDomain) {
        String sql = "select domain_domain as \"domainDomain\", domain_name as \"domainName\", domain_corp as \"domainCorp\", domain_sname as \"domainSname\", " +
                "domain_db as \"domainDb\", domain_active as \"domainActive\", domain_propath as \"domainPropath\", " +
                "domain_type as \"domainType\", domain_max_users as \"domainMaxUsers\", domain_admin as  \"domainAdmin\" " +
                "from domain_mstr " +
                " where domain_domain ilike '%" + domainDomain + "%' ";
        String message;
        ResultUtil result = dbHelperService.select(sql, DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(DomainMessage.DOMAIN_EXPORT_ERROR.getCode());
            logger.error(message);
        }
        ArrayList list = (ArrayList) result.get("result");
        if (list.size() == 0) {
            message = MessageUtil.getMessage(DomainMessage.DOMAIN_EXPORT_ERROR.getCode());
            logger.error(message);
        }
        List<String> titlelist = new ArrayList<>();
        titlelist.add("domainDomain");
        titlelist.add("domainCorp");
        titlelist.add("domainName");
        titlelist.add("domainSname");
        titlelist.add("domainDb");
        titlelist.add("domainActive");
        titlelist.add("domainPropath");
        titlelist.add("domainType");
        titlelist.add("domainMaxUsers");
        titlelist.add("domainAdmin");

        String diskPath = "D:/test/";
        String path = ExcelUtils.createMapListExcel(list, diskPath, titlelist);
        FileUtils fileUtils = new FileUtils();
        fileUtils.downLoad(path);

        message = MessageUtil.getMessage(DomainMessage.DOMAIN_EXPORT_SUCCESSS.getCode());
        logger.info(message);
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName());

    }



    /**
     * 判断作用域是否存在
     *
     * @param domain 域
     * @return ture:存在该域;false:不存在该域
     * @author jonh xiao
     * @date 2021/1/19
     */
    private boolean checkDomainExist(String domain) {
        String message;
        String sql = "select 1 from domain_mstr where domain_domain='" + domain + "';";
        ResultUtil result = dbHelperService.select(sql, DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(MenuMessage.QUERY_ERROR.getCode());
            logger.error(message);
            throw new CustomHttpException(message);
        }
        ArrayList<HashMap> list = (ArrayList) result.get("result");
        return list.size() > 0;
    }

    /**
     * 获取用户域
     *
     * @param user 用户名
     * @return
     */
    @Override
    public ResultUtil getUserdomainInfo(String user) {
        String message;
        String sql = "select userdomain_userid as user,userdomain_corp as domainCorp,userdomain_domain as domain from userdomain_ref where lower(userdomain_userid) = lower('" + user + "')";
        ResultUtil result = dbHelperService.select(sql, DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(DomainMessage.DOMAIN_GET_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        ArrayList list = (ArrayList) result.get("result");
        Map<String, Object> dataMap = new HashMap<>(3);
        dataMap.put("list", list);
        message = MessageUtil.getMessage(DomainMessage.DOMAIN_GET_SUCCESS.getCode());
        logger.info(message);
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(dataMap);
    }



    /**
     * 更新用户域信息
     */
    @Override
    public ResultUtil updateUserDomain(DomainMstr domainMstr,String user) {
        String message;
        boolean domainExist = checkDomainExist(domainMstr.getDomainDomain());
        if(!domainExist){
            message = MessageUtil.getMessage(DomainMessage.DOMAIN_NOT_EXIST.getCode());
            logger.warn(domainMstr.getDomainDomain()+": "+message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String addSql ="insert into userdomain_ref(userdomain_userid,userdomain_corp,userdomain_domain) " +
                "values ('"+user+"','"+domainMstr.getDomainCorp()+"','"+domainMstr.getDomainDomain()+"')";
        ResultUtil resultadd = dbHelperService.update(addSql,DATASOURCE_POSTGRES);
        if(!SUCCESS_CODE.equals(resultadd.get(CODE).toString())){
            message=MessageUtil.getMessage(DomainMessage.DOMAIN_UPDATE_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message=MessageUtil.getMessage(DomainMessage.DOMAIN_UPDATE_SUCCESS.getCode());

        return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName());
    }

    /**
     * 批量更新用户域信息
     * @param domainMstrList 域信息列表
     * @param user 用户名
     * @return
     */
    @Override
    public ResultUtil updateUserDomainList(List<DomainMstr> domainMstrList, String user) {
        List<Map<String,Object>> resultList = new ArrayList<>(domainMstrList.size());
        String message;
        String deleteSql = "delete from userdomain_ref where userdomain_userid = '"+user+"';";
        ResultUtil resultupdate = dbHelperService.update(deleteSql,DATASOURCE_POSTGRES);
        if(!SUCCESS_CODE.equals(resultupdate.get(CODE).toString())){
            message = MessageUtil.getMessage(DomainMessage.DOMAIN_UPDATE_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
        }

        for(int i = 0; i < domainMstrList.size(); i++){
            ResultUtil result = updateUserDomain(domainMstrList.get(i),user);
            Map<String,Object> resultMap = new HashMap<>();
            resultMap.put(domainMstrList.get(i).getDomainDomain(),result);
            resultList.add(resultMap);
        }
        message = MessageUtil.getMessage(DomainMessage.DOMAIN_UPDATE_SUCCESS.getCode());
        return  ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(resultList);
    }
}


