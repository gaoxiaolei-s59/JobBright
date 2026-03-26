package org.puregxl.site.jobbacked.dto.req;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "请输入账号或邮箱") String account,
        @NotBlank(message = "请输入密码") String password
) {
}
