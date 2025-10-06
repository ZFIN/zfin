--liquibase formatted sql
--changeset cmpich:ZFIN-9963

-- add strand info to sequence_feature_chromosome_location_generated table

alter table sequence_feature_chromosome_location_generated
add column     sfclg_strand CHAR(1),
add COLUMN     sfclg_date_created TIMESTAMP WITH TIME ZONE DEFAULT NOW()
;

CREATE INDEX gff3_feature_index ON gff3 (gff_feature);
CREATE INDEX gff3_id_index ON gff3 (gff_id);

-- set strand info for ensembl GRCz11 genes
update sequence_feature_chromosome_location_generated
set sfclg_strand = (select gff_strand
                    from gff3
                    where gff_feature = 'gene'
                      and gff_id = sfclg_acc_num
                        )
where sfclg_location_source = 'EnsemblStartEndLoader'
and sfclg_fdb_db_id = 7;
;