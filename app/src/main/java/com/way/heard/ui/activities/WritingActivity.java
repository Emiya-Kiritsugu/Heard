package com.way.heard.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.way.heard.R;
import com.way.heard.adapters.NineGridImageViewAdapter;
import com.way.heard.internal.GlidePauseOnScrollListener;
import com.way.heard.ui.views.NineGridImageView;
import com.way.heard.ui.views.TagCloudView;
import com.way.heard.utils.GlideImageLoader;
import com.way.heard.utils.GlideLocalImageLoader;
import com.way.heard.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

import cn.finalteam.galleryfinal.CoreConfig;
import cn.finalteam.galleryfinal.FunctionConfig;
import cn.finalteam.galleryfinal.GalleryFinal;
import cn.finalteam.galleryfinal.PauseOnScrollListener;
import cn.finalteam.galleryfinal.ThemeConfig;
import cn.finalteam.galleryfinal.model.PhotoInfo;
import cn.finalteam.galleryfinal.widget.GFImageView;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class WritingActivity extends ActionBarActivity {
    private final static String TAG = WritingActivity.class.getName();

    private final int REQUEST_CODE_CAMERA = 1000;
    private final int REQUEST_CODE_GALLERY = 1001;
    private final int REQUEST_CODE_CROP = 1002;
    private final int REQUEST_CODE_EDIT = 1003;

    private List<String> tagsData = null;
    private EditText etTitle;
    private TagCloudView tcvTags;
    private EditText etTags;
    private NineGridImageView ngivGallery;
    private List<PhotoInfo> mPhotoList;
    private NineGridImageViewAdapter<PhotoInfo> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        setToolBar();
        initData();
        initView();
    }


    private void setToolBar() {
        LogUtil.d(TAG, "setToolBar debug");
        Toolbar mToolbar = (Toolbar) findViewById(R.id.tb_writing_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //mToolbar.setNavigationIcon(R.drawable.btn_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initData() {
        LogUtil.d(TAG, "initData debug");
        tagsData = new ArrayList<>();
        mPhotoList = new ArrayList<>();
        String[] IMG_URL_LIST = {
                "http://img1.ph.126.net/Vs6djtwAm8UZzS2GWuF8Gg==/4944107966023526737.jpg",
                "http://img1.ph.126.net/Vs6djtwAm8UZzS2GWuF8Gg==/4944107966023526737.jpg",
                "http://img1.ph.126.net/Vs6djtwAm8UZzS2GWuF8Gg==/4944107966023526737.jpg",
                "http://img1.ph.126.net/Vs6djtwAm8UZzS2GWuF8Gg==/4944107966023526737.jpg",
                "http://img1.ph.126.net/Vs6djtwAm8UZzS2GWuF8Gg==/4944107966023526737.jpg",
                "http://img1.ph.126.net/Vs6djtwAm8UZzS2GWuF8Gg==/4944107966023526737.jpg",
                "http://img1.ph.126.net/Vs6djtwAm8UZzS2GWuF8Gg==/4944107966023526737.jpg",
                "http://img1.ph.126.net/Vs6djtwAm8UZzS2GWuF8Gg==/4944107966023526737.jpg",
                "http://img1.ph.126.net/Vs6djtwAm8UZzS2GWuF8Gg==/4944107966023526737.jpg",
        };
        //imgUrls.addAll(Arrays.asList(IMG_URL_LIST));
        mAdapter = new NineGridImageViewAdapter<PhotoInfo>() {
            @Override
            public void onDisplayImage(Context context, final GFImageView imageView, PhotoInfo photoInfo) {
                LogUtil.d(TAG, "initData debug, onDisplayImage, Image Url = " + photoInfo.getPhotoPath());
                GlideImageLoader.displayImageFromUrl(context, photoInfo.getPhotoPath(), imageView, R.drawable.ic_picture_default, R.drawable.ic_picture_default);
            }

            @Override
            public GFImageView generateImageView(Context context) {
                return super.generateImageView(context);
            }

            @Override
            public void onItemImageClick(Context context, int index, List<PhotoInfo> list) {
                if (index == 0 && TextUtils.isEmpty(list.get(index).getPhotoPath())) {
                    openGallery();
                }
                //showBigPicture(context, photoList.get(index).getBigUrl());
            }
        };
    }

    private void initView() {
        etTitle = (EditText) findViewById(R.id.et_writing_title);
        tcvTags = (TagCloudView) findViewById(R.id.tcv_writing_tags);
        etTags = (EditText) findViewById(R.id.et_writing_tags);
        ngivGallery = (NineGridImageView) findViewById(R.id.ngiv_writing_gallery);

        tcvTags.setVisibility(View.GONE);
        etTags.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    LogUtil.d(TAG, "initView debug, OnKeyListener, KEYCODE_ENTER");
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                        if (TextUtils.isEmpty(etTags.getText())) {
                            Toast.makeText(WritingActivity.this, "No Tag Input!", Toast.LENGTH_SHORT).show();
                        }
                        if (tagsData.size() > 4) {
                            Toast.makeText(WritingActivity.this, "Only Five Tags Allowed!", Toast.LENGTH_SHORT).show();
                        }
                        String tagStr = etTags.getText().toString();
                        tagsData.add(tagStr);
                        if (tcvTags.getVisibility() == View.GONE) {
                            tcvTags.setVisibility(View.VISIBLE);
                        }
                        tcvTags.setTags(tagsData);
                        etTags.setText("");
                    }
                    return true;
                }
                return false;
            }
        });

        if (tagsData != null && tagsData.size() > 0) {
            tcvTags.setVisibility(View.VISIBLE);
            tcvTags.setTags(tagsData);
        }

        tcvTags.setOnTagClickListener(new TagCloudView.OnTagClickListener() {
            @Override
            public void onTagClick(int position) {
                if (position == -1) {
                    Toast.makeText(WritingActivity.this, "Click End Text", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WritingActivity.this, "Click Position : " + position, Toast.LENGTH_SHORT).show();
                }
            }
        });
        tcvTags.setOnTagLongClickListener(new TagCloudView.OnTagLongClickListener() {
            @Override
            public boolean onTagLongClick(final int position) {
                if (position == -1) {
                    Toast.makeText(WritingActivity.this, "Long Click End Text", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    new SweetAlertDialog(WritingActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Are you sure?")
                            .setContentText("Won't be able to recover this tag!")
                            .setCancelText("No,cancel plx!")
                            .setConfirmText("Yes,delete it!")
                            .showCancelButton(true)
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    // reuse previous dialog instance, keep widget user state, reset them if you need
                                    sDialog.setTitleText("Cancelled!")
                                            .setContentText("Your tag is safe :)")
                                            .setConfirmText("OK")
                                            .showCancelButton(false)
                                            .setCancelClickListener(null)
                                            .setConfirmClickListener(null)
                                            .changeAlertType(SweetAlertDialog.ERROR_TYPE);

                                    // or you can new a SweetAlertDialog to show
                               /* sDialog.dismiss();
                                new SweetAlertDialog(SampleActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Cancelled!")
                                        .setContentText("Your imaginary file is safe :)")
                                        .setConfirmText("OK")
                                        .show();*/
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    //TODO delete the tag
                                    tagsData.remove(position);
                                    tcvTags.setTags(tagsData);
                                    if (tagsData.size() == 0) {
                                        tcvTags.setVisibility(View.GONE);
                                    }
                                    sDialog.setTitleText("Deleted!")
                                            .setContentText("Your tag has been deleted!")
                                            .setConfirmText("OK")
                                            .showCancelButton(false)
                                            .setCancelClickListener(null)
                                            .setConfirmClickListener(null)
                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                }
                            })
                            .show();
                    return true;
                }
                //return false;
            }
        });

        tcvTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(WritingActivity.this, "TagView onClick",
                        Toast.LENGTH_SHORT).show();
            }
        });

        ngivGallery.setAdapter(mAdapter);
        if (mPhotoList.size() == 0) {
            mPhotoList.add(new PhotoInfo());
            ngivGallery.setImagesData(mPhotoList);
        }
    }

    private void openGallery(){
        //公共配置都可以在application中配置，这里只是为了代码演示而写在此处
        ThemeConfig themeConfig = null;
        themeConfig = ThemeConfig.DEFAULT;

        FunctionConfig.Builder functionConfigBuilder = new FunctionConfig.Builder();
        cn.finalteam.galleryfinal.ImageLoader imageLoader;
        PauseOnScrollListener pauseOnScrollListener = null;

        imageLoader = new GlideLocalImageLoader();
        pauseOnScrollListener = new GlidePauseOnScrollListener(false, true);
        functionConfigBuilder.setMutiSelectMaxSize(9);
        functionConfigBuilder.setSelected(mPhotoList);//添加过滤集合
        final FunctionConfig functionConfig = functionConfigBuilder.build();


        CoreConfig coreConfig = new CoreConfig.Builder(WritingActivity.this, imageLoader, themeConfig)
                //.setDebug(BuildConfig.DEBUG)
                .setFunctionConfig(functionConfig)
                .setPauseOnScrollListener(pauseOnScrollListener)
                .setNoAnimcation(false)
                .build();
        GalleryFinal.init(coreConfig);
        GalleryFinal.openGalleryMuti(REQUEST_CODE_GALLERY, functionConfig, mOnHanlderResultCallback);
    }

    private GalleryFinal.OnHanlderResultCallback mOnHanlderResultCallback = new GalleryFinal.OnHanlderResultCallback() {
        @Override
        public void onHanlderSuccess(int reqeustCode, List<PhotoInfo> resultList) {
            if (resultList != null) {
                mPhotoList.addAll(resultList);
                //mChoosePhotoListAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onHanlderFailure(int requestCode, String errorMsg) {
            Toast.makeText(WritingActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_writing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_publish:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
