package com.innotech.innotechpush.sdk;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.util.Arrays;

public class AudioKeeper {
    private Thread t1 = null;
    private Thread t2 = null;
    private int mSampleRate = 8000;
    private short bufferA[];
    private short bufferB[];
    private int bufferSize;
    private float bufferDuration;
    private int loopCount;
    private static volatile AudioKeeper mAudioKeeper = null;
    private static final Object mutex = new Object();
    private int playTime = 60000;
    private boolean isPlay = false;

    private AudioKeeper() {
        bufferSize = AudioTrack.getMinBufferSize(mSampleRate,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize > 0) {
            if (bufferSize < 4)
                bufferSize = 4;
            bufferA = new short[bufferSize / 4];
            bufferB = new short[bufferSize / 4];
            Arrays.fill(bufferA, (short) 0x0101);
            Arrays.fill(bufferB, (short) 0x0101);
            bufferDuration = (float) (bufferSize * 250) / (float) mSampleRate;
            loopCount = (int) ((float) playTime / bufferDuration);
        }
    }

    public static AudioKeeper getInstance() {
        AudioKeeper result = mAudioKeeper;
        if (result == null) {
            synchronized (mutex) {
                result = mAudioKeeper;
                if (result == null)
                    mAudioKeeper = result = new AudioKeeper();
            }
        }
        return result;
    }

    public synchronized void playOnce() {
        if(t1 != null && isPlay)
            return;
        if (bufferSize <= 0)
            return;
        if (t2 != null) {
            stop();
        }
        t2 = new Thread() {
            public void run() {
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        mSampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2,
                        AudioTrack.MODE_STREAM);
                int i = loopCount / 30;
                if (i == 0)
                    i = 1;
                setPriority(Thread.MAX_PRIORITY);
                audioTrack.play();
                while (!Thread.currentThread().isInterrupted()) {
                    audioTrack.write(bufferA, 0, bufferSize / 4);
                    i--;
                    if (i <= 0) {
                        audioTrack.pause();
                        break;
                    }
                    if (!Thread.currentThread().isInterrupted()) {
                        audioTrack.write(bufferB, 0, bufferSize / 4);
                        i--;
                        if (i <= 0) {
                            audioTrack.pause();
                            break;
                        }
                    }
                }
                if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
                    audioTrack.stop();
                }
                audioTrack.flush();
                audioTrack.release();
            }
        };
        t2.start();
    }

    public synchronized void start() {
        if (bufferSize <= 0)
            return;
        if (t1 != null) {
            stop();
        }
        t1 = new Thread() {
            public void run() {
                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        mSampleRate, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2,
                        AudioTrack.MODE_STREAM);
                int i = loopCount;
                setPriority(Thread.MAX_PRIORITY);
                audioTrack.play();
                isPlay = true;
                while (!Thread.currentThread().isInterrupted()) {
                    audioTrack.write(bufferA, 0, bufferSize / 4);
                    i--;
                    if (i <= 0) {
                        audioTrack.pause();
                        if (!Thread.currentThread().isInterrupted()) {
                            i = loopCount;
                            audioTrack.flush();
                            audioTrack.play();
                        }
                    }
                    if (!Thread.currentThread().isInterrupted()) {
                        audioTrack.write(bufferB, 0, bufferSize / 4);
                        i--;
                        if (i <= 0) {
                            audioTrack.pause();
                            if (!Thread.currentThread().isInterrupted()) {
                                i = loopCount;
                                audioTrack.flush();
                                audioTrack.play();
                            }
                        }
                    }
                }
                if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) {
                    audioTrack.stop();
                }
                audioTrack.flush();
                audioTrack.release();
            }
        };
        t1.start();
    }


    public synchronized void stop() {
        if (t1 != null) {
            t1.interrupt();
            try {
                t1.join();
            } catch (InterruptedException ignored) {
            } finally {
                t1 = null;
            }
        }
        isPlay = false;
    }
}
