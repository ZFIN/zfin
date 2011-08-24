! echo "unload_zfin_transcript.sql -> zfin_tscript.gff3"

UNLOAD to '<!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_tscript.gff3' DELIMITER "	"

select gff_seqname,'ZFIN_transcript' gff_source,gff_feature,gff_start,gff_end,gff_score,gff_strand,gff_frame,
case gff_feature when 'transcript' then
 "ID="||gff_ID||";Name="||mrkr_gff_Name||";Parent="||gff_Parent||";biotype="||tscriptt_type||";zdb_id="||mrkr_zdb_id
else
 "ID="||gff_ID||";Name="||gff_Name||";Parent="|| gff_Parent
end attribute
  from  gff3, transcript, marker, transcript_type
  where gff_source = 'vega' 
    and gff_feature != 'gene'
    and tscript_load_id = gff_ID
    and mrkr_zdb_id = tscript_mrkr_zdb_id
    and tscriptt_pk_id = tscript_type_id
 ;

-- to be valid the gff3 requires a header
! awk '{a[NR]=$0}END{a[0]=h;for(i=0;i<=NR;i++)print a[i]>FILENAME}' h="##gff-version 3" <!--|ROOT_PATH|-->/home/data_transfer/Downloads/zfin_tscript.gff3
