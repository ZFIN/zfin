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


insert into marker_assembly (ma_mrkr_zdb_id, ma_a_pk_id)
select gene_zdb_id, 1
from temp_new_gene
;

insert into sequence_feature_chromosome_location_generated (sfclg_data_Zdb_id,
                                                            sfclg_chromosome,
                                                            sfclg_assembly,
                                                            sfclg_start,
                                                            sfclg_end,
                                                            sfclg_acc_num,
                                                            sfclg_location_source)
select gene_zdb_id,
       gff_seqname,
       'GRCz12',
       gff_start,
       gff_end,
       accession,
       'NCBILoader'
from temp_new_gene,
     gff3_ncbi,
     gff3_ncbi_attribute
where gna_key = 'Dbxref'
  and (regexp_like(gna_value, '.*GeneID:' || accession || '$') OR regexp_like(gna_value,'.*GeneID:' || accession || ','))
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
       'GRCz12',
       gff_start,
       gff_end,
       accession,
       'ZFIN'
from temp_new_gene,
     gff3_ncbi,
     gff3_ncbi_attribute
where gna_key = 'Dbxref'
  and (regexp_like(gna_value, '.*GeneID:' || accession || '$') OR regexp_like(gna_value,'.*GeneID:' || accession || ','))
  and gna_gff_pk_id = gff_pk_id
  and gff_feature in ('gene', 'pseudogene');
