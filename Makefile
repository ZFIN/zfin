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

testAll:
	gradle test --tests UnitTests
	gradle test --tests MeshHeadingSpec --tests MutationDetailsConversionServiceSpec --tests ReportGeneratorSpec --tests SolrServiceSpec --tests SolrQueryFacadeSpec
	gradle test --tests DbUnitTests --tests EnumValidationTest --tests DbControllerTests --tests ThirdPartyServiceTests

testSmoke:
	gradle test --tests AnatomySmokeTest --tests AntibodySmokeTest --tests BlastSmokeTest --tests DownloadSmokeTest --tests FeatureDetailSmokeTest
	gradle test --tests FigureSummarySmokeTest --tests FishSmokeTest --tests PhenotypeSummarySmokeTest --tests ConstructSmokeTest --tests GenotypeDetailSmokeTest
	gradle test --tests LookupSmokeTest --tests MappingDetailSmokeTest
	gradle test --tests MarkerViewSmokeTest --tests MarkerRestSmokeTest

