# Luciad case

This project creates a jar file that contains all workers and dependencies for the Lucaid fusion
demo. You can start a single instance of the middleware with all workers with the following command:

```bash
java -Dtaskworker.scheduler=true -Dtaskworker.rest=true -Dtaskworker.workers=true\
-Dtaskworker.configfile=config.yaml -cp\
~/taskworker-core/target/taskworker-core.jar:target/luciad-0.4.0-SNAPSHOT-jar-with-dependencies.jar\
drm.taskworker.App
```

This command assumes that you have compiled the taskworker-core project and it is available in your
current home directory. Notice, that in this case we also add the worker jar to the classpath. This
is required to fix a serialisation issue.

Config in the config.yaml file the location where to fetch the tiles and where to upload the
resulting tile. 

The tiles can be download from the openstreet maps servers with the following command:

```bash
for j in {0..9}; do 
    mkdir 3362$j
    cd 3362$j
    for i in {0..9}; do 
        sleep 10
        wget http://b.tile.openstreetmap.org/16/3362$j/2197$i.png; 
    done
    cd ..
done
```

Once the tiles are available to the workers you can submit a job to the taskworker middlware:

```bash
taskworker-client -H localhost newjob -w luciad -D "33620 21970 33624 21974"
```

The arguments are the upper left coordinate and lower right coordinate of the region to merge. The
split workers do require that the number of tiles is always a power of 2.
