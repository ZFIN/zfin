begin work;

create temp table ftr_ensdarg (feature varchar(50), ensdargid varchar(50)) with no log;

load from allelenozfin131203.inp insert into ftr_ensdarg;

unload to 'zfinGeneEnsdargMatches.unl' select feature,dblink_linked_recid,'TL',SUBSTR(feature,3) from ftr_ensdarg, db_link where ensdargid =dblink_acc_num order by dblink_linked_recid;

create temp table deletefromunknown (feature varchar(50), geneid varchar(50), bk varchar(50),linenum varchar(70)) with no log;
load from zfinGeneEnsdargMatches.unl insert into deletefromunknown;
delete from ftr_ensdarg where feature in (select feature from deletefromunknown); 

unload to InputSangerUnknown131203.unl select *,'TL' from ftr_ensdarg;
unload to sangerEnsdargs131213.txt select distinct ensdargid from ftr_ensdarg;


rollback work;
