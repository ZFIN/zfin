create or replace  function regen_genotype_display()	
  returns int as $log$

  begin	-- master exception handler

	create temp table tmp_genotype (genotype_id varchar(255), 
				genotype_handle varchar(255),
				genotype_display varchar(255));


	insert into tmp_genotype
  		select geno_zdb_id, 'test', 'test'
    		from genotype 
    		where geno_is_wildtype = 'f';

	create unique index tg_index
  		on tmp_genotype(genotype_id); 


	update tmp_genotype
  		set genotype_handle = get_genotype_handle(genotype_id) ;


	update tmp_genotype
  		set genotype_display = get_genotype_display(genotype_id);


	delete from tmp_genotype
  		where genotype_handle is null ;

	delete from tmp_genotype
  		where genotype_handle = '' ;

	update genotype
  		set geno_handle = (select genotype_handle
					from tmp_genotype
					where genotype_id = geno_zdb_id
                         		and genotype_handle is not null 
			 		and genotype_handle != '')
  		where exists (select 'x'
		  		from tmp_genotype
                  		where geno_zdb_id = genotype_id);

	update genotype
  		set geno_display_name = (select genotype_display
						from tmp_genotype
						where genotype_id = geno_zdb_id
                         			and genotype_display is not null 
			 			and genotype_display != '')
  		where exists (select 'x'
		  		from tmp_genotype
                  		where geno_zdb_id = genotype_id
				and genotype_display is not null
				and genotype_display != '');


  return 0;

end;

$log$ LANGUAGE plpgsql;
