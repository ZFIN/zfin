create function regen_fish_components()
 returning integer 

  begin	-- master exception handler

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);

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
			       '" >> /tmp/regen_fish_components<!--|DB_NAME|-->';
	system exceptionMessage;

	-- Change the mode of the regen_oevdisp_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_fish_components_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.

	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.

	return -1;
      end
    end exception;


   let errorHint = "create temp table";
  begin work ;

	create temp table tmp_fish_components (fish_id varchar(50),
	       	    	  		       affector_id varchar(50), 
			  		       gene_id varchar(50),
					       construct_id varchar(50),
					       fish_name varchar(250),
					       genotype_id varchar(50)
					       )
	with no log ;

	create temp table tmp_fish_components_distinct (fish_id varchar(50),
	       	    	  		       affector_id varchar(50), 
			  		       gene_id varchar(50),
					       construct_id varchar(50),
					       fish_name varchar(250),
					       genotype_id varchar(50))
	with no log ;

   let errorHint = "insert into tmp_fish_components";

   --FEATURES
       insert into tmp_fish_components (fish_id, affector_id, gene_id, fish_name, genotype_id) 
       	      select fish_zdb_id, feature_zdb_id, a.mrkr_zdb_id, fish_name, fish_genotype_zdb_id
	      	from fish, genotype_feature,feature, outer (feature_marker_relationship c, outer marker a)
		where fish_genotype_zdb_id = genofeat_geno_zdb_id
		and genofeat_feature_zdb_id = feature_zdb_id
		and c.fmrel_ftr_zdb_id = feature_zdb_id
 		and c.fmrel_mrkr_zdb_id = a.mrkr_zdb_id
 		and c.fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
 		and feature_Type != 'TRANSGENIC_INSERTION';

   --CONSTRUCTS
      insert into tmp_fish_components (fish_id, affector_id, gene_id, construct_id, fish_name, genotype_id)
      	      select fish_zdb_id, feature_zdb_id, a.mrkr_Zdb_id, b.mrkr_zdb_id, fish_name, fish_genotype_zdb_id
	      	from fish, feature, genotype_feature,
     		      outer (feature_marker_relationship c, outer marker a),
      		      outer (feature_marker_relationship d, outer marker b)
   		where fish_genotype_zdb_id = genofeat_geno_zdb_id
   		and genofeat_feature_zdb_id = feature_zdb_id
   		and c.fmrel_ftr_zdb_id = feature_zdb_id
 		and c.fmrel_mrkr_zdb_id = a.mrkr_zdb_id
 		and c.fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
 		and d.fmrel_ftr_zdb_id = feature_zdb_id
 		and d.fmrel_mrkr_zdb_id = b.mrkr_zdb_id
 		and d.fmrel_type like 'contains%';

   --STRS
   insert into tmp_fish_components (fish_id, affector_id, gene_id, fish_name, genotype_id)
      	      select fish_zdb_id, fishstr_str_zdb_id, mrel_mrkr_2_zdb_id, fish_name, fish_genotype_zdb_id
	      	from fish ,fish_str,marker_relationship
		where fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
		and  fish_zdb_id = fishstr_fish_zdb_id
		and mrel_mrkr_2_Zdb_id like 'ZDB-GENE%';
		

    insert into tmp_fish_components_distinct (fish_id, affector_id, gene_id, construct_id, fish_name, genotype_id)	 
    	   select distinct fish_id, affector_id, gene_id, construct_id, fish_name, genotype_id
             from tmp_fish_components;


    delete from fish_Components;

    insert into fish_components (fc_fish_zdb_id, fc_affector_Zdb_id, fc_gene_zdb_id, fc_construct_zdb_id, fc_fish_name, fc_genotype_zdb_id)
       select fish_id, affector_id, gene_id, construct_id, fish_name, genotype_id
         from tmp_fish_components_distinct; 


    commit work;

  end -- global exception handler

  return 0;

end function;
