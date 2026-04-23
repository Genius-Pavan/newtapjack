package com.pavan.tapchecker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final int OVERLAY_REQUEST = 1001;
    private WindowManager windowManager;
    private View overlayView;
    private TextView tvResult;
    private boolean overlayActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResult = findViewById(R.id.tvResult);
        Button btnCheck = findViewById(R.id.btnCheck);
        Button btnRemove = findViewById(R.id.btnRemove);

        btnCheck.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_REQUEST);
            } else {
                showOverlay();
            }
        });

        btnRemove.setOnClickListener(v -> removeOverlay());
    }

    private void showOverlay() {
        if (overlayActive) return;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Transparent red overlay
        overlayView = new View(this);
        overlayView.setBackgroundColor(Color.argb(150, 255, 0, 0));

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP;

        try {
            windowManager.addView(overlayView, params);
            overlayActive = true;
            tvResult.setText("OVERLAY ACTIVE!\n\nNow open Fisher LPG app.\n\nIf red overlay visible on Fisher LPG = VULNERABLE!\n\nPress REMOVE when done.");
            tvResult.setTextColor(Color.RED);
        } catch (Exception e) {
            tvResult.setText("Error: " + e.getMessage());
        }
    }

    private void removeOverlay() {
        if (overlayActive && overlayView != null) {
            windowManager.removeView(overlayView);
            overlayActive = false;
            tvResult.setText("Overlay removed.\nPress CHECK OVERLAY to test again.");
            tvResult.setTextColor(Color.DKGRAY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_REQUEST) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                showOverlay();
            } else {
                tvResult.setText("Permission denied! Allow overlay permission.");
                tvResult.setTextColor(Color.RED);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeOverlay();
    }
}
