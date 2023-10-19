import { userLogin, userRegister, userSendEmailCode } from '@/services/userService';
import './index.less';
import { Link } from '@@/exports';
import {
	MobileOutlined,
} from '@ant-design/icons';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import {
	LoginForm,
	ProFormCaptcha,
	ProFormCheckbox,
	ProFormText
} from '@ant-design/pro-components';
import { useModel } from '@umijs/max';
import { message, Tabs } from 'antd';
import { useState, useRef } from 'react';
import { useSearchParams } from 'umi';
import type { CaptFieldRef } from '@ant-design/pro-components';
type LoginType = 'regiter' | 'account';
/**
 * 用户登录页面
 */
export default () => {
	const [searchParams] = useSearchParams();

	const { initialState, setInitialState } = useModel('@@initialState');
	const codeBaseUrl_0 = "http://localhost:8102/api/user/checkCode?type=0"
	const codeBaseUrl_1 = "http://localhost:8102/api/user/checkCode?type=1"
	const [codeUrl, SetCodeUrl] = useState(codeBaseUrl_0);
	const captchaRef = useRef<CaptFieldRef | null | undefined>();
	const [loginType, setLoginType] = useState<LoginType>('account');
	const formRef = useRef<any>();

	/**
	 * 用户登录或者注册
	 * @param fields
	 */
	const doLoginOrRegister = async (fields: any) => {
		if (loginType === 'account') {
			doUserLogin({ ...fields })
		} else {
			doUserRegister({ ...fields })
		}
	};
	/**
	 * 用户登录
	 * @param fields
	 */
	const doUserLogin = async (fields: UserType.UserLoginRequest) => {

		//判断是登录还是注册
		const hide = message.loading('登录中');
		try {
			const res = await userLogin({ ...fields });
			message.success('登录成功');
			setInitialState({
				...initialState,
				loginUser: res.data,
			} as InitialState);
			// 重定向到之前页面
			window.location.href = searchParams.get('redirect') ?? '/';
		} catch (e: any) {
			message.error(e.message);
			SetCodeUrl(codeBaseUrl_0 + "&time=" + new Date().getTime())
		} finally {
			hide();
		}
	};

	/**
	 * 用户注册
	 * @param fields
	 */
	const doUserRegister = async (fields: UserType.UserRegisterRequest) => {
		const hide = message.loading('注册中');
		try {
			await userRegister({ ...fields });
			hide();
			message.success('注册成功');
			setLoginType('account');
		} catch (e: any) {
			hide();
			message.error('注册失败，' + e.message);
		}
	};

	/**
	 * 获取邮箱校验码
	 */
	const doSendEmailCode = async () => {

		const formValues = formRef.current?.getFieldsFormatValue?.();


		const email = formValues.email;

		const code = formValues.code;

		try {
			//校验图片验证码是否输入
			await formRef.current.validateFields(['code']);
			console.log('code验证通过');
			//校验邮箱是否合法
			await formRef.current.validateFields(['email']);
			console.log('email验证通过');
			const sendEmail = {
				code: code,
				email: email,
				type: 0
			};
			//调用
			await userSendEmailCode(sendEmail);
			message.success('已发送邮件到' + email);
			captchaRef.current?.startTiming();
			return true;
		} catch (errorInfo) {
			SetCodeUrl(codeBaseUrl_1 + "&time=" + new Date().getTime())
			captchaRef.current?.endTiming();
			message.error(errorInfo.message)
			return false;
		}




	};


	return (
    
		<div
		style={{
			height: '87vh',
		
			background:
				'url(https://gw.alipayobjects.com/zos/rmsportal/FfdJeJRQWjEeGTpqgBKj.png)',
			backgroundSize: '60% 60%',
			padding: '8px 0 0px',
		}}>
		
		<div
			style={{
				height: '87vh',
				marginTop:'50px',
				background:
					'url(https://gw.alipayobjects.com/zos/rmsportal/FfdJeJRQWjEeGTpqgBKj.png)',
				backgroundSize: '60% 60%',
				padding: '8px 0 0px',
			}}>
			<LoginForm<any>
				logo="https://github.githubassets.com/images/modules/logos_page/Octocat.png"
				title="LMS的SQL生成工具"
				subTitle="快速生成代码和数据"
				formRef={formRef}
			  className='myForm'
				submitter={{
					searchConfig: {
						submitText: loginType === 'account' ? '登录' : '注册',
					},
				}}
				onFinish={async (formData) => {
					await doLoginOrRegister(formData);
				}}
			>
				<Tabs
					centered
					activeKey={loginType}
					onChange={(activeKey) => {
		
						setLoginType(activeKey as LoginType);
						if (loginType === 'account') {
							SetCodeUrl(codeBaseUrl_0 + "&time=" + new Date().getTime())
						} else {
							SetCodeUrl(codeBaseUrl_1 + "&time=" + new Date().getTime())
						}
					}
					}
				>
					<Tabs.TabPane key={'account'} tab={'账号密码登录'} />
					<Tabs.TabPane key={'regiter'} tab={'马上注册'} />
				</Tabs>
				{loginType === 'account' && (
					<>
						<ProFormText
							name="userAccount"
							fieldProps={{
								size: 'large',
								prefix: <UserOutlined className={'prefixIcon'} />,
							}}
							placeholder={'请输入账号'}
							rules={[
								{
									required: true,
									message: '请输入账号!',
								},
							]}
						/>
						<ProFormText.Password
							name="userPassword"
							fieldProps={{
								size: 'large',
								prefix: <LockOutlined className={'prefixIcon'} />,
							}}
							placeholder={'请输入密码'}
							rules={[
								{
									required: true,
									message: '请输入密码！',
								},
							]}
						/>
		
		
		
		
						<div className="container">
							<ProFormText
								name="code"
								fieldProps={{
									size: 'large',
									prefix: <LockOutlined className={'prefixIcon'} />
								}}
								placeholder={'请输入图片验证码'}
								rules={[
									{
										required: true,
										message: '请输入图片验证码！',
									}
								]}
							/>
							<div onClick={() => {
								console.log('触发事件')
								SetCodeUrl(codeBaseUrl_0 + "&time=" + new Date().getTime())
							}}>
								<img src={codeUrl} style={{ pointerEvents: 'none' }} />
							</div>
						</div>
						<div
							style={{
								marginBlockEnd: 24,
							}}
						>
					
						<Link  to="/user/findback" >忘记密码</Link>
							<Link to="/" style={{
									float: 'right',
								}}>返回主页</Link>
						</div>
					</>
				)}
				{loginType === 'regiter' && (
					<>
		
						<ProFormText
							name="userName"
							fieldProps={{
								size: 'large',
								prefix: <UserOutlined className={'prefixIcon'} />,
							}}
		
							placeholder={'请输入用户名'}
							rules={[
								{
									required: true,
									message: '请输入用户名!',
								},
							]}
						/>
						<ProFormText
							name="userAccount"
							fieldProps={{
								size: 'large',
								prefix: <UserOutlined className={'prefixIcon'} />,
							}}
							placeholder={'请输入账号（至少 4 位）'}
							rules={[
								{
									required: true,
									message: '请输入账号!',
								},
							]}
						/>
						<ProFormText.Password
							name="userPassword"
							fieldProps={{
								size: 'large',
								prefix: <LockOutlined className={'prefixIcon'} />,
							}}
							placeholder={'请输入密码（至少 8 位）'}
							rules={[
								{
									required: true,
									message: '请输入密码！',
								},
							]}
						/>
						<ProFormText.Password
							name="checkPassword"
							fieldProps={{
								size: 'large',
								prefix: <LockOutlined className={'prefixIcon'} />,
							}}
							placeholder={'请输入确认密码'}
							rules={[
								{
									required: true,
									message: '请输入确认密码！',
								},
							]}
						/>
		
						<ProFormText
							fieldProps={{
								size: 'large',
								prefix: <MobileOutlined className={'prefixIcon'} />,
							}}
							name="email"
							placeholder={'邮箱'}
							rules={[
								{
									required: true,
									message: '请输入邮箱！',
								},
								{
									pattern: /^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(.[a-zA-Z0-9_-]+)+$/,
									message: '邮箱格式错误！',
								},
							]}
						/>
						<div className="container">
							<ProFormText
								name="code"
								fieldProps={{
									size: 'large',
									prefix: <LockOutlined className={'prefixIcon'} />
								}}
								placeholder={'请输入图片验证码'}
								rules={[
									{
										required: true,
										message: '请输入图片验证码！',
									}
								]}
							/>
							<div onClick={() => {
								SetCodeUrl(codeBaseUrl_1 + "&time=" + new Date().getTime())
							}}>
								<img src={codeUrl} style={{ pointerEvents: 'none' }} />
							</div>
						</div>
						<ProFormCaptcha
							fieldRef={captchaRef}
							fieldProps={{
								size: 'large',
								prefix: <LockOutlined className={'prefixIcon'} />,
							}}
							captchaProps={{
								size: 'large',
							}}
							placeholder={'请输入验证码'}
							name="emailCode"
							rules={[
								{
									required: true,
									message: '请输入验证码！',
								},
							]}
							onGetCaptcha={async () => {
								//校验邮箱验证码
								doSendEmailCode()
							}}
						/>
					</>
				)}
			</LoginForm>
		</div>
		</div>
	

	);
};
