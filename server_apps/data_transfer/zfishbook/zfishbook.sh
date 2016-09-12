#!/bin/bash -e

echo "#########################################################################"

cd <!--|TARGETROOT|-->/server_apps/data_transfer/zfishbook/

./preprocess_zfishbook.pl

./zfishbook.pl


