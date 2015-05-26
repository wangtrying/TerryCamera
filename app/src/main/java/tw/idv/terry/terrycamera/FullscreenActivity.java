package tw.idv.terry.terrycamera;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FullscreenActivity extends Activity {

    private final static String TAG = "SimpleCamera";
    private static final int REQUEST_IMAGE_CAPTURE = 0x01;
    private Button mAlbumBtn;
    private MediaPlayer mHereItComesPlayer, mBeautifulPlayer, mIsntItPlayer;
    private ImageView mImageView;
    private TextView mTextView;
    private String mCurrentPhotoPath;
    private volatile boolean shouldBackToCamera = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        ensureMaxVolume();
        playHereItComes();
        prepareViews();
        dispatchTakePictureIntent();

    }

    private void ensureMaxVolume() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume != audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);
        }
    }

    private void prepareViews() {
        mImageView = (ImageView) findViewById(R.id.image_taken);
        mTextView = (TextView) findViewById(R.id.speech_text);
    }

    public void onAlbum(View aView) {
        playIsntIt();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private void playIsntIt() {
        mIsntItPlayer = MediaPlayer.create(this, R.raw.isntit);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mIsntItPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mIsntItPlayer.setLooping(false);
                mIsntItPlayer.start();

            }
        };
        Thread t = new Thread(r);
        t.start();
        Toast.makeText(this, "是不是?", Toast.LENGTH_LONG).show();

    }


    private void playBeautiful() {
        mBeautifulPlayer = MediaPlayer.create(this, R.raw.beautiful);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                mBeautifulPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mBeautifulPlayer.setLooping(false);
                mBeautifulPlayer.start();
            }
        };
        Thread t = new Thread(r);
        t.start();

        Toast.makeText(this, "美美的~~~", Toast.LENGTH_LONG).show();
    }

    private void playHereItComes() {
        mHereItComesPlayer = MediaPlayer.create(this, R.raw.herecomes);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mHereItComesPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mHereItComesPlayer.setLooping(false);
                mHereItComesPlayer.start();

            }
        };
        Thread t = new Thread(r);
        t.start();
        Toast.makeText(this, "來了麻~~~", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onDestroy() {
        releasePlayers();
        super.onDestroy();

    }

    private void releasePlayers() {

        if (mBeautifulPlayer != null) {
            mBeautifulPlayer.release();
        }

        if (mIsntItPlayer != null) {
            mIsntItPlayer.release();
        }
        if (mHereItComesPlayer != null) {
            mHereItComesPlayer.release();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                playBeautiful();
                galleryAddPic();
//                mTextView.setText("美美的～");
                setPic();
                shouldBackToCamera = true;
            }
            if (resultCode == RESULT_CANCELED) {
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (shouldBackToCamera){
            dispatchTakePictureIntent();
            playHereItComes();
            shouldBackToCamera = false;
        }else{
            super.onBackPressed();
        }
    }

    private void setPic() {

        mImageView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Get the dimensions of the View
        int targetW = mImageView.getLayoutParams().width;
        int targetH = mImageView.getLayoutParams().height;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
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

        if (image.exists() == false){
            image.createNewFile();
        }

        // Save a file: path for use with ACTION_VIEW intents
//        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}
