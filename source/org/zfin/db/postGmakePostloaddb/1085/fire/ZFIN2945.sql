--liquibase formatted sql
--changeset pm:ZFIN2945

UPDATE source_url
SET srcurl_url="http://bacpacresources.org/order_clones.php?cloneList="
WHERE srcurl_source_zdb_id='ZDB-LAB-040701-1';

