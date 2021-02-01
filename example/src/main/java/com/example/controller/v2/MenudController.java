package com.example.controller.v2;

import com.example.entity.MenuEntity;
import com.example.entity.MenudDet;
import com.example.service.v2.MenudService;
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
 * 菜单标签管理
 */

@RestController("MenudV2Controller")
@RequestMapping("v2/menudManage")
public class MenudController {
    @Resource(name = "MenudV2Service")
    MenudService menudService;
    /**
     * 分页获取菜单标签信息
     * @author john.xiao
     * @date 2020-10-13 17:06
     */
    @GetMapping("/menud")
    public ResultUtil getMenudListPage(@RequestParam(value = "menudSelect",required = false,defaultValue = "")String menuSelect,
                                       @RequestParam(value = "menudCorp",required = false,defaultValue = "")String menuCorp,
                                       @RequestParam(value = "menudNbr",required = false,defaultValue = "")String menuNbr,
                                       @RequestParam(value = "menudLang",required = false,defaultValue = "")String menuLang,
                                       @RequestParam(value = "pageIndex",required = false,defaultValue = "1")int pageIndex,
                                       @RequestParam(value = "pageSize",required = false,defaultValue = "10")int pageSize,
                                       @RequestParam(value = "criteriaList",required = false,defaultValue = "[]")String criteriaList){
        JSONArray jsonArray=JSONArray.fromObject(criteriaList);
        //排序条件json转化列表
        JSONArray criteriaArray = JSONArray.fromObject(criteriaList);
        //如果有排序列表则以列表条件为优先
        Optional criteriaOptional;
        String sortParam;
        for (int i = 0; i < criteriaArray.size(); i++) {
            Map<String, Object> listMap = (Map<String, Object>) criteriaArray.get(i);
            criteriaOptional = Optional.ofNullable(listMap.get("criteria"));
            switch (criteriaOptional.orElse("").toString()) {
                case "menuCorp":
                    sortParam = "menud_corp";
                    break;
                case "menuNbr":
                    sortParam = "menud_nbr";
                    break;
                case "menuSelect":
                    sortParam = "menud_select";
                    break;
                case "menuLang":
                    sortParam = "menud_lang";
                    break;
                default:
                    sortParam = "menud_nbr";
            }
            listMap.put("criteria", sortParam);
        }
        return menudService.getMenudInfoList(pageIndex, pageSize,menuCorp,menuNbr,menuSelect,menuLang,criteriaArray);
    }
    /**
     * 添加菜单标签
     * @param menudDetList 菜单标签列表
     * @return 结果封装类
     * @author john.xiao
     * @date 2020-10-13 17:06
     */
    @PostMapping("/menud")
    public ResultUtil addMenudList(@RequestBody List<MenudDet> menudDetList){
        return menudService.insertMenudInfoList(menudDetList);
    }
    /**
     * 删除菜单标签
     * @param menudDetList 菜单实体类列表
     * @return 结果封装类
     * @author john.xiao
     * @date 2020-10-13 17:06
     */
    @DeleteMapping("/menud")
    public ResultUtil deleteMenudList(@RequestBody  List<MenudDet> menudDetList){
        return menudService.deleteMenudInfoList(menudDetList);
    }

    /**
     * 更新菜单信息标签
     * @param menudDetList 菜单实体类列表
     * @return 结果封装类
     * @author john.xiao
     * @date 2020-10-13 17:06
     */
    @PutMapping("/menud")
    public ResultUtil updateMenudList(@RequestBody  List<MenudDet> menudDetList){
        return menudService.updateMenudInfoList(menudDetList);
    }

    /**
     * 导入菜单标签信息
     *
     * @param file Excel文件
     * @return 结果封装类
     * @author
     * @date
     */
    @PostMapping("exportMenud")
    public ResultUtil exportMeund(MultipartFile file) {
        //初步处理Excel文件
        Workbook workbook = null;
        try {
            InputStream inputStream = file.getInputStream();
            workbook = WorkbookFactory.create(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return menudService.exportMenud(workbook);
    }

    /**
     * 导出菜单标签信息
     *
     * @param menudCorp
     * @param menudNbr
     * @param menudSelect
     * @param menudLang
     * @return 结果封装类
     * @author
     * @date
     */
    @GetMapping("deriveMenud")
    public void deriveCorp(@RequestParam("menudCorp") String menudCorp,
                                 @RequestParam("menudNbr") String menudNbr,
                                 @RequestParam("menudSelect") String menudSelect,
                                 @RequestParam("menudLang") String menudLang) {
         menudService.deriveMenud(menudCorp, menudNbr,menudSelect,menudLang);
    }

    /**
     * 获取导入模板
     *
     */
    @GetMapping("template")
    public void getTemplate(@RequestParam(value = "path",required = false,defaultValue = "D:/template/menud.xls")String path){
        FileUtils fileUtils=new FileUtils();
        fileUtils.downLoad(path);
    }
}

