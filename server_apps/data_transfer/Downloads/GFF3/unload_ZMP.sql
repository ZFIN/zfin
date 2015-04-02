-- unload_ZMP.sql

! echo "unload_ZMP.sql"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_zmp.gff3' DELIMITER "	"
select gff_seqname,
       gff_source,
       gff_feature,
       gff_start,
       gff_end,
       gff_score,
       gff_strand,
       gff_frame,
       'ID=' || gff_id    --- FEATURE
       ||';Name=' || feature_abbrev
       ||';zdb_id=' || feature_zdb_id
       ||';Alias='|| feature_zdb_id || ','
                  || feature_abbrev || ','
                  || feature_name   || ';'  attribute
 from  gff3 join feature on feature_abbrev == gff_name
 where gff_source == "ZMP"
 order by 1,4,5,9
 ;
