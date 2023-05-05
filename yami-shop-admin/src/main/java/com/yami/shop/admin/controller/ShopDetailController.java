package com.yami.shop.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.ShopDetail;
import com.yami.shop.bean.param.ShopDetailParam;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.admin.util.SecurityUtils;
import com.yami.shop.service.ShopDetailService;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;



/**
 * 商品详情
 * @author 北易航
 */
@RestController
@RequestMapping("/shop/shopDetail")
public class ShopDetailController {

    @Autowired
    private ShopDetailService shopDetailService;


	/**
	 * 修改分销开关
	 */
	@PutMapping("/isDistribution")
	public ServerResponseEntity<Void> updateIsDistribution(@RequestParam Integer isDistribution){
		// 创建ShopDetail实体类，并设置店铺id和是否支持配送状态
		ShopDetail shopDetail=new ShopDetail();
		shopDetail.setShopId(SecurityUtils.getSysUser().getShopId());
		shopDetail.setIsDistribution(isDistribution);
		// 调用Service更新店铺信息
		shopDetailService.updateById(shopDetail);
		// 更新完成后删除缓存
		shopDetailService.removeShopDetailCacheByShopId(shopDetail.getShopId());
		return ServerResponseEntity.success();
	}
	/**
	 * 获取信息
	 */
	@GetMapping("/info")
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:info')")
	public ServerResponseEntity<ShopDetail> info(){
		ShopDetail shopDetail = shopDetailService.getShopDetailByShopId(SecurityUtils.getSysUser().getShopId());
		return ServerResponseEntity.success(shopDetail);
	}


	/**
	 * 分页获取
	 */
    @GetMapping("/page")
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:page')")
	public ServerResponseEntity<IPage<ShopDetail>> page(ShopDetail shopDetail,PageParam<ShopDetail> page){
		IPage<ShopDetail> shopDetails = shopDetailService.page(page,
				new LambdaQueryWrapper<ShopDetail>()
						.like(StrUtil.isNotBlank(shopDetail.getShopName()),ShopDetail::getShopName,shopDetail.getShopName())
						.orderByDesc(ShopDetail::getShopId));
		return ServerResponseEntity.success(shopDetails);
	}

	/**
	 * 获取信息
	 */
	@GetMapping("/info/{shopId}")
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:info')")
	public ServerResponseEntity<ShopDetail> info(@PathVariable("shopId") Long shopId){
		ShopDetail shopDetail = shopDetailService.getShopDetailByShopId(shopId);
		// 店铺图片
		return ServerResponseEntity.success(shopDetail);
	}

	/**
	 * 保存
	 */
	@PostMapping
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:save')")
	public ServerResponseEntity<Void> save(@Valid ShopDetailParam shopDetailParam){
		// 使用 BeanUtil 工具类将 ShopDetailParam 对象的属性拷贝到 ShopDetail 对象中
		ShopDetail shopDetail = BeanUtil.copyProperties(shopDetailParam, ShopDetail.class);
		// 设置创建时间为当前时间
		shopDetail.setCreateTime(new Date());
		// 设置店铺状态为“1”（正常状态）
		shopDetail.setShopStatus(1);
		// 调用 service 的 save 方法保存店铺详情信息
		shopDetailService.save(shopDetail);
		return ServerResponseEntity.success();
	}

	/**
	 * 修改
	 */
	@PutMapping
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:update')")
	public ServerResponseEntity<Void> update(@Valid ShopDetailParam shopDetailParam){
		// 从数据库拿到对应的数据
		ShopDetail daShopDetail = shopDetailService.getShopDetailByShopId(shopDetailParam.getShopId());
		// 使用 BeanUtil 工具类将 ShopDetailParam 对象的属性拷贝到 ShopDetail 对象中
		ShopDetail shopDetail = BeanUtil.copyProperties(shopDetailParam, ShopDetail.class);
		// 设置创建时间为当前时间
		shopDetail.setUpdateTime(new Date());
		// 更新
		shopDetailService.updateShopDetail(shopDetail,daShopDetail);
		return ServerResponseEntity.success();
	}

	/**
	 * 删除
	 */
	@DeleteMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:delete')")
	public ServerResponseEntity<Void> delete(@PathVariable Long id){
		shopDetailService.deleteShopDetailByShopId(id);
		return ServerResponseEntity.success();
	}

	/**
	 * 更新店铺状态
	 */
	@PutMapping("/shopStatus")
	@PreAuthorize("@pms.hasPermission('shop:shopDetail:shopStatus')")
	public ServerResponseEntity<Void> shopStatus(@RequestParam Long shopId,@RequestParam Integer shopStatus){
		ShopDetail shopDetail = new ShopDetail();
		shopDetail.setShopId(shopId);
		shopDetail.setShopStatus(shopStatus);
		shopDetailService.updateById(shopDetail);
		// 更新完成后删除缓存
		shopDetailService.removeShopDetailCacheByShopId(shopDetail.getShopId());
		return ServerResponseEntity.success();
	}


	/**
	 * 获取所有的店铺名称
	 */
    @GetMapping("/listShopName")
	public ServerResponseEntity<List<ShopDetail>> listShopName(){
		// 获取所有的店铺详情信息
		// 使用流式API对每个店铺详情进行处理，仅保留店铺ID和店铺名称
		List<ShopDetail> list = shopDetailService.list().stream().map((dbShopDetail) ->{
			ShopDetail shopDetail = new ShopDetail();
			shopDetail.setShopId(dbShopDetail.getShopId());
			shopDetail.setShopName(dbShopDetail.getShopName());
			return shopDetail;
		}).collect(Collectors.toList());
		return ServerResponseEntity.success(list);
	}
}
