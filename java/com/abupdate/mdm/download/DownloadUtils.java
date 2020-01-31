package com.abupdate.mdm.download;

import android.os.Handler;
import android.os.Looper;

import com.abupdate.mdm.utils.LogUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUtils {
    private static DownloadUtils instance;
    private OkHttpClient okHttpClient;
    private Handler mHandler;

    public static DownloadUtils getInstance() {
        if (instance == null) instance = new DownloadUtils();
        return instance;
    }

    private DownloadUtils() {
        this.mHandler = new Handler(Looper.getMainLooper());
        okHttpClient = new OkHttpClient();
    }

    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess();

        /**
         * 下载失败
         */
        void onDownloadFailed();


        /**
         * 下载进度
         */
        void onDownloadProgress(int pro);


    }

    /**
     * @param url      下载连接
     * @param saveDir  储存下载文件的SDCard目录
     * @param listener 下载监听
     */
    public void download(final String url, final String saveDir, final String saveName, final OnDownloadListener listener) {
        Request request = new Request.Builder().url(url).build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                LogUtils.e("下载失败: " + e.getMessage());
                listener.onDownloadFailed();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Okhttp/Retofit 下载监听
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(saveDir, saveName);
                    if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    fos.flush();
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;

                        int progress = (int) (sum * 1.0f / total * 100);
                        //下载进度

                        listener.onDownloadProgress(progress);


                    }
                    fos.flush();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // 下载完成
                            listener.onDownloadSuccess();
                        }
                    });

                } catch (Exception e) {
                    LogUtils.e("下载异常: " + e.getMessage());
                    try {
                        if (is != null) is.close();
                    } catch (IOException es) {
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException es) {
                    }
                    listener.onDownloadFailed();
                } finally {
                    try {
                        if (is != null) is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null) fos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });
    }
}
