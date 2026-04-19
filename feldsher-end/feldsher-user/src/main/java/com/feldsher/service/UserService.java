package com.feldsher.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.feldsher.entity.User;
import com.feldsher.vo.UserVO;

public interface UserService {

    UserVO getById(Long id);

    Page<UserVO> page(Page<User> page, String keyword, String roleCode);

    void updateStatus(Long id, Integer status);

    void resetPassword(Long id, String newPassword);
}
