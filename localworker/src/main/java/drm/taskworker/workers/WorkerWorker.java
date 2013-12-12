package drm.taskworker.workers;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import dnet.minimetrics.TimerContext;
import drm.taskworker.Worker;
import drm.taskworker.config.Config;
import drm.taskworker.monitoring.Metrics;
import drm.taskworker.tasks.ParameterFoundException;
import drm.taskworker.tasks.Task;
import drm.taskworker.tasks.TaskResult;
import drm.taskworker.tasks.TaskResult.Result;

public class WorkerWorker {

	public class Joiner extends Worker {

		public Joiner(String name) {
			super(name);
		}

		@Override
		public TaskResult work(Task task) {
			TaskResult result = new TaskResult();

			try {
				// get the last join id from the queue (String)
				String joinQueue = (String) task.getParam(Task.JOIN_PARAM);
				if (joinQueue.length() % 37 != 0) {
					throw new IllegalArgumentException("Invalid JOIN LIST: "
							+ joinQueue);
				}

				UUID joinId = UUID.fromString(joinQueue.substring(
						joinQueue.length() - 36, joinQueue.length()));
				joinQueue = joinQueue.substring(0, joinQueue.length() - 37);

				// decrement the join counter
				int joinValue = decrementJoin(joinId);

				// register this task as a parent of the future joined task
				saveParent(joinId, task);

				// if the joinValue is zero, we need to "materialize" the join
				// task
				// WARNING: creating this task has to be idempotent because
				// retrieving
				// the join counter has a race, so two possible tasks are
				// joining
				if (joinValue == 0) {
					// GO!
					Task newTask = new Task(task.getJobId(), joinId,
							this.getNextWorker(task.getJobId()));

					// load all parents and build the map of parameters
					Map<String, List<Object>> varMap = new HashMap<>();
					List<Task> parents = children.get(joinId);
					logger.info("Joining the results of " + parents.size()
							+ " parents");

					for (Task parentTask : parents) {
						for (String paramName : parentTask.getParamNames()) {
							if (!varMap.containsKey(paramName)) {
								varMap.put(paramName, new ArrayList<Object>());
							}
							varMap.get(paramName).add(
									parentTask.getParamRef(paramName));
						}
					}

					// put the param maps in the new task
					for (String varName : varMap.keySet()) {
						newTask.addParam(varName, varMap.get(varName));
					}

					// add the new join queue
					newTask.addParam(Task.JOIN_PARAM, joinQueue);

					// return the new task
					result.addNextTask(newTask);

				}

			} catch (ParameterFoundException e) {
				result.setException(e);
				result.setResult(TaskResult.Result.EXCEPTION);
				return result;
			}

			result.setResult(TaskResult.Result.SUCCESS);
			return result;
		}

	}

	protected static final Logger logger = Logger.getLogger(WorkerWorker.class
			.getCanonicalName());

	private Config config = Config.getConfig();

	private Map<String, Worker> workers = new HashMap<>();

	private Worker getWorker(String name) {
		Worker out = workers.get(name);
		if (out != null)
			return out;

		if (config.getWorkers().get(name).getWorkerClass()
				.equals("drm.taskworker.workers.JoinWorker"))
			out = new Joiner(name);
		else
			out = config.getWorkers().get(name).getWorkerInstance();

		workers.put(name, out);
		return out;
	}

	private void trace(String cmd, Task task) {
		logger.info(String.format("[II:%s] %s %s", task.getWorker(), cmd,
				task.toString()));
	}

	public TaskResult work(TaskResult tr) {
		return work(tr.getNextTasks());
	}
	
	public TaskResult work(List<Task> in) {
		Deque<Task> queue = new LinkedList<>();
		
		UUID joinvlue = doSplit(queue,in);

		logger.info("Started inner worker " + this.toString());

		TaskResult result = null;

		while (!queue.isEmpty()) {
			try {
				Task task = queue.pop();

				TimerContext tc = Metrics.timer(
						"worker.work." + task.getWorker()).time();

				trace("FETCHED", task);

				// execute the task
				task.setStartedAt();
				try {
					result = getWorker(task.getWorker()).work(task);
				} catch (Exception e) {
					result = new TaskResult();
					result.setException(e);
					result.setResult(Result.EXCEPTION);
					result.fail();
				}
				task.setFinishedAt();
				task.saveTiming();

				if (result == null) {
					result = new TaskResult();
					result.setResult(Result.ERROR);
					result.fail();
					logger.warning("Worker returns null. Ouch ...");
				}

				if (result.getResult() == TaskResult.Result.SUCCESS) {
					trace("DONE", task);
					List<Task> tasks = result.getNextTasks();
					// is this is a split, do the split
					if (tasks.size() > 1) {
						doSplit(queue,tasks);
					} else if (tasks.size() == 1) {
						if (queue.isEmpty()&&joinstackClear(tasks.get(0),joinvlue)){
							//we are done, back to external reality
							tasks.get(0).flattenParams();
							return result;
						}
						queue.push(tasks.get(0));
					} else {
						// do nothing, dead end
					}

				} else {
					trace("FAILED", task);
					logger.warning(String.format(
							"[%s] internally failed %s: %s", task.getWorker(),
							task.toString(), result.getResult().toString()));
					if (result.getResult() == TaskResult.Result.EXCEPTION) {
						result.getException().printStackTrace();
					}

					if (result.isFatal()) {
						// if this task is fatal, kill the current workflow
						return result;
					}
				}

			} catch (Exception e) {
				logger.log(Level.SEVERE, " worker worker failed", e);
			}

		}

		throw new IllegalStateException(
				"work queue for internal worker is empty, which is bad");
	}

	private boolean joinstackClear(Task task, UUID joinvlue) throws ParameterFoundException {
		String joinQueue = (String)task.getParam(Task.JOIN_PARAM);
		return !joinQueue.contains(joinvlue.toString());
	}

	private UUID doSplit(Deque<Task> queue, List<Task> tasks) {
		// allocate a new uuid that will become the
		// taskid of the joined task
		UUID joinId = UUID.randomUUID();
		storeJoin(joinId, tasks.size());
		for (Task newTask : tasks) {
			newTask.markSplit(joinId);
			queue.push(newTask);
			trace("NEW", newTask);
		}
		return joinId;
	}

	Map<UUID, Integer> joins = new HashMap<>();
	Map<UUID, List<Task>> children = new HashMap<>();

	private void storeJoin(UUID joinId, int size) {
		joins.put(joinId, size);
	}

	private int decrementJoin(UUID joinId) {
		int out = joins.get(joinId) - 1;
		joins.put(joinId, out);
		return out;
	}

	private void saveParent(UUID joinId, Task task) {
		List<Task> children = this.children.get(joinId);
		if (children == null) {
			children = new LinkedList<Task>();
			this.children.put(joinId, children);
		}
		children.add(task);
	}


}
