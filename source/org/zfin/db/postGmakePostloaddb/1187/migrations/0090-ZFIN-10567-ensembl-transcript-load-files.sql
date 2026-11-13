--liquibase formatted sql
--changeset rtaylor:ZFIN-10567-ensembl-transcript-load-files

-- ZFIN-10567: the Load-Ensembl-Transcripts job reads one Ensembl cDNA FASTA per assembly. Each assembly's file is
-- a load_file of the "Ensembl Transcripts" load; the file names its own assembly in each header line
-- (>ENSDART... cdna GRCz12tu:7:...), so nothing here says which assembly a URL belongs to.
--
-- GRCz13ab is not listed: Ensembl has not published it yet (GCA_052040795 version 2 is empty). Add its URL as a new
-- load_file row once it is there.

insert into load (ld_name, ld_description)
select 'Ensembl Transcripts',
       'Ensembl transcripts (ENSDARTs) for the current assemblies, from the cDNA FASTA Ensembl publishes for each one (Load-Ensembl-Transcripts job). Each becomes a ZDB-TSCRIPT linked to its gene.'
 where not exists (select 1 from load where ld_name = 'Ensembl Transcripts');

insert into load_file (lf_ld_id, lf_source)
select ld_id, src
  from load,
       (values ('https://ftp.ebi.ac.uk/pub/ensemblorganisms/GCA/049/306/965/1/ensembl/2025_12/geneset/cdna.fa.bgz'),
               ('https://ftp.ebi.ac.uk/pub/ensemblorganisms/GCA/052/040/795/1/ensembl/2025_12/geneset/cdna.fa.bgz')) as f(src)
 where ld_name = 'Ensembl Transcripts'
   and not exists (select 1 from load_file where lf_ld_id = ld_id and lf_source = src);

-- Placeholder for a file uploaded by hand (a Jenkins file parameter) instead of downloaded. 'upload://manual' is the
-- source any load uses for this (LoadFileRegistry.MANUAL_UPLOAD_SOURCE); it is not a URL. Runs on an uploaded file
-- are logged against this row, whatever the file was.
insert into load_file (lf_ld_id, lf_source)
select ld_id, 'upload://manual'
  from load
 where ld_name = 'Ensembl Transcripts'
   and not exists (select 1 from load_file where lf_ld_id = ld_id and lf_source = 'upload://manual');

--rollback delete from load_file where lf_ld_id = (select ld_id from load where ld_name = 'Ensembl Transcripts');
--rollback delete from load where ld_name = 'Ensembl Transcripts';
