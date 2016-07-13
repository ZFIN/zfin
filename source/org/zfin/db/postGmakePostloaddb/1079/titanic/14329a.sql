--liquibase formatted sql
--changeset christian:removebogusUpdatesRecord


DELETE FROM updates
WHERE  submitter_id = 'UNKNOWN'
       AND field_name IS NULL
       AND new_value IS NULL
       AND old_value = ''
       AND submitter_name LIKE 'GUEST%'
	  AND comments ='NULL'
