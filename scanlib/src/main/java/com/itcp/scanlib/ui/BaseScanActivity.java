package com.itcp.scanlib.ui;

import android.Manifest;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.itcp.scanlib.R;
import com.luffy.generalutilslib.utils.PermissionUtils;
import com.itcp.scanlib.scan.helper.UploadScanHandler;
import com.itcp.scanlib.scan.scan.CameraManager;
import com.itcp.scanlib.scan.view.ViewfinderView;

/**
 * Created by lvlufei on 2018/12/3
 *
 * @desc
 */
public abstract class BaseScanActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    /*控件*/
    public ViewfinderView viewfinderView;
    public LinearLayout back;
    public TextView txt_title;
    public FrameLayout layoutSV;

    // 相机控制
    public CameraManager cameraManager;
    public UploadScanHandler handler;
    private boolean hasSurface;
    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_scan);
        initView();
    }

    private void initView() {
        layoutSV = findViewById(R.id.layout_sv);
        viewfinderView = findViewById(R.id.viewfinder_view);
        back = findViewById(R.id.back);
        txt_title = findViewById(R.id.txt_title);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickBack();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // CameraManager必须在这里初始化，而不是在onCreate()中。
        // 这是必须的，因为当我们第一次进入时需要显示帮助页，我们并不想打开Camera,测量屏幕大小
        // 当扫描框的尺寸不正确时会出现bug
        cameraManager = new CameraManager(getApplication());
        viewfinderView.setCameraManager(cameraManager);
        // 权限判断
        PermissionUtils.getInstance().meanWhileApplyMultiPermission(this, new PermissionUtils.MeanWhileApplyPermissionCallBack() {
            @Override
            public void onSucceed() {
                surfaceView = new SurfaceView(BaseScanActivity.this);
                layoutSV.addView(surfaceView);
                SurfaceHolder surfaceHolder = surfaceView.getHolder();
                if (hasSurface) {
                    /*初始化Camera*/
                    initCamera(surfaceHolder);
                } else {
                    /*重置callback，等待surfaceCreated()来初始化camera*/
                    surfaceHolder.addCallback(BaseScanActivity.this);
                }
            }

            @Override
            public void onFailure() {
                Toast.makeText(BaseScanActivity.this, "权限授权被拒绝，无法扫码，请允许权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }, Manifest.permission.CAMERA);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        cameraManager.closeDriver();
        if (layoutSV != null) {
            layoutSV.removeAllViews();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            /*初始化Camera*/
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
        if (surfaceView != null && surfaceView.getHolder() != null) {
            surfaceView.getHolder().removeCallback(BaseScanActivity.this);
        }
    }

    /**
     * 扫描成功，处理反馈信息
     *
     * @param result
     * @param barcode
     * @param scaleFactor
     */
    public void handleDecode(Result result, Bitmap barcode, float scaleFactor) {
        //这里处理解码完成后的结果，此处将参数回传到Activity处理
        if (barcode != null) {
            finish();
            handleBusiness(result);
        }
    }

    /**
     * 初始化Camera
     *
     * @param surfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            if (handler == null) {
                handler = new UploadScanHandler(this, null, null, null, cameraManager);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 处理扫码业务
     *
     * @param result
     */
    protected abstract void handleBusiness(Result result);

    /**
     * 设置标题
     *
     * @param title
     */
    public void setTitleString(String title) {
        txt_title.setText(title);
    }

    /**
     * 设置标题
     *
     * @param id
     */
    public void setTitleInt(int id) {
        txt_title.setText(id);
    }

    /**
     * 返回点击监听
     */
    public void clickBack() {
        onBackPressed();
    }

}
