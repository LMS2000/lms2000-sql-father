/**
 * 按照初始化数据定义项目中的权限，统一管理
 * @param initialState
 */
export default (initialState: InitialState) => {
  const canUser = !!initialState.loginUser;
  const canAdmin =
    initialState.loginUser && initialState.loginUser.userRole === 'admin';
  return {
    canUser,
    canAdmin,
  };
};
