package org.puregxl.site.jobbacked.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;
import org.puregxl.site.jobbacked.dao.entity.UserAccount;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserAccountDao {

    private final JdbcTemplate jdbcTemplate;

    public UserAccountDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserAccount> findById(Long id) {
        return jdbcTemplate.query(
                """
                SELECT id, username, email, password_hash, display_name, role, created_at, updated_at
                FROM user_account
                WHERE id = ?
                """,
                (rs, rowNum) -> mapRow(rs),
                id
        ).stream().findFirst();
    }

    public Optional<UserAccount> findByUsernameOrEmail(String account) {
        return jdbcTemplate.query(
                """
                SELECT id, username, email, password_hash, display_name, role, created_at, updated_at
                FROM user_account
                WHERE username = ? OR email = ?
                LIMIT 1
                """,
                (rs, rowNum) -> mapRow(rs),
                account,
                account.toLowerCase()
        ).stream().findFirst();
    }

    public boolean existsByUsernameOrEmail(String username, String email) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(1)
                FROM user_account
                WHERE username = ? OR email = ?
                """,
                Integer.class,
                username,
                email.toLowerCase()
        );
        return count != null && count > 0;
    }

    public UserAccount save(UserAccount userAccount) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    """
                    INSERT INTO user_account (username, email, password_hash, display_name, role, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, userAccount.getUsername());
            statement.setString(2, userAccount.getEmail());
            statement.setString(3, userAccount.getPasswordHash());
            statement.setString(4, userAccount.getDisplayName());
            statement.setString(5, userAccount.getRole());
            statement.setObject(6, userAccount.getCreatedAt());
            statement.setObject(7, userAccount.getUpdatedAt());
            return statement;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            userAccount.setId(key.longValue());
        }
        return userAccount;
    }

    private UserAccount mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        UserAccount userAccount = new UserAccount();
        userAccount.setId(rs.getLong("id"));
        userAccount.setUsername(rs.getString("username"));
        userAccount.setEmail(rs.getString("email"));
        userAccount.setPasswordHash(rs.getString("password_hash"));
        userAccount.setDisplayName(rs.getString("display_name"));
        userAccount.setRole(rs.getString("role"));
        userAccount.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        userAccount.setUpdatedAt(rs.getObject("updated_at", LocalDateTime.class));
        return userAccount;
    }
}
