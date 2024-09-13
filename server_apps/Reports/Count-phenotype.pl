#! /opt/zfin/bin/perl -w
#
# This monthly-run script queries the statistics on
# ZFIN nomenclature and expression pattern related
# to the ZGC project. The result is mailed to curators.
#
use strict;
use FindBin;
use lib "$FindBin::Bin/../perl_lib/";
use ZFINPerlModules qw(assertEnvironment);
assertEnvironment('ROOT_PATH', 'PGHOST', 'DB_NAME');

# set environment variables
my $dbname = $ENV{'DB_NAME'};
my $rootpath = $ENV{'ROOT_PATH'};

print "\nStart running counting SQLs ...\n";

#--------------------------------------------------------------------------
chdir "$rootpath/server_apps/Reports/PATO";

system("/bin/rm -f PhenotypeStatistics.txt");

system("psql -v ON_ERROR_STOP=1 -d $dbname -f count_phenotype.sql > PhenotypeStatistics.txt 2> err.txt");

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
           and not exists (select 'x'
                    from phenotype_experiment px2
                    join fish_experiment fx2 on fx2.genox_zdb_id = px2.phenox_genox_zdb_id
                    join fish f2 on fx2.genox_fish_zdb_id = f2.fish_zdb_id
                    join genotype_feature on f2.fish_genotype_zdb_id = genofeat_geno_zdb_id
                    join feature_marker_relationship on genofeat_feature_zdb_id = fmrel_ftr_zdb_id
                 where fmrel_mrkr_zdb_id = mrel_mrkr_2_zdb_id);";


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
           and not exists (select 'x'
                    from phenotype_experiment px2
                    join fish_experiment fx2 on fx2.genox_zdb_id = px2.phenox_genox_zdb_id
                    join fish f2 on fx2.genox_fish_zdb_id = f2.fish_zdb_id
                    join genotype_feature on f2.fish_genotype_zdb_id = genofeat_geno_zdb_id
                    join feature_marker_relationship on genofeat_feature_zdb_id = fmrel_ftr_zdb_id
                 where fmrel_mrkr_zdb_id = mrel_mrkr_2_zdb_id);";

my $ct24 = ZFINPerlModules->countData($sql);

$sql =  "select distinct mrel_mrkr_2_zdb_id
   from marker_relationship
	 join fish_str on mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
	 join fish on fishstr_fish_zdb_id = fish_zdb_id
	 join fish_experiment on genox_fish_zdb_id = fish_zdb_id
	 join genotype_feature on fish_genotype_zdb_id = genofeat_geno_zdb_id
	 join feature on genofeat_feature_zdb_id = feature_zdb_id
	 join genotype on ( geno_zdb_id = fish_genotype_zdb_id and geno_zdb_id = genofeat_geno_zdb_id )
	 join phenotype_experiment on phenox_genox_zdb_id = genox_zdb_id
  where mrel_type = 'knockdown reagent targets gene'
    and mrel_mrkr_1_zdb_id like 'ZDB-TALEN%'
    and feature_type = 'TRANSGENIC_INSERTION'
    and geno_is_wildtype = 'f'";
            
my $ct25 = ZFINPerlModules->countData($sql);

$sql =  "select distinct mrel_mrkr_2_zdb_id
   from marker_relationship
	 join fish_str on mrel_mrkr_1_zdb_id = fishstr_str_zdb_id
	 join fish on fishstr_fish_zdb_id = fish_zdb_id
	 join fish_experiment on genox_fish_zdb_id = fish_zdb_id
	 join genotype_feature on fish_genotype_zdb_id = genofeat_geno_zdb_id
	 join feature on genofeat_feature_zdb_id = feature_zdb_id
	 join genotype on ( geno_zdb_id = fish_genotype_zdb_id and geno_zdb_id = genofeat_geno_zdb_id )
	 join phenotype_experiment on phenox_genox_zdb_id = genox_zdb_id
  where mrel_type = 'knockdown reagent targets gene'
    and mrel_mrkr_1_zdb_id like 'ZDB-CRISPR%'
    and feature_type = 'TRANSGENIC_INSERTION'
    and geno_is_wildtype = 'f'";
            
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
system ("$rootpath/server_apps/Reports/PATO/FinCount.pl");


#--------------------------------------------------------------------------
# send Ken counts of various gene name types with & without orthology
system("$rootpath/server_apps/Reports/Nomenclature/get_uninformative.sh");

exit;
