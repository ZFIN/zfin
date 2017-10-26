--liquibase formatted sql
--changeset sierra:addBlastdb.sql

create temp table tmp_id (id varchar(50))
with no log;

insert into tmp_id  
 select get_id('BLASTDB') from single;

insert into blast_database (blastdb_zdb_id,
       blastdb_name,
       blastdb_abbrev,
       blastdb_description,
       blastdb_type,
       blastdb_tool_display_name,
       blastdb_origination_id)
select id, 'Reference Proteome', 'REFPROT','Sequences in the reference proteome','protein','Zebrafish Reference Proteome', 3
 from tmp_id;

insert into blastdb_order (bdborder_parent_blastdb_zdb_id,
			   bdborder_child_blastdb_zdb_id,
			   bdborder_order)
select 'ZDB-BLASTDB-090929-22',id,30
 from tmp_id;
