create or replace function checkTscriptType (vTscriptZdbId text,  vMrelMrkr2Zdb_id text, vMrelType text)
returns void as $$
declare typeName transcript_type.tscriptt_type%TYPE;
begin
typeName = (select tscriptt_type from transcript_type, transcript where vTscriptZdbId = tscript_mrkr_zdb_id and tscript_type_id = tscriptt_pk_id);

if (typeName not in ('aberrant processed transcript', 'pseudogenic transcript', 'lincRNA',
   	     	    	       'piRNA', 'miRNA', 'pre miRNA', 'rRNA', 'snRNA', 'scRNA',
			       'snoRNA', 'tRNA', 'ncRNA')
     and vMrelType = 'transcript targets gene')
  then 
       raise exception 'FAIL!: only ncRNA transcripts can have targets relationships';
  end if;

end
$$ LANGUAGE plpgsql;
