package com.example.controller.v2;

import com.example.constant.Message;
import com.example.entity.UserMstr;
import com.example.service.v2.UserService;
import com.example.utils.FileUtils;
import com.example.utils.MessageUtil;
import com.example.utils.ResultUtil;
import net.sf.json.JSONArray;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器V2版本
 * @author john.xiao
 * @date 2020-12-29 10:57
 */
@RestController("UserV2Controller")
@RequestMapping("/v2/userManage")
public class UserController {
	Logger logger= LoggerFactory.getLogger(UserController.class);
	@Resource(name = "UserV2Service")
	UserService userService;

	@PostMapping("login")
	public ResultUtil login(@RequestBody  UserMstr userMstr){
		String password=userMstr.getUserPassword();
		String userId=userMstr.getUserUserId();
		//检查密码长度
		if(password.length()<6 || password.length()>20){
			String message= MessageUtil.getMessage(Message.PASSWORD_NOT_STANDARD.getCode());
			logger.error(userId+":"+message);
			return ResultUtil.error(message);
		}
		return userService.login(userMstr);
	}

	/**
	 * 分页获取用户信息列表
	 * @param pageIndex 页数。默认值为1
	 * @param pageSize  每页大小。默认值为10
	 * @param criteriaList 排序条件列表
	 * @param userId 查询用户名
	 * @return
	 * @author john.xiao
	 * @date 2020-09-21 14:41
	 */
	@GetMapping("user")
	public ResultUtil getUserInfoList(@RequestParam(value = "pageIndex",required = false,defaultValue = "1")int pageIndex,
	                                  @RequestParam(value = "pageSize",required = false,defaultValue = "10")int pageSize,
	                                  @RequestParam(value = "userUserId",required = false,defaultValue = "")String userId,
	                                  @RequestParam(value = "criteriaList",required = false,defaultValue = "[]")String criteriaList){
		String tableParam;
		String criteria;
		//排序条件json转化列表
		JSONArray criteriaArray=JSONArray.fromObject(criteriaList);
		for (int i = 0; i < criteriaArray.size(); i++) {
			Map<String, Object> listMap = (Map<String, Object>) criteriaArray.get(i);
			criteria=listMap.get("criteria").toString();
			switch (criteria){
				case "username":
					tableParam="user_name";
					break;
				case "language":
					tableParam="user_lang";
					break;
				case "email":
					tableParam="user_mail_address";
					break;
				case "phone":
					tableParam="user_phone";
					break;
				case "qqNum":
					tableParam="user_qqnum";
					break;
				default:
					tableParam="user_userid";
			}
			listMap.put("criteria", tableParam);
		}
		return  userService.getUserInfoList(pageIndex,pageSize,userId,criteriaArray);
	}

	/**
	 * 更新单条信息
	 * @param userMstr 用户实体类
	 * @return 结果封装类
	 */
	@PutMapping("user")
	public ResultUtil updateUserInfo(UserMstr userMstr){
		return  userService.updateUserInfo(userMstr);
	}

	/**
	 * 插入单条信息
	 * @param userMstr 用户实体类
	 * @return 结果封装类
	 */
	@PostMapping("user")
	public ResultUtil insertUserInfo(@RequestBody UserMstr userMstr){
		return userService.insertUserInfo(userMstr);
	}

	/**
	 * 删除单条信息
	 * @param userMstr 用户实体类
	 * @return 结果封装类
	 */
	@DeleteMapping("user")
	public ResultUtil deleteUserInfo(UserMstr userMstr){
		return userService.deleteUserInfo(userMstr);
	}

	/**
	 * 批量删除信息
<<<<<<< HEAD
	 * @param userMstrList 用火实体类列表
=======
	 * @param userMstrList 用户实体类列表
>>>>>>> 5bd9d87719dfd1f67da3807730712881b1a37785
	 * @return 结果封装类
	 */
	@DeleteMapping("userList")
	public ResultUtil deleteUserInfoList(@RequestBody List<UserMstr> userMstrList){
		return userService.deleteUserInfoList(userMstrList);
	}

	/**
	 * 批量插入信息
	 * @param userMstrList 用火实体类列表
	 * @return 结果封装类
	 */
	@PostMapping("userList")
	public ResultUtil insertUserInfoList(@RequestBody List<UserMstr> userMstrList){
		return userService.insertUserInfoList(userMstrList);
	}

	/**
	 * 批量更新信息
	 * @param userMstrList 用火实体类列表
	 * @return 结果封装类
	 */
	@PutMapping("userList")
	public ResultUtil updateUserInfoList(@RequestBody List<UserMstr> userMstrList){
		return userService.updateUserInfoList(userMstrList);
	}

	/**
	 * 导入Excel插入或更新
	 * @param file Excel文件
	 * @return 结果封装类
	 */
	@PostMapping("userExcel")
	public ResultUtil insertUserInfoByExcel(MultipartFile file){
		//初步处理Excel文件
		Workbook workbook=null;
		try {
			InputStream inputStream=file.getInputStream();
			workbook= WorkbookFactory.create(inputStream);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return userService.batchUserInfoInsertOrUpdate(workbook);
	}

	/**
	 * 导出信息
	 * @param userId 用户Id
	 * @return
	 */
	@GetMapping("userExcel")
	public void getUserInfoByExcel(@RequestParam("userId")String userId){
		 userService.exportUserInfo(userId);
	}

	/**
	 * 获取导入模板
	 */
	@GetMapping("template")
	public void getTemplate(@RequestParam(value = "path",required = false,defaultValue = "D:/template/user.xls")String path){
		FileUtils fileUtils=new FileUtils();
		fileUtils.downLoad(path);
	}
}
