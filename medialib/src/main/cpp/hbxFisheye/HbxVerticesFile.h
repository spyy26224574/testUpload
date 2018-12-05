#include "HbxReadFileInfo.h"
#ifndef _HBX_VERTICES_FILE_H_
#define _HBX_VERTICES_FILE_H_
class CHbxVerticesFile
{
public:
	CHbxVerticesFile();
	~CHbxVerticesFile();
	FILE   *m_fp;

public:
	bool    Access(char *file);

	bool    Open(char *file, char *mode);
	bool    Open(char *file);
	void    Write(float * data,int size);
	void    Read(float** data, int &size);
	void    Close();
};
#endif
