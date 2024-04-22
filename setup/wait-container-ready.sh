#!/bin/bash
#Sample approach to wait until a container is ready
#by looking for a string in container log
container=$1
countertarget=$2
target="Startup complete"

attempt=0
while [ $attempt -le 60 ]; do
    attempt=$(( $attempt + 1 ))
    echo "Waiting for container ready (attempt: $attempt)..."
    result=$(docker logs $container)
    count=$(grep -o "$target" <<< "$result" | wc -l)
    if [ $count -eq $countertarget ]; then
        echo "Container is ready!"
        exit 0
    fi
    
    sleep 1
done
echo "ERROR: Container is not ready after maximum number of attempts"
exit 1