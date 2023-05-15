package com.yami.shop.api.listener;

import com.yami.shop.bean.app.dto.ShopCartItemDto;
import com.yami.shop.bean.app.dto.ShopCartOrderDto;
import com.yami.shop.bean.app.param.OrderParam;
import com.yami.shop.bean.event.ConfirmOrderEvent;
import com.yami.shop.bean.model.Product;
import com.yami.shop.bean.model.Sku;
import com.yami.shop.bean.model.UserAddr;
import com.yami.shop.bean.order.ConfirmOrderOrder;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.util.Arith;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.ProductService;
import com.yami.shop.service.SkuService;
import com.yami.shop.service.TransportManagerService;
import com.yami.shop.service.UserAddrService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 确认订单信息时的默认操作
 * @author 北易航
 */
@Component("defaultConfirmOrderListener")
@AllArgsConstructor
public class ConfirmOrderListener {

    private final UserAddrService userAddrService;

    private final TransportManagerService transportManagerService;

    private final ProductService productService;

    private final SkuService skuService;

    /**
     * 计算订单金额
     */
    @EventListener(ConfirmOrderEvent.class)
    @Order(ConfirmOrderOrder.DEFAULT)
    public void defaultConfirmOrderEvent(ConfirmOrderEvent event) {
        // 获取购物车订单信息
        ShopCartOrderDto shopCartOrderDto = event.getShopCartOrderDto();

        // 获取订单参数
        OrderParam orderParam = event.getOrderParam();

        // 获取当前用户的ID
        String userId = SecurityUtils.getUser().getUserId();

        // 获取订单的地址信息
        UserAddr userAddr = userAddrService.getUserAddrByUserId(orderParam.getAddrId(), userId);

        // 定义总金额、总数量和运费
        double total = 0.0;
        int totalCount = 0;
        double transfee = 0.0;

        // 遍历购物车项
        for (ShopCartItemDto shopCartItem : event.getShopCartItems()) {
            // 获取商品信息
            Product product = productService.getProductByProdId(shopCartItem.getProdId());
            // 获取sku信息
            Sku sku = skuService.getSkuBySkuId(shopCartItem.getSkuId());

            // 检查商品和sku是否存在
            if (product == null || sku == null) {
                throw new YamiShopBindException("购物车包含无法识别的商品");
            }

            // 检查商品和sku的状态
            if (product.getStatus() != 1 || sku.getStatus() != 1) {
                throw new YamiShopBindException("商品[" + sku.getProdName() + "]已下架");
            }

            // 计算总数量和总金额
            totalCount = shopCartItem.getProdCount() + totalCount;
            total = Arith.add(shopCartItem.getProductTotalAmount(), total);

            // 如果用户地址不为空，则计算运费
            if (userAddr != null) {
                transfee = Arith.add(transfee, transportManagerService.calculateTransfee(shopCartItem, userAddr));
            }

            // 设置实际总金额和运费到购物车订单信息中
            shopCartItem.setActualTotal(shopCartItem.getProductTotalAmount());
            shopCartOrderDto.setActualTotal(Arith.add(total, transfee));
            shopCartOrderDto.setTotal(total);
            shopCartOrderDto.setTotalCount(totalCount);
            shopCartOrderDto.setTransfee(transfee);
        }
    }

}
