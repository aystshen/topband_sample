package com.ayst.sample.items.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.ayst.sample.R;

public class AudioPresenter implements MediaPlayer.OnPreparedListener {
    private static final String TAG = "AudioPresenter";

    private Context mContext;
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;

    private static final int[] STREAM_ARRAY = {AudioManager.STREAM_VOICE_CALL,
                                                AudioManager.STREAM_SYSTEM,
                                                AudioManager.STREAM_RING,
                                                AudioManager.STREAM_MUSIC,
                                                AudioManager.STREAM_ALARM,
                                                AudioManager.STREAM_NOTIFICATION,
                                                11,
                                                12};

    public AudioPresenter(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void play(int stream) {
        if (stream >= 0 && stream < STREAM_ARRAY.length) {
            try {
                // Create a new media player and set the listeners
                if (mMediaPlayer == null) {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setOnPreparedListener(this);
                } else {
                    mMediaPlayer.reset();
                }
                mMediaPlayer.setLooping(true);
                mMediaPlayer.setAudioStreamType(STREAM_ARRAY[stream]);
                mMediaPlayer.setDataSource(mContext,
                        Uri.parse("android.resource://com.ayst.sample/"
                                + R.raw.media_volume));
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                Log.e(TAG, "play, error: " + e.getMessage(), e);
            }
        } else {
            Log.e(TAG, "play, invalid stream index.");
        }
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    public void setVolume(int stream, int percent) {
        if (stream >=0 && stream < STREAM_ARRAY.length) {
            int maxVolume = mAudioManager.getStreamMaxVolume(STREAM_ARRAY[stream]);
            mAudioManager.setStreamVolume(STREAM_ARRAY[stream],
                    percent*maxVolume/100, AudioManager.FLAG_PLAY_SOUND);
        } else {
            Log.e(TAG, "setVolume, invalid stream index.");
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer.start();
    }
}
