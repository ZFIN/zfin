
begin work;

delete from gff3 where gff_source == 'vega';

-- this file is only regenerated with new vega loads
! echo "load_drerio_vega_id.sql <- drerio_vega_id.unl"
-- expects the  drerio_vega_id.unl file to be copied to the local dir.
-- default cache location is /research/zprodmore/gff3/
-- can be changed in calling file (GFF3Files.pl)

load from 'drerio_vega_id.unl' insert into gff3;

--
update statistics high for table gff3;

-- commit/rollback applied externaly

