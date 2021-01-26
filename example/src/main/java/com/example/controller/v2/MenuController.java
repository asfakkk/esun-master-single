package com.example.controller.v2;

import com.example.entity.MenuEntity;
import com.example.service.v2.MenuService;
import com.example.utils.ResultUtil;
import net.sf.json.JSONArray;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;


/**
 * 菜单接口控制器V2版本
 * 提供对菜单信息的的查询，修改，插入及删除
 * 提供查询角色所拥有的菜单，及给角色分配菜单接口
 * 提供查询用户所拥有的菜单
 * @author john.xiao
 */
@RequestMapping("v2/menuManage")
@RestController("MenuV2Controller")
public class MenuController {
	@Resource(name = "MenuV2Service")
	MenuService menuService;

	/**
	 * 分页获取菜单信息
	 * @author john.xiao
	 * @date 2020-10-13 17:06
	 */
	@GetMapping("/menuList/page")
	public ResultUtil getMenuListPage(@RequestParam(value = "menuSelect",required = false,defaultValue = "")String menuSelect,
	                              @RequestParam(value = "menuNbr",required = false,defaultValue = "")String menuNbr,
	                              @RequestParam(value = "pageIndex",required = false,defaultValue = "1")int pageIndex,
	                              @RequestParam(value = "pageSize",required = false,defaultValue = "10")int pageSize,
	                              @RequestParam(value = "criteriaList",required = false,defaultValue = "[]")String criteriaList){
		JSONArray jsonArray=JSONArray.fromObject(criteriaList);
		return menuService.getMenuInfoList(pageIndex,pageSize,menuNbr,menuSelect,jsonArray);
	}

	/**
	 * 获取全部菜单
	 * 该接口会对返回信息进行递归处理
	 * @author john.xiao
	 * @date 2020-10-13 17:06
	 */
	@GetMapping("/menuList")
	public ResultUtil getMenuList(@RequestParam(value = "menuSelect",required = false,defaultValue = "")String menuSelect,
	                              @RequestParam(value = "menuNbr",required = false,defaultValue = "")String menuNbr){
		return menuService.getMenuInfoList(menuNbr,menuSelect);
	}


	/**
	 * 添加菜单
	 * @param menuEntityList 菜单实体类列表
	 * @return 结果封装类
	 * @author john.xiao
	 * @date 2020-10-13 17:06
	 */
	@PostMapping("/menuList")
	public ResultUtil addMenuList(@RequestBody List<MenuEntity> menuEntityList){
		ResultUtil resultUtil=menuService.insertMenuInfoList(menuEntityList);
		return resultUtil;
	}

	/**
	 * 删除菜单
	 * @param menuEntityList 菜单实体类列表
	 * @return 结果封装类
	 * @author john.xiao
	 * @date 2020-10-13 17:06
	 */
	@DeleteMapping("/menuList")
	public ResultUtil deleteMenuList(@RequestBody  List<MenuEntity> menuEntityList){
		return menuService.deleteMenuInfoList(menuEntityList);
	}

	/**
	 * 更新菜单信息
	 * @param menuEntityList 菜单实体类列表
	 * @return 结果封装类
	 * @author john.xiao
	 * @date 2020-10-13 17:06
	 */
	@PutMapping("/menuList")
	public ResultUtil updateMenuList(@RequestBody  List<MenuEntity> menuEntityList){
		return menuService.updateMenuInfoList(menuEntityList);
	}

	/**
	 * 导入菜单信息
	 * @param file Excel文件
	 * @return 结果封装类
	 */
	@PostMapping("/menuExcel")
	public ResultUtil exportMenuList(MultipartFile file){
		//初步处理Excel文件
		Workbook workbook=null;
		try {
			InputStream inputStream=file.getInputStream();
			workbook= WorkbookFactory.create(inputStream);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return menuService.batchMenuInfoInsertOrUpdate(workbook);
	}

	/**
	 * 导出菜单信息
	 */
	@GetMapping("/menuExcel")
	public void exportMenuList(@RequestParam(value = "menuSelect",required = false,defaultValue = "")String menuSelect,
	                           @RequestParam(value = "menuNbr",required = false,defaultValue = "")String menuNbr){
		menuService.exportUserInfo(menuNbr,menuSelect);
	}

	/**
	 * 获取角色菜单信息
	 * @return 查询结果
	 * @author john.xiao
	 * @date 2020-10-13 17:06
	 */
	@GetMapping("/menuList/role")
	public ResultUtil getRoleMenuList(@RequestParam("roleName")String roleName){
		return menuService.getRoleMenuInfoList(roleName);
	}

	/**
	 * 更新角色菜单
	 * @param menuEntityList 菜单实体列表
	 * @param roleName 角色名
	 * @return 结果封装类
	 */
	@PutMapping("/menuList/role")
	public ResultUtil updateRoleMenuList(@RequestBody List<MenuEntity> menuEntityList,
										 @RequestParam("roleName") String roleName){
		return menuService.updateRoleMenuInfoList(menuEntityList,roleName);
	}

	/**
	 * 获取用户菜单信息
	 * @return 查询结果
	 * @author john.xiao
	 * @date 2020-10-13 17:06
	 */
	@GetMapping("/menuList/user")
	public ResultUtil getUserMenuList(@RequestParam("userUserId")String userUserId){
		return menuService.getUserMenuInfoList(userUserId);
	}




}
