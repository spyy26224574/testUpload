
#include "HbxAudioPlay.h"

ACALLBACK CHbxAudioPlay::m_aCallBack = NULL;

CHbxAudioPlay::CHbxAudioPlay() {
}


CHbxAudioPlay::~CHbxAudioPlay() {
}


void CHbxAudioPlay::Start(CHbxBaseFile *mediafile) {
    m_pMediaFile = mediafile;
    if (m_pMediaFile->AudioContext()) {
        m_nOutChannels = m_pMediaFile->AudioContext()->channels;
        m_nOutSamplesPerSec = m_pMediaFile->AudioContext()->sample_rate;
    }
}

void CHbxAudioPlay::RaiseVolume(char *buf, int size, uint32_t uRepeat, double vol) {
    if (!size)
        return;

    for (int i = 0; i < size; i += 2) {
        short wData;
        wData = buf[i + 1] & 0xff;
        wData <<= 8;
        wData += buf[i] & 0xff;


        long dwData = wData;
        for (int j = 0; j < uRepeat; j++) {
            dwData = dwData * vol;
            if (dwData < -0x8000) {
                dwData = -0x8000;
            } else if (dwData > 0x7FFF) {
                dwData = 0x7FFF;
            }
        }
        wData = dwData & 0xffff;
        buf[i] = dwData & 0x00ff;
        buf[i + 1] = (dwData & 0xff00) >> 8;
    }
}

int CHbxAudioPlay::AVFrameToAudio(AVCodecContext *audio_dec_ctx, AVFrame *pAudioDecodeFrame,
                                  unsigned char *buffer, int &len) {
    SwrContext *swr_ctx = NULL;
    int data_size = 0;
    int ret = 0;
    int64_t src_ch_layout = AV_CH_LAYOUT_STEREO;
    int64_t dst_ch_layout = AV_CH_LAYOUT_STEREO;
    int dst_nb_channels = 0;
    int dst_linesize = 0;
    int src_nb_samples = 0;
    int dst_nb_samples = 0;
    int max_dst_nb_samples = 0;
    uint8_t **dst_data = NULL;
    int resampled_data_size = 0;

    enum AVSampleFormat out_sample_fmt = AVSampleFormat::AV_SAMPLE_FMT_S16;
    int out_channels = m_nOutChannels;
    int out_sample_rate = m_nOutSamplesPerSec;


    src_ch_layout = (pAudioDecodeFrame->channel_layout &&
                     pAudioDecodeFrame->channels ==
                     av_get_channel_layout_nb_channels(pAudioDecodeFrame->channel_layout)) ?
                    pAudioDecodeFrame->channel_layout :
                    av_get_default_channel_layout(pAudioDecodeFrame->channels);

    if (out_channels == 1) {
        dst_ch_layout = src_ch_layout;
        out_channels = pAudioDecodeFrame->channels;
        out_sample_rate = pAudioDecodeFrame->sample_rate;
    } else if (out_channels == 2) {
        dst_ch_layout = AV_CH_LAYOUT_STEREO;
    }

    if (src_ch_layout <= 0)
        return -1;

    src_nb_samples = pAudioDecodeFrame->nb_samples;
    if (src_nb_samples <= 0)
        return -1;

    swr_ctx = swr_alloc_set_opts(NULL, //AV_CH_LAYOUT_MONO,
                                 dst_ch_layout,           //Êä³öÍ¨µÀ²¼¾Ö
                                 out_sample_fmt,
                                 out_sample_rate,
                                 src_ch_layout, //3 4 AV_CH_LAYOUT_MONO
                                 audio_dec_ctx->sample_fmt, //6  8 AV_SAMPLE_FMT_FLTP
                                 pAudioDecodeFrame->sample_rate, //44100 16000
                                 0, NULL);

    if (!swr_ctx)
        return -1;

    swr_init(swr_ctx);
    max_dst_nb_samples = dst_nb_samples =
            av_rescale_rnd(src_nb_samples, out_sample_rate, pAudioDecodeFrame->sample_rate,
                           AV_ROUND_INF);

    if (max_dst_nb_samples <= 0)
        return -1;

    dst_nb_channels = av_get_channel_layout_nb_channels(dst_ch_layout);
    ret = av_samples_alloc_array_and_samples(&dst_data, &dst_linesize, dst_nb_channels,
                                             dst_nb_samples, (AVSampleFormat) out_sample_fmt, 0);
    if (ret < 0)
        return -1;


    dst_nb_samples = av_rescale_rnd(swr_get_delay(swr_ctx, pAudioDecodeFrame->sample_rate) +
                                    src_nb_samples, out_sample_rate, pAudioDecodeFrame->sample_rate,
                                    AV_ROUND_INF);
    if (dst_nb_samples <= 0)
        return -1;

    if (dst_nb_samples > max_dst_nb_samples) {
        av_free(dst_data[0]);
        ret = av_samples_alloc(dst_data, &dst_linesize, dst_nb_channels,
                               dst_nb_samples, (AVSampleFormat) out_sample_fmt, 0);
        max_dst_nb_samples = dst_nb_samples;
    }

    data_size = av_samples_get_buffer_size(NULL, pAudioDecodeFrame->channels,
                                           pAudioDecodeFrame->nb_samples, audio_dec_ctx->sample_fmt,
                                           0);

    if (data_size <= 0)
        return -1;

    resampled_data_size = data_size;

    if (swr_ctx) {
        ret = swr_convert(swr_ctx, dst_data, dst_nb_samples,
                          (const uint8_t **) pAudioDecodeFrame->data,
                          pAudioDecodeFrame->nb_samples);
        if (ret <= 0)
            return -1;

        resampled_data_size = av_samples_get_buffer_size(&dst_linesize, dst_nb_channels,
                                                         ret, (AVSampleFormat) out_sample_fmt, 0);

        len = resampled_data_size;
        if (resampled_data_size <= 0) {
            return -1;
        }
    } else
        return -1;

    if (dst_data) {
        memcpy(buffer, dst_data[0], resampled_data_size);
        av_freep(&dst_data[0]);
    }
    av_freep(&dst_data);
    dst_data = NULL;

    if (swr_ctx)
        swr_free(&swr_ctx);

    return 0;
}