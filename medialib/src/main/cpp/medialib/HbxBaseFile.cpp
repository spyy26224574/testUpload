
#include "HbxBaseFile.h"

CHbxBaseFile::CHbxBaseFile() {
    m_pFC = NULL;
    m_nVideoStream = -1;
    m_nAudioStream = -1;

    m_vCodecContext = NULL;
    m_aCodecContext = NULL;
    m_pkt = NULL;
}


CHbxBaseFile::~CHbxBaseFile() {
    if (m_vCodecContext) {
        avcodec_free_context(&m_vCodecContext);
        m_vCodecContext = NULL;
    }
    if (m_aCodecContext) {
        avcodec_free_context(&m_aCodecContext);
        m_aCodecContext = NULL;
    }
}

int CHbxBaseFile::Seek(int seek) {
    int ret;
    if (!m_pFC)
        return 0;
    int defaultStreamIndex = av_find_default_stream_index(m_pFC);
    auto time_base = m_pFC->streams[defaultStreamIndex]->time_base;
    auto seekTime = av_rescale(seek, time_base.den, time_base.num);

    m_Mutex.Lock();
    if (seekTime > m_nCurrentDts) {
        ret = av_seek_frame(m_pFC, defaultStreamIndex, seekTime, AVSEEK_FLAG_ANY);
    } else {
        ret = av_seek_frame(m_pFC, defaultStreamIndex, seekTime,
                            AVSEEK_FLAG_ANY | AVSEEK_FLAG_BACKWARD);
    }
    m_Mutex.UnLock();
    return ret;
}

int CHbxBaseFile::ReadFrame(CHbxFrame *hbxFrame) {
    int nRet = 0;
    //
    if (!m_pFC || !hbxFrame)
        return -2;
    m_Mutex.Lock();
    try {
        //pkt
        if (!m_pkt)
            m_pkt = av_packet_alloc();

//		HBXLOG("start:  readframe...........................\r\n");
        nRet = av_read_frame(m_pFC, m_pkt);
        if (0 != nRet) {
            av_packet_unref(m_pkt);
            if (AVERROR_EOF == nRet) {
                HBXLOG("error:  read.end..........................\r\n");
                nRet = -2;
            } else
                nRet = -1;
            hbxFrame->m_pkt = NULL;
            m_Mutex.UnLock();
//			HBXLOG("error:  readframe...........................\r\n");
            return nRet;
        }
        //video
        hbxFrame->m_nPts =
                m_pkt->dts * 1000 * av_q2d(m_pFC->streams[m_pkt->stream_index]->time_base);
        //video read frame
        if (m_pkt->stream_index == m_nVideoStream) {
            m_nCurrentDts = hbxFrame->m_nPts;
            hbxFrame->m_nIndex = _VIDEO_INDEX_STREAM_;
            hbxFrame->m_pkt = av_packet_alloc();
            av_packet_ref(hbxFrame->m_pkt, m_pkt);
            av_packet_unref(m_pkt);

        } else {
            // audio decodec
            hbxFrame->m_nIndex = _AUDIO_INDEX_STREAM_;
            hbxFrame->m_avFrame = NULL;
            nRet = avcodec_send_packet(m_aCodecContext, m_pkt);
            if (nRet) {
                av_packet_unref(m_pkt);
                HBXLOG("avcodec_send_packet audio ........................ \r\n");
                hbxFrame->m_pkt = NULL;
                m_Mutex.UnLock();
                return -1;
            }

            AVFrame *avFrame = av_frame_alloc();
            if (avcodec_receive_frame(m_aCodecContext, avFrame) != 0)
                if (nRet) {
                    av_packet_unref(m_pkt);
                    av_frame_free(&avFrame);
                    hbxFrame->m_pkt = NULL;
                    HBXLOG("avcodec_receive_frame audio ...........................\r\n");
                    m_Mutex.UnLock();
                    return -1;
                }
            hbxFrame->m_avFrame = avFrame;
            av_packet_unref(m_pkt);
            hbxFrame->m_pkt = NULL;
        }
    }
    catch (exception &e) {
        m_Mutex.UnLock();
        CHbxInteractive api;
        api.UpdateMediaInfo("error:Decodec ReadFrame \r\n", _INFO_ERROR_TAG_);
//		HBXLOG("error:  readframe...........................\r\n");
        return -1;
    }
    m_Mutex.UnLock();
    return 0;
}

int CHbxBaseFile::SoftVideoDecodec(CHbxFrame *hbxFrame) {
    int nRet = 0;
    static int flag = 0;
    if (!hbxFrame)
        return -1;
    try {
        if (avcodec_send_packet(m_vCodecContext, hbxFrame->m_pkt) != 0) {
            av_packet_unref(hbxFrame->m_pkt);
//            HBXLOG("avcodec_send_packet video\r\n");
            av_packet_free(&hbxFrame->m_pkt);
            hbxFrame->m_pkt = NULL;
            flag = 1;
            return -1;
        }

        AVFrame *avFrame = av_frame_alloc();
        if (avcodec_receive_frame(m_vCodecContext, avFrame) != 0) {
            av_frame_free(&avFrame);
            av_packet_unref(hbxFrame->m_pkt);
            av_packet_free(&hbxFrame->m_pkt);
            hbxFrame->m_pkt = NULL;
            flag = 1;
            return -1;
        }

        if (flag && avFrame->key_frame) {
            flag = 0;
        }
        if (!flag) {
            hbxFrame->m_avFrame = avFrame;
            hbxFrame->m_nIndex = 0;
        } else
            av_frame_free(&avFrame);

        av_packet_unref(hbxFrame->m_pkt);
        av_packet_free(&hbxFrame->m_pkt);
        hbxFrame->m_pkt = NULL;
        avFrame = NULL;
    }
    catch (exception &e) {
        CHbxInteractive api;
        api.UpdateMediaInfo("error:Decode \r\n", _INFO_ERROR_TAG_);
        HBXLOG("error:Decode \r\n");
        return -1;
    }
    return nRet;
}

int CHbxBaseFile::Open(const char *path, struct MediaInfo *mediainfo) {
    int nRet = 0;
    m_Mutex.Lock();
    try {
        mediainfo->Init();
        m_nCurrentDts = 0;

        m_nVideoStream = -1;
        m_nAudioStream = -1;

        av_register_all();
        avformat_network_init();

        if (m_pFC)
            avformat_close_input(&m_pFC);

        m_pFC = NULL;
        nRet = avformat_open_input(&m_pFC, (const char *) path, NULL, NULL);
        if (nRet) {
            char *errbuf = new char[1024];
            av_strerror(nRet, errbuf, 1024);
            HBXLOG("avformat_open_input error path = %s ,err = %s,no=%d\r\n", path, errbuf, nRet);
            m_Mutex.UnLock();
            return -1;
        }
        nRet = avformat_find_stream_info(m_pFC, NULL);
        if (nRet) {
            HBXLOG("avformat_open_input error \r\n");
            m_Mutex.UnLock();
            return -1;
        }
        //
        if (m_vCodecContext) {
            avcodec_free_context(&m_vCodecContext);
            m_vCodecContext = NULL;
        }
        if (m_aCodecContext) {
            avcodec_free_context(&m_aCodecContext);
            m_aCodecContext = NULL;
        }
        for (int i = 0; i < m_pFC->nb_streams; i++) {
            AVCodecParameters *codecpar = m_pFC->streams[i]->codecpar;
            if (NULL == codecpar) {
                HBXLOG("avformat_open_input error \r\n");
                m_Mutex.UnLock();
                return -1;
            }
            AVCodec *codec = avcodec_find_decoder(codecpar->codec_id);
            if (NULL == codec) {
                HBXLOG("avformat_open_input error \r\n");
                m_Mutex.UnLock();
                return -1;
            }
            AVCodecContext *avCodecContext = avcodec_alloc_context3(codec);
            if (NULL == avCodecContext) {
                HBXLOG("avformat_open_input error \r\n");
                m_Mutex.UnLock();
                return -1;
            }
            avcodec_parameters_to_context(avCodecContext, m_pFC->streams[i]->codecpar);
            av_codec_set_pkt_timebase(avCodecContext, m_pFC->streams[i]->time_base);
            if (AVMEDIA_TYPE_VIDEO == m_pFC->streams[i]->codecpar->codec_type) {
#ifdef _IOS_
                avCodecContext->thread_count = 2;
                avCodecContext->active_thread_type = FF_THREAD_SLICE;
#endif // _IOS_

#ifdef _ANDROID_
                avCodecContext->thread_count = 4;
                avCodecContext->active_thread_type = FF_THREAD_SLICE;
#endif // _IOS_
            }
            nRet = avcodec_open2(avCodecContext, codec, NULL);
            if (nRet) {
                HBXLOG("avformat_open_input error \r\n");
                m_Mutex.UnLock();
                return -1;
            }
            if (AVMEDIA_TYPE_VIDEO == m_pFC->streams[i]->codecpar->codec_type) {
                m_nVideoStream = i;
                m_vCodecContext = avCodecContext;
                mediainfo->av_codec_id = m_vCodecContext->codec_id;
                mediainfo->first_pts = m_pFC->streams[i]->first_dts;
                mediainfo->frame_rate = 30;
                HBXLOG("mediainfo->av_codec_id =%d.......................%s. \r\n",
                       mediainfo->av_codec_id, m_vCodecContext->codec->name);
                HBXLOG(" m_pFC->streams[i]->avg_frame_rate.den =%d \r\n",
                       m_pFC->streams[i]->avg_frame_rate.den);
                if (m_pFC->streams[i]->avg_frame_rate.den > 0)
                    mediainfo->frame_rate = m_pFC->streams[i]->avg_frame_rate.num /
                                            m_pFC->streams[i]->avg_frame_rate.den;
            }
            if (AVMEDIA_TYPE_AUDIO == m_pFC->streams[i]->codecpar->codec_type) {
                m_nAudioStream = i;
                m_aCodecContext = avCodecContext;
                mediainfo->audio_codec_id = m_aCodecContext->codec_id;
                mediainfo->audio_sample_fmt = m_aCodecContext->sample_fmt;
                mediainfo->audio_sample_rate = m_aCodecContext->sample_rate;
                mediainfo->audio_ch_layout = m_aCodecContext->channel_layout;
                mediainfo->audio_channels = m_aCodecContext->channels;
            }
        }
        mediainfo->nDuration = (m_pFC->duration + 5000) / AV_TIME_BASE;
        if (-1 != m_nVideoStream) {
            //
            if (m_pFC->streams[m_nVideoStream]->codecpar->codec_id == AV_CODEC_ID_H264) {
                if (NULL != m_vCodecContext->extradata) {
                    int nSpsLength =
                            m_vCodecContext->extradata[6] * 256 + m_vCodecContext->extradata[7];
                    int nPpsLength = m_vCodecContext->extradata[nSpsLength + 9] * 256 +
                                     m_vCodecContext->extradata[nSpsLength + 10];


                    memset(mediainfo->sps, 0x00, 3);
                    mediainfo->sps[3] = 0x01;
                    memcpy(&mediainfo->sps[4], &m_vCodecContext->extradata[8], nSpsLength);

                    memset(&mediainfo->sps[nSpsLength + 4], 0x00, 3);
                    mediainfo->sps[nSpsLength + 4 + 3] = 0x01;
                    memcpy(&mediainfo->sps[nSpsLength + 8],
                           &m_vCodecContext->extradata[11 + nSpsLength], nPpsLength);
                    mediainfo->spsppslength = nSpsLength + nPpsLength + 8;
                    mediainfo->spslength = nSpsLength;
                    mediainfo->ppslength = nPpsLength;
                }
            }
            mediainfo->nWidth = m_vCodecContext->width;
            mediainfo->nHeight = m_vCodecContext->height;

        }
    }
    catch (exception &e) {
        m_Mutex.UnLock();
        CHbxInteractive api;
        api.UpdateMediaInfo("error:open  \r\n", _INFO_ERROR_TAG_);
        HBXLOG("error:open  \r\n");
        return -1;
    }
    m_Mutex.UnLock();
    return 0;
}

int CHbxBaseFile::Close() {
    int nRet = 0;
    m_Mutex.Lock();
    try {
        if (m_pkt)
            av_packet_free(&m_pkt);
        m_pkt = NULL;

        if (m_vCodecContext) {
            avcodec_free_context(&m_vCodecContext);
            m_vCodecContext = NULL;
        }
        if (m_aCodecContext) {
            avcodec_free_context(&m_aCodecContext);
            m_aCodecContext = NULL;
        }

        if (m_pFC)
            avformat_close_input(&m_pFC);
        m_pFC = NULL;
        HBXLOG("CHbxBaseFile:Close  \r\n");
    }
    catch (exception &e) {
        m_Mutex.UnLock();
        //	m_nStatus = _STATUS_READ_STOP_;
        HBXLOG("error:stop  \r\n");
        return -1;
    }
    m_Mutex.UnLock();
    return nRet;
}
