{
	update_ottdarP.sql
	
	called by the "vega_public" target
}

begin work;
create table ottdarT_ottdarP ( --ot varchar (25) primary key, op varchar(50) 
	tp_ottdarT varchar(20), -- primary key, 
	tp_mrkrid varchar(50),
	tp_ottdarP varchar(20) 
)
fragment by round robin in tbldbs1 , tbldbs2 , tbldbs3
;

load from 'novel_vega_genes_generated.unl' insert into ottdarT_ottdarP;

create index ottdarT_ottdarP_tp_ottdarP_idx on ottdarT_ottdarP(tp_ottdarP);

update statistics for table ottdarT_ottdarP;


select tp_ottdarT, tp_ottdarP, count(*)
from ottdarT_ottdarP
group by 1, 2
having count(*) > 1;

! echo "Drop any incomming rows with NULL  ottdarP"
delete from ottdarT_ottdarP where tp_ottdarP is NULL;

! echo "Drop any incomming rows with 'no_translation'  ottdarP"
delete from ottdarT_ottdarP where tp_ottdarP == 'no_translation';

! echo "Drop existing vega links that are not continued"
delete from zdb_active_data where zactvd_zdb_id in(
        select dblink_zdb_id
         from db_link
         where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-090929-9' -- VEGAPROT
           and not exists (
                select 't' from ottdarT_ottdarP
                 where dblink_acc_num = tp_ottdarP
                   and tp_ottdarP is not NULL
        )
);

------------------------------------------------------------
! echo "drop incomming ottdarP that already have a Vega Link"
delete from ottdarT_ottdarP where exists (
        select 't' from db_link where dblink_acc_num = tp_ottdarP
);
update statistics for table ottdarT_ottdarP;

! echo "make the rest into new vega prot links"

update ottdarT_ottdarP set tp_mrkrid = (
    select tscript_mrkr_zdb_id from transcript where tp_ottdarT = tscript_load_id
)
where exists(select 't' from transcript where tp_ottdarT = tscript_load_id)
;

! echo "Drop any incomming ottdarT that fails to translate to transcript"
select * from ottdarT_ottdarP where tp_mrkrid[1,8] == 'OTTDARG0';
delete from ottdarT_ottdarP where tp_mrkrid[1,8] == 'OTTDARG0';

! echo "ensure remainder is unique"
select distinct tp_mrkrid tp_mrkr, tp_ottdarP tp_p, count(*) tp_count
 from ottdarT_ottdarP 
 group by 1,2
 having count(*) == 1
 into temp tmp_tp with no log;

delete from ottdarT_ottdarP
where not exists (
   select * from tmp_tp
   where tp_mrkr = tp_mrkrid
     and tp_p = tp_ottdarP
   )
;

drop table tmp_tp;

! echo "generate new dblinks"

alter table ottdarT_ottdarP add zad varchar (50);
update ottdarT_ottdarP set zad = get_id('DBLINK');

select tp_mrkrid from ottdarT_ottdarP
where not exists (
   select tp_mrkrid 
   from marker
   where mrkr_zdb_id = tp_mrkrid )
;

insert into zdb_active_data select zad from ottdarT_ottdarP;

insert into db_link  (
    dblink_linked_recid,
    dblink_acc_num,
    dblink_info,
    dblink_zdb_id,
--    dblink_acc_num_display,
--    dblink_length integer,
    dblink_fdbcont_zdb_id
  )
select distinct tp_mrkrid, tp_ottdarP,"uncurated " ||TODAY, zad ,'ZDB-FDBCONT-090929-9'  -- VEGAPROT
 from ottdarT_ottdarP where zad is not NULL and tp_ottdarP is not NULL
;

! echo "attribute"

insert into record_attribution (
    recattrib_data_zdb_id,
    recattrib_source_zdb_id
) select  zad, 'ZDB-PUB-030703-1' -- vega pub
 from ottdarT_ottdarP
 where zad is not NULL and tp_ottdarP is not NULL and tp_mrkrid is not NULL
;

drop table ottdarT_ottdarP;

--transaction terminated externally

