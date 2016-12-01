
#adb -s 0567cbc33443c06b reboot
#sleep 30s
#adb -s 0567cbc33443c06b root
#sleep 2m
i=1

for j in `seq 1 1`;
do
	adb -s 0567cbc33443c06b shell sh /data/mtremoveBenchmarkData.sh
	adb -s 0567cbc33443c06b shell sh /data/mtpreBenchmark.sh #create database 
	adb -s 0567cbc33443c06b shell sh /data/mtbenchmark.sh #run queries
	adb -s 0567cbc33443c06b pull /data/trace.log
	mv trace.log test.log
done
