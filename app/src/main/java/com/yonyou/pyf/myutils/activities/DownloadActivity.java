package com.yonyou.pyf.myutils.activities;

import android.app.DownloadManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.yonyou.pyf.myutils.R;

import java.util.Timer;
import java.util.TimerTask;

public class DownloadActivity extends AppCompatActivity implements View.OnClickListener {

    private Button down;
    private TextView file_name;
    private TextView progress;
    private ProgressBar pb_update;
    private DownloadManager downloadManager;
    private static String downloadUrl = "";
    private DownloadManager.Request request;
    private Timer timer;
    private TimerTask task;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            int pro = bundle.getInt("pro");
            String name = bundle.getString("name");
            pb_update.setProgress(pro);
            progress.setText(String.valueOf(pro) + "%");
            file_name.setText(name);
        }
    };
    private long id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        down = (Button) findViewById(R.id.down);
        file_name = (TextView) findViewById(R.id.file_name);
        progress = (TextView) findViewById(R.id.progress);
        pb_update = (ProgressBar) findViewById(R.id.pb_update);
        down.setOnClickListener(this);
        downloadManager = ((DownloadManager) getSystemService(DOWNLOAD_SERVICE));
        request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setTitle("---标题---")
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
                .setAllowedOverRoaming(false)
                .setMimeType("application/vnd.android.package-archive")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //创建目录
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir();
        //设置文件存放目录
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app.release.apk");
        pb_update.setMax(100);
        final DownloadManager.Query query = new DownloadManager.Query();
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                Cursor cursor = downloadManager.query(query.setFilterById(android.R.attr.id));
                if (cursor != null && cursor.moveToFirst()) {
                    if (cursor.getInt(
                            cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                            == DownloadManager.STATUS_SUCCESSFUL) {
                        pb_update.setProgress(100);
                        install(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/app-release.apk");
                        task.cancel();
                        String title = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE));
                        String address = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        int pro = (bytes_downloaded * 100) / bytes_total;
                        Message msg = Message.obtain();
                        Bundle bundle = new Bundle();
                        bundle.putInt("pro", pro);
                        bundle.putString("name", title);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                    cursor.close();
                }
            }
        };
        timer.schedule(task, 0, 1000);
    }

    @Override
    public void onClick(View v) {
        id = downloadManager.enqueue(request);
        task.run();
        down.setClickable(false);
        down.setBackgroundColor(Color.RED);

    }

    private void install(String path) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//4.0以上系统弹出安装成功打开界面
        startActivity(intent);
    }
}
