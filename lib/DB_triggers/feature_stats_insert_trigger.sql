      create trigger feature_stats_insert_trigger insert on feature_stats
          referencing new as new_fst
          for each row (execute procedure p_feature_stats_check_constraints(new_fst.fstat_pk_id,
							new_fst.fstat_feat_zdb_id,
							new_fst.fstat_superterm_zdb_id,
							new_fst.fstat_subterm_zdb_id,
							new_fst.fstat_fig_zdb_id,
							new_fst.fstat_pub_zdb_id,
							new_fst.fstat_xpatres_zdb_id)
       );