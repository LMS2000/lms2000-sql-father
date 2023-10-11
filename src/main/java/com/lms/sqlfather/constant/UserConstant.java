package com.lms.sqlfather.constant;

/**
 * 用户常量
 *
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "userLoginState";

    /**
     * 系统用户 id（虚拟用户）
     */
    long SYSTEM_USER_ID = 0;

    //  region 权限

    /**
     * 默认权限
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员权限
     */
    String ADMIN_ROLE = "admin";


    String CHECK_CODE_KEY = "check_code_key";
    String CHECK_CODE_KEY_EMAIL = "check_code_key_email";

    // 邮箱验证码前缀

    String EMAIIL_HEADER="check_email_code:";



    // endregion


}
