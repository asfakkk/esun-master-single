package com.example.controller.v2;


import com.example.entity.RoleMstr;
import com.example.service.v2.RoleService;
import com.example.utils.FileUtils;
import com.example.utils.ResultUtil;
import feign.Param;
import net.sf.json.JSONArray;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 角色管理接口控制器
 * 提供对角色信息的查询，插入，更新，删除
 * 提供对用户角色的查询，以及更新用户角色
 * @author john.xiao
 * @date 2020-1-4 09:49
 */
@RestController
@RequestMapping("v2/roleManage")
public class RoleController {
	@Resource(name = "RoleV2Service")
	RoleService roleService;

	/**
	 * 分页获取角色信息列表
	 *
	 * @param pageIndex 页数。默认值为1
	 * @param pageSize  每页大小。默认值为10
	 * @param roleName  查询角色名
	 * @return
	 * @author john.xiao
	 * @date 2020-09-21 14:41
	 */
	@GetMapping("/role")
	public ResultUtil getRoleInfoList(@RequestParam(value = "pageIndex", required = false, defaultValue = "1") int pageIndex,
									  @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize,
									  @RequestParam(value = "roleName", required = false, defaultValue = "") String roleName,
									  @RequestParam(value = "criteriaList", required = false, defaultValue = "[]") String criteriaList) {
		String tableParam;
		String criteria;
		//排序条件json转化列表
		JSONArray criteriaArray=JSONArray.fromObject(criteriaList);
		for (int i = 0; i < criteriaArray.size(); i++) {
			Map<String, Object> listMap = (Map<String, Object>) criteriaArray.get(i);
			criteria=listMap.get("criteria").toString();
			switch (criteria){
				case "roleName":
					tableParam="role_name";
					break;
				case "roleDesc":
					tableParam="role_desc";
					break;
				default:
					tableParam="role_name";
			}
			listMap.put("criteria", tableParam);
		}
		return roleService.getRoleInfoList(pageIndex, pageSize, roleName, criteriaArray);
	}


	/**
	 * 添加角色
	 *
	 * @param roleMstrList 角色表
	 * @return
	 * @author john.xiao
	 * @date 2020-10-13 17:06
	 */
	@PostMapping("/role")
	public ResultUtil addRole(@RequestBody List<RoleMstr> roleMstrList) {
		return roleService.insertRoleInfoList(roleMstrList);
	}

	/**
	 * 删除角色
	 *
	 * @param roleMstrList 角色表
	 * @return
	 * @author john.xiao
	 * @date 2020-10-13 17:06
	 */
	@DeleteMapping("/role")
	public ResultUtil deleteRole(@RequestBody List<RoleMstr> roleMstrList) {
		return roleService.deleteRoleInfolist(roleMstrList);
	}

	/**
	 * 更新角色
	 *
	 * @param roleMstrList 角色表
	 * @return
	 * @author john.xiao
	 * @date 2020-10-13 17:06
	 */
	@PutMapping("/role")
	public ResultUtil updateRole(@RequestBody List<RoleMstr> roleMstrList) {
		return roleService.updateRoleInfolist(roleMstrList);
	}

	/**
	 * 获取用户角色信息
	 *
	 * @param userUserId
	 * @return
	 */
	@GetMapping("/userRole")
	public ResultUtil getUserRole(@RequestParam("userUserId") String userUserId) {
		return roleService.getUserRoleInfoList(userUserId);
	}

	/**
	 * 更新用户角色
	 *
	 * @param roleMstrList
	 * @param userUserId
	 * @return
	 * @author john.xiao
	 * @date 2020-10-13 17:06
	 */
	@PutMapping("/userRole")
	public ResultUtil updateUserRole(@RequestBody List<RoleMstr> roleMstrList,
									 @RequestParam("userUserId") String userUserId) {
		return roleService.updateUserRoleInfoList(roleMstrList, userUserId);

	}

	/**
	 * 导入Excel
	 *
	 * @param file Excel文件
	 * @return 结果封装类
	 */
	@PostMapping("/roleExcel")
	public ResultUtil insertUserInfoByExcel(MultipartFile file) {
		//初步处理Excel文件
		Workbook workbook = null;
		try {
			InputStream inputStream = file.getInputStream();
			workbook = WorkbookFactory.create(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return roleService.batchRoleInfoInsertOrUpdate(workbook);
	}

	/**
	 * 导出信息
	 *
	 * @param roleName 角色名
	 * @return
	 */
	@GetMapping("/roleExcel")
	public void getUserInfoByExcel(@RequestParam("roleName") String roleName) {
		roleService.exportRoleInfo(roleName);
	}

	/**
	 * 获取导入模板
	 */
	@GetMapping("template")
	public void getTemplate(@RequestParam(value = "path",required = false,defaultValue = "D:/template/role.xls")String path) {
		FileUtils fileUtils = new FileUtils();
		fileUtils.downLoad(path);
	}

}