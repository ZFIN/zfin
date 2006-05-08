-- update orthologue table with chromosome data from MGI, FlyBase and SGD

begin work ;

-- create a temporary table in order to update orthologue table with the chromosome info
create temp table tmp_ortholog_location (
     orthloc_symbol        varchar(30),
     orthloc_chromosome    varchar(30),
     orthloc_position      varchar(30),
     orthloc_organism      varchar(30)
) with no log ;

-- populate the temporary table for updating orthologue table with chromosome info
load from chromInfo.unl insert into tmp_ortholog_location;

update tmp_ortholog_location 
  set orthloc_symbol = lower(orthloc_symbol);

unload to duplicate_ortho_symbol.unl
select count(*), orthloc_symbol, orthloc_organism
  from tmp_ortholog_location
  group by orthloc_symbol, orthloc_organism
  having count(*) > 1;

create temp table tmp_dup (
     dup_symbol        varchar(30),
     dup_chromosome    varchar(30),
     dup_position      varchar(30),
     dup_organism      varchar(30)
) with no log ;

delete from tmp_ortholog_location
  where exists (select 'x'
  		  from tmp_dup
  		  where dup_symbol = orthloc_symbol
		  and orthloc_organism = dup_organism);

create unique index tmp_ortho_loc_index 
  on tmp_ortholog_location(orthloc_symbol, orthloc_organism)
  using btree in idxdbs3 ;

update statistics high for table tmp_ortholog_location ;

-- update orthologue table with chromosome info

update orthologue 
  set ortho_chromosome =  (select orthloc_chromosome
           		   from tmp_ortholog_location 
           		   where orthloc_symbol = lower(ortho_abbrev)
                 	   and orthloc_organism = organism)
  where exists
       (select 'x' 
	  from tmp_ortholog_location
          where orthloc_symbol = lower(ortho_abbrev)
	  and orthloc_organism = organism);     

update orthologue 
  set ortho_position =  (select orthloc_position
           		   from tmp_ortholog_location 
           		   where orthloc_symbol = lower(ortho_abbrev)
                 	   and orthloc_organism = organism)
  where exists
       (select 'x' 
	  from tmp_ortholog_location
          where orthloc_symbol = lower(ortho_abbrev)
	  and orthloc_organism = organism);      

commit work ;

--regenerate the display

execute function regen_oevdisp() ;

