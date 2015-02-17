package com.voxmecanica.vdk.core;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;

import com.voxmecanica.vdk.VoxException;
import com.voxmecanica.vdk.logging.Logger;

import java.io.IOException;

public class VoxMediaPlayer {
    private Logger LOG = new Logger("VoxMediaPlayer");
    private MediaPlayer player;
    private Callback.OnCompleted onCompleted;
    private Callback.OnError onError;

    public VoxMediaPlayer(VoxMediaPlayer.Callback... callbacks) {
        player = new MediaPlayer();
        initPlayer();
    }

    public void setCallbacks(VoxMediaPlayer.Callback... callbacks) {
        for (VoxMediaPlayer.Callback cb : callbacks) {
            if (cb instanceof Callback.OnCompleted) onCompleted = (Callback.OnCompleted) cb;
            if (cb instanceof Callback.OnError) onError = (Callback.OnError) cb;
        }
    }

    private void initPlayer() {
        LOG.d("Initializing MediaPlayer for playback...");
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setLooping(false);

        player.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                LOG.d("Media resource prepared, ready for playback OK.");
                mp.start();
            }
        });

        player.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                LOG.d("Playback completed OK.");
                mp.stop();
                mp.reset();
                if (onCompleted != null) {
                    onCompleted.exec();
                }
            }
        });

        player.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                LOG.d("Playback encountered error(s).");
                if (onError != null) {
                    onError.exec(what);
                }
                return true;
            }
        });
    }

    public void playbackUri(String url) {
        try {
            LOG.d("Preparing to playback [" + url + "]");
            player.setDataSource(url);
            player.prepareAsync();
        } catch (IllegalArgumentException e) {
            throw new VoxException(VoxException.ErrorType.DialogExecutionError, String.format("Error playing media at URL [%s]: %s", url, e.getMessage()));
        } catch (IllegalStateException e) {
            throw new VoxException(VoxException.ErrorType.DialogExecutionError, String.format("Error playing media: MediaPlayer encountered an illegal state."));
        } catch (IOException e) {
            throw new VoxException(VoxException.ErrorType.DialogExecutionError, String.format("Error playing media: unable to reach URL [%s]: %s", url, e.getMessage()));
        } catch (Exception ex) {
            throw new VoxException(VoxException.ErrorType.DialogExecutionError, "Error playing media: " + ex.getMessage());
        }

    }

    public void stop() {
        if (player.isPlaying()) {
            player.stop();
        }
    }

    public void shutdown() {
        LOG.d("Shutting down MediaPalyer instance.");
        stop();
        player.release();
    }

    public static interface Callback {
        public static interface OnCompleted extends Callback {
            public void exec();
        }

        public static interface OnError extends Callback {
            public void exec(int errorCode);
        }
    }
}
