package com.example.dheeraj.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawableResource;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

/**
 * Created by Dheeraj on 6/5/2016.
 */
public class ImageLoader {

    DBHelper databaseHelper;
    Context mContxet;
    ImageLoader(Context context)
    {
        mContxet = context;
        databaseHelper = new DBHelper(context);
    }


    class MyGlideTransform extends BitmapTransformation {
        int m_type;
        public MyGlideTransform(Context context , int type) {
            super(context);
            m_type = type;
        }

        @Override protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            //return circleCrop(pool, toTransform);
            if( m_type == 1)
                return createAsMarker( pool, toTransform ,  80 , 4 , 25 );
            else
                return circleCrop( pool, toTransform );
        }

        private Bitmap createAsMarker(BitmapPool pool, Bitmap source , int bitmapSize , int border , int pinSize) {

            if( source == null)
                return null;

            //first create for baloon.
            Bitmap canvasBmp = Bitmap.createBitmap( bitmapSize + 2*border , bitmapSize + 2*border + pinSize , Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(canvasBmp);
            Paint color = new Paint();
            color.setColor(Color.BLACK);
            canvas.drawRect(0, 0, bitmapSize + 2 * border, bitmapSize + 2 * border, color);
            //canvas.drawCircle( (bitmapSize + 2*border)/2, (bitmapSize + 2*border)/2, bitmapSize/2 , color);
            //color.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            Rect rSrc = new Rect(0,0,source.getWidth(),source.getHeight());
            Rect rDest = new Rect( border , border , bitmapSize + border , bitmapSize + border );
            canvas.drawBitmap(source, rSrc , rDest , color);
            //color.setTextSize(10);
            //canvas.drawText("Le Messi", 30, 40, color);
            color.setXfermode(null);
            color.setColor(Color.BLACK);
            Point a = new Point( (bitmapSize + 2*border) / 2 -10  , bitmapSize + 2*border );
            Point b = new Point( (bitmapSize + 2*border) / 2 +10 , bitmapSize + 2*border );
            Point c = new Point( (bitmapSize + 2*border) / 2  , bitmapSize + 2*border + pinSize);
            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.moveTo(b.x, b.y);
            path.lineTo(c.x, c.y);
            path.lineTo(a.x, a.y);
            path.close();
            canvas.drawPath(path, color);
            //color.setStyle(Paint.Style.STROKE);
            //color.setStrokeWidth(border);
            //canvas.drawCircle( (bitmapSize + 2*border)/2, (bitmapSize + 2*border)/2, (bitmapSize+border)/2 , color );
            return canvasBmp;
        }

        private Bitmap circleCrop(BitmapPool pool, Bitmap source) {
            if (source == null) return null;

            int size = Math.min(source.getWidth(), source.getHeight());
            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            // TODO this could be acquired from the pool too
            Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

            Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
            if (result == null) {
                result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(result);
            Paint paint = new Paint();
            paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);
            return result;
        }

        @Override public String getId() {
            return getClass().getName()+m_type;
        }

        /*
        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap,
                    BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }*/
    }

    class MyGlideTarget extends GlideDrawableImageViewTarget {
        Person person;
        MyGlideTarget( ImageView imageView , Person person) {
            super( imageView  );
            this.person = person;
        }

        @Override public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
            super.onResourceReady(resource, animation);
            //
            if( person.isPicOld == 1) {
                ContentValues cv = new ContentValues();
                cv.put("PROFILE_PIC_REFRESH" , 0);
                databaseHelper.getWritableDatabase().update("person", cv, "MOBILE=" + person.mobile, null);
            }
        }
    }

    class MyGlideImageView extends SimpleTarget<Bitmap> {
        ImageView mImgView;

        MyGlideImageView(ImageView marker) {
            mImgView = marker;
        }

        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> animation) {
            mImgView.setImageBitmap(resource);
        }
    }

    class MyGlideMarker extends SimpleTarget<Bitmap> {
        Marker mMarker;

        MyGlideMarker(Marker marker) {
            mMarker = marker;
        }

        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> animation) {
            DBHelper.LOG("onResourceReady" );
            mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resource));
        }
    }

    void LoadImageInToView(String mobile , ImageView view , int imageSize)
    {
        Person person = new Person();
        databaseHelper.GetFriendInfoFromLocalDB("",mobile,person);

        String url = DBHelper.hostAddr + "profile_pic_raw.php?"
                + "mobile=" + mobile + "&"
                + "size=" + imageSize + "&";
        url = url.replace(" ", "%20");

        Glide.with(mContxet)
                .load(url)
                .signature(new StringSignature(person.profilePicChecksum+"_"+mobile+"_"+imageSize))
                .into(new MyGlideTarget( view , person ));
    }

    void LoadImageInToMarker(String mobile , Marker marker , int imageSize) {

        Person person = new Person();
        databaseHelper.GetFriendInfoFromLocalDB("",mobile,person);

        String url = DBHelper.hostAddr + "profile_pic_raw.php?"
                + "mobile=" + mobile + "&"
                + "size=" + imageSize + "&";
        url = url.replace(" ", "%20");

        Glide.with(mContxet)
                .load(url)
                .asBitmap()
                .transform(new MyGlideTransform(mContxet,1))
                .signature(new StringSignature(person.profilePicChecksum+"_"+mobile+"_"+imageSize))
                .into(new MyGlideMarker( marker ));
    }

    void LoadImageInToMapList(String mobile , ImageView view , int imageSize) {

        Person person = new Person();
        databaseHelper.GetFriendInfoFromLocalDB("",mobile,person);

        String url = DBHelper.hostAddr + "profile_pic_raw.php?"
                + "mobile=" + mobile + "&"
                + "size=" + imageSize + "&";
        url = url.replace(" ", "%20");

        Glide.with(mContxet)
                .load(url)
                .asBitmap()
                .transform(new MyGlideTransform(mContxet,2))
                .signature(new StringSignature(person.profilePicChecksum+"_"+mobile+"_"+imageSize))
                .into(new MyGlideImageView( view  ));
    }

    void LoadImageInToFriendsList(String mobile , ImageView view , int imageSize) {

        Person person = new Person();
        databaseHelper.GetFriendInfoFromLocalDB("",mobile,person);

        String url = DBHelper.hostAddr + "profile_pic_raw.php?"
                + "mobile=" + mobile + "&"
                + "size=" + imageSize + "&";
        url = url.replace(" ", "%20");

        Glide.with(mContxet)
                .load(url)
                .asBitmap()
                .signature(new StringSignature(person.profilePicChecksum+"_"+mobile+"_"+imageSize))
                .into(new MyGlideImageView( view  ));
    }
}
