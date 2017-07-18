create or replace function p_markers_present_absent_exclusive (vFmrelMrkrId text,
       					  vFmrelFtrId text,
					  vFmrelRel text)
returns void as $$

    declare vOk int :=0 ;
     vFmrelId feature_marker_relationship.fmrel_zdb_id%TYPE := 0;
     vFeatureType feature.feature_type%TYPE := (select feature_type
        from feature
        where feature_zdb_id = vFmrelFtrId);
     vKnownInsertionSite feature.feature_known_insertion_site%TYPE := (select feature_known_insertion_site 
	from feature 
	where feature_zdb_id = vFmrelFtrId);
begin  
  if (vFmrelRel = 'markers moved') 
       then
        if (vFeatureType in ('POINT_MUTATION','INSERTION','DELETION','UNSPECIFIED'))
         then
		raise exception 'FAIL!: Ftr type can not have markers moved relationship';
        end if;
	
       vOk = (select count(*) 
       	     	     from feature_marker_relationship
       	     	     where fmrel_mrkr_Zdb_id = vfmrelmrkrid
		     and fmrel_ftr_zdb_id = vfmrelftrid
		     and fmrel_type in ('is allele of',
					'markers missing',
					'markers present'));
       if (vOk > 0)
       then 
   	    raise exception 'FAIL!: can not have m_present m_absent. p_markers_present_absent_exclusive';

       end if;
   
    end if;
    if (vFmrelRel = 'markers present') 
       then
       if (vFeatureType in ('POINT_MUTATION','INSERTION','DELETION','UNSPECIFIED'))
       	then
		raise exception 'FAIL!: Ftr type can not have markers present relationship';
       end if;
       vOk = (select count(*) 
       	     	     from feature_marker_relationship
       	     	     where fmrel_mrkr_Zdb_id = vfmrelmrkrid
		     and fmrel_ftr_zdb_id = vfmrelftrid
		     and fmrel_type in ('is allele of','markers missing','markers moved')
		     ));
       if (vOk > 0)
       then 
   	    raise exception 'FAIL!: can not have m_present m_absent. p_markers_present_absent_exclusive';

       end if;
   
    end if;

    if (vFmrelRel = 'markers missing') 
       then
       if (vFeatureType in ('POINT_MUTATION','INSERTION','DELETION','UNSPECIFIED'))
       	then
		raise exception 'FAIL!: Ftr type can not have markers missing relationship';
       end if;
       vOk = (select count(*) 
       	     	     from feature_marker_relationship
       	     	     where fmrel_mrkr_Zdb_id = vfmrelmrkrid
		     and fmrel_ftr_zdb_id = vfmrelftrid
		     and fmrel_type in ('markers present','is allele of','markers moved')
		     );
       if (vOk > 0)
       then 
   	    raise exception 'FAIL!: can not have m_present m_absent. p_markers_present_absent_exclusive';

       end if;
    end if ;
    if (vFmrelRel = 'is allele of') 
       then
       if (vFeatureType in ('POINT_MUTATION','INSERTION','DELETION','UNSPECIFIED') or (vFeatureType ='TRANSGENIC_INSERTION' and vKnownInsertionSite = 't'))
       	then
		vFmrelId := (Select count(*) from feature_marker_relationship 
		    	       	       where fmrel_ftr_zdb_id = vFmrelFtrId
				       and fmrel_type = 'is allele of');
		if (vFmrelId > '1')
		then
			raise exception 'FAIL!: feature type can not have more than one is allele of relationship';

       		end if;			
				
        end if;
       
        vOk = (select count(*) 
       	     	     from feature_marker_relationship
       	     	     where fmrel_mrkr_Zdb_id = vfmrelmrkrid
		     and fmrel_ftr_zdb_id = vfmrelftrid
		     and fmrel_type in ('markers missing','markers present','markers moved')
		  );
       if (vOk > 0)
       then 
   	    raise exception 'FAIL!: can not have m_present m_absent. p_markers_present_absent_exclusive';

       end if;
   
   
   end if;

end
$$ LANGUAGE plpgsql
