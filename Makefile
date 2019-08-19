deploy:
	gradle make
	ant deploy-without-tests deploy-solr restart-solr deploy-jobs

load:
	gradle loaddb
	gradle postloaddb


