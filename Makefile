load:
	gradle loaddb
	gradle buildDatabase
	gradle make
	gradle buildPostGmakeDatabase
	gradle deployPostgresFunctions
	gradle dropTriggers
	gradle dropFunctions
	gradle deployPostgresFunctions
	gradle deployPostgresTriggers

rebuild:
	gradle make
	ant deploy-without-tests

