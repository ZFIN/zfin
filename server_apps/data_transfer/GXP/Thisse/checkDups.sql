
create temp table probes_dup_check_tmp (
   prbdupchk_keyValue	 	varchar(50) not null primary key,
   prbdupchk_clone_name  	varchar(50) not null unique,
   prbdupchk_gene_zdb_id   	varchar(120) default null, 
   prbdupchk_gb5p 			varchar (50),
   prbdupchk_gb3p 			varchar (50),
   prbdupchk_library 		varchar(80),
   prbdupchk_digest 		varchar(20),
   prbdupchk_vector 		varchar(80),
   prbdupchk_pcr_amp 		lvarchar,
   prbdupchk_insert_kb 		float,
   prbdupchk_cloning_site 	varchar(20),
   prbdupchk_polymerase 	varchar(80),
   prbdupchk_comments 		lvarchar,              
   prbdupchk_modified 		varchar(20)          
)with no log;


load from './probes.raw' insert into probes_dup_check_tmp;


select prbdupchk_clone_name
  from probes_dup_check_tmp
group by prbdupchk_clone_name
having count(prbdupchk_clone_name) > 1;

 
select prbdupchk_gb5p
  from probes_dup_check_tmp
group by prbdupchk_gb5p
having count(prbdupchk_gb5p) > 1;

