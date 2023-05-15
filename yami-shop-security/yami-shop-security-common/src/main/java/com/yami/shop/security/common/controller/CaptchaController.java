
package com.yami.shop.security.common.controller;

import com.anji.captcha.model.common.RepCodeEnum;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 北易航
 * @date 2022/3/25 17:33
 */
@RestController
@RequestMapping("/captcha")
@Tag(name = "验证码")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @PostMapping({ "/get" })
    public ServerResponseEntity<ResponseModel> get(@RequestBody CaptchaVO captchaVO) {
        return ServerResponseEntity.success(captchaService.get(captchaVO));
    }

    @PostMapping({ "/check" })
    public ServerResponseEntity<ResponseModel> check(@RequestBody CaptchaVO captchaVO) {
        // 声明一个ResponseModel对象
        ResponseModel responseModel;
        try {
            // 调用captchaService的check方法，传入captchaVO进行验证码校验，获取校验结果
            responseModel = captchaService.check(captchaVO);
        } catch (Exception e) {
            // 如果在校验过程中出现异常，返回一个表示错误的ResponseModel对象
            return ServerResponseEntity.success(ResponseModel.errorMsg(RepCodeEnum.API_CAPTCHA_COORDINATE_ERROR));
        }
        // 返回一个表示成功的ServerResponseEntity对象，携带校验结果responseModel
        return ServerResponseEntity.success(responseModel);
    }


}
