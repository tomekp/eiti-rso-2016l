#/bin/bash
source setenv.sh
# prepare data
# czy pliki w sklejce będą oddzielone jakimś separatorem?
echo "-----------------------------------" >> temp_separator; 
fileId1=$(( ( RANDOM % 1000 )  + 1 ));
fileId2=$(( ( RANDOM % 1000 )  + 1 ));
fileId3=$(( ( RANDOM % 1000 )  + 1 ));
#generate files ~ 550MB each
head -c550000K /dev/urandom >> temp_file1
head -c550000K /dev/urandom >> temp_file2
head -c550000K /dev/urandom >> temp_file3

#TODO: zaladowac pliki, jak to zrobić? o ile dobrze rozumiem API to powinienem puścić PUTa z parametrem urlToDownload, ale coś nie banglało
#curl -F ....

#get data from app
# aktualne API pozwala pobierać jeden na raz, moja propozycja - specyfikowanie żądanych przez headery
curl --header "X-Requested-Files: $fileId1,$fileId2,$fileId3" $RSO_URL/files >> temp_dlresult;

# assertion
cat $fileId1 temp_separator $fileId2 temp_separator $fileId3 >> temp_local_concat;
diff -q temp_dlresult temp_local_concat >> temp_assertion;

if [ -s temp_assertion ] 
then
	echo "Assertion failed: files differ";
else 
	echo "Test case ran successfully";
fi

# cleanup
rm temp_*;