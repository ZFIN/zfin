--liquibase formatted sql
--changeset rtaylor:ZFIN-10190

DELETE FROM zdb_active_data WHERE zactvd_zdb_id LIKE 'ZDB-INFGRP-%';
