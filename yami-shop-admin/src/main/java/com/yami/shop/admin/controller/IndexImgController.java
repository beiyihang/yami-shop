package com.yami.shop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.IndexImg;
import com.yami.shop.bean.model.Product;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.admin.util.SecurityUtils;
import com.yami.shop.service.IndexImgService;
import com.yami.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.Objects;

/**
 * 首页轮播图
 * @author 北易航
 */
@RestController
@RequestMapping("/admin/indexImg")
public class IndexImgController {

    @Autowired
    private IndexImgService indexImgService;

    @Autowired
    private ProductService productService;


    /**
     * 分页获取
     */
    @GetMapping("/page")
    @PreAuthorize("@pms.hasPermission('admin:indexImg:page')")
    public ServerResponseEntity<IPage<IndexImg>> page(IndexImg indexImg, PageParam<IndexImg> page) {
        // 调用service层的page方法获取首页轮播图分页数据
        IPage<IndexImg> indexImgPage = indexImgService.page(page,
                // 获取数据 并且进行筛选
                new LambdaQueryWrapper<IndexImg>()
                        .eq(indexImg.getStatus() != null, IndexImg::getStatus, indexImg.getStatus())
                        .orderByAsc(IndexImg::getSeq));
        // 将分页数据封装成响应实体并返回
        return ServerResponseEntity.success(indexImgPage);
    }

    /**
     * 获取信息
     */
    @GetMapping("/info/{imgId}")
    @PreAuthorize("@pms.hasPermission('admin:indexImg:info')")
    public ServerResponseEntity<IndexImg> info(@PathVariable("imgId") Long imgId) {
        // 获取当前用户所在商店的ID
        Long shopId = SecurityUtils.getSysUser().getShopId();
        // 查询符合条件的首页轮播图记录
        IndexImg indexImg = indexImgService.getOne(new LambdaQueryWrapper<IndexImg>().eq(IndexImg::getShopId, shopId).eq(IndexImg::getImgId, imgId));
        // 如果轮播图有关联商品，则查询关联的商品信息
        if (Objects.nonNull(indexImg.getRelation())) {
            Product product = productService.getProductByProdId(indexImg.getRelation());
            indexImg.setPic(product.getPic());
            indexImg.setProdName(product.getProdName());
        }
        // 返回查询到的首页轮播图信息
        return ServerResponseEntity.success(indexImg);
    }

    /**
     * 保存
     */
    @PostMapping
    @PreAuthorize("@pms.hasPermission('admin:indexImg:save')")
    public ServerResponseEntity<Void> save(@RequestBody @Valid IndexImg indexImg) {
        // 获取当前用户所在商店的ID
        Long shopId = SecurityUtils.getSysUser().getShopId();
        // 设置商店id
        indexImg.setShopId(shopId);
        // 设置上传时间
        indexImg.setUploadTime(new Date());
        // 检查商品状态
        checkProdStatus(indexImg);
        // 进行更新
        indexImgService.save(indexImg);
        // 清除缓存
        indexImgService.removeIndexImgCache();
        return ServerResponseEntity.success();
    }

    /**
     * 修改
     */
    @PutMapping
    @PreAuthorize("@pms.hasPermission('admin:indexImg:update')")
    public ServerResponseEntity<Void> update(@RequestBody @Valid IndexImg indexImg) {
        checkProdStatus(indexImg);
        // 添加或者更新轮播图
        indexImgService.saveOrUpdate(indexImg);
        // 清除缓存
        indexImgService.removeIndexImgCache();
        return ServerResponseEntity.success();
    }

    /**
     * 删除
     */
    @DeleteMapping
    @PreAuthorize("@pms.hasPermission('admin:indexImg:delete')")
    public ServerResponseEntity<Void> delete(@RequestBody Long[] ids) {
        // 根据图片id进行删除
        indexImgService.deleteIndexImgByIds(ids);
        indexImgService.removeIndexImgCache();
        return ServerResponseEntity.success();
    }

    private void checkProdStatus(IndexImg indexImg) {
        // 如果索引图片类型不为0，则不需要进行商品状态检查，直接返回
        if (!Objects.equals(indexImg.getType(), 0)) {
            return;
        }
        // 如果关联商品为空，则抛出异常
        if (Objects.isNull(indexImg.getRelation())) {
            throw new YamiShopBindException("请选择商品");
        }
        // 根据关联商品ID查询商品信息
        Product product = productService.getById(indexImg.getRelation());
        // 如果商品信息不存在，则抛出异常
        if (Objects.isNull(product)) {
            throw new YamiShopBindException("商品信息不存在");
        }
        // 如果商品未上架，则抛出异常
        if (!Objects.equals(product.getStatus(), 1)) {
            throw new YamiShopBindException("该商品未上架，请选择别的商品");
        }
    }
}
