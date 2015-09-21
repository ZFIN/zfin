#! /private/bin/perl -w
#
# This monthly-run script queries the statistics on
# ZFIN nomenclature and expression pattern related
# to the ZGC project. The result is mailed to curators.
#
use strict;
use lib "<!--|ROOT_PATH|-->/server_apps/";
use ZFINPerlModules;

# set environment variables

$ENV{"INFORMIXDIR"}="<!--|INFORMIX_DIR|-->";
$ENV{"INFORMIXSERVER"}="<!--|INFORMIX_SERVER|-->";
$ENV{"ONCONFIG"}="<!--|ONCONFIG_FILE|-->";
$ENV{"INFORMIXSQLHOSTS"}="<!--|INFORMIX_DIR|-->/etc/<!--|SQLHOSTS_FILE|-->";

my $dbname = "<!--|DB_NAME|-->";

#--------------------------------------------------------------------------
chdir "<!--|ROOT_PATH|-->/server_apps/Reports/PATO";

system("$ENV{'INFORMIXDIR'}/bin/rm -f PhenotypeStatistics.txt");

#-------------------non-transgenics-------------------------------

my $sql =
'select distinct fmrel_mrkr_zdb_id
 from genotype,
	phenotype_experiment,
	fish_experiment,
	genotype_feature,
	feature_marker_relationship,
	fish
 where fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and fish_Zdb_id = genox_fish_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and geno_zdb_id = fish_genotype_Zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and fmrel_type = "is allele of"
 and get_feature_type(fmrel_ftr_zdb_id) != "TRANSGENIC_INSERTION";';

 my $ct1 = ZFINPerlModules->countData($sql);


$sql =
'select distinct mrel_mrkr_2_zdb_id
   from marker_relationship, 
	fish_str,
        fish_experiment, 
	genotype_feature,
	genotype,
        phenotype_experiment,
	feature_marker_relationship,
	fish
   where mrel_type = "knockdown reagent targets gene"
   and mrel_mrkr_1_zdb_id like "ZDB-MRPHLNO%" 
   and fishstr_fish_zdb_id = fish_Zdb_id
   and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
   and fish_genotype_Zdb_id = genofeat_geno_zdb_id
   and fish_zdb_id = genox_fish_zdb_id
   and geno_zdb_id = genofeat_geno_zdb_id
   and phenox_genox_zdb_id = genox_zdb_id
   and mrel_mrkr_1_zdb_id != fmrel_mrkr_zdb_id
   and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
   and geno_is_wildtype = "f";';

my $ct2 = ZFINPerlModules->countData($sql);

$sql =
'select distinct mrel_mrkr_2_zdb_id
   from marker_relationship,
	fish_str,
        fish_experiment,
	genotype,
        phenotype_experiment, fish
   where mrel_type = "knockdown reagent targets gene"
   and mrel_mrkr_1_zdb_id like "ZDB-MRPHLNO%" 
   and mrel_mrkr_1_zdb_id =fishstr_str_zdb_id
   and fishstr_fish_zdb_id = fish_zdb_id
   and phenox_genox_zdb_id = genox_zdb_id
   and geno_is_wildtype = "t"
   and fish_zdb_id = genox_fish_zdb_id
   and geno_zdb_id = fish_genotype_Zdb_id;';

my $ct3 = ZFINPerlModules->countData($sql);

$sql =
'select distinct fmrel_mrkr_zdb_id
 from genotype, 
	phenotype_experiment,
	fish_experiment,fish,
	genotype_feature, 
	feature_marker_relationship,
	image,
	figure
 where fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and genox_fish_zdb_id = fish_Zdb_id
 and fmrel_type = "is allele of" 
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null
 and get_feature_type(fmrel_ftr_zdb_id) != "TRANSGENIC_INSERTION";';

my $ct4 = ZFINPerlModules->countData($sql);

$sql =
'select distinct mrel_mrkr_2_zdb_id
   from marker_relationship, 
	fish_str,
        fish_experiment, fish,
	genotype_feature,
        phenotype_experiment,
	figure, 
	image, genotype
   where mrel_type = "knockdown reagent targets gene"
   and mrel_mrkr_1_zdb_id like "ZDB-MRPHLNO%" 
   and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
   and fishstr_fish_Zdb_id = fish_zdb_id
   and genox_fish_Zdb_id = fish_Zdb_id
   and fish_genotype_zdb_id = genofeat_geno_zdb_id
   and geno_zdb_id = genofeat_geno_zdb_id
   and phenox_genox_zdb_id = genox_zdb_id
   and phenox_fig_zdb_id = fig_zdb_id
   and img_fig_zdb_id = fig_zdb_id
   and img_image is not null
 and get_feature_type(genofeat_feature_zdb_id) != "TRANSGENIC_INSERTION"
 and geno_is_wildtype = "f";';

my $ct5 = ZFINPerlModules->countData($sql);

##---------------------------------features----------------------------

$sql=
'select distinct fmrel_ftr_zdb_id
 from genotype, phenotype_experiment, fish_experiment,
	genotype_feature, feature_marker_relationship, fish
 where fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and fish_genotype_zdb_id = geno_Zdb_id
 and genox_fish_zdb_id = fish_zdb_id
 and fmrel_type = "is allele of" 
 and get_feature_type(fmrel_ftr_zdb_id) != "TRANSGENIC_INSERTION";';

my $ct6 = ZFINPerlModules->countData($sql);

$sql =
'select distinct fmrel_ftr_zdb_id
 from genotype, phenotype_experiment, fish_experiment,
	genotype_feature, feature_marker_relationship,
	image, figure, fish
 where fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and fish_genotype_zdb_id = geno_Zdb_id
 and fmrel_type = "is allele of" 
 and phenox_fig_zdb_id = fig_zdb_id
 and fish_genotype_zdb_id = genofeat_geno_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null
 and get_feature_type(fmrel_ftr_zdb_id) != "TRANSGENIC_INSERTION";';

my $ct7 = ZFINPerlModules->countData($sql);

##---------------------------- transgenics-----------------------------------

$sql =
'select distinct fmrel_mrkr_zdb_id
 from genotype, 
	phenotype_experiment,
	fish_experiment,fish,
        genotype_feature, 
	feature_marker_relationship, 
	feature
 where fish_genotype_Zdb_id = geno_zdb_id
 and fish_Zdb_id = genox_Fish_zdb_id
and genofeat_geno_zdb_id = geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
and genofeat_feature_zdb_id = feature_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
and fmrel_type in ("contains phenotypic sequence feature")
 and get_feature_type(fmrel_ftr_zdb_id) = "TRANSGENIC_INSERTION"
 and get_obj_type(fmrel_mrkr_zdb_id) = "TGCONSTRCT"
and feature_zdb_id = genofeat_feature_zdb_id;';

my $ct8 = ZFINPerlModules->countData($sql);

$sql =
'select distinct fmrel_mrkr_zdb_id
 from genotype, phenotype_experiment, fish_experiment,
	genotype_feature, feature_marker_relationship,
	image, figure, fish
 where genox_fish_Zdb_id = fish_zdb_id
 and fish_genotype_zdb_id = genofeat_geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and fmrel_type in ("contains sequence feature", "contains phenotypic sequence feature") 
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null
  and get_feature_type(fmrel_ftr_zdb_id) = "TRANSGENIC_INSERTION";';

my $ct9 = ZFINPerlModules->countData($sql);

$sql =
'select distinct mrel_mrkr_2_zdb_id
   from marker_relationship, 
	experiment_condition,
        fish_experiment, fish, fish_str,
	genotype_feature,
	genotype,
        phenotype_experiment
   where mrel_type = "knockdown reagent targets gene"
   and mrel_mrkr_1_zdb_id like "ZDB-MRPHLNO%" 
   and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
   and fishstr_fish_zdb_id = fish_Zdb_id
   and fish_genotype_Zdb_id = genofeat_geno_zdb_id
   and fish_zdb_id = genox_fish_zdb_id
   and fish_genotype_zdb_id  = genofeat_geno_zdb_id
   and get_feature_type(genofeat_feature_zdb_id) = "TRANSGENIC_INSERTION"
   and geno_zdb_id = genofeat_geno_zdb_id
   and geno_is_wildtype = "f"
   and phenox_genox_zdb_id = genox_Zdb_id;';

my $ct10 = ZFINPerlModules->countData($sql);

$sql =
'select distinct mrel_mrkr_2_zdb_id
   from marker_relationship, 
        fish_experiment,fish,fish_str,
	genotype_feature,
        phenotype_experiment,
	figure, 
	image
   where mrel_type = "knockdown reagent targets gene"
   and mrel_mrkr_1_zdb_id like "ZDB-MRPHLNO%" 
   and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
   and fishstr_fish_zdb_id = fish_zdb_id
   and fish_zdb_id = genox_fish_zdb_id
   and fish_genotype_Zdb_id = genofeat_geno_zdb_id
   and phenox_genox_zdb_id = genox_zdb_id
   and phenox_fig_zdb_id = fig_zdb_id
   and img_fig_zdb_id = fig_zdb_id
   and img_image is not null
 and get_feature_type(genofeat_feature_zdb_id) = "TRANSGENIC_INSERTION";';

my $ct11 = ZFINPerlModules->countData($sql);

$sql =
'select distinct genofeat_feature_zdb_id
 from genotype, phenotype_experiment, fish_experiment,
	genotype_feature, fish
 where fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
and fish_zdb_id = genox_fish_zdb_id
 and get_feature_type(genofeat_feature_zdb_id) = "TRANSGENIC_INSERTION";';

my $ct12 = ZFINPerlModules->countData($sql);

$sql =
'select distinct genofeat_feature_zdb_id
 from genotype, phenotype_experiment, fish_experiment,
	genotype_feature, image, figure, fish
 where fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and fish_genotype_zdb_id = geno_Zdb_id
 and fish_zdb_id = genox_fish_zdb_id
 and get_feature_type(genofeat_feature_zdb_id) = "TRANSGENIC_INSERTION"
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null;';

my $ct13 = ZFINPerlModules->countData($sql);

$sql =
'select distinct fmrel_mrkr_zdb_id as gene_id
 from genotype, 
	phenotype_experiment,
	fish_experiment,fish,
	genotype_feature, 
	feature_marker_relationship,
	image,
	figure
 where fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and genox_fish_zdb_id = fish_Zdb_id
 and fish_genotype_zdb_id = geno_Zdb_id
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null
 and fmrel_type = "contains sequence feature"
 and get_feature_type(fmrel_ftr_zdb_id) = "TRANSGENIC_INSERTION"
and get_obj_type(fmrel_mrkr_zdb_id) = "TGCONSTRCT"
union
  select distinct fmrel_mrkr_zdb_id
	from expression_Experiment,
	feature_marker_relationship,
	expression_result,
	genotype, 
	fish_experiment,fish,
	expression_pattern_figure, 
	figure, 
	image, 
	genotype_feature, 
	feature
	where xpatex_zdb_id = xpatres_xpatex_zdb_id
	and xpatres_zdb_id = xpatfig_xpatres_zdb_id
	and xpatfig_fig_zdb_id = fig_Zdb_id
	and fig_zdb_id = img_fig_zdb_id
	and img_image is not null
        and xpatex_genox_zdb_id = genox_zdb_id
        and genox_fish_zdb_id = fish_Zdb_id
	and fish_genotype_zdb_id = geno_Zdb_id
	and geno_zdb_id = genofeat_geno_zdb_id
	and feature_zdb_id = genofeat_feature_zdb_id
        and feature_type = "TRANSGENIC_INSERTION" 
	and feature_zdb_id = fmrel_ftr_zdb_id
        and genofeat_feature_zdb_id = fmrel_ftr_Zdb_id
        and get_feature_type(fmrel_mrkr_zdb_id)="TRANSGENIC_CONSTRUCT"';

my $ct14 = ZFINPerlModules->countData($sql);

##-------------------------PAPERS---------------------------------

$sql =
 'select distinct fig_source_zdb_id
    from phenotype_experiment, publication, figure
    where fig_source_zdb_id = zdb_id
        and fig_zdb_id = phenox_fig_zdb_id
        and jtype in ("Curation", "Active Curation", "Unpublished");';

my $ct15 = ZFINPerlModules->countData($sql);

$sql =
'select distinct phenos_pk_id
    from phenotype_statement, publication, figure, phenotype_experiment
    where fig_source_zdb_id = zdb_id
        and fig_zdb_id = phenox_fig_zdb_id
        and phenox_pk_id = phenos_phenox_pk_id
        and jtype in ("Curation", "Active Curation", "Unpublished");';

my $ct16 = ZFINPerlModules->countData($sql);

$sql =
 'select distinct fig_source_zdb_id
    from phenotype_experiment, publication, figure
    where fig_source_zdb_id = zdb_id
        and phenox_fig_zdb_id  = fig_zdb_id
	    and jtype != "Curation"
        and jtype != "Unpublished";';

my $ct17 = ZFINPerlModules->countData($sql);

$sql =
 'select distinct phenos_pk_id
    from phenotype_statement;';

my $ct18 = ZFINPerlModules->countData($sql);

##------------------IMAGES xpat and PATO or just xpat-------------------------

$sql = 
  'select distinct xpatex_gene_zdb_id
	from expression_Experiment,
	expression_result,
	expression_pattern_figure, figure, image
	where xpatex_zdb_id = xpatres_xpatex_zdb_id
	and xpatres_zdb_id = xpatfig_xpatres_zdb_id
	and xpatfig_fig_zdb_id = fig_Zdb_id
	and fig_zdb_id = img_fig_zdb_id
	and img_image is not null;';

my $ct19 = ZFINPerlModules->countData($sql);

$sql =
'select distinct fmrel_mrkr_zdb_id as gene_id
 from genotype, phenotype_experiment, fish_experiment,fish,
	genotype_feature, feature_marker_relationship,
	image, figure
 where fish_genotype_Zdb_id = genofeat_geno_zdb_id
 and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
 and phenox_genox_zdb_id = genox_zdb_id
 and fish_genotype_zdb_id = geno_Zdb_id
 and genox_fish_zdb_id = fish_zdb_id
 and fmrel_type = "is allele of" 
 and phenox_fig_zdb_id = fig_zdb_id
 and img_fig_zdb_id = fig_zdb_id
 and img_image is not null
 and get_feature_type(fmrel_ftr_zdb_id) != "TRANSGENIC_INSERTION"
union
  select distinct xpatex_gene_zdb_id
	from expression_Experiment,
	expression_result,genotype, fish_experiment,fish,
	expression_pattern_figure, figure, image, genotype_feature, feature
	where xpatex_zdb_id = xpatres_xpatex_zdb_id
	and xpatres_zdb_id = xpatfig_xpatres_zdb_id
	and xpatfig_fig_zdb_id = fig_Zdb_id
	and fig_zdb_id = img_fig_zdb_id
	and img_image is not null
        and xpatex_genox_zdb_id = genox_zdb_id
        and genox_fish_zdb_id = fish_Zdb_id
	and fish_genotype_Zdb_id = geno_Zdb_id
	and geno_zdb_id = genofeat_geno_zdb_id
	and feature_zdb_id = genofeat_feature_zdb_id
        and feature_type != "TRANSGENIC_INSERTION"
union
  select distinct xpatex_gene_zdb_id
		from expression_Experiment,
	expression_result,expression_pattern_figure, figure, image
	where xpatex_zdb_id = xpatres_xpatex_zdb_id
	and xpatres_zdb_id = xpatfig_xpatres_zdb_id
	and xpatfig_fig_zdb_id = fig_Zdb_id
	and fig_zdb_id = img_fig_zdb_id
	and img_image is not null;';

my $ct20 = ZFINPerlModules->countData($sql);

###-------- New section for case 10249, STR additions to Monthly phenotype statistics --------------------------------

$sql = 'select distinct mrel_mrkr_2_zdb_id   
             from marker_relationship 
            where mrel_type = "knockdown reagent targets gene"
              and mrel_mrkr_1_zdb_id like "ZDB-TALEN%" 
              and exists(select "x" 
                           from fish_str
                           where fishstr_str_zdb_id = mrel_mrkr_1_zdb_id);';
                                                 

my $ct21 = ZFINPerlModules->countData($sql);

$sql = 'select distinct mrel_mrkr_2_zdb_id   
          from marker_relationship 
         where mrel_type = "knockdown reagent targets gene"
           and mrel_mrkr_1_zdb_id like "ZDB-CRISPR%" 
           and exists(select "x" from fish_str
                        where fishstr_str_zdb_id = mrel_mrkr_1_zdb_id);';
                                                 

my $ct22 = ZFINPerlModules->countData($sql);

$sql = 'select distinct mrel_mrkr_2_zdb_id
          from marker_relationship, fish_str, fish f1, fish_experiment fx1, phenotype_experiment px1, genotype
         where mrel_type = "knockdown reagent targets gene"
           and mrel_mrkr_1_zdb_id like "ZDB-TALEN%"
           and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
           and fx1.genox_zdb_id = px1.phenox_genox_zdb_id
           and fx1.genox_fish_zdb_id = f1.fish_zdb_id
           and f1.fish_genotype_zdb_id = geno_zdb_id
           and geno_is_wildtype = "t"
           and not exists(select "x" from phenotype_experiment px2, fish f2, fish_experiment fx2, genotype_feature, feature_marker_relationship
                           where fx2.genox_zdb_id = px2.phenox_genox_zdb_id
                             and fx2.genox_fish_zdb_id = f2.fish_zdb_id
                             and f2.fish_genotype_zdb_id = genofeat_geno_zdb_id
                             and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
                             and fmrel_mrkr_zdb_id = mrel_mrkr_2_zdb_id);';


my $ct23 = ZFINPerlModules->countData($sql);

$sql = 'select distinct mrel_mrkr_2_zdb_id
          from marker_relationship, fish_str, fish f1, fish_experiment fx1, phenotype_experiment px1, genotype
         where mrel_type = "knockdown reagent targets gene"
           and mrel_mrkr_1_zdb_id like "ZDB-CRISPR%"
           and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
           and fx1.genox_zdb_id = px1.phenox_genox_zdb_id
           and fx1.genox_fish_zdb_id = f1.fish_zdb_id
           and f1.fish_genotype_zdb_id = geno_zdb_id
           and geno_is_wildtype = "t"
           and not exists(select "x" from phenotype_experiment px2, fish f2, fish_experiment fx2, genotype_feature, feature_marker_relationship
                           where fx2.genox_zdb_id = px2.phenox_genox_zdb_id
                             and fx2.genox_fish_zdb_id = f2.fish_zdb_id
                             and f2.fish_genotype_zdb_id = genofeat_geno_zdb_id
                             and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
                             and fmrel_mrkr_zdb_id = mrel_mrkr_2_zdb_id);';

my $ct24 = ZFINPerlModules->countData($sql);

$sql =  'select distinct mrel_mrkr_2_zdb_id
           from marker_relationship, fish_str, fish, fish_experiment, genotype_feature, genotype, phenotype_experiment
          where mrel_type = "knockdown reagent targets gene"
            and mrel_mrkr_1_zdb_id like "ZDB-TALEN%"
            and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
            and genox_fish_zdb_id = fish_zdb_id
            and fish_genotype_zdb_id = genofeat_geno_zdb_id
            and geno_zdb_id = fish_genotype_zdb_id
            and geno_zdb_id = genofeat_geno_zdb_id
            and get_feature_type(genofeat_feature_zdb_id) = "TRANSGENIC_INSERTION"
            and geno_zdb_id = genofeat_geno_zdb_id
            and geno_is_wildtype = "f"
            and phenox_genox_zdb_id = genox_zdb_id;';
            
my $ct25 = ZFINPerlModules->countData($sql);

$sql =  'select distinct mrel_mrkr_2_zdb_id
           from marker_relationship, fish_str, fish, fish_experiment, genotype_feature, genotype, phenotype_experiment
          where mrel_type = "knockdown reagent targets gene"
            and mrel_mrkr_1_zdb_id like "ZDB-CRISPR%" 
            and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
            and genox_fish_zdb_id = fish_zdb_id
            and fish_genotype_zdb_id = genofeat_geno_zdb_id
            and geno_zdb_id = fish_genotype_zdb_id
            and geno_zdb_id = genofeat_geno_zdb_id
            and get_feature_type(genofeat_feature_zdb_id) = "TRANSGENIC_INSERTION"
            and geno_zdb_id = genofeat_geno_zdb_id
            and geno_is_wildtype = "f"
            and phenox_genox_zdb_id = genox_zdb_id;';
            
my $ct26 = ZFINPerlModules->countData($sql);

open (PHENO, ">PhenotypeStatistics.txt") || die "Cannot open PhenotypeStatistics.txt : $!\n";

print PHENO "\n\nNumber of genes with phenotypes, non-transgenic only: $ct1";
print PHENO "\n\nNumber of extra genes found via morpholinos with phenotypes, non-transgenic only, in mutant genotypes only: $ct2";
print PHENO "\n\nNumber of genes whose morpholinos are used in environments on WT genotypes that have phenotypes: $ct3";
print PHENO "\n\nNumber of genes with phenotypes and images: not transgenic: $ct4";
print PHENO "\n\nNumber of genes whose morpholinos are used in environments with non-transgenic, non-WT genotypes that have phenotypes and images: $ct5";
print PHENO "\n\nNumber of features with phenotypes; non-transgenic features only: $ct6";
print PHENO "\n\nNumber of features with phenotypes and images; non-transgenic features only: $ct7";
print PHENO "\n\nNumber of transgenic constructs with phenotypes (Transgenic constructs w/Phenotypes): $ct8";
print PHENO "\n\nNumber of transgenic constructs with phenotypes and images: $ct9";
print PHENO "\n\nNumber of distinct genes whose morpholinos are used in genotype environments, where the genotypes have tg insertion features and produce phenotypes: $ct10";
print PHENO "\n\nNumber of distinct genes whose morpholinos are used in genotype environments, where the genotypes have tg insertion features and produce phenotypes and have images: $ct11";
print PHENO "\n\nNumber of transgenic insertion features with phenotypes: $ct12";
print PHENO "\n\nNumber of transgenic insertion features with phenotypes and images: $ct13";
print PHENO "\n\nTotal number of distinct transgenic constructs with images (both tgconstructs with either FX images and/or tg constructs with PATO images: $ct14";
print PHENO "\n\nNumber of direct submission phenotype papers: $ct15";
print PHENO "\n\nNumber of direct submission phenotype records: $ct16";
print PHENO "\n\nNumber of non-curation, published papers with phenotypes: $ct17";
print PHENO "\n\nNumber of phenotypes (EQs) total: $ct18";
print PHENO "\n\nNumber of genes with expression images: $ct19";
print PHENO "\n\nTotal number of distinct genes with images (either FX or PATO images).  genes included: those in non-transgenic genotypes with phenotype images, those in non-transgeinc genotype-backgrounds with FX images, those in FX experiments: $ct20";   
print PHENO "\n\nNumber of Genes with TALEN (in environment) Phenotype: $ct21";
print PHENO "\n\nNumber of Genes with CRISPR (in environment) Phenotype: $ct22";
print PHENO "\n\nNumber of Genes with only TALEN phenotype in a WT background (TALEN in environment \n  and target gene for TALEN has no other alleles with associated phenotype): $ct23";
print PHENO "\n\nNumber of Genes with only CRISPR phenotype in a WT background (CRISPR in environment \n  and target gene for CRISPR has no other alleles with associated phenotype): $ct24";
print PHENO "\n\nNumber of Genes with TALEN phenotype in a Tg background (TALEN in environment): $ct25";
print PHENO "\n\nNumber of Genes with CRISPR phenotype in a Tg background (CRISPR in environment): $ct26";


close PHENO;

print "\n call FinCount.pl to get monthly fin phenotype count\n";
system ("<!--|ROOT_PATH|-->/server_apps/Reports/PATO/FinCount.pl");


#--------------------------------------------------------------------------
# send Ken counts of various gene name types with & without orthology
system("<!--|ROOT_PATH|-->/server_apps/Reports/Nomenclature/get_uninformative.sh");

exit;
