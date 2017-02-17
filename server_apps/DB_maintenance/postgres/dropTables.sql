begin work ;

set constraints all deferred;
 
drop table staging_webpages;
drop table sysblderrorlog;
drop table sysbldiprovided;
drop table sysbldirequired;
drop table sysbldobjdepends;
drop table sysbldobjects;
drop table sysbldobjkinds ;
drop table sysbldregistered;
drop table syserrors;
drop table systraceclasses; 
drop table systracemsgs;
drop table webcmimages ;
drop table webcmpages ;
drop table webconfigs;
drop table webenvvariables ;
drop table webpages;
drop table webtags; 
drop table webudrs;
drop table btsfse_storage ;
drop table affected_gene_group;


--TODO: why don't these process with the schema file
drop table paneled_markers;
drop table pub_db_xref;

alter table company 
  modify (entry_time datetime year to second default current year to second);

alter table lab 
  modify (entry_time datetime year to second default current year to second);

alter table person 
  modify (entry_time datetime year to second default current year to second);

alter table genotype
  modify (geno_date_entered datetime year to second default current year to second);

commit work;
