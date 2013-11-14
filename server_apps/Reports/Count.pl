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
my $username = "";
my $password = "";

chdir "<!--|ROOT_PATH|-->/server_apps/Reports/ZGC";

system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> zgcCount.sql > zgcStatistics 2> err");

ZFINPerlModules->sendMailWithAttachedReport("<!--|COUNT_ZGC_OUT|-->", "Monthly ZGC statistics from $dbname", "./zgcStatistics");
ZFINPerlModules->sendMailWithAttachedReport("<!--|COUNT_ZGC_ERR|-->", "Monthly ZGC statistics Err from $dbname", "./err");

#-----------------------------------------------------------------------

chdir "<!--|ROOT_PATH|-->/server_apps/Reports/Vega";

# Run vega_thisse_report.sql before VegaCount.sql.
# A file is created by vega_thisse_report.sql that is read by VegaCount.sql.

system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> vega_thisse_report.sql 2> err");

ZFINPerlModules->sendMailWithAttachedReport("<!--|COUNT_THISSE_VEGA_OUT|-->", "Monthly Vega-Thisse statistics from $dbname", "./vega_thisse_report.unl");
ZFINPerlModules->sendMailWithAttachedReport("<!--|COUNT_THISSE_VEGA_ERR|-->", "Vega-Thisse statistics Err from $dbname", "./err");


system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> VegaCount.sql > VegaStatistics 2> err");

ZFINPerlModules->sendMailWithAttachedReport("<!--|COUNT_VEGA_OUT|-->", "Monthly Vega statistics from $dbname", "./VegaStatistics");
ZFINPerlModules->sendMailWithAttachedReport("<!--|COUNT_VEGA_ERR|-->", "Monthly Vega statistics Err from $dbname", "./err");

#--------------------------------------------------------------------------
# send Ken counts of various gene name types with & without orthology
system("<!--|ROOT_PATH|-->/server_apps/Reports/Nomenclature/get_uninformative.sh");


#--------------------------------------------------------------------------
chdir "<!--|ROOT_PATH|-->/server_apps/Reports/PATO";

system("$ENV{'INFORMIXDIR'}/bin/dbaccess -a <!--|DB_NAME|--> PhenoytpeCount.sql > PhenotypeStatistics 2> err");

print "\n Call FinCount.pl to get monthly fin phenotype count\n";
system ("<!--|ROOT_PATH|-->/server_apps/Reports/PATO/FinCount.pl");

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
          from marker_relationship, experiment_condition, genotype_experiment gx1, phenotype_experiment px1, phenotype_statement ps1, genotype 
         where mrel_type = "knockdown reagent targets gene" 
           and mrel_mrkr_1_zdb_id like "ZDB-TALEN%" 
           and mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id 
           and expcond_exp_zdb_id = gx1.genox_exp_zdb_id 
           and gx1.genox_zdb_id = px1.phenox_genox_zdb_id 
           and gx1.genox_geno_zdb_id = geno_zdb_id 
           and geno_is_wildtype = "t" 
           and ps1.phenos_phenox_pk_id = px1.phenox_pk_id
           and not exists(select "x" from phenotype_statement ps2, phenotype_experiment px2, genotype_experiment gx2, genotype_feature, feature_marker_relationship
                           where ps2.phenos_entity_1_superterm_zdb_id||
                                 ps2.phenos_entity_1_subterm_zdb_id||
                                 ps2.phenos_entity_2_superterm_zdb_id||
                                 ps2.phenos_entity_2_subterm_zdb_id||
                                 ps2.phenos_tag =
                                 ps1.phenos_entity_1_superterm_zdb_id||
				 ps1.phenos_entity_1_subterm_zdb_id||
				 ps1.phenos_entity_2_superterm_zdb_id||
				 ps1.phenos_entity_2_subterm_zdb_id||
                                 ps1.phenos_tag
                             and ps2.phenos_phenox_pk_id = px2.phenox_pk_id
                             and gx2.genox_zdb_id = px2.phenox_genox_zdb_id
                             and gx2.genox_geno_zdb_id = genofeat_geno_zdb_id
                             and genofeat_feature_zdb_id = fmrel_ftr_zdb_id
                             and fmrel_mrkr_zdb_id = mrel_mrkr_2_zdb_id);';
                                                 

my $ctGenes3 = ZFINPerlModules->countData($sql);

$sql = 'select distinct mrel_mrkr_2_zdb_id   
          from marker_relationship, experiment_condition, genotype_experiment gx1, phenotype_experiment px1, phenotype_statement ps1, genotype 
         where mrel_type = "knockdown reagent targets gene" 
           and mrel_mrkr_1_zdb_id like "ZDB-CRISPR%" 
           and mrel_mrkr_1_zdb_id = expcond_mrkr_zdb_id 
           and expcond_exp_zdb_id = gx1.genox_exp_zdb_id 
           and gx1.genox_zdb_id = px1.phenox_genox_zdb_id 
           and gx1.genox_geno_zdb_id = geno_zdb_id 
           and geno_is_wildtype = "t" 
           and ps1.phenos_phenox_pk_id = px1.phenox_pk_id
           and not exists(select "x" from phenotype_statement ps2, phenotype_experiment px2, genotype_experiment gx2, genotype_feature, feature_marker_relationship
                           where ps2.phenos_entity_1_superterm_zdb_id||
                                 ps2.phenos_entity_1_subterm_zdb_id||
                                 ps2.phenos_entity_2_superterm_zdb_id||
                                 ps2.phenos_entity_2_subterm_zdb_id||
                                 ps2.phenos_tag =
                                 ps1.phenos_entity_1_superterm_zdb_id||
				 ps1.phenos_entity_1_subterm_zdb_id||
				 ps1.phenos_entity_2_superterm_zdb_id||
				 ps1.phenos_entity_2_subterm_zdb_id||
                                 ps1.phenos_tag
                             and ps2.phenos_phenox_pk_id = px2.phenox_pk_id
                             and gx2.genox_zdb_id = px2.phenox_genox_zdb_id
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

open (PHENO, ">>PhenotypeStatistics") || die "Cannot open PhenotypeStatistics : $!\n";

print PHENO "\n\nNumber of Genes with TALEN (in environment) Phenotype: $ctGenes1";
print PHENO "\n\nNumber of Genes with CRISPR (in environment) Phenotype: $ctGenes2";
print PHENO "\n\nNumber of Genes with only TALEN phenotype in a WT background (TALEN in environment \n  and target gene for TALEN has no other alleles with associated phenotype): $ctGenes3";
print PHENO "\n\nNumber of Genes with only CRISPR phenotype in a WT background (CRISPR in environment \n  and target gene for CRISPR has no other alleles with associated phenotype): $ctGenes4";
print PHENO "\n\nNumber of Genes with TALEN phenotype in a Tg background (TALEN in environment): $ctGenes5";
print PHENO "\n\nNumber of Genes with CRISPR phenotype in a Tg background (CRISPR in environment): $ctGenes6";


close PHENO;

ZFINPerlModules->sendMailWithAttachedReport("<!--|COUNT_PATO_OUT|-->", "Monthly Phenotype statistics from $dbname", "./PhenotypeStatistics");
ZFINPerlModules->sendMailWithAttachedReport("<!--|COUNT_VEGA_ERR|-->", "Monthly Phenotype statistics Err from $dbname", "./err");

exit;
