--liquibase formatted sql
--changeset christian:ZFIN-7612

-- three fish that have only wild type genotype / fish and STR
update fish set fish_genotype_zdb_id = 'ZDB-GENO-960809-7'
where fish_zdb_id in ('ZDB-FISH-160315-3','ZDB-FISH-160315-5','ZDB-FISH-160314-8');

update genotype_background set genoback_background_zdb_id = 'ZDB-GENO-960809-7'
from genotype, fish
where fish_genotype_zdb_id = geno_zdb_id
and genoback_geno_zdb_id = geno_zdb_id
and fish_zdb_id = 'ZDB-FISH-160314-7';

-- need to update the fish name
update fish set fish_name = get_fish_name(fish_zdb_id, 'ZDB-GENO-960809-7')
where fish_zdb_id in ('ZDB-FISH-160315-3','ZDB-FISH-160315-5','ZDB-FISH-160314-8');

update fish set fish_name = get_fish_name(fish_zdb_id, fish_genotype_zdb_id)
where fish_zdb_id in ('ZDB-FISH-160314-7','ZDB-FISH-160315-2','ZDB-FISH-160315-4','ZDB-FISH-160314-9');
