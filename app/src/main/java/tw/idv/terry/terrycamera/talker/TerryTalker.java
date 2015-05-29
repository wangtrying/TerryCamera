package tw.idv.terry.terrycamera.talker;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import tw.idv.terry.terrycamera.R;
import tw.idv.terry.terrycamera.TerryCameraApp;

/**
 * Created by wangtrying on 2015/5/28.
 */
public class TerryTalker {

    private Context mContext;
    private MediaPlayer mHereItComesPlayer, mBeautifulPlayer, mIsntItPlayer;

    public TerryTalker(Context aContext) throws Exception {
        if (aContext == null) {
            throw new Exception("Context is null!!");
        }
        mContext = aContext;
        initIsntItPlayer();
        initHereItComesPlayer();
        initBeautifulPlayer();
    }

    private void initIsntItPlayer() {
        if (mIsntItPlayer == null) {
            if (mContext == null) {
                mContext = TerryCameraApp.getContext();
            }
            mIsntItPlayer = MediaPlayer.create(mContext, R.raw.isntit);
        }
    }

    public void playIsntIt() {
        initIsntItPlayer();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mIsntItPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mIsntItPlayer.setLooping(false);
                mIsntItPlayer.start();
                gaRecord("isnt it");
            }
        };
        Thread t = new Thread(r);
        t.start();
//        Toast.makeText(mContext, "是不是?", Toast.LENGTH_LONG).show();
    }

    private void gaRecord(String aLabel) {
        if (!TerryCameraApp.IS_DEBUG) {
            HitBuilders.EventBuilder event = new HitBuilders.EventBuilder();
            event.setCategory("UX");
            event.setAction("play");
            event.setLabel(aLabel);
            Tracker tracker = TerryCameraApp.getTracker();
            tracker.setScreenName("FullScreenActivity");
            tracker.send(event.build());
        }
    }

    private void initBeautifulPlayer() {
        if (mBeautifulPlayer == null) {
            if (mContext == null) {
                mContext = TerryCameraApp.getContext();
            }
            mBeautifulPlayer = MediaPlayer.create(mContext, R.raw.beautiful);
        }
    }

    public void playBeautiful() {
        initBeautifulPlayer();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mBeautifulPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mBeautifulPlayer.setLooping(false);
                mBeautifulPlayer.start();
                gaRecord("beautiful");
            }
        };
        Thread t = new Thread(r);
        t.start();

//        Toast.makeText(mContext, "美美的~~~", Toast.LENGTH_LONG).show();
    }


    private void initHereItComesPlayer() {
        if (mHereItComesPlayer == null) {
            if (mContext == null) {
                mContext = TerryCameraApp.getContext();
            }
            mHereItComesPlayer = MediaPlayer.create(mContext, R.raw.herecomes);
        }
    }

    public void playHereItComes() {
        initHereItComesPlayer();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mHereItComesPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mHereItComesPlayer.setLooping(false);
                mHereItComesPlayer.start();
                gaRecord("hereComes");

            }
        };
        Thread t = new Thread(r);
        t.start();
//        Toast.makeText(mContext, "來了麻~~~", Toast.LENGTH_LONG).show();

    }

    public void releasePlayers() {

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

    public void ensureMaxVolume() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume != audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);
        }
    }

}
