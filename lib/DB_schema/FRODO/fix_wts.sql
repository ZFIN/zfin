begin work ;

update genotype 
  set geno_handle = 'WIK'
  where geno_zdb_id = 'ZDB-GENO-010531-2' ;

update genotype 
  set geno_handle = 'NA'
  where geno_zdb_id = 'ZDB-GENO-030115-2' ;

update genotype 
  set geno_handle = 'WT'
  where geno_zdb_id = 'ZDB-GENO-030619-2' ;

update genotype 
  set geno_handle = 'AB'
  where geno_zdb_id = 'ZDB-GENO-960809-7' ;

update genotype 
  set geno_handle = 'IND'
  where geno_zdb_id = 'ZDB-GENO-980210-28' ;

update genotype 
  set geno_handle = 'HK/SING'
  where geno_zdb_id = 'ZDB-GENO-980210-38' ;

update genotype 
  set geno_handle = 'TL'
  where geno_zdb_id = 'ZDB-GENO-990623-2' ;

update genotype 
  set geno_handle = 'KOLN'
  where geno_zdb_id = 'ZDB-GENO-010725-1' ;

update genotype 
  set geno_handle = 'AB/TU'
  where geno_zdb_id = 'ZDB-GENO-010924-10' ;


update genotype 
  set geno_handle = 'C32'
  where geno_zdb_id = 'ZDB-GENO-030501-1' ;

update genotype 
  set geno_handle = 'AB/TL'
  where geno_zdb_id = 'ZDB-GENO-031202-1' ;

update genotype 
  set geno_handle = 'WIK/AB'
  where geno_zdb_id = 'ZDB-GENO-050511-1' ;

update genotype 
  set geno_handle = 'INDO'
  where geno_zdb_id = 'ZDB-GENO-980210-32' ;

update genotype 
  set geno_handle = 'HK/AB'
  where geno_zdb_id = 'ZDB-GENO-980210-40' ;

update genotype 
  set geno_handle = 'TU'
  where geno_zdb_id = 'ZDB-GENO-990623-3' ;

update genotype 
  set geno_handle = 'DAR'
  where geno_zdb_id = 'ZDB-GENO-960809-13' ;

update genotype 
  set geno_handle = 'SING'
  where geno_zdb_id = 'ZDB-GENO-980210-24' ;

update genotype 
  set geno_handle = 'HK'
  where geno_zdb_id = 'ZDB-GENO-980210-34' ;

update genotype 
  set geno_handle = 'SJD'
  where geno_zdb_id = 'ZDB-GENO-990308-9' ;

update genotype 
  set geno_handle = 'EKW'
  where geno_zdb_id = 'ZDB-GENO-990520-2' ;

update genotype 
  set geno_handle = 'HK/SING'
  where geno_zdb_id = 'ZDB-GENO-980210-38' ;


commit work ;

--rollback work ;