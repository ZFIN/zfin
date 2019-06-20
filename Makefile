load:
	gradle loaddb
	gradle postloaddb

deploy:
	gradle make
	ant deploy-without-tests deploy-solr restart-solr deploy-jobs 

