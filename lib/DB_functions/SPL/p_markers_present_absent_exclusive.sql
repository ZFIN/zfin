
create procedure p_markers_present_absent_exclusive (vFmrelMrkrId varchar(50),
       					  vFmrelFtrId varchar(50),
					  vFmrelRel varchar(50))

    define vOk int;
    define vFmrelId like feature_marker_relationship.fmrel_zdb_id;
    define vFeatureType like feature.feature_type;
    define vKnownInsertionSite like feature.feature_known_insertion_site;

  set debug file to '/tmp/trace.out';
  trace on ;

    let vOk = 0;
    let vFeatureType,vKnownInsertionSite = (select feature_type,feature_known_insertion_site from feature where feature_zdb_id = vFmrelFtrId);
    let vFmrelId = '0';


  
  if (vFmrelRel = 'markers moved') 
       then
        if (vFeatureType in ('POINT_MUTATION','INSERTION','DELETION','UNSPECIFIED'))
         then
		raise exception -746,0,"FAIL!: Ftr type can not have markers moved relationship";
        end if;
	
       let vOk = (select count(*) 
       	     	     from feature_marker_relationship
       	     	     where fmrel_mrkr_Zdb_id = vfmrelmrkrid
		     and fmrel_ftr_zdb_id = vfmrelftrid
		     and fmrel_type in ('is allele of','markers missing','markers present')
		     );
       if (vOk > 0)
       then 
   	    raise exception -746,0,"FAIL!: can not have m_present m_absent. p_markers_present_absent_exclusive";

       end if;
   
    end if;
    if (vFmrelRel = 'markers present') 
       then
       if (vFeatureType in ('POINT_MUTATION','INSERTION','DELETION','UNSPECIFIED'))
       	then
		raise exception -746,0,"FAIL!: Ftr type can not have markers present relationship";
       end if;
       let vOk = (select count(*) 
       	     	     from feature_marker_relationship
       	     	     where fmrel_mrkr_Zdb_id = vfmrelmrkrid
		     and fmrel_ftr_zdb_id = vfmrelftrid
		     and fmrel_type in ('is allele of','markers missing','markers moved')
		     );
       if (vOk > 0)
       then 
   	    raise exception -746,0,"FAIL!: can not have m_present m_absent. p_markers_present_absent_exclusive";

       end if;
   
    end if;

    if (vFmrelRel = 'markers missing') 
       then
       if (vFeatureType in ('POINT_MUTATION','INSERTION','DELETION','UNSPECIFIED'))
       	then
		raise exception -746,0,"FAIL!: Ftr type can not have markers missing relationship";
       end if;
       let vOk = (select count(*) 
       	     	     from feature_marker_relationship
       	     	     where fmrel_mrkr_Zdb_id = vfmrelmrkrid
		     and fmrel_ftr_zdb_id = vfmrelftrid
		     and fmrel_type in ('markers present','is allele of','markers moved')
		     );
       if (vOk > 0)
       then 
   	    raise exception -746,0,"FAIL!: can not have m_present m_absent. p_markers_present_absent_exclusive";

       end if;
    end if ;
    if (vFmrelRel = 'is allele of') 
       then
       if (vFeatureType in ('POINT_MUTATION','INSERTION','DELETION','UNSPECIFIED') or (vFeatureType ='TRANSGENIC_INSERTION' and vKnownInsertionSite = 't'))
       	then
		let vFmrelId = (Select count(*) from feature_marker_relationship 
		    	       	       where fmrel_ftr_zdb_id = vFmrelFtrId
				       and fmrel_type = 'is allele of');
		if (vFmrelId > '1')
		then
			raise exception -746,0,"FAIL!: feature type can not have more than one is allele of relationship";

       		end if;			
				
        end if;
       
       let vOk = (select count(*) 
       	     	     from feature_marker_relationship
       	     	     where fmrel_mrkr_Zdb_id = vfmrelmrkrid
		     and fmrel_ftr_zdb_id = vfmrelftrid
		     and fmrel_type in ('markers missing','markers present','markers moved')
		  );
       if (vOk > 0)
       then 
   	    raise exception -746,0,"FAIL!: can not have m_present m_absent. p_markers_present_absent_exclusive";

       end if;
   
   
   end if;

end procedure;