#! /usr/bin/csh

# fetch new Genpept set. creates file named prot_len_acc.unl
# containing a Protein acc# ,length in aa and 
# Nuculotide accession# the aa sequence was derived from

#if ($#argv == 0) then
#     echo "usage: ./fetch_load_genpept.sh <dbname>"
#    exit(0);
#endif

set dbname="<!--|DB_NAME|-->";

echo "fetching GenPept `date`";
/private/bin/rebol -sqw fetch-genpept.r
echo "GenPept fetched `date`" 

# set up the ENV to run dbaccess
# Prompt var is set for the benifit of the ENV
set Prompt="%";

if ($HOST == "bionix.cs.uoregon.edu") then
    source /research/zfin/central/Commons/env/wavy
else if ($HOST == "chromix.cs.uoregon.edu") then
    source /research/zfin/central/Commons/env/wildtype
else 
	echo "Where the heck am I?"; exit(1);	
endif

setenv DBNAME $dbname

dbaccess $dbname load_prot_len_acc.sql
