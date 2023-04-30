package com.moling.micabrowser.ui;

import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.moling.micabrowser.R;
import com.moling.micabrowser.databinding.ActivityBrowserBinding;
import com.moling.micabrowser.utils.Download;
import com.moling.micabrowser.utils.Global;
import com.moling.micabrowser.views.CircularProgressView;
import com.moling.micabrowser.widgets.URL.URLModel;

import org.xwalk.core.XWalkActivity;
import org.xwalk.core.XWalkDownloadListener;
import org.xwalk.core.XWalkNavigationHistory;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkView;

import java.util.List;
import java.util.Objects;

public class BrowserActivity extends XWalkActivity {
    public static BrowserActivity browserActivity;
    private ActivityBrowserBinding binding;
    private ImageView mImageMenu;
    private XWalkView mXWalkView;
    private CircularProgressView mProgressLoading;

    @Override
    protected void onXWalkReady() {
        XWalkSettings settings = mXWalkView.getSettings();
        // XWalkView 设置
        settings.setInitialPageScale(100); // 设置缩放比例
        settings.setJavaScriptEnabled(true); // 启用 JavaScript 支持
        settings.setJavaScriptCanOpenWindowsAutomatically(true); // 启用 JS 跳转
        settings.setAllowFileAccess(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        // XWalkView 事件监听
        mXWalkView.setResourceClient(new XWalkResourceClient(this.mXWalkView) {
            @Override
            public void onProgressChanged(XWalkView view, int i) {
                Log.d("[Mica]", "<Progress> | " + i);
                mProgressLoading.setProgress(i);
            }
            @Override
            public void onLoadStarted(XWalkView view, String url) {
                Global.history.put(mXWalkView.getTitle(), mXWalkView.getUrl());
                Log.d("[Mica]", "<LoadStarted> | " + mXWalkView.getTitle() + " - " + mXWalkView.getUrl());
            }
            @Override
            public void onLoadFinished(XWalkView view, String url) {
                Log.d("[Mica]", "<LoadFinished> | " + mXWalkView.getTitle() + " - " + mXWalkView.getUrl());
                mProgressLoading.setProgress(0);
            }
        });
        mXWalkView.setDownloadListener(new XWalkDownloadListener(getApplicationContext()) {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                String[] urlArray = url.split("/");
                Log.d("[Mica]", "url: " + url + " | UA: " + userAgent + " | contentDisposition: " + contentDisposition + " | contentLength: " + contentLength);
                new Thread(() -> {
                    if (!Download.fromUrl(url, urlArray[urlArray.length - 1], Environment.getExternalStoragePublicDirectory(DOWNLOAD_SERVICE).getAbsolutePath(), userAgent).equals("")) {
                        Looper.prepare();
                        Toast.makeText(MainActivity.mainActivity,urlArray[urlArray.length - 1] + "\n下载完成", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                }).start();
            }
        });
        // 加载 Url
        String url = getIntent().getData().toString();
        Log.d("[Mica]", "<BrowserActivity> | LoadURL - " + url);
        mXWalkView.loadUrl(url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBrowserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        browserActivity = this;

        // 控件绑定
        mXWalkView = binding.xwalkview;
        mImageMenu = binding.imageMenu;
        mProgressLoading = binding.progressLoading;

        // BottomSheet 初始化
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.dialog_menu);

        // BottomSheet 按钮
        mImageMenu.setOnClickListener(view -> {
            // 初始化书签按钮
            List<URLModel> bookmarksList = Global.bookmark.get();
            for (int i = 0; i < bookmarksList.size(); i++) {
                if (Objects.equals(mXWalkView.getUrl(), bookmarksList.get(i).getUrl())) {
                    ((ImageButton) dialog.findViewById(R.id.button_bookmark)).setImageResource(R.drawable.bookmark_starred);
                    break;
                } else {
                    ((ImageButton) dialog.findViewById(R.id.button_bookmark)).setImageResource(R.drawable.bookmark);
                }
            }
            // 初始化 URL 文本框
            ((EditText) dialog.findViewById(R.id.text_url)).setText(mXWalkView.getUrl());
            // 初始化标题文本框
            ((TextView) dialog.findViewById(R.id.text_url_title)).setText(mXWalkView.getTitle());
            dialog.show();
        });

        // 后退按钮事件
        dialog.findViewById(R.id.button_back).setOnClickListener(view -> {
            if (mXWalkView.getNavigationHistory().canGoBack()) {
                mXWalkView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.BACKWARD, 1);
            } else {
                Toast.makeText(this,"已是最前", Toast.LENGTH_SHORT).show();
            }
        });

        // 刷新按钮事件
        dialog.findViewById(R.id.button_refresh).setOnClickListener(view -> {
            mXWalkView.reload(0);
        });

        // 书签按钮事件
        dialog.findViewById(R.id.button_bookmark).setOnClickListener(view -> {
            List<URLModel> bookmarksList = Global.bookmark.get();
            // 书签已存在
            for (int i = 0; i < bookmarksList.size(); i++) {
                if (Objects.equals(mXWalkView.getUrl(), bookmarksList.get(i).getUrl())) {
                    Global.bookmark.delete(bookmarksList.size() - 1 - i);
                    dialog.findViewById(R.id.button_bookmark).setForeground(getResources().getDrawable(R.drawable.bookmark));
                    return;
                }
            }
            // 书签不存在
            Global.bookmark.put(mXWalkView.getTitle(), mXWalkView.getUrl());
            dialog.findViewById(R.id.button_bookmark).setForeground(getResources().getDrawable(R.drawable.bookmark_starred));
        });

        // 前进按钮事件
        dialog.findViewById(R.id.button_forward).setOnClickListener(view -> {
            if (mXWalkView.getNavigationHistory().canGoForward()) {
                mXWalkView.getNavigationHistory().navigate(XWalkNavigationHistory.Direction.FORWARD, 1);
            } else {
                Toast.makeText(this,"已是最后", Toast.LENGTH_SHORT).show();
            }
        });

        // URL 文本框回车事件
        EditText urlText = (EditText) dialog.findViewById(R.id.text_url);
        urlText.setOnKeyListener((view, KeyCode, keyEvent) -> {
            if (KeyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN && !urlText.getText().toString().equals("")) {
                Message searchMsg = new Message();
                searchMsg.obj = urlText.getText().toString();
                MainActivity.search.sendMessage(searchMsg);
            }
            return false;
        });
    }
}
