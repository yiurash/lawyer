package com.feldsher.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.feldsher.dto.LoginDTO;
import com.feldsher.dto.RegisterDTO;
import com.feldsher.entity.Role;
import com.feldsher.entity.User;
import com.feldsher.entity.UserRole;
import com.feldsher.exception.BusinessException;
import com.feldsher.mapper.RoleMapper;
import com.feldsher.mapper.UserMapper;
import com.feldsher.mapper.UserRoleMapper;
import com.feldsher.security.JwtUtil;
import com.feldsher.service.AuthService;
import com.feldsher.util.PasswordValidator;
import com.feldsher.vo.LoginVO;
import com.feldsher.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        PasswordValidator.PasswordValidationResult validationResult = PasswordValidator.validate(dto.getPassword());
        if (!validationResult.isValid()) {
            throw new BusinessException(validationResult.getMessage());
        }

        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, dto.getPhone())
        );

        if (existingUser != null) {
            throw new BusinessException("该手机号已被注册");
        }

        Role role = roleMapper.selectOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, dto.getRoleCode())
                        .eq(Role::getStatus, 1)
        );

        if (role == null) {
            throw new BusinessException("无效的角色编码");
        }

        User user = new User();
        user.setPhone(dto.getPhone());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);

        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRoleMapper.insert(userRole);

        log.info("用户注册成功, 用户ID: {}, 手机号: {}, 角色: {}", user.getId(), user.getPhone(), dto.getRoleCode());
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getPhone(), dto.getPassword())
        );

        org.springframework.security.core.userdetails.User userDetails =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getPhone, userDetails.getUsername())
        );

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());

        String token = jwtUtil.generateToken(user.getId(), user.getPhone());

        user.setLastLoginTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户登录成功, 用户ID: {}, 手机号: {}", user.getId(), user.getPhone());

        return LoginVO.builder()
                .userId(user.getId())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .roles(roles)
                .token(token)
                .expiresIn(jwtUtil.getExpiration())
                .build();
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());

        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender());
        vo.setRealName(user.getRealName());
        vo.setDepartment(user.getDepartment());
        vo.setTitle(user.getTitle());
        vo.setIntroduction(user.getIntroduction());
        vo.setStatus(user.getStatus());
        vo.setRoles(roles);
        vo.setLastLoginTime(user.getLastLoginTime());
        vo.setCreateTime(user.getCreateTime());

        return vo;
    }

    @Override
    public void logout(String token) {
        if (StrUtil.isNotBlank(token)) {
            String key = BLACKLIST_PREFIX + token;
            long ttl = jwtUtil.getExpiration();
            stringRedisTemplate.opsForValue().set(key, "1", ttl, TimeUnit.SECONDS);
            log.info("用户登出成功, token已加入黑名单");
        }
    }
}
