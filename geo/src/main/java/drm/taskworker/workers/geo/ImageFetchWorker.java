package drm.taskworker.workers.geo;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import com.github.sardine.DavResource;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;


import drm.taskworker.Worker;
import drm.taskworker.tasks.Task;
import drm.taskworker.tasks.TaskResult;
import drm.taskworker.tasks.TaskResult.Result;


public class ImageFetchWorker extends AbstractGeoWorker {

	public ImageFetchWorker(String name) {
		super(name);
	}

	@Override
	public TaskResult work(Task task) {
		TaskResult tr = new TaskResult();
		
		try {
			
			RasterPoint point = (RasterPoint) task.getParam("position");
			//BufferedImage bimg = ImageIO.read(new File("/home/wouterdb/freak/maps/testtiles/"+point.getX()+"/"+point.getY()+".png"));
			//BufferedImage bimg = ImageIO.read(new File("/home/wouterdb/freak/maps/b.tile.openstreetmap.org/16/"+point.getX()+"/"+point.getY()+".png"));
			//BufferedImage bimg = ImageIO.read(new URL(task.getJobOption("fetch.url")+"/"+point.getX()+"/"+point.getY()+".png"));
			
			final String baseUrl = task.getJobOption("fetch.url");
			if (baseUrl == null) {
				logger.log(Level.SEVERE, "No base url configured to upload result to.");
				return tr.setResult(TaskResult.Result.ARGUMENT_ERROR);
			}
			             
			
			Sardine sardine = SardineFactory.begin(task.getJobOption("fetch.username"), task.getJobOption("fetch.password"));
			@SuppressWarnings("unused")
			List<DavResource> resources = sardine.list(baseUrl);
		
			BufferedImage bimg = ImageIO.read(sardine.get(task.getJobOption("fetch.url")+"/"+point.getX()+"/"+point.getY()+".png"));

			
			Task next = new Task(task, "join");
			attachImage(next, bimg, new Region(point));
			tr.addNextTask(next);
			tr.setResult(Result.SUCCESS);
			
		} catch (Exception e) {
			tr.setException(e);
			tr.setResult(Result.EXCEPTION);
			tr.fail();
		}
		return tr;
		
	}

}
