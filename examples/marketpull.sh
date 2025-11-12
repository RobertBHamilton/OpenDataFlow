
# dump the environment variables except for sensity passwords
set|grep market|grep -v pass

if [ ! -d $marketfile_schemaname ];then mkdir -f $marketdata_schemaname;fi

#echo curl "https://$marketwatch_hostname/$marketwatch_schemaname?access_key=secredpassword&symbols=$marketwatch_tablename > " $marketfile_schemaname/${marketfile_tablename}_${dataid}.dat
curl "https://$marketwatch_hostname/$marketwatch_schemaname?access_key=${marketwatch_password}&symbols=$marketwatch_tablename" >  examples/$marketfile_schemaname/${marketfile_tablename}_${dataid}.dat
