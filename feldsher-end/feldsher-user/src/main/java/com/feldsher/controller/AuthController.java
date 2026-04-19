package com.feldsher.controller;

import com.feldsher.common.Result;
import com.feldsher.dto.LoginDTO;
import com.feldsher.dto.RegisterDTO;
import com.feldsher.security.JwtUtil;
import com.feldsher.service.AuthService;
import com.feldsher.vo.LoginVO;
import com.feldsher.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证接口", description = "用户注册、登录、登出、获取用户信息等认证相关接口")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户通过手机号和密码注册账号")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success("注册成功", null);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户通过手机号和密码登录，返回JWT令牌")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = authService.login(dto);
        return Result.success("登录成功", vo);
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "根据Token获取当前登录用户的信息")
    public Result<UserVO> getUserInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String phone,
            @RequestHeader(value = "Authorization", required = false) String token) {
        Long userId = null;
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7);
            userId = jwtUtil.getUserId(jwtToken);
        }
        UserVO userInfo = authService.getUserInfo(userId);
        return Result.success(userInfo);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出，将Token加入黑名单")
    public Result<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String token) {
        String jwtToken = null;
        if (token != null && token.startsWith("Bearer ")) {
            jwtToken = token.substring(7);
        }
        authService.logout(jwtToken);
        return Result.success("登出成功", null);
    }
}
