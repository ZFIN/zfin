create or replace function regen_accession()	
  returns int as $log$


    declare humanEntrezGeneFdbContZdbId varchar(50);
     mouseEntrezGeneFdbContZdbId varchar(50);

     humanUniProtFdbContZdbId varchar(50);
     mouseUniProtFdbContZdbId varchar(50);

     humanOMIMFdbContZdbId varchar(50);
     mouseMGIFdbContZdbId  varchar(50);

begin	-- master exception handler

    if grab_zdb_flag("regen_accession") <> 0 then
      return 1;
    end if;

    drop table if exists accession_bank_temp;

    drop table if exists accession_rel_temp;

    create table accession_bank_temp (
        entrez_acc varchar(50),
	other_acc varchar(50),
	acc_type varchar(255),
	fdbcont_zdb_id text,
	entrez_symbol varchar(60),
	entrez_name varchar(255),
	entrez_species varchar(60)	
      );

    alter table accession_bank_temp 
     add constraint abtemp_unique unique
    (entrez_acc, other_acc, acc_type);
	

--the Uniprot fdbcont records for human an mouse 

    humanUniProtFdbContZdbId = (select fdbcont_zdb_id
			      	  	  from foreign_db_contains, foreign_db, foreign_db_data_type
				 	  where fdb_db_name = "UniProt"
				 	   and fdbdt_super_type = "sequence"
				  	   and fdbdt_data_type = "Polypeptide"
					   and fdb_db_pk_id = fdbcont_fdb_id
					   and fdbdt_pk_id = fdbcont_fdbdt_id
				  	   and fdbcont_organism_common_name = "Human");

 
    mouseUniProtFdbContZdbId = (select fdbcont_zdb_id
			      	  	  from foreign_db_contains, foreign_db, foreign_db_data_type
				 	  where fdb_db_name = "UniProt"
				 	   and fdbdt_super_type = "sequence"
				  	   and fdbdt_data_type = "Polypeptide"
					   and fdbcont_fdb_db_id = fdb_db_pk_id
					   and fdbcont_fdbdt_id = fdbdt_pk_id
				  	   and fdbcont_organism_common_name = "Mouse");


--the Entrez Gene fdbcont record for human and mouse ZDB-FDBCONT-040412-28

    mouseEntrezGeneFdbContZdbId = (select fdbcont_zdb_id
			      	  	  from foreign_db_contains, foreign_db, foreign_db_data_type
				 	  where fdb_db_name = "Entrez Gene"
				  	    and fdbdt_super_type = "ortholog"
				  	    and fdbdt_data_type = "ortholog"
				  	    and fdbcont_organism_common_name = "Mouse"
					    and fdbcont_fdb_db_id = fdb_db_pk_id
					    and fdbcont_fdbdt_id = fdbdt_pk_id);


    humanEntrezGeneFdbContZdbId = (select fdbcont_zdb_id
			      	  	  from foreign_db_contains, foreign_db, foreign_db_data_type
				 	  where fdb_db_name = "Entrez Gene"
				 	   and fdbdt_super_type = "ortholog"
				  	   and fdbdt_data_type = "ortholog"
				  	   and fdbcont_organism_common_name = "Human"
 					   and fdbcont_fdb_db_id = fdb_db_pk_id
					    and fdbcont_fdbdt_id = fdbdt_pk_id);


--the OMIM and MGI fdbcont records for human and mouse ZDB-FDBCONT-040412-27

    humanOMIMFdbContZdbId = (select fdbcont_zdb_id
			      	  	  from foreign_db_contains, foreign_db, foreign_db_data_type
				 	  where fdb_db_name = "OMIM"
				 	   and fdbdt_super_type = "ortholog"
				  	   and fdbdt_data_type = "ortholog"
				  	   and fdbcont_organism_common_name = "Human"
					    and fdbcont_fdb_db_id = fdb_db_pk_id
					    and fdbcont_fdbdt_id = fdbdt_pk_id);

    mouseMGIFdbContZdbId = (select fdbcont_zdb_id
			      	  	  from foreign_db_contains
				 	  where fdb_db_name = "MGI"
				 	   and fdbdt_super_type = "ortholog"
				  	   and fdbdt_data_type = "ortholog"
				  	   and fdbcont_organism_common_name = "Mouse"
					    and fdbcont_fdb_db_id = fdb_db_pk_id
					    and fdbcont_fdbdt_id = fdbdt_pk_id); 

--here we insert all related protein and entrez ids so that we can update existin
--entrez ids (from prior runs, that may not be used in blast_query or blast_hit, but also
--have not been deleted.

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

   update accession_bank
     set accbk_abbreviation = (Select eon_symbol
      	  		          from entrez_orth_name
				  where accbk_acc_num = eon_entrez_id
				    and accbk_fdbcont_zdb_id in (mouseEntrezGeneFdbContZdbId,humanEntrezGeneFdbContZdbId)
				     and accbk_abbreviation != eon_symbol)
      where accbk_fdbcont_zdb_id in (mouseEntrezGeneFdbContZdbId,humanEntrezGeneFdbContZdbId)
      ;

   update accession_bank
      set accbk_name = (Select eon_name
      	  		          from entrez_orth_name
				  where accbk_acc_num = eon_entrez_id
				    and accbk_fdbcont_zdb_id in (mouseEntrezGeneFdbContZdbId,humanEntrezGeneFdbContZdbId)
				    and accbk_abbreviation != eon_symbol)
     where accbk_fdbcont_zdb_id in (mouseEntrezGeneFdbContZdbId,humanEntrezGeneFdbContZdbId) ;

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
   
   delete from accession_relationship ;


---HERE IS THE ERROR---

    create table accession_rel_temp (
        entrez_acc_r varchar(50),
	other_acc_r varchar(50),
	acc_type_r varchar(65),
	entrez_pk_id_r int8,
	other_pk_id_r int8
	
      );


    alter table accession_rel_temp
     add constraint acrel_pk_index unique (entrez_pk_id_r, other_pk_id_r, acc_type_r)
    ;


    create unique index acrel_ak_index
      on accession_rel_temp (entrez_acc_r, other_acc_r, acc_type_r);


    insert into accession_rel_temp (entrez_acc_r, other_acc_r, acc_type_r, entrez_pk_id_r, other_pk_id_r)
      select a.accbk_acc_num, b.accbk_acc_num, acc_type, a.accbk_pk_id, b.accbk_pk_id 
         from accession_bank a, accession_bank b, accession_bank_temp
         where a.accbk_acc_num = entrez_acc
	 and b.accbk_acc_num = other_acc;
	 --and b.accbk_fdbcont_zdb_id = fdbcont_zdb_id ;

   insert into accession_relationship (accrel_zdb_id,
					accrel_accbk_pk_id_1,
					accrel_accbk_pk_id_2,
					accrel_accrelt_type)
      select get_id("ACCREL"),
       	     other_pk_id_r,
	     entrez_pk_id_r,
	     acc_type_r
    from accession_rel_temp ;


  delete from accession_rel_temp ;

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
 
   insert into accession_relationship (accrel_zdb_id,
					accrel_accbk_pk_id_1,
					accrel_accbk_pk_id_2,
					accrel_accrelt_type)
      select get_id("ACCREL"),
        entrez_pk_id_r,
	other_pk_id_r,
	acc_type_r
    from accession_rel_temp;

    drop table accession_bank_temp;
    drop table accession_rel_temp;   

  if release_zdb_flag('regen_accession') <> 0 then
    return 1;
  end if;

  return 0;
 
end ;

$log$ LANGUAGE plpgsql;
