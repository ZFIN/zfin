-- E_load_BL_gff.sql
! echo "E_load_BL_gff.sql <- Burgess_Lin.unl"

-- we do not know if this will work a second time 
-- since we do not know the update stratagy they will use.
begin work; 

delete from gff3 where gff_source == 'BurgessLin';
load from 'Burgess_Lin.unl' insert into gff3;
update statistics high for table gff3;

-- commit/rollback applied externaly
