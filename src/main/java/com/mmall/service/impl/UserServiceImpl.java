package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * ClassName: UserServiceImpl
 *
 * @author HoleLin
 * @version 1.0
 * @date 2019/4/8
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {
	@Autowired
	private UserMapper mUserMapper;

	@Override
	public ServerResponse<User> login(String username, String password) {
		// 校验用户名存不存在
		int resultCount = mUserMapper.checkUsername(username);
		if (resultCount == 0) {
			return ServerResponse.createByErrorMessage("用户名不存在!");
		}

		String md5PassWord = MD5Util.MD5EncodeUtf8(password);
		User user = mUserMapper.selectLogin(username, md5PassWord);
		if (user == null) {
			return ServerResponse.createByErrorMessage("密码错误!");
		}
		user.setPassword(StringUtils.EMPTY);
		return ServerResponse.createBySuccess("登录成功", user);
	}

	@Override
	public ServerResponse<String> register(User user) {
		// 校验用户名存不存在
		ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
		if (!validResponse.isSuccess()) {
			return validResponse;
		}
		validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
		if (!validResponse.isSuccess()) {
			return validResponse;
		}
		user.setRole(Const.Role.ROLE_CUSTOMER);
		// MD5加密
		user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
		int resultCount = mUserMapper.insert(user);
		if (resultCount == 0) {
			return ServerResponse.createByErrorMessage("注册失败");
		}
		return ServerResponse.createBySuccessMessage("注册成功");

	}

	@Override
	public ServerResponse<String> checkValid(String str, String type) {
		if (StringUtils.isNotBlank(type)) {
			// 开始校验
			if (Const.USERNAME.equals(type)) {
				int resultCount = mUserMapper.checkUsername(str);
				if (resultCount > 0) {
					return ServerResponse.createByErrorMessage("用户名已存在");
				}
			} else if (Const.EMAIL.equals(type)) {
				int resultCount = mUserMapper.checkEmail(str);
				if (resultCount > 0) {
					return ServerResponse.createByErrorMessage("email已存在");
				}
			}
		} else {
			return ServerResponse.createByErrorMessage("参数错误");
		}
		return ServerResponse.createBySuccessMessage("校验成功");
	}

	@Override
	public ServerResponse selectQuestion(String username) {
		ServerResponse validResponse = checkValid(username, Const.USERNAME);
		if (validResponse.isSuccess()) {
			// 用户不存在
			return ServerResponse.createByErrorMessage("用户不存在");
		}
		String question = mUserMapper.selectQuestionByUsername(username);
		if (StringUtils.isNotBlank(question)) {
			return ServerResponse.createBySuccess(question);
		}
		return ServerResponse.createByErrorMessage("找回密码的问题是空的");
	}

	@Override
	public ServerResponse<String> checkAnswer(String username, String question, String answer) {
		int resultCount = mUserMapper.checkAnswer(username, question, answer);
		if (resultCount > 0) {
			// 说明问题以及问题答案是这个用户的,并且正确的
			String forgetToken = UUID.randomUUID().toString();
			// forgetToken放在本体cache中
			TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
			return ServerResponse.createBySuccess(forgetToken);
		}
		return ServerResponse.createByErrorMessage("问题答案错误");
	}

	@Override
	public ServerResponse<String> forgetResetPassWord(String username, String passWordNew, String forgetToken) {
		if (StringUtils.isBlank(forgetToken)) {
			return ServerResponse.createByErrorMessage("参数错误,token需要传递");
		}
		// 校验username
		ServerResponse<String> validResponse = checkValid(username, Const.USERNAME);
		if (validResponse.isSuccess()) {
			return ServerResponse.createByErrorMessage("用户不存在");
		}
		String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
		if (StringUtils.isBlank(token)) {
			return ServerResponse.createByErrorMessage("token无效或者过期");
		}
		if (StringUtils.equals(forgetToken, token)) {
			String md5Password = MD5Util.MD5EncodeUtf8(passWordNew);
			int rowCount = mUserMapper.updatePasswordByUsername(username, md5Password);
			if (rowCount > 0) {
				return ServerResponse.createBySuccessMessage("修改密码成功");
			}
		} else {
			return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
		}
		return ServerResponse.createByErrorMessage("修改密码失败");
	}

	@Override
	public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
		// 防止横向越权,要校验一下这个用户的旧密码,一定要指定这个用户,因为我们会查询查询一个count(1),如果不指定id,那么结果就是true就是true及count>0
		int resultCount = mUserMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
		if (resultCount == 0) {
			return ServerResponse.createByErrorMessage("旧密码错误");
		}
		user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
		int updateCount = mUserMapper.updateByPrimaryKeySelective(user);
		if (updateCount > 0) {
			return ServerResponse.createBySuccessMessage("密码更新成功");
		}
		return ServerResponse.createByErrorMessage("密码更新失败");
	}

	@Override
	public ServerResponse<User> update_information(User user) {
		// username 不能被更新的
		// email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的
		int resultCount = mUserMapper.checkEmailByUserId(user.getEmail(), user.getId());
		if (resultCount > 0) {
			return ServerResponse.createByErrorMessage("email已经存在,请更换email再尝试更新");
		}
		User updateUser = new User();
		updateUser.setId(user.getId());
		updateUser.setEmail(user.getEmail());
		updateUser.setPhone(user.getPhone());
		updateUser.setQuestion(user.getQuestion());
		updateUser.setAnswer(user.getAnswer());

		int updateCount = mUserMapper.updateByPrimaryKeySelective(updateUser);
		if (updateCount > 0) {
			return ServerResponse.createBySuccessMessage("更新个人信息成功");
		}
		return ServerResponse.createByErrorMessage("更新个人信息失败");
	}

	@Override
	public ServerResponse<User> getInformation(Integer userId) {
		User user = mUserMapper.selectByPrimaryKey(userId);
		if (user == null) {
			return ServerResponse.createByErrorMessage("找不到当前用户");
		}
		user.setPassword(StringUtils.EMPTY);
		return ServerResponse.createBySuccess(user);
	}

	// backend

	/**
	 * 校验是否是管理员
	 * @param user
	 * @return
	 */
	@Override
	public ServerResponse checkAdminRole(User user) {
		if (user != null && user.getRole() == Const.Role.ROLE_ADMIN) {
			return ServerResponse.createBySuccess();
		} else {
			return ServerResponse.createByError();
		}
	}

}