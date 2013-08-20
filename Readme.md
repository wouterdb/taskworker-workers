# DREAMaaS middleware workers

This project contains all the workers that process tasks in a workflow. Each
worker is a seperate maven project.
 
Additionally this project contains integration projects (starting with workflow-)
that contain the configuration and the packaging for a specific case workflow.

For more information about the middleware and how to run the case workflows, 
see the core project.

## 1. Develop a worker

Workers execute a particular type of Task. To implement a Worker, subclass 
drm.taskworker.Worker. When work is available for this worker, the work(Task) 
method is called. The Task argument contains the specifics of the work to be 
done. After all work for a particular Job is done, the work(EndTask) method is 
called. Workers should not retain state between different invocations, 
as multiple instances of each worker may exist, on different machines. All state 
should be stored in the tasks itself.

When the work is done, a taskresult is returned. The worker can add new tasks 
to the taskresult, to order further work to be done. For each task, it must be 
indicated what kind of worker it requires.  To allow modular composition, the 
name given to each task is first looked up in the configuration file. 
If it is not found, the name is used directly. In the example code, 
all work sent out is of the type 'next'. In the configuration file, 'next' 
is translated to the actual work type. 

## 2. Develop a workflow

### 2.1 Project structure

### 2.2 The configuration file

The started workers and available workflows are defined in a YAML file with
three sections: workers, worklows and scheduler. The workers section creates 
instances of worker classes, binding each to a unique name The workflow section 
defines new workflows. Each worklow has the following structure

    [name]:
       start: [name of start task]
       end: [name of end task]

additionally, a section step can be used to map abstract task names to actual task names. 

The third section initializes a scheduler. It requires at least a class argument. 
All other arguments are passed on to the scheduler itself. 


