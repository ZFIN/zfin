
begin work;

--truncate table gff3; only works if vega is not in there also

delete from gff3 where gff_source[1,8] == 'Ensembl_';


-- this file is only regenerated with new ensembl loads
! echo "load_drerio_ensembl.sql <- drerio_ensembl.unl"
-- expects the  drerio_ensembl.unl file to be copied to the local dir.
-- default cache location is /research/zprodmore/gff3/
-- can be changed in calling file (GFF3Files.pl)

load from 'drerio_ensembl.unl' insert into gff3;

-- we are not updating Ensembl names ...now

update statistics high for table gff3;


! echo "unload the set of background features which will remain constant"

! echo "load_drerio_ensembl.sql -> E_drerio_constant.gff3"
unload to '/tmp/E_drerio_constant.gff3_headless'  DELIMITER "	"
select
   gff_seqname,
   gff_source ,
   gff_feature,gff_start,gff_end,gff_score,gff_strand,gff_frame,
    "ID="       || gff_ID      ||
    ";Name="    || case gff_Name when NULL then "" else gff_Name end ||
    ";Parent="  || case
    				when gff_Parent IS NULL AND gff_feature != 'gene' AND gff_Name IS NOT NULL then gff_Name
    				when gff_Parent IS NULL AND gff_feature == 'gene' then ''
    				else gff_Parent end ||
    ";Alias="   || gff_ID
 from  gff3
 where gff_source[1,8] == 'Ensembl_'
   and gff_feature not in ('mRNA','transcript')
order by 1,4,3
;

-- to be valid the gff3 requires a header
! echo "##gff-version 3" >! /research/zprodmore/gff3/E_drerio_constant.gff3
! cat /tmp/E_drerio_constant.gff3_headless >> /research/zprodmore/gff3/E_drerio_constant.gff3
! rm  /tmp/E_drerio_constant.gff3_headless
! chmod g+w /research/zprodmore/gff3/E_drerio_constant.gff3
! cp -f /research/zprodmore/gff3/E_drerio_constant.gff3  <!--|ROOT_PATH|-->/home/data_transfer/Downloads/E_drerio_constant.gff3

-- commit/rollback applied externaly
