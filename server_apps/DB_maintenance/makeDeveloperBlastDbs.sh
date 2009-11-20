#!/bin/bash

# called from loaddb.pl with the -b input parameter.
# creates /research/zblastfiles/zmore/${DBNAME} directory and copies over the nightly dump 
# of this dir for developer to use independent of the default set of files and the set of 
# files from another user.
# should be used only when actively developing with the blastdbs as cp's of these files
# take up a lot of space.

# script is deploy'ed on ant deploy.
WEBHOST_BLAST_DATABASE_PATH=/research/zblastfiles/zmore/${DBNAME}
BLASTSERVER_BLAST_DATABASE_PATH=/zdevblast/${DBNAME}

FROM_DIRECTORY=${WEBHOST_BLASTDB_TO_COPY}

TO_DIRECTORY=$WEBHOST_BLAST_DATABASE_PATH/Current/

if [ "${HOST}" == "embryonix" ]; then

	if [ -d $WEBHOST_BLAST_DATABASE_PATH ]; then
	    
	    echo $WEBHOST_BLAST_DATABASE_PATH exists
    #check to see if /Current and /Backup exist, if not, create them too.
	    if [ -d $TO_DIRECTORY ]; then
		echo Current exists
	    else
		mkdir $TO_DIRECTORY
	    fi
	    
	    if [ -d $WEBHOST_BLAST_DATABASE_PATH/Backup ]; then
		echo Backup exists
	    else
		mkdir $WEBHOST_BLAST_DATABASE_PATH/Backup
	    fi
	    
    # make the copy, or update the copy.
	    echo rsyning the almdb copy that is updated nightly.
	    rsync -cvur $FROM_DIRECTORY/Current/* $TO_DIRECTORY
	    
    # check if the .svn directory exists, if it doesn't, do a checkout, if it does, do an svn up.
	    if [ -d $WEBHOST_BLAST_DATABASE_PATH/.svn/ ]; then
		
		echo $WEBHOST_BLAST_DATABASE_PATH exists
		echo $WEBHOST_BLAST_DATABASE_PATH/.svn exists
		
		echo removing rsyncd versions of the curated blastdbs to be replaced by svn checkout 
		rm -f $TO_DIRECTORY/Curated*
		rm -f $TO_DIRECTORY/unreleased*
		rm -f $TO_DIRECTORY/published*
		
		svn up --accept 'theirs-full' $WEBHOST_BLAST_DATABASE_PATH/Current/
		echo updated Curated blastdbs to most recent version
		
	    else
		echo removing rsyncd versions of the curated blastdbs to be replaced by svn checkout 
		rm -f $TO_DIRECTORY/Curated*
		rm -f $TO_DIRECTORY/unreleased*
		rm -f $TO_DIRECTORY/published*
		
		echo checking out most recent version of curated blastdbs.
		svn checkout $SVNROOT/Curated_Blast_Databases/trunk $TO_DIRECTORY
	        
	    fi
	    
# no directories exist, start the process from scratch.
	else
	    echo making directories.
	    mkdir $WEBHOST_BLAST_DATABASE_PATH
	    mkdir $WEBHOST_BLAST_DATABASE_PATH/Current
	    mkdir $WEBHOST_BLAST_DATABASE_PATH/Backup
	    
	    echo rsyncing the nightly copy of blastdbs.
	    
    # make the copy, or update the copy.
	    rsync -cvur $FROM_DIRECTORY/Current/* $TO_DIRECTORY
	    rsync -cvur $FROM_DIRECTORY/Current/* $TO_DIRECTORY
	    
	    echo removing rsyncd versions of the curated blastdbs to be replaced by svn checkout 
	    rm -f $WEBHOST_BLAST_DATABASE_PATH/Current/Curated*
	    rm -f $WEBHOST_BLAST_DATABASE_PATH/Current/unreleased*
	    rm -f $WEBHOST_BLAST_DATABASE_PATH/Current/published*
	    
	    echo checking out most recent version of curated blastdbs.
    #export SVNROOT=/research/zcentral/Vault/SVNROOT
	    svn co $SVNROOT/Curated_Blast_Databases/trunk $WEBHOST_BLAST_DATABASE_PATH/Current
	    
	fi 

# establish the write permissions and group.
	chgrp -R fishadmin $TO_DIRECTORY
	chmod -R g+w $TO_DIRECTORY
	
	echo "!!! WARNING !!! : Change env variable WEBHOST_BLAST_DATABASE_PATH " $TO_DIRECTORY

else 
    echo "no makeDeveloperBlastDbs.sh on helix."
fi
