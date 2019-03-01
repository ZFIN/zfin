copy (
select
   gff_seqname,
   case coalesce(enm_ensdart_stable_id,'') when '' then gff_source else 'vega' end,
   gff_feature,gff_start,gff_end,gff_score,gff_strand,gff_frame,
    'ID='       || gff_ID      ||
    ';Name='    || nvl(mrkr_name,nvl(gff_name,'')) ||
    ';Parent='  || case coalesce(gff_Parent,'') when '' then '' else gff_Parent end ||
    case coalesce(enm_ensdart_stable_id,'') when '' then '' else (';zdb_id=' || enm_tscript_zdb_id) end ||
    ';Alias='   || gff_ID
 from ensdart_name_mapping
  right outer join gff3 on gff_ID = enm_ensdart_stable_id
  left outer join marker on mrkr_zdb_id = enm_tscript_zdb_id  
 where gff_source like  'Ensembl_%'
   and gff_feature <> 'gene'
order by 1,4,5,3,9 )  to 'E_drerio_transcript.gff3'  DELIMITER '	'
