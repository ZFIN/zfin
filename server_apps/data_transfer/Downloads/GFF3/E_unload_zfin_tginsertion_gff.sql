-- E_unload_zfin_tginsertion_gff.sql
! echo "E_unload_zfin_tginsertion_gff.sql -> zfin_tginsertion.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_tginsertion.gff3' DELIMITER "	"
select gff_seqname,
       "ZFIN" gff_source,
       gff_feature,
       gff_start,
       gff_end,
       gff_score,
       gff_strand,
       gff_frame,
       'ID=' || feature_zdb_id    --- FEATURE
       ||';Name=' || feature_abbrev[1,8]
       ||';Alias='|| feature_zdb_id || ','
                  || feature_abbrev || ','
                  || feature_name   || ';'  attribute
 from  gff3 join feature on feature_abbrev[1,8] == gff_id
 where gff_source == "BurgessLin"
   and gff_feature == "Transgenic_insertion"
 order by 1,4,5,9
 ;

