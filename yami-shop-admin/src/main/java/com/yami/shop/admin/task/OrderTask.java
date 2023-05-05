package com.yami.shop.admin.task;

import java.util.Date;
import java.util.List;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.yami.shop.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yami.shop.bean.enums.OrderStatus;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.OrderItem;
import com.yami.shop.bean.model.User;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;


/**
 * @author 北易航
 * 定时任务的配置，请查看xxl-job的java配置文件。
 * @see com.yami.shop.admin.config.XxlJobConfig
 */
@Component("orderTask")
public class OrderTask {


    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;
    @Autowired
    private SkuService skuService;

    @XxlJob("cancelOrder")
    public void cancelOrder(){
        // 获取当前时间now
        Date now = new Date();
        logger.info("取消超时未支付订单。。。");
        // 获取30分钟之前未支付的订单
        List<Order> orders = orderService.listOrderAndOrderItems(OrderStatus.UNPAY.value(),DateUtil.offsetMinute(now, -30));
        // 如果查询结果为空，则直接返回；
        if (CollectionUtil.isEmpty(orders)) {
            return;
        }
        // 如果查询结果为空，则直接返回；
        orderService.cancelOrders(orders);
        for (Order order : orders) {
            // 遍历所有被取消的订单，获取其中所有的订单项（OrderItem），
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                // 并删除这些订单项对应的商品（Product）和SKU缓存。
                productService.removeProductCacheByProdId(orderItem.getProdId());
                skuService.removeSkuCacheBySkuId(orderItem.getSkuId(),orderItem.getProdId());
            }
        }
    }

    /**
     * 确认收货
     */
    @XxlJob("confirmOrder")
    public void confirmOrder(){
        Date now = new Date();
        logger.info("系统自动确认收货订单。。。");
        // 获取15天之前未支付的订单
        List<Order> orders = orderService.listOrderAndOrderItems(OrderStatus.CONSIGNMENT.value(),DateUtil.offsetDay(now, -15));
        if (CollectionUtil.isEmpty(orders)) {
            return;
        }
        orderService.confirmOrder(orders);
        for (Order order : orders) {
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                productService.removeProductCacheByProdId(orderItem.getProdId());
                skuService.removeSkuCacheBySkuId(orderItem.getSkuId(),orderItem.getProdId());
            }
        }
    }

}
