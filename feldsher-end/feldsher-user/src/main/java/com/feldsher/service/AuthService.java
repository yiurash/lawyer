package com.feldsher.service;

import com.feldsher.dto.LoginDTO;
import com.feldsher.dto.RegisterDTO;
import com.feldsher.vo.LoginVO;
import com.feldsher.vo.UserVO;

public interface AuthService {

    void register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserVO getUserInfo(Long userId);

    void logout(String token);
}
