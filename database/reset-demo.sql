delete from unsafe_users
where
  email <> 'student@example.com';

delete from secure_users;

update unsafe_users
set
  email = 'student@example.com',
  display_name = 'Demo Student',
  password_plaintext = 'Test123!',
  profile_message = '',
  failed_login_attempts = 0;
