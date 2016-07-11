#!/bin/bash

INSTANCE_ID=$1

docker cp /home/user/Software/sqldeveloper/jdbc/lib/ojdbc6.jar $INSTANCE_ID:/root
docker cp /home/user/Documents/broker/ambientar_BUS.sh $INSTANCE_ID:/root
docker cp /home/user/Documents/broker/mit-esb-queues.mqs $INSTANCE_ID:/root

exit 0
