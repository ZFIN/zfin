create procedure checkFeatureAbbrev (vFeatureZdbId varchar(50),
       		 		     vFeatureType varchar(30), 
       		 		     vFeatureAbbrev varchar(70), 
				     vFeatureLabPrefixId int8, 
				     vFeatureLineNumber varchar(70),
				     vFeatureMrkrAbbrev varchar(60), 
				     vFeatureDfTranslocComplexPrefix varchar(60),
				     vFeatureDominant boolean, 
				     vFeatureUnspecified boolean,
				     vFeatureUnrecovered boolean,
				     vFeatureTgSuffix varchar(5),
				     vFeatureKnownInsertionSite boolean)

       define vFeatureLabPrefix like feature_prefix.fp_prefix;

       let vFeatureLabPrefix =
       	   (select fp_prefix 
	   	   from feature_prefix
		   where vFeatureLabPrefixId = fp_pk_id);

       if (vFeatureUnspecified ='t')
       then
          if (vFeatureAbbrev != vFeatureMrkrAbbrev||"_unspecified")
	  then raise exception -746,0,"FAIL!: unspecified allele must have abbrev like _unspecified. checkFeatureAbbrev.";
	  end if;
       elif (vFeatureUnrecovered ='t')
       then
          if (vFeatureAbbrev != vFeatureMrkrAbbrev||"_unrecovered")
	  then raise exception -746,0,"FAIL!: unrecovered allele must have abbrev like _unrecovered. checkFeatureAbbrev.";
	  end if;
       elif (vFeatureDominant = 't')
       then 
       	    if (vFeatureAbbrev not like 'd%')
       	    then raise exception -746,0,"FAIL!: dominant allele must have abbrev like d*. checkFeatureAbbrev.";
	    end if;
       elif (vFeatureType = 'TRANSGENIC_INSERTION' and vFeatureKnownInsertionSite = 'f')
       then 
	    if (vFeatureAbbrev != vFeatureLabPrefix||vFeatureLineNumber)
	    then raise exception -746,0,"FAIL!:tg insert not like labPrefix||lineNumber. checkFeatureAbbrev.";
	    end if;
       elif (vFeatureType = 'TRANSGENIC_INSERTION' and vFeatureKnownInsertionSite = 't')
       then
            if (vFeatureAbbrev != vFeatureLabPrefix||vFeatureLineNumber||vFeatureTgSuffix)
            then raise exception -746,0,"FAIL!:tg known insert not like labPrefix||lineNumber||tgSuffix checkFeatureAbbrev.";
	    end if;
       elif (vFeatureType = 'TRANSGENIC_UNSPECIFIED' and vFeatureUnspecified='f')
       	    then raise exception -746,0,"FAIL!:tg_unspecified is not marked unspecified in the db.";
       elif (vFeatureType in ('DEFICIENCY','COMPLEX','TRANSLOCATION','INVERSION'))
       then 
            if (vFeatureAbbrev != vFeatureLabPrefix||vFeatureLineNumber)
      	    then raise exception -746,0,"FAIL!: complex, DF, T must have abbrev like labPrefix||lineNumber. checkFeatureAbbrev.";
	    end if;
       else 
       	    if (vFeatureType != vFeatureLabPrefix||vFeatureLineNumber and vFeatureUnspecified = 'f' and vFeatureDominant = 'f' and vFeatureMrkrAbbrev is null and vFeatureKnownInsertionSite = 'f' and vFeatureDfTranslocComplexPrefix is null and vFeatureTgSuffix = 'f')
	    then raise exception -746,0,"FAIL!: feature_abbrev != fPrefix||fLineNumber. checkFeatureAbbrev.";
	    end if;
       end if;

--check that abbrev is prefix+suffix except when transgenic insertion, then tgconstruct+prefix+suffix
--if unspecified then mrkr_abbrev+_unspecified
	    
end procedure;
