
package com.yami.shop.security.common.config;

import com.anji.captcha.model.common.CaptchaTypeEnum;
import com.anji.captcha.model.common.Const;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import com.anji.captcha.util.ImageUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileCopyUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 这里把验证码的底图存入redis中，如果报获取验证码失败找管理员什么的可以看下redis的情况
 * @author 北易航
 * @date 2022/3/25 17:33
 */
@Configuration
public class CaptchaConfig {

    @Bean
    public CaptchaService captchaService() {
        Properties config = new Properties();
        config.put(Const.CAPTCHA_CACHETYPE, "redis");
        config.put(Const.CAPTCHA_WATER_MARK, "");
        // 滑动验证
        config.put(Const.CAPTCHA_TYPE, CaptchaTypeEnum.BLOCKPUZZLE.getCodeValue());
        config.put(Const.CAPTCHA_INIT_ORIGINAL, "true");
        initializeBaseMap();
        return CaptchaServiceFactory.getInstance(config);
    }

    private static void initializeBaseMap() {
        ImageUtils.cacheBootImage(getResourcesImagesFile("classpath:captcha" + "/original/*.png"), getResourcesImagesFile("classpath:captcha" + "/slidingBlock/*.png"), Collections.emptyMap());
    }

    public static Map<String, String> getResourcesImagesFile(String path) {
        // 创建一个存储图片文件名和Base64编码的映射的Map
        Map<String, String> imgMap = new HashMap<>(16);
        // 创建一个路径匹配的资源模式解析器
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            // 根据指定的资源路径获取匹配的资源数组
            Resource[] resources = resolver.getResources(path);
            // 遍历资源数组
            for (Resource resource : resources) {
                // 读取资源的字节数据
                byte[] bytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
                // 将字节数据转换为Base64编码的字符串
                String string = Base64Utils.encodeToString(bytes);
                // 获取资源的文件名
                String filename = resource.getFilename();
                // 将文件名和Base64编码的字符串存入Map中
                imgMap.put(filename, string);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imgMap;
    }

}
