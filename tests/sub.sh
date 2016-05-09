#!/bin/bash
source setenv.sh

givenId=$1;

#DL
curl --header "X-Requested-Files: $givenId" $RSO_URL/files >> temp_dled_$givenId;

#assert
diff -q temp_dled_$givenId temp_file_$givenId >> temp_assertion_$givenId;

if [ -s temp_assertion temp_assertion_$givenId] 
then
	temp_failure_$givenId;
fi

