#include "HbxReadFileInfo.h"

static unsigned char nVersion = 101;

void inline _UCHAR_TO_FLOAT_(unsigned char *buffer, float *fData) {
    unsigned char *pdata = (unsigned char *) fData;
    //
    for (int i = 0; i < 4; i++) {
        pdata[i] = buffer[i];
    }
}


CHbxReadFileInfo::CHbxReadFileInfo() {
    list_init(&m_CalibrationList);
    m_fp = NULL;
    m_nSn = 0;
    m_nFishEyeId = 0;
    m_nFileType = 0;
}


CHbxReadFileInfo::~CHbxReadFileInfo() {
    if (m_fp)
        fclose(m_fp);
    m_fp = NULL;

}

long long CHbxReadFileInfo::GetFileLength() {
    long long nFileLength = 0L;
    if (m_fp) {
        fseek(m_fp, 0, SEEK_END);
        nFileLength = ftell(m_fp);
        fseek(m_fp, 0, SEEK_SET);
    }
    return nFileLength;
}

int CHbxReadFileInfo::FindLigoInfo(unsigned char *buffer, int length) {
    char tag[8];
    if (!buffer)
        return -1;

    for (int i = 0; i < (length - _DATAHEADER_MAX_); i += 1) {
        memcpy(tag, buffer + i, 8);
        if ((strstr(tag, "LIGOFILE") || strstr(tag, "LIGOGPS")) && tag[0] == 'L' && tag[1] == 'I') {
            return i;
        }
    }
    return -1;
}

int CHbxReadFileInfo::FindTailSegment(unsigned char *buffer, int length) {
    char tag[4];
    if (!buffer)
        return -1;

    for (int i = 0; i < (length); i += 1) {
        memcpy(tag, buffer + i, 4);
        if (strstr(tag, "LIGO") && tag[0] == 'L' && tag[1] == 'I') {
            return i;
        }
    }
    return -1;
}

int CHbxReadFileInfo::FindInfoSegment(unsigned char *buffer, int length, unsigned char type) {
    char tag[4];
    if (!buffer)
        return -1;
    //
    for (int i = 0; i < length; i += 1) {
        memcpy(tag, buffer + i, 4);
        if (strstr(tag, "####")) {
            if (buffer[i + 4] == type) {
                return i;
            }
        }
    }
    return -1;
}

int CHbxReadFileInfo::DecodecFEParame(CHbxFishEyeParameters *item, unsigned char *buffer) {
    int i = 0;
    if (!item || !buffer)
        return -1;
    // fish eye id 4byte
    _UCHAR_TO_UINT(buffer, item->m_nFishEyeId);
    // fWidth 4byte fHeight4byte
    _UCHAR_TO_FLOAT_(&buffer[4], &(item->m_ImageWidth));
    _UCHAR_TO_FLOAT_(&buffer[8], &(item->m_ImageHigh));
    // fCenterX，fCenterY 8byte
    _UCHAR_TO_FLOAT_(&buffer[12], &(item->m_CXMatlab));
    _UCHAR_TO_FLOAT_(&buffer[16], &(item->m_CYMatlab));
    //
    //fRadius 4byte
    _UCHAR_TO_FLOAT_(&buffer[20], &(item->m_R));
    // fStartAngle 4byte fEndAngle 4byte
    _UCHAR_TO_FLOAT_(&buffer[24], &(item->m_fStartAngle));
    _UCHAR_TO_FLOAT_(&buffer[28], &(item->m_fEndAngle));
    // nLength 4byte
    _UCHAR_TO_UINT((buffer + 32), item->m_KMatlabLen);
    item->m_KMatlab = new float[item->m_KMatlabLen];
    // n * 4 byte
    for (i = 0; i < item->m_KMatlabLen; i++) {
        _UCHAR_TO_FLOAT_(&buffer[36 + i * 4], &(item->m_KMatlab[i]));
    }
    return 0;
}

void CHbxReadFileInfo::Open(char *file) {
    int nRet = 0;
    int nGetBufferSize = 0;

    int nInfoLength = 0;
    unsigned char buffer[4];
    unsigned char *pInfobuffer = NULL;
    //
    int nPosIndex = 0;
    unsigned char *pLength = NULL;
    //
    long long filesize = 0;
    int pos = 0;
    unsigned char curVersion = 0;
    int nIndexCount = 0;

    if (!file)
        goto END;

    m_fp = fopen(file, "rb");
    if (!m_fp)
        goto END;
    //
    filesize = GetFileLength();
    //tail segmet
    fread(buffer, 1, 4, m_fp);
    _UCHAR_TO_UINT(buffer, nInfoLength);
    //
    if (nInfoLength > filesize)
        goto END;

    //
    pInfobuffer = (unsigned char *) malloc(nInfoLength + 1);
    if (!pInfobuffer) {
        goto END;
    }
    //ligo tag
    memset(pInfobuffer, 0, (nInfoLength + 1));
    fseek(m_fp, filesize - nInfoLength, SEEK_SET);
    fread(pInfobuffer, 1, nInfoLength, m_fp);
    //ligo tag
    pos = FindLigoInfo(pInfobuffer, nInfoLength);
    if (pos < 0)
        goto END;

    if (pInfobuffer[15 + pos] != 4)
        goto END;
    //
    nPosIndex = 0;
    pLength = &pInfobuffer[pos + 15 + 1];
    _UCHAR_TO_UINT(pLength, nPosIndex);
    pLength = NULL;
    //
    nGetBufferSize = FindInfoSegment(&pInfobuffer[pos + _DATAHEADER_MAX_], nInfoLength,
                                     TAG_VERSION);
    if (nGetBufferSize == -1) {
        //get type segment
        pos += FindInfoSegment(&pInfobuffer[pos + _DATAHEADER_MAX_], nInfoLength, TAG_FILETYPE);
        if (pos > -1) {
            nRet = pInfobuffer[pos + _DATAHEADER_MAX_ + 7] & 0xff;
            m_nFishEyeId = 0x01;//defult eyefish
        }
    } else {
        curVersion = pInfobuffer[pos + _DATAHEADER_MAX_ + 7];
        if (curVersion == 101) {
            //
            nIndexCount = (pInfobuffer[nPosIndex + 5] * 256 + pInfobuffer[nPosIndex + 6]) - 7;
            nIndexCount = nIndexCount / 5;

            unsigned char *pbuffer = &pInfobuffer[nPosIndex + 7];
            for (int i = 0; i < nIndexCount; i++) {
                unsigned IndexType = pbuffer[0];
                int nSegmentPos = 0;
                //
                _UCHAR_TO_UINT((&pbuffer[1]), nSegmentPos);
                //
                if (TAG_FILETYPE == IndexType) {
                    pos += FindInfoSegment(&pInfobuffer[nSegmentPos], 0x08, TAG_FILETYPE);
                    if (pos > -1) {
                        m_nFileType = pInfobuffer[nSegmentPos + 7] & 0xff;
                    }
                } else if (TAG_EYEFISHID == IndexType) {
                    pos += FindInfoSegment(&pInfobuffer[nSegmentPos], 0x0b, TAG_EYEFISHID);
                    if (pos > -1) {
                        _UCHAR_TO_UINT((&pInfobuffer[nSegmentPos + 7]), m_nFishEyeId);
                    }
                } else if (TAG_CALIBRATION == IndexType) {
                    pos += FindInfoSegment(&pInfobuffer[nSegmentPos], 0x0b, TAG_CALIBRATION);
                    if (pos > -1) {
                        CHbxFishEyeParameters *item = new CHbxFishEyeParameters(0);
                        DecodecFEParame(item, &pInfobuffer[nSegmentPos + 7]);
                        list_add_tail(&(item->next), &m_CalibrationList);
                    }
                } else if (TAG_SN == IndexType) {
                    pos += FindInfoSegment(&pInfobuffer[nSegmentPos], 0x0b, TAG_CALIBRATION);
                    if (pos > -1) {
                        _UCHAR_TO_UINT((&pInfobuffer[nSegmentPos + 7]), m_nSn);
                    }
                }
                pbuffer += 5;
            }
            pbuffer = NULL;
        }
    }
    END:
    if (m_fp)
        fclose(m_fp);
    m_fp = NULL;

    if (pInfobuffer)
        free((void *) pInfobuffer);
    pInfobuffer = NULL;
}

void CHbxReadFileInfo::OpenMediaFile(char *file) {
    int nRet = 0;
    int nGetBufferSize = 0;

    int nInfoLength = 0;
    unsigned char buffer[_DATATAIL_MAX_ + 1];
    unsigned char *pInfobuffer = NULL;
    //
    int nIndexCount = 0;
    long long filesize = 0;
    unsigned char *pLength = NULL;
    unsigned char curVersion = 0;
    int nPosIndex = 0;
    int pos = 0;

    if (!file)
        goto End;

    m_fp = fopen(file, "rb");
    if (!m_fp)
        goto End;
    //
    filesize = GetFileLength();
    //tail segmet
    memset(buffer, 0, _DATATAIL_MAX_);
    fseek(m_fp, filesize - _DATATAIL_MAX_, SEEK_SET);
    fread(buffer, 1, _DATATAIL_MAX_, m_fp);
    pos = FindTailSegment(buffer, _DATATAIL_MAX_);
    if (pos < 0)
        goto End;

    pLength = &buffer[pos + 4];
    _UCHAR_TO_UINT(pLength, nInfoLength);
    //
    if (nInfoLength >= filesize || nInfoLength > 0xa00000)
        goto End;

    //
    pInfobuffer = (unsigned char *) malloc(nInfoLength + 1);
    if (!pInfobuffer) {
        goto End;
    }
    //ligo tag
    memset(pInfobuffer, 0, (nInfoLength + 1));
    fseek(m_fp, filesize - nInfoLength, SEEK_SET);
    fread(pInfobuffer, 1, nInfoLength, m_fp);
    //ligo tag
    pos = FindLigoInfo(pInfobuffer, nInfoLength);
    if (pos < 0)
        goto End;

    if (pInfobuffer[15 + pos] != 4)
        goto End;
    //

    pLength = &pInfobuffer[pos + 15 + 1];
    _UCHAR_TO_UINT(pLength, nPosIndex);
    pLength = NULL;
    //
    nGetBufferSize = FindInfoSegment(&pInfobuffer[pos + _DATAHEADER_MAX_], nInfoLength,
                                     TAG_VERSION);
    if (nGetBufferSize == -1) {
        //get type segment
        pos += FindInfoSegment(&pInfobuffer[pos + _DATAHEADER_MAX_], nInfoLength, TAG_FILETYPE);
        if (pos > -1) {
            nRet = pInfobuffer[pos + _DATAHEADER_MAX_ + 7] & 0xff;
            m_nFishEyeId = 0x01;//defult eyefish
        }
    } else {
        curVersion = pInfobuffer[pos + _DATAHEADER_MAX_ + 7];
        if (curVersion >= 101) {
            //获取索引
            nIndexCount = (pInfobuffer[nPosIndex + 5] * 256 + pInfobuffer[nPosIndex + 6]) - 7;
            nIndexCount = nIndexCount / 5;

            unsigned char *pbuffer = &pInfobuffer[nPosIndex + 7];
            for (int i = 0; i < nIndexCount; i++) {
                unsigned IndexType = pbuffer[0];
                int nSegmentPos = 0;
                //
                _UCHAR_TO_UINT((&pbuffer[1]), nSegmentPos);
                //
                if (TAG_FILETYPE == IndexType) {
                    pos += FindInfoSegment(&pInfobuffer[nSegmentPos], 0x08, TAG_FILETYPE);
                    if (pos > -1) {
                        m_nFileType = pInfobuffer[nSegmentPos + 7] & 0xff;
                    }
                } else if (TAG_EYEFISHID == IndexType) {
                    pos += FindInfoSegment(&pInfobuffer[nSegmentPos], 0x0b, TAG_EYEFISHID);
                    if (pos > -1) {
                        _UCHAR_TO_UINT((&pInfobuffer[nSegmentPos + 7]), m_nFishEyeId);
                    }
                }
                pbuffer += 5;
            }
            pbuffer = NULL;
        }
    }
    End:
    if (m_fp)
        fclose(m_fp);
    m_fp = NULL;

    if (pInfobuffer)
        free((void *) pInfobuffer);
    pInfobuffer = NULL;
}
//