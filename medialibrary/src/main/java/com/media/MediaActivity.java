package com.media;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.media.image.ImageModel;
import com.media.image.ImageUtils;
import com.media.image.ImageWork;
import com.media.utils.BitmapUtils;
import com.media.utils.FileSizeUtil;
import com.media.utils.MediaFileUtils;
import com.media.utils.LoadingManager;
import com.media.utils.MediaCommonUtil;
import com.media.utils.ToastUtils;
import com.vincent.videocompressor.VideoCompress;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import co.senab.photoview.PhotoView;
import co.senab.photoview.PhotoViewAttacher;

import static android.animation.ObjectAnimator.ofPropertyValuesHolder;

public class MediaActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private static final String TAG = "MediaActivity";
    private static final int RESULT_CAMERA = 100;

    private TextView title;
    private TextView confirm;
    private View back;
    private TextView mediaType;
    private GridView mGridView;
    private ListView mediaListView;
    private View imageContainer;
    private TextView preview;
    private View previewContainer;
    private View viewContainer;
    private ImageView picBack;
    private TextView page_count;
    private View selecLayout;
    private ImageView selecCheck;
    private RecyclerView recyclerView;

    private LinearLayoutManager linearLayoutManager;
    private SelecImageListAdapter selecAdapter;
    private ImagrListAdapter typeAdapter;

    private Adapter imageAdapter;
    private ImageWork mImageWork;//图片加载类
    private ArrayList<ImageModel> AllImageList = new ArrayList<>();//所有相册图片
    private ArrayList<ImageModel> typeImageList = new ArrayList<>();//相册图片列表
    private ArrayList<ImageModel> itemImageList = new ArrayList<>();//相册图片
    private ArrayList<ImageModel> selecImageList = new ArrayList<>();//选中图片
    private ArrayList<ImageModel> VideoImageList = new ArrayList<>();//相册视频
    private ArrayList<ImageModel> sendList = new ArrayList<>();
    private int max = 9;
    private int count = 0;
    private int number = 9;
    private String type = ImageModel.TYPE_IMAGE;
    private String fileType = ImageModel.TYPE_IMAGE;

    private File videoThumbnail;

    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_media);

        type = getIntent().getStringExtra("type");
        number = getIntent().getIntExtra("max", 9);

        initView();

        title.setText("图片和视频");
        confirm.setText("发送");


        mImageWork = new ImageWork(this);

        if (type.equals(ImageModel.TYPE_IMAGE)) {
            title.setText("图片");
            AllImageList = ImageUtils.getImages(this);
            if (AllImageList.size() > 0) {
                for (ImageModel imageModel : AllImageList) {
                    itemImageList.add(imageModel);
                }
            }
        } else if (type.equals(ImageModel.TYPE_VIDEO)) {
            title.setText("视频");
            number = 1;
            mediaType.setVisibility(View.GONE);
            VideoImageList = ImageUtils.getVideos(this);
            if (VideoImageList.size() > 0) {
                for (ImageModel imageModel : VideoImageList) {
                    itemImageList.add(imageModel);
                }
            }
        } else if (type.equals(ImageModel.TYPE_ALL)) {
            title.setText("图片和视频");
//            type = ImageModel.TYPE_IMAGE;
            AllImageList = ImageUtils.getImages(this);
            VideoImageList = ImageUtils.getVideos(this);
            if (AllImageList.size() > 0) {
                for (ImageModel imageModel : AllImageList) {
                    itemImageList.add(imageModel);
                }
            }
        }

        if (VideoImageList.size() > 0) {
            videoThumbnail = MediaFileUtils.createVideoThumbnailFile(MediaActivity.this, new File(VideoImageList.get(0).path));
        }

        if (number == 1 && type.equals(ImageModel.TYPE_IMAGE)) {
            AllImageList.add(0, new ImageModel("", "", "", ImageModel.TYPE_CAMERA));
        }

        getTypeImageDate();
        if (type.equals(ImageModel.TYPE_ALL)) {
            type = ImageModel.TYPE_IMAGE;
        }
        max = number;

        selecImageList = new ArrayList<>();
        imageAdapter = new Adapter();
        mGridView.setAdapter(imageAdapter);

        listenerContainer();
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    }

    private void initView() {
        title = findViewById(R.id.title);
        confirm = findViewById(R.id.menu);
        back = findViewById(R.id.back);
        mediaType = findViewById(R.id.mediaType);
        mGridView = findViewById(R.id.gv_main);
        mediaListView = findViewById(R.id.mediaListView);
        imageContainer = findViewById(R.id.imageContainer);
        preview = findViewById(R.id.preview);
        previewContainer = findViewById(R.id.previewContainer);
        viewContainer = findViewById(R.id.viewContainer);
        picBack = findViewById(R.id.picBack);
        page_count = findViewById(R.id.page_count);
        selecLayout = findViewById(R.id.selecLayout);
        selecCheck = findViewById(R.id.selecCheck);
        recyclerView = findViewById(R.id.recyclerView);
        mediaType.setOnClickListener(this);
        imageContainer.setOnClickListener(this);
        preview.setOnClickListener(this);
        selecLayout.setOnClickListener(this);
        picBack.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    private View imageSelec;
    private View videoSelec;

    private void initImageTypeListView() {
        View headView = LayoutInflater.from(getApplication()).inflate(R.layout.media_item_image_list_header, null);
        View image = headView.findViewById(R.id.image);
        View video = headView.findViewById(R.id.video);
        ImageView imageIcon = headView.findViewById(R.id.image_icon);
        ImageView videoIcon = headView.findViewById(R.id.video_icon);
        TextView image_num = headView.findViewById(R.id.image_num);
        TextView videoNum = headView.findViewById(R.id.video_num);
        imageSelec = headView.findViewById(R.id.image_selec);
        videoSelec = headView.findViewById(R.id.video_selec);
        if (AllImageList.size() > 0) {
            if (number > 1) {
                MediaGlideLoader.LoderLoadImageType(getApplication(), AllImageList.get(0).path, imageIcon);
            } else {
                if (AllImageList.size() > 1) {
                    MediaGlideLoader.LoderLoadImageType(getApplication(), AllImageList.get(1).path, imageIcon);
                }
            }
        }
        if (VideoImageList.size() > 0) {
            MediaGlideLoader.LoderLoadImageType(getApplication(), videoThumbnail.getAbsolutePath(), videoIcon);
        }
        Log.i(TAG, "initImageTypeListView: "+type);
        if (type.equals(ImageModel.TYPE_IMAGE)) {
            video.setVisibility(View.GONE);
        }
        image_num.setText(AllImageList.size() + "张");
        videoNum.setText(VideoImageList.size() + "张");
        initSelecView();
        int mHeigth = MediaCommonUtil.getScreenHeight(getApplication()) * 3 / 4;
        mediaListView.getLayoutParams().height = mHeigth;
        mediaListView.addHeaderView(headView);
        typeAdapter = new ImagrListAdapter(typeImageList);
        mediaListView.setAdapter(typeAdapter);
        mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                removeImageTypeView();
                if (type.equals(ImageModel.TYPE_VIDEO)) {
                    for (ImageModel imageModel : selecImageList) {
                        if (imageModel.isChecked) {
                            imageModel.isChecked = false;
                        }
                    }
                    selecImageList.clear();
                    confirm.setText("发送");
                    preview.setText("预览");
                }
                max = number;
                type = ImageModel.TYPE_IMAGE;
                fileType = typeImageList.get(position - 1).fileName;
                itemImageList.clear();
                if (AllImageList.size() > 0) {
                    for (ImageModel imageModel : AllImageList) {
                        if (imageModel.fileName.equals(fileType)) {
                            itemImageList.add(imageModel);
                        }
                    }
                }
                mediaType.setText(fileType);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initSelecView();
                        typeAdapter.notifyDataSetChanged();
                        imageAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeImageTypeView();
                if (type.equals(ImageModel.TYPE_VIDEO)) {
                    for (ImageModel imageModel : selecImageList) {
                        if (imageModel.isChecked) {
                            imageModel.isChecked = false;
                        }
                    }
                    selecImageList.clear();
                    confirm.setText("发送");
                    preview.setText("预览");
                }
                max = number;
                type = ImageModel.TYPE_IMAGE;
                fileType = ImageModel.TYPE_IMAGE;
                itemImageList.clear();
                if (AllImageList.size() > 0) {
                    for (ImageModel imageModel : AllImageList) {
                        itemImageList.add(imageModel);
                    }
                }
                mediaType.setText("所有图片");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initSelecView();
                        typeAdapter.notifyDataSetChanged();
                        imageAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeImageTypeView();
                if (type.equals(ImageModel.TYPE_IMAGE)) {
                    for (ImageModel imageModel : selecImageList) {
                        if (imageModel.isChecked) {
                            imageModel.isChecked = false;
                        }
                    }
                    selecImageList.clear();
                    confirm.setText("发送");
                    preview.setText("预览");
                }
                max = 1;
                type = ImageModel.TYPE_VIDEO;
                fileType = ImageModel.TYPE_VIDEO;
                itemImageList.clear();
                if (VideoImageList.size() > 0) {
                    for (ImageModel imageModel : VideoImageList) {
                        itemImageList.add(imageModel);
                    }
                }
                mediaType.setText("所有视频");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        initSelecView();
                        typeAdapter.notifyDataSetChanged();
                        imageAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void initSelecView() {
        if (fileType.equals(ImageModel.TYPE_IMAGE)) {
            imageSelec.setVisibility(View.VISIBLE);
            videoSelec.setVisibility(View.GONE);
        } else if (fileType.equals(ImageModel.TYPE_VIDEO)) {
            imageSelec.setVisibility(View.GONE);
            videoSelec.setVisibility(View.VISIBLE);
        } else {
            imageSelec.setVisibility(View.GONE);
            videoSelec.setVisibility(View.GONE);
        }
    }

    private void listenerContainer() {
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    if (!ImageUtils.hasHoneycomb()) {
                        mImageWork.setPauseWork(true);
                    }
                } else {
                    mImageWork.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
            }
        });
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                count = selecImageList.size();
                double ImgSize = FileSizeUtil.getFileOrFilesSize(itemImageList.get(i).path, FileSizeUtil.SIZETYPE_MB);

                if (type.equals(ImageModel.TYPE_IMAGE) && ImgSize <= 0) {
                    ToastUtils.showShort(getApplication(), "图片已损坏");
                    return;
                }

                if (type.equals(ImageModel.TYPE_VIDEO)) {
                    if (ImgSize <= 0) {
                        ToastUtils.showShort(getApplication(), "视频已损坏");
                        return;
                    } else if (itemImageList.get(i).duration > 180 * 1000) {
                        ToastUtils.showShort(getApplication(), "视频时长不能大于180s");
                        return;
                    }
                }

                if (itemImageList.get(i).type.equals(ImageModel.TYPE_CAMERA)) {
                    openCamera();
                    return;
                }

                //单选图片
                if (number == 1 && type.equals(ImageModel.TYPE_IMAGE)) {
                    saveImage(itemImageList.get(i));
                    sendMedias();
                    return;
                }

                //多选图片
                if (count >= max && !itemImageList.get(i).getIsChecked()) {
                    ToastUtils.showShort(getApplication(), "你最多只能选择" + max + "个" + (type.equals(ImageModel.TYPE_IMAGE) ? "图片" : "视频"));
                } else if (count >= max && itemImageList.get(i).getIsChecked()) {
                    count = count - 1;
                    itemImageList.get(i).setIsChecked(false);
                    selecImageList.remove(itemImageList.get(i));
                } else if (count < max && !itemImageList.get(i).getIsChecked()) {
                    count = count + 1;
                    itemImageList.get(i).setIsChecked(true);
                    selecImageList.add(itemImageList.get(i));
                } else if (count < max && itemImageList.get(i).getIsChecked()) {
                    count = count - 1;
                    itemImageList.get(i).setIsChecked(false);
                    selecImageList.remove(itemImageList.get(i));
                }
                if (count > 0) {
                    confirm.setText("发送(" + count + "/" + max + ")");
                    preview.setText("预览(" + count + ")");
                } else {
                    confirm.setText("发送");
                    preview.setText("预览");
                }
                imageAdapter.notifyDataSetChanged();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                if (selecImageList.size() > 0) {
                    if (type.equals(ImageModel.TYPE_IMAGE)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                LoadingManager.showLoadingDialog(MediaActivity.this);
                                for (ImageModel imageModel : selecImageList) {
                                    saveImage(imageModel);
                                }
                                LoadingManager.hideLoadingDialog(MediaActivity.this);
                                sendMedias();
                            }
                        }).start();
                    } else if (type.equals(ImageModel.TYPE_VIDEO)) {
                        for (final ImageModel imageModel : selecImageList) {
                            final File thumbnailFile = MediaFileUtils.createVideoThumbnailFile(MediaActivity.this, new File(imageModel.getPath()));
                            final String destPath = MediaFileUtils.createAttachmentFile(MediaActivity.this, "video_" + System.currentTimeMillis() + ".mp4").getPath();
                            final VideoCompress.VideoCompressTask task = VideoCompress.compressVideoLow(imageModel.getPath(), destPath, new VideoCompress.CompressListener() {
                                @Override
                                public void onStart() {
                                    LoadingManager.showProgress(MediaActivity.this, String.format(getResources().getString(R.string.str_compressor_wait), "0.00%"));
                                }

                                @Override
                                public void onSuccess() {
                                    LoadingManager.hideProgress(MediaActivity.this);
                                    imageModel.setPath(destPath);
                                    imageModel.setThumb(thumbnailFile.getPath());
                                    sendList.add(imageModel);
                                    sendMedias();
                                }

                                @Override
                                public void onFail() {
                                    LoadingManager.hideProgress(MediaActivity.this);
                                }

                                @Override
                                public void onProgress(float percent) {
                                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                                    String strPercent = decimalFormat.format(percent);
                                    LoadingManager.updateProgress(MediaActivity.this, String.format(getResources().getString(R.string.str_compressor_wait), strPercent + "%"));
                                }
                            });
                            LoadingManager.OnDismissListener(MediaActivity.this, new LoadingManager.OnDismissListener() {
                                @Override
                                public void onDismiss() {
                                    task.cancel(true);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void sendMedias() {
        try {
            JSONObject jsonObject = new JSONObject();
            JSONArray imgsArray = new JSONArray();
            JSONArray thumbnailsArray = new JSONArray();
            if (sendList.size() > 0) {
                if (type.equals(ImageModel.TYPE_IMAGE)) {
                    for (int i = 0; i < sendList.size(); i++) {
                        imgsArray.put(sendList.get(i).getPath());
                        thumbnailsArray.put(sendList.get(i).getThumb());
                    }
                    jsonObject.put("imgsList", imgsArray);
                    jsonObject.put("thumbnailsList", thumbnailsArray);
                } else if (type.equals(ImageModel.TYPE_VIDEO)) {
                    jsonObject.put("video", sendList.get(0).getPath());
                    jsonObject.put("cover", sendList.get(0).getThumb());
                    jsonObject.put("duration", sendList.get(0).getDuration());
                }
            }
            Intent intent = new Intent();
            intent.putExtra("resultJson", jsonObject.toString());
            setResult(RESULT_OK, intent);
            finish();
        } catch (JSONException e) {
            e.getMessage();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public Object getTypeImageDate() {
        typeImageList = ImageUtils.getTypeImageslist(this);
        initImageTypeListView();
        return typeImageList;
    }

    // 保存图片
    private void saveImage(ImageModel imageModel) {
        String picPath = imageModel.getPath();
        if (!MediaCommonUtil.isBlank(picPath)) {
            try {
                int angle = BitmapUtils.readPictureDegree(picPath);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(picPath, options);
                options.inSampleSize = BitmapUtils.calculateInSampleSize(options, 1080, 1920);
                options.inJustDecodeBounds = false;

                Bitmap bitmap = BitmapFactory.decodeFile(picPath, options);

                // 修复图片被旋转的角度
                bitmap = BitmapUtils.rotaingImageView(angle, bitmap);

                String fileName = System.currentTimeMillis() + "_image.jpg";
                File compressFile = MediaFileUtils.createAttachmentFile(MediaActivity.this, fileName);
                MediaFileUtils.compressBmpToFile(bitmap, compressFile, 500);

                if (compressFile.exists()) {
                    imageModel.setPath(compressFile.getPath());
                    imageModel.setThumb(compressFile.getPath());
                    sendList.add(imageModel);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Adapter extends BaseAdapter {

        @Override
        public int getCount() {
            return itemImageList.size();
        }

        @Override
        public Object getItem(int i) {
            return itemImageList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            ImageModel imageModel = (ImageModel) getItem(i);
            String path = imageModel.getPath();
            if (view == null) {
                viewHolder = new ViewHolder();
                view = LayoutInflater.from(getApplication()).inflate(R.layout.media_view_media_imageview, null);
                viewHolder.imageView = (ImageView) view.findViewById(R.id.iv_imageView);
                viewHolder.iv_select = (ImageView) view.findViewById(R.id.iv_select);
                viewHolder.view_select_bg = view.findViewById(R.id.view_select_bg);
                viewHolder.bottom = view.findViewById(R.id.bottom);
                viewHolder.videoTime = (TextView) view.findViewById(R.id.videoTime);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            if (number > 1) {
                if (imageModel.getIsChecked()) {
                    viewHolder.iv_select.setImageResource(R.drawable.media_ic_square_select_green);
                } else {
                    viewHolder.iv_select.setImageResource(R.drawable.media_ic_square_normal_white);
                }
            } else if (number == 1 && type.equals(ImageModel.TYPE_IMAGE)) {
                viewHolder.iv_select.setVisibility(View.GONE);
            } else {
                if (imageModel.getIsChecked()) {
                    viewHolder.iv_select.setImageResource(R.drawable.media_ic_square_select_green);
                } else {
                    viewHolder.iv_select.setImageResource(R.drawable.media_ic_square_normal_white);
                }
            }

            if (imageModel.getType().equals(ImageModel.TYPE_IMAGE)
                    || imageModel.getType().equals(ImageModel.TYPE_CAMERA)) {
                viewHolder.bottom.setVisibility(View.GONE);
            } else if (imageModel.getType().equals(ImageModel.TYPE_VIDEO)) {
                viewHolder.bottom.setVisibility(View.VISIBLE);
                viewHolder.videoTime.setText(stringForTime(imageModel.duration));
            }

            if (imageModel.type.equals(ImageModel.TYPE_CAMERA)) {
                MediaGlideLoader.LoderDrawable(MediaActivity.this, R.mipmap.media_ic_camera, viewHolder.imageView, 0);
            } else {
                mImageWork.loadImage(imageModel.getType(), path, viewHolder.imageView);
            }
            return view;
        }

        class ViewHolder {
            ImageView imageView;
            ImageView iv_select;
            View view_select_bg;
            View bottom;
            TextView videoTime;
        }

        private String stringForTime(int timeMs) {
            int totalSeconds = timeMs / 1000;
            int seconds = totalSeconds % 60;
            int minutes = (totalSeconds / 60) % 60;
            int hours = totalSeconds / 3600;

            mFormatBuilder.setLength(0);
            if (hours > 0) {
                return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
            } else {
                return mFormatter.format("%01d:%02d", minutes, seconds).toString();
            }
        }

    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.mediaType) {
            if (imageContainer.isShown()) {
                removeImageTypeView();
            } else {
                startImageTypeView();
            }

        } else if (i == R.id.imageContainer) {
            removeImageTypeView();

        } else if (i == R.id.preview) {
            if (selecImageList.size() > 0) {
                startPreviewView();
            }

        } else if (i == R.id.selecLayout) {
            if (selecImageList.get(current).isChecked) {
                selecImageList.get(current).isChecked = false;
                selecCheck.setImageResource(R.drawable.media_ic_square_normal_gray);
            } else {
                selecCheck.setImageResource(R.drawable.media_ic_square_select_green);
                selecImageList.get(current).isChecked = true;
            }
            selecAdapter.notifyItemChanged(current);

        } else if (i == R.id.picBack) {
            removePreviewView();

        } else if (i == R.id.back) {
            onBackPressed();

        }
    }

    private void startPreviewView() {
        previewContainer.setVisibility(View.VISIBLE);
        viewContainer.setVisibility(View.VISIBLE);
        initImageGallery(current);

    }

    private PhotoViewPager imagePager = null;
    private GalleryPageAdapter pageAdapter = null;
    int current = 0;

    private void initImageGallery(int pageIndex) {
        if (pageIndex >= selecImageList.size()) {
            pageIndex = 0;
        }
        this.selecImageList.get(pageIndex).isCurrent = true;
        this.page_count.setText((current + 1) + "/" + selecImageList.size());
        this.pageAdapter = new GalleryPageAdapter();
        this.pageAdapter.setDatas(selecImageList);
        this.imagePager = (PhotoViewPager) findViewById(R.id.viewpager);
        this.imagePager.setOnPageChangeListener(this);
        this.imagePager.setAdapter(pageAdapter);
        this.imagePager.setCurrentItem(pageIndex);
        this.imagePager.setOffscreenPageLimit(0);

        this.selecAdapter = new SelecImageListAdapter();
        this.linearLayoutManager = new LinearLayoutManager(getApplication());
        this.linearLayoutManager.setOrientation(OrientationHelper.HORIZONTAL);
        this.recyclerView.setLayoutManager(linearLayoutManager);
        this.recyclerView.setAdapter(selecAdapter);
        this.selecAdapter.refreshData(selecImageList);

    }

    public class SelecImageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<ImageModel> datas = new ArrayList<>();

        public void refreshData(List<ImageModel> datas) {
            if (null != datas) {
                this.datas = datas;
                notifyDataSetChanged();
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            View v = getLayoutInflater().inflate(R.layout.media_view_image_item, null);
            viewHolder = new ItemHolder(v);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ItemHolder itemHolder = (ItemHolder) holder;
            ImageModel imageModel = datas.get(position);
            String path = imageModel.path;
            if (imageModel.getType().equals(ImageModel.TYPE_IMAGE)) {
                MediaGlideLoader.LoderMedia(getApplication(), path, itemHolder.image);
            } else if (imageModel.getType().equals(ImageModel.TYPE_VIDEO)) {
                mImageWork.loadImage(imageModel.getType(), path, itemHolder.image);
            }
            if (imageModel.isCurrent) {
                itemHolder.bg_image.setVisibility(View.VISIBLE);
            } else {
                itemHolder.bg_image.setVisibility(View.GONE);
            }
            if (imageModel.isChecked) {
                itemHolder.bg_normal.setVisibility(View.GONE);
            } else {
                itemHolder.bg_normal.setVisibility(View.VISIBLE);
            }

        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        private class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            ImageView image;
            ImageView bg_image;
            ImageView bg_normal;

            public ItemHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.image);
                bg_image = itemView.findViewById(R.id.bg_image);
                bg_normal = itemView.findViewById(R.id.bg_normal);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                imagePager.setCurrentItem(getAdapterPosition());
            }
        }
    }

    private class GalleryPageAdapter extends PagerAdapter {

        private List<ImageModel> datas = new ArrayList<>();

        public GalleryPageAdapter() {
        }

        public void setDatas(List<ImageModel> datas) {
            this.datas.clear();
            this.datas.addAll(datas);
            notifyDataSetChanged();
        }

        public ImageModel getImgDescr(int position) {
            return this.datas.get(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int LayoutId = R.layout.media_view_page_gallery_item;
            View view = LayoutInflater.from(container.getContext()).inflate(LayoutId, container, false);
            PhotoView photoView = view.findViewById(R.id.img);
            ImageView video = view.findViewById(R.id.bg_video);
            final ImageModel imageModel = datas.get(position);
            photoView.setLayoutParams(new RelativeLayout.LayoutParams(-1, -1));
            container.addView(view, -1, -1);
            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    if (viewContainer.isShown()) {
                        viewContainer.setVisibility(View.GONE);
                    } else {
                        viewContainer.setVisibility(View.VISIBLE);
                    }
                }
            });
            if (imageModel.getType().equals(ImageModel.TYPE_IMAGE)) {
                video.setVisibility(View.GONE);
                MediaGlideLoader.LoderGalleryImage(getApplication(), imageModel.getPath(), photoView);
            } else if (imageModel.getType().equals(ImageModel.TYPE_VIDEO)) {
                video.setVisibility(View.VISIBLE);
                mImageWork.loadImage(imageModel.getType(), imageModel.getPath(), photoView);
            }
            video.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaFileUtils.openFile(MediaActivity.this, new File(imageModel.getPath()));
                }
            });

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return this.datas.size();
        }

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        current = position;
        page_count.setText((current + 1) + "/" + selecImageList.size());
        for (ImageModel imageModel : selecImageList) {
            if (imageModel.isCurrent) {
                imageModel.isCurrent = false;
            }
        }
        if (selecImageList.get(position).isChecked) {
            selecCheck.setImageResource(R.drawable.media_ic_square_select_green);
        } else {
            selecCheck.setImageResource(R.drawable.media_ic_square_normal_gray);
        }
        selecImageList.get(position).isCurrent = true;
        selecAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(position);

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void removePreviewView() {
        previewContainer.setVisibility(View.GONE);
        viewContainer.setVisibility(View.GONE);
//        float y = CommonUtil.getScreenHeight(getApplication()) - mediaType.getY();
//        PropertyValuesHolder tlY = PropertyValuesHolder.ofFloat("translationY", 0.0f, y);
//        PropertyValuesHolder tlX = PropertyValuesHolder.ofFloat("translationX", 0.0f, 0.0f);
//        ObjectAnimator anim = ofPropertyValuesHolder(previewContainer, tlX, tlY);
//        anim.setDuration(300).start();
//        anim.start();
//        anim.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                previewContainer.setVisibility(View.GONE);
//                viewContainer.setVisibility(View.GONE);
//            }
//        });
        imageAdapter.notifyDataSetChanged();
        //删除多个元素
        Iterator<ImageModel> it = selecImageList.iterator();
        while (it.hasNext()) {
            ImageModel imageModel = it.next();
            if (!imageModel.isChecked) {
                it.remove();
            }
        }
        if (selecImageList.size() > 0) {
            if (current > selecImageList.size()) {
                current = 0;
            }
            confirm.setText("发送(" + selecImageList.size() + "/" + max + ")");
            preview.setText("预览(" + selecImageList.size() + ")");
            selecCheck.setImageResource(R.drawable.media_ic_square_select_green);
        } else {
            confirm.setText("发送");
            preview.setText("预览");
        }

    }

    private void startImageTypeView() {
        float y = MediaCommonUtil.getScreenHeight(getApplication()) - mediaType.getY();
        imageContainer.setVisibility(View.VISIBLE);
        PropertyValuesHolder tlY = PropertyValuesHolder.ofFloat("translationY", y, 0.0f);
        PropertyValuesHolder tlX = PropertyValuesHolder.ofFloat("translationX", 0.0f, 0.0f);
        ObjectAnimator anim = ofPropertyValuesHolder(imageContainer, tlX, tlY);
        anim.setDuration(300).start();
        anim.start();
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

    }

    private void removeImageTypeView() {
        float y = MediaCommonUtil.getScreenHeight(getApplication()) - mediaType.getY();
        PropertyValuesHolder tlY = PropertyValuesHolder.ofFloat("translationY", 0.0f, y);
        PropertyValuesHolder tlX = PropertyValuesHolder.ofFloat("translationX", 0.0f, 0.0f);
        ObjectAnimator anim = ofPropertyValuesHolder(imageContainer, tlX, tlY);
        anim.setDuration(300).start();
        anim.start();
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                imageContainer.setVisibility(View.GONE);
            }
        });
    }

    public class ImagrListAdapter extends BaseAdapter {

        private List<ImageModel> datas = new ArrayList<>();

        public ImagrListAdapter(List<ImageModel> datas) {
            if (null != datas) {
                this.datas.addAll(datas);
            }
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public Object getItem(int i) {
            return datas.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.media_view_type_image_item, null);
                viewHolder = new ViewHolder();
                viewHolder.imageIcon = convertView.findViewById(R.id.image_icon);
                viewHolder.fileName = convertView.findViewById(R.id.file_name);
                viewHolder.imageNum = convertView.findViewById(R.id.image_num);
                viewHolder.selec = convertView.findViewById(R.id.selec);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ImageModel item = datas.get(position);
            if (fileType.equals(item.fileName)) {
                viewHolder.selec.setVisibility(View.VISIBLE);
            } else {
                viewHolder.selec.setVisibility(View.GONE);
            }
            viewHolder.fileName.setText("" + item.fileName);
            viewHolder.imageNum.setText(item.pisNum + "张");
            MediaGlideLoader.LoderLoadImageType(getApplication(), item.path, viewHolder.imageIcon);
            return convertView;
        }

        public class ViewHolder {
            private ImageView imageIcon;
            private TextView fileName;
            private TextView imageNum;
            private ImageView selec;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (previewContainer.isShown()) {
                removePreviewView();
                return true;
            } else if (imageContainer.isShown()) {
                removeImageTypeView();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private String curPhotoPath;

    private void openCamera() {
        // 设置照片文件路径
        String fileName = System.currentTimeMillis() + "_IMG.jpg";
        File file = MediaFileUtils.createTempFile(MediaActivity.this, fileName);
        if (null != file && file.exists()) {
            curPhotoPath = file.getPath();
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //系统7.0打开相机权限处理
        if (Build.VERSION.SDK_INT >= 24) {
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            Uri uri = getApplication().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }
        startActivityForResult(intent, RESULT_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_CAMERA:
                    ImageModel imageModel = new ImageModel.Builder()
                            .path(curPhotoPath)
                            .thumb(curPhotoPath)
                            .build();
                    saveImage(imageModel);
                    sendMedias();
                    finish();
                    break;
                default:
                    break;
            }
        }
    }

}

