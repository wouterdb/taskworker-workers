package drm.taskworker.workers.geo;

import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import drm.taskworker.Worker;
import drm.taskworker.tasks.ParameterFoundException;
import drm.taskworker.tasks.Task;
import drm.taskworker.tasks.TaskResult;
import drm.taskworker.tasks.TaskResult.Result;

public class ArchiveWorker extends Worker {

	public ArchiveWorker(String name) {
		super(name);
	}

	@Override
	public TaskResult work(Task task) {
		TaskResult tr = new TaskResult();
		try {
			SerialisableImageContainer img = (SerialisableImageContainer)task.getParam("img");

			Region r = (Region) task.getParam("region");

			File f = File.createTempFile("images", ".png");

			ImageIO.write(img.getImage(), "png", f);

		} catch (Exception e) {
			tr.setException(e);
		}
		
		tr.setResult(Result.FINISHED);
		return tr;
	}

}
