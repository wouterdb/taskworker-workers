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

import static drm.taskworker.config.Config.cfg;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;

import drm.taskworker.Worker;
import drm.taskworker.tasks.ParameterFoundException;
import drm.taskworker.tasks.Task;
import drm.taskworker.tasks.TaskResult;

/**
 * Collect all files in a workflow and zip them when an end of workflow task
 * is received.
 * 
 * @author Bart Vanbrabant <bart.vanbrabant@cs.kuleuven.be>
 */
public class WebdavArchiveWorker extends Worker {
	/**
	 * Creates a new work with the name blob-to-cache
	 */
	public WebdavArchiveWorker(String workerName) {
		super(workerName);
	}

	/**
	 * Archive the result of the previous task
	 */
	public TaskResult work(Task task) {
		logger.info("Archiving file");
		TaskResult result = new TaskResult();
		
		byte[] fileData = null;
		
		try {
			fileData = (byte[])task.getParam("arg0");
		} catch (ParameterFoundException e) {
			return result.setResult(TaskResult.Result.ARGUMENT_ERROR);
		}
		
		try {
			// WebDAV URL:
			final String baseUrl = cfg().getProperty("taskworker.archive.url");
			if (baseUrl == null) {
				logger.log(Level.SEVERE, "No base url configured to upload result to.");
				return result.setResult(TaskResult.Result.ARGUMENT_ERROR);
			}
			
			Sardine sardine = SardineFactory.begin(cfg().getProperty("taskworker.archive.username", "admin"), 
					cfg().getProperty("taskworker.archive.password", "admin"));
			List<DavResource> resources = sardine.list(baseUrl);
			
			sardine.put(baseUrl + task.getJobId().toString(), fileData, "application/zip");

			Task newTask = new Task(task, this.getNextWorker(task.getJobId()));
			result.addNextTask(newTask);
			
		} catch (IOException e) {
			result.setResult(TaskResult.Result.EXCEPTION);
			result.setException(e);
			return result;
		}

		return result.setResult(TaskResult.Result.SUCCESS);
	}
}
