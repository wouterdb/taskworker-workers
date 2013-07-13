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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * Servlet implementation class DownloadServlet
 */
public class DownloadServlet extends HttpServlet {
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public DownloadServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String id = request.getParameter("id");

		// totally insecure storage!
		File file = new File("/tmp/" + id);
		
		if (file.canRead()) {
		    byte[] result = new byte[(int)file.length()];
		    try {
		      InputStream input = null;
		      ServletOutputStream outStream = null;
		      try {
		        int totalBytesRead = 0;
		        input = new BufferedInputStream(new FileInputStream(file));
		        
		        while(totalBytesRead < result.length){
		          int bytesRemaining = result.length - totalBytesRead;
		          //input.read() returns -1, 0, or more :
		          int bytesRead = input.read(result, totalBytesRead, bytesRemaining); 
		          if (bytesRead > 0){
		            totalBytesRead = totalBytesRead + bytesRead;
		          }
		        }
				response.setContentType("application/zip");
				response.setContentLength(result.length);
				// sets HTTP header
				response.setHeader("Content-Disposition", "attachment; filename=\"result.zip\"");
		        
				outStream = response.getOutputStream();
				outStream.write(result, 0, result.length);
		        
		      } finally {
		        input.close();
				outStream.close();
		      }
		    } catch (IOException e) {
			    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		    }
		} else {
		    response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String id = req.getParameter("id");
		
		File file = new File("/tmp/" + id);
		
		InputStream is = req.getInputStream();  
		OutputStream os = new FileOutputStream(file);
		IOUtils.copy(is, os);
		is.close();  
		os.close(); 
	}
}
