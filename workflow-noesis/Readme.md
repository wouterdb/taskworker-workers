# Luciad case

This project creates a jar file that contains all workers and dependencies for the Noesis simulation
demo. You can start a single instance of the middleware with all workers with the following command:

```bash
java -Dtaskworker.scheduler=true -Dtaskworker.rest=true -Dtaskworker.workers=true \
-Dtaskworker.configfile=config.yaml -jar ~/taskworker-core/target/taskworker-core.jar
```

This command assumes that you have compiled the taskworker-core project and it is available in your
current home directory. 

This demo can be started with the following command:

```bash
taskworker-client -H localhost newjob -w simulate -p a=1 b=150 d=20 \
 command=~/taskworker-workers/workflow-noesis/compute.py
```

This demo searches between the a and the b parameter for new a and b parameters until their distance
is less than the d parameter. The optimus worker receives these parameters and if the process has
not converged it will spawn new (b-a)/d new workers. Each worker generates a random number between a
and b and sleeps for +/- 2 seconds. The join workers collects the generated random numbers and gives
them to the optimus worker. It will do a = min(results) and b = max(results).
