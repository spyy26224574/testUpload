//
// Created by admin on 2016/11/5.
//

#ifndef FFMPEGPLAYER_FFMPEGPLAYER_H
#define FFMPEGPLAYER_FFMPEGPLAYER_H


#include "public.h"
#include "HbxFrame.h"
#include "HbxBaseFile.h"
#include "HbxThread.h"

using namespace std;

class CFFmpeg
{
public:
	CFFmpeg();
	~CFFmpeg();

protected:
	//
	CHbxBaseFile * m_pMediaFile;
	//
	void DeleteBitmap();
    list<CHbxFrame*> m_Bitmap;

	int    m_nStatus;
	int    m_nSeek;

	bool    m_bWaitting;
	bool    m_bHaveCmd;
	int     m_nThreadExit;
public:
	int Open(char * path);
	int Seek(int seek);
	int Stop();
	int Status();
	int Duration();
	int Current();
	int Pause();
	int Play();
    int GetSpsPps(unsigned char  ** sps);
	void SetDecodeType(int type);
    CHbxFrame* GetRawBitmap();
public:
	//
	CHbxFrame* GetBitmap();
	void  PopBitmap();
	int   OnReadFrame();
	void  GetWidthAndHigh(int & width, int &high);
};




#endif //FFMPEGPLAYER_FFMPEGPLAYER_H
