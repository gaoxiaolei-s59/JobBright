package org.puregxl.site.jobbacked.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 24, message = "用户名长度需要在 3 到 24 之间")
        String username,
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,
        @NotBlank(message = "昵称不能为空")
        @Size(max = 40, message = "昵称长度不能超过 40")
        String displayName,
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 32, message = "密码长度需要在 6 到 32 之间")
        String password
) {
}
