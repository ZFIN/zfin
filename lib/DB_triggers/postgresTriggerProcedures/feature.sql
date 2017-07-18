drop trigger if exists feature_trigger on feature;

create or replace function feature()
returns trigger as
$BODY$
declare feature_name feature.feature_name%TYPE;
declare feature_abbrev feature.feature_abbrev%TYPE;
declare feature_name_order feature.feature_name_order%TYPE;
declare feature_abbrev_order feature.feature_abbrev_order%TYPE;

begin

     feature_name = (select scrub_char(NEW.feature_name));
     NEW.feature_name = feature_name;

     feature_abbrev = (Select scrub_char(NEW.feature_abbrev));
     NEW.feature_abbrev = feature_name;
 
     feature_name_order = (Select zero_pad(NEW.feature_name_order));
     NEW.feature_name_order = feature_name_order;
   
     feature_abbrev_order = (Select zero_pad(NEW.feature_abbrev_order));
     NEW.feature_abbrev_order = feature_abbrev_order;

     perform checkFeatureAbbrev(NEW.feature_zdb_id,
       		 		     NEW.feature_type, 
       		 		     NEW.feature_abbrev, 
				     NEW.feature_lab_prefix_id, 
				     NEW.feature_line_number, 
				     NEW.feature_df_transloc_complex_prefix,
				     NEW.feature_dominant, 
				     NEW.feature_unspecified,
				     NEW.feature_unrecovered,
				     NEW.feature_tg_suffix,
				     NEW.feature_known_insertion_site);
  
     perform fhist_event(NEW.feature_zdb_id,
       		'assigned', NEW.feature_name,NEW.feature_abbrev);

     perform checkDupFeaturePrefixLineDesignation (NEW.feature_lab_prefix_id, NEW.feature_line_number);
     perform populate_feature_Tracking(NEW.feature_abbrev, NEW.feature_name, NEW.feature_zdb_id);

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger feature_trigger before insert on feature
 for each row
 execute procedure feature();
