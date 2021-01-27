package com.example.service.v2;

import com.example.entity.DomainMstr;
import com.example.utils.ResultUtil;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * @author John.xiao
 */
public interface DomainService {
    /**
     * 获取域信息
     * @param pageIndex 页码数
     * @param pageSize  分页大小
     * @param criteriaList 排序列表
     * @return 返回结果工具类
     */
    ResultUtil getDomainInfoList(int pageIndex, int pageSize, String domain, List<Map<String, Object>> criteriaList);

    /**
     * 插入单条域信息
     * @param domainMstr
     * @return
     */
    ResultUtil insertDomainInfo(DomainMstr domainMstr);

    /**
     * 更新单条信息
     * @param domainMstr
     * @return
     */
    ResultUtil updateDomainInfo(DomainMstr domainMstr);

    /**
     * 删除单条信息
     * @param domainMstr
     * @return
     */
    ResultUtil deleteDomainInfo(DomainMstr domainMstr);


    /**
     * 根据域ID批量删除域，该方法为精确查询
     * @param domainMstrList 域实体类列表
     * @return 结果封装类
     */
    ResultUtil deleteDomainInfoList(List<DomainMstr> domainMstrList);

    /**
     * 根据域ID批量插入域
     * @param domainMstrList 域实体类列表
     * @return 结果封装类
     */
    ResultUtil insertDomainInfoList(List<DomainMstr> domainMstrList);

    /**
     * 根据域ID批量更新域
     * @param domainMstrList 域实体类列表
     * @return 结果封装类
     */
    ResultUtil updateDomainInfoList(List<DomainMstr> domainMstrList);



    /**
     * 分页模糊查询域，
     * @param pageIndex 页码数
     * @param pageSize 分页大小
     * @param domainId 域对象实体类
     * @param criteriaList  排序列表
     * @return 结果封装类
     */





    /**
     * 导入Excel更新或添加域信息
     * @param workbook Excel文件
     * @return 结果封装类
     */
    ResultUtil batchDomainInfoInsertOrUpdate(Workbook workbook);


    /**
     *  导出域信息
     * @param domainDomain
     * @return
     */
   ResultUtil exportDomainInfo(String domainDomain);
    /**
     * 获取用户域信息
     * @param user
     * @return
     */
    ResultUtil getUserdomainInfo(String user);

    /**
     * 更新用户域
     * @param user 用户名
     * @param domainMstrList 域信息列表
     * @return 返回结果工具类
     */
    ResultUtil updateUserDomain(String user, List<DomainMstr> domainMstrList);

}
