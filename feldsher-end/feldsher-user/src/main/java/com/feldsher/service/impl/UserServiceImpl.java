package com.feldsher.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feldsher.entity.Role;
import com.feldsher.entity.User;
import com.feldsher.entity.UserRole;
import com.feldsher.exception.BusinessException;
import com.feldsher.mapper.RoleMapper;
import com.feldsher.mapper.UserMapper;
import com.feldsher.mapper.UserRoleMapper;
import com.feldsher.service.UserService;
import com.feldsher.util.PasswordValidator;
import com.feldsher.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());

        return toUserVO(user, roles);
    }

    @Override
    public Page<UserVO> page(Page<User> page, String keyword, String roleCode) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getDeleted, 0);

        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(User::getPhone, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getRealName, keyword)
            );
        }

        if (StrUtil.isNotBlank(roleCode)) {
            Role role = roleMapper.selectOne(
                    new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, roleCode)
            );
            if (role != null) {
                List<Long> userIds = userRoleMapper.selectList(
                                new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, role.getId())
                        ).stream()
                        .map(UserRole::getUserId)
                        .collect(Collectors.toList());
                if (!userIds.isEmpty()) {
                    queryWrapper.in(User::getId, userIds);
                } else {
                    return new Page<>();
                }
            }
        }

        queryWrapper.orderByDesc(User::getCreateTime);

        Page<User> userPage = userMapper.selectPage(page, queryWrapper);

        List<UserVO> voList = userPage.getRecords().stream()
                .map(user -> {
                    List<String> roles = roleMapper.selectRoleCodesByUserId(user.getId());
                    return toUserVO(user, roles);
                })
                .collect(Collectors.toList());

        Page<UserVO> voPage = new Page<>();
        voPage.setCurrent(userPage.getCurrent());
        voPage.setSize(userPage.getSize());
        voPage.setTotal(userPage.getTotal());
        voPage.setPages(userPage.getPages());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        user.setStatus(status);
        userMapper.updateById(user);

        log.info("用户状态更新成功, 用户ID: {}, 状态: {}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        PasswordValidator.PasswordValidationResult validationResult = PasswordValidator.validate(newPassword);
        if (!validationResult.isValid()) {
            throw new BusinessException(validationResult.getMessage());
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("用户密码重置成功, 用户ID: {}", id);
    }

    private UserVO toUserVO(User user, List<String> roles) {
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
}
