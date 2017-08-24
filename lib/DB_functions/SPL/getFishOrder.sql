create function getFishOrder (vFishId varchar(50))
returning varchar(50), int8;

define fishOrder like fish.fish_order;
define numAffectedGene int8;
define workingZyg like zygocity.zyg_name;
define workingMrkr like genotype_feature.genofeat_feature_zdb_id;
define existingMrkr like genotype_feature.genofeat_feature_zdb_id;
define affectorType varchar(20);
define numFeatures int8;
define affectiveZygosity like zygocity.zyg_name;
define strExists int8;
define genoIsWT boolean;

let genoIsWT = 'f';
let numAffectedGene = 0;
let fishOrder = 9999999999;
let existingMrkr = "none";

--find the functional number of affected genes.
--hardcoding any MO's  that contain tp-53 (CLNDTY-7)
foreach 
	select  fmrel_mrkr_zdb_id into workingMrkr
	   from fish, genotype_Feature, feature_marker_Relationship
	   where fish_genotype_zdb_id =genofeat_geno_zdb_id
   	   and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
	   and fish_zdb_id = vFishId
	   and fmrel_type in ('is allele of','markers missing','markers present','markers moved')
	  union
        select  mrel_mrkr_2_zdb_id
           from marker_relationship, fish_str
           where fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
           and fishstr_fish_zdb_id = vFishId
and mrel_mrkr_2_zdb_id !='ZDB-GENE-990415-270'
 and mrel_mrkr_1_zdb_id like 'ZDB-MRPH%'

	  union
        select  mrel_mrkr_2_zdb_id
           from marker_relationship, fish_str
           where fishstr_str_zdb_id = mrel_mrkr_1_zdb_id
           and fishstr_fish_zdb_id = vFishId
	   and (mrel_mrkr_1_zdb_id like 'ZDB-CRISPR%' or mrel_mrkr_1_zdb_id like 'ZDB-TALEN%')
	   


	   
	   if (existingMrkr = "none")
	   then
		let existingMrkr = workingMrkr;
		let numAffectedGene = numAffectedGene + 1;
		let fishOrder = 10000000100;
           else
	     if (existingMrkr != workingMrkr)
	     then
		let numAffectedGene = numAffectedGene + 1;
		let fishOrder = fishOrder + 100100;
	     end if;
	   
	   end if; 

	   
end foreach ;

let genoIsWT = (select geno_is_wildtype from genotype, fish
    	       	       where fish_genotype_zdb_id = geno_Zdb_id
		       and fish_zdb_id = vFishId);

let strExists = (select count(*) from fish_str
    	      		where fishstr_fish_zdb_id = vFishId);
if (strExists is null)
then
 let strExists = 0;
end if;

--if geno is WT, should always occur lower than other fish in the same category.  Geno is WT means its an STR fish.

if (genoIsWT = 't')
 then
    let fishOrder = fishOrder + 25;
end if;


--more than 1 affected gene means fish is complex.
if (numAffectedGene > 1)
then
 let fishOrder = fishOrder + 20000000000;
end if;

let numFeatures = (select count(*) from genotype_Feature, fish
    		  	  	   where fish_genotype_Zdb_id = genofeat_geno_Zdb_id
				   and fish_Zdb_id = vFishId);

--and more than 1 feature, then fish is also complex.
if (numFeatures > 1) 
then
 let fishOrder = fishOrder + 20000000000;
else
	-- affective zygosity means: homozygous plus any number of STRs vs. heterozygous 
	let affectiveZygosity = (Select zyg_name from zygocity, genotype_Feature, fish
	    		      		where fish_genotype_zdb_id = genofeat_geno_zdb_id
					and genofeat_zygocity = zyg_zdb_id
					and fish_zdb_id = vFishId
					);
	if ((affectiveZygosity = "heterozygous") and (strExists > 0) and (numAffectedGene = 1))
	then
		let fishOrder = fishOrder + 50;
	elif (affectiveZygosity = "heterozygous" and (strExists = 0) and (numAffectedGene = 1) )
	then 
		let fishOrder = fishOrder + 100;
	elif (affectiveZygosity = "complex")
	then 
	        let fishOrder = fishOrder + 20000000000;
        elif (affectiveZygosity = "unknown")
	then
	        let fishOrder = fishOrder + 10000500000;
        else		
		let fishOrder = fishOrder;
        end if;

end if;

return fishOrder, numAffectedGene;

end function;