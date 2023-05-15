package com.yami.shop.api.listener;

import com.google.common.collect.Lists;
import com.yami.shop.bean.app.dto.ShopCartDto;
import com.yami.shop.bean.app.dto.ShopCartItemDiscountDto;
import com.yami.shop.bean.app.dto.ShopCartItemDto;
import com.yami.shop.bean.event.ShopCartEvent;
import com.yami.shop.bean.order.ShopCartEventOrder;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 默认的购物车链进行组装时的操作
 * @author 北易航
 */
@Component("defaultShopCartListener")
public class ShopCartListener {

    /**
     * 将店铺下的所有商品归属到该店铺的购物车当中
     * @param event#getShopCart() 购物车
     * @param event#shopCartItemDtoList 该购物车的商品
     * @return 是否继续组装
     */
    @EventListener(ShopCartEvent.class)
    @Order(ShopCartEventOrder.DEFAULT)
    public void defaultShopCartEvent(ShopCartEvent event) {
        // 获取购物车信息
        ShopCartDto shopCart = event.getShopCartDto();

        // 获取购物车项列表
        List<ShopCartItemDto> shopCartItemDtoList = event.getShopCartItemDtoList();

        // 对数据进行组装
        List<ShopCartItemDiscountDto> shopCartItemDiscountDtoList = Lists.newArrayList();

        // 创建购物车项折扣信息对象
        ShopCartItemDiscountDto shopCartItemDiscountDto = new ShopCartItemDiscountDto();

        // 设置购物车项列表到购物车项折扣信息对象中
        shopCartItemDiscountDto.setShopCartItems(shopCartItemDtoList);

        // 将购物车项折扣信息对象添加到购物车的折扣信息列表中
        shopCartItemDiscountDtoList.add(shopCartItemDiscountDto);

        // 设置购物车的折扣信息列表
        shopCart.setShopCartItemDiscounts(shopCartItemDiscountDtoList);
    }


}
