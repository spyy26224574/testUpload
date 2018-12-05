#pragma once
#include "HbxReadFileInfo.h"

#ifndef  _HBX_WRITER_FILEINFO_H_
#define  _HBX_WRITER_FILEINFO_H_

class CHbxWriteFileInfo
{
public:
	CHbxWriteFileInfo();
	~CHbxWriteFileInfo();
protected:
	FILE *            m_fp;
	int               m_nSize;
	int               m_nIndexPos;

	unsigned  char *  m_pBuffer;
	int               m_nMax;

	struct _HBX_FILE_INFO_INDEX_ *m_nIndex;
	int                           m_nIndexCount;
	//
	int  MakeInfoHeader(char *output, int type, int count);
	int  MakeInfoTail(unsigned char *output, int length);
	int  MakeInfoType(unsigned char *output, int type);
	int  MakeFishEyeInfo(unsigned char *output, int eyefishid);
	int  MakeProtocolVersion(unsigned char *output);
	int  MakeSegIndexInfo(unsigned char *output, \
		 struct _HBX_FILE_INFO_INDEX_ *info, int infosize);
	int  MakeSn(unsigned char *output, int nSn);


	void AllocBigMem(int size);
public:
	//
	void Open(char *file);
	void AddCalibrationBlock(CHbxFishEyeParameters *parame);
	void AddFishEyeIDBlock(int fisheye);
	void AddFileTypeBlock(int fisheye);
	void AddTailBlock();
	void AddIndexBlock();
	void AddSnBlock(int nSn);
	void Close();
	//
//	void CloseMediaFile();
};
#endif // ! _HBX_WRITER_FILEINFO_H_
