create dba function "informix".regen_accession()	
  returning integer


    define humanEntrezGeneFdbContZdbId varchar(50);
    define mouseEntrezGeneFdbContZdbId varchar(50);

    define humanUniProtFdbContZdbId varchar(50);
    define mouseUniProtFdbContZdbId varchar(50);

    define humanOMIMFdbContZdbId varchar(50);
    define mouseMGIFdbContZdbId  varchar(50);

begin	-- master exception handler

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);
    define zdbFlagReturn integer;

    on exception
      set sqlError, isamError, errorText
      begin

	-- An error happened while function was running.

	on exception in (-206, -255, -668)

	  --  255: OK to get a "Not in transaction" here, since
	  --       we might not be in a transaction when the rollback work 
	  --       below is performed.
	  --  668: OK to get a "System command not executed" here.
	  --       Is probably the result of the chmod failing because we
	  --	   are not the owner.
	end exception with resume;

        let exceptionMessage = 'echo "' || CURRENT ||
			       ' SQL Error: '  || sqlError::varchar(200) || 
			       ' ISAM Error: ' || isamError::varchar(200) ||
			       ' ErrorText: '  || errorText || 
			       ' ErrorHint: '  || errorHint ||
			       '" >> /tmp/regen_accession_exception_<!--|DB_NAME|-->';
	system exceptionMessage;

	-- Change the mode of the regen_accession_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_accession_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.

	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.
	
        let zdbFlagReturn = release_zdb_flag("regen_accession");
	return -1;
      end
    end exception;


    -- -------------------------------------------------------------------
    --   GRAB ZDB_FLAG
    -- -------------------------------------------------------------------

    let errorHint = "Grab zdb_flag";
    if grab_zdb_flag("regen_accession") <> 0 then
      return 1;
    end if
	
    let errorHint = "Creating _temp";	

    if (exists( select *
		  from systables
		  where tabname = "accession_bank_temp")) then 
      drop table accession_bank_temp;
    end if


    if (exists( select *
		  from systables
		  where tabname = "accession_rel_temp")) then 
      drop table accession_rel_temp;
    end if

  begin work ;
    create table accession_bank_temp (
        entrez_acc varchar(50),
	other_acc varchar(50),
	acc_type varchar(255),
	fdbcont_zdb_id varchar(50),
	entrez_symbol varchar(60),
	entrez_name varchar(255),
	entrez_species varchar(60)	
      )
      fragment by round robin in tbldbs1, tbldbs2, tbldbs3
      extent size 1024 next size 1024
      lock mode page;

    let errorHint = "Create temp indexes";

    create unique index accbktemp_pk_index
       on accession_bank_temp (entrez_acc, other_acc, acc_type)
       using btree in idxdbs3;

    let errorHint = "Define human/mouse fdbcont variable";	

--the Uniprot fdbcont records for human an mouse 

    let humanUniProtFdbContZdbId = (select fdbcont_zdb_id
			      	  	  from foreign_db_contains
				 	  where fdbcont_fdb_db_name = "UniProt"
				 	   and fdbcont_fdbdt_super_type = "sequence"
				  	   and fdbcont_fdbdt_data_type = "Polypeptide"
				  	   and fdbcont_organism_common_name = "Human");

 
    let mouseUniProtFdbContZdbId = (select fdbcont_zdb_id
			      	  	  from foreign_db_contains
				 	  where fdbcont_fdb_db_name = "UniProt"
				 	   and fdbcont_fdbdt_super_type = "sequence"
				  	   and fdbcont_fdbdt_data_type = "Polypeptide"
				  	   and fdbcont_organism_common_name = "Mouse");


--the Entrez Gene fdbcont record for human and mouse ZDB-FDBCONT-040412-28

    let mouseEntrezGeneFdbContZdbId = (select fdbcont_zdb_id
			      	  	  from foreign_db_contains
				 	  where fdbcont_fdb_db_name = "Entrez Gene"
				  	    and fdbcont_fdbdt_super_type = "orthologue"
				  	    and fdbcont_fdbdt_data_type = "orthologue"
				  	    and fdbcont_organism_common_name = "Mouse");


    let humanEntrezGeneFdbContZdbId = (select fdbcont_zdb_id
			      	  	  from foreign_db_contains
				 	  where fdbcont_fdb_db_name = "Entrez Gene"
				 	   and fdbcont_fdbdt_super_type = "orthologue"
				  	   and fdbcont_fdbdt_data_type = "orthologue"
				  	   and fdbcont_organism_common_name = "Human");


--the OMIM and MGI fdbcont records for human and mouse ZDB-FDBCONT-040412-27

    let humanOMIMFdbContZdbId = (select fdbcont_zdb_id
			      	  	  from foreign_db_contains
				 	  where fdbcont_fdb_db_name = "OMIM"
				 	   and fdbcont_fdbdt_super_type = "orthologue"
				  	   and fdbcont_fdbdt_data_type = "orthologue"
				  	   and fdbcont_organism_common_name = "Human");

    let mouseMGIFdbContZdbId = (select fdbcont_zdb_id
			      	  	  from foreign_db_contains
				 	  where fdbcont_fdb_db_name = "MGI"
				 	   and fdbcont_fdbdt_super_type = "orthologue"
				  	   and fdbcont_fdbdt_data_type = "orthologue"
				  	   and fdbcont_organism_common_name = "Mouse"); 

--here we insert all related protein and entrez ids so that we can update existin
--entrez ids (from prior runs, that may not be used in blast_query or blast_hit, but also
--have not been deleted.
    let errorHint = "insert entrez and their protein relations into temp accession_bank";


    insert into accession_bank_temp (entrez_acc,
    	   			    other_acc,
				    entrez_species,
				    fdbcont_zdb_id,
				    acc_type,
				    entrez_symbol,
				    entrez_name)
    select distinct eon_entrez_id,
    	   	    eop_protein_acc,
		    eop_taxid,
    	   	    humanEntrezGeneFdbContZdbId,
		    "Human Protein to Entrez Accession",	            
		    eon_symbol,
		    eon_name
        from entrez_orth_prot, entrez_orth_name
	where eop_entrez_id = eon_entrez_id 
	and eop_taxid = "Human";


    insert into accession_bank_temp (entrez_acc,
    	   			    other_acc,
				    entrez_species,
				    fdbcont_zdb_id,
				    acc_type,
				    entrez_symbol,
				    entrez_name)
    select distinct eon_entrez_id,
    	   	    eop_protein_acc,
		    eop_taxid,
    	   	    mouseEntrezGeneFdbContZdbId,
		    "Mouse Protein to Entrez Accession",	            
		    eon_symbol,
		    eon_name
        from entrez_orth_prot, entrez_orth_name
	where eop_entrez_id = eon_entrez_id 
	and eop_taxid = "Mouse";

	
   let errorHint = "update existing entrez symbols";

   update accession_bank
      set accbk_abbreviation = (Select eon_symbol
      	  		          from entrez_orth_name
				  where accbk_acc_num = eon_entrez_id
				    and accbk_fdbcont_zdb_id in (mouseEntrezGeneFdbContZdbId,humanEntrezGeneFdbContZdbId)
				     and accbk_abbreviation != eon_symbol)
      where accbk_fdbcont_zdb_id in (mouseEntrezGeneFdbContZdbId,humanEntrezGeneFdbContZdbId)
      ;

  let errorHint = "update existing entrez names";

   update accession_bank
      set accbk_name = (Select eon_name
      	  		          from entrez_orth_name
				  where accbk_acc_num = eon_entrez_id
				    and accbk_fdbcont_zdb_id in (mouseEntrezGeneFdbContZdbId,humanEntrezGeneFdbContZdbId)
				    and accbk_abbreviation != eon_symbol)
     where accbk_fdbcont_zdb_id in (mouseEntrezGeneFdbContZdbId,humanEntrezGeneFdbContZdbId) ;
	 
   let errorHint = "Inserting new entrez ids into accession_bank ";

   insert into accession_bank (accbk_acc_num,
   	       		       accbk_fdbcont_zdb_id,
			       accbk_abbreviation,
			       accbk_name)
	select entrez_acc,
	       fdbcont_zdb_id,
	       entrez_symbol,
	       entrez_name
	  from accession_bank_temp
	  where not exists (Select 'x'
	  	    	   	   from accession_bank a
				   where a.accbk_acc_num = entrez_acc
				   and a.accbk_fdbcont_zdb_id = fdbcont_zdb_id
				   )
          and exists (select 'x'
      	    	   	   from accession_bank
			   where accbk_acc_num = other_acc
			   and accbk_fdbcont_zdb_id in (humanUniProtFdbContZdbId,
			       			        mouseUniProtFdbContZdbId) 
							); 

   let errorHint = "Inserting related MGI/OMIM ids into accession_bank ";

   insert into accession_bank (accbk_acc_num,
   	       		       accbk_fdbcont_zdb_id)
	select eox_xref,
	       case
		when eox_xref like 'MIM:%'
		 then humanOMIMFdbContZdbId
		when eox_xref like 'MGI:%'
		 then mouseMGIFdbContZdbId
		else null
		end
	  from entrez_orth_xref
	  where not exists (Select 'x'
	  	    	   	   from accession_bank
				   where accbk_acc_num = eox_xref
				   and accbk_fdbcont_zdb_id in (humanOMIMFdbContZdbId,mouseMGIFdbContZdbId)
				   )
           and exists (select 'x'
      	    	   	   from accession_bank
			   where accbk_acc_num = eox_entrez_id
			   and accbk_fdbcont_zdb_id in (humanEntrezGeneFdbContZdbId,
			       			        mouseEntrezGeneFdbContZdbId) 
							);
   
   let errorHint = "delete all from accession_relationship wipe it clean and reload";

   delete from accession_relationship ;


---HERE IS THE ERROR---
   let errorHint = "create accession_rel_temp" ;

    create table accession_rel_temp (
        entrez_acc_r varchar(50),
	other_acc_r varchar(50),
	acc_type_r varchar(65),
	entrez_pk_id_r int8,
	other_pk_id_r int8
	
      )

      fragment by round robin in tbldbs1, tbldbs2, tbldbs3
      extent size 1024 next size 1024
      lock mode page;

  let errorHint = "create accrel_temp_pk_index" ;

    create unique index acrel_pk_index 
      on accession_rel_temp (entrez_pk_id_r, other_pk_id_r, acc_type_r)
      using btree in idxdbs2 ;

  let errorHint = "create accrel_temp_ak_index";

    create unique index acrel_ak_index
      on accession_rel_temp (entrez_acc_r, other_acc_r, acc_type_r)
      using btree in idxdbs1 ;

  let errorHint = "enter entrez/uniprot rels into accrel_temp" ;

    insert into accession_rel_temp (entrez_acc_r, other_acc_r, acc_type_r, entrez_pk_id_r, other_pk_id_r)
      select a.accbk_acc_num, b.accbk_acc_num, acc_type, a.accbk_pk_id, b.accbk_pk_id 
         from accession_bank a, accession_bank b, accession_bank_temp
         where a.accbk_acc_num = entrez_acc
	 and b.accbk_acc_num = other_acc;
	 --and b.accbk_fdbcont_zdb_id = fdbcont_zdb_id ;

 let errorHint = "add unipqrot/entrez accession_relationship reload";

   insert into accession_relationship (accrel_zdb_id,
					accrel_accbk_pk_id_1,
					accrel_accbk_pk_id_2,
					accrel_accrelt_type)
      select get_id("ACCREL"),
       	     other_pk_id_r,
	     entrez_pk_id_r,
	     acc_type_r
    from accession_rel_temp ;

  let errorHint = "delete first temp rels" ;

  delete from accession_rel_temp ;

  let errorHint = "enter OMIM/MGI rels into accrel_temp" ;

     insert into accession_rel_temp (entrez_acc_r, other_acc_r, acc_type_r, entrez_pk_id_r, other_pk_id_r)
      select a.accbk_acc_num, 
      	     b.accbk_acc_num, 
      	       case
		when eox_xref like 'MIM:%'
		 then "Entrez to OMIM"
		when eox_xref like 'MGI:%'
		 then "Entrez to MGI"
		else null
		end, 
		a.accbk_pk_id, 
		b.accbk_pk_id 
         from accession_bank a, accession_bank b, entrez_orth_xref
         where a.accbk_acc_num = eox_entrez_id
	 and b.accbk_acc_num = eox_xref;
 
   let errorHint = "add entrez/MGI accession_relationship reload";

   insert into accession_relationship (accrel_zdb_id,
					accrel_accbk_pk_id_1,
					accrel_accbk_pk_id_2,
					accrel_accrelt_type)
      select get_id("ACCREL"),
        entrez_pk_id_r,
	other_pk_id_r,
	acc_type_r
    from accession_rel_temp;

    let errorHint = "Dropping accession_bank_temp and accession_rel_temp";	  
  
    drop table accession_bank_temp;
    drop table accession_rel_temp;   

    let errorHint = "commit work";

    commit work;

  end -- global exception handler

--  begin work ;

  -- update statistics for new table

--  update statistics high for table accession_bank;
   
--  update statistics high for table accession_relationship;

--  commit work ;

  -- -------------------------------------------------------------------
  --   RELEASE ZDB_FLAG
  -- -------------------------------------------------------------------
 
  if release_zdb_flag("regen_accession") <> 0 then
    return 1;
  end if

  return 0;
 

end function;

grant execute on function regen_accession () 
  to public as informix;