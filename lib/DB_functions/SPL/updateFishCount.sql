create procedure updateFishCount ()

define vFish like fish.fish_zdb_id;
define vGeneCount int;
define vFishOrder like fish.fish_order;

foreach
  select distinct fish_Zdb_id into vFish from fish
  execute function getFishOrder(vFish) into vFishOrder,vGeneCount;
  update fish set fish_functional_affected_gene_count=vGeneCount where fish_zdb_id=vFish;
  update fish set fish_order=vFishOrder where fish_zdb_id=vFish;
end foreach

end procedure;
