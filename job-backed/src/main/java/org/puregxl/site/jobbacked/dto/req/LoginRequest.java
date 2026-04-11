package org.puregxl.site.jobbacked.dto.req;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {


    @NotBlank(message = "请输入账号或邮箱")
    private String account;


    @NotBlank(message = "请输入密码")
    private String password;

    @NotBlank(message = "请先获取图形验证码")
    private String captchaKey;

    @NotBlank(message = "请输入图形验证码")
    private String captchaCode;
}
