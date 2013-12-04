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

system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> PhenoytpeCount.sql > PhenotypeStatistics.txt 2> err.txt");

###-------- New section for case 10249, STR additions to Monthly phenotype statistics --------------------------------

my $sql = 'select distinct mrel_mrkr_2_zdb_id   
             from marker_relationship 
            where mrel_type = "knockdown reagent targets gene"
              and mrel_mrkr_1_zdb_id like "ZDB-TALEN%" 
              and exists(select "x" 
                           from experiment_condition, genotype_experiment, phenotype_experiment 
                          where mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id 
                            and expcond_exp_zdb_id = genox_exp_zdb_id 
                            and genox_zdb_id = phenox_genox_zdb_id);';
                                                 

my $ctGenes1 = ZFINPerlModules->countData($sql);

$sql = 'select distinct mrel_mrkr_2_zdb_id   
          from marker_relationship 
         where mrel_type = "knockdown reagent targets gene"
           and mrel_mrkr_1_zdb_id like "ZDB-CRISPR%" 
           and exists(select "x" 
                        from experiment_condition, genotype_experiment, phenotype_experiment 
                       where mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id 
                         and expcond_exp_zdb_id = genox_exp_zdb_id 
                         and genox_zdb_id = phenox_genox_zdb_id);';
                                                 

my $ctGenes2 = ZFINPerlModules->countData($sql);

$sql = 'select distinct mrel_mrkr_2_zdb_id   
          from marker_relationship, experiment_condition, genotype_experiment gx1, phenotype_experiment px1, genotype 
         where mrel_type = "knockdown reagent targets gene" 
           and mrel_mrkr_1_zdb_id like "ZDB-TALEN%" 
           and mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id 
           and expcond_exp_zdb_id = gx1.genox_exp_zdb_id 
           and gx1.genox_zdb_id = px1.phenox_genox_zdb_id 
           and gx1.genox_geno_zdb_id = geno_zdb_id 
           and geno_is_wildtype = "t" 
           and not exists(select "x" from phenotype_experiment px2, genotype_experiment gx2, genotype_feature, feature_marker_relationship
                           where gx2.genox_zdb_id = px2.phenox_genox_zdb_id
                             and gx2.genox_geno_zdb_id = genofeat_geno_zdb_id
                             and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
                             and fmrel_mrkr_zdb_id = mrel_mrkr_2_zdb_id);';
                                                 

my $ctGenes3 = ZFINPerlModules->countData($sql);

$sql = 'select distinct mrel_mrkr_2_zdb_id   
          from marker_relationship, experiment_condition, genotype_experiment gx1, phenotype_experiment px1, genotype 
         where mrel_type = "knockdown reagent targets gene" 
           and mrel_mrkr_1_zdb_id like "ZDB-CRISPR%" 
           and mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id 
           and expcond_exp_zdb_id = gx1.genox_exp_zdb_id 
           and gx1.genox_zdb_id = px1.phenox_genox_zdb_id 
           and gx1.genox_geno_zdb_id = geno_zdb_id 
           and geno_is_wildtype = "t" 
           and not exists(select "x" from phenotype_experiment px2, genotype_experiment gx2, genotype_feature, feature_marker_relationship
                           where gx2.genox_zdb_id = px2.phenox_genox_zdb_id
                             and gx2.genox_geno_zdb_id = genofeat_geno_zdb_id
                             and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
                             and fmrel_mrkr_zdb_id = mrel_mrkr_2_zdb_id);';

my $ctGenes4 = ZFINPerlModules->countData($sql);

$sql =  'select distinct mrel_mrkr_2_zdb_id
           from marker_relationship, experiment_condition, genotype_experiment, genotype_feature, genotype, phenotype_experiment
          where mrel_type = "knockdown reagent targets gene"
            and mrel_mrkr_1_zdb_id like "ZDB-TALEN%" 
            and mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id
            and expcond_exp_zdb_id = genox_exp_zdb_id
            and genox_geno_zdb_id = genofeat_geno_zdb_id
            and geno_zdb_id = genox_geno_zdb_id
            and geno_zdb_id = genofeat_geno_zdb_id
            and get_feature_type(genofeat_feature_zdb_id) = "TRANSGENIC_INSERTION"
            and geno_zdb_id = genofeat_geno_zdb_id
            and geno_is_wildtype = "f"
            and phenox_genox_zdb_id = genox_zdb_id;';
            
my $ctGenes5 = ZFINPerlModules->countData($sql);

$sql =  'select distinct mrel_mrkr_2_zdb_id
           from marker_relationship, experiment_condition, genotype_experiment, genotype_feature, genotype, phenotype_experiment
          where mrel_type = "knockdown reagent targets gene"
            and mrel_mrkr_1_zdb_id like "ZDB-CRISPR%" 
            and mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id
            and expcond_exp_zdb_id = genox_exp_zdb_id
            and genox_geno_zdb_id = genofeat_geno_zdb_id
            and geno_zdb_id = genox_geno_zdb_id
            and geno_zdb_id = genofeat_geno_zdb_id
            and get_feature_type(genofeat_feature_zdb_id) = "TRANSGENIC_INSERTION"
            and geno_zdb_id = genofeat_geno_zdb_id
            and geno_is_wildtype = "f"
            and phenox_genox_zdb_id = genox_zdb_id;';
            
my $ctGenes6 = ZFINPerlModules->countData($sql);

open (PHENO, ">>PhenotypeStatistics.txt") || die "Cannot open PhenotypeStatistics.txt : $!\n";

print PHENO "\n\nNumber of Genes with TALEN (in environment) Phenotype: $ctGenes1";
print PHENO "\n\nNumber of Genes with CRISPR (in environment) Phenotype: $ctGenes2";
print PHENO "\n\nNumber of Genes with only TALEN phenotype in a WT background (TALEN in environment \n  and target gene for TALEN has no other alleles with associated phenotype): $ctGenes3";
print PHENO "\n\nNumber of Genes with only CRISPR phenotype in a WT background (CRISPR in environment \n  and target gene for CRISPR has no other alleles with associated phenotype): $ctGenes4";
print PHENO "\n\nNumber of Genes with TALEN phenotype in a Tg background (TALEN in environment): $ctGenes5";
print PHENO "\n\nNumber of Genes with CRISPR phenotype in a Tg background (CRISPR in environment): $ctGenes6";


close PHENO;

print "\n call FinCount.pl to get monthly fin phenotype count\n";
system ("<!--|ROOT_PATH|-->/server_apps/Reports/PATO/FinCount.pl");


#--------------------------------------------------------------------------
# send Ken counts of various gene name types with & without orthology
system("<!--|ROOT_PATH|-->/server_apps/Reports/Nomenclature/get_uninformative.sh");

exit;
