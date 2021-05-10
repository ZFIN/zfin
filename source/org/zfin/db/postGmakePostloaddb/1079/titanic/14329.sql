--liquibase formatted sql
--changeset christian:removebogusUpdatesRecord


DELETE FROM updates
WHERE  rec_id = 'ZDB-BAC-041207-140' 
       AND submitter_id = 'UNKNOWN'; 