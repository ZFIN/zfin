--liquibase formatted sql
--changeset christian:ZFIN-7641

delete
from pub_correspondence_sent_tracker
where pubcst_sent_email_id in (
    select pubcse_pk_id from pub_correspondence_sent_email where pubcse_pub_zdb_id = 'ZDB-PUB-201229-47'
);

delete
from pub_correspondence_recipient
where pubcr_recipient_sent_email_id in (
    select pubcse_pk_id from pub_correspondence_sent_email where pubcse_pub_zdb_id = 'ZDB-PUB-201229-47'
);
