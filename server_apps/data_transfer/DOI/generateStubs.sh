#!/bin/bash

echo "Generating client" ; 
./wsimport.sh -keep -p uk.ac.ebi.cdb.client "http://www.ebi.ac.uk/webservices/citexplore/v1.0/service?wsdl"
echo "Generating server" ; 
./wsimport.sh -keep "http://www.ebi.ac.uk/webservices/citexplore/v1.0/service?wsdl"
rm -f ../../../../lib/Java/jaxws/wsdl-client.jar
jar -cf ../../../../lib/Java/jaxws/wsdl-client.jar uk
rm -rf uk


