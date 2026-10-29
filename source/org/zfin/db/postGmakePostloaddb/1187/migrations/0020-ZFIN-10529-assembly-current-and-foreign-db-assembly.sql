--liquibase formatted sql
--changeset rtaylor:ZFIN-10529-assembly-current-and-foreign-db-assembly

-- Safe to run again. The file was renamed from ZFIN-10529-assembly-current-and-foreign-db-assembly.sql so
-- that it runs before the migrations that use its columns, and Liquibase sees a renamed file as a new changeset,
-- so a database that ran it under the old name runs it a second time. Each statement leaves the same result.

-- More than one assembly can be current at once.
alter table assembly add column if not exists a_is_current boolean not null default false;

update assembly set a_is_current = true
 where a_name in ('GRCz13ab', 'GRCz12ab', 'GRCz12tu');

-- A database that belongs to a single assembly (e.g. Ensembl(GRCz12ab)) says so here.
-- Null for databases that span assemblies (RefSeq) or have none (Vega, Alliance, NCBI Gene).
alter table foreign_db add column if not exists fdb_assembly_id bigint references assembly (a_pk_id);

update foreign_db
   set fdb_assembly_id = (select a_pk_id from assembly where a_name = 'GRCz11')
 where fdb_db_name = 'Ensembl(GRCz11)';

update foreign_db
   set fdb_assembly_id = (select a_pk_id from assembly where a_name = 'GRCz12ab')
 where fdb_db_name = 'Ensembl(GRCz12ab)';

update foreign_db
   set fdb_assembly_id = (select a_pk_id from assembly where a_name = 'GRCz12tu')
 where fdb_db_name = 'Ensembl(GRCz12tu)';

update foreign_db
   set fdb_assembly_id = (select a_pk_id from assembly where a_name = 'GRCz13ab')
 where fdb_db_name = 'Ensembl(GRCz13ab)';
