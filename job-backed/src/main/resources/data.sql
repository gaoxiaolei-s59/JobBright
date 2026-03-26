MERGE INTO user_account (id, username, email, password_hash, display_name, role, created_at, updated_at)
KEY(id)
VALUES (
    1,
    'demo',
    'demo@jobbacked.com',
    '$2y$10$FNxHlrkEOq8aXajwrvxIq.C.Lk.1dk15maTV5WeoAZMP6JEc1EWwe',
    'Demo User',
    'USER',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
