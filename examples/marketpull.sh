
# dump the environment variables except for sensitive passwords
set|grep market |grep -v password

mkdir -p $marketfile_schemaname


echo curl "https://$marketwatch_hostname/$marketwatch_schemaname?access_key=${marketwatch_password}&symbols=$marketwatch_tablename" 
curl "https://$marketwatch_hostname/$marketwatch_schemaname?access_key=${marketwatch_password}&symbols=$marketwatch_tablename" >  examples/$marketfile_schemaname/${marketfile_tablename}_${dataid}.dat
# todo: scan the response for error message
