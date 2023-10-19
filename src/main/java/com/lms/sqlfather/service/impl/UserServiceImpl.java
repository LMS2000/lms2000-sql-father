package com.lms.sqlfather.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lms.contants.HttpCode;
import com.lms.sqlfather.config.AppConfig;
import com.lms.sqlfather.constant.CommonConstants;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.mapper.UserMapper;
import com.lms.sqlfather.model.dto.SysSettingsRequest;
import com.lms.sqlfather.model.dto.UserFindBackPasswordRequest;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.service.UserService;
import com.lms.sqlfather.utils.MybatisUtils;
import com.lms.sqlfather.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;


import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.stream.BaseStream;

import static com.lms.sqlfather.constant.UserConstant.ADMIN_ROLE;
import static com.lms.sqlfather.constant.UserConstant.USER_LOGIN_STATE;
@Slf4j
@Service
public class UserServiceImpl  extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;


    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private AppConfig appConfig;
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "luomosan";

    @Override
    public long userRegister(String userName, String userAccount, String userPassword, String checkPassword, String email,String userRole) {
        // 1. 校验

        BusinessException.throwIf(StringUtils.isAnyBlank(userName, userAccount, userPassword, checkPassword),
                "参数为空");

        BusinessException.throwIf(userName.length() > 16,"用户名过长");

        BusinessException.throwIf(userAccount.length() < 4);

        BusinessException.throwIf(userPassword.length() < 8 || checkPassword.length() < 8,
                "用户密码过短");

        BusinessException.throwIf(!userPassword.equals(checkPassword),"两次密码不一致");
        //使用同步块来保证在高并发的环境下,同一时间有很多人用同一个账号注册冲突
       synchronized (userAccount.intern()){
           QueryWrapper<User> wrapper=new QueryWrapper<>();
           wrapper.eq("user_account",userAccount);
           Long aLong = this.userMapper.selectCount(wrapper);
           BusinessException.throwIf(aLong>0,"账号已存在！");

           // 3. 插入数据
           User user = new User();
           user.setUserName(userName);
           user.setUserAccount(userAccount);
           String encryptPassword= DigestUtils.md5DigestAsHex((SALT+userPassword).getBytes());
           user.setUserPassword(encryptPassword);
           user.setUserRole(userRole);
           user.setEmail(email);
           int insert = this.userMapper.insert(user);
           BusinessException.throwIf(insert<0,"注册失败");
           return user.getId();
       }
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验

        BusinessException.throwIf(StringUtils.isAnyBlank(userAccount, userPassword),
                "参数为空");

        BusinessException.throwIf(userAccount.length() < 4,"账号错误");

        BusinessException.throwIf(userPassword.length() < 8,"密码错误");
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.nested(i -> i.eq("user_account", userAccount)
                        .or().eq("email", userAccount))
                .eq("user_password", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(HttpCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return user;

    }

    @Override
    public User getLoginUser(HttpServletRequest request) {

        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser=(User) attribute;
        BusinessException.throwIf(currentUser==null||currentUser.getId()==null,"未登录");
        Long id = currentUser.getId();
        User byId = this.getById(id);
        BusinessException.throwIf(byId==null,"未登录");
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

        BusinessException.throwIf(attribute==null,"未登录");

        request.getSession().removeAttribute(USER_LOGIN_STATE);

        return true;
    }

    @Override
    public String sendEmail(String email, Integer type) {
        //如果是注册，校验邮箱是否已存在
        if (Objects.equals(type, CommonConstants.ZERO)) {
            BusinessException.throwIf(MybatisUtils.existCheck(this, Map.of("email",email)),HttpCode.PARAMS_ERROR,
                    "邮箱已占用");
        }
        //随机的邮箱验证码
        String code = StringTools.getRandomNumber(CommonConstants.LENGTH_5);
        sendEmailCode(email, code);
        return code;
    }

    @Override
    public Boolean resetPasswordForFindBack(String email ,String password,String checkPassword) {

        User user = this.getOne(new QueryWrapper<User>().eq("email", email));

        BusinessException.throwIf(user==null);

        BusinessException.throwIf(!password.equals(checkPassword),HttpCode.PARAMS_ERROR,"两次密码不一致");

        String encodePassword=DigestUtils.md5DigestAsHex((SALT + password).getBytes());
       return this.update(new UpdateWrapper<User>().set("user_password",encodePassword));
    }

    private void sendEmailCode(String toEmail, String code) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            //邮件发件人
            helper.setFrom(appConfig.getSendUserName());
            //邮件收件人 1或多个
            helper.setTo(toEmail);

            SysSettingsRequest sysSettingsDto = new SysSettingsRequest();

            //邮件主题
            helper.setSubject(sysSettingsDto.getRegisterEmailTitle());
            //邮件内容
            helper.setText(String.format(sysSettingsDto.getRegisterEmailContent(), code));
            //邮件发送时间
            helper.setSentDate(new Date());
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            throw new BusinessException(HttpCode.OPERATION_ERROR,"邮件发送失败");
        }
    }
}
