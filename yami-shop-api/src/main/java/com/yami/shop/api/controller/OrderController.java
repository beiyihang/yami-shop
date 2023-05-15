package com.yami.shop.api.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.yami.shop.bean.app.dto.*;
import com.yami.shop.bean.app.param.OrderParam;
import com.yami.shop.bean.app.param.OrderShopParam;
import com.yami.shop.bean.app.param.SubmitOrderParam;
import com.yami.shop.bean.event.ConfirmOrderEvent;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.UserAddr;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.util.Arith;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author 北易航
 */
@RestController
@RequestMapping("/p/order")
@Tag(name = "订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private SkuService skuService;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserAddrService userAddrService;
    @Autowired
    private BasketService basketService;
    @Autowired
    private ApplicationContext applicationContext;


    /**
     * 生成订单
     */
    @PostMapping("/confirm")
    @Operation(summary = "结算，生成订单信息" , description = "传入下单所需要的参数进行下单")
    public ServerResponseEntity<ShopCartOrderMergerDto> confirm(@Valid @RequestBody OrderParam orderParam) {
        String userId = SecurityUtils.getUser().getUserId();
        // 订单的地址信息
        UserAddr userAddr = userAddrService.getUserAddrByUserId(orderParam.getAddrId(), userId);
        UserAddrDto userAddrDto = BeanUtil.copyProperties(userAddr, UserAddrDto.class);

        // 组装获取用户提交的购物车商品项
        List<ShopCartItemDto> shopCartItems = basketService.getShopCartItemsByOrderItems(orderParam.getBasketIds(),orderParam.getOrderItem(),userId);

        if (CollectionUtil.isEmpty(shopCartItems)) {
            throw new YamiShopBindException("请选择您需要的商品加入购物车");
        }

        // 根据店铺组装购车中的商品信息，返回每个店铺中的购物车商品信息
        List<ShopCartDto> shopCarts = basketService.getShopCarts(shopCartItems);

        // 将要返回给前端的完整的订单信息
        ShopCartOrderMergerDto shopCartOrderMergerDto = new ShopCartOrderMergerDto();

        shopCartOrderMergerDto.setUserAddr(userAddrDto);

        // 所有店铺的订单信息
        List<ShopCartOrderDto> shopCartOrders = new ArrayList<>();

        double actualTotal = 0.0;
        double total = 0.0;
        int totalCount = 0;
        double orderReduce = 0.0;
        for (ShopCartDto shopCart : shopCarts) {

            // 每个店铺的订单信息
            ShopCartOrderDto shopCartOrder = new ShopCartOrderDto();
            shopCartOrder.setShopId(shopCart.getShopId());
            shopCartOrder.setShopName(shopCart.getShopName());


            List<ShopCartItemDiscountDto> shopCartItemDiscounts = shopCart.getShopCartItemDiscounts();

            // 创建一个空列表，用于存储店铺中的所有商品项信息
            List<ShopCartItemDto> shopAllShopCartItems = new ArrayList<>();

            // 遍历店铺的所有折扣商品项
            for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartItemDiscounts) {
                // 获取该折扣商品项中的所有商品项
                List<ShopCartItemDto> discountShopCartItems = shopCartItemDiscount.getShopCartItems();

                // 将该折扣商品项中的所有商品项添加到店铺的所有商品项列表中
                shopAllShopCartItems.addAll(discountShopCartItems);
            }

            // 设置店铺的折扣商品项列表
            shopCartOrder.setShopCartItemDiscounts(shopCartItemDiscounts);

            // 发布确认订单事件，传递店铺的订单、订单参数和店铺的所有商品项
            applicationContext.publishEvent(new ConfirmOrderEvent(shopCartOrder, orderParam, shopAllShopCartItems));

            // 计算实际总价、总价、总数量和订单优惠金额
            actualTotal = Arith.add(actualTotal, shopCartOrder.getActualTotal());
            total = Arith.add(total, shopCartOrder.getTotal());
            totalCount = totalCount + shopCartOrder.getTotalCount();
            orderReduce = Arith.add(orderReduce, shopCartOrder.getShopReduce());

            // 将店铺订单添加到店铺订单列表中
            shopCartOrders.add(shopCartOrder);


        }

        // 设置店铺订单合并对象的实际总价、总价、总数量、店铺订单列表和订单优惠金额
        shopCartOrderMergerDto.setActualTotal(actualTotal);
        shopCartOrderMergerDto.setTotal(total);
        shopCartOrderMergerDto.setTotalCount(totalCount);
        shopCartOrderMergerDto.setShopCartOrders(shopCartOrders);
        shopCartOrderMergerDto.setOrderReduce(orderReduce);

        // 将店铺订单合并对象存入缓存，并获取更新后的合并对象
        shopCartOrderMergerDto = orderService.putConfirmOrderCache(userId, shopCartOrderMergerDto);

        // 返回成功响应，携带更新后的店铺订单合并对象
        return ServerResponseEntity.success(shopCartOrderMergerDto);

    }

    /**
     * 购物车/立即购买  提交订单,根据店铺拆单
     */
    @PostMapping("/submit")
    @Operation(summary = "提交订单，返回支付流水号" , description = "根据传入的参数判断是否为购物车提交订单，同时对购物车进行删除，用户开始进行支付")
    public ServerResponseEntity<OrderNumbersDto> submitOrders(@Valid @RequestBody SubmitOrderParam submitOrderParam) {
        // 获取当前用户的用户ID
        String userId = SecurityUtils.getUser().getUserId();

        // 从缓存中获取店铺订单合并对象
        ShopCartOrderMergerDto mergerOrder = orderService.getConfirmOrderCache(userId);

        // 如果合并对象为空，抛出订单绑定异常，提示订单已过期
        if (mergerOrder == null) {
            throw new YamiShopBindException("订单已过期，请重新下单");
        }

        // 获取提交订单参数中的店铺订单参数列表
        List<OrderShopParam> orderShopParams = submitOrderParam.getOrderShopParam();

        // 获取合并对象中的店铺订单列表
        List<ShopCartOrderDto> shopCartOrders = mergerOrder.getShopCartOrders();
        // 设置备注
        if (CollectionUtil.isNotEmpty(orderShopParams)) {
            for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
                for (OrderShopParam orderShopParam : orderShopParams) {

                    // 如果店铺订单的店铺ID与订单参数的店铺ID相同，设置备注
                    if (Objects.equals(shopCartOrder.getShopId(), orderShopParam.getShopId())) {
                        shopCartOrder.setRemarks(orderShopParam.getRemarks());
                    }
                }
            }
        }

        // 提交订单，获取订单列表
        List<Order> orders = orderService.submit(userId, mergerOrder);

        // 用于存储订单号的字符串构建器
        StringBuilder orderNumbers = new StringBuilder();

        // 遍历订单列表，获取订单号并拼接到字符串构建器中
        for (Order order : orders) {
            orderNumbers.append(order.getOrderNumber()).append(",");
        }
        // 删除最后一个逗号
        orderNumbers.deleteCharAt(orderNumbers.length() - 1);

        // 用于判断是否为购物车订单的标志
        boolean isShopCartOrder = false;

        // 移除缓存
        for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
            for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartOrder.getShopCartItemDiscounts()) {
                for (ShopCartItemDto shopCartItem : shopCartItemDiscount.getShopCartItems()) {
                    Long basketId = shopCartItem.getBasketId();
                    // 如果购物车ID不为空且不为0，表示为购物车订单
                    if (basketId != null && basketId != 0) {
                        isShopCartOrder = true;
                    }
                    // 移除SKU缓存
                    skuService.removeSkuCacheBySkuId(shopCartItem.getSkuId(), shopCartItem.getProdId());
                    // 移除产品缓存
                    productService.removeProductCacheByProdId(shopCartItem.getProdId());
                }
            }
        }

        // 购物车提交订单时(即有购物车ID时)
        if (isShopCartOrder) {
            basketService.removeShopCartItemsCacheByUserId(userId);
        }
        orderService.removeConfirmOrderCache(userId);
        return ServerResponseEntity.success(new OrderNumbersDto(orderNumbers.toString()));
    }

}
