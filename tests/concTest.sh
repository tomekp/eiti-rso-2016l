#!/bin/bash
source setenv.sh
# scenariusz: 5 równoległych requestów po plik (inny), dla uproszczenia jeden, sprawdzenie czy każdy otrzymał to czego żądał


#prepare
idsToGenerate=();

for ct in {1..10}
do
	genId=$(( ( RANDOM % 1000 )  + 1 ));
	idsToGenerate+=($genId);
done;

printf "Generated IDs: ";
printf '%s\t' "${idsToGenerate[@]}";
printf "\n ";

#generate files
for generatedId in ${idsToGenerate[@]}
do
	cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 550000000 | head -n 1 >> temp_file_$generatedId;
	# cat -F .... # upload generated file
done

#download from subprocesses
for generatedId in ${idsToGenerate[@]}
do
	./sub.sh $generatedId;
done

wait

count=`ls -l temp_result* | wc -l`

if [ $count>0 ]
then 
	echo "Test failed";
else
	echo "Test complitted successfully";	
fi


rm temp_*;