package com.way.heard.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.victor.loading.rotate.RotateLoading;
import com.way.heard.R;
import com.way.heard.models.Image;
import com.way.heard.services.DownLoadImageService;
import com.way.heard.services.LeanCloudBackgroundTask;
import com.way.heard.utils.GlideImageLoader;
import com.way.heard.utils.LogUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class ImageDisplayActivity extends BaseActivity {
    private final static String TAG = ImageDisplayActivity.class.getName();
    public final static String IMAGE_DETAIL = "ImageDetail";
    public final static String IMAGE_POST_INDEX = "ImagePostIndex";
    public static final int IMAGE_DISPLAY_REQUEST = 1004;

    private ImageView ivPhoto;
    private TextView tvSave;
    private TextView tvLike;
    private Intent mIntent;
    private Image photo;
    private RotateLoading loading;
    private List<String> likeObjectIDs;
    private boolean isLiked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_display);

        getParamData();
        initView();
    }

    private void getParamData() {
        mIntent = getIntent();
        Bundle bundle = mIntent.getExtras();
        photo = bundle.getParcelable(IMAGE_DETAIL);
        likeObjectIDs = photo.getLikes();
        if (likeObjectIDs == null) {
            likeObjectIDs = new ArrayList<>();
        }
        LogUtil.d(TAG, "getParamData debug, Image Url = " + photo.getUrl());
    }

    private void initView() {
        ivPhoto = (ImageView) findViewById(R.id.iv_image_photo);
        tvSave = (TextView) findViewById(R.id.tv_image_save);
        tvLike = (TextView) findViewById(R.id.tv_image_like);
        loading = (RotateLoading) findViewById(R.id.loading);

        if (likeObjectIDs.contains(AVUser.getCurrentUser().getObjectId())) {
            isLiked = true;
            Drawable drawable = getResources().getDrawable(R.drawable.ic_thumb_up_accent);
            // 这一步必须要做,否则不会显示.
            drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
            tvLike.setCompoundDrawables(drawable, null, null, null);
        } else {
            isLiked = false;
        }
        GlideImageLoader.displayImage(ImageDisplayActivity.this, photo.getUrl(), ivPhoto);
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //new SaveTask(ImageDisplayActivity.this).execute();
                //saveImageToStorage();
                saveImageToStorage2();
            }
        });
        tvLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LikeTask(ImageDisplayActivity.this).execute();
            }
        });

        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    class LikeTask extends LeanCloudBackgroundTask {

        protected LikeTask(Context ctx) {
            super(ctx);
        }

        @Override
        protected void onPre() {
            loading.start();
        }

        @Override
        protected void doInBack() throws AVException {
            if (isLiked) {//already like
                likeObjectIDs.remove(AVUser.getCurrentUser().getObjectId());
                photo.setLikes(likeObjectIDs);
                photo.setFetchWhenSave(true);
                photo.save();
                isLiked = false;
            } else {
                likeObjectIDs.add(AVUser.getCurrentUser().getObjectId());
                photo.setLikes(likeObjectIDs);
                photo.setFetchWhenSave(true);
                photo.save();
                isLiked = true;
            }
        }

        @Override
        protected void onPost(AVException e) {
            loading.stop();
            if (isLiked) {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_thumb_up_accent);
                // 这一步必须要做,否则不会显示.
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                tvLike.setCompoundDrawables(drawable, null, null, null);
            } else {
                Drawable drawable = getResources().getDrawable(R.drawable.ic_thumb_up_white);
                // 这一步必须要做,否则不会显示.
                drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                tvLike.setCompoundDrawables(drawable, null, null, null);
            }
            if (e != null) {
                Toast.makeText(ImageDisplayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //
            mIntent.putExtra(IMAGE_DETAIL, photo);
            setResult(RESULT_OK, mIntent);
        }

        @Override
        protected void onCancel() {
            loading.stop();
        }
    }

    private void saveImageToStorage() {
        try {
            String storageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
            LogUtil.d(TAG, "saveImageToStorage debug, Storage Directory : " + storageDirectory);

            String fileDirectory = storageDirectory + "/" + getPackageName();
            LogUtil.d(TAG, "saveImageToStorage debug, File Directory : " + fileDirectory);

            String fileFullname = getFileFullName(photo.getUrl()) + ".jpg";
            LogUtil.d(TAG, "saveImageToStorage debug, File Full Name : " + fileFullname);

            File dir = new File(fileDirectory);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            OkHttpUtils//
                    .get()//
                    .url(photo.getUrl())//
                    .build()//
                    .execute(new FileCallBack(fileDirectory, fileFullname)//
                    {

                        @Override
                        public void inProgress(float progress, long total, int id) {
                            super.inProgress(progress, total, id);
                        }

                        @Override
                        public void onError(Call call, Exception e, int id) {
                            if (e != null) {
                                Toast.makeText(ImageDisplayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                LogUtil.e(TAG, "saveImageToStorage error", e);
                            }
                        }

                        @Override
                        public void onResponse(File response, int id) {
                            Toast.makeText(ImageDisplayActivity.this, response.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                            LogUtil.d(TAG, "saveImageToStorage debug, File Full Path : " + response.getAbsolutePath());
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(ImageDisplayActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            LogUtil.e(TAG, "saveImageToStorage error", e);
        }
    }

    private void saveImageToStorage2() {
        Intent startIntent = new Intent(ImageDisplayActivity.this, DownLoadImageService.class);
        Bundle bundle = new Bundle();
        bundle.putString("FILE_URL", photo.getUrl());
        bundle.putString("FILE_NAME", getFileFullName(photo.getUrl()) + ".jpg");
        //startIntent.setClass(MainActivity.this, DownLoadAPKService.class);
        startIntent.putExtras(bundle);
        startService(startIntent);
    }

    public String getFileName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }
    }

    public String getFileFullName(String pathandname) {

        int start = pathandname.lastIndexOf("/");
        if (start != -1) {
            return pathandname.substring(start + 1, pathandname.length() - 1);
        } else {
            return null;
        }
    }

    public static void go(Activity context, Image image, int position) {
        Intent intent = new Intent(context, ImageDisplayActivity.class);
        intent.putExtra(IMAGE_POST_INDEX, position);
        intent.putExtra(IMAGE_DETAIL, image);
        context.startActivityForResult(intent, IMAGE_DISPLAY_REQUEST);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
