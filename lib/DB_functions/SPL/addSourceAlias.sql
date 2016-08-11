create procedure addSourceAlias (vJournalZdbId varchar(50), vOldJournalValue varchar(255))

       define zdbId varchar(50);
       let zdbId = get_id('SALIAS');

       insert into zdb_Active_source
          values(zdbId);

       insert into source_alias (salias_Zdb_id, salias_source_zdb_id,
       	      	   				salias_alias)
          select zdbId, vJournalZdbId, vOldJournalValue
	  	 from single
		 where not exists (Select 'x' from source_alias
		       	   	  	  where salias_source_zdb_id = vJournalZdbId
					  and salias_alias = vOldJournalValue);
       
end procedure;
