package com.example.service.v2;

import com.example.entity.MenuEntity;
import com.example.entity.MenudDet;
import com.example.utils.ResultUtil;
import net.sf.json.JSONArray;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

public interface MenudService {
    /**
     * 分页模糊查询
     * @param pageIndex 页码数
     * @param pageSize 分页大小
     * @param menuNbr 菜单编号
     * @param menuSelect 下级菜单
     * @param menuCorp
     * @param menuLang
     * @param criteriaList  排序列表
     * @return 结果封装类
     */
    ResultUtil getMenudInfoList(int pageIndex, int pageSize, String menuCorp, String menuNbr, String menuSelect, String menuLang, List<Map<String, Object>> criteriaList);

    /**
     * 插入单条信息
     * @param menudDet
     * @return 结果封装类
     */
    ResultUtil insertMenudInfo(MenudDet menudDet);

    /**
     * 批量插入
     * @param menudDetList 实体类列表
     * @return 结果封装类
     */
    ResultUtil insertMenudInfoList(List<MenudDet> menudDetList);
    /**
     * 删除单条信息
     * @param menudDet
     * @return 结果封装类
     */
    ResultUtil deleteMenudInfo(MenudDet menudDet);
    /**
     * 批量删除
     * @param menudDetList 实体类列表
     * @return 结果封装类
     */
    ResultUtil deleteMenudInfoList(List<MenudDet> menudDetList);
    /**
     * 更新单条信息
     * @param menudDet
     * @return 结果封装类
     */
    ResultUtil updateMenudInfo(MenudDet menudDet);
    /**
     * 批量更新
     * @param menudDetList 实体类列表
     * @return 结果封装类
     */
    ResultUtil updateMenudInfoList(List<MenudDet> menudDetList);
    /**
     * 导入菜单标签信息
     *
     * @param workbook
     * @return 结果封装类
     * @author
     * @date
     */
    ResultUtil exportMenud(Workbook workbook);
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
    void deriveMenud(String menudCorp, String menudNbr, String menudSelect, String menudLang);
}
