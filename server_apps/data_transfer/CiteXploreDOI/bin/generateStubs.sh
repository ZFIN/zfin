#!/bin/bash

./wsimport.sh -keep -p uk.ac.ebi.cdb.client "http://www.ebi.ac.uk/webservices/citexplore/v1.0/service?wsdl"
./wsimport.sh -keep "http://www.ebi.ac.uk/webservices/citexplore/v1.0/service?wsdl"
jar -cvf ../lib/wsdl-client.jar uk
rm -rf uk


