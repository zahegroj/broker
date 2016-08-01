#!/bin/bash
QUEUE_MANAGER="QMDEV"
INTEG_NODE="QMDEV_BRK"

mkdir /root/jars
mkdir /brokerlogs
mv /root/ojdbc6.jar /root/jars

/opt/mqm/bin/./crtmqm $QUEUE_MANAGER
/opt/mqm/bin/./strmqm $QUEUE_MANAGER
/opt/mqm/bin/./runmqsc $QUEUE_MANAGER < /root/mit-esb-queues.mqs

/opt/iib-10.0.0.3/./iib mqsicreatebroker $INTEG_NODE
/opt/iib-10.0.0.3/./iib mqsichangebroker $INTEG_NODE -q $QUEUE_MANAGER
/opt/iib-10.0.0.3/./iib mqsichangeproperties $INTEG_NODE -b webadmin -o HTTPConnector -n port -v 4414
/opt/iib-10.0.0.3/./iib mqsistart $INTEG_NODE
/opt/iib-10.0.0.3/./iib mqsicreateexecutiongroup $INTEG_NODE -e default -w 90

/opt/iib-10.0.0.3/./iib mqsicreateconfigurableservice $INTEG_NODE -c JDBCProviders -o DS_CIERREN_ETL -n connectionUrlFormat,connectionUrlFormatAttr1,description,jarsURL,portNumber,serverName,type4DatasourceClassName,type4DriverClassName -v "jdbc:oracle:thin:[user]/[password]@[serverName]:[portNumber]/[connectionUrlFormatAttr1],PROFBAC,MITStaging,/root/jars,1630,oranci.profuturo.mx,oracle.jdbc.xa.client.OracleXADataSource,oracle.jdbc.OracleDriver"

/opt/iib-10.0.0.3/./iib mqsisetdbparms $INTEG_NODE -n jdbc::cierrenEtlAppSecurIdent -u CIERREN_ETL_APP -p Mlo978y6#

/opt/iib-10.0.0.3/./iib mqsichangeproperties $INTEG_NODE -c JDBCProviders -o DS_CIERREN_ETL -n securityIdentity -v cierrenEtlAppSecurIdent

/opt/iib-10.0.0.3/./iib mqsistop $INTEG_NODE
/opt/iib-10.0.0.3/./iib mqsistart $INTEG_NODE

echo "172.16.60.65	wasnci.profuturo.mx" >> /etc/hosts
echo "172.16.60.74	devdatastage" >> /etc/hosts
echo "172.16.60.72	bpmpc.profuturo.mx" >> /etc/hosts
echo "172.16.48.248   oranci.profuturo.mx" >> /etc/hosts

exit 0
