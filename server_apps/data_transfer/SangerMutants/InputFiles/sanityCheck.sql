begin work;

!echo 'create allelenomap table to dump alelle-ensdarg ids from sanger'
create temp table allelenomap (allelenomap varchar(50), ensdargid varchar(50), bkgrd varchar(3)) with no log;

!echo 'load  allelenomap table from input file sangerEnsdarg.unl'
load from sangerEnsdarg.unl insert into allelenomap;

!echo 'create allelemap table to dump alelle-known zfin gene  ids'
create temp table allelemap (allelemap varchar(50), zfingeneid varchar(50), bkgrd varchar(3)) with no log;

!echo 'load  allelemap table from input file sangerZfin.unl'
load from sangerZfin.unl insert into allelemap;


!echo 'make sure no input file has missing ensdarg or zfin gene ids, if so send these files to Leyla'
unload to 'nomapnoensdarg' select * from allelenomap where ensdargid is null;
unload to 'mapnogene' select * from allelemap where zfingeneid is null;

!echo 'dump features with multiple gene mappings to send to Leyla'
unload to 'mapmultiplegene' select allelemap from allelemap group by allelemap having count(allelemap) > 1;

create temp table multigeneftrs (feature varchar(50));
load from mapmultiplegene insert into multigeneftrs;

unload to 'multigeneftrs' select distinct feature, zfingeneid 
from multigeneftrs, allelemap
where feature=allelemap order by feature, zfingeneid;


!echo 'make sure no common feautures in both input files'
unload to 'commontoboth' select allelenomap, allelemap, ensdargid, zfingeneid from allelenomap, allelemap where trim(allelenomap)=trim(allelemap);

!echo 'dump of alleles with ensdarg matches but zfin has them mapped'
unload to 'ensdargmatches.unl' select distinct allelenomap,ensdargid, dblink_acc_num, dblink_linked_recid  from db_link, allelenomap where trim(ensdargid)=trim(dblink_acc_num); 

!echo 'creating a new file with allele and known ensdarg-zfin gene matches'

unload to 'alleleZfin1.unl' select distinct allelenomap, dblink_linked_recid,'TL'  from db_link, allelenomap where trim(ensdargid)=trim(dblink_acc_num); 

!echo 'alleleZfin1.unl loaded into temp table'
create temp table newallelemap (newallelemap varchar(50), ensdargid varchar(50), bkgrd varchar(3)) with no log;
load from alleleZfin1.unl insert into newallelemap;

!echo 'alleleZfin1.unl needs to be appended to sangerZfin.unl'

!echo 'records from alleleZfin1.unl(newallelemap) need to be deleted from  sangerEnsdarg.unl'

delete from allelenomap where allelenomap in (select distinct newallelemap from newallelemap);  

unload to 'correctedsangerEnsdarg.unl' select * from allelenomap;

!echo 'prepare final input file for Reno'
unload to uniqueEnsdargs.txt DELIMITER " " select distinct ensdargid from allelenomap; 

rollback work;
