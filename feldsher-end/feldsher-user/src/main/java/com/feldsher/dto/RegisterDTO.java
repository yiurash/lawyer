package com.feldsher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "用户注册请求参数")
public class RegisterDTO {

    @Schema(description = "手机号", example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "密码", example = "Abc123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "确认密码", example = "Abc123456")
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    @Schema(description = "角色编码：DOCTOR-医生, PATIENT-患者", example = "PATIENT")
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
}
