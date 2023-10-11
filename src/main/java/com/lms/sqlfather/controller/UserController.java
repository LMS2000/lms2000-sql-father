package com.lms.sqlfather.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.gson.internal.$Gson$Preconditions;
import com.lms.contants.HttpCode;
import com.lms.result.EnableResponseAdvice;
import com.lms.result.ResultData;
import com.lms.sqlfather.annotation.AuthCheck;
import com.lms.sqlfather.common.DeleteRequest;
import com.lms.sqlfather.constant.UserConstant;
import com.lms.sqlfather.core.utils.CreateImageCode;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.model.dto.*;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.model.vo.UserVO;
import com.lms.sqlfather.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Positive;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@EnableResponseAdvice
public class UserController {

    @Resource
    private UserService userService;


    /**
     * 验证码
     *  0 为注册    1 为邮箱验证
     * @param response
     * @param request
     * @param type
     * @throws IOException
     */
    @GetMapping(value = "/checkCode")
    @ApiOperation("图片校验码")
    public void checkCode(HttpServletResponse response, HttpServletRequest request, Integer type) throws
            IOException {
        CreateImageCode vCode = new CreateImageCode(130, 38, 5, 10);
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setContentType("image/jpeg");
        String code = vCode.getCode();
        HttpSession session = request.getSession();
        if (type == null || type == 0) {
            session.setAttribute(UserConstant.CHECK_CODE_KEY,code);
        } else {
            session.setAttribute(UserConstant.CHECK_CODE_KEY_EMAIL,code);
        }
        vCode.write(response.getOutputStream());
    }



    /**
     * type 0 为注册 1 为找回密码
     * @param session
     * @return
     */
    @PostMapping("/sendEmailCode")
    public Boolean sendEmailCode(HttpSession session,@Validated @RequestBody SendEmailRequest sendEmailRequest) {
        String code = sendEmailRequest.getCode();
        String email = sendEmailRequest.getEmail();
        Integer type = sendEmailRequest.getType();
        try {
            if (!code.equalsIgnoreCase((String) session.getAttribute(UserConstant.CHECK_CODE_KEY_EMAIL))) {
                throw new BusinessException(HttpCode.PARAMS_ERROR,"图片验证码不正确");
            }
            String emailCode = userService.sendEmail(email, type);
            session.setAttribute(UserConstant.EMAIIL_HEADER+type,emailCode);
            return true;
        } finally {
            session.removeAttribute(UserConstant.CHECK_CODE_KEY_EMAIL);
        }
    }






    /**
     * 注册
     * @param userRegisterRequest
     * @return
     */

    @PostMapping("/register")
    public Long registerUser(@RequestBody(required = true) UserRegisterRequest userRegisterRequest,HttpSession session){

        try{

            BusinessException.throwIf(userRegisterRequest == null);
            String code = (String) session.getAttribute(UserConstant.EMAIIL_HEADER);
            BusinessException.throwIf(org.springframework.util.StringUtils.isEmpty(code)||!code.equals(userRegisterRequest.getEmailCode()),
                    HttpCode.PARAMS_ERROR,"邮箱验证码错误");
            String userName = userRegisterRequest.getUserName();
            String userAccount = userRegisterRequest.getUserAccount();
            String userPassword = userRegisterRequest.getUserPassword();
            String checkPassword = userRegisterRequest.getCheckPassword();
            String email = userRegisterRequest.getEmail();
            if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
                return null;
            }
            return userService.userRegister(userName, userAccount, userPassword, checkPassword,email, UserConstant.DEFAULT_ROLE);

        }finally {
            session.removeAttribute(UserConstant.EMAIIL_HEADER);
        }


    }

    /**
     * 登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public User userLogin(@RequestBody(required = true)UserLoginRequest userLoginRequest, HttpServletRequest request){

        try{
            BusinessException.throwIf(userLoginRequest == null);
            //校验码校验
            String trueCode =(String)request.getSession().getAttribute(UserConstant.CHECK_CODE_KEY);
            String code = userLoginRequest.getCode();
            BusinessException.throwIf(org.springframework.util.StringUtils.isEmpty(trueCode)||!trueCode.equals(code),HttpCode.PARAMS_ERROR,
                    "图片校验码不正确");
            String userAccount = userLoginRequest.getUserAccount();
            String userPassword = userLoginRequest.getUserPassword();
            BusinessException.throwIf(StringUtils.isAnyBlank(userAccount, userPassword));
            return userService.userLogin(userAccount, userPassword, request);

        }finally {
            request.getSession().removeAttribute(UserConstant.CHECK_CODE_KEY);
        }
    }

    /**
     * 注销
     * @param request
     * @return
     */
   @PostMapping("/logout")
   public Boolean logout(HttpServletRequest request){
        return userService.userLogout(request);
    }


    /**
     * 获取当前用户
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public UserVO getCurrentUser(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        UserVO userVO=new UserVO();
        BeanUtils.copyProperties(loginUser,userVO);
        return userVO;
    }

    /**
     * 添加用户
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Long add(@RequestBody(required = true)UserAddRequest userAddRequest,HttpServletRequest request){

        BusinessException.throwIf(userAddRequest == null);
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        BusinessException.throwIf(!result, HttpCode.OPERATION_ERROR);
        return user.getId();
    }

    /**
     * 删除用户
     * @param deleteRequest
     * @param request
     * @return
     */
   @DeleteMapping("/delete")
   @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   public Boolean delete(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){

        BusinessException.throwIf(deleteRequest == null || deleteRequest.getId() <= 0);
        return userService.removeById(deleteRequest.getId());

   }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Boolean updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {

        BusinessException.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null);
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        return userService.updateById(user);

    }
    /**
     * 获取用户列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public List<UserVO> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        List<User> userList = userService.list(queryWrapper);
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return userVOList;
    }
    /**
     * 根据 id 获取用户
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public UserVO getUserById(@Positive(message = "id不合法") Integer id, HttpServletRequest request) {

        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }







    /**
     * 获取用户分页列表
     * @param userQueryRequest
     * @param request
     * @return
     */

    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public Page<UserVO> page(UserQueryRequest userQueryRequest,HttpServletRequest request){
        long current = 1;
        long size = 10;
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
            current = userQueryRequest.getCurrent();
            size = userQueryRequest.getPageSize();
        }
        QueryWrapper<User> wrapper=new QueryWrapper<>(userQuery);

        Page<User> userPage=userService.page(new Page<>(current,size),wrapper);
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList=userPage.getRecords().stream().map(user->{
            UserVO userVO=new UserVO();
            BeanUtils.copyProperties(user,userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

}
