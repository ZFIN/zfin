-- delete redundant alias which cannot contribute to history
-- because they are un-attributed and those where the
-- redundant alias attribution echoes the marker attribution

begin work;
! echo "find redundant alias without attribution"
! echo " and "
! echo "find redundant alias with redundant attribution"
select dalias_zdb_id zad
 from data_alias 
 join marker on dalias_data_zdb_id == mrkr_zdb_id
  and dalias_alias == mrkr_abbrev
 where dalias_group_id == 1
   and not exists (
    select 't' from record_attribution 
     where recattrib_data_zdb_id == dalias_zdb_id
 )
 union
 select dalias_zdb_id zad
 from data_alias
 join marker on dalias_data_zdb_id == mrkr_zdb_id
 join record_attribution redundant on redundant.recattrib_data_zdb_id == dalias_zdb_id
 join record_attribution existant on mrkr_zdb_id == existant.recattrib_data_zdb_id
 where dalias_group_id == 1
   and dalias_alias == mrkr_abbrev
   and redundant.recattrib_source_zdb_id == existant.recattrib_source_zdb_id
 into temp tmp_dup_alias with no log
;

! echo "dump the records of redundant alias into report file"
unload to 'deleted_redundant_alias' select * from tmp_dup_alias order by zad;

! echo "disconnect alias history from alias"
select mhist_zdb_id zad
 from marker_history where exists (
 select 't' from tmp_dup_alias
  where zad == mhist_dalias_zdb_id
) into temp tmp_dup_mhist with no log
;

update marker_history
  set mhist_dalias_zdb_id = null
 where mhist_zdb_id in (select zad from tmp_dup_mhist);

! echo "delete alias"
delete from zdb_active_data where exists (
  select 't' from tmp_dup_alias 
   where zad == zactvd_zdb_id
);

drop table tmp_dup_alias;
drop table tmp_dup_mhist;

rollback work;
--commit work;