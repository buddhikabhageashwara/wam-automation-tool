INSERT INTO wam_automation.wam_user
    (ID, first_name, last_name, user_email, user_password, is_deleted, create_date, modify_date, user_type_id)
VALUES
    (UUID(),
     'wam',
     'admin',
     'wamadmin@default.com',
     'WtQ9WUQ2MSRVsueCY1PyxA==',
     FALSE,
     '1000-01-01 00:00:00',
     '1000-01-01 00:00:00',
     'ea635eb0-8ee4-11ef-8eba-38f3abe02677');