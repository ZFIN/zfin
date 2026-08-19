--liquibase formatted sql

-- ZFIN-10454: log submissions through the public account, organization and user comment forms,
-- including the ones rejected as spam, so attempted submissions can be reviewed periodically
-- alongside successful ones. The person fields are stored individually as well as in the details
-- text so the account creation form can be prefilled from a request.

--changeset rtaylor:0040-ZFIN-10454-submission-log.sql

create table submission_log (
    sublog_pk_id serial8 not null primary key,
    sublog_type varchar(30) not null,
    sublog_outcome varchar(30) not null,
    sublog_date timestamp not null default now(),
    sublog_spam_score integer not null default 0,
    sublog_spam_reasons text,
    sublog_validation_errors text,
    sublog_name text,
    sublog_first_name text,
    sublog_last_name text,
    sublog_email text,
    sublog_orcid text,
    sublog_address text,
    sublog_country text,
    sublog_phone text,
    sublog_lab text,
    sublog_role text,
    sublog_url text,
    sublog_comments text,
    sublog_details text,
    sublog_ip_address text,
    sublog_user_agent text
)
;

comment on table submission_log is
    'Log of submissions through the public account, organization and user comment forms, including the ones rejected as spam, so attempted submissions can be reviewed periodically.'
;

create index submission_log_date_index on submission_log (sublog_date)
;

create index submission_log_outcome_index on submission_log (sublog_outcome, sublog_date)
;
