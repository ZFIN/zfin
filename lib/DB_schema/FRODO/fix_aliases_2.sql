begin work ;

set constraints all deferred ;

--select feature_abbrev, feature_name
--		from data_alias, feature
--		where dalias_alias = feature_abbrev ;


delete from zdb_active_Data
  where exists (select 'x'
		from data_alias, feature
		where dalias_alias = feature_abbrev
		and zactvd_zdb_id = dalias_zdb_id
		and dalias_data_zdb_id = feature_zdb_id);

update feature_history
  set fhist_dalias_zdb_id = null
  where not exists (Select 'x' from data_alias
			where dalias_zdb_id = fhist_dalias_zdb_id)
  and fhist_dalias_zdb_id is not null;


update marker_history
  set mhist_dalias_zdb_id = null
  where not exists (Select 'x' from data_alias
			where dalias_zdb_id = mhist_dalias_zdb_id)
  and mhist_dalias_zdb_id is not null;

--CC terms must have whole organism AO terms too.

update atomic_phenotype
  set apato_entity_b_zdb_id =
                (select anatitem_zdb_id
                                from anatomy_item
                                where anatitem_name = 'whole organism')
  where exists (select 'x'
                        from go_term
                        where goterm_zdb_id = apato_Entity_a_zdb_id
                        and goterm_ontology = 'Cellular Component')
  and apato_entity_a_zdb_id like 'ZDB-GOTERM-%'
  and apato_entity_b_zdb_id is null ;


set constraints all immediate ;

--rollback work ;
commit work ;