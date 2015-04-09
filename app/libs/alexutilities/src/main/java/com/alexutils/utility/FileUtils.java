package com.alexutils.utility;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    private static boolean isExternalStorageAvailable;
    private static boolean isExternalStorageWritable;
    private static long availableSpace;
    private Context context;

    private static FileUtils instance;

    private FileUtils(Context context) {
        this.context = context;
    }

    private static final String TAG = FileUtils.class.getSimpleName();

    public static synchronized File getFilesDir() {
        updateExternalStorageState();
        File dir;
        if (!isExternalStorageAvailable) {
            dir = instance.getContext().getFilesDir();
        } else {
            dir = instance.getContext().getExternalFilesDir(null);
        }
        if (dir == null && isExternalStorageAvailable) {
            dir = instance.getContext().getFilesDir();
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static synchronized File getExternalFilesDir() {
        if(isExternalStorageAvailable()) {
            return Environment.getExternalStorageDirectory();
        }
        return null;
    }

	private static double calcAvailableSpaceFromStat(StatFs statFs) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return statFs.getAvailableBlocks() * statFs.getBlockSize();
		} else {
			return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
		}
	}


    private static void updateExternalStorageState() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            isExternalStorageAvailable = isExternalStorageWritable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            isExternalStorageAvailable = true;
            isExternalStorageWritable = false;
        } else {
            isExternalStorageAvailable = isExternalStorageWritable = false;
        }

        if (isExternalStorageAvailable) {
            File extdir = Environment.getExternalStorageDirectory();
	        try {
		        StatFs stats = new StatFs(extdir.getAbsolutePath());
		        availableSpace = Math.round(calcAvailableSpaceFromStat(stats));
	        } catch (IllegalArgumentException e) {
		        availableSpace = 0L;
	        }
        }
    }

    private static File getFile(File dir, String filename) {
        File tmpFile = new File(dir, filename);
        if (!tmpFile.exists()) {
            try {
                tmpFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tmpFile;
    }

    public static void deleteFiles(File directory, final String filtername) {
        String[] files = directory.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains(filtername);
            }
        });
        if (files == null) {
            return;
        }
        for (String file : files) {
            File imageFile = new File(directory.getPath() + File.separator + file);
            imageFile.delete();
        }
    }

    public static void deleteRecursively(File fileOrDirectory) {
        if (!fileOrDirectory.exists()) {
            return;
        }
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursively(child);
            }
        }
        fileOrDirectory.delete();
    }

    public static File getDir(String filename) {
        File dir = new File(getFilesDir(), filename);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static void clearCache(File cacheDir, long time) {
        if (cacheDir == null) {
            return;
        }
        if (!cacheDir.isDirectory()) {
            if (System.currentTimeMillis() - cacheDir.lastModified() >= time) {
                cacheDir.delete();
            }
            return;
        }
        File[] files = cacheDir.listFiles();
        if (files == null) {
            return;
        }
        for (File child : files) {
            clearCache(child, time);
        }
        if (files.length == 0) {
            cacheDir.delete();
        }
    }


	public static boolean copyFile(File fileIn, File fileOut) {
		if (!fileIn.exists())
			return false;
		if (fileIn.equals(fileOut))
			return true;

		try {
			FileInputStream fis = new FileInputStream(fileIn);

			try {
				FileOutputStream fos = new FileOutputStream(fileOut);
				try {
					copyStream(fis, fos);
				} finally {
					fos.close();
				}
			} catch (IOException ignore) {
			    /* Nothing to do */
			}
			fis.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

    public static void copyStream(InputStream is, OutputStream os) throws IOException {
        final int buffer_size = 1024;
        byte[] bytes = new byte[buffer_size];
        int count;
        while ((count = is.read(bytes, 0, buffer_size)) != -1) {
            os.write(bytes, 0, count);
        }
    }

    public static boolean saveBytes(File saveFile, byte[] bytes) {
        boolean success = false;
        OutputStream os = null;
        try {
            os = new FileOutputStream(saveFile);
            os.write(bytes);
            success = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    public static synchronized void init(@NotNull Context context) {
        instance = new FileUtils(context);
    }

    private Context getContext(){
        return context;
    }

    public static boolean isExternalStorageAvailable() {
        updateExternalStorageState();
        return isExternalStorageAvailable && isExternalStorageWritable;
    }
}
