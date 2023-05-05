package com.yami.shop.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.User;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;


/**
 * @author 北易航
 */
@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页获取
     */
    @GetMapping("/page")
    @PreAuthorize("@pms.hasPermission('admin:user:page')")
    public ServerResponseEntity<IPage<User>> page(User user,PageParam<User> page) {
        // 调用userService的page方法进行分页查询，使用LambdaQueryWrapper构建查询条件
        IPage<User> userPage = userService.page(page, new LambdaQueryWrapper<User>()
                // 模糊查询昵称
                .like(StrUtil.isNotBlank(user.getNickName()), User::getNickName, user.getNickName())
                // 筛选状态
                .eq(user.getStatus() != null, User::getStatus, user.getStatus()));
        // 对查询结果进行处理，将昵称为null的用户昵称改为""
        for (User userResult : userPage.getRecords()) {
            userResult.setNickName(userResult.getNickName() == null ? "" : userResult.getNickName());
        }
        // 返回成功响应
        return ServerResponseEntity.success(userPage);
    }


    /**
     * 获取信息
     */
    @GetMapping("/info/{userId}")
    @PreAuthorize("@pms.hasPermission('admin:user:info')")
    public ServerResponseEntity<User> info(@PathVariable("userId") String userId) {
        User user = userService.getById(userId);
        user.setNickName(user.getNickName() == null ? "" : user.getNickName());
        return ServerResponseEntity.success(user);
    }

    /**
     * 修改
     */
    @PutMapping
    @PreAuthorize("@pms.hasPermission('admin:user:update')")
    public ServerResponseEntity<Void> update(@RequestBody User user) {
        user.setModifyTime(new Date());
        // 判断用户的昵称是否为空，如果为空，则设置为空字符串。
        user.setNickName(user.getNickName() == null ? "" : user.getNickName());
        userService.updateById(user);
        return ServerResponseEntity.success();
    }

    /**
     * 删除
     */
    @DeleteMapping
    @PreAuthorize("@pms.hasPermission('admin:user:delete')")
    public ServerResponseEntity<Void> delete(@RequestBody String[] userIds) {
        userService.removeByIds(Arrays.asList(userIds));
        return ServerResponseEntity.success();
    }
}
