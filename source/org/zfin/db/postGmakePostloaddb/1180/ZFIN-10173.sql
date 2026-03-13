--liquibase formatted sql
--changeset rtaylor:ZFIN-10173

UPDATE figure SET fig_label = 'Figure 1' WHERE fig_zdb_id = 'ZDB-FIG-130516-10';
UPDATE figure SET fig_label = 'Figure 2' WHERE fig_zdb_id = 'ZDB-FIG-130516-11';
UPDATE figure SET fig_label = 'Figure 3' WHERE fig_zdb_id = 'ZDB-FIG-130516-12';
UPDATE figure SET fig_label = 'Figure 4' WHERE fig_zdb_id = 'ZDB-FIG-130516-13';
UPDATE figure SET fig_label = 'Figure 5' WHERE fig_zdb_id = 'ZDB-FIG-130516-8';
UPDATE figure SET fig_label = 'Figure 6' WHERE fig_zdb_id = 'ZDB-FIG-130516-9';
