-- unload_ZMP.sql

! echo "unload_ZMP.sql"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_zmp.gff3' DELIMITER "	"
select
       sfcl_chromosome,
       'source',
       'sequence_alteration',
       sfcl_start_position,
       sfcl_end_position,
       '.',
       '+',
       '.',
       'ID=' || '0'    --- FEATURE
       ||';Name=' || feature_abbrev
       ||';zdb_id=' || feature_zdb_id
       ||';Alias='|| feature_zdb_id || ','
                  || feature_abbrev || ','
                  || feature_name   || ';'
 from  sequence_feature_chromosome_location join feature on feature_zdb_id == sfcl_feature_zdb_id
 where sfcl_assembly == 'GRCz10'
 order by 1,4,5,9
 ;

