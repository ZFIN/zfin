drop trigger if exists feature_abbrev_trigger on feature;

create or replace function feature_abbrev()
returns trigger as
$BODY$

declare feature_abbrev feature.feature_abbrev%TYPE := scrub_char(NEW.feature_abbrev);
declare feature_abbrev_order feature.feature_abbrev_order%TYPE :=scrub_char(NEW.feature_abbrev_order);

begin

     
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
       		'reassigned', NEW.feature_abbrev, OLD.feature_abbrev);
     
     perform checkDupFeaturePrefixLineDesignation (NEW.feature_lab_prefix_id, NEW.feature_line_number);

     perform populate_feature_tracking(feature_abbrev, NEW.feature_name, NEW.feature_zdb_id); 

     RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;


create trigger feature_abbrev_trigger before update on feature
 for each row 
 when (OLD.feature_abbrev IS DISTINCT FROM NEW.feature_abbrev)
 execute procedure feature_abbrev();
