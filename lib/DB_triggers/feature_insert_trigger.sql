create trigger feature_insert_trigger insert on feature
  referencing new as new_feature
    for each row
        (
        execute function zero_pad(new_feature.feature_name 
    ) into feature.feature_name_order,
      execute function scrub_char(new_feature.feature_name 
    ) into feature.feature_name,
        execute function zero_pad(new_feature.feature_abbrev 
    ) into feature.feature_abbrev_order,
        execute function scrub_char(new_feature.feature_abbrev 
    ) into feature.feature_abbrev,
       execute procedure fhist_event(new_feature.feature_zdb_id,
       'assigned', new_feature.feature_name,new_feature.feature_abbrev) 
);
