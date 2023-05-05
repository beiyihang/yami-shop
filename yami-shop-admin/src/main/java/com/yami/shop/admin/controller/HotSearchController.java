package com.yami.shop.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.HotSearch;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.admin.util.SecurityUtils;
import com.yami.shop.service.HotSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * 热搜数据
 * @author 北易航
 */
@RestController
@RequestMapping("/admin/hotSearch")
public class HotSearchController {

    @Autowired
    private HotSearchService hotSearchService;

	/**
	 * 分页获取
	 */
    @GetMapping("/page")
	@PreAuthorize("@pms.hasPermission('admin:hotSearch:page')")
	public ServerResponseEntity<IPage<HotSearch>> page(HotSearch hotSearch,PageParam<HotSearch> page){
		// 使用 hotSearchService 进行分页查询
		IPage<HotSearch> hotSearchs = hotSearchService.page(page,new LambdaQueryWrapper<HotSearch>()
				// 查询指定店铺的数据
			.eq(HotSearch::getShopId, SecurityUtils.getSysUser().getShopId())
				// 模糊查询 content 字段
			.like(StrUtil.isNotBlank(hotSearch.getContent()), HotSearch::getContent,hotSearch.getContent())
				// 模糊查询 title 字段
				.like(StrUtil.isNotBlank(hotSearch.getTitle()), HotSearch::getTitle,hotSearch.getTitle())
				// 精确查询 status 字段
			.eq(hotSearch.getStatus()!=null, HotSearch::getStatus,hotSearch.getStatus())
				// 根据 seq 字段升序排序
				.orderByAsc(HotSearch::getSeq)
		);
		return ServerResponseEntity.success(hotSearchs);
	}

    /**
	 * 获取信息
	 */
	@GetMapping("/info/{id}")
	public ServerResponseEntity<HotSearch> info(@PathVariable("id") Long id){
		HotSearch hotSearch = hotSearchService.getById(id);
		return ServerResponseEntity.success(hotSearch);
	}

	/**
	 * 保存
	 * 新增一个热门搜索
	 */
	@PostMapping
	@PreAuthorize("@pms.hasPermission('admin:hotSearch:save')")
	public ServerResponseEntity<Void> save(@RequestBody @Valid HotSearch hotSearch){
		// 设置当前时间为热门搜索记录的创建时间
		hotSearch.setRecDate(new Date());
		// 将当前登录用户所属的商店ID设置为热门搜索记录的所属商店ID；
		hotSearch.setShopId(SecurityUtils.getSysUser().getShopId());
		// 将热门搜索记录保存到数据库中
		hotSearchService.save(hotSearch);
		//清除缓存
		hotSearchService.removeHotSearchDtoCacheByShopId(SecurityUtils.getSysUser().getShopId());
		return ServerResponseEntity.success();
	}

	/**
	 * 修改
	 */
	@PutMapping
	@PreAuthorize("@pms.hasPermission('admin:hotSearch:update')")
	public ServerResponseEntity<Void> update(@RequestBody @Valid HotSearch hotSearch){
		hotSearchService.updateById(hotSearch);
		//清除缓存
		hotSearchService.removeHotSearchDtoCacheByShopId(SecurityUtils.getSysUser().getShopId());
		return ServerResponseEntity.success();
	}

	/**
	 * 删除
	 */
	@DeleteMapping
	@PreAuthorize("@pms.hasPermission('admin:hotSearch:delete')")
	public ServerResponseEntity<Void> delete(@RequestBody List<Long> ids){
		hotSearchService.removeByIds(ids);
		//清除缓存
		hotSearchService.removeHotSearchDtoCacheByShopId(SecurityUtils.getSysUser().getShopId());
		return ServerResponseEntity.success();
	}
}
