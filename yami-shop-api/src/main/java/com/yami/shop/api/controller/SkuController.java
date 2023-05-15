package com.yami.shop.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.bean.app.dto.SkuDto;
import com.yami.shop.bean.model.Sku;
import com.yami.shop.service.SkuService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import cn.hutool.core.bean.BeanUtil;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 北易航
 */
@RestController
@RequestMapping("/sku")
@Tag(name = "sku规格接口")
@AllArgsConstructor
public class SkuController {

    private final SkuService skuService;


    @GetMapping("/getSkuList")
    @Operation(summary = "通过prodId获取商品全部规格列表" , description = "通过prodId获取商品全部规格列表")
    @Parameter(name = "prodId", description = "商品id" )
    public ServerResponseEntity<List<SkuDto>> getSkuListByProdId(Long prodId) {
        // 查询符合条件的SKU列表
        List<Sku> skus = skuService.list(new LambdaQueryWrapper<Sku>()
                .eq(Sku::getStatus, 1)            // SKU状态为1
                .eq(Sku::getIsDelete, 0)          // SKU未删除
                .eq(Sku::getProdId, prodId)       // SKU关联的商品ID为prodId
        );

        // 将SKU列表转换为DTO列表
        List<SkuDto> skuDtoList = BeanUtil.copyToList(skus, SkuDto.class);

        // 返回包含SKU列表的成功响应
        return ServerResponseEntity.success(skuDtoList);
    }

}
