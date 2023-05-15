

package com.yami.shop.common.serializer.json;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.yami.shop.common.bean.Qiniu;
import com.yami.shop.common.util.ImgUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * @author 北易航
 */
@Component
public class ImgJsonSerializer extends JsonSerializer<String> {

    @Autowired
    private Qiniu qiniu;
    @Autowired
    private ImgUploadUtil imgUploadUtil;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // 检查 value 是否为空或空白字符串
        if (StrUtil.isBlank(value)) {
            // 如果为空或空白字符串，则将空字符串写入 JSON 输出
            gen.writeString(StrUtil.EMPTY);
            return;
        }

        // 将 value 字符串按逗号分隔为图片数组
        String[] imgs = value.split(StrUtil.COMMA);
        StringBuilder sb = new StringBuilder();
        String resourceUrl = "";

        // 根据上传类型选择资源URL
        if (Objects.equals(imgUploadUtil.getUploadType(), 2)) {
            resourceUrl = qiniu.getResourcesUrl();
        } else if (Objects.equals(imgUploadUtil.getUploadType(), 1)) {
            resourceUrl = imgUploadUtil.getResourceUrl();
        }

        // 构建包含完整资源URL的图片路径字符串
        for (String img : imgs) {
            sb.append(resourceUrl).append(img).append(StrUtil.COMMA);
        }
        sb.deleteCharAt(sb.length()-1);

        // 将构建的图片路径字符串写入 JSON 输出
        gen.writeString(sb.toString());
    }

}
