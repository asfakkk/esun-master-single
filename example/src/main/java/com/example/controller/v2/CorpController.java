package com.example.controller.v2;

import com.example.entity.CorpMstr;
import com.example.service.v2.CorpService;
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

/**
 * 公司管理控制器
 * @author
 * @date
 */
@RestController("CorpV2Controller")
@RequestMapping("/v2/corpManage")
public class CorpController {
    @Resource(name = "CorpV2Service")
    CorpService corpService;

    /**
     * 分页获取公司信息
     *
     * @param pageIndex    页码数o
     * @param pageSize     分页大小
     * @param criteriaList 排序列表
     * @param corp        公司名
     * @return 结果封装类
     */
    @GetMapping("corp")
    public ResultUtil getCorp(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
                              @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
                              @RequestParam(value = "criteriaList", required = false, defaultValue = "[]") String criteriaList,
                              @RequestParam(value = "corp", required = false, defaultValue = "") String corp) {
        //排序条件json转化列表
        JSONArray criteriaArray = JSONArray.fromObject(criteriaList);
        //如果有排序列表则以列表条件为优先
        Optional criteriaOptional;
        String sortParam;
        for (int i = 0; i < criteriaArray.size(); i++) {
            Map<String, Object> listMap = (Map<String, Object>) criteriaArray.get(i);
            criteriaOptional = Optional.ofNullable(listMap.get("criteria"));
            switch (criteriaOptional.orElse("").toString()) {
                case "corpName":
                    sortParam = "corp_name";
                    break;
                case "corp":
                    sortParam = "corp_id";
                    break;
                case "shortName":
                    sortParam = "corp_sname";
                    break;
                case "dataBase":
                    sortParam = "corp_db";
                    break;
                case "active":
                    sortParam = "corp_active";
                    break;
                case "corpProPath":
                    sortParam = "corp_propath";
                    break;
                case "corpType":
                    sortParam = "corp_type";
                    break;
                case "maxUsers":
                    sortParam = "corp_max_users";
                    break;
                case "corpAdmin":
                    sortParam = "corp_admin";
                    break;
                default:
                    sortParam = "corp_id";
            }
            listMap.put("criteria", sortParam);
        }
        return corpService.getCorpInfoList(pageIndex, pageSize,corp,criteriaArray);
    }

    /**
    * 添加公司信息
    * @param corpMstrList 公司信息列表
    * @Return 结果封装类
    * @author
     * @date
     */
    @PostMapping("corp")
    public ResultUtil addCorp(@RequestBody List<CorpMstr> corpMstrList) {
        return corpService.insertCorpInfoList(corpMstrList);
    }

    /**
     * 删除公司信息
     *
     * @param corpMstrList 公司信息列表
     * @return 结果封装类
     * @author
     * @date
     */
    @DeleteMapping("corp")
    public ResultUtil deleteCorp(@RequestBody List<CorpMstr> corpMstrList) {
        return corpService.deleteCorpInfolist(corpMstrList);
    }

    /**
     * 更新公司信息
     *
     * @param corpMstrList 公司信息列表
     * @return 结果封装类
     * @author
     * @date
     */
    @PutMapping("corp")
    public ResultUtil updateCorp(@RequestBody List<CorpMstr> corpMstrList) {
        return corpService.updateCorpInfolist(corpMstrList);
    }

    /**
     * 导入公司信息
     *
     * @param file Excel文件
     * @return 结果封装类
     * @author
     * @date
     */
    @PostMapping("exportCorp")
    public ResultUtil exportCorp(MultipartFile file) {
        //初步处理Excel文件
        Workbook workbook = null;
        try {
            InputStream inputStream = file.getInputStream();
            workbook = WorkbookFactory.create(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return corpService.exportCorp(workbook);
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
    @GetMapping("deriveCorp")
    public ResultUtil deriveCorp(@RequestParam(value = "isDelete", required = false, defaultValue = "false") boolean isDelete,
                                 @RequestParam(value = "corp", required = false, defaultValue = "") String corp) {
        return corpService.deriveCorp(corp, isDelete);
    }

    /**
     * 获取导入模板
     *
     */
    @GetMapping("template")
        public void getTemplate(@RequestParam(value = "path",required = false,defaultValue = "D:/template/domain.xls")String path){
        FileUtils fileUtils=new FileUtils();
        fileUtils.downLoad(path);
      }
}
