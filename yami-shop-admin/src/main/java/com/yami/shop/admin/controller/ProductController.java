package com.yami.shop.admin.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.Product;
import com.yami.shop.bean.model.Sku;
import com.yami.shop.bean.param.ProductParam;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Json;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.admin.util.SecurityUtils;
import com.yami.shop.service.BasketService;
import com.yami.shop.service.ProdTagReferenceService;
import com.yami.shop.service.ProductService;
import com.yami.shop.service.SkuService;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * 商品列表、商品发布controller
 *
 * @author 北易航
 */
@RestController
@RequestMapping("/prod/prod")
public class ProductController {

    @Autowired
    private ProductService productService;


    @Autowired
    private SkuService skuService;

    @Autowired
    private ProdTagReferenceService prodTagReferenceService;

    @Autowired
    private BasketService basketService;

    /**
     * 分页获取商品信息
     */
    @GetMapping("/page")
    @PreAuthorize("@pms.hasPermission('prod:prod:page')")
    public ServerResponseEntity<IPage<Product>> page(ProductParam product, PageParam<Product> page) {
        IPage<Product> products = productService.page(page,
                new LambdaQueryWrapper<Product>()
                        // 判断查询条件 product.getProdName() 是否不为空，如果不为空则添加一个模糊查询条件
                        .like(StrUtil.isNotBlank(product.getProdName()), Product::getProdName, product.getProdName())
                        // 查询条件为当前登录用户的店铺 ID
                        .eq(Product::getShopId, SecurityUtils.getSysUser().getShopId())
                        // 判断查询条件 product.getStatus() 是否不为空，如果不为空则添加一个等于查询条件
                        .eq(product.getStatus() != null, Product::getStatus, product.getStatus())
                        // 按照商品的上架时间倒序排列
                        .orderByDesc(Product::getPutawayTime));
        return ServerResponseEntity.success(products);
    }

    /**
     * 获取信息
     */
    @GetMapping("/info/{prodId}")
    @PreAuthorize("@pms.hasPermission('prod:prod:info')")
    public ServerResponseEntity<Product> info(@PathVariable("prodId") Long prodId) {
        // 拿到商品信息id信息
        Product prod = productService.getProductByProdId(prodId);
        // 判断商品所属店铺是否与当前用户所属店铺一致，若不一致则抛出异常
        if (!Objects.equals(prod.getShopId(), SecurityUtils.getSysUser().getShopId())) {
            throw new YamiShopBindException("没有权限获取该商品规格信息");
        }
        // 获取商品的sku列表
        List<Sku> skuList = skuService.listByProdId(prodId);
        prod.setSkuList(skuList);

        //获取分组标签
        List<Long> listTagId = prodTagReferenceService.listTagIdByProdId(prodId);
        prod.setTagList(listTagId);
        return ServerResponseEntity.success(prod);
    }

    /**
     * 保存
     */
    @PostMapping
    @PreAuthorize("@pms.hasPermission('prod:prod:save')")
    public ServerResponseEntity<String> save(@Valid @RequestBody ProductParam productParam) {
        // 校验参数是否符合要求
        checkParam(productParam);
        // 将参数对象转化为商品实体对象
        Product product = BeanUtil.copyProperties(productParam, Product.class);
        // 设置商品的配送模式为JSON格式字符串
        product.setDeliveryMode(Json.toJsonString(productParam.getDeliveryModeVo()));
        // 设置商品的店铺ID为当前登录用户所属店铺的ID
        product.setShopId(SecurityUtils.getSysUser().getShopId());
        // 设置商品的更新时间为当前时间
        product.setUpdateTime(new Date());
        // 如果商品状态为上架状态，则设置上架时间为当前时间
        if (product.getStatus() == 1) {
            product.setPutawayTime(new Date());
        }
        // 设置商品的创建时间为当前时间
        product.setCreateTime(new Date());
        // 调用商品服务层的保存商品方法
        productService.saveProduct(product);
        // 返回保存成功的响应实体对象
        return ServerResponseEntity.success();
    }

    /**
     * 修改
     */
    @PutMapping
    @PreAuthorize("@pms.hasPermission('prod:prod:update')")
    public ServerResponseEntity<String> update(@Valid @RequestBody ProductParam productParam) {
        // 校验参数是否符合要求
        checkParam(productParam);
        // 获取数据库中该商品的信息
        Product dbProduct = productService.getProductByProdId(productParam.getProdId());
        // 判断该商品是否属于本店铺，若不是则无法修改
        if (!Objects.equals(dbProduct.getShopId(), SecurityUtils.getSysUser().getShopId())) {
            return ServerResponseEntity.showFailMsg("无法修改非本店铺商品信息");
        }
        // 获取该商品的所有SKU信息
        List<Sku> dbSkus = skuService.listByProdId(dbProduct.getProdId());

        // 将前端传来的参数拷贝到Product对象中
        Product product = BeanUtil.copyProperties(productParam, Product.class);
        // 将前端传来的DeliveryModeVo对象序列化为json字符串并设置到Product对象中
        product.setDeliveryMode(Json.toJsonString(productParam.getDeliveryModeVo()));
        // 设置更新时间为当前时间
        product.setUpdateTime(new Date());
        // 如果原商品状态为下架或新商品状态为上架，则设置上架时间为当前时间
        if (dbProduct.getStatus() == 0 || productParam.getStatus() == 1) {
            product.setPutawayTime(new Date());
        }
        // 将原商品的SKU信息设置到Product对象中
        dbProduct.setSkuList(dbSkus);
        // 调用productService中的方法更新商品信息
        productService.updateProduct(product, dbProduct);
        // 根据商品ID获取所有购物车中包含该商品的用户ID
        List<String> userIds = basketService.listUserIdByProdId(product.getProdId());
        // 根据用户ID移除购物车中该商品的缓存
        for (String userId : userIds) {
            basketService.removeShopCartItemsCacheByUserId(userId);
        }
        // 移除所有SKU的缓存
        for (Sku sku : dbSkus) {
            skuService.removeSkuCacheBySkuId(sku.getSkuId(), sku.getProdId());
        }
        // 返回更新成功的响应信息
        return ServerResponseEntity.success();
    }

    /**
     * 删除
     */
    public ServerResponseEntity<Void> delete(Long prodId) {
        // 获取数据库中该商品的信息
        Product dbProduct = productService.getProductByProdId(prodId);
        // 判断该商品是否属于本店铺，若不是则无法删除
        if (!Objects.equals(dbProduct.getShopId(), SecurityUtils.getSysUser().getShopId())) {
            throw new YamiShopBindException("无法获取非本店铺商品信息");
        }
        // 拿到商品SKU信息
        List<Sku> dbSkus = skuService.listByProdId(dbProduct.getProdId());
        // 删除商品
        productService.removeProductByProdId(prodId);
        // 根据SKU移除购物车中该商品的缓存
        for (Sku sku : dbSkus) {
            skuService.removeSkuCacheBySkuId(sku.getSkuId(), sku.getProdId());
        }
        // 根据商品ID获取所有购物车中包含该商品的用户ID
        List<String> userIds = basketService.listUserIdByProdId(prodId);
        // 根据用户id移除购物车中该商品的缓存
        for (String userId : userIds) {
            basketService.removeShopCartItemsCacheByUserId(userId);
        }
        // 返回删除成功的响应信息
        return ServerResponseEntity.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping
    @PreAuthorize("@pms.hasPermission('prod:prod:delete')")
    public ServerResponseEntity<Void> batchDelete(@RequestBody Long[] prodIds) {
        // 遍历商品id数组，逐个删除
        for (Long prodId : prodIds) {
            delete(prodId);
        }
        return ServerResponseEntity.success();
    }

    /**
     * 更新商品上下架状态
     */
    @PutMapping("/prodStatus")
    @PreAuthorize("@pms.hasPermission('prod:prod:status')")
    public ServerResponseEntity<Void> shopStatus(@RequestParam Long prodId, @RequestParam Integer prodStatus) {
        // 构建一个商品对象，设置商品id和更新后的状态值
        Product product = new Product();
        product.setProdId(prodId);
        product.setStatus(prodStatus);
        // 如果商品是上架状态，则设置上架时间为当前时间
        if (prodStatus == 1) {
            product.setPutawayTime(new Date());
        }
        // 更新商品上架状态
        productService.updateById(product);
        // 移除商品缓存
        productService.removeProductCacheByProdId(prodId);
        // 获取购物车中包含该商品的用户id列表
        List<String> userIds = basketService.listUserIdByProdId(prodId);
        // 遍历购物车中的用户id列表，依次移除对应用户的购物车缓存
        for (String userId : userIds) {
            basketService.removeShopCartItemsCacheByUserId(userId);
        }
        // 返回成功响应结果
        return ServerResponseEntity.success();
    }

    private void checkParam(ProductParam productParam) {
        // 验证TagList是否为空，是则提示
        if (CollectionUtil.isEmpty(productParam.getTagList())) {
            throw new YamiShopBindException("请选择产品分组");
        }
        // 校验是否选择了配送方式
        Product.DeliveryModeVO deliveryMode = productParam.getDeliveryModeVo();
        boolean hasDeliverMode = deliveryMode != null
                && (deliveryMode.getHasShopDelivery() || deliveryMode.getHasUserPickUp());
        if (!hasDeliverMode) {
            throw new YamiShopBindException("请选择配送方式");
        }
        // 校验商品规格是否全部未启用
        List<Sku> skuList = productParam.getSkuList();
        boolean isAllUnUse = true;
        for (Sku sku : skuList) {
            if (sku.getStatus() == 1) {
                isAllUnUse = false;
            }
        }
        if (isAllUnUse) {
            throw new YamiShopBindException("至少要启用一种商品规格");
        }
    }
}
