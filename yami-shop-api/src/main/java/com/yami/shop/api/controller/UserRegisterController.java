package com.yami.shop.api.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.bean.model.User;
import com.yami.shop.bean.param.UserRegisterParam;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.security.common.bo.UserInfoInTokenBO;
import com.yami.shop.security.common.enums.SysTypeEnum;
import com.yami.shop.security.common.manager.PasswordManager;
import com.yami.shop.security.common.manager.TokenStore;
import com.yami.shop.security.common.vo.TokenInfoVO;
import com.yami.shop.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;

/**
 * 用户信息
 *
 * @author 北易航
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户注册相关接口")
@AllArgsConstructor
public class UserRegisterController {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final TokenStore tokenStore;

    private final PasswordManager passwordManager;

    @PostMapping("/register")
    @Operation(summary = "注册" , description = "用户注册或绑定手机号接口")
    public ServerResponseEntity<TokenInfoVO> register(@Valid @RequestBody UserRegisterParam userRegisterParam) {
        // 如果昵称为空，则将用户名作为昵称
        if (StrUtil.isBlank(userRegisterParam.getNickName())) {
            userRegisterParam.setNickName(userRegisterParam.getUserName());
        }

        // 检查是否正在进行注册申请
        if (userService.count(new LambdaQueryWrapper<User>().eq(User::getNickName, userRegisterParam.getNickName())) > 0) {
            // 该用户名已注册，无法重新注册
            throw new YamiShopBindException("该用户名已注册，无法重新注册");
        }

        // 获取当前时间
        Date now = new Date();

        // 创建一个新的User对象
        User user = new User();

        // 设置User对象的属性
        user.setModifyTime(now);
        user.setUserRegtime(now);
        user.setStatus(1);
        user.setNickName(userRegisterParam.getNickName());
        user.setUserMail(userRegisterParam.getUserMail());

        // 解密密码并进行加密
        String decryptPassword = passwordManager.decryptPassword(userRegisterParam.getPassWord());
        user.setLoginPassword(passwordEncoder.encode(decryptPassword));

        // 生成用户ID
        String userId = IdUtil.simpleUUID();
        user.setUserId(userId);

        // 调用userService的save方法保存用户信息到数据库中
        userService.save(user);

        // 构建用户信息对象
        UserInfoInTokenBO userInfoInTokenBO = new UserInfoInTokenBO();
        userInfoInTokenBO.setUserId(user.getUserId());
        userInfoInTokenBO.setSysType(SysTypeEnum.ORDINARY.value());
        userInfoInTokenBO.setIsAdmin(0);
        userInfoInTokenBO.setEnabled(true);

        // 生成并返回TokenInfoVO对象
        return ServerResponseEntity.success(tokenStore.storeAndGetVo(userInfoInTokenBO));
    }



    @PutMapping("/updatePwd")
    @Operation(summary = "修改密码" , description = "修改密码")
    public ServerResponseEntity<Void> updatePwd(@Valid @RequestBody UserRegisterParam userPwdUpdateParam) {
        // 根据昵称查询用户信息
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getNickName, userPwdUpdateParam.getNickName()));

        // 如果用户信息为空，则抛出异常
        if (user == null) {
            // 无法获取用户信息
            throw new YamiShopBindException("无法获取用户信息");
        }

        // 解密新密码
        String decryptPassword = passwordManager.decryptPassword(userPwdUpdateParam.getPassWord());

        // 检查新密码是否为空
        if (StrUtil.isBlank(decryptPassword)) {
            // 新密码不能为空
            throw new YamiShopBindException("新密码不能为空");
        }

        // 加密新密码
        String password = passwordEncoder.encode(decryptPassword);

        // 检查新密码是否与原密码相同
        if (StrUtil.equals(password, user.getLoginPassword())) {
            // 新密码不能与原密码相同
            throw new YamiShopBindException("新密码不能与原密码相同");
        }

        // 更新用户信息
        user.setModifyTime(new Date());
        user.setLoginPassword(password);
        userService.updateById(user);

        return ServerResponseEntity.success();
    }

}
