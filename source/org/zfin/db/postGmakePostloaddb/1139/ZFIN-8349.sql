--liquibase formatted sql
--changeset rtaylor:ZFIN-8349

UPDATE
    person
SET
    email = replace(email, '&#64;', '@')
WHERE
    email ILIKE '%&#64;%'

