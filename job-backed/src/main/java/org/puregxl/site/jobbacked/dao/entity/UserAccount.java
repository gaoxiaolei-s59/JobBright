package org.puregxl.site.jobbacked.dao.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAccount {

    private Long id;

    private String username;

    private String email;

    private String passwordHash;

    private String displayName;

    private String role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
