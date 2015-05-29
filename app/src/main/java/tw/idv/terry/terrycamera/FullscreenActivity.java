package tw.idv.terry.terrycamera;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tw.idv.terry.terrycamera.talker.TerryTalker;

public class FullscreenActivity extends Activity {

    private final static String TAG = "SimpleCamera";
    private static final int REQUEST_IMAGE_CAPTURE = 0x01;
    private Button mAlbumBtn;

    private RelativeLayout mImageView;
    //    private ImageView mImageView;
    private TextView mTextView;
    private String mCurrentPhotoPath;
        private volatile boolean shouldBackToCamera = false;
    private TerryTalker mTalker;
    private String mPreviousTakenImageName;

    @Override
    protected void onResume() {
        if (mTextView.getText().equals("~是不是")) {
            mTalker.playBeautiful();
        }
//        mTalker.playBeautiful();
        mTextView.setText("～的美美");
        mPreviousTakenImageName = getPreviousImageName();
        if (!mPreviousTakenImageName.equals(TerryCameraApp.DEFAULT_PREV_IMAGE_NAME)) {
            setPic(mPreviousTakenImageName);
        }
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        initTalker();
        mTalker.ensureMaxVolume();

        prepareViews();
        dispatchTakePictureIntent();


    }

    private String getPreviousImageName() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(TerryCameraApp.getContext());
        return (preferences.getString(TerryCameraApp.KEY_IMAGE_TAKEN_PREVIOUSLY, TerryCameraApp.DEFAULT_PREV_IMAGE_NAME));
    }

    private void initTalker() {
        try {
            mTalker = new TerryTalker(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void prepareViews() {
        mImageView = (RelativeLayout) findViewById(R.id.image_taken);
        mTextView = (TextView) findViewById(R.id.speech_text);
    }

    public void onAlbum(View aView) {
        mTalker.playIsntIt();
        mTextView.setText("~是不是");
        File file = new File(mCurrentPhotoPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    public void onCapture(View aView) {
        dispatchTakePictureIntent();
    }


    @Override
    protected void onDestroy() {
        mTalker.releasePlayers();
        super.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mTalker.playBeautiful();
                galleryAddPic();
                mTextView.setText("～的美美");
                setPic(mCurrentPhotoPath);
                shouldBackToCamera = true;
                writeIntoPref();

            }
            if (resultCode == RESULT_CANCELED) {
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (shouldBackToCamera) {
            dispatchTakePictureIntent();
            mTalker.playHereItComes();
            shouldBackToCamera = false;
        } else {
            super.onBackPressed();
        }
//        super.onBackPressed();

    }

    private void setPic(String aImagePath) {

        mImageView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Get the dimensions of the View
        int targetW = mImageView.getLayoutParams().width;
        int targetH = mImageView.getLayoutParams().height;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(aImagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(aImagePath, bmOptions);
        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
        mImageView.setBackground(drawable);
//        mImageView.setImageBitmap(bitmap);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        if (image.exists() == false) {
            image.createNewFile();
        }

        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        mTalker.playHereItComes();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityFromChild(this, takePictureIntent, REQUEST_IMAGE_CAPTURE);
//                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void writeIntoPref() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(TerryCameraApp.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(TerryCameraApp.KEY_IMAGE_TAKEN_PREVIOUSLY, mCurrentPhotoPath);
        editor.commit();
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

    }

}
