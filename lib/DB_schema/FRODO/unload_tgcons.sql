begin work ;

unload to tgcons
 select zdb_id, name, allele
   from fish where name like 'Tg%' 
  union 
    select distinct zdb_id, locus_name, allele
      from locus_registration
      where locus_name like 'Tg%'
	and locusreg_public_release_date is null
     ;



commit work ;