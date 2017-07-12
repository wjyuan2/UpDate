package com.example.demo.update;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by  on 2017/7/10.
 */

public class SoftUpdate {
    private static final int DOWN_UPDATE = 1;
    private File ApkFile;
    private static final int DOWN_OVER = 2;
    private int progress;
    public static final File FILE_SDCARD = Environment.getExternalStorageDirectory();
    public static final String IMAGE_PATH = "FZGJ";
    public static final File FILE_LOCAL = new File(FILE_SDCARD, IMAGE_PATH);
    public static final File FILE_APK = new File(FILE_LOCAL, "/");
    private ProgressDialog dialogp;
    /* 下载包安装路径 */
    private static String savePath = FILE_APK.getAbsolutePath();
    private static final String saveFileName = "FZGJ.apk";
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    dialogp.setProgress(progress);
                    break;
                case DOWN_OVER:
                    installApk();
                    break;
                default:
                    break;
            }
        }

    };

    private Context mContext;
    public SoftUpdate(Context context)
    {
        this.mContext = context;
    }
    /**
     * 检测软件更新
     */
    public void checkUpdate()
    {
        if (isUpdate())
        {
            // 显示提示对话框
            showNoticeDialog();
        } else
        {
            Toast.makeText(mContext, "不需要更新", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 检查软件是否有更新版本
     *
     * @return
     */
    private boolean isUpdate(){
        // 获取当前软件版本
        int versionCode = getVersionCode(mContext);
        //通过服务器获得版本号
        try {
//            String str = HttpHelper.sendHttpRequest("http://127.0.0.1/Service1.svc/get_version");
//            JSONArray json1 =HttpHelper.GetJsonValue(str);
//            str_version=json1.getLong(0);
//            str_appname=json1.getString(1);
//            str_downurl=json1.getString(2);
//            if (str_version > versionCode) {
//                return true;
//            }
        }
        catch (Exception e) {
            //strError=e.toString();
        }
        return true;
    }

    /**
     * 获取软件版本号
     *
     * @param context
     * @return
     */
    private int getVersionCode(Context context)
    {
        int versionCode = 0;
        try
        {
            // 获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo("包名", 0).versionCode;
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog()
    {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle("应用更新");
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);

        float density = mContext.getResources().getDisplayMetrics().density;
        TextView tv = new TextView(mContext);
        tv.setMovementMethod(new ScrollingMovementMethod());
        tv.setVerticalScrollBarEnabled(true);
        tv.setTextSize(14);
        tv.setMaxHeight((int) (250 * density));
        tv.setText("我是更新的内容");
        alertDialog.setView(tv, (int) (25 * density), (int) (15 * density), (int) (25 * density), 0);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 显示下载对话框
                dialogp = new ProgressDialog(mContext);
                dialogp.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialogp.setMessage("下载中...");
                dialogp.setIndeterminate(false);
                dialogp.setCancelable(false);
                dialogp.show();

                // 下载文件
                downloadApk();
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "以后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog()
    {
        dialogp = new ProgressDialog(mContext);
        dialogp.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialogp.setMessage("下载中...");
        dialogp.setIndeterminate(false);
        dialogp.setCancelable(false);
        dialogp.show();
        // 现在文件
        downloadApk();
    }
    /**
     * 安装apk
     */
    private void installApk() {
        //File apkfile = new File(saveFileName);
        if (!ApkFile.exists()) {
            return;
        }
//        Intent i = new Intent(Intent.ACTION_VIEW);
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        i.setDataAndType(Uri.parse("file://" + ApkFile.toString()),
//                "application/vnd.android.package-archive");
//        context.startActivity(i);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.setDataAndType(Uri.fromFile(ApkFile), "application/vnd.android.package-archive");
        } else {
            Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".updatefileprovider", ApkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }


    private Runnable mdownApkRunnable = new Runnable() {


        @Override
        public void run() {
            try {
                URL url = new URL("https://raw.githubusercontent.com/WVector/AppUpdateDemo/master/apk/app-debug.apk");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdir();
                } else {
                    file.delete();
                }
                ApkFile = new File(savePath + saveFileName);
                FileOutputStream fos = new FileOutputStream(ApkFile);

                int count = 0;
                byte buf[] = new byte[1024];
                do {
                    int numread = is.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    // 更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        dialogp.dismiss();
                        // 下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (true);// 点击取消就停止下载.
                fos.close();
                is.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.e("printStackTrace", "" + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IOException", "" + e.getMessage());
            }

        }
    };

    /**
     * 下载apk
     */

    private void downloadApk() {
        Thread downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }
}
