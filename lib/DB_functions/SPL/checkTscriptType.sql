create procedure checkTscriptType (vTscriptZdbId varchar(50),  vMrelMrkr2Zdb_id varchar(50), vMrelType varchar(50))

define typeName like transcript_type.tscriptt_type;

let typeName = (select tscriptt_type from transcript_type, transcript where vTscriptZdbId = tscript_mrkr_zdb_id and tscript_type_id = tscriptt_pk_id);

if (typeName not in ('aberrant processed transcript', 'pseudogenic transcript', 'lincRNA',
   	     	    	       'piRNA', 'miRNA', 'pre miRNA', 'rRNA', 'snRNA', 'scRNA',
			       'snoRNA', 'tRNA', 'ncRNA')
     and vMrelType == 'transcript targets gene')
  then 
       raise exception -746,0,"FAIL!: only ncRNA transcripts can have targets relationships";
  end if;


end procedure;
