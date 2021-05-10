--liquibase formatted sql
--changeset christian:zfin-6183

update image set img_fig_zdb_id = 'ZDB-FIG-051107-316' where
img_zdb_id = 'ZDB-IMAGE-051107-337';