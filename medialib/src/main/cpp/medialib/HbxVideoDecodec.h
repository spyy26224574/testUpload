
#include "HbxThread.h"
#include "HbxBaseFile.h"
#include "HbxListFrame.h"

#ifndef  _SOFT_DECODE_H_
#define  _SOFT_DECODE_H_
//
class CHbxVideoDecodec : public CHbxThread
{
public:
	CHbxVideoDecodec();
	~CHbxVideoDecodec();
public:
	CHbxBaseFile  *m_pMediaFile;

	int            OnVideoDecodec();
	void           SetDecodecType(int type) { m_bSoftDecodec = type; };
	int            GetDecodecType() { return m_bSoftDecodec; };
	//
	CHbxListFrame  *m_ListFrame;
	//
	CHbxFrame     *GetFrame();
	virtual  void  CreatThread();
	virtual  void  Run();
protected:
	int     	   m_bSoftDecodec;
};
#endif
