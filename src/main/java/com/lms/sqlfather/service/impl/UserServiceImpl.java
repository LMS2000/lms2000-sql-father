package com.lms.sqlfather.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lms.sqlfather.common.ErrorCode;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.mapper.UserMapper;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Wrapper;
import java.util.Queue;

import static com.lms.sqlfather.constant.UserConstant.ADMIN_ROLE;
import static com.lms.sqlfather.constant.UserConstant.USER_LOGIN_STATE;
@Slf4j
@Service
public class UserServiceImpl  extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "luomosan";

    @Override
    public long userRegister(String userName, String userAccount, String userPassword, String checkPassword, String userRole) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userName, userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userName.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名过长");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }
        //使用同步块来保证在高并发的环境下,同一时间有很多人用同一个账号注册冲突
       synchronized (userAccount.intern()){
           QueryWrapper<User> wrapper=new QueryWrapper<>();
           wrapper.eq("userAccount",userAccount);
           Long aLong = this.userMapper.selectCount(wrapper);
           if(aLong>0){
               throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在！");
           }

           // 3. 插入数据
           User user = new User();
           user.setUserName(userName);
           user.setUserAccount(userAccount);
           String encryptPassword= DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
           user.setUserPassword(encryptPassword);
           user.setUserRole(userRole);

           int insert = this.userMapper.insert(user);
           if(insert<0){
               throw new BusinessException(ErrorCode.OPERATION_ERROR,"注册失败");
           }
           return user.getId();
       }
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return user;

    }

    @Override
    public User getLoginUser(HttpServletRequest request) {

        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser=(User) attribute;
        if(currentUser==null||currentUser.getId()==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"未登录");
        }

        Long id = currentUser.getId();
        User byId = this.getById(id);
        if(byId==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"未登录");
        }

        return byId;

    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);

        User user=(User)attribute;

        return user!=null&&ADMIN_ROLE.equals(user.getUserRole());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);


        if(attribute==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR,"未登录");
        }

        request.getSession().removeAttribute(USER_LOGIN_STATE);

        return true;
    }
}
