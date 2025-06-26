#!/bin/sh

TARGET_PATH=$TARGETROOT/server_apps/data_transfer/BLAST

cd $TARGET_PATH/VegaProtein/

$TARGET_PATH/VegaProtein/downloadVegaProtein.sh

$TARGET_PATH/VegaProtein/convertVegaProtein.sh 
$TARGET_PATH/VegaProtein/pushVegaProtein.sh

exit
