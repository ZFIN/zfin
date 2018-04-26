
! echo "unload_vega_transcript.sql -> drerio_vega_transcript.gff3"

unload to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/drerio_vega_transcript.gff3'  DELIMITER "	"
select
   gff_seqname,gff_source,gff_feature,gff_start,gff_end,gff_score,gff_strand,gff_frame,
    case gff_feature
    when 'transcript' then
    "ID="       || gff_ID      ||
    ";Name="    || gff_Name    ||
    ";Parent="  || gff_Parent  ||
    ";biotype=" || gff_biotype ||
    ";zdb_id="  || case when tscript_load_id is NULL then "" else mrkr_zdb_id end ||
    ";Alias="   || gff_ID
    when 'gene' then
    "ID="       || gff_ID      ||
    ";Name="    || gff_Name    ||
    ";biotype=" || gff_biotype
    when 'exon' then
    "ID="       || gff_ID      ||
    ";Name="    || gff_Name    ||
    ";Parent="  || gff_Parent
    when 'CDS' then
    "ID="       || gff_ID      ||
    ";Name="    || gff_Name    ||
    ";Parent="  || gff_Parent
    end
from gff3, outer (transcript, marker) 
where gff_source == 'vega'
  and tscript_load_id = gff_ID 
  and mrkr_zdb_id == tscript_mrkr_zdb_id

order by 1::integer,4,3
;
