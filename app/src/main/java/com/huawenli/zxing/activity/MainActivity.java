package com.huawenli.zxing.activity;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.huawenli.zxing.R;
import com.huawenli.zxing.camera.CreateQRBitmp;
import com.huawenli.zxing.dialog.ImageOptDialog;
import com.huawenli.zxing.utils.BitmapUtil;
import com.huawenli.zxing.utils.ImageUtil;

public class MainActivity extends AppCompatActivity {
    private int SCAN_REQUEST_CODE=200;
    private int SELECT_IMAGE_REQUEST_CODE=201;
    protected final int PERMS_REQUEST_CODE = 202;

    private EditText etInput;//要生成的二维码的值
    private Bitmap qrCodeBitmap; //解析的二维码对象
    private ImageView ivQrImage; //生成的二维码图片

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //6.0版本或以上需请求权限
        String[] permissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            requestPermissions(permissions,PERMS_REQUEST_CODE);
        }


        etInput=findViewById(R.id.et_input);  //输入要生成的二维码内容
        ivQrImage=findViewById(R.id.iv_qr_image); //生成的二维码图片

        //按钮们
        findViewById(R.id.btn_scanning).setOnClickListener(onClickListener); //点击扫描图片
        findViewById(R.id.btn_select).setOnClickListener(onClickListener); //从相册选择图片识别
        findViewById(R.id.generate_qr_code).setOnClickListener(onClickListener); //点击生成二维码
        findViewById(R.id.btn_long_press).setOnClickListener(onClickListener); //长按二维码图片或者点击我识别

        //长按图片
        ivQrImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longPress();
                return false;
            }
        });
    }

    private View.OnClickListener onClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_scanning://点击扫描图片
                    Intent intent = new Intent(MainActivity.this,ScanActivity.class);//转到扫描活动
                    startActivityForResult(intent,SCAN_REQUEST_CODE);
                    break;
                case R.id.btn_select:
                    //激活系统图库，选择一张图片
                    Intent innerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
                    startActivityForResult(wrapperIntent, SELECT_IMAGE_REQUEST_CODE);
                    break;
                case R.id.generate_qr_code://生成二维码
                    String contentString = etInput.getText().toString().trim();
                    if(TextUtils.isEmpty(contentString)){
                        showToast("请输入二维码内容");
                        return ;
                    }
                    Log.i("huawenli","输入的内容:"+contentString);
                    Bitmap portrait = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
                    //两个方法，一个不传大小，使用默认
                    qrCodeBitmap = CreateQRBitmp.createQRCodeBitmap(contentString, portrait);
                    ivQrImage.setImageBitmap(qrCodeBitmap);
                    break;
                case R.id.btn_long_press:
                    longPress();
                    break;

            }
        }
    };

    private void longPress(){
        if(qrCodeBitmap==null){
            showToast("请先生成二维码图片");
            return ;
        }
        ImageOptDialog imageOptDialog=new ImageOptDialog(this);
        imageOptDialog.setCallback(new ImageOptDialog.ImageOptCallback() {
            //识别二维码
            @Override
            public void onIdentifyQrClick() {
                View view = getWindow().getDecorView().getRootView();//找到当前页面的根布局
                view.setDrawingCacheEnabled(true);//禁用绘图缓存，不禁用调用不了buildDrawingCache
                view.buildDrawingCache();//对view截图

                Bitmap temBitmap = view.getDrawingCache();
                String result= BitmapUtil.parseQRcode(temBitmap);
                showToast("长按识别二维码结果:"+result);

                //禁用DrawingCahce否则会影响性能 ,而且不禁止会导致每次截图到保存的是缓存的位图
                view.setDrawingCacheEnabled(false);//识别完成之后开启绘图缓存
            }

            //保存图片到本地
            @Override
            public void onSaveImageClick() {
                View view = getWindow().getDecorView().getRootView();//找到当前页面的根布局
                view.setDrawingCacheEnabled(true);//禁用绘图缓存
                view.buildDrawingCache();

                Bitmap temBitmap = view.getDrawingCache();
                ImageUtil.savePicToLocal(temBitmap,MainActivity.this);

                //禁用DrawingCahce否则会影响性能 ,而且不禁止会导致每次截图到保存的是缓存的位图
                view.setDrawingCacheEnabled(false);//识别完成之后开启绘图缓存

                showToast("保存图片到本地成功");
            }
        });
        imageOptDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode==SELECT_IMAGE_REQUEST_CODE){//从图库选择图片
            String[] proj = {MediaStore.Images.Media.DATA};
            // 获取选中图片的路径
            Cursor cursor = this.getContentResolver().query(intent.getData(),proj, null, null, null);
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String photoPath = cursor.getString(columnIndex);
                String result= BitmapUtil.parseQRcode(photoPath);
                if (!TextUtils.isEmpty(result)) {
                    showToast("从图库选择的图片识别结果:"+result);
                } else {
                    showToast("从图库选择的图片不是二维码图片");
                }
            }
            cursor.close();
        }else if (requestCode == SCAN_REQUEST_CODE && resultCode == RESULT_OK) {  //扫描活动传回来的
            String input = intent.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
            showToast("扫描结果:"+input);
        }
    }

    private void showToast(String str){
        Toast.makeText(MainActivity.this,str,Toast.LENGTH_LONG).show();
    }
}
