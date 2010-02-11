! echo "unload_zfin_transcript.sql -> zfin_tscript.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_tscript.gff3' DELIMITER "	"

select seqname,'ZFIN_transcript' source,feature,start,end,score,strand,frame,

case feature when 'transcript' then
 "ID="|| id||";Name="||mrkr_name||";Parent="||parent||";biotype="||tscriptt_type||";zdb_id=" || mrkr_zdb_id
else
 "ID="||id||";Name="||name||";Parent="|| parent
end attribute
  from  gff3, transcript, marker, transcript_type
  where source = 'vega' and feature != 'gene'
    and tscript_load_id = id
    and mrkr_zdb_id = tscript_mrkr_zdb_id
    and tscriptt_pk_id = tscript_type_id
 ;


-- to be valid the gff3 requires a header
! awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_tscript.gff3
