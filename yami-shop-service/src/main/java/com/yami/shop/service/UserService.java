

package com.yami.shop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.shop.bean.model.User;
import com.yami.shop.bean.param.UserRegisterParam;

/**
 *
 * @author 北易航 on 2018/09/11.
 */
public interface UserService extends IService<User> {
    /**
     * 根据用户id获取用户信息
     * @param userId
     * @return
     */
    User getUserByUserId(String userId);

    /**
     * 校验验证码
     * @param userRegisterParam
     * @param checkRegisterSmsFlag
     */
    void validate(UserRegisterParam userRegisterParam, String checkRegisterSmsFlag);
}
