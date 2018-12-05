#include "HbxWriteFileInfo.h"
static unsigned char  nVersion = 101;

void inline _FLOAT_TO_UCHAR_(unsigned char *buffer, float *fData)
{
	unsigned char* pdata = (unsigned char *)fData;
	//
	for (int i = 0; i<4; i++)
	{
		buffer[i] = pdata[i];//把相应地址中的数据保存到unsigned char数组中       
	}
}

CHbxWriteFileInfo::CHbxWriteFileInfo()
{
	m_fp = NULL;
	m_nSize =0;
	m_nIndexPos =0;

	m_nIndex = NULL;
	m_nIndexCount = 0;

	m_pBuffer = NULL;
	m_nMax = 4086;
}


CHbxWriteFileInfo::~CHbxWriteFileInfo()
{
	if (m_pBuffer)
		delete m_pBuffer;
	m_pBuffer = NULL;

	if (m_nIndex)
		delete m_nIndex;
	m_nIndex = NULL;

	if (m_fp)
		fclose(m_fp);
	m_fp = NULL;
}

int  CHbxWriteFileInfo::MakeInfoHeader(char *output, int type, int count)
{
	if (!output)
		return 0;
	sprintf(output, "LIGOFILEINFO");
	output[15] = type;
	output[19] = count & 0xff;
	output[18] = (count >> 8) & 0xff;
	output[17] = (count >> 16) & 0xff;
	output[16] = (count >> 24) & 0xff;

	return _DATAHEADER_MAX_;
}

int  CHbxWriteFileInfo::MakeInfoTail(unsigned char *output, int length)
{
	int nRet = 0;
	if (!output)
		return 0;
	char *data = (char *)output;
	//header tag
	sprintf(data, "####");
	nRet += 4;
	//type
	output[nRet] = TAG_TAIL;
	nRet++;
	//length
	output[nRet] = 0x0;
	nRet++;
	output[nRet] = 0x0f;
	nRet++;
	//information
	data = (char *)&output[nRet];
	sprintf(data, "LIGO");
	nRet += 4;
	output[nRet] = (length >> 24) & 0xff;
	nRet++;
	output[nRet] = (length >> 16) & 0xff;
	nRet++;
	output[nRet] = (length >> 8) & 0xff;
	nRet++;
	output[nRet] = length & 0xff;
	nRet++;
	return _DATATAIL_MAX_;
}

int  CHbxWriteFileInfo::MakeInfoType(unsigned char *output, int type)
{
	int nRet = 0;
	if (!output)
		return 0;
	char *data = (char *)output;
	//header tag
	sprintf(data, "####");
	nRet += 4;
	//type
	output[nRet] = TAG_FILETYPE;
	nRet++;
	//length
	output[nRet] = 0x0;
	nRet++;
	output[nRet] = 0x08;
	nRet++;
	//information
	output[nRet] = type & 0xff;
	nRet++;
	return nRet;
}

int  CHbxWriteFileInfo::MakeSn(unsigned char *output, int nSn)
{
	int nRet = 0;
	if (!output)
		return 0;
	char *data = (char *)output;
	//header tag
	sprintf(data, "####");
	nRet += 4;
	//type
	output[nRet++] = TAG_SN;
	//length
	output[nRet++] = 0x0;
	output[nRet++] = 0x0b;
	//information
	UINT_TO_UCHAR_((output+nRet), nSn);
//	output[nRet++] = (nSn >> 24) & 0xff;
//	output[nRet++] = (nSn >> 16) & 0xff;
//	output[nRet++] = (nSn >> 8) & 0xff;
//	output[nRet++] = nSn & 0xff;
	nRet += 4;
	//
	return nRet;
}
int CHbxWriteFileInfo::MakeFishEyeInfo(unsigned char *output, int eyefishid)
{
	int nRet = 0;
	if (!output)
		return 0;
	char *data = (char *)output;
	//header tag
	sprintf(data, "####");
	nRet += 4;
	//type
	output[nRet] = TAG_EYEFISHID;
	nRet++;
	//length
	output[nRet] = 0x0;
	nRet++;
	output[nRet] = 0x0b;
	nRet++;
	//information
	output[nRet] = (eyefishid >> 24) & 0xff;
	nRet++;
	output[nRet] = (eyefishid >> 16) & 0xff;
	nRet++;
	output[nRet] = (eyefishid >> 8) & 0xff;
	nRet++;
	output[nRet] = eyefishid & 0xff;
	nRet++;
	return nRet;
}

int CHbxWriteFileInfo::MakeProtocolVersion(unsigned char *output)
{
	int nRet = 0;
	if (!output)
		return 0;
	char *data = (char *)output;
	//header tag
	sprintf(data, "####");
	nRet += 4;
	//type
	output[nRet] = TAG_VERSION;;
	nRet++;
	//length
	output[nRet] = 0x0;
	nRet++;
	output[nRet] = 0x0a;
	nRet++;
	//information
	output[nRet] = nVersion & 0xff;
	nRet++;
	return nRet;
}

int CHbxWriteFileInfo::MakeSegIndexInfo(unsigned char *output, struct _HBX_FILE_INFO_INDEX_ *info, int infosize)
{
	int nRet = 0;
	if (!output)
		return 0;
	char *data = (char *)output;
	//header tag
	sprintf(data, "####");
	nRet += 4;
	//type
	output[nRet] = TAG_INDEX;
	nRet++;
	//length
	output[nRet] = 0x0;
	nRet++;
	output[nRet] = 7 + infosize * 5;
	nRet++;
	//information
	for (int i = 0; i < infosize; i++)
	{
		output[nRet] = info[i].nIndexType;
		nRet++;
		memcpy(&output[nRet], info[i].nPos, 4);
		nRet += 4;
	}
	return nRet;
}

//
void CHbxWriteFileInfo::AllocBigMem(int size)
{
	if ((size) >= m_nMax)
	{
		unsigned char * pBuffer = new unsigned char[m_nMax * 2];
		m_nMax = m_nMax * 2;
		memcpy(pBuffer, m_pBuffer, m_nSize);
		delete m_pBuffer;
		m_pBuffer = pBuffer;
		pBuffer = NULL;
	}
}


void CHbxWriteFileInfo::Open(char *file)
{
	m_nSize = 0;
	m_nIndexPos = 0;
	m_nIndex = new struct _HBX_FILE_INFO_INDEX_[256];

	if(!m_pBuffer)
		m_pBuffer = new unsigned char[m_nMax];
	//
	memset(m_pBuffer, 0, m_nMax);
    //
	if (!file)
		return;
	m_fp = fopen(file, "ab+");
	if (!m_fp)
		return;

	//segment header
	m_pBuffer[0] = 0;
	m_pBuffer[1] = 0;
	m_pBuffer[2] = 0;
	m_pBuffer[3] = 64;
	m_pBuffer[4] = 'L';
	m_pBuffer[5] = 'I';
	m_pBuffer[6] = 'G';
	m_pBuffer[7] = 'O';
	m_nSize = 8;
	//
	//data header
	m_nSize += MakeInfoHeader((char *)&m_pBuffer[m_nSize], 4, 2);
	m_nIndex[m_nIndexCount].nIndexType = TAG_VERSION;
	UINT_TO_UCHAR_(m_nIndex[m_nIndexCount].nPos, m_nSize);
	m_nSize += MakeProtocolVersion(&m_pBuffer[m_nSize]);
	m_nIndexCount ++;
}

void CHbxWriteFileInfo::AddCalibrationBlock(CHbxFishEyeParameters *parame)
{
	//
	int nRet = 0;
	int nLength = parame->m_KMatlabLen * 4;
	nLength += (20 + 4 + 4 + 4 + 8 + 4 + 4 + 4 + 4);
	nLength += 7;
	//
	AllocBigMem((m_nSize + nLength));
	//
	char *data = (char *)&m_pBuffer[m_nSize];
	//header tag
	sprintf(data, "####");
	nRet += 4;
	//type
	data[nRet++] = TAG_CALIBRATION;;
	//length
	data[nRet++] = nLength / 256;
	data[nRet++] = nLength % 256;
	//information
	int i = 0;
	unsigned char *buffer = (unsigned char *)&data[nRet];
	// fish eye id 4byte
	UINT_TO_UCHAR_(buffer, parame->m_nFishEyeId);
	// fWidth 4byte fHeight4byte
	_FLOAT_TO_UCHAR_(&buffer[4], &(parame->m_ImageWidth));
	_FLOAT_TO_UCHAR_(&buffer[8], &(parame->m_ImageHigh));
	// fCenterX，fCenterY 8byte
	_FLOAT_TO_UCHAR_(&buffer[12], &(parame->m_CXMatlab));
	_FLOAT_TO_UCHAR_(&buffer[16], &(parame->m_CYMatlab));
	//
	//fRadius 4byte
	_FLOAT_TO_UCHAR_(&buffer[20], &(parame->m_R));
	// fStartAngle 4byte fEndAngle 4byte 
	_FLOAT_TO_UCHAR_(&buffer[24], &(parame->m_fStartAngle));
	_FLOAT_TO_UCHAR_(&buffer[28], &(parame->m_fEndAngle));
	// nLength 4byte
	UINT_TO_UCHAR_((buffer + 32), parame->m_KMatlabLen);
	// n * 4 byte
	for (i = 0; i < parame->m_KMatlabLen; i++)
	{
		_FLOAT_TO_UCHAR_(&buffer[36 + i * 4], &(parame->m_KMatlab[i]));
	}

	m_nIndex[m_nIndexCount].nIndexType = TAG_CALIBRATION;
	UINT_TO_UCHAR_(m_nIndex[m_nIndexCount].nPos, m_nSize);
	m_nIndexCount++;
	m_nSize += nLength;
}

void CHbxWriteFileInfo::AddFishEyeIDBlock(int fisheye)
{
	int nLength = 0x0b;
	AllocBigMem((nLength + m_nSize));
	m_nIndex[m_nIndexCount].nIndexType = TAG_EYEFISHID;
	UINT_TO_UCHAR_(m_nIndex[m_nIndexCount].nPos, m_nSize);
	m_nSize += MakeFishEyeInfo(&m_pBuffer[m_nSize], fisheye);
	m_nIndexCount++;
}

void CHbxWriteFileInfo::AddFileTypeBlock(int fisheye)
{
	int nLength = 0x0b;
	AllocBigMem((nLength + m_nSize));
	m_nIndex[m_nIndexCount].nIndexType = TAG_FILETYPE;
	UINT_TO_UCHAR_(m_nIndex[m_nIndexCount].nPos, m_nSize);
	m_nSize += MakeInfoType(&m_pBuffer[m_nSize], fisheye);
	m_nIndexCount++;
}

void CHbxWriteFileInfo::AddSnBlock(int nSn)
{
	int nLength = 0x0b;
	AllocBigMem((nLength + m_nSize));
	m_nIndex[m_nIndexCount].nIndexType = TAG_SN;
	UINT_TO_UCHAR_(m_nIndex[m_nIndexCount].nPos, m_nSize);
	m_nSize += MakeSn(&m_pBuffer[m_nSize], nSn);
	m_nIndexCount++;
}

void CHbxWriteFileInfo::AddTailBlock()
{
	int nLength = 0x0b;
	AllocBigMem((nLength + m_nSize));
	m_nIndex[m_nIndexCount].nIndexType = TAG_TAIL;
	UINT_TO_UCHAR_(m_nIndex[m_nIndexCount].nPos, m_nSize);
	m_nSize += MakeInfoTail(&m_pBuffer[m_nSize],(m_nSize + 15));
	m_nIndexCount++;
}

void  CHbxWriteFileInfo::AddIndexBlock()
{
	int nLength = 7 + sizeof(struct _HBX_FILE_INFO_INDEX_ ) * m_nIndexCount;
	AllocBigMem((nLength + m_nSize));
	//
	MakeInfoHeader((char *)&m_pBuffer[8], 4, m_nSize);
	m_nSize += MakeSegIndexInfo(&m_pBuffer[m_nSize], m_nIndex, m_nIndexCount);
}

void CHbxWriteFileInfo::Close()
{
	if (m_fp) {
		UINT_TO_UCHAR_(m_pBuffer, m_nSize);
		fseek(m_fp, 0, SEEK_END);
		fwrite(m_pBuffer, 1, m_nSize, m_fp);
		fclose(m_fp);
	}
	m_fp = NULL;
}
