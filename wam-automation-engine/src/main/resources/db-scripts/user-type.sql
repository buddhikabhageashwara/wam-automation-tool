INSERT INTO wam_automation.user_type
    (id, user_type_name, description, is_deleted, create_date, modify_date)
VALUES
    (UUID(),
     'SUPER_ADMIN',
     'This user type, SUPER_ADMIN, is capable of accessing all services within the automation tool. It is encouraged to assign this role to only one user, as there should be only one admin user managing the entire tool.',
     FALSE,
     NOW(),
     NOW());