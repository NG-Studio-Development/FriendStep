package com.ngstudio.friendstep.model.connectivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import com.ngstudio.friendstep.BuildConfig;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.utils.SingletonHelper;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkExecutor {

	private static final String TAG = NetworkExecutor.class.getSimpleName();


	private final Context         context;
	private final ExecutorService taskExecutor;

	private NetworkPriority networkPriority;


	private NetworkExecutor(@NotNull Context context, @NotNull ExecutorService taskExecutor) {
		this.context = context;
		this.taskExecutor = taskExecutor;
		this.networkPriority = new NetworkPriority.Builder().build();
	}


	public final synchronized void setNetworkPriority(@NotNull NetworkPriority networkPriority) {
		this.networkPriority = networkPriority;
	}

	public final synchronized NetworkPriority getNetworkPriority() {
		return networkPriority;
	}


	private static final int NETWORK_TYPE_NONE = -1;

	private final Queue<Task> postponedTasks = new ArrayDeque<>();


	private BroadcastReceiver connectionStateReceiver;

	private void engageConnectionStateReceiver() {
		if (connectionStateReceiver != null)
			return;

		context.registerReceiver(connectionStateReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (isInitialStickyBroadcast() || intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false))
					return;

				final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Service.CONNECTIVITY_SERVICE);

				NetworkInfo network;
				{
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
						network = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
					} else {
						network = cm.getNetworkInfo(intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, NETWORK_TYPE_NONE));
					}
					if (network == null || network.isFailover())
						return;

					if (!network.isConnectedOrConnecting()) {
						network = cm.getActiveNetworkInfo();

						if (network == null)
							return;
					}
				}

				synchronized (postponedTasks) {
					Iterator<Task> taskIt = postponedTasks.iterator();

					while (taskIt.hasNext()) {
						Task task = taskIt.next();

						if (task.getNetworkPriority().getNetworkPriority(network.getType()) >= task.getDesiredNetworkPriority()) {
                            Log.e("test!","do postponed");
							task.submit(NetworkExecutor.this);

							taskIt.remove();
							Log.d(TAG, "Executing the postponed task: " + task);
						}
					}
					if (postponedTasks.isEmpty()) {
						connectionStateReceiver = null;

						context.unregisterReceiver(this);
					}
				}
			}
		}, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}

	private static boolean checkTaskAffinity(@NotNull ConnectivityManager cm, @NotNull Task regularTask) {
		NetworkInfo[] networksAvailable = cm.getAllNetworkInfo();

		if (networksAvailable == null || networksAvailable.length == 0)
			return false;

		List<NetworkInfo> networksExclusiveList = new ArrayList<>(Arrays.asList(networksAvailable));
		for (int i = networksExclusiveList.size() - 1; i >= 0; --i) {
			NetworkInfo info = networksExclusiveList.get(i);
			if (!(info.isAvailable() && info.isConnectedOrConnecting()))
				networksExclusiveList.remove(info);
		}
		if (networksExclusiveList.isEmpty())
			return false;

		final NetworkPriority networkPriority = regularTask.getNetworkPriority();

		int available = 0;
		for (int i = 0, imax = networkPriority.size(); i < imax; ++i) {
			for (NetworkInfo info : networksExclusiveList) {
				if (info.getType() == networkPriority.getAffinityAt(i)) {
					available = networkPriority.getPriorityAt(i);

					networksExclusiveList.remove(info);
					break;
				}
			}
		}
		return available >= regularTask.getDesiredNetworkPriority();
	}

	private boolean isPostponed(Task task) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (!checkTaskAffinity(cm, task)) {
			if (task.waitForConnection(cm, MainThreadHandler.INSTANCE))
				return true;

			synchronized (postponedTasks) {
				if (postponedTasks.offer(task)) {
					engageConnectionStateReceiver();

					return true;
				}
			}
			throw new Error("Postponed task queue exceeded!");
		}
		return false;
	}


	private static final int TASK_MESSAGE_PUBLISH_PROGRESS = 1;
	private static final int TASK_MESSAGE_CANCEL           = 2;
	private static final int TASK_MESSAGE_FINISH           = 3;

	private static final class TaskHolder<DataType> {

		public final Task       task;
		public final DataType[] data;

		@SafeVarargs
		public TaskHolder(Task task, DataType... data) {
			this.task = task;
			this.data = data;
		}

	}


	@SuppressWarnings("unchecked")
	private static final class MainThreadHandler {
		private static final Handler INSTANCE = new Handler(Looper.getMainLooper()) {
			@Override
			public void handleMessage(Message msg) {
				final TaskHolder taskHolder = (TaskHolder) msg.obj;

				assert taskHolder != null;
				switch (msg.what) {
					case TASK_MESSAGE_PUBLISH_PROGRESS:
						taskHolder.task.onProgress(taskHolder.data);
						break;

					case TASK_MESSAGE_CANCEL:
						taskHolder.task.onCancelled(taskHolder.data[0]);
						break;

					case TASK_MESSAGE_FINISH:
						taskHolder.task.onFinish(taskHolder.data[0]);
						break;
				}
			}
		};
	}


	public static abstract class Task<Result, Progress> {

		private final int networkAffinity;


		public Task(@MagicConstant(valuesFromClass = ConnectivityManager.class) int networkAffinity) {
			this.networkAffinity = networkAffinity;

			resetNetworkPriorities();
		}


		public boolean waitForConnection(@NotNull ConnectivityManager cm, @NotNull Handler mainThreadHandler) {
			return true;
		}


		@MagicConstant(valuesFromClass = ConnectivityManager.class)
		public final int getNetworkAffinity() {
			return networkAffinity;
		}


		protected void onSubmit() {
			/* onSubmit method stub */
		}

		public void onStart() {
			/* onStart method stub */
		}

		public void onPostponed() {
			/* onPostponed method stub */
		}


		@SuppressWarnings("unchecked")
		public void onProgress(@NotNull Progress... progress) {
			/* onProgress method stub */
		}

		public void onFinish(@Nullable Result result) {
			/* onFinish method stub */
		}

		public void onCancelled(@Nullable Result result) {
			/* onCancelled method stub */
		}


		public abstract Result doInBackground();


		@SafeVarargs
		protected final void reportProgress(Progress... progress) {
			if (progress == null)
				return;

			MainThreadHandler.INSTANCE.obtainMessage(
					TASK_MESSAGE_PUBLISH_PROGRESS, new TaskHolder<>(this, progress)).sendToTarget();
		}


		public final boolean execute() {
			return submit(getInstance());
		}


		private volatile Future taskFuture;

		private final AtomicBoolean cancelled        = new AtomicBoolean(false);
		private final Object        executionMonitor = new Object();


		private NetworkPriority networkPriority;

		public final NetworkPriority getNetworkPriority() {
			synchronized (executionMonitor) {
				return networkPriority;
			}
		}


		private int desiredNetworkPriority = NetworkPriority.NETWORK_PRIORITY_UNDEFINED;

		public final int getDesiredNetworkPriority() {
			synchronized (executionMonitor) {
				return desiredNetworkPriority;
			}
		}


		private void setNetworkPriorities(NetworkPriority networkPriority) {
			int desiredNetworkPriority = networkPriority.getNetworkPriority(getNetworkAffinity());
			if (desiredNetworkPriority == NetworkPriority.NETWORK_PRIORITY_UNDEFINED) {
				throw new Error("Network executor does not provide the state of desired connection.");
			}
			this.networkPriority = networkPriority;
			this.desiredNetworkPriority = desiredNetworkPriority;
		}

		private void resetNetworkPriorities() {
			this.networkPriority = null;
			this.desiredNetworkPriority = NetworkPriority.NETWORK_PRIORITY_UNDEFINED;
		}


		private boolean submit(final NetworkExecutor executor) {
			synchronized (executionMonitor) {
				Future taskFutureLocal = taskFuture;

				if (taskFutureLocal != null) {
					throw new IllegalStateException("This task is submitted already!");
				}
				setNetworkPriorities(executor.getNetworkPriority());

				try {
					taskFutureLocal = executor.taskExecutor.submit(new Runnable() {
						@Override
						public void run() {
							if (isCancelled()) {
								return;
							}

							if (executor.isPostponed(Task.this)) {
//								cancel();

								synchronized (executionMonitor) {
									taskFuture = null;
								}

								MainThreadHandler.INSTANCE.post(new Runnable() {
									@Override
									public void run() {
										Task.this.onPostponed();
									}
								});
								return;
							}

							TaskHolder<Result> result = null;
							try {
								final Runnable onStartTask = new Runnable() {
									@Override
									public void run() {
										onStart();

										synchronized (this) {
											((Object) this).notify();
										}
									}
								};

								//noinspection SynchronizationOnLocalVariableOrMethodParameter
								synchronized (onStartTask) {
									MainThreadHandler.INSTANCE.post(onStartTask);
									onStartTask.wait();

									result = new TaskHolder<>(Task.this, doInBackground());
								}
							} catch (InterruptedException e) {
								cancel();
							}

							final Message taskMessage;
							if (isCancelled()) {
								taskMessage = MainThreadHandler.INSTANCE
										.obtainMessage(TASK_MESSAGE_CANCEL, result);
							} else {
								taskMessage = MainThreadHandler.INSTANCE
										.obtainMessage(TASK_MESSAGE_FINISH, result);
							}
							taskMessage.sendToTarget();

							if (!isCancelled()) {
								synchronized (executionMonitor) {
									resetNetworkPriorities();

									taskFuture = null;
								}
							}
						}
					});
				} catch (RejectedExecutionException e) {
					resetNetworkPriorities();

					e.printStackTrace();
					return false;
				}
				taskFuture = taskFutureLocal;

				cancelled.compareAndSet(true, false);
				MainThreadHandler.INSTANCE.post(new Runnable() {
					@Override
					public void run() {
						onSubmit();
					}
				});
			}
			return true;
		}


		protected final boolean cancel(boolean mayInterrupt) {
			if (!isCancelled()) {
				synchronized (executionMonitor) {
					if (isCancelled())
						return true;

					Future taskFutureLocal = taskFuture;

					if (taskFutureLocal == null) {
						throw new IllegalStateException("Task wasn't submitted yet!");
					}

					try {
						return taskFutureLocal.cancel(mayInterrupt);
					} finally {
						cancelled.compareAndSet(false, true);

						taskFuture = null;
					}
				}
			}
			return true;
		}

		public final void cancel() {
			cancel(false);
		}


		public final boolean isCancelled() {
			return cancelled.get();
		}


		@Override
		public String toString() {
			if (BuildConfig.DEBUG) {
				synchronized (executionMonitor) {
					return this.getClass()
							.getSimpleName() + '@' + Integer.toHexString(this.hashCode()) + "{" +
							"networkAffinity=" + networkAffinity +
							", networkPriority=" + networkPriority +
							", desiredNetworkPriority=" + desiredNetworkPriority +
							", submitted=" + (taskFuture != null) +
							", cancelled=" + cancelled.get() +
							'}';
				}
			}
			return super.toString();
		}
	}


	private static final int      NUMBER_OF_CORES         = Runtime.getRuntime().availableProcessors();
	private static final int      THREAD_TIMEOUT_DURATION = 5;
	private static final TimeUnit THREAD_TIMEOUT_UNIT     = TimeUnit.SECONDS;

	private static final SingletonHelper<NetworkExecutor> SINGLETON
			= new SingletonHelper<>(NetworkExecutor.class, Context.class, ExecutorService.class);


	public static void init(@NotNull Context context, @NotNull ExecutorService executorService) {
		SINGLETON.initialize(null, context, executorService);
	}

	public static void init() {
		init(WhereAreYouApplication.getInstance(), new ThreadPoolExecutor(
				NUMBER_OF_CORES, NUMBER_OF_CORES * 2, THREAD_TIMEOUT_DURATION, THREAD_TIMEOUT_UNIT, new LinkedBlockingQueue<Runnable>()));
	}

	public static NetworkExecutor getInstance() {
		return SINGLETON.obtain(null);
	}


	public boolean submitTask(@NotNull Task task) {
		return task.execute();
	}

	public static boolean submit(@NotNull Task task) {
		return getInstance().submitTask(task);
	}

}