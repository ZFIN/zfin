--P_2_LINKAGE_PAIRS.SQL
------------------------------------------------------
--procedure to check inserts and/or updates on the linkage_pair_member table.
--assumptions: a linkage_pair must have 2 and only 2 members, 
--and linkage_pairs historically have not included
--more than 2 linkage_pair_members, so we should never have to account for 
--linkage_pairs
--that have more than 2 lkpair members. So we will alert the user if the 
--lkpair already has -->1 member (2 that is), but, if there are 
--already > 2 members for a linkage_pair, this procedure will not 
--indicate this to the user.  It will just not let the user add additional.
--REPLACES:
--sub linkagePairHas2Members

  create or replace function  p_2_linkage_pairs (vLMem varchar(25))
 returns void as $$

  declare vOk 	integer;
 begin
  create temp table if not exists tmp_lp1 as
  select lpmem_linkage_pair_zdb_id 
  from linkage_pair, linkage_pair_member 
  where lnkgpair_zdb_id = vLMem 
  group by lpmem_linkage_pair_zdb_id
  having count(*) > 1 ;

  vOk = (select count(*) from tmp_lp1);

  if vOk = 1 then
     raise exception 'FAIL!: This pair already has > 1 members!';
     drop table tmp_lp1;
  end if;

  drop table tmp_lp1;

end 
$$ LANGUAGE plpgsql
