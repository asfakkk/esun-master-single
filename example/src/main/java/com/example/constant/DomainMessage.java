package com.example.constant;

public enum DomainMessage {
    DOMAIN_GET_SUCCESS("domain_get_success","获取域成功"),
    DOMAIN_GET_ERROR("domain_get_error","获取域失败"),
    DOMAIN_UPDATE_SUCCESS("domain_update_success","更新域成功"),
    DOMAIN_UPDATE_ERROR("domain_update_error","更新域失败"),
    DOMAIN_DELETE_SUCCESS("domain_delete_success","删除域成功"),
    DOMAIN_DELETE_ERROR("domain_delete_error","删除域失败"),
    DOMAIN_ADD_SUCCESS("domain_add_success","添加域成功"),
    DOMAIN_ADD_ERROR("domain_add_error","添加域失败"),
    DOMAIN_IS_EXIST("domain_is_exist","域已存在"),
    DOMAIN_NOT_EXIST("domain_not_exist","域不存在"),
    DOMAIN_EXPORT_SUCCESSS("domain_export_success","导出域信息成功"),
    DOMAIN_EXPORT_ERROR("domain_export_error","导出域信息失败");


    //信息码
    private final String code;
    //信息码描述
    private final String description;

    private DomainMessage(String code, String description) {
        this.code=code;
        this.description=description;
    }



    public String getCode() {
        return code;
    }
}
