#Setup for AGR ZFIN JBrowse docker container

cd ${SOURCEROOT}/docker/
git clone https://github.com/alliance-genome/agr_jbrowse_zfin.git jbrowse

#Build and run jbrowse container

docker compose build jbrowse
docker compose up jbrowse

#Build and run processgff container
#Unnecessary unless we are building files locally
#Tries to upload to AWS

docker compose build processgff
docker compose run --rm processgff
