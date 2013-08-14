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

package drm.taskworker.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;

import drm.taskworker.Job;
import drm.taskworker.Service;
import drm.taskworker.config.Config;
import drm.taskworker.tasks.Task;
// import this here so entities are always loaded

/**
 * Servlet implementation class StartWorkflowServlet
 * 
 * @author Bart Vanbrabant <bart.vanbrabant@cs.kuleuven.be>
 */
@SuppressWarnings("serial")
@WebServlet("/start")
@MultipartConfig
public class StartWorkflowServlet extends HttpServlet {

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public StartWorkflowServlet() {
		super();
	}
	
	private String[] getWorkflows() {
		// load the configuration
		Config cfg = Config.loadConfig(this.getServletContext().getResourceAsStream("/WEB-INF/workers.yaml"));
		String[] workflowNames = cfg.getWorkflows().keySet().toArray(new String[0]);
		
		return workflowNames;
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		request.setAttribute("workflows", this.getWorkflows());
		request.getRequestDispatcher("/start.jsp").forward(request, response);
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		Object data = null;
		String textField = request.getParameter("text");
		
	    Part filePart = request.getPart("file"); // Retrieves <input type="file" name="file">

		if (filePart != null) {
		    InputStream filecontent = filePart.getInputStream();

		    StringWriter writer = new StringWriter();
		    IOUtils.copy(filecontent, writer, "utf-8");
		    data = writer.toString();
		} 
		else if (textField != null && textField.length() > 0) {
			data = request.getParameter("text");
		} 
		else {
			request.setAttribute("error", "Either file or text should be provided.");
		}
		
		if (data != null) {
			// get the delay
			int delay = Integer.valueOf(request.getParameter("date"));
			Date when = new Date(System.currentTimeMillis() + (delay * 1000));
			
			// create a workflow and save it
			Job job = new Job(request.getParameter("workflow"));
			job.setStartAfter(when);
			Task task = job.newStartTask();
			task.addParam("arg0", data);
						
			Service.get().addJob(job);
			
			String id = job.getName();
			request.setAttribute("workflowId", id);
			request.setAttribute("info", "Started workflow with id <a href=\"/workflow?jobId=" + id + "\">" + id + "</a>");
		}

		request.setAttribute("workflows", this.getWorkflows());
		request.getRequestDispatcher("/start.jsp").forward(request, response);
	}
	

}
