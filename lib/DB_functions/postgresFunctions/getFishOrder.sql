create or replace function getFishOrder (vFishId text,  out fishOrder bigint,  out numAffectedGene  int) as $func$


declare workingZyg  zygocity.zyg_name%TYPE;
 workingMrkr  genotype_feature.genofeat_feature_zdb_id%TYPE;
 existingMrkr  genotype_feature.genofeat_feature_zdb_id%TYPE := 'none';
 affectorType varchar(20);
 numFeatures  int8;
 affectiveZygosity  zygocity.zyg_name%TYPE;
 strExists int8;
 genoIsWT boolean := 'f';

begin

--find the functional number of affected genes.
numAffectedGene = 0;
for workingMrkr in
	--get the allele-ish genes
	select  fmrel_mrkr_zdb_id  
	   from fish, genotype_Feature, feature_marker_Relationship
	   where fish_genotype_zdb_id =genofeat_geno_zdb_id
   	   and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
	   and fish_zdb_id = vFishId
	   and fmrel_type in ('is allele of','markers missing','markers present','markers moved')
	 union
--get the non-tp53 morph genes
	select  mrel_mrkr_2_zdb_id 
           from marker_relationship, fish_str
	   where fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
	   and fishstr_fish_zdb_id = vFishId
 	   and mrel_mrkr_2_zdb_id !='ZDB-GENE-990415-270'
	   and mrel_mrkr_1_zdb_id like 'ZDB-MRPH%'
	 union
-- get the non-mrph str genes
	select  mrel_mrkr_2_zdb_id 
           from marker_relationship, fish_str
	   where fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
	   and fishstr_fish_zdb_id = vFishId
	   and (mrel_mrkr_1_zdb_id like 'ZDB-CRISPR%' or mrel_mrkr_1_zdb_id like 'ZDB-TALEN%')
	union
-- get the tp53-single mprhants 
	select mrel_mrkr_2_zdb_id
	  	 from marker_relationship, fish_str a,fish
		 where a.fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
		 and a.fishstr_fish_zdb_id = vFishId
		 and a.fishstr_fish_zdb_id=fish_zdb_id
		 and mrel_mrkr_1_zdb_id like 'ZDB-MRPH%'
		 and mrel_mrkr_2_zdb_id ='ZDB-GENE-990415-270' 
		 and not exists (Select 'x' 
		     	 	     from fish_str b
		       	       	     where a.fishstr_fish_zdb_id = b.fishstr_fish_zdb_id
				     and a.fishstr_str_zdb_id != b.fishstr_str_zdb_id)
-- get the double/triple morphant tp53 genes
 	union
	  select a.mrel_mrkr_2_zdb_id
	  	 from marker_relationship a, fish_str b, marker_relationship a2, fish_str b2
		 where b.fishstr_str_zdb_id = a.mrel_mrkr_1_zdb_id
		 and b.fishstr_fish_zdb_id = vFishId
		 and a.mrel_mrkr_2_zdb_id !='ZDB-GENE-990415-270'
		 and a.mrel_mrkr_1_zdb_id like 'ZDB-MRPH%'
		 and a2.mrel_mrkr_2_zdb_id = 'ZDB-GENE-990415-270'
		 and a2.mrel_mrkr_1_zdb_id like 'ZDB-MRPH%'
		 and b.fishstr_str_zdb_id != b2.fishstr_str_zdb_id
		 and b.fishstr_fish_zdb_id = b2.fishstr_fish_zdb_id

    loop 
	
	   if (existingMrkr = 'none')
	   then
		 existingMrkr := workingMrkr;
		 numAffectedGene := numAffectedGene + 1;
		 fishOrder := 10000000100;
		 raise notice 'existingMarkerNone: %', fishOrder;
           else
	     if (existingMrkr != workingMrkr)
	     then
		 numAffectedGene := numAffectedGene + 1;
		 fishOrder := fishOrder + 100100;
		 raise notice 'existingMarkerNotEqualWorkingMarker: %', fishOrder;
	     end if;
	   
	   end if; 

	   
end loop ;
raise notice 'endLoop: %', fishOrder;
raise notice 'existingMrkr: %', existingMrkr;

 genoIsWT = (select geno_is_wildtype from genotype, fish
    	       	       where fish_genotype_zdb_id = geno_Zdb_id
		       and fish_zdb_id = vFishId);

 strExists = (select count(*) from fish_str
    	      		where fishstr_fish_zdb_id = vFishId);
if (strExists is null)
then
  strExists = 0;
end if;

--if geno is WT, should always occur lower than other fish in the same category.  Geno is WT means its an STR fish.

if (genoIsWT = 't')
 then
     fishOrder := fishOrder::bigint + 25;
end if;

raise notice 'genoWT: %', fishOrder;

--more than 1 affected gene means fish is complex.
if (numAffectedGene > 1)
then
  fishOrder := fishOrder::bigint + 20000000000;
end if;

 numFeatures = (select count(*) from genotype_Feature, fish
    		  	  	   where fish_genotype_Zdb_id = genofeat_geno_Zdb_id
				   and fish_Zdb_id = vFishId);

--and more than 1 feature, then fish is also complex.
if (numFeatures > 1) 
then
 fishOrder := fishOrder::bigint + 20000000000;
else
	-- affective zygosity means: homozygous plus any number of STRs vs. heterozygous 
	 affectiveZygosity := (Select zyg_name from zygocity, genotype_Feature, fish
	    		      		where fish_genotype_zdb_id = genofeat_geno_zdb_id
					and genofeat_zygocity = zyg_zdb_id
					and fish_zdb_id = vFishId
					);
	if ((affectiveZygosity = 'heterozygous') and (strExists > 0) and (numAffectedGene = 1))
	then
		 fishOrder := fishOrder::bigint + 50;
	elsif (affectiveZygosity = 'heterozygous' and (strExists = 0) and (numAffectedGene = 1) )
	then 
		 fishOrder := fishOrder::bigint + 100;
	elsif (affectiveZygosity = 'complex')
	then 
	         fishOrder := fishOrder::bigint + 20000000000;
        elsif (affectiveZygosity = 'unknown')
	then
	         fishOrder := fishOrder::bigint + 10000500000;
        else		
		 fishOrder := fishOrder::bigint;
        end if;
	raise notice 'features=1: %', fishOrder;
end if;

raise notice 'end: %', fishOrder;
raise notice 'end: %', numAffectedGene;

--return fishOrder, numAffectedGene;

end;


$func$ LANGUAGE plpgsql ;
