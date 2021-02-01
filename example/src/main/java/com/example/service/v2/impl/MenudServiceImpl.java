package com.example.service.v2.impl;

import com.example.constant.MenuMessage;
import com.example.constant.Message;
import com.example.entity.CorpMstr;
import com.example.entity.MenudDet;
import com.example.exception.CustomHttpException;
import com.example.service.feign.DbHelperService;
import com.example.service.v2.MenudService;
import com.example.utils.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 菜单标签管理
 */
@Service("MenudV2Service")
public class MenudServiceImpl implements MenudService {
    public static final String CODE = "code";
    private static Logger logger = LoggerFactory.getLogger(MenudServiceImpl.class);
    public static final String SUCCESS_CODE = "10000";
    private static final String DATASOURCE_POSTGRES = "postgres";
    @Value("${file.disk.path}")
    String fileDiskPath;
    @Autowired
    @Lazy
    DbHelperService dbHelperService;

    /**
     * 分页模糊查询
     *
     * @param pageIndex    页码数
     * @param pageSize     分页大小
     * @param menuNbr     菜单编号
     * @param menuSelect  下级菜单
     * @param menuCorp
     * @param menuLang
     * @param criteriaList 排序列表
     * @return 结果封装类
     */
    @Override
    public ResultUtil getMenudInfoList(int pageIndex, int pageSize, String menuCorp, String menuNbr, String menuSelect, String menuLang, List<Map<String, Object>> criteriaList) {
        String sortString = getSortString(criteriaList);
        String sql = "select" +
                "    md.menud_corp as \"menuCorp\", md.menud_nbr as \"menuNbr\", md.menud_select as \"menuSelect\", md.menud_lang as \"menuLang\", md.menud_label as \"menuLabel\",mm.menu_name as \"menuName\" " +
                "    from menud_det md"+
                "    left join menu_mstr mm on md.menud_nbr = mm.menu_nbr and md.menud_select = mm.menu_select" +
                "    where  menu_nbr ilike '%25" + menuNbr + "%25' and menud_select ilike '%25" + menuSelect + "%25' and menud_corp ilike '%25" + menuCorp + "%25' and menud_lang ilike '%25" + menuLang + "%25' " +
                "order by " + sortString + ";";
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
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(dataMap);
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
                criteriaBuilder.append(criteria.orElse("menudNbr"));
                if (!"0".equals(sort.orElse("0"))) {
                    criteriaBuilder.append(" desc");
                }
                criteriaBuilder.append(" ,");
            }
        } else {
            criteriaBuilder.append("menud_nbr,");
        }
        criteriaBuilder.setLength(criteriaBuilder.length() - 1);
        return criteriaBuilder.toString();
    }


    /**
     * 单条添加菜单标签
     *
     * @param menudDet 菜单标签实体类
     * @return 结果封装类
     */
    @Override
    public ResultUtil insertMenudInfo(MenudDet menudDet) {
        String message;
        boolean menudExist = isMenudExist(menudDet.getMenuCorp(), menudDet.getMenuNbr(), menudDet.getMenuSelect(), menudDet.getMenuLang());
        if (menudExist) {
            message = MessageUtil.getMessage(Message.DELIVERY_IS_EXIST.getCode());
            logger.warn(menudDet.getMenuCorp() + "," + menudDet.getMenuNbr() + "," + menudDet.getMenuSelect() + "," + menudDet.getMenuLang() + "：" + message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String sql = "insert into menud_det " +
                "(menud_corp,menud_nbr, menud_select, menud_lang, menud_label) " +
                "values('" + menudDet.getMenuCorp() + "','" + menudDet.getMenuNbr() + "','" + menudDet.getMenuSelect() + "','" +
                menudDet.getMenuLang() + "','" + menudDet.getMenuLabel() + "');";
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
     * 批量添加菜单标签
     *
     * @param menudDetList 菜单标签实体类
     * @return 结果封装类
     */
    @Override
    public ResultUtil insertMenudInfoList(List<MenudDet> menudDetList) {
        String message;
        for (MenudDet menudDet : menudDetList) {
            ResultUtil result = insertMenudInfo(menudDet);
            menudDet.setResult(result.get("msg").toString());
            menudDet.setCode(result.get("code").toString());
        }
        message = MessageUtil.getMessage(Message.DELIVERY_ADD_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(menudDetList);
    }

    /**
     * 删除单条信息
     *
     * @param menudDet
     * @return 结果封装类
     */
    @Override
    public ResultUtil deleteMenudInfo(MenudDet menudDet) {
        String message;
        boolean menudExist = isMenudExist(menudDet.getMenuCorp(), menudDet.getMenuNbr(), menudDet.getMenuSelect(), menudDet.getMenuLang());
        if (!menudExist) {
            message = MessageUtil.getMessage(Message.DELIVERY_NOT_EXIST.getCode());
            logger.warn(menudDet.getMenuCorp() + "," + menudDet.getMenuNbr() + "," + menudDet.getMenuSelect() + "," + menudDet.getMenuLang() + "：" + message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String sql = "delete from public.menud_det " +
                "where " +
                "menud_corp='" + menudDet.getMenuCorp() + "' and menud_nbr='" + menudDet.getMenuNbr() + "' and menud_select='" + menudDet.getMenuSelect() + "'and menud_lang='" + menudDet.getMenuLang() + "'";
        ResultUtil result = dbHelperService.insert(sql, DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(Message.DELIVERY_DELETE_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message = MessageUtil.getMessage(Message.DELIVERY_DELETE_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName());

    }

    /**
     * 批量删除
     *
     * @param menudDetList 实体类列表
     * @return 结果封装类
     */
    @Override
    public ResultUtil deleteMenudInfoList(List<MenudDet> menudDetList) {
        String message;
        for (MenudDet menudDet : menudDetList) {
            ResultUtil result = deleteMenudInfo(menudDet);
            menudDet.setResult(result.get("msg").toString());
            menudDet.setCode(result.get("code").toString());
        }
        message = MessageUtil.getMessage(Message.DELIVERY_DELETE_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(menudDetList);
    }

    /**
     * 更新单条信息
     *
     * @param menudDet
     * @return 结果封装类
     */
    @Override
    public ResultUtil updateMenudInfo(MenudDet menudDet) {
        String message;
        boolean menudExist = isMenudExist(menudDet.getMenuCorp(), menudDet.getMenuNbr(), menudDet.getMenuSelect(), menudDet.getMenuLang());
        if (!menudExist) {
            message = MessageUtil.getMessage(Message.DELIVERY_NOT_EXIST.getCode());
            logger.warn(menudDet.getMenuCorp() + "," + menudDet.getMenuNbr() + "," + menudDet.getMenuSelect() + "," + menudDet.getMenuLang() + "：" + message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        String sql = "update menud_det set menud_label= '" + menudDet.getMenuLabel() + "' " +
                "where " +
                "menud_corp='" + menudDet.getMenuCorp() + "' and menud_nbr='" + menudDet.getMenuNbr() + "' and menud_select='" + menudDet.getMenuSelect() + "'and menud_lang='" + menudDet.getMenuLang() + "'";
        ResultUtil result = dbHelperService.insert(sql, DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(Message.DELIVERY_UPDATE_ERROR.getCode());
            logger.error(message);
            return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
        }
        message = MessageUtil.getMessage(Message.DELIVERY_UPDATE_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName());

    }


    /**
     * 批量更新
     *
     * @param menudDetList 实体类列表
     * @return 结果封装类
     */
    @Override
    public ResultUtil updateMenudInfoList(List<MenudDet> menudDetList) {
        String message;
        for (MenudDet menudDet : menudDetList) {
            ResultUtil result = updateMenudInfo(menudDet);
            menudDet.setResult(result.get("msg").toString());
            menudDet.setCode(result.get("code").toString());
        }
        message = MessageUtil.getMessage(Message.DELIVERY_UPDATE_SUCCESS.getCode());
        return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(menudDetList);
    }


    /**
     * 导入菜单标签信息
     *
     * @param workbook
     * @return 结果封装类
     * @author
     * @date
     */
    @Override
    public ResultUtil exportMenud(Workbook workbook) {
        String defaultPassword = "123456";
        Sheet sheet = workbook.getSheetAt(0);
        //获取表格标题列表
        List titleList = PoiUtils.getTitleList(PoiUtils.getRow(sheet, 0));
        //请求结果列表
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> menudInfo;

        Optional<Object> menudCorp;
        Optional<Object> menudNbr;
        Optional<Object> menudSelect;
        Optional<Object> menudLang;
        Optional<Object> menudLabel;
        //循环遍历用户信息写入列表
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            //获取相应行的数据，转换为list
            menudInfo = PoiUtils.getRowData(PoiUtils.getRow(sheet, i), titleList);
            menudCorp = Optional.ofNullable(menudInfo.get("menudCorp"));
            menudNbr = Optional.ofNullable(menudInfo.get("menudNbr"));
            menudSelect = Optional.ofNullable(menudInfo.get("menudSelect"));
            menudLang = Optional.ofNullable(menudInfo.get("menudLang"));
            menudLabel = Optional.ofNullable(menudInfo.get("menudLabel"));
            //实体类赋值
            MenudDet menudDet = new MenudDet();
            menudDet.setMenuCorp(menudCorp.orElse("").toString());
            menudDet.setMenuNbr(menudNbr.orElse("").toString());
            menudDet.setMenuSelect(menudSelect.orElse("").toString());
            menudDet.setMenuLang(menudLang.orElse("").toString());
            menudDet.setMenuLabel(menudLabel.orElse("").toString());
            //菜单标签运行结果Map
            Map<String, Object> resultMap = new HashMap<>(2);
            //查看该菜单标签是否存在
            if (!isMenudExist(menudDet.getMenuCorp(), menudDet.getMenuNbr(), menudDet.getMenuSelect(), menudDet.getMenuLang())) {
                ResultUtil insertResult = insertMenudInfo(menudDet);
                resultMap.put(menudDet.getMenuCorp() + " " + menudDet.getMenuNbr() + " " + menudDet.getMenuSelect() + " " + menudDet.getMenuLang(), insertResult);
            } else {
                ResultUtil updateResult = updateMenudInfo(menudDet);
                resultMap.put(menudDet.getMenuCorp() + " " + menudDet.getMenuNbr() + " " + menudDet.getMenuSelect() + " " + menudDet.getMenuLang(), updateResult);
            }
            resultList.add(resultMap);
        }
        return ResultUtil.ok().setData(resultList);
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
    @Override
    public void deriveMenud(String menudCorp, String menudNbr, String menudSelect, String menudLang) {
        String sql = "select" +
                " menud_corp as \"menudCorp\", menud_nbr as \"menudNbr\", menud_select as \"menudSelect\", menud_lang as \"menudLang\", menud_label as \"menudLabel\" " +
                "from menud_det where menud_nbr ilike '%25" + menudNbr + "%25' and menud_select ilike '%25" + menudSelect + "%25' and menud_corp ilike '%25" + menudCorp + "%25' and menud_lang ilike '%25" + menudLang + "%25' " + ";";
        String message;
        ResultUtil result = dbHelperService.select(sql, DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(Message.QUERY_ERROR.getCode());
            logger.error(message);
        }
        ArrayList list = (ArrayList) result.get("result");
        //判断导出记录是否存在
        if (list.size() == 0) {
            message = MessageUtil.getMessage(Message.QUERY_ERROR.getCode());
            logger.error(message);
        }
        List<String> titleList = new ArrayList<>();
        titleList.add("menudCorp");
        titleList.add("menudNbr");
        titleList.add("menudSelect");
        titleList.add("menudLang");
        titleList.add("menudLabel");
        String path = ExcelUtils.createMapListExcel(list, fileDiskPath, titleList);
        FileUtils fileUtils = new FileUtils();
        fileUtils.downLoad(path);

    }
    /**
     * 判断菜单标签是否存在
     *
     * @param menudCorp
     * @param menudNbr    菜单编号
     * @param menudSelect 下级菜单编号
     * @param menudLang
     * @return 结果封装类
     */
    private boolean isMenudExist(String menudCorp, String menudNbr, String menudSelect, String menudLang) {
        String message;
        String sql = "select 1 from  menud_det where  menud_corp = '" + menudCorp + "' and  menud_nbr = '" + menudNbr + "' and  menud_select = '" + menudSelect + "' and  menud_lang = '" + menudLang + "';";
        ResultUtil result = dbHelperService.select(sql, DATASOURCE_POSTGRES);
        if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
            message = MessageUtil.getMessage(MenuMessage.QUERY_ERROR.getCode());
            logger.error(message);
            throw new CustomHttpException(message);
        }
        ArrayList<HashMap> list = (ArrayList) result.get("result");
        return list.size() > 0;
    }

}