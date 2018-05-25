
BASEDIR=$(dirname "$0")

PID_FILE=$BASEDIR/portal.pid

if [ -f $PID_FILE ] ; then
	PID=$(head -n 1 $PID_FILE)
    rm $PID_FILE
else
	echo "File containing Portal WebServer doesn't exist. Server is down?" & exit
fi

echo "Interrupting Portal Webserver PID: $PID with SIGINT signal"
KILL -SIGINT $PID

i=0
while [ $i -lt 10 ];
do
	sleep 1;
    i=$((i+1))

    # check if webserver session alive
    [ -d /proc/$PID ] || echo "Exiting." & exit;

    echo "Failure. Trying again"
    KILL -SIGINT $PID
done

echo "Unable to interrupt Webserwer. Killing PID: $PID with KILL -9 command"
kill -9 $PID