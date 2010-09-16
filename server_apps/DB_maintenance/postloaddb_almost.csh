#!/bin/tcsh -e
#----------------------------------------------------------------------------
#
# This script exists because I couldn't figure out a way for the
# load_and_index_almost.pl script to run multiple commands in any shell
# besides sh.  We need to run the postloaddb target in csh because the
# .env file it relies on assumes csh or tcsh.
#
# Usage:
# 
#   postloaddb_almost.csh zfinWwwDir makeEnvFile
#
# Params:
#
#    zfinWwwDir  - the ZFIN_WWW dir for almost.
#    makeEnvFile - the .env file for almost.
setenv Prompt
set prompt  # a hack so the source works.
cd $1
source $2
/local/bin/gmake postloaddb
