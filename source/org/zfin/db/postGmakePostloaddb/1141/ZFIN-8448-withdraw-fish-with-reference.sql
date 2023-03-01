--liquibase formatted sql
--changeset rtaylor:ZFIN-8448-withdraw-fish-with-reference

INSERT INTO withdrawn_data ("wd_old_zdb_id", "wd_new_zdb_id", "wd_display_note") VALUES
         ('ZDB-FISH-180827-12', 'ZDB-PUB-201228-1', 'This fish has been removed from our database. Please see the linked errata for more information');
