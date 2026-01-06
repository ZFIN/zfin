-- update marker assembly association after the load
-- list all genes that got a new NCBI ID attached and should be GRCz12 but are not yet.

create temp table temp_new_gene as
select db.dblink_linked_recid as gene_zdb_id, db.dblink_acc_num as accession
from db_link as db
where db.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
  and not exists(
        select *
        from zfindb.public.sequence_feature_chromosome_location_generated
        where db.dblink_linked_recid = sfclg_data_zdb_id
          and sfclg_assembly = 'GRCz12tu'
    )
  and exists(
        select *
        from gff3_ncbi
        where gff_attributes like '%GeneID:' || db.dblink_acc_num || '%'
          AND gff_feature in ('gene', 'pseudogene')
    )
  and not exists(
        select d.dblink_linked_recid
        from db_link as d
        where d.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1'
          AND d.dblink_linked_recid = db.dblink_linked_recid
        group by d.dblink_linked_recid
        having count(*) > 1
    )
;

\echo Genes or Pseudogenes that exist in the original GFF3 file from NCBI
\echo that got loaded and need post-load processing:
\echo insert marker-assembly records to associate them to GRCz12tu
\echo insert  sequence_feature_chromosome_location_generated records (official genome location info)
\echo add gene_id records into gff3_ncbi_attribute table so the outgoing zfin_genes.grcz12.gff3 file
\echo contains this attribute needed to link the glyph in jBrowse

select * from temp_new_gene;

-- using upsert to avoid message: ERROR:  duplicate key value violates unique constraint "marker_assembly_ma_a_pk_id_ma_mrkr_zdb_id_key"
insert into marker_assembly (ma_mrkr_zdb_id, ma_a_pk_id)
select gene_zdb_id, 1
from temp_new_gene
on conflict (ma_mrkr_zdb_id, ma_a_pk_id) do nothing;

insert into sequence_feature_chromosome_location_generated (sfclg_data_Zdb_id,
                                                            sfclg_chromosome,
                                                            sfclg_assembly,
                                                            sfclg_start,
                                                            sfclg_end,
                                                            sfclg_acc_num,
                                                            sfclg_location_source)
select gene_zdb_id,
       gff_seqname,
       'GRCz12tu',
       gff_start,
       gff_end,
       accession,
       'NCBILoader'
from temp_new_gene,
     gff3_ncbi,
     gff3_ncbi_attribute
where gna_key = 'Dbxref'
  and (regexp_like(gna_value, '.*GeneID:' || accession || '$') OR regexp_like(gna_value, '.*GeneID:' || accession || ','))
  and gna_gff_pk_id = gff_pk_id
  and gff_feature in ('gene', 'pseudogene');

-- insert ZFIN records matching NCBI
insert into sequence_feature_chromosome_location_generated (sfclg_data_Zdb_id,
                                                            sfclg_chromosome,
                                                            sfclg_assembly,
                                                            sfclg_start,
                                                            sfclg_end,
                                                            sfclg_acc_num,
                                                            sfclg_location_source)
select gene_zdb_id,
       gff_seqname,
       'GRCz12tu',
       gff_start,
       gff_end,
       accession,
       'ZFIN'
from temp_new_gene,
     gff3_ncbi,
     gff3_ncbi_attribute
where gna_key = 'Dbxref'
  and (regexp_like(gna_value, '.*GeneID:' || accession || '$') OR regexp_like(gna_value, '.*GeneID:' || accession || ','))
  and gna_gff_pk_id = gff_pk_id
  and gff_feature in ('gene', 'pseudogene');

-- insert gene_id into gff3_ncbi_attribute

insert into gff3_ncbi_attribute (gna_gff_pk_id,gna_key, gna_value)
select gff_pk_id,
       'gene_id',
       gene_zdb_id
from temp_new_gene,
     gff3_ncbi,
     gff3_ncbi_attribute
where gna_key = 'Dbxref'
  and (regexp_like(gna_value, '.*GeneID:' || accession || '$') OR regexp_like(gna_value, '.*GeneID:' || accession || ','))
  and gna_gff_pk_id = gff_pk_id
  and gff_feature in ('gene', 'pseudogene');

-- marker gene as GRCz11 if they do not have a z12 association but have a sequence_feature_chromosome_location_generated record for ZFIN with GRCz11
insert into marker_assembly
select distinct gg.sfclg_data_zdb_id, 3 from sequence_feature_chromosome_location_generated as gg
where gg.sfclg_assembly = 'GRCz11'
  and gg.sfclg_location_source = 'ZfinGbrowseStartEndLoader'
  and gg.sfclg_acc_num like 'ENSDARG%'
  and not exists (
        select * from zfindb.public.sequence_feature_chromosome_location_generated as ss
        where gg.sfclg_data_zdb_id = ss.sfclg_data_zdb_id
          and ss.sfclg_assembly = 'GRCz12tu'
    )
  and not exists (
        select * from db_link
        where dblink_linked_recid = gg.sfclg_data_zdb_id
          and dblink_acc_num = gg.sfclg_acc_num
          and dblink_fdbcont_zdb_id ='ZDB-FDBCONT-040412-1'
    )
  and not exists (
        select * from marker_assembly as g
        where gg.sfclg_data_zdb_id = g.ma_mrkr_zdb_id
          and g.ma_a_pk_id =3
    )
on conflict (ma_mrkr_zdb_id, ma_a_pk_id) do nothing;
