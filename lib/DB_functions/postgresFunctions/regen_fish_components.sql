create or replace function regen_fish_components()
 returns text as $regen_fish_components$


  declare errorHint varchar(255);

  begin
	create temporary table tmp_fish_components (fish_id text,
	       	    	  		       affector_id text, 
			  		       gene_id text,
					       construct_id text,
					       fish_name varchar(250),
					       genotype_id text
					       );

	create temporary table tmp_fish_components_distinct (fish_id text,
	       	    	  		       affector_id text, 
			  		       gene_id text,
					       construct_id text,
					       fish_name varchar(250),
					       genotype_id text);

   errorHint = 'insert into tmp_fish_components';

   --FEATURES
       insert into tmp_fish_components (fish_id, affector_id, gene_id, fish_name, genotype_id) 
       	     select fish_zdb_id, feature_zdb_id, mrkr_zdb_id, fish_name, fish_genotype_zdb_id
	      	from fish
   		 join genotype_feature on genofeat_geno_zdb_id = fish_genotype_zdb_id
   		 join feature on genofeat_feature_zdb_id = feature_zdb_id
		 left outer join (feature_marker_relationship left outer join marker on mrkr_zdb_id = fmrel_mrkr_zdb_id ) on fmrel_ftr_Zdb_id = genofeat_feature_zdb_id and fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
		where feature_Type != 'TRANSGENIC_INSERTION';

		  
   --CONSTRUCTS
      insert into tmp_fish_components (fish_id, affector_id, gene_id, construct_id, fish_name, genotype_id)
      	      select fish_zdb_id, genofeat_feature_zdb_id, a.mrkr_Zdb_id, b.mrkr_zdb_id, fish_name, fish_genotype_zdb_id
	      	from fish
 		join genotype_feature on fish_genotype_zdb_id = genofeat_geno_zdb_id
		left outer join (feature_marker_relationship c left outer join marker a on a.mrkr_zdb_id = c.fmrel_mrkr_zdb_id) on c.fmrel_ftr_zdb_id = genofeat_feature_zdb_id and c.fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
     		left outer join (feature_marker_relationship d left outer join marker b on b.mrkr_zdb_id = d.fmrel_mrkr_zdb_id) on d.fmrel_ftr_zdb_id = genofeat_feature_zdb_id and d.fmrel_type like 'contains%';

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


 return 'success';
  return 'regen_genox() completed without error; success!';
  exception when raise_exception then
  	    return errorHint;

end;
$regen_fish_components$ LANGUAGE plpgsql;
