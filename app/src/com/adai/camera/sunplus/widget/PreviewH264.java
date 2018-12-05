package com.adai.camera.sunplus.widget;

import android.content.Context;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.adai.camera.product.ISunplusCamera;
import com.adai.camera.sunplus.SDKAPI.PreviewStream;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.adai.camera.sunplus.tool.ScaleTool;
import com.icatch.wificam.customer.ICatchWificamPreview;
import com.icatch.wificam.customer.exception.IchAudioStreamClosedException;
import com.icatch.wificam.customer.exception.IchBufferTooSmallException;
import com.icatch.wificam.customer.exception.IchCameraModeException;
import com.icatch.wificam.customer.exception.IchInvalidArgumentException;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.exception.IchSocketException;
import com.icatch.wificam.customer.exception.IchStreamNotRunningException;
import com.icatch.wificam.customer.exception.IchTryAgainException;
import com.icatch.wificam.customer.type.ICatchAudioFormat;
import com.icatch.wificam.customer.type.ICatchFrameBuffer;
import com.icatch.wificam.customer.type.ICatchVideoFormat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class PreviewH264 extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = PreviewH264.class.getSimpleName();
    private AudioTrack audioTrack;
    private PreviewStream previewStream = PreviewStream.getInstance();
    private SurfaceHolder holder;
    private H264DecodeThread mySurfaceViewThread;
    private boolean hasSurface = false;
    private AudioThread audioThread;
    private boolean hasInit = false;
    private ICatchWificamPreview previewStreamControl;
    public ICatchWificamPreview icatchMedia;
    private int BUFFER_LENGTH = 1280 * 720 * 4;
    private int timeout = 0;// us
    private View parent;
    private int myWidth;
    private Handler handler;
    private ISunplusCamera mCamera;
    public final int ADJUST_LAYOUT_H264 = 1;
    public final int test_message = 2;
    Queue<ICatchFrameBuffer> audioQueue;
    boolean audioPlayFlag;
    private boolean adjustLayoutH264Complete = false;
    private MediaCodec decoder;
    private ScaleTool.ScaleType mScaleType = ScaleTool.ScaleType.AUTO;

    public PreviewH264(Context context) {
        this(context, null);
    }

    public PreviewH264(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.e(TAG, "call PreviewH264()");
        // JIRA ICOM-2098 Begin Add by b.jiang 2015-10-19
        holder = this.getHolder();
        holder.addCallback(this);
        // JIRA ICOM-2098 End Add by b.jiang 2015-10-19
    }

    private void initH264() {
        audioQueue = new LinkedList<ICatchFrameBuffer>();
        audioPlayFlag = false;
        // JIRA ICOM-2098 Begin Delete by b.jiang 2015-10-19
        // holder = this.getHolder();
        // holder.addCallback(this);
        // JIRA ICOM-2098 End Delete by b.jiang 2015-10-19
        hasInit = true;
        previewStreamControl = mCamera.getPreviewStreamClient();
        parent = (View) this.getParent();
        myWidth = 0;
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ADJUST_LAYOUT_H264:
                        setSurfaceViewArea(parent.getWidth(), parent.getHeight());
                        adjustLayoutH264Complete = true;
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public void setScaleType(ScaleTool.ScaleType scaleType) {
        mScaleType = scaleType;
    }

    public void start(ISunplusCamera mCamera) {
        this.mCamera = mCamera;
        if (!hasInit) {
            initH264();
        }
        // 创建和启动图像更新线程
        if (mySurfaceViewThread == null) {
            // mySurfaceViewThread = new VideoThread();
            mySurfaceViewThread = new H264DecodeThread();
            Log.e(TAG, "PreviewH264: hasSurface =" + hasSurface);
            if (hasSurface) {
                setFormat();
                mySurfaceViewThread.start();
            }
        }
        // 启动音频线程
        // JIRA ICOM-1844 Start Add by b.jiang 2015-08-14
        if (previewStream.supportAudio(mCamera.getPreviewStreamClient()) && (!GlobalInfo.forbidAudioOutput)) {
            if (audioThread == null) {
                audioThread = new AudioThread();
                audioThread.start();
            }
        }
        // JIRA ICOM-1844 End Add by b.jiang 2015-08-14
    }

    public boolean stop() {
        // 杀死图像更新线程
        if (mySurfaceViewThread != null) {
            mySurfaceViewThread.requestExitAndWait();
            mySurfaceViewThread = null;
        }
        // 杀死音频线程
        if (audioThread != null) {
            audioThread.requestExitAndWait();
            audioThread = null;
        }
        hasInit = false;
        return true;
    }

    private class AudioThread extends Thread {
        private boolean done = false;

        public void run() {
            ICatchFrameBuffer temp;

            ICatchAudioFormat audioFormat = previewStream.getAudioFormat(previewStreamControl);
            int bufferSize = AudioTrack.getMinBufferSize(audioFormat.getFrequency(),
                    audioFormat.getNChannels() == 2 ? AudioFormat.CHANNEL_IN_STEREO : AudioFormat.CHANNEL_IN_LEFT,
                    audioFormat.getSampleBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT);

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioFormat.getFrequency(),
                    audioFormat.getNChannels() == 2 ? AudioFormat.CHANNEL_IN_STEREO : AudioFormat.CHANNEL_IN_LEFT,
                    audioFormat.getSampleBits() == 16 ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT, bufferSize,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();

            // long audioStartTime = 0;
            boolean ret = false;
            ICatchFrameBuffer tempBuffer = new ICatchFrameBuffer(1024 * 50);
            byte[] testaudioBuffer = new byte[1024 * 50];
            tempBuffer.setBuffer(testaudioBuffer);
            while (!done) {
                ICatchFrameBuffer icatchBuffer = new ICatchFrameBuffer(1024 * 50);
                byte[] audioBuffer = new byte[1024 * 50];
                icatchBuffer.setBuffer(audioBuffer);
                ret = false;
                try {
                    ret = previewStreamControl.getNextAudioFrame(icatchBuffer);
                } catch (IchSocketException | IchBufferTooSmallException | IchCameraModeException | IchInvalidSessionException | IchStreamNotRunningException | IchAudioStreamClosedException | IchInvalidArgumentException e) {
                    e.printStackTrace();
                    return;
                } catch (IchTryAgainException e) {
                    e.printStackTrace();
                }
                if (!ret) {
                    continue;
                } else {
                    if (audioQueue.size() > 100) {
                        audioQueue.poll();
                    }
                    audioQueue.offer(icatchBuffer);
                }
                if (audioPlayFlag) {
                    temp = audioQueue.poll();
                    audioTrack.write(temp.getBuffer(), 0, temp.getFrameSize());
                }
            }
            audioTrack.stop();
            audioTrack.release();
        }

        public void requestExitAndWait() {
            // 把这个线程标记为完成，并合并到主程序线程
            done = true;
//            try {
//                join();
//            } catch (InterruptedException ignored) {
//            }
        }
    }

    private class H264DecodeThread extends Thread {
        private boolean done = false;
        private BufferInfo info;

        H264DecodeThread() {
            super();
            done = false;
        }

        @Override
        public void run() {
            adjustLayoutH264Complete = false;


            ByteBuffer[] inputBuffers = decoder.getInputBuffers();
            info = new BufferInfo();

            byte[] mPixel = new byte[BUFFER_LENGTH];
            ICatchFrameBuffer frameBuffer = new ICatchFrameBuffer();
            frameBuffer.setBuffer(mPixel);
            int inIndex = -1;
            int sampleSize = 0;
            long pts = 0;
            boolean retvalue = true;
            while (!done) {
                // 添加变量adjustLayoutH264Complete,使设定宽高才能去显示Preview;
                if (myWidth != getWidth()) {
                    if (getWidth() > 0) {
                        adjustLayoutH264Complete = false;
                        myWidth = getWidth();
                        handler.obtainMessage(ADJUST_LAYOUT_H264).sendToTarget();
                    }
                }
                // JIRA ICOM-1723 Start add by b.jiang 20150821
                if (!adjustLayoutH264Complete) {
                    continue;
                }
                // JIRA ICOM-1723 End add by b.jiang 20150821
                retvalue = false;
                try {
                    retvalue = icatchMedia.getNextVideoFrame(frameBuffer);

                    if (!retvalue) {
                        continue;
                    }
                } catch (IchTryAgainException ex) {
//					WriteLogToDevice.writeLog("[Error] -- PreviewH264: ", "getNextVideoFrame IchTryAgainException");
                    // Log.d("1111", "getNextVideoFrame IchTryAgainException");
                    ex.printStackTrace();

                    retvalue = false;
                    continue;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    retvalue = false;
                    break;
                }
                if (frameBuffer.getFrameSize() <= 0 || frameBuffer == null) {
                    retvalue = false;
                    continue;
                }

                inIndex = -1;
                inIndex = decoder.dequeueInputBuffer(timeout);
                if (inIndex >= 0) {
                    sampleSize = frameBuffer.getFrameSize();
                    pts = (long) (frameBuffer.getPresentationTime() * 1000 * 1000); // (seconds
                    ByteBuffer buffer = inputBuffers[inIndex];
                    buffer.clear();
                    buffer.rewind();
                    buffer.put(frameBuffer.getBuffer(), 0, sampleSize);

                    decoder.queueInputBuffer(inIndex, 0, sampleSize, pts, 0);
                }
                dequeueAndRenderOutputBuffer(timeout);

                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break;
                }

            }
            decoder.stop();
            decoder.release();
        }

        public boolean dequeueAndRenderOutputBuffer(int outtime) {
            int outIndex = decoder.dequeueOutputBuffer(info, outtime);

            if (outIndex >= 0) {
                decoder.releaseOutputBuffer(outIndex, true);
                if (!audioPlayFlag) {
                    audioPlayFlag = true;
                }
                return true;
            } else {
                return false;
            }
        }

        public void requestExitAndWait() {
            // 把这个线程标记为完成，并合并到主程序线程
            done = true;
//            try {
//                join();
//            } catch (InterruptedException ex) {
//            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "PreviewH264: surfaceCreated hasSurface =" + hasSurface);
        hasSurface = true;
        if (mySurfaceViewThread != null) {
            if (mySurfaceViewThread.isAlive() == false) {
                setFormat();
                mySurfaceViewThread.start();
            }
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "PreviewH264: surfaceDestroyed hasSurface =" + hasSurface);
        hasSurface = false;
        stop();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }

    public void setSurfaceViewArea(int mWidth, int mHeight) {
        // JIRA ICOM-1723 begin add by b.jiang 2015-09-07
        int frmW = previewStream.getVideoWidth(mCamera.getPreviewStreamClient());
        int frmH = previewStream.getVideoHeigth(mCamera.getPreviewStreamClient());
        if (frmH <= 0 || frmW <= 0) {
            return;
        }
        Rect drawFrameRect = ScaleTool.getScaledPosition(frmW, frmH, mWidth, mHeight, mScaleType);
        // JIRA ICOM-1723 end add by b.jiang 2015-09-07
        RelativeLayout.LayoutParams surfaceViewLayoutParams = (RelativeLayout.LayoutParams) this.getLayoutParams();
        surfaceViewLayoutParams.setMargins(drawFrameRect.left, drawFrameRect.top, drawFrameRect.left, drawFrameRect.top);
        this.setLayoutParams(surfaceViewLayoutParams);
    }

    private void setFormat() {
        icatchMedia = mCamera.getPreviewStreamClient();
        /* create & config android.media.MediaFormat */
        ICatchVideoFormat videoFormat = null;
        int w = 0, h = 0;
        try {
            videoFormat = icatchMedia.getVideoFormat();
            w = videoFormat.getVideoW();
            h = videoFormat.getVideoH();
        } catch (IchSocketException | IchCameraModeException | IchInvalidSessionException | IchStreamNotRunningException e1) {
            e1.printStackTrace();
        }
        String type = videoFormat.getMineType();
        // MediaFormat format =
        // MediaFormat.createVideoFormat(videoFormat.getMineType(),
        // videoFormat.getVideoW(), videoFormat.getVideoH());
        MediaFormat format = MediaFormat.createVideoFormat(type, w, h);

        // MediaFormat format =
        // MediaFormat.createVideoFormat(videoFormat.getMineType(),
        // videoFormat.getVideoW(), videoFormat.getVideoH());
        format.setByteBuffer("csd-0", ByteBuffer.wrap(videoFormat.getCsd_0(), 0, videoFormat.getCsd_0_size()));
        format.setByteBuffer("csd-1", ByteBuffer.wrap(videoFormat.getCsd_1(), 0, videoFormat.getCsd_0_size()));
        format.setInteger("durationUs", videoFormat.getDurationUs());
        format.setInteger("max-input-size", videoFormat.getMaxInputSize());

        /* create & config android.media.MediaCodec */
        String ret = videoFormat.getMineType();

        decoder = null;
        try {
            decoder = MediaCodec.createDecoderByType(ret);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        decoder.configure(format, holder.getSurface(), null, 0);
        decoder.start();
    }
}
