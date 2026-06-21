
alter table users
add otp INT not null default 0;

alter table users
add otp_expiry BIGINT;

alter table users
add reset_key VARCHAR(255);