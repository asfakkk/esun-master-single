package com.example.service.v2.impl;

import com.example.constant.Message;
import com.example.entity.UserMstr;
import com.example.service.feign.DbHelperService;
import com.example.service.feign.TokenService;
import com.example.service.v2.UserService;
import com.example.utils.*;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author test
 */
@Service("UserV2Service")
public class UserServiceImpl implements UserService {
	public static final String CODE = "code";
	private static final String DATASOURCE_POSTGRES="postgres";

	public static final String SUCCESS_CODE="10000";

	Logger logger= LoggerFactory.getLogger(UserServiceImpl.class);
	@Autowired
	@Lazy
	DbHelperService dbHelperService;
	@Autowired
	@Lazy
	TokenService tokenService;
	@Value("${file.disk.path}")
	String fileDiskPath;
	@Override
	public ResultUtil login(UserMstr userMstr) {
		String userId=userMstr.getUserUserId();
		String sql = "select user_password as \"password\",user_salt \"salt\" from user_mstr where lower(user_userId) = lower('"+userId+"') ";
		//结果信息
		String message;
		//调用DbHelper中间件服务，所有对数据库的请求均使用该中间件调用
		ResultUtil result=dbHelperService.select(sql,DATASOURCE_POSTGRES);
		if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
			//使用MessageUtil.getMessage方法从数据库中获取信息，不允许自己写返回信息
			//返回信息的code在Message枚举类中创建，在数据库中插入相应语言版本的返回信息
			message= MessageUtil.getMessage(Message.USER_INFO_GET_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		ArrayList list= (ArrayList) result.get("result");
		if(list.size()==0){
			message=MessageUtil.getMessage(Message.USER_NOT_EXIST.getCode());
			logger.error(message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		HashMap resultmap= (HashMap) list.get(0);
		//使用Optional类防止NPE
		Optional<Object> salt= Optional.ofNullable(resultmap.get("salt"));
		Optional<Object> userPassword=Optional.ofNullable(resultmap.get("password"));
		//MD5工具类
		Md5Util md5Util=new Md5Util();
		//
		if(!md5Util.checkPassword(userMstr.getUserPassword(),salt.orElse("").toString(),userPassword.orElse("").toString())){
			message="test";
			logger.error(message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}

		//更新token
		ResultUtil tokenResult=tokenService.updateToken(userId);
		if(!SUCCESS_CODE.equals(tokenResult.get(CODE).toString())){
			message=MessageUtil.getMessage(Message.TOKEN_UPDATE_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		//获取token
		Optional token=Optional.ofNullable(tokenResult.get("token"));
		//ResultUtil.put()传输返回结果
		message=MessageUtil.getMessage(Message.LOGIN_SUCCESS.getCode());
		logger.info(message);
		Map<String,Object> dataMap=new HashMap<>(2);
		dataMap.put("token",token.orElse(""));
		return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(dataMap);
	}

	@Override
	public ResultUtil insertUserInfo(UserMstr userMstr) {
		String message;
		DateTime date=new DateTime();
		String changeTime=date.toString("yyyyMMdd");
		Md5Util md5Util=new Md5Util();
		String encodePassword=md5Util.encodePassword(userMstr.getUserPassword());
		String salt=md5Util.getSalt();
		String GUID=GUIDUtils.create();
		boolean userExist=checkUserExist(userMstr.getUserUserId());
		if(userExist){
			message=MessageUtil.getMessage(Message.USER_IS_EXISTED.getCode());
			logger.error(userMstr.getUserUserId()+"："+message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		String sql="insert into user_mstr "+
				"(guid,user_userid,user_name,user_lang,user_password,user_last_chg_date,user_mail_address,user_type," +
				"user_country,user_actived,user_depart,user_post,user_phone,user_qqnum,user_salt,user_corp) " +
				"values('"+GUID+"','"+userMstr.getUserUserId()+"','"+userMstr.getUserName()+"','"+userMstr.getUserLang()+"','"+encodePassword+"','"+changeTime+"','"+userMstr.getUserMailAddress()+"','"+userMstr.getUserType()+"'," +
				"'"+userMstr.getUserCountry()+"',"+userMstr.getUserActived()+",'"+userMstr.getUserDepart()+"','"+userMstr.getUserPost()+"','"+userMstr.getUserPhone()+"','"+userMstr.getUserQqnum()+"','"+salt+"','"+userMstr.getUserCorp()+"');";
		ResultUtil result=dbHelperService.insert(sql,DATASOURCE_POSTGRES);
		if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
			message=MessageUtil.getMessage(Message.USER_INFO_INSERT_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		message=MessageUtil.getMessage(Message.USER_INFO_INSERT_SUCCESS.getCode());
		return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName());
	}

	@Override
	public ResultUtil updateUserInfo(UserMstr userMstr) {
		DateTime dateTime=new DateTime();
		String changeTime=dateTime.toString("yyyy-MM-dd");
		String message;
		Md5Util md5Util=new Md5Util();
		String encodePassword=md5Util.encodePassword(userMstr.getUserPassword());
		String salt=md5Util.getSalt();
		boolean userExist=checkUserExist(userMstr.getUserUserId());
		if(!userExist){
			message=MessageUtil.getMessage(Message.USER_NOT_EXIST.getCode());
			logger.error(userMstr.getUserUserId()+"："+message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		String sql="update user_mstr set user_name='"+userMstr.getUserName()+"',user_lang='"+userMstr.getUserLang()+"', user_mail_address='"+userMstr.getUserMailAddress()+"',user_last_chg_date='"+changeTime+"'," +
				"user_country='"+userMstr.getUserCountry()+"',user_actived="+userMstr.getUserActived()+",user_depart='"+userMstr.getUserDepart()+"',user_post='"+userMstr.getUserPost()+"',user_type='"+userMstr.getUserType()+"', "+
				"user_phone='"+userMstr.getUserPhone()+"',user_qqnum='"+userMstr.getUserQqnum()+"',user_password = '"+encodePassword+"',user_salt = '"+salt+"',user_corp = '"+userMstr.getUserCorp()+"' "+
				"where lower(user_userid)=lower('"+userMstr.getUserUserId()+"') ;";
		ResultUtil result=dbHelperService.update(sql,DATASOURCE_POSTGRES);
		if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
			message=MessageUtil.getMessage(Message.USER_INFO_UPDATE_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		message=MessageUtil.getMessage(Message.USER_INFO_UPDATE_SUCCESS.getCode());
		logger.info(message);
		return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName());
	}



	@Override
	public ResultUtil deleteUserInfo(UserMstr userMstr) {
		String message;
		String sql="delete from user_mstr where lower(user_userid)=lower('"+userMstr.getUserUserId()+"')";
		boolean userExist=checkUserExist(userMstr.getUserUserId());
		if(!userExist){
			message=MessageUtil.getMessage(Message.USER_NOT_EXIST.getCode());
			logger.error(userMstr.getUserUserId()+"："+message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		ResultUtil result=dbHelperService.delete(sql, DATASOURCE_POSTGRES);
		if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
			message=MessageUtil.getMessage(Message.USER_INFO_DELETE_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		message=MessageUtil.getMessage(Message.USER_INFO_DELETE_SUCCESS.getCode());
		logger.info(message);
		return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName());
	}

	@Override
	public ResultUtil deleteUserInfoList(List<UserMstr> userMstrList) {
		String message;
		for (UserMstr userMstr : userMstrList) {
			ResultUtil result = deleteUserInfo(userMstr);
			userMstr.setResult(result.get("msg").toString());
			userMstr.setCode(result.get("code").toString());
		}
		message=MessageUtil.getMessage(Message.USER_INFO_DELETE_SUCCESS.getCode());
		return  ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(userMstrList);
	}

	@Override
	public ResultUtil insertUserInfoList(List<UserMstr> userMstrList) {
		String message;
		for (UserMstr userMstr : userMstrList) {
			ResultUtil result = insertUserInfo(userMstr);
			userMstr.setResult(result.get("msg").toString());
			userMstr.setCode(result.get("code").toString());
		}
		message=MessageUtil.getMessage(Message.USER_INFO_INSERT_SUCCESS.getCode());
		return  ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(userMstrList);
	}

	@Override
	public ResultUtil updateUserInfoList(List<UserMstr> userMstrList) {
		String message;
		for (UserMstr userMstr : userMstrList) {
			ResultUtil result = updateUserInfo(userMstr);
			userMstr.setResult(result.get("msg").toString());
			userMstr.setCode(result.get("code").toString());
		}
		message=MessageUtil.getMessage(Message.USER_INFO_UPDATE_SUCCESS.getCode());
		return  ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(userMstrList);
	}

	@Override
	public ResultUtil getUserInfoList(int pageIndex, int pageSize, String userId, List<Map<String, Object>> criteriaList) {
		String sortString=getSortString(criteriaList);
		String sql="select guid,user_name as \"userName\" ,user_userid as \"userUserId\" ,user_phone as \"userPhone\" ," +
				"user_mail_address as \"userMailAddress\" ,user_lang as \"userLang\", user_type as \"userType\", " +
				"user_country as \"userCountry\", user_actived as \"userActived\" ,user_depart as \"userDepart\"," +
				"user_post as \"userPost\" , user_qqnum as \"userQqnum\" " +
				" from user_mstr where user_userid ilike '%25"+userId+"%25' order by "+sortString+";";
		String message;
		ResultUtil result=dbHelperService.selectPage(sql,DATASOURCE_POSTGRES,pageIndex,pageSize);
		if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
			message=MessageUtil.getMessage(Message.USER_INFO_GET_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		ArrayList list= (ArrayList) result.get("result");
		Map<String,Object> dataMap=new HashMap<>(2);
		//获取总条数
		int count= (int) result.get("count");
		dataMap.put("list",list);
		dataMap.put("count",count);
		message=MessageUtil.getMessage(Message.USER_INFO_GET_SUCCESS.getCode());
		logger.info(message);
		return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName()).setData(dataMap);
	}

	/**
	 * 获取排序条件字符串
	 * @param criteriaList 排序列表
	 * @return 排序后的字符
	 * @author john.xiao
	 * @date 2020-12-17 11-27
	 */
	private String getSortString(List<?> criteriaList){
		StringBuilder criteriaBuilder=new StringBuilder();
		if(criteriaList.size()>0){
			for (int i = 0; i < criteriaList.size(); i++) {
				Map<String,Object> listMap= (Map<String, Object>) criteriaList.get(i);
				Optional<Object> sort=Optional.ofNullable(listMap.get("sort"));
				Optional<Object> criteria=Optional.ofNullable(listMap.get("criteria"));
				criteriaBuilder.append(criteria.orElse("order_id"));
				if (!"0".equals(sort.orElse("0"))){
					criteriaBuilder.append(" desc");
				}
				criteriaBuilder.append(" ,");
			}
		}
		else {
			//设置默认排序项
			criteriaBuilder.append("user_userid,");
		}
		criteriaBuilder.setLength(criteriaBuilder.length()-1);
		return  criteriaBuilder.toString();
	}

	@Override
	public ResultUtil updatePassword(UserMstr userMstr, String newPassword) {
		String password=userMstr.getUserPassword();
		String userId=userMstr.getUserUserId();
		Md5Util md5Util=new Md5Util();
		String message;
		boolean userExist=checkUserExist(userMstr.getUserUserId());
		if(!userExist){
			message=MessageUtil.getMessage(Message.USER_NOT_EXIST.getCode());
			logger.error(userMstr.getUserUserId()+"："+message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		//获取盐
		String saltSql="select user_salt,user_password from user_mstr where lower(user_userid)= lower('"+userId+"');";
		ResultUtil saltResult=dbHelperService.select(saltSql,DATASOURCE_POSTGRES);
		if(!SUCCESS_CODE.equals(saltResult.get(CODE).toString())){
			message=MessageUtil.getMessage(Message.GET_SALT_FAIL.getCode());
			logger.error(message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		message=MessageUtil.getMessage(Message.GET_SALT_SUCCESS.getCode());
		logger.info(message);
		ArrayList<Map<String,Object>> list= (ArrayList) saltResult.get("result");
		String userSalt=list.get(0).get("user_salt").toString();
		String userPassword=list.get(0).get("user_password").toString();
		//校验密码是否相同
		if(!md5Util.checkPassword(password,userSalt,userPassword)){
			message=MessageUtil.getMessage(Message.PASSWORD_ERROR.getCode());
			logger.error(userId+":"+message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		//使用MD5进行加密
		String encodePassword= md5Util.encodePassword(newPassword);
		String salt=md5Util.getSalt();
		String sql="update user_mstr set user_password= '"+encodePassword+"',user_salt= '"+salt+"'"+
				"where lower(user_userid) = lower('"+userId+"') ;";
		ResultUtil result=dbHelperService.update(sql,DATASOURCE_POSTGRES);
		if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
			message=MessageUtil.getMessage(Message.UPDATE_PASSWORD_ERROR.getCode());
			logger.error(message);
			return ResultUtil.error(message,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		message=MessageUtil.getMessage(Message.UPDATE_PASSWORD_SUCCESS.getCode());
		logger.info(message);
		return ResultUtil.ok(message,Thread.currentThread().getStackTrace()[1].getMethodName());
	}

	@Override
	public ResultUtil batchUserInfoInsertOrUpdate(Workbook workbook) {
		String defaultPassword= "123456";
		//获取Excel文档第一个表格
		Sheet sheet=workbook.getSheetAt(0);
		//获取表格标题列表
		List titleList= PoiUtils.getTitleList(PoiUtils.getRow(sheet,0));
		//请求结果列表
		List<Map<String,Object>> resultList=new ArrayList<>();
		Map<String,Object> userInfo;
		Optional<Object> username;
		Optional<Object> userId;
		Optional<Object> language;
		Optional<Object> email;
		Optional<Object> type;
		Optional<Object> phone;
		Optional<Object> country;
		Optional<Object> isActive;
		Optional<Object> depart;
		Optional<Object> post;
		Optional<Object> qqNum;
		Optional<Object> corp;
		//循环遍历用户信息写入列表
		for (int i = 1; i <=sheet.getLastRowNum() ; i++) {
			//获取相应行的数据，转换为list
			userInfo=PoiUtils.getRowData(PoiUtils.getRow(sheet,i),titleList);
			username=Optional.ofNullable(userInfo.get("userName"));
			userId=Optional.ofNullable(userInfo.get("userUserId"));
			language=Optional.ofNullable(userInfo.get("userLang"));
			email=Optional.ofNullable(userInfo.get("userMailAddress"));
			type=Optional.ofNullable(userInfo.get("userType"));
			phone=Optional.ofNullable(userInfo.get("userPhone"));
			country=Optional.ofNullable(userInfo.get("userCountry"));
			isActive= Optional.ofNullable(userInfo.get("userActived"));
			depart=Optional.ofNullable(userInfo.get("userDepart"));
			post=Optional.ofNullable(userInfo.get("userPost"));
			qqNum=Optional.ofNullable(userInfo.get("userQqnum"));
			corp=Optional.ofNullable(userInfo.get("userCorp"));

			//实体类赋值
			UserMstr userMstr=new UserMstr();
			userMstr.setUserName(username.orElse("").toString());
			userMstr.setUserUserId(userId.orElse("").toString());
			userMstr.setUserLang(language.orElse("CH").toString());
			userMstr.setUserMailAddress(email.orElse("").toString());
			userMstr.setUserType(type.orElse("").toString());
			userMstr.setUserPhone(phone.orElse("").toString());
			userMstr.setUserCountry(country.orElse("CH").toString());
			userMstr.setUserActived((Boolean.parseBoolean(isActive.orElse("true").toString())));
			userMstr.setUserDepart(depart.orElse("").toString());
			userMstr.setUserPost(post.orElse("").toString());
			userMstr.setUserQqnum(qqNum.orElse("").toString());
			userMstr.setCorpId(corp.orElse("").toString());
			userMstr.setUserPassword(defaultPassword);


			//用户运行结果Map
			Map<String,Object> resultMap=new HashMap<>(2);
			//查看该用户是否存在
			if (!checkUserExist(userMstr.getUserUserId())){
				ResultUtil insertResult= insertUserInfo(userMstr);
				resultMap.put(userMstr.getUserUserId(),insertResult);
			}
			else {
				ResultUtil updateResult=updateUserInfo(userMstr);
				resultMap.put(userMstr.getUserUserId(),updateResult);
			}
			resultList.add(resultMap);
		}
		return ResultUtil.ok().setData(resultList);
	}

	@Override
	public void exportUserInfo(String userId) {
		String sql="select user_name as \"userName\" ,user_userid as \"userUserId\" ,user_phone as \"userPhone\" ," +
				"user_mail_address as \"userMailAddress\" ,user_lang as \"userLang\", user_type as \"userType\", " +
				"user_country as \"userCountry\", user_actived as \"userActived\" ,user_depart as \"userDepart\", " +
				"user_post as \"userPost\" , user_qqnum as \"userQqnum\",user_corp as \"userCorp\" " +
				"from user_mstr where user_userid ilike '%25"+userId+"%25';";
		String message;
		ResultUtil result=dbHelperService.select(sql,DATASOURCE_POSTGRES);
		if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
			message=MessageUtil.getMessage(Message.USER_INFO_GET_ERROR.getCode());
			logger.error(message);
		}
		ArrayList list= (ArrayList) result.get("result");
		//判断用户是否存在
		if (list.size()==0){
			message=MessageUtil.getMessage(Message.USER_NOT_EXIST.getCode());
			logger.error(message);
		}
		List<String> titleList=new ArrayList<>();
		titleList.add("userUserId");
		titleList.add("userName");
		titleList.add("userPhone");
		titleList.add("userMailAddress");
		titleList.add("userLang");
		titleList.add("userType");
		titleList.add("userCountry");
		titleList.add("userActived");
		titleList.add("userDepart");
		titleList.add("userPost");
		titleList.add("userQqnum");
		String path= ExcelUtils.createMapListExcel(list,fileDiskPath,titleList);
		FileUtils fileUtils=new FileUtils();
		fileUtils.downLoad(path);
	}


	/**
	 * 检查用户是否存在
	 * @param userId 用户Id
	 * @return 用户是否存在
	 */
	private boolean checkUserExist(String userId){
		String sql ="select 1 from  user_mstr  where lower(user_userid)=lower('"+userId+"')";
		ResultUtil result=dbHelperService.select(sql,DATASOURCE_POSTGRES);
		String meesage;
		if(!SUCCESS_CODE.equals(result.get(CODE).toString())){
			meesage=MessageUtil.getMessage(Message.USER_INFO_GET_ERROR.getCode());
			logger.error(meesage,Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		ArrayList list= (ArrayList) result.get("result");
		return list.size() > 0;
	}
}
