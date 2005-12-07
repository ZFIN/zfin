-- Script to unload current fly and yeast orthology data in order to ease parsing
--   of downloaded FlyBase and SGD files by extracting chromosome info of only  
--   those with orthologues in ZFIN.

begin work ;

-- unload current fly orthology data to file flyOrthoData.unl
unload to flyOrthoData.unl 
  select zdb_id,organism,ortho_abbrev,dblink_acc_num
    from orthologue,db_link
    where organism="Fly" 
      and zdb_id = dblink_linked_recid
    group by zdb_id,organism,ortho_abbrev,dblink_acc_num;

-- unload current yeast orthology data to file yeastOrthoData.unl
unload to yeastOrtho.unl 
  select organism,ortho_abbrev 
    from orthologue 
    where organism="Yeast"
    group by organism,ortho_abbrev;

commit work ;
--rollback work ;


