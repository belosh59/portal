
BASEDIR=$(dirname "$0")

PID_FILE=$BASEDIR/portal.pid

if [ -f $PID_FILE ] ; then
	PID=$(head -n 1 $PID_FILE)
	if [ $(ps -ef | grep -c $PID.*java) -eq 1 ]; then 
		echo "Portal Webserver already running"; 
		exit
	fi
fi

cd ..

[ -d logs ] || mkdir logs
eval java -jar portal-1.0-SNAPSHOT-jar-with-dependencies.jar > logs/portal.out 2>&1 "&"
	PID=$!

echo "$PID" > gatekeeper/portal.pid

echo "Portal Webserver started with PID: $PID. Have fun!"
echo "Execution logs: logs/portal.out"