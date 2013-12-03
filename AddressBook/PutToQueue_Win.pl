use strict;

open(MYLOGFILE, ">>putmsglogfile.log");
my $count;
for ($count = 1; $count < 101; $count++) {

my $result = `amqsput ADDRESSBOOK_IN MB8QMGR < InputMessage.xml 2>&1`;
print MYLOGFILE ( scalar localtime ) . ': ' . "$result" . "\n" ;

}
