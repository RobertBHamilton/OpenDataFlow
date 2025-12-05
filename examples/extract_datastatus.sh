#!/bin/sh 

#This is a full fledged extract. Let us require that the extract is of the table datastatus, and the dataid will be a date in yyyy-mm-dd format. The extract will be for every record which has a modified timestamp corresponding to that date. Output will be a file in the configured output directory with the name status_$dataid.


export output_path=${datastatusextract_schemaname}/${datastatusextract_tablename}/status_$dataid
if [ ! -d $datastatusextract_schemaname ];then mkdir -p $datastatusextract_schemaname;fi

./utility.sh sql "select * from ${datastatus_schemaname}.${datastatus_tablename} cast(modified as date) = parsedatetime('$dataid','yyyy-MM-dd')"	> $output_path 


#No statements after this if you want dataflow to caputure the run status

