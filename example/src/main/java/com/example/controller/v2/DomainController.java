package com.example.controller.v2;

import com.example.entity.DomainMstr;
import com.example.service.v2.DomainService;
import com.example.utils.FileUtils;
import com.example.utils.ResultUtil;
import net.sf.json.JSONArray;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController("DomainV2Controller")
@RequestMapping("v2/domainManager")
public class DomainController {
    @Resource(name = "DomainV2Service")
    DomainService domainService;

    @GetMapping("domainList")
    public ResultUtil getDomain(@RequestParam(value="pageIndex",required = false,defaultValue = "1")int pageIndex ,
                                @RequestParam(value="pageSize",required = false,defaultValue = "10")int pageSize,
                                @RequestParam(value="criteriaList",required = false,defaultValue ="[]" )String criteriaList,
                                @RequestParam(value ="domainDomain",required = false,defaultValue = "")String domainDomain){
        JSONArray criteriaArray = JSONArray.fromObject(criteriaList);
        String criteria;
        String sortParam;
        for (int i = 0; i < criteriaArray.size(); i++) {
            Map<String, Object> listMap = (Map<String, Object>) criteriaArray.get(i);
            criteria = listMap.get("criteria").toString();
            switch (criteria) {
                case "domainName":
                    sortParam = "domain_name";
                    break;
                case "domainCorp":
                    sortParam = "domain_corp";
                    break;
                case "domainSname":
                    sortParam = "domain_sname";
                    break;
                case "domainDb":
                    sortParam = "domain_db";
                    break;
                case "domainActive":
                    sortParam = "domain_active";
                    break;
                case "domainPropath":
                    sortParam = "domain_propath";
                    break;
                case "domainType":
                    sortParam = "domain_type";
                    break;
                case "maxDomain":
                    sortParam = "domain_max_domain";
                    break;
                case "domainAdmin":
                    sortParam = "domain_admin";
                    break;
                case "domainMaxUsers":
                    sortParam = "domain_max_users";
                    break;
                default:
                    sortParam = "domain_domain";
            }
            listMap.put("criteria", sortParam);
        }return domainService.getDomainInfoList(pageIndex,pageSize,domainDomain,criteriaArray);
    }
    /**
     * 批量插入信息
     * @param domainMstrList 用实体类列表
     * @return 结果封装类
     */
    @PostMapping("domainList")
    public ResultUtil insertDomainInfoList(@RequestBody List<DomainMstr> domainMstrList){
        return domainService.insertDomainInfoList(domainMstrList);
    }

    /**
     * 批量更新信息
     * @param domainMstrList 用实体类列表
     * @return 结果封装类
     */
    @PutMapping("domainList")
    public ResultUtil updateDomainInfoList(@RequestBody List<DomainMstr> domainMstrList){
        return domainService.updateDomainInfoList(domainMstrList);
    }

    /**
     * 导入Excel插入或更新
     * @param file Excel文件
     * @return 结果封装类
     */
    @PostMapping("domainExcel")
    public ResultUtil insertDomainInfoByExcel(MultipartFile file){
        //初步处理Excel文件
        Workbook workbook=null;
        try {
            InputStream inputStream=file.getInputStream();
            workbook= WorkbookFactory.create(inputStream);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return domainService.batchDomainInfoInsertOrUpdate(workbook);
    }

    /**
     * 批量删除
     * @param domainMstrList
     * @return
     */
    @DeleteMapping("domainList")
    public ResultUtil deleteDomainInfoList(@RequestBody List<DomainMstr> domainMstrList){
        return domainService.deleteDomainInfoList(domainMstrList);
    }
    /**
     * 导出信息
     * @param domain 用户Id
     * @return
     */
    @GetMapping("domainExcel")
    public ResultUtil getDomainInfoByExcel(
            @RequestParam(value="domainDomain",required = false,defaultValue = "")String domain){
     return domainService.exportDomainInfo(domain);
    }
    /**
     * 获取导入模板
     */
    @GetMapping("template")
    public void getTemplate(){
        String path="E:/template/domain.xls";
        FileUtils fileUtils=new FileUtils();
        fileUtils.downLoad(path);
    }
   /**
     * 获取用户域信息     
    * */
   @GetMapping("userDomain")
   public ResultUtil getUserdomainInfo(
           @RequestParam("user") String user) {
       return domainService. getUserdomainInfo(user);
   }

    /**
     * 更新用户域信息
     *
     * @param domainMstrList
     * @param user
     * @return
     */
    @PutMapping ("userDomain")
    public ResultUtil updateUserDomain(@RequestBody List<DomainMstr> domainMstrList,
                                       @RequestParam("user") String user) {
        return domainService.updateUserDomainList(domainMstrList,user);
    }
}
