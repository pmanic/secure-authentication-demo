merge into unsafe_users (
    display_name,
    email,
    password_plaintext,
    profile_message,
    failed_login_attempts
)
key (email)
values (
    'Demo Student',
    'student@example.com',
    'Test123!',
    '',
    0
);
