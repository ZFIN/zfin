--liquibase formatted sql
--changeset pkalita:PUB-331

-- for issue PUB-331, i'm closing any pubs that are open and not owned by a curator and have a note
-- indicating that an author notification email went out. the assumption is that these were curated
-- and should have been closed at some point, but weren't. as of 29 June 2017 there were 68 pubs
-- in this category.

INSERT INTO pub_tracking_history (pth_pub_zdb_id, pth_status_id, pth_status_set_by, pth_status_insert_date)
SELECT zdb_id, 11, 'ZDB-PERS-140612-1', CURRENT FROM publication
inner join pub_tracking_history on zdb_id = pth_pub_zdb_id
inner join pub_tracking_status on pth_status_id = pts_pk_id
inner join publication_note on zdb_id = pnote_pub_zdb_id
where pth_status_is_current = 't'
and pts_status != 'CLOSED'
and pnote_text like 'Notified authors%'
and pth_claimed_by is null;
