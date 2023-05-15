package com.yami.shop.api.controller;

import com.yami.shop.bean.app.dto.UserDto;
import com.yami.shop.bean.app.param.UserInfoParam;
import com.yami.shop.bean.model.User;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import cn.hutool.core.bean.BeanUtil;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.web.bind.annotation.*;
/**
 * @author 北易航
 */
@RestController
@RequestMapping("/p/user")
@Tag(name = "用户接口")
@AllArgsConstructor
public class UserController {

	private final UserService userService;
	
	/**
	 * 查看用户接口
	 */
	@GetMapping("/userInfo")
	@Operation(summary = "查看用户信息" , description = "根据用户ID（userId）获取用户信息")
	public ServerResponseEntity<UserDto> userInfo() {
		// 获取当前用户的ID
		String userId = SecurityUtils.getUser().getUserId();

		// 根据用户ID从数据库中获取用户信息
		User user = userService.getById(userId);

		// 将User对象转换为UserDto对象
		UserDto userDto = BeanUtil.copyProperties(user, UserDto.class);

		// 返回用户信息的响应实体
		return ServerResponseEntity.success(userDto);
	}


	@PutMapping("/setUserInfo")
	@Operation(summary = "设置用户信息" , description = "设置用户信息")
	public ServerResponseEntity<Void> setUserInfo(@RequestBody UserInfoParam userInfoParam) {
		// 获取当前用户的ID
		String userId = SecurityUtils.getUser().getUserId();

		// 创建一个新的User对象
		User user = new User();

		// 设置User对象的属性
		user.setUserId(userId);
		user.setPic(userInfoParam.getAvatarUrl());
		user.setNickName(userInfoParam.getNickName());

		// 调用userService的updateById方法更新用户信息
		userService.updateById(user);

		// 返回操作成功的响应实体
		return ServerResponseEntity.success();
	}

}
