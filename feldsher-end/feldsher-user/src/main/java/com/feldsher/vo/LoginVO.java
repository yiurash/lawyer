package com.feldsher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "登录响应视图对象")
public class LoginVO {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "角色列表")
    private List<String> roles;

    @Schema(description = "JWT令牌")
    private String token;

    @Schema(description = "令牌过期时间（秒）")
    private Long expiresIn;
}
