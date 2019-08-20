deploy:
	gradle make
	ant deploy-without-tests deploy-solr restart-solr deploy-jobs

loaddb:
	gradle loaddb

postloaddb:
	gradle buildDatabase
	gradle update
	gradle dropTriggers
	gradle dropFunctions
	gradle deployPostgresFunctions
	gradle deployPostgresTriggers
	gradle buildPostGmakeDatabase

load: loaddb postloaddb


