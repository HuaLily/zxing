package com.huawenli.myzxing.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huawenli.myzxing.R;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int START_SACN_REQUESTCODE = 1 ;
    TextView mShowResultTv;
    boolean island;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //隐藏标题栏
        ActionBar bar = getSupportActionBar();
        if (bar != null){
            bar.hide();
        }

        mShowResultTv =  findViewById(R.id.show_result_tv);

        Button startScanBtn = findViewById(R.id.start_scan_bt);

        Button change_orientation_btn = findViewById(R.id.change_orientation);
        change_orientation_btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SourceLockedOrientationActivity")
            @Override
            public void onClick(View view) {
                    Configuration cfg = getResources().getConfiguration();
                    if ( cfg.orientation == Configuration.ORIENTATION_LANDSCAPE ){ //当前是横屏
                        MainActivity.this.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //转化成竖屏

                    }else {
                        MainActivity.this.setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                    }
            }
        });


        startScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                //使用EasyPermissions查看权限是否已申请
//                String str = PermissionsLogUtils.easyCheckPermissions(MainActivity.this,
//                        Manifest.permission.CAMERA);
//                Log.i("permission",str);
//
//                //获取权限
//                EasyPermissions.requestPermissions(MainActivity.this,
//                        "接下来需要获取WRITE_EXTERNAL_STORAGE权限",
//                        1,
//                        Manifest.permission.CAMERA);



                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //当前系统大于等于6.0
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        //具有读写权限，直接调用
                        Toast.makeText(MainActivity.this, "具有读写权限", Toast.LENGTH_LONG).show();
                    } else {
                        //不具有读写权限，需要进行权限申请
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1001);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "具有读写权限", Toast.LENGTH_LONG).show();
                }

                //点击扫码的时候，需要判断当前的屏幕方向
                Configuration configuration = MainActivity.this.getResources().getConfiguration();
                if (configuration.orientation ==  Configuration.ORIENTATION_LANDSCAPE  ){
                    island = true;
                }
                if (configuration.orientation ==  Configuration.ORIENTATION_PORTRAIT ){
                    island = false;
                }

                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                intent.putExtra("island",island);
                startActivityForResult(intent,START_SACN_REQUESTCODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == START_SACN_REQUESTCODE ){
            if (resultCode == RESULT_OK){
                //显示扫描的内容
                mShowResultTv.setText(data.getStringExtra("result"));
            }

        }

    }

    //申请权限
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        //EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    //当权限被成功申请的时候执行回调，requestCode是代表你权限请求的识别码，list里面装着申请的权限的名字：
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        switch (requestCode){
            case 1:
                Toast.makeText(this, "已获取Camera权限", Toast.LENGTH_SHORT).show();
                break;
//            case 1:
//                Toast.makeText(this, "已获取WRITE_EXTERNAL_STORAGE和WRITE_EXTERNAL_STORAGE权限", Toast.LENGTH_SHORT).show();
//                break;
        }

    }

    //当权限申请失败的时候执行的回调
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

        //处理权限名字字符串
        StringBuffer sb = new StringBuffer();
        for (String str : perms){
            sb.append(str);
            sb.append("\n");
        }
        sb.replace(sb.length() - 2,sb.length(),"");

        switch (requestCode){
            case 1:
                Toast.makeText(this, "已拒绝权限" + perms.get(0), Toast.LENGTH_SHORT).show();
                break;
//            case 1:
//                Toast.makeText(this, "已拒绝WRITE_EXTERNAL_STORAGE和WRITE_EXTERNAL_STORAGE权限"+ perms.get(0), Toast.LENGTH_SHORT).show();
//                break;
        }
        //判断是否有权限被勾选了不再询问并拒绝
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            Toast.makeText(this, "已拒绝权限" + sb + "并不再询问" , Toast.LENGTH_SHORT).show();
            new AppSettingsDialog
                    .Builder(this)
                    .setRationale("此功能需要" + sb + "权限，否则无法正常使用，是否打开设置")
                    .setPositiveButton("好")
                    .setNegativeButton("不行")
                    .build()
                    .show();
        }
    }
}
