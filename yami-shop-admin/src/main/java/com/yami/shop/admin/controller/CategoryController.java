package com.yami.shop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.bean.model.Category;
import com.yami.shop.common.annotation.SysLog;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.security.admin.util.SecurityUtils;
import com.yami.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;



/**
 * 分类管理
 * @author 北易航
 *
 */
@RestController
@RequestMapping("/prod/category")
public class CategoryController {

	@Autowired
	private CategoryService categoryService;

	/**
	 * 获取菜单页面的表
	 * @return
	 */
	@GetMapping("/table")
	@PreAuthorize("@pms.hasPermission('prod:category:page')")
	public ServerResponseEntity<List<Category>> table(){
		List<Category> categoryMenuList = categoryService.tableCategory(SecurityUtils.getSysUser().getShopId());
		return ServerResponseEntity.success(categoryMenuList);
	}

	/**
	 * 获取分类信息
	 */
	@GetMapping("/info/{categoryId}")
	public ServerResponseEntity<Category> info(@PathVariable("categoryId") Long categoryId){
		Category category = categoryService.getById(categoryId);
		return ServerResponseEntity.success(category);
	}



	/**
	 * 保存分类
	 */
	@SysLog("保存分类")
	@PostMapping
	@PreAuthorize("@pms.hasPermission('prod:category:save')")
	public ServerResponseEntity<Void> save(@RequestBody Category category){
		// 类目所属的商店ID和记录创建时间
		category.setShopId(SecurityUtils.getSysUser().getShopId());
		category.setRecTime(new Date());
		// 通过查询是否存在同名的类目，来防止重复添加类目
		Category categoryName = categoryService.getOne(new LambdaQueryWrapper<Category>().eq(Category::getCategoryName,category.getCategoryName())
				.eq(Category::getShopId,category.getShopId()));
		if(categoryName != null){
			// 如果存在
			throw new YamiShopBindException("类目名称已存在！");
		}
		// 保存该类目
		categoryService.saveCategory(category);
		return ServerResponseEntity.success();
	}

	/**
	 * 更新分类
	 */
	@SysLog("更新分类")
	@PutMapping
	@PreAuthorize("@pms.hasPermission('prod:category:update')")
	public ServerResponseEntity<String> update(@RequestBody Category category){
		// 设置分类所属的店铺ID
		category.setShopId(SecurityUtils.getSysUser().getShopId());
		// 判断分类的上级是否为自己本身，如果是，返回失败信息
		if (Objects.equals(category.getParentId(),category.getCategoryId())) {
			return ServerResponseEntity.showFailMsg("分类的上级不能是自己本身");
		}
		// 判断分类名称是否已经存在
		Category categoryName = categoryService.getOne(new LambdaQueryWrapper<Category>().eq(Category::getCategoryName,category.getCategoryName())
				.eq(Category::getShopId,category.getShopId()).ne(Category::getCategoryId,category.getCategoryId()));
		if(categoryName != null){
			throw new YamiShopBindException("类目名称已存在！");
		}
		// 获取待更新分类的原始信息
		Category categoryDb = categoryService.getById(category.getCategoryId());
		// 如果从下线改成正常，则需要判断上级的状态
		if (Objects.equals(categoryDb.getStatus(),0) && Objects.equals(category.getStatus(),1) && !Objects.equals(category.getParentId(),0L)){
			// 获取上级分类信息
			Category parentCategory = categoryService.getOne(new LambdaQueryWrapper<Category>().eq(Category::getCategoryId, category.getParentId()));
			// 如果上级分类不存在或者状态为下线，则返回失败信息
			if(Objects.isNull(parentCategory) || Objects.equals(parentCategory.getStatus(),0)){
				// 修改失败，上级分类不存在或者不为正常状态
				throw new YamiShopBindException("修改失败，上级分类不存在或者不为正常状态");
			}
		}
		// 更新分类信息
		categoryService.updateCategory(category);
		return ServerResponseEntity.success();
	}

	/**
	 * 删除分类
	 */
	@SysLog("删除分类")
	@DeleteMapping("/{categoryId}")
	@PreAuthorize("@pms.hasPermission('prod:category:delete')")
	public ServerResponseEntity<String> delete(@PathVariable("categoryId") Long categoryId){
		// 判断当前分类下是否还有子分类
		if (categoryService.count(new LambdaQueryWrapper<Category>().eq(Category::getParentId,categoryId)) >0) {
			// 如果有子分类，则返回删除失败的提示信息
			return ServerResponseEntity.showFailMsg("请删除子分类，再删除该分类");
		}
		// 如果没有子分类，则调用categoryService的deleteCategory方法删除该分类
		categoryService.deleteCategory(categoryId);
		return ServerResponseEntity.success();
	}

	/**
	 * 所有的
	 */
	@GetMapping("/listCategory")
	public ServerResponseEntity<List<Category>> listCategory(){
		// 通过categoryService调用list方法，查询符合以下条件的分类
		// 将查询到的分类列表包装为ServerResponseEntity对象，并返回
		return ServerResponseEntity.success(categoryService.list(new LambdaQueryWrapper<Category>()
				// 分类等级小于等于2（因为只需要获取一级分类和二级分类）
				.le(Category::getGrade, 2)
				// 商铺ID与当前用户所属商铺ID相同
				.eq(Category::getShopId, SecurityUtils.getSysUser().getShopId())
				// 按照seq字段升序排序
				.orderByAsc(Category::getSeq)));
	}

	/**
	 * 所有的产品分类
	 */
	@GetMapping("/listProdCategory")
	public ServerResponseEntity<List<Category>> listProdCategory(){
		// 调用 categoryService 的 treeSelect 方法获取商品分类列表
		// 2 表示只获取二级分类列表。
    	List<Category> categories = categoryService.treeSelect(SecurityUtils.getSysUser().getShopId(),2);
		// 返回成功的响应和商品分类列表
		return ServerResponseEntity.success(categories);
	}
}
