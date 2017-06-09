begin work ;


drop table monthly_curated_average_metric;
drop table staging_webpages;
drop table sysblderrorlog;
drop table sysbldiprovided;
drop table sysbldirequired;
drop table sysbldobjdepends;
drop table sysbldobjects;
drop table sysbldobjkinds ;
drop table sysbldregistered;
drop table webcmimages ;
drop table webcmpages ;
drop table webconfigs;
drop table webenvvariables ;
drop table webpages;
drop table webtags; 
drop table webudrs;
drop table btsfse_storage ;
drop table affected_gene_group;
drop view vmrkrgoevsamesize;
drop view vgroupsize;

--TODO: why don't these process with the schema file
--drop table paneled_markers;
--drop table pub_db_xref;


commit work;
