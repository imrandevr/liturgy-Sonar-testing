package com.stpauls.dailyliturgy.mp3Downloader;

import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.stpauls.dailyliturgy.Global;
import com.stpauls.dailyliturgy.base.App;
import com.stpauls.dailyliturgy.others.Callback;
import com.tonyodev.fetch2.DefaultFetchNotificationManager;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.DownloadNotification;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FetchHelper {

    private static final String TAG = "FetchHelp";
    private static FetchHelper instance;
    private Fetch fetch;
    private Callback<Download> callback;
    private ProgressCallBack downloadListener = new ProgressCallBack();

    private FetchHelper() {
        final FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(App.get())
                .setDownloadConcurrentLimit(4)
                .setNotificationManager(new com.stpauls.dailyliturgy.notification.DefaultFetchNotificationManager(App.get()) {
                    @NotNull
                    @Override
                    public Fetch getFetchInstanceForNamespace(@NotNull String namespace) {
                        return fetch;
                    }

                    @Override
                    public void updateNotification(
                            @NotNull NotificationCompat.Builder notificationBuilder,
                            @NotNull DownloadNotification downloadNotification,
                            @NotNull Context context
                    ) {
                        super.updateNotification(notificationBuilder, downloadNotification, context);
                        //update builder title here
                        //notificationBuilder.setContentTitle("new title");
                    }
                })
                .build();
        fetch = Fetch.Impl.getInstance(fetchConfiguration);
    }

    public synchronized static FetchHelper getInstance() {
        if (instance == null) {
            synchronized (FetchHelper.class) {
                if (instance == null) {
                    instance = new FetchHelper();
                }
            }
        }
        return instance;
    }

    public void downloadUrl(String url, Callback<Download> param) {
        downloadListener.setCallback(param);
        String[] index = url.split("/");
        String fileName = index[index.length - 1];
        File folder = Global.INSTANCE.getPOPULAR_HYMNS_FOLDER_PATH();
        folder.mkdir();
        File appSpecificExternalDir = new File(folder, fileName);
        try {
            appSpecificExternalDir.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Request request = new Request(url, appSpecificExternalDir.getPath());
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);
        fetch.removeListener(downloadListener);
        fetch.addListener(downloadListener);
        fetch.enqueue(request, updatedRequest -> {
            getListFiles(App.get().getFilesDir());

        }, error -> {
            Log.d(TAG, "downloadUrl: "+error.toString());
        });

    }


    public Set<String> getListFiles(File parentDir) {
        Set<String> inFiles = new HashSet();
        File[] files = parentDir.listFiles();
        if (files == null || files.length <= 0) {
            Log.d(TAG, "No file available");
            return inFiles;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file));
            } else {
                inFiles.add(file.getPath());
                Log.d(TAG, "FilesName = [" + file.getPath() + "]");
            }
        }

        return inFiles;
    }


    static class ProgressCallBack extends OnDownloadingCallback {

        private Callback<Download> callback;
        public void setCallback(Callback<Download> callback) {
            this.callback = callback;
        }

        @Override
        public void onProgress(@NotNull Download download, long etaInMilliSeconds, long downloadedBytesPerSecond) {
            super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond);
            Log.d(TAG, "onProgress() called with: download = [" + download + "], etaInMilliSeconds = [" + etaInMilliSeconds + "], downloadedBytesPerSecond = [" + downloadedBytesPerSecond + "]");
            //if (callback != null) callback.onSuccess(download);
        }

        @Override
        public void onCompleted(@NotNull Download download) {
            super.onCompleted(download);
            Log.d(TAG, "onCompleted() called with: download = [" + download + "]");
            if (callback != null) callback.onSuccess(download);
        }
    }
}
