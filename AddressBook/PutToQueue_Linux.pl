#!/usr/bin/perl
use strict;
# put the workspace_path here

my $workspace_path = ""; 

open(MYLOGFILE, ">>$workspace_path/AddressBook/putmsglogfile.log");
my $count;
for ($count = 1; $count < 101; $count++) {

my $result = `./amqsput ADDRESSBOOK_IN MB8QMGR < $workspace_path/AddressBook/InputMessage.xml 2>&1`;
print MYLOGFILE ( scalar localtime ) . ': ' . "$result" . "\n" ;

}
