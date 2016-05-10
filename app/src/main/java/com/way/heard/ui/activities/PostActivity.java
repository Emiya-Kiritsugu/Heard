package com.way.heard.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVRelation;
import com.avos.avoscloud.AVUser;
import com.victor.loading.rotate.RotateLoading;
import com.way.heard.R;
import com.way.heard.models.Image;
import com.way.heard.models.Post;
import com.way.heard.utils.LeanCloudBackgroundTask;
import com.way.heard.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PostActivity extends AppCompatActivity {
    private final static String TAG = PostActivity.class.getName();

    private static final int IMAGE_PICK_REQUEST = 1002;
    private static final int TAG_SEARCH_REQUEST = 1003;
    private static final int LOCATION_SEARCH_REQUEST = 1004;

    private ImageView ivPhoto;
    private EditText etContent;
    private CheckBox cbPrivate;
    private TextView tvTag;
    private TextView tvLocation;
    private RotateLoading loading;

    private static Bitmap bitmap;
    private static boolean hasTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        setToolBar();
        initView();
    }

    private void setToolBar() {
        LogUtil.d(TAG, "setToolBar debug");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //mToolbar.setNavigationIcon(R.drawable.btn_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initView() {
        ivPhoto = (ImageView) findViewById(R.id.iv_post2_photo);
        etContent = (EditText) findViewById(R.id.et_post2_content);
        cbPrivate = (CheckBox) findViewById(R.id.cb_post2_private);
        tvTag = (TextView) findViewById(R.id.tv_post2_tag);
        tvLocation = (TextView) findViewById(R.id.tv_post2_location);
        loading = (RotateLoading) findViewById(R.id.loading);

        ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(PostActivity.this, IMAGE_PICK_REQUEST);
            }
        });
        tvTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchTagActivity.go(PostActivity.this, SearchTagActivity.SEARCH_TYPE_TAG, PostActivity.TAG_SEARCH_REQUEST);
            }
        });
        tvLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchTagActivity.go(PostActivity.this, SearchTagActivity.SEARCH_TYPE_LOCATION, PostActivity.LOCATION_SEARCH_REQUEST);
            }
        });
    }

    public static void pickImage(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMAGE_PICK_REQUEST) {
                Uri uri = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    ivPhoto.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == TAG_SEARCH_REQUEST) {
                String tag = data.getStringExtra(SearchTagActivity.SEARCH_RESULT);
                tvTag.setText(tag);
                hasTag = true;
                LogUtil.d(TAG, "onActivityResult debug, SEARCH_TYPE = " + tag);
            } else if (requestCode == LOCATION_SEARCH_REQUEST) {

            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_publish:
                publishPost();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void publishPost() {
        if (TextUtils.isEmpty(etContent.getText())) {
            Toast.makeText(PostActivity.this, "No Post Content!", Toast.LENGTH_SHORT).show();
            return;
        }
        new PostTask(PostActivity.this).execute();
    }

    private void turnBack() {
        setResult(RESULT_OK);
        finish();
    }

    class PostTask extends LeanCloudBackgroundTask {

        protected PostTask(Context ctx) {
            super(ctx);
        }

        @Override
        protected void onPre() {
            loading.start();
        }

        @Override
        protected void doInBack() throws AVException {
            AVUser currentUser = AVUser.getCurrentUser();

            String url = "";
            String thumbnailurl = "";
            boolean isFileSaved = false;
            if (bitmap != null) {
                //save file
                LogUtil.d(TAG, "PostTask debug, doInBack, Save File");
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                byte[] data = out.toByteArray();
                AVFile avfile = new AVFile(currentUser.getUsername() + "-photo.jpg", data);
                avfile.save();
                url = avfile.getUrl();
                thumbnailurl = avfile.getThumbnailUrl(true, 100, 100);

                isFileSaved = true;
            }

            boolean isImageSaved = false;
            Image img = new Image();
            if (isFileSaved) {
                LogUtil.d(TAG, "PostTask debug, doInBack, Save Image");
                img.setAuthor(currentUser);
                img.setThumbnailurl(thumbnailurl);
                img.setUrl(url);
                if (cbPrivate.isChecked()) {
                    img.setType(0);//private
                } else {
                    img.setType(1);//private
                }
                img.setFrom(1);//from post
                img.save();

                isImageSaved = true;
            }

            LogUtil.d(TAG, "PostTask debug, doInBack, Save Post");
            Post post = new Post();
            post.setAuthor(currentUser);
            if (!TextUtils.isEmpty(etContent.getText())) {
                post.setContent(etContent.getText().toString());
            }
            if (hasTag) {
                post.setTag(tvTag.getText().toString());
            }
            if (cbPrivate.isChecked()) {
                post.setType(0);//private
            } else {
                post.setType(1);//private
            }
            if (isImageSaved) {
                AVRelation<Image> photos = post.getPhotos();
                photos.add(img);
            }
            post.save();
        }

        @Override
        protected void onPost(AVException e) {
            loading.stop();
            if (e == null) {
                Toast.makeText(PostActivity.this, "Done", Toast.LENGTH_SHORT).show();
                turnBack();
            } else {
                Toast.makeText(PostActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
