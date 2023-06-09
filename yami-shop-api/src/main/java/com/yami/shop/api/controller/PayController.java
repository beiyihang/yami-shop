package com.yami.shop.api.controller;

import com.yami.shop.bean.app.param.PayParam;
import com.yami.shop.bean.pay.PayInfoDto;
import com.yami.shop.security.api.model.YamiUser;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.PayService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 北易航
 */
@RestController
@RequestMapping("/p/order")
@Tag(name = "订单接口")
@AllArgsConstructor
public class PayController {

    private final PayService payService;

    /**
     * 支付接口
     */
    @PostMapping("/pay")
    @Operation(summary = "根据订单号进行支付" , description = "根据订单号进行支付")
    public ServerResponseEntity<Void> pay(@RequestBody PayParam payParam) {
        // 获取当前登录用户信息
        YamiUser user = SecurityUtils.getUser();
        String userId = user.getUserId();

        // 调用支付服务进行支付
        PayInfoDto payInfo = payService.pay(userId, payParam);

        // 标记支付成功
        payService.paySuccess(payInfo.getPayNo(), "");

        // 返回支付成功的响应
        return ServerResponseEntity.success();
    }


    /**
     * 普通支付接口
     */
    @PostMapping("/normalPay")
    @Operation(summary = "根据订单号进行支付" , description = "根据订单号进行支付")
    public ServerResponseEntity<Boolean> normalPay(@RequestBody PayParam payParam) {
        // 获取当前登录用户信息
        YamiUser user = SecurityUtils.getUser();
        String userId = user.getUserId();

        // 调用支付服务进行支付
        PayInfoDto pay = payService.pay(userId, payParam);

        // 根据内部订单号更新订单结算状态
        payService.paySuccess(pay.getPayNo(), "");

        // 返回支付成功的响应
        return ServerResponseEntity.success(true);
    }

}
