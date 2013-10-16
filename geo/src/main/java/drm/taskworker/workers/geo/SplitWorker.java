package drm.taskworker.workers.geo;
import drm.taskworker.Worker;
import drm.taskworker.tasks.ParameterFoundException;
import drm.taskworker.tasks.Task;
import drm.taskworker.tasks.TaskResult;
import drm.taskworker.tasks.TaskResult.Result;

public class SplitWorker extends AbstractGeoWorker {

	public SplitWorker(String name) {
		super(name);
	}

	@Override
	public TaskResult work(Task task) {
		TaskResult tr = new TaskResult();
		try {
			
			Region r = getRegion(task);
			
			for (Region rx : r.split()) {
				if (rx.isPoint())
					addPoint(task, tr, rx);
				else if (rx.isVoid())
					addJoin(task, tr, rx);
				else
					addSplit(task, tr, rx);
			}

			tr.setResult(Result.SUCCESS);

		} catch (ParameterFoundException e) {
			tr.setException(e);
			tr.setResult(Result.EXCEPTION);
			tr.fail();
		}
		return tr;
	}

	private void addJoin(Task task, TaskResult tr, Region rx) {
		Task next = new Task(task, "join");
		attachImage(next, null, rx);
		tr.addNextTask(next);
	}

	private void addSplit(Task task, TaskResult tr, Region rx) {
		Task next = new Task(task, "split");
		next.addParam("region", rx);
		tr.addNextTask(next);
	}

	private void addPoint(Task task, TaskResult tr, Region rx) {
		Task next = new Task(task, "fetch");
		next.addParam("position", rx.getUpperleft());
		tr.addNextTask(next);
	}

}
