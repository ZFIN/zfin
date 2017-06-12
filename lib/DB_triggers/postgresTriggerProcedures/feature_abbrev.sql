drop trigger if exists feature_abbrev_trigger on publication;


create or replace function feature_abbrev()
returns trigger as
$BODY$

declare feature_abbrev feature.feature_abbrev%TYPE;
declare feature_abbrev_order feature.feature_abbrev_order%TYPE;

begin
     feature_abbrev = (select scrub_char(feature_abbrev));
     NEW.feature_abbrev = feature_abbrev;
     
     select checkFeatureAbbrev(NEW.feature_zdb_id,
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

     feature_abbrev_order = (select zero_pad(feature_abbrev_order));
     NEW.feature_abbrev_order = feature_abbrev_order;

     select fhist_event(NEW.feature_zdb_id,
       		'reassigned', NEW.feature_abbrev, OLD.feature_abbrev);
     
     select checkDupFeaturePrefixLineDesignation (NEW.feature_lab_prefix_id, NEW.feature_line_number);

     select populate_feature_tracking(NEW.feature_Abbrev, NEW.feature_name, NEW.feature_zdb_id); 

     RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;


create trigger feature_abbrev_trigger before update on feature
 for each row 
 when (OLD.feature_abbrev IS DISTINCT FROM NEW.feature_abbrev)
 execute procedure feature_abbrev();
