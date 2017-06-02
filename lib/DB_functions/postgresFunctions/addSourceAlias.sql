create or replace function addSourceAlias (vJournalZdbId varchar, vOldJournalValue varchar)
     returns void as $$ 

       declare zdbId varchar := get_id('SALIAS');
       begin

       insert into zdb_Active_source
          values(zdbId);

       insert into source_alias (salias_Zdb_id, salias_source_zdb_id,
       	      	   				salias_alias)
          select zdbId, vJournalZdbId, vOldJournalValue
	  	 from single
		 where not exists (Select 'x' from source_alias
		       	   	  	  where salias_source_zdb_id = vJournalZdbId
					  and salias_alias = vOldJournalValue);
       
       end 

$$ LANGUAGE plpgsql;
