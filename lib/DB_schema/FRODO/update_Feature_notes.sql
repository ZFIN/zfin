begin work ;

create temp table tmp_load_notes (geno_id varchar(50),
					note varchar(255))
 with no log;

load from EUtrans3Tabbed
  insert into tmp_load_notes ;


update feature
  set feature_comments = (select note
				from tmp_load_notes
				where exists (Select 'x'
						from genotype_Feature
						where genofeat_geno_zdb_id =
							geno_id
						and genofeat_feature_zdb_id =							feature_zdb_id))
  where exists (select 'x'
			from tmp_load_notes, genotype_Feature
			where genofeat_geno_zdb_id = geno_id
			and genofeat_feature_zdb_id = feature_zdb_id);



commit work ;

--rollback work ;