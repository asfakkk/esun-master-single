package com.example.constant;

/**
 * 信息码枚举类
 * @author John.xiao
 */
public enum Message {
    //密码格式不规范
    PASSWORD_NOT_STANDARD("password_not_standard","密码格式不规范"),
    EMAIL_NOT_STANDARD("email_not_standard","邮箱不规范"),
    PASSWORD_IS_NULL("password_is_null","密码为空"),
    QUERY_ERROR("query_error","请求失败"),
    QUERY_SUCCESS("query_success","请求成功"),
    USER_INFO_GET_ERROR("user_info_get_error","获取用户信息失败"),
    USER_INFO_DELETE_ERROR("user_info_delete_error","删除用户信息失败"),
    USER_INFO_UPDATE_ERROR("user_info_update_error","更新用户信息失败"),
    USER_INFO_INSERT_ERROR("user_info_insert_error","插入用户信息失败"),
    USER_INFO_GET_SUCCESS("user_info_get_success","获取用户信息成功"),
    USER_INFO_DELETE_SUCCESS("user_info_delete_success","删除用户信息成功"),
    USER_INFO_UPDATE_SUCCESS("user_info_update_success","更新用户信息成功"),
    USER_INFO_INSERT_SUCCESS("user_info_insert_success","插入用户信息成功"),
    USER_NOT_EXIST("user_not_exist","用户不存在"),
    USER_IS_EXISTED("user_is_existed","用户已存在"),
    USER_NOT_LOGIN("user_not_login","用户未登入"),
    PASSWORD_ERROR("password_error","密码错误"),
    USER_LOGGED("user_logged","用户已登入"),
    LOGIN_SUCCESS("login_success","登入成功"),
    REGISTER_ERROR("register_error","注册失败"),
    REGISTER_SUCCESS("register_success","注册成功"),
    FILE_EXISTED("file_existed","文件已存在"),
    TOKEN_UPDATE_ERROR("token_update_error","更新token失败"),
    TOKEN_IS_NULL("token_is_null","token为空"),
    TOKEN_CHECK_ERROR("token_check_error","token校验错误"),
    TOKEN_CHECK_SUCCESS("token_check_success","token校验成功"),
    ROUTER_CHECK_SUCCESS("router_check_success","路由校验成功"),
    ROUTER_CHECK_ERROR("router_check_error","理由校验失败"),
    ROUTER_GET_ERROR("router_get_error","获取路由表失败"),
    ROUTER_GET_SUCCESS("router_get_success","获取路由表成功"),
    ROUTER_DELETE_ERROR("router_delete_error","删除路由失败"),
    ROUTER_DELETE_SUCCESS("router_delete_success","路由删除成功"),
    ROUTER_ADD_ERROR("router_add_error","路由添加失败"),
    ROUTER_ADD_SUCCESS("router_add_success","路由添加成功"),
    ROUTER_IS_EXIST("router_is_exist","路由已存在"),
    ROUTER_NOT_EXIST("routre_not_exits","路由不存在"),
    GROUP_GET_ERROR("group_get_error","用户组获取失败"),
    GROUP_GET_SUCCESS("group_get_success","用户组获取成功"),
    GROUP_ADD_ERROR("group_add_error","用户组添加失败"),
    GROUP_ADD_SUCCESS("group_add_success","用户组添加成功"),
    GROUP_DELETE_ERROR("group_delete_error","用户组删除失败"),
    GROUP_DELETE_SUCCESS("group_delete_success","用户组删除成功"),
    GROUP_IS_EXIST("group_is_exist","用户组已存在"),
    GROUP_IS_NOT_EXIST("group_is_not_exist","用户组不存在"),
    UPDATE_PASSWORD_ERROR("update_password_error","更新密码失败"),
    UPDATE_PASSWORD_SUCCESS("update_password_success","更新密码成功"),
    GET_PROJECT_INFO_SUCCESS("get_project_info_success","获取项目信息成功"),
    GET_PROJECT_INFO_ERROR("get_project_info_error","获取项目信息失败"),
    UPLOAD_FTP_FAIL("upload_ftp_fail","上传FTP服务器失败"),
    UPLOAD_FTP_SUCCESS("upload_ftp_success","上传FTP服务器成功"),
    GET_SALT_FAIL("get_salt_fail","获取用户盐失败"),
    GET_SALT_SUCCESS("get_salt_success","获取用户盐成功"),
    //基础数据消息枚举
    BASE_GET_ERROR("base_get_error","获取基础数据失败"),
    BASE_GET_SUCCESS("base_get_success","获取基础数据成功"),
    BASE_ADD_ERROR("base_add_error","添加基础数据失败"),
    BASE_ADD_SUCCESS("base_add_success","添加基础数据成功"),
    BASE_DELETE_ERROR("base_delete_error","删除基础数据失败"),
    BASE_DELETE_SUCCESS("base_delete_success","基础数据删除成功"),
    BASE_UPDATE_ERROR("base_update_error","更新基础数据失败"),
    BASE_UPDATE_SUCCESS("base_update_success","更新基础数据成功"),
    BASE_IS_EXIST("base_is_exist","基础数据已存在"),
    BASE_NOT_EXIST("base_not_exits","基础数据不存在"),
    //发货信息消息枚举
    DELIVERY_GET_ERROR("delivery_get_error","获取发货信息失败"),
    DELIVERY_GET_SUCCESS("delivery_get_success","获取发货信息成功"),
    DELIVERY_ADD_ERROR("delivery_add_error","添加发货信息失败"),
    DELIVERY_ADD_SUCCESS("delivery_add_success","添加发货信息成功"),
    DELIVERY_DELETE_ERROR("delivery_delete_error","删除发货信息失败"),
    DELIVERY_DELETE_SUCCESS("delivery_delete_success","发货信息删除成功"),
    DELIVERY_UPDATE_ERROR("delivery_update_error","更新发货信息失败"),
    DELIVERY_UPDATE_SUCCESS("delivery_update_success","更新发货信息成功"),
    DELIVERY_IS_EXIST("delivery_is_exist","发货信息已存在"),
    DELIVERY_NOT_EXIST("delivery_not_exits","发货信息不存在"),
    //收货信息消息枚举
    RECEIVING_GET_ERROR("receiving_get_error","获取收货信息失败"),
    RECEIVING_GET_SUCCESS("receiving_get_success","获取收货信息成功"),
    RECEIVING_ADD_ERROR("receiving_add_error","添加收货信息失败"),
    RECEIVING_ADD_SUCCESS("receiving_add_success","添加收货信息成功"),
    RECEIVING_DELETE_ERROR("receiving_delete_error","删除收货信息失败"),
    RECEIVING_DELETE_SUCCESS("receiving_delete_success","收货信息删除成功"),
    RECEIVING_UPDATE_ERROR("receiving_update_error","更新收货信息失败"),
    RECEIVING_UPDATE_SUCCESS("receiving_update_success","更新收货信息成功"),
    RECEIVING_IS_EXIST("receiving_is_exist","收货信息已存在"),
    RECEIVING_NOT_EXIST("receiving_not_exits","收货信息不存在"),
    //客户信息消息枚举
    CUSTOMER_GET_ERROR("customer_get_error","获取客户信息失败"),
    CUSTOMER_GET_SUCCESS("customer_get_success","获取客户信息成功"),
    CUSTOMER_ADD_ERROR("customer_add_error","添加客户信息失败"),
    CUSTOMER_ADD_SUCCESS("customer_add_success","添加客户信息成功"),
    CUSTOMER_DELETE_ERROR("customer_delete_error","删除客户信息失败"),
    CUSTOMER_DELETE_SUCCESS("customer_delete_success","客户信息删除成功"),
    CUSTOMER_UPDATE_ERROR("customer_update_error","更新客户信息失败"),
    CUSTOMER_UPDATE_SUCCESS("customer_update_success","更新客户信息成功"),
    CUSTOMER_IS_EXIST("customer_is_exist","客户信息已存在"),
    CUSTOMER_NOT_EXIST("customer_not_exits","客户信息不存在"),
    //订单消息枚举
    ORDER_GET_ERROR("order_get_error","获取订单失败"),
    ORDER_GET_SUCCESS("order_get_success","获取订单成功"),
    ORDER_ADD_ERROR("order_add_error","添加订单失败"),
    ORDER_ADD_SUCCESS("order_add_success","添加订单成功"),
    ORDER_DELETE_ERROR("order_delete_error","删除订单失败"),
    ORDER_DELETE_SUCCESS("order_delete_success","订单删除成功"),
    ORDER_UPDATE_ERROR("order_update_error","添加订单失败"),
    ORDER_UPDATE_SUCCESS("order_update_success","添加订单失败"),
    ORDER_IS_EXIST("order_is_exist","订单已存在"),
    ORDER_NOT_EXIST("order_not_exits","订单不存在"),
    //导入发货消息枚举
    EXPORT_DELIVERY_ERROR("export_delivery_error","导入发货信息失败"),
    EXPORT_DELIVERY_SUCCESS("export_delivery_success","导入发货信息成功"),
    //导入收货消息枚举
    EXPORT_RECEIVING_ERROR("export_receiving_error","导入发货信息失败"),
    EXPORT_RECEIVING_SUCCESS("export_receiving_success","导入发货信息成功"),
    //导出发货信息
    DERIVE_DELIVERY_ERROR("derive_delivery_error","导出发货信息失败"),
    DERIVE_DELIVERY_SUCCESS("derive_delivery_success","导出发货信息成功"),
    //导出收货消息枚举
    DERIVE_RECEIVING_ERROR("derive_receiving_error","导出发货信息失败"),
    DERIVE_RECEIVING_SUCCESS("derive_receiving_success","导出发货信息成功"),
    PASSWORD_IS_SAME("password_is_same","密码相同"),
    TEST_MESSAGE("test_message","测试信息");


    /**
     * 信息码
     */
    private final String code;
    /**
     * 信息码描述
     */
    private final String description;

    private Message(String code,String description) {
        this.code=code;
        this.description=description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
