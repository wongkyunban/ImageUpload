package com.wong.imageupload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // 动态请求权限
    private final static int REQUEST_PERMISSION_CODE = 1000;
    // 相册
    private final static int REQUEST_GALLERY = 100;
    // 招照
    private final static int REQUEST_CAMERA = 101;
    // 图片最大数量
    private final static int DEFAULT_NUM = 4;

    private ActivityViewHolder viewHolder;
    private List<ItemBean> list;
    private LoadImageAdapter adapter;
    private ItemBean addImgButton = new ItemBean(null, true);
    private Uri cameraUri; //  拍照的照片URI
    private File cameraFile;//  拍照

    private MyHandler myHandler ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewHolder = new ActivityViewHolder(getWindow());
        init();

        // 对于api23或以上,动态请求权限
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
                    || (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                    || (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {
                String sdpath = Environment.getExternalStorageDirectory() + "/tmp";
                File file = new File(sdpath);
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
            }
        }
    }

    private void init() {
        list = new ArrayList<>();
        list.add(addImgButton);
        viewHolder.mRV.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new LoadImageAdapter(list);
        myHandler = new MyHandler(this,adapter);
        adapter.setOnImageItemClickListener(new LoadImageAdapter.OnImageItemClickListener() {
            @Override
            public void onClick(View view, ItemBean itemBean, int position) {
                if (itemBean.isButton()) {
                    // 按钮事件，则打开图片选择器，添加图片
                    selectImage();
                } else {
                    // 图片事件，则预览图片

                    previewPhoto(itemBean.getImageFileBean());
                }
            }

            @Override
            public void onDelete(View view, ItemBean itemBean, int position) {

                list.remove(addImgButton);
                list.remove(itemBean);
                list.add(addImgButton);
                adapter.notifyDataSetChanged();

            }
        });
        viewHolder.mRV.setAdapter(adapter);
        viewHolder.mRV.addItemDecoration(new GridDividerItemDecoration(this, 15, true));


    }

    private void previewPhoto(ImageFileBean fileBean) {
        // TODO 添加预览图片功能
    }

    /**
     * 选择图片
     */
    private void selectImage() {
            showSelectDialog();
    }

    /**
     * 弹出对话框，让其决定选择图片方式：相册、拍照
     */
    private void showSelectDialog() {
        final CharSequence[] items = {"相册", "拍照"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("添加图片");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 根据item决定选择方式
                switch (which) {
                    case 0:// 相册
                        // 这种方式是通过action方式打开android的其他app来完成的
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK); // 系统默认的图片选择程序
                        galleryIntent.setType("image/*");
                        startActivityForResult(galleryIntent, REQUEST_GALLERY);
                        break;
                    case 1:// 拍照
                        // 这种方式是通过action方式打开android的其他app来完成的
                        // MediaStore.ACTION_IMAGE_CAPTURE 即android.media.action.IMAGE_CAPTURE
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 系统的相机程序
                        // 准备图片名称
                        String imageName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
                        // 创建应用内缓存目录cache/images
                        CacheUtils.createImagesCacheFolder(MainActivity.this);
                        cameraFile = new File(CacheUtils.getCacheImagesFolder(MainActivity.this).getPath() + "/" + imageName);
                        // 创建好图片文件接收拍照的数据
                        if (!cameraFile.exists()) {
                            try {
                                cameraFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            // 在官方7.0的以上的系统中，尝试传递 file://URI可能会触发FileUriExposedException。7.0以上的系统需要使用FileProvider兼容拍照
                            cameraUri = FileProvider.getUriForFile(MainActivity.this, "com.wong.camera.fileprovider", cameraFile);
                        } else {
                            cameraUri = Uri.fromFile(cameraFile);
                        }
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                        startActivityForResult(cameraIntent, REQUEST_CAMERA);

                        break;
                }
            }
        }).create();
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GALLERY:
                // 来自相册
                if (resultCode == Activity.RESULT_OK && data != null) {
                    Uri uri = data.getData();
                    saveUriToFile(uri,REQUEST_GALLERY);
                }

                break;
            case REQUEST_CAMERA:
                // 来自拍照
                if (resultCode == Activity.RESULT_OK) {
                    saveUriToFile(cameraUri,REQUEST_CAMERA);
                }
                break;
        }
    }

    /**
     * 将Uri图片类型转换成File，BitMap类型
     * 在界面上显示BitMap图片，以防止内存溢出
     * 上传可选择File文件上传
     *
     * @param uri
     */
    private void saveUriToFile(Uri uri,int from) {
        Bitmap bitmap = null;
        if (uri != null) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2; // 图片宽高都为原来的二分之一，即图片为原来的四分之一
                bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(uri), null, options);
                File file = null;
                switch (from){
                    case REQUEST_GALLERY:
                        String filePath = FileUtils.getRealFilePath(this,uri);
                        File oldFile = new File(filePath);
                        // 修改文件名
                        String newFileName = UUID.randomUUID().toString().replace("-","")+".jpg";
                        String newFilePath = oldFile.getParent()+"/"+newFileName;
                        file = new File(newFilePath);
                        oldFile.renameTo(file);

                        break;
                    case REQUEST_CAMERA:
                        file = cameraFile;
                        break;
                }

                if(file == null || !file.exists()){
                    Log.i("异常：","文件不存在！");
                }
                list.remove(addImgButton); // 先删除
                if (list.size() < DEFAULT_NUM) {
                    ItemBean itemBean = new ItemBean(new ImageFileBean(file, bitmap, false), false);
                    list.add(itemBean);
                    if (list.size() < DEFAULT_NUM) {
                        // 如果图片数量还没有达到最大值，则将添加按钮添加到list后面
                        list.add(addImgButton);
                    }
                }

                adapter.notifyDataSetChanged();
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_PERMISSION_CODE == requestCode) {
            // TODO 添加权限请求后的逻辑
        }
    }

    /**
     * 单文件上传
     * @param view
     */
    public void singleUpload(View view) {

        for (ItemBean itemBean : list) {
            if (itemBean.isButton()) continue;
            uploadImage(itemBean.getImageFileBean());
            itemBean.getImageFileBean().setStartUpload(true);
            adapter.notifyDataSetChanged();
        }

    }

    /**
     * 多文件上传
     * @param view
     */
    public void multiUpload(View view){

        List<File> fileList = new ArrayList<>();
        for (ItemBean itemBean : list) {
            if (itemBean.isButton()) continue;
            fileList.add(itemBean.getImageFileBean().getFile());
            itemBean.getImageFileBean().setStartUpload(true);
            adapter.notifyDataSetChanged();
        }
        // 用上传的文件生成RequestBody
        if(fileList == null || fileList.size() == 0)return;

        //创建MultipartBody.Builder，用于添加请求的数据
        MultipartBody.Builder builder = new MultipartBody.Builder();
        for (int i = 0; i < fileList.size(); i++) { //对文件进行遍历
            //根据文件的后缀名，获得文件类型
            builder.setType(MultipartBody.FORM)
                    .addFormDataPart("name",fileList.get(i).getName())// 其他信息
                    .addFormDataPart("id","12,13,14")// 其他信息
                    .addFormDataPart("type","2"+i)// 其他信
                    .addFormDataPart( //给Builder添加上传的文件
                            "images",  //请求的名字
                            fileList.get(i).getName(), //文件的文字，服务器端用来解析的
                            RequestBody.Companion.create(fileList.get(i),MediaType.parse("multipart/form-data"))//创建RequestBody，把上传的文件放入
                    );
        }
        RequestBody requestBody = builder.build();//根据Builder创建请求
        Request request = new Request.Builder()
                .url(Global.MULTI_FILE_UPLOAD_URL)
                .post(requestBody)
                .addHeader("user-agent", "PDA")
                .addHeader("x-userid", "752332")// 添加x-userid请求头
                .addHeader("x-sessionkey", "kjhsfjkaskfashfuiwf")// 添加x-sessionkey请求头
                .addHeader("x-tonce", Long.valueOf(System.currentTimeMillis()).toString())// 添加x-tonce请求头
                .addHeader("x-timestamp", Long.valueOf(System.currentTimeMillis()).toString())// 添加x-timestamp请求头
                .build();

        final Message msg = myHandler.obtainMessage();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request)
                .enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                msg.obj = list;
                msg.what =0;
                myHandler.sendMessage(msg);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();
                Log.i("上传图片结果：", result);
                msg.obj = list;
                if (!response.isSuccessful()) {
                    Log.i("响应失败：", response.code() + "");
                    msg.what =1;
                    return;
                }
                msg.what = 3;
                myHandler.sendMessage(msg);

            }
        });
    }

    private static class MyHandler extends Handler {
        private Context mContext;
        private LoadImageAdapter loadImageAdapter;
        public MyHandler(Context context,LoadImageAdapter loadImageAdapter) {
            this.mContext = context;
            this.loadImageAdapter = loadImageAdapter;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            List<ItemBean> itemBeanList = null;
            ImageFileBean fileBean = null;
            if(msg.obj instanceof List){
                itemBeanList = (List<ItemBean>)msg.obj;
                for(ItemBean itemBean:itemBeanList){
                    if(itemBean.isButton())continue;
                    itemBean.getImageFileBean().setStartUpload(false);
                }
            }else if(msg.obj instanceof ImageFileBean){
                fileBean= (ImageFileBean)msg.obj;
                fileBean.setStartUpload(false);
            }

            switch (msg.what) {
                case 0:
                    Toast.makeText(mContext, "连接服务器失败", Toast.LENGTH_SHORT).show();
                    if(fileBean !=null) {
                        fileBean.setUpload(false);
                    }else if(itemBeanList != null){
                        for(ItemBean itemBean:itemBeanList){
                            if(itemBean.isButton())continue;
                            itemBean.getImageFileBean().setUpload(false);
                        }
                    }
                    break;

                case 2:
                    Toast.makeText(mContext,"获取服务端数据为空",Toast.LENGTH_SHORT).show();

                    break;

                case 3:
                    Toast.makeText(mContext, "上传成功！", Toast.LENGTH_SHORT).show();
                    if(fileBean !=null) {
                        fileBean.setUpload(true);
                    }else if(itemBeanList != null){
                        for(ItemBean itemBean:itemBeanList){
                            if(itemBean.isButton())continue;
                            itemBean.getImageFileBean().setUpload(true);
                        }
                    }

                    break;
            }
            loadImageAdapter.notifyDataSetChanged();

        }
    }

    private void uploadImage(final ImageFileBean fileBean) {
        File file = fileBean.getFile();
        if (file == null) return;
        if(!file.exists()){
            Toast.makeText(this, "文件不存在！", Toast.LENGTH_SHORT).show();
            return;
        }
        viewHolder.mTVText.setText("HashCode#"+SHA256.getSHA256(file));
        Log.i("文件HashCode:",SHA256.getSHA256(file));

        // 准备Body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name",file.getName())// 其他信息
                .addFormDataPart("id","12,13,14")// 其他信息
                .addFormDataPart("type","2")// 其他信
                .addFormDataPart("file", file.getName(),
                        RequestBody.Companion.create(file,MediaType.parse("multipart/form-data")))//文件
                .build();
        Request request = new Request.Builder()
                .url(Global.UPLOAD_URL).post(requestBody)
                .addHeader("user-agent", "PDA")
                .addHeader("x-userid", "752332")// 添加x-userid请求头
                .addHeader("x-sessionkey", "kjhsfjkaskfashfuiwf")// 添加x-sessionkey请求头
                .addHeader("x-tonce", Long.valueOf(System.currentTimeMillis()).toString())// 添加x-tonce请求头
                .addHeader("x-timestamp", Long.valueOf(System.currentTimeMillis()).toString())// 添加x-timestamp请求头
                .build();

        OkHttpClient okHttpClient = new OkHttpClient();
       final Message msg = myHandler.obtainMessage();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                msg.obj = fileBean;
                msg.what =0;
                myHandler.sendMessage(msg);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String result = response.body().string();
                Log.i("上传图片结果：", result);
                msg.obj = fileBean;
                if (!response.isSuccessful()) {
                    Log.i("响应失败：", response.code() + "");
                    msg.what =1;
                    return;
                }
                msg.what = 3;
                myHandler.sendMessage(msg);

            }
        });
    }

    static class ActivityViewHolder {
        RecyclerView mRV;
        TextView mTVText;

        public ActivityViewHolder(Window window) {
            mRV = window.findViewById(R.id.rv_img);
            mTVText = window.findViewById(R.id.tv_text);
        }
    }
}
