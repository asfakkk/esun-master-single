package com.example.service.v2.impl;

import com.example.constant.MenuMessage;
import com.example.constant.Message;
import com.example.entity.MenuEntity;
import com.example.entity.RoleMstr;
import com.example.exception.CustomHttpException;
import com.example.service.feign.DbHelperService;
import com.example.service.v2.MenuService;
import com.example.utils.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * 菜单管理
 * @author test
 */
@Service("MenuV2Service")
public class MenuServiceImpl implements MenuService {

	public static final String CODE = "code";
	private static Logger logger = LoggerFactory.getLogger(MenuServiceImpl.class);
	public static final String SUCCESS_CODE = "10000";
	private static final String DATASOURCE_POSTGRES = "postgres";
	@Autowired
	@Lazy
	DbHelperService dbHelperService;

	/**
	 * 分页模糊查询
	 * 该接口不进行递归处理
	 *
	 * @param pageIndex    页码数
	 * @param pageSize     分页大小
	 * @param menuNo       菜单编号
	 * @param menuSelect   下级菜单编号
	 * @param criteriaList 排序列表
	 * @return 结果封装类
	 */
	@Override
	public ResultUtil getMenuInfoList(int pageIndex, int pageSize, String menuNo, String menuSelect, List<Map<String, Object>> criteriaList) {
		String sortString = getSortString(criteriaList);
		String sql = "select guid,menu_corp as \"menuCorp\" ,menu_nbr as \"menuNbr\" ,menu_select as \"menuSelect\" ," +
				"menu_program as \"menuProgram\" ,menu_name as \"menuName\", menu_mod_date as \"menuModDate\" " +
				"from menu_mstr where menu_nbr ilike '%25" + menuNo + "%25' and menu_select ilike '%25" + menuSelect + "%25' " +
				"order by " + sortString + ";";
		String message;
		ResultUtil result = dbHelperService.selectPage(sql, DATASOURCE_POSTGRES, pageIndex, pageSize);
		if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
			message = MessageUtil.getMessage(MenuMessage.MENU_GET_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		List<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) result.get("result");
		Map<String, Object> dataMap = new HashMap<>(2);
		//获取总条数
		int count = (int) result.get("count");
		dataMap.put("list", list);
		dataMap.put("count", count);
		message = MessageUtil.getMessage(MenuMessage.MENU_GET_SUCCESS.getCode());
		logger.info(message);
		return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(dataMap);
	}

	/**
	 * 查询信息
	 * 该接口将会对结果进行递归处理
	 *
	 * @param menuNo     菜单编号
	 * @param menuSelect 下级菜单号
	 * @return 结果封装类
	 */
	@Override
	public ResultUtil getMenuInfoList(String menuNo, String menuSelect) {
		String sql = "select guid,menu_corp as \"menuCorp\" ,menu_nbr as \"menuNbr\" ,menu_select as \"menuSelect\" ," +
				"menu_program as \"menuProgram\" ,menu_name as \"menuName\", menu_mod_date as \"menuModDate\" " +
				"from menu_mstr where menu_nbr ilike '%25" + menuNo + "%25' and menu_select ilike '%25" + menuSelect + "%25' ";
		String message;
		ResultUtil result = dbHelperService.select(sql, DATASOURCE_POSTGRES);
		if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
			message = MessageUtil.getMessage(MenuMessage.MENU_GET_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		List<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) result.get("result");
		list = treeMenu(list);
		Map<String, Object> dataMap = new HashMap<>(2);
		//获取总条数
		dataMap.put("list", list);
		message = MessageUtil.getMessage(MenuMessage.MENU_GET_SUCCESS.getCode());
		logger.info(message);
		return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(dataMap);
	}


	/**
	 * 处理排序列表
	 *
	 * @param criteriaList 排序列表
	 * @return 处理后的排序条件
	 */
	private String getSortString(List<?> criteriaList) {
		StringBuilder criteriaBuilder = new StringBuilder();
		if (criteriaList.size() > 0) {
			for (int i = 0; i < criteriaList.size(); i++) {
				Map<String, Object> listMap = (Map<String, Object>) criteriaList.get(i);
				Optional<Object> sort = Optional.ofNullable(listMap.get("sort"));
				Optional<Object> criteria = Optional.ofNullable(listMap.get("criteria"));
				criteriaBuilder.append(criteria.orElse("guid"));
				if (!"0".equals(sort.orElse("0"))) {
					criteriaBuilder.append(" desc");
				}
				criteriaBuilder.append(" ,");
			}
		} else {
			//设置默认排序项
			criteriaBuilder.append("guid");
		}
		return criteriaBuilder.toString();
	}

	/**
	 * 将菜单处理为树型结构
	 *
	 * @param list 菜单列表
	 * @return 菜单列表
	 */
	private List<HashMap<String, Object>> treeMenu(List<HashMap<String, Object>> list) {
		//最顶级菜单编号
		String highestMenuNo = "X";
		list = doTreeMenu(highestMenuNo, list);
		return list;
	}

	private List<HashMap<String, Object>> doTreeMenu(String menuNo, List<HashMap<String, Object>> list) {
		List<HashMap<String, Object>> resultList = new ArrayList<>(10);
		for (int i = 0; i < list.size(); i++) {
			HashMap<String, Object> stringObjectHashMap = list.get(i);
			String mapMenuNo = stringObjectHashMap.get("menuNbr").toString();
			String mapMenuSelect = stringObjectHashMap.get("menuSelect").toString();
			String childNo = "X".equals(menuNo) ? mapMenuSelect : mapMenuNo + "." + mapMenuSelect;
			if (menuNo.equals(mapMenuNo)) {
				stringObjectHashMap.put("children", doTreeMenu(childNo, list));
				resultList.add(stringObjectHashMap);
			}
		}
		return resultList;
	}

	/**
	 * 添加菜单
	 *
	 * @param menuEntity 菜单实体类
	 * @return 结果封装类
	 */
	@Override
	public ResultUtil insertMenuInfo(MenuEntity menuEntity) {
		String message;
		String GUID = GUIDUtils.create();
		boolean menuExist = isMenuExist(menuEntity.getMenuNbr(), menuEntity.getMenuSelect());
		if (menuExist) {
			message = MessageUtil.getMessage(MenuMessage.MENU_IS_EXIST.getCode());
			logger.warn(menuEntity.getMenuNbr() + "," + menuEntity.getMenuSelect() + "：" + message);
			return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		String sql = "insert into menu_mstr " +
				"(guid,menu_corp, menu_nbr, menu_select, menu_program, menu_name) " +
				"values('" + GUID + "','" + menuEntity.getMenuCorp() + "','" + menuEntity.getMenuNbr() + "','" + menuEntity.getMenuSelect() + "'," +
				"'" + menuEntity.getMenuProgram() + "','" + menuEntity.getMenuName() + "');";
		ResultUtil result = dbHelperService.insert(sql, DATASOURCE_POSTGRES);
		if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
			message = MessageUtil.getMessage(MenuMessage.MENU_ADD_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		message = MessageUtil.getMessage(MenuMessage.MENU_ADD_SUCCESS.getCode());
		return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName());
	}

	/**
	 * 更新菜单
	 *
	 * @param menuEntity 菜单实体类
	 * @return 结果封装类
	 */
	@Override
	public ResultUtil updateMenuInfo(MenuEntity menuEntity) {
		String message;
		boolean menuExist = isMenuExist(menuEntity.getMenuNbr(), menuEntity.getMenuSelect());
		if (!menuExist) {
			message = MessageUtil.getMessage(MenuMessage.MENU_NOT_EXIST.getCode());
			logger.warn(menuEntity.getMenuNbr() + "," + menuEntity.getMenuSelect() + "：" + message);
			return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		String sql = "update public.menu_mstr " +
				"set " +
				"menu_corp = '" + menuEntity.getMenuCorp() + " '," +
				"menu_nbr = '" + menuEntity.getMenuNbr() + "' ," +
				"menu_select = '" + menuEntity.getMenuSelect() + "'," +
				"menu_name = '" + menuEntity.getMenuName() + "'," +
				"menu_program ='" + menuEntity.getMenuProgram() + "' " +
				"where " +
				"guid='" + menuEntity.getGuid() + "'";
		ResultUtil result = dbHelperService.update(sql, DATASOURCE_POSTGRES);
		if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
			message = MessageUtil.getMessage(MenuMessage.MENU_UPDATE_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		message = MessageUtil.getMessage(MenuMessage.MENU_UPDATE_SUCCESS.getCode());
		return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName());
	}

	/**
	 * 删除菜单
	 * @param menuEntity 菜单实体类
	 * @return 结果封装类
	 */
	@Override
	public ResultUtil deleteMenuInfo(MenuEntity menuEntity) {
		String message;
		boolean menuExist = isMenuExist(menuEntity.getMenuNbr(), menuEntity.getMenuSelect());
		if (!menuExist) {
			message = MessageUtil.getMessage(MenuMessage.MENU_NOT_EXIST.getCode());
			logger.warn(menuEntity.getMenuNbr() + "," + menuEntity.getMenuSelect() + "：" + message);
			return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		String sql = "delete from public.menu_mstr " +
				"where " +
				"guid='" + menuEntity.getGuid() + "'";
		ResultUtil result = dbHelperService.delete(sql, DATASOURCE_POSTGRES);
		if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
			message = MessageUtil.getMessage(MenuMessage.MENU_DELETE_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		message = MessageUtil.getMessage(MenuMessage.MENU_DELETE_SUCCESS.getCode());
		return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName());

	}

	/**
	 * 批量更新菜单
	 *
	 * @param menuEntityList 菜单实体类列表
	 * @return 结果封装类
	 */
	@Override
	public ResultUtil updateMenuInfoList(List<MenuEntity> menuEntityList) {
		List<Map<String, Object>> resultList = new ArrayList<>(menuEntityList.size());
		String message;
		for (int i = 0; i < menuEntityList.size(); i++) {
			ResultUtil result = updateMenuInfo(menuEntityList.get(i));
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(menuEntityList.get(i).getMenuName(), result);
			resultList.add(resultMap);
		}
		message = MessageUtil.getMessage(MenuMessage.MENU_UPDATE_SUCCESS.getCode());
		return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(resultList);
	}

	/**
	 * 批量删除菜单信息
	 *
	 * @param menuEntityList 实体类列表
	 * @return 结果封装类
	 */
	@Override
	public ResultUtil deleteMenuInfoList(List<MenuEntity> menuEntityList) {
		List<Map<String, Object>> resultList = new ArrayList<>(menuEntityList.size());
		String message;
		for (int i = 0; i < menuEntityList.size(); i++) {
			ResultUtil result = deleteMenuInfo(menuEntityList.get(i));
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put(menuEntityList.get(i).getMenuName(), result);
			resultList.add(resultMap);
		}
		message = MessageUtil.getMessage(MenuMessage.MENU_DELETE_SUCCESS.getCode());
		return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(resultList);
	}

	/**
	 * 批量插入菜单信息
	 *
	 * @param menuEntityList 实体类列表
	 * @return 结果封装类
	 */
	@Override
	public ResultUtil insertMenuInfoList(List<MenuEntity> menuEntityList) {
		List<Map<String, Object>> resultList = new ArrayList<>(menuEntityList.size());
		String message;
		for (int i = 0; i < menuEntityList.size(); i++) {
			ResultUtil result = insertMenuInfo(menuEntityList.get(i));
			Map<String, Object> resultMap = new HashMap<>(2);
			resultMap.put(menuEntityList.get(i).getMenuName(), result);
			resultList.add(resultMap);
		}
		message = MessageUtil.getMessage(MenuMessage.MENU_ADD_SUCCESS.getCode());
		return  ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(resultList);
	}

	/**
	 * 导入菜单信息
	 *
	 * @param workbook Excel文件
	 * @return 结果封装类
	 */
	@Override
	public ResultUtil batchMenuInfoInsertOrUpdate(Workbook workbook) {
		String defaultPassword = "123456";
		//获取Excel文档第一个表格
		Sheet sheet = workbook.getSheetAt(0);
		//获取表格标题列表
		List titleList = PoiUtils.getTitleList(PoiUtils.getRow(sheet, 0));
		//请求结果列表
		List<Map<String, Object>> resultList = new ArrayList<>();
		Map<String, Object> menuInfo;
		Optional<Object> menuCorp;
		Optional<Object> menuNbr;
		Optional<Object> menuSelect;
		Optional<Object> menuProgram;
		Optional<Object> menuName;
		//循环遍历用户信息写入列表
		for (int i = 1; i <= sheet.getLastRowNum(); i++) {
			//获取相应行的数据，转换为list
			menuInfo = PoiUtils.getRowData(PoiUtils.getRow(sheet, i), titleList);
			menuCorp = Optional.ofNullable(menuInfo.get("menuCorp"));
			menuNbr = Optional.ofNullable(menuInfo.get("menuNbr"));
			menuSelect = Optional.ofNullable(menuInfo.get("menuSelect"));
			menuProgram = Optional.ofNullable(menuInfo.get("menuProgram"));
			menuName = Optional.ofNullable(menuInfo.get("menuName"));

			//实体类赋值
			MenuEntity menuEntity = new MenuEntity();
			menuEntity.setMenuCorp(menuCorp.orElse("").toString());
			menuEntity.setMenuNbr(menuNbr.orElse("").toString());
			menuEntity.setMenuSelect(menuSelect.orElse("CH").toString());
			menuEntity.setMenuProgram(menuProgram.orElse("").toString());
			menuEntity.setMenuName(menuName.orElse("").toString());

			//用户运行结果Map
			Map<String, Object> resultMap = new HashMap<>(2);
			boolean menuExist = isMenuExist(menuEntity.getMenuNbr(), menuEntity.getMenuSelect());
			//查看该用户是否存在
			if (!menuExist) {
				ResultUtil insertResult = insertMenuInfo(menuEntity);
				resultMap.put(menuEntity.getMenuNbr() + " " + menuEntity.getMenuSelect(), insertResult);
			} else {
				ResultUtil updateResult = updateMenuInfo(menuEntity);
				resultMap.put(menuEntity.getMenuNbr() + " " + menuEntity.getMenuSelect(), updateResult);
			}
			resultList.add(resultMap);
		}
		return ResultUtil.ok().setData(resultList);
	}

	/**
	 * 导出菜单信息
	 *
	 * @param menuNo     菜单编号
	 * @param menuSelect 下级菜单号
	 */
	@Override
	public void exportUserInfo(String menuNo, String menuSelect) {
		String sql = "select guid,menu_corp as \"menuCorp\" ,menu_nbr as \"menuNbr\" ,menu_select as \"menuSelect\" ," +
				"menu_program as \"menuProgram\" ,menu_name as \"menuName\", menu_mod_date as \"menuModDate\" " +
				"from menu_mstr where menu_nbr ilike '%25" + menuNo + "%25' and menu_select ilke '%25" + menuSelect + "%25' ";
		String message;
		ResultUtil result = dbHelperService.select(sql, DATASOURCE_POSTGRES);
		if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
			message = MessageUtil.getMessage(Message.USER_INFO_GET_ERROR.getCode());
			logger.error(message);
		}
		ArrayList list = (ArrayList) result.get("result");
		//判断用户是否存在
		if (list.size() == 0) {
			message = MessageUtil.getMessage(Message.USER_NOT_EXIST.getCode());
			logger.error(message);
		}
		List<String> titleList = new ArrayList<>();
		titleList.add("menuCorp");
		titleList.add("menuNbr");
		titleList.add("menuSelect");
		titleList.add("menuProgram");
		titleList.add("menuName");
		String diskPath = "E:/test/";
		String path = ExcelUtils.createMapListExcel(list, diskPath, titleList);
		FileUtils fileUtils = new FileUtils();
		fileUtils.downLoad(path);
	}

	/**
	 * 判断菜单是否存在
	 *
	 * @param menuNo     菜单编号
	 * @param menuSelect 下级菜单编号
	 * @return 结果封装类
	 */
	private boolean isMenuExist(String menuNo, String menuSelect) {
		String message;
		String sql = "select 1 from  menu_mstr where  menu_nbr = '" + menuNo + "' and  menu_select = '" + menuSelect + "';";
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
	 * 查询角色菜单信息
	 *
	 * @param roleName 角色名
	 * @return 结果封装类
	 */
	@Override
	public ResultUtil getRoleMenuInfoList(String roleName) {
		String sql = "select distinct guid,menu_corp as \"menuCorp\",menud_nbr as \"menuNbr\",menu_select as \"menuSelect\",menu_program as \"menuProgram\",menu_name as \"menuName\" " +
				"    from menu_mstr ms\n" +
				"    left join menud_det md on ms.menu_nbr = md.menud_nbr and ms.menu_select = md.menud_select\n" +
				"    left join roled_det rd on ms.menu_nbr = rd.roled_nbr and ms.menu_select = rd.roled_select\n" +
				"where lower(rd.roled_role) = lower('" + roleName + "')";
		String message;
		ResultUtil result = dbHelperService.select(sql, DATASOURCE_POSTGRES);
		if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
			message = MessageUtil.getMessage(MenuMessage.MENU_GET_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		List<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) result.get("result");
		list = treeMenu(list);
		Map<String, Object> dataMap = new HashMap<>(2);
		//获取总条数
		dataMap.put("list", list);
		message = MessageUtil.getMessage(MenuMessage.MENU_GET_SUCCESS.getCode());
		logger.info(message);
		return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(dataMap);
	}



	/**
	 * 查询用户菜单信息
	 *
	 * @param userUserId 用户名
	 * @return 结果封装类
	 */
	@Override
	public ResultUtil getUserMenuInfoList(String userUserId) {
		String sql = "select distinct menud_nbr as \"menuNbr\",menu_select as \"menuSelect\",menu_program as \"menuProgram\",pgrm_url as \"programUrl\",menud_label as \"menuLabel\",menu_name as \"menuName\" from menu_mstr ms\n" +
				"    left join menud_det md on ms.menu_nbr = md.menud_nbr and ms.menu_select = md.menud_select\n" +
				"    left join roled_det rd on ms.menu_nbr = rd.roled_nbr and ms.menu_select = rd.roled_select\n" +
				"where  rd.roled_role in (\n" +
				"    select ud.userd_role from  userd_det ud\n" +
				"    where  lower(ud.userd_userid ) = lower('" + userUserId + "')\n" +
				"    )";
		String message;
		ResultUtil result = dbHelperService.select(sql, DATASOURCE_POSTGRES);
		if (!SUCCESS_CODE.equals(result.get(CODE).toString())) {
			message = MessageUtil.getMessage(MenuMessage.MENU_GET_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message, Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		List<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>) result.get("result");
		list = treeMenu(list);
		Map<String, Object> dataMap = new HashMap<>(2);
		//获取总条数
		dataMap.put("list", list);
		message = MessageUtil.getMessage(MenuMessage.MENU_GET_SUCCESS.getCode());
		logger.info(message);
		return ResultUtil.ok(message, Thread.currentThread().getStackTrace()[1].getMethodName()).setData(dataMap);
	}

	/**
	 * 更新角色菜单列表
	 *
	 * @param menuEntity 菜单列表
	 * @param roleName  角色名
	 * @return
	 */
	@Override
	public ResultUtil updateRoleMenuInfo(MenuEntity menuEntity, String roleName) {
		String message;
		boolean roleExist=isMenuExist(menuEntity.getMenuNbr(),menuEntity.getMenuSelect());
		if(!roleExist){
			message=MessageUtil.getMessage(MenuMessage.MENU_NOT_EXIST.getCode());
			logger.warn(menuEntity.getMenuNbr()+menuEntity.getMenuSelect()+"："+message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		String addSql = "insert into roled_det(roled_role,roled_nbr,roled_select) values  ('"+roleName+"','"+menuEntity.getMenuNbr()+"','"+menuEntity.getMenuSelect()+"') ";
		ResultUtil resultadd=dbHelperService.update(addSql,DATASOURCE_POSTGRES);
		if(!SUCCESS_CODE.equals(resultadd.get(CODE).toString())){
			message=MessageUtil.getMessage(MenuMessage.MENU_UPDATE_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		message=MessageUtil.getMessage(MenuMessage.MENU_UPDATE_SUCCESS.getCode());
		return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName());
	}

	/**
	 * 批量更新角色菜单
	 * @param menuEntityList
	 * @param roleName
	 * @return
	 * @author john.xiao
	 * @date 2020-10-13 17:06
	 */

    @Override
    public ResultUtil updateRoleMenuInfoList(List<MenuEntity> menuEntityList, String roleName) {
		List<Map<String,Object>> resultList=new ArrayList<>(menuEntityList.size());
		String message;
		String deleteSql = "delete from roled_det where roled_role = '"+roleName+"'";
		ResultUtil resultupdate=dbHelperService.update(deleteSql,DATASOURCE_POSTGRES);
		if(!SUCCESS_CODE.equals(resultupdate.get(CODE).toString())){
			message=MessageUtil.getMessage(MenuMessage.MENU_UPDATE_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}

		for (int i = 0; i < menuEntityList.size(); i++) {
			ResultUtil result=updateRoleMenuInfo(menuEntityList.get(i),roleName);
			Map<String,Object> resultMap=new HashMap<>();
			resultMap.put(menuEntityList.get(i).getMenuNbr(),result);
			resultMap.put(menuEntityList.get(i).getMenuSelect(),result);
			resultList.add(resultMap);
		}
		message=MessageUtil.getMessage(MenuMessage.MENU_UPDATE_SUCCESS.getCode());
		return  ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(resultList);
	}

}