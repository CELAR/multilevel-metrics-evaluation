#!/bin/bash

MELA_DATA_DIR=/opt/mela-data-service

mkdir -p $MELA_DATA_DIR
cp ./*jar $MELA_DATA_DIR
cp -R ./dataServiceConfig $MELA_DATA_DIR

cp ./mela-data-service /etc/init.d/
chmod +x /etc/init.d/mela-data-service
update-rc.d -f mela-data-service defaults
