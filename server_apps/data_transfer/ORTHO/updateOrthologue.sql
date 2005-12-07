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


-- update orthologue table with chromosome info
update orthologue 
  set (ortho_chromosome, ortho_position) = 
      ((select orthloc_chromosome, orthloc_position 
           from tmp_ortholog_location 
           where lower(orthloc_symbol) = lower(ortho_abbrev)
                 and orthloc_organism = organism)) 
  where ortho_abbrev in 
       (select orthloc_symbol from tmp_ortholog_location) 
     and organism in 
       (select orthloc_organism from tmp_ortholog_location);      
  
commit work ;
-- rollback work ;
