/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.shop.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.enums.AreaLevelEnum;
import com.yami.shop.bean.model.Area;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.service.AreaService;
import org.springframework.beans.factory.annotation.Autowired;
import com.yami.shop.common.response.ServerResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * 城市
 * @author 北易航
 */
@RestController
@RequestMapping("/admin/area")
public class AreaController {

    @Autowired
    private AreaService areaService;

    /**
     * 分页获取
     */
    @GetMapping("/page")
    @PreAuthorize("@pms.hasPermission('admin:area:page')")
    public ServerResponseEntity<IPage<Area>> page(Area area,PageParam<Area> page) {
        IPage<Area> sysUserPage = areaService.page(page, new LambdaQueryWrapper<Area>());
        return ServerResponseEntity.success(sysUserPage);
    }

    /**
     * 获取省市
     */
    @GetMapping("/list")
    // 在方法执行前进行权限检查，只有通过才能执行
    @PreAuthorize("@pms.hasPermission('admin:area:list')")
    public ServerResponseEntity<List<Area>> list(Area area) {
        // 调用areaService的list()方法，该方法接受一个LambdaQueryWrapper对象作为参数，并返回一个Area对象的列表
        List<Area> areas = areaService.list(new LambdaQueryWrapper<Area>()
                .like(area.getAreaName() != null, Area::getAreaName, area.getAreaName()));
        // 将查询到的列表包装成ServerResponseEntity对象返回
        return ServerResponseEntity.success(areas);
    }
    /**
     * 通过父级id获取区域列表
     */
    @GetMapping("/listByPid")
    public ServerResponseEntity<List<Area>> listByPid(Long pid) {
        List<Area> list = areaService.listByPid(pid);
        return ServerResponseEntity.success(list);
    }

    /**
     * 获取信息
     */
    @GetMapping("/info/{id}")
    @PreAuthorize("@pms.hasPermission('admin:area:info')")
    public ServerResponseEntity<Area> info(@PathVariable("id") Long id) {
        Area area = areaService.getById(id);
        return ServerResponseEntity.success(area);
    }

    /**
     * 保存
     */
    @PostMapping
    @PreAuthorize("@pms.hasPermission('admin:area:save')")
    public ServerResponseEntity<Void> save(@Valid @RequestBody Area area) {
        // 如果有父级地区，就根据父级地区计算当前地区的层级
        if (area.getParentId() != null) {
            Area parentArea = areaService.getById(area.getParentId());
            area.setLevel(parentArea.getLevel() + 1);
            // 由于新增了一个子地区，需要清空所有以该父级地区为父级的缓存
            areaService.removeAreaCacheByParentId(area.getParentId());
        }
        // 保存地区信息到数据库
        areaService.save(area);
        return ServerResponseEntity.success();
    }

    /**
     * 修改
     */
    @PutMapping
    @PreAuthorize("@pms.hasPermission('admin:area:update')")
    public ServerResponseEntity<Void> update(@Valid @RequestBody Area area) {
        Area areaDb = areaService.getById(area.getAreaId());
        // 判断当前省市区级别，如果是1级、2级则不能修改级别，不能修改成别人的下级
        if(Objects.equals(areaDb.getLevel(), AreaLevelEnum.FIRST_LEVEL.value()) && !Objects.equals(area.getLevel(),AreaLevelEnum.FIRST_LEVEL.value())){
            throw new YamiShopBindException("不能改变一级行政地区的级别");
        }
        if(Objects.equals(areaDb.getLevel(),AreaLevelEnum.SECOND_LEVEL.value()) && !Objects.equals(area.getLevel(),AreaLevelEnum.SECOND_LEVEL.value())){
            throw new YamiShopBindException("不能改变二级行政地区的级别");
        }
        hasSameName(area);
        areaService.updateById(area);
        areaService.removeAreaCacheByParentId(area.getParentId());
        return ServerResponseEntity.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@pms.hasPermission('admin:area:delete')")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        Area area = areaService.getById(id);
        areaService.removeById(id);
        areaService.removeAreaCacheByParentId(area.getParentId());
        return ServerResponseEntity.success();
    }
    // 是否存在同名的地区
    private void hasSameName(Area area) {
        // 传入的地区对象中的 parentId 和 areaName 属性进行查询
        long count = areaService.count(new LambdaQueryWrapper<Area>()
                .eq(Area::getParentId, area.getParentId())
                .eq(Area::getAreaName, area.getAreaName())
                .ne(Objects.nonNull(area.getAreaId()) && !Objects.equals(area.getAreaId(), 0L), Area::getAreaId, area.getAreaId())
        );
        if (count > 0) {
            // 如果数据库中已经存在了具有相同地区，则弹出
            throw new YamiShopBindException("该地区已存在");
        }
    }
}
