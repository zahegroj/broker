-- Populate Database STAFFDB

-- Creating table, dropping an old one if in existence

/*
Sample program for use with IBM WebSphere Message Broker
© Copyright International Business Machines Corporation 2003, 2010 
Licensed Materials - Property of IBM
*/

DROP TABLE STAFF
CREATE TABLE STAFF (STAFFNUM CHAR(10), LASTCHANGE TIMESTAMP, FIRSTNAME CHAR(30), LASTNAME CHAR(30))
