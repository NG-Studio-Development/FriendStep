package com.alexutils.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.alexutilities.BuildConfig;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Alex on 13.03.14.
 */
public abstract class SQLiteInterlockHelper extends SQLiteOpenHelper {

	private final Context context;
	private final String  name;

	protected SQLiteInterlockHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(context, name, factory, version);

		this.context = context;
		this.name = name;
	}


	public Context getContext() {
		return context;
	}

	public String getName() {
		return name;
	}


	public interface Creator<T extends SQLiteInterlockHelper> {
		public T newInstance(Context context, String name);
	}


	private static final String OBLIGATORY_CREATOR_FIELD_NAME = "CREATOR";

	private static <T extends SQLiteInterlockHelper> T invokeCreator(Class<T> clazz, Context context, String name) {
		try {
			@SuppressWarnings("unchecked")
			Creator<T> creator = (Creator<T>) clazz
					.getField(OBLIGATORY_CREATOR_FIELD_NAME).get(null);
			return creator.newInstance(context, name);
		} catch (NoSuchFieldException e) {
			throw new Error("SQLInterlockHelper successor MUST implements a Creator<T> interface! " +
					"It should be stored in 'public static Creator<T> CREATOR'");
		} catch (IllegalAccessException e) {
			throw new Error("SQLInterlockHelper successor CREATOR field is inaccessible or non-static!");
		}
	}

	private static final class SQLiteInterlockHelperSingleton {
		public static final Map<Class, SQLiteInterlockHelper> INSTANCE_MAP
				= Collections.synchronizedMap(new WeakHashMap<Class, SQLiteInterlockHelper>());
	}


	private static boolean isObsolete(SQLiteInterlockHelper cachedInstance, Context context, String name) {
		return cachedInstance == null || !(cachedInstance.getContext().equals(context)
				&& TextUtils.equals(cachedInstance.getName(), name));
	}

	@SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter", "unchecked"})
	protected static <T extends SQLiteInterlockHelper> T getInstance(@NotNull Class<T> clazz, @NotNull Context context, @NotNull String name) {
		if (BuildConfig.DEBUG) {
			if (TextUtils.isEmpty(name)) {
				throw new IllegalArgumentException("name cannot be empty!");
			}
		}

		SQLiteInterlockHelper instanceLocal = SQLiteInterlockHelperSingleton.INSTANCE_MAP.get(clazz);

		if (isObsolete(instanceLocal, context, name)) {
			synchronized (clazz) {
				instanceLocal = SQLiteInterlockHelperSingleton.INSTANCE_MAP.get(clazz);

				if (isObsolete(instanceLocal, context, name)) {
					SQLiteInterlockHelperSingleton.INSTANCE_MAP.put(clazz, instanceLocal
							= invokeCreator(clazz, context, name));
				}
			}
		}
		return (T) instanceLocal;
	}


	private static class DatabaseHolder {
		public final SQLiteDatabase database;
		public final int            accessMode;

		public DatabaseHolder(SQLiteDatabase database, int accessMode) {
			if (BuildConfig.DEBUG) {
				if (database == null)
					throw new NullPointerException("Database object is 'null'!");

				if (accessMode == SQLiteDatabase.OPEN_READWRITE && database.isReadOnly())
					throw new IllegalStateException("Database state and provided access mode mismatch!");
			}

			this.database = database;
			this.accessMode = accessMode;
		}
	}


	private static final int DATABASE_ACCESS_MODE_RELEASE = -1;
	private static final int DATABASE_ACCESS_MODE_CURRENT = 0;
	private static final int DATABASE_ACCESS_MODE_READ    = 1;
	private static final int DATABASE_ACCESS_MODE_WRITE   = 2;


	private DatabaseHolder databaseHolder;

	private DatabaseHolder getDatabaseHolder(int accessMode) {
		if (databaseHolder != null) {
			if (BuildConfig.DEBUG) {
				if (!databaseHolder.database.isOpen())
					throw new IllegalStateException("Database object is corrupted!");
				if (databaseHolder.accessMode < accessMode)
					throw new IllegalStateException("Database open mode mismatch!");
			}

			try {
				return databaseHolder;
			} finally {
				if (accessMode == DATABASE_ACCESS_MODE_RELEASE) {
					databaseHolder.database.close();

					databaseHolder = null;
				}
			}
		} else if (accessMode == DATABASE_ACCESS_MODE_CURRENT
				|| accessMode == DATABASE_ACCESS_MODE_RELEASE) {
			throw new Error("Database hasn't been open yet!");
		}

		final SQLiteDatabase db;

		switch (accessMode) {
			case DATABASE_ACCESS_MODE_READ:
				db = getReadableDatabase();

				assert db != null;
				if (!db.isOpen())
					return null;
				break;

			case DATABASE_ACCESS_MODE_WRITE:
				db = getWritableDatabase();

				assert db != null;
				if (!db.isOpen() || db.isReadOnly())
					return null;
				break;

			default:
				throw new Error("Bad database open mode!");
		}
		databaseHolder = new DatabaseHolder(db, accessMode);

		return databaseHolder;
	}


	public final class DatabaseHandler {

		private final SQLiteDatabase database;
		private final int            openMode;

		private DatabaseHandler(SQLiteDatabase database, int openMode) {
			this.database = database;
			this.openMode = openMode;
		}

		@NotNull
		public SQLiteDatabase getDatabase() {
			return database;
		}

		public int getOpenMode() {
			return openMode;
		}


		public void release() {
			releaseLockedDatabase(openMode);
		}
	}



	private final AtomicInteger referenceCount = new AtomicInteger(0);
	private final ReadWriteLock readWriteLock  = new ReentrantReadWriteLock();

	private final Lock readLock = readWriteLock.readLock();

	public DatabaseHandler acquireLockedDatabaseReadable() {
		readLock.lock();

		synchronized (referenceCount) {
			final DatabaseHolder dbHolder = getDatabaseHolder(DATABASE_ACCESS_MODE_READ);

			if (dbHolder != null) {
				referenceCount.incrementAndGet();

				return new DatabaseHandler(
						dbHolder.database, SQLiteDatabase.OPEN_READONLY);
			}
			readLock.unlock();
		}
		return null;
	}


	private final Lock writeLock = readWriteLock.writeLock();

	public DatabaseHandler acquireLockedDatabaseWritable() {
		writeLock.lock();

		synchronized (referenceCount) {
			final DatabaseHolder dbHolder = getDatabaseHolder(DATABASE_ACCESS_MODE_WRITE);

			if (dbHolder != null) {
				referenceCount.incrementAndGet();

				return new DatabaseHandler(
						dbHolder.database, SQLiteDatabase.OPEN_READWRITE);
			}
			writeLock.unlock();
		}
		return null;
	}


    private void releaseLockedDatabase(int openMode) {
        synchronized (referenceCount) {
            getDatabaseHolder(referenceCount.decrementAndGet() == 0
                    ? DATABASE_ACCESS_MODE_RELEASE : DATABASE_ACCESS_MODE_CURRENT);

            switch (openMode) {
                case SQLiteDatabase.OPEN_READONLY:
                    readLock.unlock();
                    break;

                case SQLiteDatabase.OPEN_READWRITE:
                    writeLock.unlock();
                    break;
            }
        }
    }
}
