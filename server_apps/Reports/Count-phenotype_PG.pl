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

my $dbname = "<!--|DB_NAME|-->";

print "\nStart running counting SQLs ...\n";

#--------------------------------------------------------------------------
chdir "<!--|ROOT_PATH|-->/server_apps/Reports/PATO";

system("/bin/rm -f PhenotypeStatistics.txt");

system("psql -d <!--|DB_NAME|--> -a -f count_phenotype.sql > PhenotypeStatistics.txt 2> err.txt");

###-------- New section for case 10249, STR additions to Monthly phenotype statistics --------------------------------

my $sql = "select distinct mrel_mrkr_2_zdb_id
             from marker_relationship 
            where mrel_type = 'knockdown reagent targets gene'
              and mrel_mrkr_1_zdb_id like 'ZDB-TALEN%'
              and exists(select 'x'
                           from fish_str
                           where fishstr_str_zdb_id = mrel_mrkr_1_zdb_id);";
                                                 

my $ct21 = ZFINPerlModules->countData($sql);

$sql = "select distinct mrel_mrkr_2_zdb_id
          from marker_relationship 
         where mrel_type = 'knockdown reagent targets gene'
           and mrel_mrkr_1_zdb_id like 'ZDB-CRISPR%'
           and exists(select 'x' from fish_str
                        where fishstr_str_zdb_id = mrel_mrkr_1_zdb_id);";
                                                 

my $ct22 = ZFINPerlModules->countData($sql);

$sql = "select distinct mrel_mrkr_2_zdb_id
          from marker_relationship, fish_str, fish f1, fish_experiment fx1, phenotype_experiment px1, genotype
         where mrel_type = 'knockdown reagent targets gene'
           and mrel_mrkr_1_zdb_id like 'ZDB-TALEN%'
           and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
           and fx1.genox_zdb_id = px1.phenox_genox_zdb_id
           and fx1.genox_fish_zdb_id = f1.fish_zdb_id
           and f1.fish_genotype_zdb_id = geno_zdb_id
           and geno_is_wildtype = 't'
           and not exists(select 'x' from phenotype_experiment px2, fish f2, fish_experiment fx2, genotype_feature, feature_marker_relationship
                           where fx2.genox_zdb_id = px2.phenox_genox_zdb_id
                             and fx2.genox_fish_zdb_id = f2.fish_zdb_id
                             and f2.fish_genotype_zdb_id = genofeat_geno_zdb_id
                             and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
                             and fmrel_mrkr_zdb_id = mrel_mrkr_2_zdb_id);";


my $ct23 = ZFINPerlModules->countData($sql);

$sql = "select distinct mrel_mrkr_2_zdb_id
          from marker_relationship, fish_str, fish f1, fish_experiment fx1, phenotype_experiment px1, genotype
         where mrel_type = 'knockdown reagent targets gene'
           and mrel_mrkr_1_zdb_id like 'ZDB-CRISPR%'
           and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
           and fx1.genox_zdb_id = px1.phenox_genox_zdb_id
           and fx1.genox_fish_zdb_id = f1.fish_zdb_id
           and f1.fish_genotype_zdb_id = geno_zdb_id
           and geno_is_wildtype = 't'
           and not exists(select 'x' from phenotype_experiment px2, fish f2, fish_experiment fx2, genotype_feature, feature_marker_relationship
                           where fx2.genox_zdb_id = px2.phenox_genox_zdb_id
                             and fx2.genox_fish_zdb_id = f2.fish_zdb_id
                             and f2.fish_genotype_zdb_id = genofeat_geno_zdb_id
                             and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
                             and fmrel_mrkr_zdb_id = mrel_mrkr_2_zdb_id);";

my $ct24 = ZFINPerlModules->countData($sql);

$sql =  "select distinct mrel_mrkr_2_zdb_id
           from marker_relationship, fish_str, fish, fish_experiment, genotype_feature, feature, genotype, phenotype_experiment
          where mrel_type = 'knockdown reagent targets gene'
            and mrel_mrkr_1_zdb_id like 'ZDB-TALEN%'
            and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
            and genox_fish_zdb_id = fish_zdb_id
            and fish_genotype_zdb_id = genofeat_geno_zdb_id
            and geno_zdb_id = fish_genotype_zdb_id
            and geno_zdb_id = genofeat_geno_zdb_id
            and genofeat_feature_zdb_id = feature_zdb_id
            and feature_type = 'TRANSGENIC_INSERTION'
            and geno_zdb_id = genofeat_geno_zdb_id
            and geno_is_wildtype = 'f'
            and phenox_genox_zdb_id = genox_zdb_id;";
            
my $ct25 = ZFINPerlModules->countData($sql);

$sql =  "select distinct mrel_mrkr_2_zdb_id
           from marker_relationship, fish_str, fish, fish_experiment, genotype_feature, feature, genotype, phenotype_experiment
          where mrel_type = 'knockdown reagent targets gene'
            and mrel_mrkr_1_zdb_id like 'ZDB-CRISPR%'
            and mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
            and genox_fish_zdb_id = fish_zdb_id
            and fish_genotype_zdb_id = genofeat_geno_zdb_id
            and geno_zdb_id = fish_genotype_zdb_id
            and geno_zdb_id = genofeat_geno_zdb_id
            and genofeat_feature_zdb_id = feature_zdb_id
            and feature_type = 'TRANSGENIC_INSERTION'
            and geno_zdb_id = genofeat_geno_zdb_id
            and geno_is_wildtype = 'f'
            and phenox_genox_zdb_id = genox_zdb_id;";
            
my $ct26 = ZFINPerlModules->countData($sql);

open (PHENO, ">>PhenotypeStatistics.txt") || die "Cannot open PhenotypeStatistics.txt : $!\n";

print PHENO "\n\nNumber of Genes with TALEN (used transiently) Phenotype: $ct21";
print PHENO "\n\nNumber of Genes with CRISPR (used transiently) Phenotype: $ct22";
print PHENO "\n\nNumber of Genes with only TALEN phenotype in a WT background (TALEN used transiently \n  and target gene for TALEN has no other alleles with associated phenotype): $ct23";
print PHENO "\n\nNumber of Genes with only CRISPR phenotype in a WT background (CRISPR used transiently \n  and target gene for CRISPR has no other alleles with associated phenotype): $ct24";
print PHENO "\n\nNumber of Genes with TALEN phenotype in a Tg background (TALEN used transiently): $ct25";
print PHENO "\n\nNumber of Genes with CRISPR phenotype in a Tg background (CRISPR used transiently): $ct26";


close PHENO;

print "\n call FinCount.pl to get monthly fin phenotype count\n";
system ("<!--|ROOT_PATH|-->/server_apps/Reports/PATO/FinCount.pl");


#--------------------------------------------------------------------------
# send Ken counts of various gene name types with & without orthology
system("<!--|ROOT_PATH|-->/server_apps/Reports/Nomenclature/get_uninformative.sh");

exit;
