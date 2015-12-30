create function get_genotype_display( genoZdbId varchar(50) )

  returning lvarchar;	

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



  define genoDisplayHtml like genotype.geno_display_name;
  define featAbbrev	 like feature.feature_abbrev;
  define gcs	         like genotype_component_significance.gcs_significance;
 
  define featAbbrevHtml  lvarchar;
  define fad2		 lvarchar;
  define genoBackground  lvarchar;
  define zygAllele       lvarchar;
  define tgRepeat        boolean;
  define tgLastFeat      lvarchar;
  define tgFirstFeatHtml lvarchar;
  define featCount       integer;
  define tgLastMrkr      lvarchar;
  define featOrder       varchar(2);
  define fmrelType       like feature_marker_relationship.fmrel_type;

  define featSig         like feature_type.ftrtype_significance;
  define startName       like genotype.geno_display_name;
  define wildtype        like genotype.geno_is_wildtype;
  define featType	 like feature.feature_type;
  define zygOrder	 like zygocity.zyg_abbrev;
  define mrkrAbbrev	 like marker.mrkr_abbrev; 
  define featMrkrAbbrev	 varchar(255);

--set debug file to '/tmp/debug.txt';
--trace on;



  select geno_display_name, geno_is_wildtype 
  into startName, wildtype 
  from genotype where geno_zdb_id = genoZdbId;

  let fad2 = '';  
  let tgRepeat = 'f';
  let tgLastMrkr = '';
  let tgLastFeat = '';
  let fmrelType = '';
  let featCount = 0;

  if ( wildtype != 't') then  
  
    let genoDisplayHtml = '';
  
 
      ---add numbers to TG insertion significance in this routine.
--ZDB-GENO-100601-2
--ZDB-GENO-080905-5
--ZDB-GENO-080418-2 
--ZDB-GENO-080905-6 
--ZDB-GENO-090603-10
--ZDB-GENO-100215-6
--ZDB-GENO-100325-5

    foreach
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
	  and fmrel_type = "is allele of"
	  union 

	select distinct get_feature_abbrev_display(feature_zdb_id) as fad, 
                            zyg_allele_display, 
              case 
                when fmrel_type in ("contains innocuous sequence feature","created by","contains phenotypic sequence feature")
                then mrkr_abbrev
                else lower(get_feature_abbrev_display(feature_zdb_id)) 
                end as fad2,
	      feature_Abbrev,
	      feature_type,
              zyg_abbrev, 
              feature_abbrev, 
              case
	        when fmrel_type = "contains innocuous sequence feature"
   		then 24
		else
		  gcs_significance--,
	      end
              --fmrel_type
		   
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
	            and fm1.fmrel_type in ("contains innocuous sequence feature",
	                                   "contains phenotypic sequence feature")
		    and fm2.fmrel_type = "is allele of"
	       )

	  union 

	select distinct get_feature_abbrev_display(feature_zdb_id) as fad, 
                            zyg_allele_display, 
              case 
                when fmrel_type in ("contains innocuous sequence feature","created by","contains phenotypic sequence feature")
                then mrkr_abbrev
                else lower(get_feature_abbrev_display(feature_zdb_id)) 
                end as fad2,
	      feature_Abbrev,
	      feature_type,
              zyg_abbrev, 
              feature_abbrev, 
              case
	        when fmrel_type = "contains innocuous sequence feature"
   		then 24
		else
		  gcs_significance--,
	      end
              --fmrel_type
		   
          into featAbbrevHtml, zygAllele, mrkrAbbrev, featAbbrev, featType, zygOrder, featMrkrAbbrev, gcs--, fmrelType
          
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
	            and fm1.fmrel_type in ("contains innocuous sequence feature",
	                                   "contains phenotypic sequence feature")
		    and fm2.fmrel_type = "is allele of"
	       )
          and not exists (SElect 'x' from feature_marker_relationship
	      	  	 	 where fmrel_ftr_zdb_id = feature_Zdb_id
				 and fmrel_type = 'is allele of')

       order by gcs_significance asc, mrkr_abbrev asc , zyg_abbrev , fad2, fad desc
       

  if (fad2 == featAbbrevHtml) then
	       continue foreach;
       else
     
            if (featAbbrev like "%unspecified") then
               let featAbbrev = "unspecified";    
            end if
       	    if (featAbbrev like "%unrecovered") then
               let featAbbrev = "unrecovered";    
            end if
        
	
	    if (zygAllele == '/allele') then
               let zygAllele = '/' || featAbbrev;
	    else
	    end if
        

            
	    if (zygAllele is null) then
               let zygAllele = '';
            end if
               
            if (genoDisplayHtml == '') then
               let genoDisplayHtml =  featAbbrevHtml ;
          
	    else

		   let genoDisplayHtml = genoDisplayHtml ||'; '||  featAbbrevHtml;
       
            end if


            if (zygAllele != '') then
        
		if (featAbbrevHtml like "%<sup>%") then
            	  
            	  let genoDisplayHtml = genoDisplayHtml || '<sup>' || zygAllele || '</sup>';
                
                else
		   
		   let genoDisplayHtml = genoDisplayHtml || zygAllele ;
	        
	        end if
               
            end if

              
	   let fad2 = featAbbrevHtml ;
	   end if ;

   end foreach

    
    let genoDisplayHTML = replace(genoDisplayHTML,'</sup><sup>',''); 

  else
  
    let genoDisplayHTML = startName;
  
  end if

  return genoDisplayHtml;

end function;
