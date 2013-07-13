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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import drm.taskworker.Worker;
import drm.taskworker.tasks.Task;
import drm.taskworker.tasks.TaskResult;

/**
 * Collect all files in a workflow and zip them when an end of workflow task
 * is received.
 * 
 * @author Bart Vanbrabant <bart.vanbrabant@cs.kuleuven.be>
 */
public class ArchiveWorker extends Worker {
	/**
	 * Creates a new work with the name blob-to-cache
	 */
	public ArchiveWorker(String workerName) {
		super(workerName);
	}

	/**
	 * Archive the result of the previous task
	 */
	@SuppressWarnings("unchecked")
	public TaskResult work(Task task) {
		logger.info("Archiving file");
		TaskResult result = new TaskResult();
		if (!task.hasParam("arg0")) {
			return result.setResult(TaskResult.Result.ARGUMENT_ERROR);
		}
		
		try {
			byte[] fileData = (byte[])task.getParam("arg0");
			
			String archiveStore = System.getProperty("dreamaas.archive.url");
			if (archiveStore == null) {
				archiveStore = "http://localhost:8080/download";
			}
			archiveStore += "?id=" + task.getWorkflowId().toString();
			
			URL url = new URL(archiveStore);
			HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			BufferedOutputStream bos = new BufferedOutputStream(httpCon.getOutputStream());
			bos.write(fileData);
			bos.close();
			httpCon.getInputStream();
		} catch (IOException e) {
			result.setResult(TaskResult.Result.EXCEPTION);
			result.setException(e);
		}

		return result.setResult(TaskResult.Result.SUCCESS);
	}
}
