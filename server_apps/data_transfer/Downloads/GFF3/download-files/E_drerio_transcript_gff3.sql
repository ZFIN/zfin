copy (
select
   gff_seqname,
   case coalesce(dblink_acc_num,'') when '' then gff_source else 'vega' end,
   gff_feature,gff_start,gff_end,gff_score,gff_strand,gff_frame,
    'ID='       || gff_ID      ||
    ';Name='    || nvl(mrkr_name,nvl(gff_name,'')) ||
    ';Parent='  || case coalesce(gff_Parent,'') when '' then '' else gff_Parent end ||
    case coalesce(dblink_linked_recid,'') when '' then '' else (';zdb_id=' || dblink_linked_recid) end ||
    ';Alias='   || gff_ID
 from db_link
  right outer join gff3 on gff_ID = dblink_acc_num
  left outer join marker on mrkr_zdb_id = dblink_linked_recid
 where substring(gff_source from 1 for 8) = 'Ensembl_'
   and gff_feature in ('mRNA','transcript')
order by 1,4,5,3,9 )  to 'E_drerio_transcript.gff3'  DELIMITER '	'
