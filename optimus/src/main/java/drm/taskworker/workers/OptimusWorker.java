/*
    Copyright 2013 KU Leuven Research and Development - iMinds - Distrinet

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

    Administrative Contact: dnet-project-office@cs.kuleuven.be
    Technical Contact: bart.vanbrabant@cs.kuleuven.be
*/

package drm.taskworker.workers;

import java.util.List;
import java.util.Set;

import drm.taskworker.Worker;
import drm.taskworker.tasks.ParameterFoundException;
import drm.taskworker.tasks.Task;
import drm.taskworker.tasks.TaskResult;
import drm.taskworker.tasks.ValueRef;

/**
 * The optimus worker coordinates the execution of a computational command. The worker always requires
 * a 'command' to be an argument. 
 * 
 * Parameters:
 * 		@param: command The command to execute
 * 		@param: a The start value of the interval
 * 		@param: b The end value of the interval
 * 		@param: d The distance between a and b that indicates we can stop searching
 * 
 * The number of tasks required to search is determined as following
 * 	n = int(abs(a-b)/d)
 * 
 * @author Bart Vanbrabant <bart.vanbrabant@cs.kuleuven.be>
 */
public class OptimusWorker extends Worker {
	/**
	 * Creates a new work with the name blob-to-cache
	 */
	public OptimusWorker(String workerName) {
		super(workerName);
	}
	
	public int parseResult(String result) {
		return Integer.valueOf(result);
	}
	
	/**
	 * Archive the result of the previous task
	 */
	@SuppressWarnings("unchecked")
	public TaskResult work(Task task) {
		logger.info("Executing");
		TaskResult result = new TaskResult();
		
		Set<String> paramNames = task.getParamNames();
		
		String command = null;
		int a = 0;
		int b = 0;
		int d = 0;
		int iteration = 0;
		
		if (paramNames.contains("iteration")) {
			// we get the results from a join
			a = Integer.MAX_VALUE;
			b = 0;
			List<ValueRef> resultList = null;
			
			try {
				resultList = (List<ValueRef>) task.getParam("result");
				// extract the iteration number
				List<ValueRef> iList = (List<ValueRef>) task.getParam("iteration");
				
				if (iList.size() <= 0) {
					return result.setResult(TaskResult.Result.ARGUMENT_ERROR);
				}
				
				iteration = (Integer)iList.get(0).getValue();
				
				// extract the original d parameter
				List<ValueRef> dList = (List<ValueRef>) task.getParam("d");

				if (dList.size() <= 0) {
					return result.setResult(TaskResult.Result.ARGUMENT_ERROR);
				}
				
				d = (Integer)dList.get(0).getValue();
				
				// extract the command
				List<ValueRef> cmdList = (List<ValueRef>) task.getParam("command");

				if (cmdList.size() <= 0) {
					return result.setResult(TaskResult.Result.ARGUMENT_ERROR);
				}
				command = (String)cmdList.get(0).getValue();
			} catch (ParameterFoundException e) {
				return result.setResult(TaskResult.Result.ARGUMENT_ERROR);
			}
			
			for (ValueRef value : resultList) {
				int num;
				try {
					String numValue = new String((byte[])value.getValue());
					logger.info("Got result: " + numValue);
					num = parseResult(numValue);
					if (num < a) {
						a = num;
					}
					if (num > b) {
						b = num;
					}
				} catch (ParameterFoundException | NumberFormatException e) {
				}

			}
		} else {
			// initial command
			try {
				command = (String)task.getParam("command");
				a = Integer.valueOf((String)task.getParam("a"));
				b = Integer.valueOf((String)task.getParam("b"));
				d = Integer.valueOf((String)task.getParam("d"));
			} catch (ParameterFoundException e) {
				return result.setResult(TaskResult.Result.ARGUMENT_ERROR);
			}
		}
		
		try {
			int l = Math.abs(a - b);
			if (l < d) {
				String report = String.format("a=%d, b=%d, iterations=%d", a,b,iteration);
				logger.info(String.format("Result is => a=%d, b=%d, iterations=%d", a,b,iteration));
				// go to the next step
				Task newTask = new Task(task, this.getNextWorker(task.getJobId()));
				newTask.addParam("arg0", report.getBytes());
				result.addNextTask(newTask);
			} else {
				iteration++;
				
				// start child workers
				int n_workers = Math.max((int)Math.floor(l/d), 2);
				
				for (int i = 0; i < n_workers; i++) {
					Task newTask = new Task(task, "execute");
					newTask.addParam("command", command);
					newTask.addParam("a", a);
					newTask.addParam("b", b);
					newTask.addParam("d", d);
					newTask.addParam("iteration", iteration);
					result.addNextTask(newTask);
				}
			}
			result.setResult(TaskResult.Result.SUCCESS);
			
		} catch (Exception e) {
			result.setResult(TaskResult.Result.EXCEPTION);
			result.setException(e);
		}

		return result;
	}
}
