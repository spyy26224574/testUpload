
#include "HbxFishEyeParameters.h"
#ifndef _HBX_READFILE_INFO_H_
#define _HBX_READFILE_INFO_H_
class CHbxReadFileInfo
{
public:
	CHbxReadFileInfo();
	~CHbxReadFileInfo();
protected: 
	FILE *            m_fp;
	//
	int               FindTailSegment(unsigned char *buffer, int length);
	int               FindLigoInfo(unsigned char *buffer, int length);
	int               FindInfoSegment(unsigned char *buffer, int length, unsigned char type);
	//
	int               DecodecFEParame(CHbxFishEyeParameters *item, unsigned char *buffer);
public:
	//
	int               m_nFishEyeId;
	int               m_nFileType;
	int               m_nSn;
	//
	long long         GetFileLength();
	struct list_head  m_CalibrationList;
public:
	//
	void              Open(char *file);
	void              OpenMediaFile(char *file);
	//
};
#endif


