package com.lms.sqlfather.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.google.gson.internal.$Gson$Preconditions;
import com.lms.sqlfather.annotation.AuthCheck;
import com.lms.sqlfather.common.BaseResponse;
import com.lms.sqlfather.common.DeleteRequest;
import com.lms.sqlfather.common.ErrorCode;
import com.lms.sqlfather.common.ResultUtils;
import com.lms.sqlfather.constant.UserConstant;
import com.lms.sqlfather.exception.BusinessException;
import com.lms.sqlfather.model.dto.*;
import com.lms.sqlfather.model.entity.User;
import com.lms.sqlfather.model.vo.UserVO;
import com.lms.sqlfather.service.UserService;
import net.datafaker.Bool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.DecimalMin;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 注册
     * @param userRegisterRequest
     * @return
     */

    @PostMapping("/register")
    public BaseResponse<Long> registerUser(@RequestBody(required = true) UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userName = userRegisterRequest.getUserName();
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userName, userAccount, userPassword, checkPassword, UserConstant.DEFAULT_ROLE);
        return ResultUtils.success(result);
    }

    /**
     * 登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody(required = true)UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 注销
     * @param request
     * @return
     */
   @PostMapping("/logout")
   public BaseResponse<Boolean> logout(HttpServletRequest request){
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isLogout = userService.userLogout(request);
         return ResultUtils.success(isLogout);
    }


    /**
     * 获取当前用户
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<UserVO> getCurrentUser(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        UserVO userVO=new UserVO();
        BeanUtils.copyProperties(loginUser,userVO);
        return ResultUtils.success(userVO);
    }

    /**
     * 添加用户
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> add(@RequestBody(required = true)UserAddRequest userAddRequest,HttpServletRequest request){
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     * @param deleteRequest
     * @param request
     * @return
     */
   @DeleteMapping("/delete")
   @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
   public BaseResponse<Boolean> delete(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
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
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        return ResultUtils.success(result);
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
    public BaseResponse<List<UserVO>> listUser(UserQueryRequest userQueryRequest, HttpServletRequest request) {
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
        return ResultUtils.success(userVOList);
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
    public BaseResponse<UserVO> getUserById(int id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }







    /**
     * 获取用户分页列表
     * @param userQueryRequest
     * @param request
     * @return
     */

    @GetMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> page(UserQueryRequest userQueryRequest,HttpServletRequest request){
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
        return ResultUtils.success(userVOPage);
    }

}
