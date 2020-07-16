package com.itcp.generalscan;

import android.widget.Toast;

import com.google.zxing.Result;
import com.itcp.scanlib.ui.BaseScanActivity;

public class ScanActivity extends BaseScanActivity {

    @Override
    protected void handleBusiness(Result result) {
        Toast.makeText(ScanActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
    }
}
