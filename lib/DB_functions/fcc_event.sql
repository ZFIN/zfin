create or replace function fcc_event(fcc_feature_zdb_id text,
                        fcc_functional_consequence text,
                        fcc_adult_viable boolean,
                        fcc_maternal_zygosity_examined boolean,
                        fcc_currently_available boolean,
                        fcc_other_line_information text,
                        fcc_date_added timestamp,
                        fcc_added_by text);
returns void as $$
begin

insert into feature_community_Contribution_audit (fcc_feature_zdb_id,
                        fcc_functional_consequence,
                        fcc_adult_viable,
                        fcc_maternal_zygosity_examined,
                        fcc_currently_available,
                        fcc_other_line_information,
                        fcc_date_added timestamp,
                        fcc_added_by)
values (fcc_feature_zdb_id,
                        fcc_functional_consequence,
                        fcc_adult_viable,
                        fcc_maternal_zygosity_examined,
                        fcc_currently_available,
                        fcc_other_line_information,
                        fcc_date_added timestamp,
                        fcc_added_by);

end
$$ LANGUAGE plpgsql
