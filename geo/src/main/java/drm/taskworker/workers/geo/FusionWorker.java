package drm.taskworker.workers.geo;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import drm.taskworker.Worker;
import drm.taskworker.tasks.ParameterFoundException;
import drm.taskworker.tasks.Task;
import drm.taskworker.tasks.TaskResult;
import drm.taskworker.tasks.TaskResult.Result;
import drm.taskworker.tasks.ValueRef;

public class FusionWorker extends AbstractGeoWorker {

	public FusionWorker(String name) {
		super(name);
	}

	@Override
	public TaskResult work(Task task) {
		TaskResult tr = new TaskResult();

		try {
			//eager loading, more elegant code
			task.loadParamRefs();
			task.flattenParams();
			
			List<Region> reg = getRegions(task);
			List<BufferedImage> imgs = getImages(task);

			SortedMap<Region, BufferedImage> data = coorder(reg, imgs);

			Region[] regs = data.keySet().toArray(new Region[4]);
			ensureFit(regs);

			BufferedImage[] parts = data.values().toArray(new BufferedImage[4]);

			int xsize = regs[0].getHeight();
			int ysize = regs[0].getWidth();
			// FIXME: non power of 2 grids
			int xdim = (parts[1] == null) ? 128 : 256;
			int ydim = (parts[2] == null) ? 128 : 256;

			BufferedImage whole = new BufferedImage(xdim, ydim,
					BufferedImage.TYPE_INT_RGB);

			Graphics2D graph = whole.createGraphics();

			// Scale the image to the new buffer using the specified rendering
			// hint.
			graph.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);

			boolean doom1 = graph.drawImage(parts[0], 0, 0, 128, 128, null);
			boolean doom2 = graph.drawImage(parts[1], 128, 0, 128, 128, null);
			boolean doom3 = graph.drawImage(parts[2], 0, 128, 128, 128, null);
			boolean doom4 = graph.drawImage(parts[3], 128, 128, 128, 128, null);

			if (!doom1 || !doom2 || !doom3 || !doom4)
				System.out.println("ddddddooooooooooooooooOOMMMMMMMMMMMMMMM");

			// Just to be clean, explicitly dispose our temporary graphics
			// object
			graph.dispose();

			String joinQueue = (String) task.getParam(Task.JOIN_PARAM);
			Task next;
			if (joinQueue.length() == 0) {
				next = new Task(task, "archive");
				attachBinaryImage(next, "arg0", whole);
			} else {
				next = new Task(task, "join");
				attachImage(next, whole, new Region(regs));
			}

			tr.addNextTask(next);
			tr.setResult(Result.SUCCESS);

		} catch (ParameterFoundException | IOException e) {
			tr.setException(e);
			tr.fail();
		}
		return tr;
	}

	private List<Region> getRegions(Task task) throws ParameterFoundException {

		/*
		 * List<ValueRef> refs = (List<ValueRef>) task.getParam("region");
		 * List<Region> regs = new LinkedList<>(); for (ValueRef valueRef :
		 * refs) { regs.add((Region) valueRef.getValue()); } return regs;
		 */
		List<Region> refs = (List<Region>) task.getParam("region");
		return refs;
	}

	private List<BufferedImage> getImages(Task task)
			throws ParameterFoundException {
		/*
		 * List<ValueRef> refs = (List<ValueRef>) task.getParam("img");
		 * List<BufferedImage> regs = new LinkedList<>(); for (ValueRef valueRef
		 * : refs) { regs.add(((SerialisableImageContainer)
		 * valueRef.getValue()).getImage()); } return regs;
		 */

		List<SerialisableImageContainer> refs = (List<SerialisableImageContainer>) task
				.getParam("img");
		List<BufferedImage> regs = new LinkedList<>();
		for (SerialisableImageContainer valueRef : refs) {
			regs.add(valueRef.getImage());
		}
		return regs;
	}

	private void ensureFit(Region[] parts) {
		// fixme, make better fit check
		if (parts[2] == null)
			return;

		int x0 = parts[0].getUpperleft().getX();
		int x1 = parts[0].getLowerRight().getX();
		int x2 = parts[1].getLowerRight().getX();

		int y0 = parts[0].getUpperleft().getY();
		int y1 = parts[0].getLowerRight().getY();
		int y2 = parts[2].getLowerRight().getY();

		if (!parts[1].equals(new Region(x1, y0, x2, y1))
				|| !parts[2].equals(new Region(x0, y1, x1, y2))
				|| !parts[3].equals(new Region(x1, y1, x2, y2)))
			throw new IllegalStateException("regions do not connect " + parts);

	}

	private <T> SortedMap<Region, T> coorder(List<Region> reg, List<T> imgs) {
		SortedMap<Region, T> sorter = new TreeMap<>(new RegionSort());
		for (int i = 0; i < reg.size(); i++) {
			sorter.put(reg.get(i), imgs.get(i));
		}
		return sorter;
	}

}
