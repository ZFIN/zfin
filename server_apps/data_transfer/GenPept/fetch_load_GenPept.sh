#! /usr/bin/csh

# fetch new Genpept set. creates file named prot_len_acc.unl
# containing a Protein acc# ,length in aa and 
# Nuculotide accession# the aa sequence was derived from

#if ($#argv == 0) then
#     echo "usage: ./fetch_load_genpept.sh <dbname>"
#    exit(0);
#endif

cd  <!--|ROOT_PATH|-->/server_apps/data_transfer/GenPept/

set dbname="<!--|DB_NAME|-->";

echo "fetching GenPept `date`";
./fetch-genpept.r
echo "GenPept fetched `date`" 

# set up the ENV to run dbaccess
# Prompt var is set for the benifit of the ENV
set Prompt="%";

if (`/local/bin/hostname` == "bionix") then
    source /research/zfin/central/Commons/env/wavy
else if (`/local/bin/hostname` == "chromix") then
    source /research/zfin/central/Commons/env/wildtype
else 
	echo "Where the heck am I?"; exit(1);	
endif

setenv DBNAME $dbname

dbaccess $dbname load_prot_len_acc.sql

echo "GenPept loaded `date`"


echo "these are the new potential problems for the curators to resolve"

diff previous_potential_problems.unl potential_problems.unl
cp -pf potential_problems.unl previous_potential_problems.unl

