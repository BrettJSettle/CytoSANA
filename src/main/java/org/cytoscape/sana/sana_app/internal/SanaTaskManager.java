package org.cytoscape.sana.sana_app.internal;

import org.cytoscape.sana.sana_app.internal.util.SanaUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import java.util.ArrayList;
import java.util.List;

public class SanaTaskManager extends Unloadable {
	private MultiTaskRunner mt;

	public SanaTaskManager() {
		super();
	}

	@Override
	public void unload() {
	}

	private void runJob(Runnable r) {
		Thread th = new Thread(r);
		th.start();
	}

	private static class MultiTaskRunner extends AbstractTask implements IUnloadable {
		Task currentTask; // possibly a SanaTask
		List<Runnable> cleanupFuncs;
		TaskIterator iter;

		public MultiTaskRunner(TaskIterator it) {
			unloadLater(this);
			iter = it;
		}

		@Override
		public void run(TaskMonitor mon) {
			try {
				runTasks(mon);
			} catch (Exception ex) {
				mon.showMessage(TaskMonitor.Level.ERROR, ex.toString());
				SanaUtil.msgboxAsync(SanaUtil.getStackTrace(ex));
			}
			unloadNow(this);

			iter = null;
			currentTask = null;

			if (cleanupFuncs != null) {
				for (Runnable r : cleanupFuncs)
					r.run();
				cleanupFuncs = null;
			}
		}

		public void addCleanupFunc(Runnable r) {
			if (cleanupFuncs == null)
				cleanupFuncs = new ArrayList<Runnable>();
			cleanupFuncs.add(r);
		}

		private void runTasks(TaskMonitor mon) throws Exception {

			while (true) {
				synchronized (this) {
					if (iter == null || !iter.hasNext())
						return;
					currentTask = iter.next();

				}
				currentTask.run(mon); // this is probably lengthy; don't keep
										// any lock held during call
			}
		}

		@Override
		public synchronized void cancel() {
			iter = null;
			cleanupFuncs = null;
			unloadNow(this);
			if (currentTask != null) {
				currentTask.cancel();
				currentTask = null;
			}
		}

		@Override
		public void unload() {
			cancel();
		}

	}

	public void execute(String prefix, final TaskIterator it, Runnable onFinish) {
		mt = new MultiTaskRunner(it);
		final TaskMonitor mon = new SanaTaskMonitor();

		if (onFinish != null)
			mt.addCleanupFunc(onFinish);

		runJob(new Runnable() {
			@Override
			public void run() {
				mt.run(mon);
			}
		});
	}

	public void cancel() {
		if (mt != null) {
			mt.cancel();
		}

	}

}
