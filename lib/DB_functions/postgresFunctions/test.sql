begin work;


select a.mrel_mrkr_2_zdb_id 
from marker_relationship a, fish_str b, marker_relationship a2, fish_str b2
where b.fishstr_str_zdb_id = a.mrel_mrkr_1_zdb_id
and b.fishstr_fish_zdb_id ='ZDB-FISH-150901-12789' 
and a.mrel_mrkr_2_zdb_id !='ZDB-GENE-990415-270'
and a.mrel_mrkr_1_zdb_id like 'ZDB-MRPH%'
and a2.mrel_mrkr_2_zdb_id = 'ZDB-GENE-990415-270'
and a2.mrel_mrkr_1_zdb_id like 'ZDB-MRPH%'
and b.fishstr_str_zdb_id != b2.fishstr_str_zdb_id
and b.fishstr_fish_zdb_id = b2.fishstr_fish_zdb_id
;



rollback work;
