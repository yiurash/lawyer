package com.feldsher.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feldsher.common.Result;
import com.feldsher.entity.User;
import com.feldsher.service.UserService;
import com.feldsher.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理接口", description = "用户信息查询、状态管理等接口")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户信息", description = "根据用户ID获取用户详细信息")
    public Result<UserVO> getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        UserVO user = userService.getById(id);
        return Result.success(user);
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询用户列表", description = "分页查询用户列表，支持关键词搜索和角色筛选")
    public Result<Page<UserVO>> page(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "搜索关键词（手机号、昵称、真实姓名）") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色编码") @RequestParam(required = false) String roleCode) {
        Page<User> page = new Page<>(current, size);
        Page<UserVO> userPage = userService.page(page, keyword, roleCode);
        return Result.success(userPage);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新用户状态", description = "启用或禁用用户账号")
    public Result<Void> updateStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "状态: 1-启用, 0-禁用") @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @PutMapping("/{id}/password/reset")
    @Operation(summary = "重置用户密码", description = "管理员重置用户密码")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "新密码") @RequestParam String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success();
    }
}
