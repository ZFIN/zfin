--liquibase formatted sql
--changeset christian:ZFIN-7878

update blast_database set blastdb_public = 't' where blastdb_name = 'Curated NTR / Regions';

update blastdb_order set bdborder_order = 10 * bdborder_order;


insert into blastdb_order (bdborder_child_blastdb_zdb_id, bdborder_order)
select blastdb_zdb_id, 60 from blast_database where  blastdb_name = 'Curated NTR / Regions';