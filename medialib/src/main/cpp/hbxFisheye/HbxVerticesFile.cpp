#include "HbxVerticesFile.h"

CHbxVerticesFile::CHbxVerticesFile()
{
}

CHbxVerticesFile::~CHbxVerticesFile()
{
}

bool CHbxVerticesFile::Access(char *file)
{
	unsigned char buffer[4];
	FILE *fp = fopen(file, "rb");
	if(!fp)
		return false;
	//
	int  nFileLength = 0L;
	if (fp) {
		fseek(fp, 0, SEEK_END);
		nFileLength = ftell(fp);
		fseek(fp, 0, SEEK_SET);
	}
	//
	int  nSize;
	fread(buffer, 1, 4, fp);
	_UCHAR_TO_UINT(buffer, nSize);
	if (nSize <= nFileLength / 4 && nSize >4 )
		return true;
	return false;
}

bool    CHbxVerticesFile::Open(char *file,char *mode)
{
	m_fp = fopen(file, mode);
	if (m_fp)
		return true;
	return false;
}

bool    CHbxVerticesFile::Open(char *file)
{
	return Open(file,"wb");
}

void    CHbxVerticesFile::Write(float* data, int nsize)
{
	unsigned char buffer[4];
	//
	if (m_fp && data) {
		//
		buffer[3] = nsize & 0xff;
		buffer[2] = (nsize >> 8) & 0xff;
		buffer[1] = (nsize >> 16) & 0xff;
		buffer[0] = (nsize >> 24) & 0xff;
		//
		fwrite(buffer, 1, 4, m_fp);
		//
		fwrite(data, 4, nsize, m_fp);
		fclose(m_fp);
	}
}

void    CHbxVerticesFile::Read(float ** data, int &nsize)
{
	unsigned char buffer[4];
	if (!m_fp || !data)
		return;
	fread(buffer, 1, 4, m_fp);
	_UCHAR_TO_UINT(buffer, nsize);
	//
	*data = new float[nsize];
	fread(*data, sizeof(float), nsize, m_fp);
}

void    CHbxVerticesFile::Close()
{
	if (m_fp)
	{
		fclose(m_fp);
		m_fp = NULL;
	}
}