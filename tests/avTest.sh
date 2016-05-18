#!/bin/bash
#test odporności systemu na awarie węzłów
source setenv.sh;

#generate single file
fileId=$(( ( RANDOM % 1000 )  + 1 ));
head -c550000K /dev/urandom  >> temp_file

#upload generated file
#curl -F ....

#get nodes addresses
nodes=`compgen -A variable | grep RSO_NODE_`;

for nodeNo in $(seq 1 $RSO_REP_CNT)
do
	paramName="RSO_NODE_$nodeNo";
	nodeAddr=${!paramName};
	ssh $nodeAddr command="ps aux | grep -i rso | awk '{print $2}' | xargs kill -9";
	curl --header "X-Requested-Files: $fileId" $RSO_URL/files >> temp_dlresult;
	diff -q temp_dlresult temp_file >> temp_assertion;

	if [ -s temp_assertion ] 
	then
		echo "Availability test failed after killing $nodeNo nodes";
		exit 5;
	fi
done
	
echo "Availability test completed";


#cleanup
rm temp_*;