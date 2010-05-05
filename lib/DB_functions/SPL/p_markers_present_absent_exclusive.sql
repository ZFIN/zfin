create procedure p_markers_present_absent_exclusive (vFmrelMrkrId varchar(50),
       					  vFmrelFtrId varchar(50),
					  vFmrelRel varchar(50))

    define vOk int;
    let vOk = 0;

    if (vFmrelRel = 'markers present') 
       then
       let vOk = (select count(*) 
       	     	     from feature_marker_relationship
       	     	     where fmrel_mrkr_Zdb_id = vfmrelmrkrid
		     and fmrel_ftr_zdb_id = vfmrelftrid
		     and fmrel_type in ('is allele of','markers missing')
		     );
       if (vOk > 0)
       then 
   	    raise exception -746,0,"FAIL!: can not have m_present m_absent. p_markers_present_absent_exclusive";

       end if;
   
    end if;

    if (vFmrelRel = 'markers missing') 
       then
       let vOk = (select count(*) 
       	     	     from feature_marker_relationship
       	     	     where fmrel_mrkr_Zdb_id = vfmrelmrkrid
		     and fmrel_ftr_zdb_id = vfmrelftrid
		     and fmrel_type in ('markers present','is allele of')
		     );
       if (vOk > 0)
       then 
   	    raise exception -746,0,"FAIL!: can not have m_present m_absent. p_markers_present_absent_exclusive";

       end if;
    end if ;

    if (vFmrelRel = 'is allele of') 
       then
       let vOk = (select count(*) 
       	     	     from feature_marker_relationship
       	     	     where fmrel_mrkr_Zdb_id = vfmrelmrkrid
		     and fmrel_ftr_zdb_id = vfmrelftrid
		     and fmrel_type in ('markers missing','markers present')
		  );
       if (vOk > 0)
       then 
   	    raise exception -746,0,"FAIL!: can not have m_present m_absent. p_markers_present_absent_exclusive";

       end if;
   
   
   end if;

end procedure;
