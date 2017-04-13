package com.yonyou.pyf.myutils.activities;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Intent;
import android.database.Cursor;
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
    private static String downloadUrl = "http://a.wdjcdn.com/release/files/phoenix/5.52.20.13520/wandoujia-wandoujia-web_inner_referral_binded_history_5.52.20.13520.apk?remove=2&append=%78%03eyJhcHBEb3dubG9hZCI6eyJkb3dubG9hZFR5cGUiOiJkb3dubG9hZF9ieV91cmwiLCJwYWNrYWdlTmFtZSI6ImNvbS5jaGluYW13b3JsZC5tYWluIiwiZG93bmxvYWRVcmwiOiJodHRwOi8vYXBwcy53YW5kb3VqaWEuY29tL3JlZGlyZWN0P3NpZ25hdHVyZVx1MDAzZDE1NDFiYjlcdTAwMjZ1cmxcdTAwM2RodHRwJTNBJTJGJTJGbW9iaWxlLnpodXNob3Uuc29nb3UuY29tJTJGYW5kcm9pZCUyRmRvd25sb2FkLmh0bWwlM0ZhcHBfaWQlM0RiMWU1YmNiM2QyYjkwNDQ2ZmIwMzk4NGRiZWIwYTRkY2UyYWFiNmEzYmY2YjUyYWUzYTVlY2E5Y2JlMzhiODUzZTdhNjNiYzkyZDBmZGExODY2ODBmZDUxNzJmOTI0NTdcdTAwMjZwblx1MDAzZGNvbS5jaGluYW13b3JsZC5tYWluXHUwMDI2bWQ1XHUwMDNkMzI5ZjUyNjYzZDc1OTE1MTcyZDVjOWNkOTQzNzQzNDlcdTAwMjZhcGtpZFx1MDAzZDIwODQ0OTMwXHUwMDI2dmNcdTAwM2QyMTJcdTAwMjZzaXplXHUwMDNkODU3ODY0NDdcdTAwMjZwb3NcdTAwM2R0JTJGaGlzdG9yeSUyRnZlcnNpb25zIiwidGl0bGUiOiLkuK3lm73lu7rorr7pk7booYwiLCJpY29uVXJsIjoiaHR0cDovL2ltZy53ZGppbWcuY29tL21tcy9pY29uL3YxLzUvZjAvZTdlMWVmZTc3YjIzNTM1YmY5MTI1NzA3ZjgzNjFmMDVfNzhfNzgucG5nIn19Wdj01B0003683638";
    private DownloadManager.Request request;
    private Timer timer;
    private TimerTask task;
    Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
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
                Cursor cursor = downloadManager.query(query.setFilterById(id));
                if (cursor != null && cursor.moveToFirst()) {
                    if (cursor.getInt(
                            cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                            == DownloadManager.STATUS_SUCCESSFUL) {
                        pb_update.setProgress(100);
                        install(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/app-release.apk");
                        task.cancel();
                    }

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
        down.setText("下载中，请勿重复点击");
//        down.setBackgroundColor(Color.RED);
        down.setBackgroundResource(R.drawable.btn_disable_shape);
    }

    private void install(String path) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + path), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//4.0以上系统弹出安装成功打开界面
        startActivity(intent);
    }
}
