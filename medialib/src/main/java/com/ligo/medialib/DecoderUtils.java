package com.ligo.medialib;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * @author huangxy
 */
public class DecoderUtils {
    private static final String TAG = DecoderUtils.class.getSimpleName();
    private MediaCodec mDecoder;
    private byte[] mRawData;
    private int mWidth, mHeight;

    public void initDecoder(Surface surface, String type, int width, int height) {
        if (mDecoder != null) {
            return;
        }
        try {
            Log.e("9527", "initDecoder 123");
            mDecoder = MediaCodec.createDecoderByType(type);
            MediaFormat format = MediaFormat.createVideoFormat(type, width, height);
            mDecoder.configure(format, surface, null, 0);
            mDecoder.start();
            mWidth = width;
            mHeight = height;
        } catch (IOException e) {
            e.printStackTrace();
        }
//        MediaCodecInfo.VideoCapabilities
    }

    private OnDecoderListener mOnDecoderListener;

    public interface OnDecoderListener {
        void decoded(byte[] data, int length, int width, int height);
    }

    public void setOnDecoderListener(OnDecoderListener onDecoderListener) {
        mOnDecoderListener = onDecoderListener;
    }

    public void offerDecoder(byte[] input, int length) {
        String format = String.format("%02x%02x%02x%02x%02x%02x%02x%02x",
                input[0], input[1], input[2], input[3], input[4], input[5], input[6], input[7], input[8]);
        Log.e("1234",format);
        try {
            ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();
            int inputBufferIndex = mDecoder.dequeueInputBuffer(50);
            while (inputBufferIndex == -1) {
                SystemClock.sleep(10);
                inputBufferIndex = mDecoder.dequeueInputBuffer(50);
            }
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                inputBuffer.clear();
                inputBuffer.rewind();
                inputBuffer.put(input, 0, length);
                //计算pts
                mDecoder.queueInputBuffer(inputBufferIndex, 0, length, 0, 0);
            }
//            Log.e("1234", "1111111111111111111111111");

            try {
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                ByteBuffer[] outputBuffers = mDecoder.getOutputBuffers();
                int outputBufferIndex = mDecoder.dequeueOutputBuffer(bufferInfo, 0);
                while (outputBufferIndex >= 0) {
                    if (mRawData == null || mRawData.length < bufferInfo.size) {
                        mRawData = new byte[bufferInfo.size];
                    }
//                                 LogUtils.e("offerDecoder: bufferInfo.size = " + bufferInfo.size);
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                    outputBuffer.get(mRawData);
                    if (mOnDecoderListener != null) {
                        mOnDecoderListener.decoded(mRawData, bufferInfo.size, mWidth, mHeight);
                    }
                    mDecoder.releaseOutputBuffer(outputBufferIndex, true);

                    outputBufferIndex = mDecoder.dequeueOutputBuffer(bufferInfo, 0);
                }
//                        Log.e("1234", "333333333333333333");
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    public void release() {
        mRawData = null;
        try {
            if (mDecoder != null) {
                mDecoder.stop();
                mDecoder.release();
                mDecoder = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
