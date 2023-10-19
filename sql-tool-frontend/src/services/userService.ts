import { request } from '@umijs/max';

/**
 * 分页获取用户列表
 * @param params
 */
export async function listUserByPage(params: UserType.UserQueryRequest) {
  return request<ResultData<any>>('/user/list/page', {
    method: 'GET',
    params,
  });
}


/**
 * 用户发送邮件
 * @param params
 */
export async function userSendEmailCode(params: UserType.UserSerndEmailCodeRequest) {
  return request<ResultData<any>>('/user/sendEmailCode', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: params,
  });
}

/**
 * 创建用户
 * @param params
 */
export async function addUser(params: UserType.UserAddRequest) {
  return request<ResultData<any>>('/user/add', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: params,
  });
}

/**
 * 根据 id 获取用户
 * @param id
 */
export async function getUserById(id: number) {
  return request<ResultData<any>>(`/user/get`, {
    method: 'GET',
    params: { id },
  });
}

/**
 * 根据 emailCode 判断是否通过校验
 * @param params
 */
export async function validEmailCode(params: UserType.UserEmailCodeRequest) {
  return request<ResultData<any>>(`/user/valid/findback`, {
    method: 'POST',
    data: params,
  });
}


/**
 * 根据 emailCode 判断是否通过校验
 * @param params
 */
export async function resetPassword(params: UserType.UserResetPasswordRequest) {
  return request<ResultData<any>>(`/user/update/password`, {
    method: 'POST',
    data: params,
  });
}
/**
 * 更新用户
 * @param params
 */
export async function updateUser(params: UserType.UserUpdateRequest) {
  return request<ResultData<any>>(`/user/update`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: params,
  });
}

/**
 * 删除用户
 * @param params
 */
export async function deleteUser(params: UserType.UserDeleteRequest) {
  return request<ResultData<any>>(`/user/delete`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: params,
  });
}

/**
 * 用户注册
 * @param params
 */
export async function userRegister(params: UserType.UserRegisterRequest) {
  return request<ResultData<any>>('/user/register', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: params,
  });
}

/**
 * 用户登录
 * @param params
 */
export async function userLogin(params: UserType.UserLoginRequest) {
	return request<ResultData<UserType.User>>('/user/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: params,
  });
}

/**
 * 用户匿名登录
 * @param params
 */
export async function userLoginAnonymous() {
  return request<ResultData<any>>('/user/login/anonymous', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: {},
  });
}

/**
 * 用户注销
 */
export async function userLogout() {
  return request<ResultData<any>>('/user/logout', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: {},
  });
}

/**
 * 获取当前登录用户
 */
export async function getLoginUser() {
  return request<ResultData<any>>('/user/get/login', {
    method: 'GET',
  });
}
