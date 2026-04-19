package com.feldsher.security;

import com.feldsher.entity.User;
import com.feldsher.mapper.RoleMapper;
import com.feldsher.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.<User>lambdaQuery()
                        .eq(User::getPhone, phone)
                        .eq(User::getDeleted, 0)
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + phone);
        }

        List<String> roleCodes = roleMapper.selectRoleCodesByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = roleCodes.stream()
                .map(roleCode -> "ROLE_" + roleCode)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPhone())
                .password(user.getPassword())
                .disabled(user.getStatus() == 1)
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(user.getStatus() == 0)
                .authorities(authorities)
                .build();
    }

    public UserDetails loadUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new UsernameNotFoundException("用户不存在: " + userId);
        }

        List<String> roleCodes = roleMapper.selectRoleCodesByUserId(user.getId());
        List<SimpleGrantedAuthority> authorities = roleCodes.stream()
                .map(roleCode -> "ROLE_" + roleCode)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPhone())
                .password(user.getPassword())
                .disabled(user.getStatus() == 1)
                .accountExpired(false)
                .credentialsExpired(false)
                .accountLocked(user.getStatus() == 0)
                .authorities(authorities)
                .build();
    }
}
