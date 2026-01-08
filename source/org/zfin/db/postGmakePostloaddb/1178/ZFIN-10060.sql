--liquibase formatted sql
--changeset rtaylor:ZFIN-10060.sql

update zdb_submitters set is_curator = false where login = 'dfashena';

INSERT INTO updates ("submitter_id", "rec_id", "field_name", "new_value", "old_value", "comments",
                     "submitter_name", "upd_when") VALUES
                    ('ZDB-PERS-210917-1', 'ZDB-PERS-960805-646', 'is_curator',
                     'false', 'true', '', 'Taylor, Ryan', now());

-- While we're at it, redact password changes
update updates set old_value='redacted', new_value='redacted' where field_name = 'password';
