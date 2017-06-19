create or replace function get_genotype_display( genoZdbId varchar(50) )
returns text as $genoDisplayHtml$

  -- --------------------------------------------------------------------- 
  -- Given the ZDB ID of a genotype, returns the genotype display name in 
  -- html format. 
  -- [the same value that is stored in the genotype table. this function
  --  generates the value that is stored.]
  --
  --  INPUT VARS: 
  --     genoZdbId   
  --  
  --  OUTPUT VARS: 
  --     none
  -- 
  --  RETURNS:
  --     genotype display with proper HTML formatting. 
  --     NULL if genoZdbId does not exist in genotype table.
  --  
  --  EFFECTS:
  --     none
  -------------------------------------------------------------------------- 



  declare genoDisplayHtml  genotype.geno_display_name%TYPE;
   featAbbrev	  feature.feature_abbrev%TYPE;
   gcs	          genotype_component_significance.gcs_significance%TYPE;
 
   featAbbrevHtml  text;
   fad2		 text;
   genoBackground  text;
   zygAllele       text;
   tgRepeat        boolean;
   tgLastFeat      text;
   tgFirstFeatHtml text;
   featCount       integer;
   tgLastMrkr      text;
   featOrder       varchar(2);
   fmrelType        feature_marker_relationship.fmrel_type%TYPE;

   featSig          feature_type.ftrtype_significance%TYPE;
   startName        genotype.geno_display_name%TYPE;
   wildtype         genotype.geno_is_wildtype%TYPE;
   featType	  feature.feature_type%TYPE;
   zygOrder	  zygocity.zyg_abbrev%TYPE;
   mrkrAbbrev	  marker.mrkr_abbrev%TYPE; 
   featMrkrAbbrev	 varchar(255);

--set debug file to '/tmp/debug.txt';
--trace on;

 begin

  select geno_display_name, geno_is_wildtype 
  into startName, wildtype 
  from genotype where geno_zdb_id = genoZdbId;

   fad2 = '';  
   tgRepeat = 'f';
   tgLastMrkr = '';
   tgLastFeat = '';
   fmrelType = '';
   featCount = 0;

  if ( wildtype != 't') then  
  
     genoDisplayHtml = '';
  
    for featAbbrevHtml, zygAllele, mrkrAbbrev, featAbbrev, featType, zygOrder, featMrkrAbbrev, gcs in
       select distinct get_feature_abbrev_display(feature_zdb_id) as fad,  
              zyg_allele_display, 
	      case
		when mrkr_abbrev is null 
		then lower(get_feature_abbrev_display(feature_zdb_id))
		else lower(mrkr_abbrev)||get_feature_abbrev_display(feature_zdb_id)
                end as fad2,
	      feature_Abbrev,
	      feature_type,
	     zyg_abbrev, 
	     mrkr_abbrev, 
	     gcs_significance--,
	    -- fmrel_type
--into featAbbrevHtml, zygAllele, mrkrAbbrev, featAbbrev, featType, zygOrder, featMrkrAbbrev, gcs, fmrelType
         from feature, genotype_feature, zygocity, feature_type, feature_marker_relationship, marker, genotype_component_significance
        where genofeat_geno_zdb_id = genoZdbId
          and genofeat_feature_zdb_id = feature_zdb_id
          and genofeat_zygocity = zyg_zdb_id
	  and feature_type = ftrtype_name
	  and fmrel_mrkr_zdb_id = mrkr_zdb_id
	  and fmrel_ftr_zdb_id = feature_zdb_id
	  and fmrel_type = gcs_fmrel_type
 and feature_type = ftrtype_name
	  and gcs_mrkr_type = mrkr_type
	  and gcs_ftr_type = feature_type
	  and fmrel_type = 'is allele of'
	  union 
	select distinct get_feature_abbrev_display(feature_zdb_id) as fad, 
                            zyg_allele_display, 
              case 
                when fmrel_type in ('contains innocuous sequence feature','created by','contains phenotypic sequence feature')
                then mrkr_abbrev
                else lower(get_feature_abbrev_display(feature_zdb_id)) 
                end as fad2,
	      feature_Abbrev,
	      feature_type,
              zyg_abbrev, 
              feature_abbrev, 
              case
	        when fmrel_type = 'contains innocuous sequence feature'
   		then 24
		else
		  gcs_significance--,
	      end		   
         from feature, genotype_feature, zygocity, feature_type,
	      feature_marker_relationship as fm1, genotype_component_significance, marker
        where genofeat_geno_zdb_id = genoZdbId
          and genofeat_feature_zdb_id = feature_zdb_id
          and genofeat_zygocity = zyg_zdb_id
	  and feature_type = ftrtype_name
	  and fmrel_ftr_zdb_id = feature_zdb_id
	  and fmrel_mrkr_zdb_id = mrkr_zdb_id
 	  and feature_type = ftrtype_name
	  and feature_Type = 'DEFICIENCY'
	  and fmrel_type = gcs_fmrel_type
	  and gcs_mrkr_type = mrkr_type
	  and gcs_ftr_type = feature_type
	  and fmrel_type not in ('is allele of', 'created by','markers present','markers moved')
          and not exists (SElect 'x' from feature_marker_relationship
	      	  	 	 where fmrel_ftr_zdb_id = feature_Zdb_id
				 and fmrel_type = 'is allele of')
	  and not exists (
	          select *
	          from feature_marker_relationship as fm2
	          where fm2.fmrel_ftr_zdb_id = fm1.fmrel_ftr_zdb_id
	            and fm1.fmrel_type in ('contains innocuous sequence feature',
	                                   'contains phenotypic sequence feature')
		    and fm2.fmrel_type = 'is allele of'
	       )

	  union 
	select distinct get_feature_abbrev_display(feature_zdb_id) as fad, 
                            zyg_allele_display, 
              case 
                when fmrel_type in ('contains innocuous sequence feature','created by','contains phenotypic sequence feature')
                then mrkr_abbrev
                else lower(get_feature_abbrev_display(feature_zdb_id)) 
                end as fad2,
	      feature_Abbrev,
	      feature_type,
              zyg_abbrev, 
              feature_abbrev, 
              case
	        when fmrel_type = 'contains innocuous sequence feature'
   		then 24
		else
		  gcs_significance--,
	      end
         from feature, genotype_feature, zygocity, feature_type,
	      feature_marker_relationship as fm1, genotype_component_significance, marker
        where genofeat_geno_zdb_id = genoZdbId
          and genofeat_feature_zdb_id = feature_zdb_id
          and genofeat_zygocity = zyg_zdb_id
	  and feature_type = ftrtype_name
	  and fmrel_ftr_zdb_id = feature_zdb_id
	  and fmrel_mrkr_zdb_id = mrkr_zdb_id
 	  and feature_type = ftrtype_name
	  and feature_Type != 'DEFICIENCY'
	  and fmrel_type = gcs_fmrel_type
	  and gcs_mrkr_type = mrkr_type
	  and gcs_ftr_type = feature_type
	  and fmrel_type not in ('is allele of', 'created by','markers present','markers missing','markers moved')
	  and not exists (
	          select *
	          from feature_marker_relationship as fm2
	          where fm2.fmrel_ftr_zdb_id = fm1.fmrel_ftr_zdb_id
	            and fm1.fmrel_type in ('contains innocuous sequence feature',
	                                   'contains phenotypic sequence feature')
		    and fm2.fmrel_type = 'is allele of'
	       )
          and not exists (SElect 'x' from feature_marker_relationship
	      	  	 	 where fmrel_ftr_zdb_id = feature_Zdb_id
				 and fmrel_type = 'is allele of')

          order by gcs_significance asc, mrkr_abbrev asc , zyg_abbrev , fad2, fad desc
          loop

  	  if (fad2 = featAbbrevHtml) then
	       continue ;
       	  else
     
            if (featAbbrev like '%unspecified') then
                featAbbrev = 'unspecified';    
            end if;
       	    if (featAbbrev like '%unrecovered') then
                featAbbrev = 'unrecovered';    
            end if;
        
	
	    if (zygAllele = '/allele') then
                zygAllele = '/' || featAbbrev;
	    else
	    end if;
            
	    if (zygAllele is null) then
                zygAllele = '';
            end if;
               
            if (genoDisplayHtml = '') then
                genoDisplayHtml =  featAbbrevHtml ;
          
	    else

	       genoDisplayHtml = genoDisplayHtml ||'; '||  featAbbrevHtml;
       
            end if;


            if (zygAllele != '') then
        
		if (featAbbrevHtml like '%<sup>%') then
            	  
            	   genoDisplayHtml = genoDisplayHtml || '<sup>' || zygAllele || '</sup>';
                
                else
		   
		    genoDisplayHtml = genoDisplayHtml || zygAllele ;
	        
	        end if;
               
            end if;

              
	    fad2 = featAbbrevHtml ;
	   end if ;

   end loop;

    
     genoDisplayHTML = replace(genoDisplayHTML,'</sup><sup>',''); 

  else
  
     genoDisplayHTML = startName;
  
  end if;

  return genoDisplayHtml;

end
$genoDisplayHtml$ LANGUAGE plpgsql
