package com.way.heard.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.SaveCallback;
import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.AutoLinkOnClickListener;
import com.luseen.autolinklibrary.AutoLinkTextView;
import com.way.heard.R;
import com.way.heard.models.Image;
import com.way.heard.models.Post;
import com.way.heard.ui.activities.PostDisplayActivity;
import com.way.heard.ui.activities.ProfileActivity;
import com.way.heard.ui.views.TagCloudView;
import com.way.heard.utils.GlideImageLoader;
import com.way.heard.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 2016/5/7.
 */
public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = PostAdapter.class.getName();
    private final static int TYPE_POST = 0;
    private final static int TYPE_REPOST = 1;

    private int lastPosition = -1;
    private Context mContext;
    private List<Post> mPosts;
    private OnImageClickListener onImageClickListener;
    private OnRepostClickListener onRepostClickListener;
    private OnDeleteClickListener onDeleteClickListener;
    private OnAutoLinkTextViewClickListener onAutoLinkTextViewClickListener;


    public void setOnImageClickListener(OnImageClickListener onImageClickListener1) {
        this.onImageClickListener = onImageClickListener1;
    }

    public void setOnRepostClickListener(OnRepostClickListener onRepostClickListener1) {
        this.onRepostClickListener = onRepostClickListener1;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener onDeleteClickListener1) {
        this.onDeleteClickListener = onDeleteClickListener1;
    }

    public void setOnAutoLinkTextViewClickListener(OnAutoLinkTextViewClickListener onAutoLinkTextViewClickListener) {
        this.onAutoLinkTextViewClickListener = onAutoLinkTextViewClickListener;
    }

    public void setPosts(List<Post> mPosts) {
        this.mPosts = mPosts;
    }

    public PostAdapter(Context context) {
        this.mContext = context;
        this.mPosts = new ArrayList<>();
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R
                    .anim.item_bottom_in);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (holder instanceof PostViewHolder)
            ((PostViewHolder) holder).itemview.clearAnimation();
        else if (holder instanceof RepostViewHolder)
            ((RepostViewHolder) holder).itemview.clearAnimation();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_POST == viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_post_normal, parent, false);
            return new PostViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_post_repost, parent, false);
            return new RepostViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        //1. Get Item
        final Post post = mPosts.get(position);
        if (null == post)
            return;

        if (0 == post.getFrom()) {
            final PostViewHolder holder = (PostViewHolder) viewHolder;
            //2. Get Data From Item
            String strAvatarUrl = post.getAuthor().getString("avatar");
            String strUsername = post.getAuthor().getUsername();
            long longCreateAt = post.getCreatedAt().getTime();
            String strContent = post.getContent();
            final List<Image> images = post.tryGetPhotoList();
            String strImageUrl = "";
            if (images != null && images.size() > 0) {
                strImageUrl = images.get(0).getUrl();
            }
            List<String> strTags = post.getTags();
            List<String> likesObjectIDs = post.getLikes();
            List<String> commentObjectIDs = post.getComments();

            //3.Set Data
            //3.1.Avatar
            GlideImageLoader.displayImage(mContext, strAvatarUrl, holder.ivAvatar);
            holder.ivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfileActivity.go(mContext, post.getAuthor());
                }
            });
            //3.2.Username
            holder.tvUsername.setText(strUsername);
            holder.tvUsername.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfileActivity.go(mContext, post.getAuthor());
                }
            });
            //3.3.CreateAt
            holder.tvCreateAt.setText(Util.millisecs2DateString(longCreateAt));
            //3.3.DeleteButton
            if (strUsername.equalsIgnoreCase(AVUser.getCurrentUser().getUsername())) {
                holder.ivDelete.setVisibility(View.VISIBLE);
                holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onDeleteClickListener != null) {
                            onDeleteClickListener.onDeleteClick(position);
                        }
                    }
                });
            } else {
                holder.ivDelete.setVisibility(View.GONE);
            }

            //3.4.Content
            if (!TextUtils.isEmpty(strContent)) {
                holder.altv_post2_item_content.addAutoLinkMode(
                        AutoLinkMode.MODE_HASHTAG,
                        AutoLinkMode.MODE_PHONE,
                        AutoLinkMode.MODE_URL,
                        AutoLinkMode.MODE_EMAIL,
                        AutoLinkMode.MODE_MENTION,
                        AutoLinkMode.MODE_CUSTOM);
                holder.altv_post2_item_content.setHashtagModeColor(ContextCompat.getColor(mContext, R.color.colorTextHashTag));
                holder.altv_post2_item_content.setPhoneModeColor(ContextCompat.getColor(mContext, R.color.colorTextPhone));
                holder.altv_post2_item_content.setCustomModeColor(ContextCompat.getColor(mContext, R.color.colorTextCustom));
                holder.altv_post2_item_content.setMentionModeColor(ContextCompat.getColor(mContext, R.color.colorTextMention));
                holder.altv_post2_item_content.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
                    @Override
                    public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                        if (onAutoLinkTextViewClickListener != null) {
                            onAutoLinkTextViewClickListener.onAutoLinkTextViewClick(autoLinkMode, matchedText);
                        }
                    }
                });
                holder.altv_post2_item_content.setVisibility(View.VISIBLE);
                holder.altv_post2_item_content.setAutoLinkText(strContent);

            } else {
                holder.altv_post2_item_content.setVisibility(View.GONE);
            }
            //3.5.Photo
            if (!TextUtils.isEmpty(strImageUrl)) {
                holder.ivPhoto.setVisibility(View.VISIBLE);
                final String finalStrImageUrl = strImageUrl;
                holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //ImageDisplayActivity.go(mContext, images.get(0), position);
                        if (onImageClickListener != null) {
                            onImageClickListener.onImageClick(position);
                        }
                    }
                });
                //CustomImageSizeModel customImageRequest = new CustomImageSizeModelFutureStudio(strImageUrl);
                GlideImageLoader.displayImage(mContext, strImageUrl, holder.ivPhoto);
            } else {
                holder.ivPhoto.setVisibility(View.GONE);
            }
            //3.6.Tag
            if (strTags != null && strTags.size() > 0) {
                holder.tcvTags.setVisibility(View.VISIBLE);
                holder.tcvTags.setTags(strTags);
            } else {
                holder.tcvTags.setVisibility(View.GONE);
            }
            //3.7.Likes
            final AVUser currentUser = AVUser.getCurrentUser();
            if (likesObjectIDs == null) {
                likesObjectIDs = new ArrayList<>();
            }
            final boolean isliked = likesObjectIDs.contains(currentUser.getObjectId());
            if (isliked) {
                //tvLikesCount.setTextColor(context.getResources().getColor(R.color.colorAccent));
                holder.ivLikesButton.setImageResource(R.drawable.ic_thumb_up_accent);
            } else {
                holder.ivLikesButton.setImageResource(R.drawable.ic_thumb_up);
            }

            final List<String> finalLikesObjectIDs = likesObjectIDs;
            holder.ivLikesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isliked) {//Remove Like
                        finalLikesObjectIDs.remove(currentUser.getObjectId());
                        post.setLikes(finalLikesObjectIDs);
                        post.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    notifyDataSetChanged();
                                    //ivLikesButton.setImageResource(R.drawable.ic_thumb_up);
                                } else {
                                    Util.toast(mContext, "Error, " + e.getMessage());
                                }
                            }
                        });
                    } else {//Add Like
                        finalLikesObjectIDs.add(currentUser.getObjectId());
                        post.setLikes(finalLikesObjectIDs);
                        post.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    notifyDataSetChanged();
                                    //ivLikesButton.setImageResource(R.drawable.ic_thumb_up_accent);
                                } else {
                                    Util.toast(mContext, "Error, " + e.getMessage());
                                }
                            }
                        });
                    }
                }
            });

            //3.8.Comments
            holder.ivCommentsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PostDisplayActivity.go(mContext, post);
                }
            });
            //3.9.Repost
            holder.ivRepostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onRepostClickListener != null) {
                        onRepostClickListener.onRepostClick(position);
                    }
                    //RepostActivity.go(mContext, post, post.getReplyOriginal());
                }
            });
            setAnimation(holder.itemview, position);
        } else {
            RepostViewHolder holder = (RepostViewHolder) viewHolder;
            //2. Get Data From Item
            String strAvatarUrl = post.getAuthor().getString("avatar");
            String strUsername = post.getAuthor().getUsername();
            long longCreateAt = post.getCreatedAt().getTime();
            String strContent = post.getContent();

            List<String> likesObjectIDs = post.getLikes();
            List<String> commentObjectIDs = post.getComments();

            //3.Set Data
            //3.1.Avatar
            GlideImageLoader.displayImage(mContext, strAvatarUrl, holder.ivAvatar);
            holder.ivAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProfileActivity.go(mContext, post.getAuthor());
                }
            });
            //3.2.Username
            holder.tvUsername.setText(strUsername);
            //3.3.CreateAt
            holder.tvCreateAt.setText(Util.millisecs2DateString(longCreateAt));
            //3.3.DeleteButton
            if (strUsername.equalsIgnoreCase(AVUser.getCurrentUser().getUsername())) {
                holder.ivDelete.setVisibility(View.VISIBLE);
                holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onDeleteClickListener != null) {
                            onDeleteClickListener.onDeleteClick(position);
                        }
                    }
                });
            } else {
                holder.ivDelete.setVisibility(View.GONE);
            }
            //3.4.Content
            if (!TextUtils.isEmpty(strContent)) {
                holder.altv_repost_item_content.addAutoLinkMode(
                        AutoLinkMode.MODE_HASHTAG,
                        AutoLinkMode.MODE_PHONE,
                        AutoLinkMode.MODE_URL,
                        AutoLinkMode.MODE_EMAIL,
                        AutoLinkMode.MODE_MENTION,
                        AutoLinkMode.MODE_CUSTOM);
                holder.altv_repost_item_content.setHashtagModeColor(ContextCompat.getColor(mContext, R.color.colorTextHashTag));
                holder.altv_repost_item_content.setPhoneModeColor(ContextCompat.getColor(mContext, R.color.colorTextPhone));
                holder.altv_repost_item_content.setCustomModeColor(ContextCompat.getColor(mContext, R.color.colorTextCustom));
                holder.altv_repost_item_content.setMentionModeColor(ContextCompat.getColor(mContext, R.color.colorTextMention));
                holder.altv_repost_item_content.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
                    @Override
                    public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                        if (onAutoLinkTextViewClickListener != null) {
                            onAutoLinkTextViewClickListener.onAutoLinkTextViewClick(autoLinkMode, matchedText);
                        }
                    }
                });
                holder.altv_repost_item_content.setVisibility(View.VISIBLE);
                holder.altv_repost_item_content.setAutoLinkText(strContent);

            } else {
                holder.altv_repost_item_content.setVisibility(View.GONE);
            }

            //Original Post Data
            Post postOriginal = post.tryGetPostOriginal();
            String strUsernameOriginal = postOriginal.getAuthor().getUsername();
            String strContentOriginal = postOriginal.getContent();
            final List<Image> imagesOriginal = postOriginal.tryGetPhotoList();
            String strImageUrlOriginal = "";
            if (imagesOriginal != null && imagesOriginal.size() > 0) {
                strImageUrlOriginal = imagesOriginal.get(0).getUrl();
            }
            List<String> strTagsOriginal = postOriginal.getTags();

            holder.altv_repost_item_nickname_original.addAutoLinkMode(
                    AutoLinkMode.MODE_HASHTAG,
                    AutoLinkMode.MODE_PHONE,
                    AutoLinkMode.MODE_URL,
                    AutoLinkMode.MODE_EMAIL,
                    AutoLinkMode.MODE_MENTION,
                    AutoLinkMode.MODE_CUSTOM);
            holder.altv_repost_item_nickname_original.setHashtagModeColor(ContextCompat.getColor(mContext, R.color.colorTextHashTag));
            holder.altv_repost_item_nickname_original.setPhoneModeColor(ContextCompat.getColor(mContext, R.color.colorTextPhone));
            holder.altv_repost_item_nickname_original.setCustomModeColor(ContextCompat.getColor(mContext, R.color.colorTextCustom));
            holder.altv_repost_item_nickname_original.setMentionModeColor(ContextCompat.getColor(mContext, R.color.colorTextMention));
            holder.altv_repost_item_nickname_original.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
                @Override
                public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                    if (onAutoLinkTextViewClickListener != null) {
                        onAutoLinkTextViewClickListener.onAutoLinkTextViewClick(autoLinkMode, matchedText);
                    }
                }
            });
            holder.altv_repost_item_nickname_original.setAutoLinkText("@" + strUsernameOriginal);

            if (TextUtils.isEmpty(strContentOriginal)) {
                holder.altv_repost_item_content_original.setVisibility(View.GONE);
            } else {
                holder.altv_repost_item_content_original.addAutoLinkMode(
                        AutoLinkMode.MODE_HASHTAG,
                        AutoLinkMode.MODE_PHONE,
                        AutoLinkMode.MODE_URL,
                        AutoLinkMode.MODE_EMAIL,
                        AutoLinkMode.MODE_MENTION,
                        AutoLinkMode.MODE_CUSTOM);
                holder.altv_repost_item_content_original.setHashtagModeColor(ContextCompat.getColor(mContext, R.color.colorTextHashTag));
                holder.altv_repost_item_content_original.setPhoneModeColor(ContextCompat.getColor(mContext, R.color.colorTextPhone));
                holder.altv_repost_item_content_original.setCustomModeColor(ContextCompat.getColor(mContext, R.color.colorTextCustom));
                holder.altv_repost_item_content_original.setMentionModeColor(ContextCompat.getColor(mContext, R.color.colorTextMention));
                holder.altv_repost_item_content_original.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
                    @Override
                    public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                        if (onAutoLinkTextViewClickListener != null) {
                            onAutoLinkTextViewClickListener.onAutoLinkTextViewClick(autoLinkMode, matchedText);
                        }
                    }
                });
                holder.altv_repost_item_content_original.setVisibility(View.VISIBLE);
                holder.altv_repost_item_content_original.setAutoLinkText(strContentOriginal);

            }

            //3.5.Photo
            if (!TextUtils.isEmpty(strImageUrlOriginal)) {
                holder.ivPhotoOriginal.setVisibility(View.VISIBLE);
                //final String finalStrImageUrl = strImageUrl;
                holder.ivPhotoOriginal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (onImageClickListener != null) {
                            onImageClickListener.onImageClick(position);
                        }
                    }
                });
                //CustomImageSizeModel customImageRequest = new CustomImageSizeModelFutureStudio(strImageUrlOriginal);
                GlideImageLoader.displayImage(mContext, strImageUrlOriginal, holder.ivPhotoOriginal);
            } else {
                holder.ivPhotoOriginal.setVisibility(View.GONE);
            }
            //3.6.Tag
            if (strTagsOriginal != null && strTagsOriginal.size() > 0) {
                holder.tcvTagsOriginal.setVisibility(View.VISIBLE);
                holder.tcvTagsOriginal.setTags(strTagsOriginal);
            } else {
                holder.tcvTagsOriginal.setVisibility(View.GONE);
            }
            //3.7.Likes
            final AVUser currentUser = AVUser.getCurrentUser();
            if (likesObjectIDs == null) {
                likesObjectIDs = new ArrayList<>();
            }
            final boolean isliked = likesObjectIDs.contains(currentUser.getObjectId());
            if (isliked) {
                //tvLikesCount.setTextColor(context.getResources().getColor(R.color.colorAccent));
                holder.ivLikesButton.setImageResource(R.drawable.ic_thumb_up_accent);
            } else {
                holder.ivLikesButton.setImageResource(R.drawable.ic_thumb_up);
            }

            final List<String> finalLikesObjectIDs = likesObjectIDs;
            holder.ivLikesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isliked) {//Remove Like
                        finalLikesObjectIDs.remove(currentUser.getObjectId());
                        post.setLikes(finalLikesObjectIDs);
                        post.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    notifyDataSetChanged();
                                    //ivLikesButton.setImageResource(R.drawable.ic_thumb_up);
                                } else {
                                    Util.toast(mContext, "Error, " + e.getMessage());
                                }
                            }
                        });
                    } else {//Add Like
                        finalLikesObjectIDs.add(currentUser.getObjectId());
                        post.setLikes(finalLikesObjectIDs);
                        post.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(AVException e) {
                                if (e == null) {
                                    notifyDataSetChanged();
                                    //ivLikesButton.setImageResource(R.drawable.ic_thumb_up_accent);
                                } else {
                                    Util.toast(mContext, "Error, " + e.getMessage());
                                }
                            }
                        });
                    }
                }
            });

            //3.8.Comments
            holder.ivCommentsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PostDisplayActivity.go(mContext, post);
                }
            });
            //3.9.Repost
            holder.ivRepostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onRepostClickListener != null) {
                        onRepostClickListener.onRepostClick(position);
                    }
                    //RepostActivity.go(mContext, post, post.getReplyOriginal());
                }
            });
            setAnimation(holder.itemview, position);
        }
    }

    @Override
    public int getItemCount() {
        return mPosts == null ? 0 : mPosts.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (TYPE_POST == mPosts.get(position).getFrom())
            return TYPE_POST;
        else
            return TYPE_REPOST;
    }

    public class PostViewHolder extends RecyclerView.ViewHolder {

        View itemview;
        ImageView ivAvatar;
        TextView tvUsername;
        TextView tvCreateAt;
        //TextView tvContent;
        AutoLinkTextView altv_post2_item_content;
        ImageView ivDelete;

        ImageView ivPhoto;
        TagCloudView tcvTags;
        ImageView ivLikesButton;
        ImageView ivCommentsButton;
        ImageView ivRepostButton;

        public PostViewHolder(View contentView) {
            super(contentView);
            itemview = contentView;
            //cvContainer = (CardView) contentView.findViewById(R.id.cv_post2_item_container);
            ivAvatar = (ImageView) contentView.findViewById(R.id.iv_post2_item_avatar);
            tvUsername = (TextView) contentView.findViewById(R.id.tv_post2_item_nickname);
            tvCreateAt = (TextView) contentView.findViewById(R.id.tv_post2_item_createat);
            //tvContent = (TextView) contentView.findViewById(R.id.tv_post2_item_content);
            altv_post2_item_content = (AutoLinkTextView) contentView.findViewById(R.id.altv_post2_item_content);
            ivDelete = (ImageView) contentView.findViewById(R.id.iv_post2_item_delete);

            ivPhoto = (ImageView) contentView.findViewById(R.id.iv_post2_item_photo);
            tcvTags = (TagCloudView) contentView.findViewById(R.id.tcv_post2_item_tags);
            ivLikesButton = (ImageView) contentView.findViewById(R.id.iv_post2_item_likes_button);
            ivCommentsButton = (ImageView) contentView.findViewById(R.id.iv_post2_item_comments_button);
            ivRepostButton = (ImageView) contentView.findViewById(R.id.iv_post2_item_repost_button);


        }
    }

    public class RepostViewHolder extends RecyclerView.ViewHolder {
        View itemview;
        //CardView cvContainer;
        ImageView ivAvatar;
        TextView tvUsername;
        TextView tvCreateAt;
        //TextView tvContent;
        AutoLinkTextView altv_repost_item_content;
        ImageView ivDelete;

        //        TextView tvUsernameOriginal;
//        TextView tvContentOriginal;
        AutoLinkTextView altv_repost_item_nickname_original;
        AutoLinkTextView altv_repost_item_content_original;

        ImageView ivPhotoOriginal;
        TagCloudView tcvTagsOriginal;

        ImageView ivLikesButton;
        ImageView ivCommentsButton;
        ImageView ivRepostButton;

        public RepostViewHolder(View contentView) {
            super(contentView);
            itemview = contentView;
            //cvContainer = (CardView) contentView.findViewById(R.id.cv_repost_item_container);
            ivAvatar = (ImageView) contentView.findViewById(R.id.iv_repost_item_avatar);
            tvUsername = (TextView) contentView.findViewById(R.id.tv_repost_item_nickname);
            tvCreateAt = (TextView) contentView.findViewById(R.id.tv_repost_item_createat);
            //tvContent = (TextView) contentView.findViewById(R.id.tv_repost_item_content);
            altv_repost_item_content = (AutoLinkTextView) contentView.findViewById(R.id.altv_repost_item_content);
            ivDelete = (ImageView) contentView.findViewById(R.id.iv_repost_item_delete);

//            tvUsernameOriginal = (TextView) contentView.findViewById(R.id.tv_repost_item_nickname_original);
//            tvContentOriginal = (TextView) contentView.findViewById(R.id.tv_repost_item_content_original);
            altv_repost_item_nickname_original = (AutoLinkTextView) contentView.findViewById(R.id.altv_repost_item_nickname_original);
            altv_repost_item_content_original = (AutoLinkTextView) contentView.findViewById(R.id.altv_repost_item_content_original);
            ivPhotoOriginal = (ImageView) contentView.findViewById(R.id.iv_repost_item_photo_original);
            tcvTagsOriginal = (TagCloudView) contentView.findViewById(R.id.tcv_repost_item_tags_original);

            ivLikesButton = (ImageView) contentView.findViewById(R.id.iv_repost_item_likes_button);
            ivCommentsButton = (ImageView) contentView.findViewById(R.id.iv_repost_item_comments_button);
            ivRepostButton = (ImageView) contentView.findViewById(R.id.iv_repost_item_repost_button);

        }
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int pos);
    }

    public interface OnImageClickListener {
        void onImageClick(int pos);
    }

    public interface OnRepostClickListener {
        void onRepostClick(int pos);
    }

    public interface OnAutoLinkTextViewClickListener {
        void onAutoLinkTextViewClick(AutoLinkMode autoLinkMode, String matchedText);
    }
}
