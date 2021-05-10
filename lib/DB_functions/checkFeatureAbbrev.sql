create or replace function checkfeatureabbrev (vFeatureZdbId text,
       		 		     vFeatureType varchar(30), 
       		 		     vFeatureAbbrev varchar(70), 
				     vFeatureLabPrefixId int8, 
				     vFeatureLineNumber varchar(70),
				    
				     vFeatureDfTranslocComplexPrefix varchar(60),
				     vFeatureDominant boolean, 
				     vFeatureUnspecified boolean,
				     vFeatureUnrecovered boolean,
				     vFeatureTgSuffix varchar(5),
				     vFeatureKnownInsertionSite boolean)

returns void as $$
declare vFeatureLabPrefix  feature_prefix.fp_prefix%TYPE;
begin
        vFeatureLabPrefix =
           (select fp_prefix 
                   from feature_prefix
                   where vFeatureLabPrefixId = fp_pk_id);


       if (vFeatureUnspecified ='t')
       then
          if (vFeatureAbbrev not like '%\_unspecified')
	  then raise exception 'FAIL!: unspecified allele must have abbrev like _unspecified. checkFeatureAbbrev.';
	  end if;
       elsif (vFeatureUnrecovered ='t')
       then
          if (vFeatureAbbrev not like '%\_unrecovered')
	  then raise exception 'FAIL!: unrecovered allele must have abbrev like _unrecovered. checkFeatureAbbrev.';
	  end if;
       elsif (vFeatureDominant = 't')
       then 
       	    if (vFeatureAbbrev not like 'd%')
       	    then raise exception 'FAIL!: dominant allele must have abbrev like d*. checkFeatureAbbrev.';
	    end if;
       elsif (vFeatureType = 'TRANSGENIC_INSERTION')
       then
            if (vFeatureAbbrev != vFeatureLabPrefix||vFeatureLineNumber||vFeatureTgSuffix)
            then raise exception 'FAIL!:tg known insert not like labPrefix||lineNumber||tgSuffix checkFeatureAbbrev.';
	    end if;
       elsif (vFeatureType in ('DEFICIENCY','COMPLEX','TRANSLOCATION','INVERSION'))
       then 
            if (vFeatureAbbrev != vFeatureLabPrefix||vFeatureLineNumber)
      	    then raise exception 'FAIL!: complex, DF, T must have abbrev like labPrefix||lineNumber. checkFeatureAbbrev.';
	    end if;
       else 
       	    if (vFeatureAbbrev != vFeatureLabPrefix||vFeatureLineNumber and vFeatureUnspecified = 'f' and vFeatureDominant = 'f' and vFeatureKnownInsertionSite = 'f' and vFeatureDfTranslocComplexPrefix is null and vFeatureTgSuffix = 'f')
	    then raise exception 'FAIL!: feature_abbrev != fPrefix||fLineNumber. checkFeatureAbbrev.';
	    end if;
       end if;
end

$$ LANGUAGE plpgsql
