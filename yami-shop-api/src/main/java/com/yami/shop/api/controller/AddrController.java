package com.yami.shop.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.bean.app.dto.UserAddrDto;
import com.yami.shop.bean.app.param.AddrParam;
import com.yami.shop.bean.model.UserAddr;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.UserAddrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import cn.hutool.core.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * @author 北易航
 */
@RestController
@RequestMapping("/p/address")
@Tag(name = "地址接口")
@AllArgsConstructor
public class AddrController {

    @Autowired
    private UserAddrService userAddrService;

    /**
     * 选择订单配送地址
     */
    @GetMapping("/list")
    @Operation(summary = "用户地址列表" , description = "获取用户的所有地址信息")
    public ServerResponseEntity<List<UserAddrDto>> dvyList() {
        // 获取当前用户id
        String userId = SecurityUtils.getUser().getUserId();
        // 查询该用户的所有收货地址，并按常用地址和更新时间进行倒序排列
        List<UserAddr> userAddrs = userAddrService.list(new LambdaQueryWrapper<UserAddr>()
                .eq(UserAddr::getUserId, userId)
                .orderByDesc(UserAddr::getCommonAddr)
                .orderByDesc(UserAddr::getUpdateTime));
        // 将查询到的用户收货地址列表转换为UserAddrDto类型并返回响应实体类
        return ServerResponseEntity.success(BeanUtil.copyToList(userAddrs, UserAddrDto.class));
    }

    @PostMapping("/addAddr")
    @Operation(summary = "新增用户地址" , description = "新增用户地址")
    public ServerResponseEntity<String> addAddr(@Valid @RequestBody AddrParam addrParam) {
        String userId = SecurityUtils.getUser().getUserId();
        // 判断地址是否已经存在
        if (addrParam.getAddrId() != null && addrParam.getAddrId() != 0) {
            return ServerResponseEntity.showFailMsg("该地址已存在");
        }
        // 查询用户已有地址数量
        long addrCount = userAddrService.count(new LambdaQueryWrapper<UserAddr>().eq(UserAddr::getUserId, userId));
        UserAddr userAddr = BeanUtil.copyProperties(addrParam, UserAddr.class);
        // 判断是否为用户第一条地址记录
        if (addrCount == 0) {
            userAddr.setCommonAddr(1);
        } else {
            userAddr.setCommonAddr(0);
        }
        // 设置用户ID、状态、创建时间和更新时间
        userAddr.setUserId(userId);
        userAddr.setStatus(1);
        userAddr.setCreateTime(new Date());
        userAddr.setUpdateTime(new Date());
        // 保存用户地址
        userAddrService.save(userAddr);
        // 如果新增地址为用户的默认地址，清除默认地址缓存
        if (userAddr.getCommonAddr() == 1) {
            // 清除默认地址缓存
            userAddrService.removeUserAddrByUserId(0L, userId);
        }
        return ServerResponseEntity.success("添加地址成功");
    }

    /**
     * 修改订单配送地址
     */
    @PutMapping("/updateAddr")
    @Operation(summary = "修改订单用户地址" , description = "修改用户地址")
    public ServerResponseEntity<String> updateAddr(@Valid @RequestBody AddrParam addrParam) {
        // 获取当前用户的id
        String userId = SecurityUtils.getUser().getUserId();
        // 根据用户id和地址id获取数据库中该地址的信息
        UserAddr dbUserAddr = userAddrService.getUserAddrByUserId(addrParam.getAddrId(), userId);
        // 判断地址是否为空
        if (dbUserAddr == null) {
            return ServerResponseEntity.showFailMsg("该地址已被删除");
        }
        // 将请求中的地址信息复制到一个新的 UserAddr 对象中
        UserAddr userAddr = BeanUtil.copyProperties(addrParam, UserAddr.class);
        userAddr.setUserId(userId);
        userAddr.setUpdateTime(new Date());
        // 更新数据库中该地址的信息
        userAddrService.updateById(userAddr);
        // 清除当前地址缓存
        userAddrService.removeUserAddrByUserId(addrParam.getAddrId(), userId);
        // 清除默认地址缓存
        userAddrService.removeUserAddrByUserId(0L, userId);
        return ServerResponseEntity.success("修改地址成功");
    }

    /**
     * 删除订单配送地址
     */
    @DeleteMapping("/deleteAddr/{addrId}")
    @Operation(summary = "删除订单用户地址" , description = "根据地址id，删除用户地址")
    @Parameter(name = "addrId", description = "地址ID" , required = true)
    public ServerResponseEntity<String> deleteDvy(@PathVariable("addrId") Long addrId) {
        // 获取当前用户的id
        String userId = SecurityUtils.getUser().getUserId();
        // 根据用户id和地址id获取数据库中该地址的信息
        UserAddr userAddr = userAddrService.getUserAddrByUserId(addrId, userId);
        // 如果该地址已被删除，返回失败响应
        if (userAddr == null) {
            return ServerResponseEntity.showFailMsg("该地址已被删除");
        }
        // 如果该地址为默认地址，无法删除，返回失败响应
        if (userAddr.getCommonAddr() == 1) {
            return ServerResponseEntity.showFailMsg("默认地址无法删除");
        }
        // 根据地址id删除该地址
        userAddrService.removeById(addrId);
        // 清除当前地址缓存
        userAddrService.removeUserAddrByUserId(addrId, userId);
        // 返回成功响应
        return ServerResponseEntity.success("删除地址成功");
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/defaultAddr/{addrId}")
    @Operation(summary = "设置默认地址" , description = "根据地址id，设置默认地址")
    public ServerResponseEntity<String> defaultAddr(@PathVariable("addrId") Long addrId) {
        String userId = SecurityUtils.getUser().getUserId();

        userAddrService.updateDefaultUserAddr(addrId, userId);
        // 清除默认地址缓存和当前地址缓存
        userAddrService.removeUserAddrByUserId(0L, userId);
        userAddrService.removeUserAddrByUserId(addrId, userId);
        return ServerResponseEntity.success("修改地址成功");
    }

    /**
     * 获取地址信息订单配送地址
     */
    @GetMapping("/addrInfo/{addrId}")
    @Operation(summary = "获取地址信息" , description = "根据地址id，获取地址信息")
    @Parameter(name = "addrId", description = "地址ID" , required = true)
    public ServerResponseEntity<UserAddrDto> addrInfo(@PathVariable("addrId") Long addrId) {
        // 获取当前用户的ID
        String userId = SecurityUtils.getUser().getUserId();
        // 根据地址ID和用户ID获取该地址在数据库中的信息
        UserAddr userAddr = userAddrService.getUserAddrByUserId(addrId, userId);
        // 根据地址ID和用户ID获取该地址在数据库中的信息
        if (userAddr == null) {
            throw new YamiShopBindException("该地址已被删除");
        }
        // 将获取到的地址信息转化为DTO并返回
        return ServerResponseEntity.success(BeanUtil.copyProperties(userAddr, UserAddrDto.class));
    }

}
