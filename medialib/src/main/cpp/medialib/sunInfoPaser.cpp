//
//  sunGpsPaser.c
//  SunGps
//
//  Created by bobobobobo on 2017/12/11.
//  Copyright © 2017年 bobobobobo. All rights reserved.
//
#include "sunInfoPaser.h"
#define _DATAHEADER_MAX_ 20
#define _DATATAIL_MAX_   15


#define  _UCHAR_TO_UINT(chData,value) { \
value = chData[0]; \
value = (value << 8) + chData[1]; \
value = (value << 8) + chData[2]; \
value = (value << 8)  + chData[3]; \
}

static int FindLigoInfo(unsigned char *buffer,int length)
{
    char  tag[8];
    if (!buffer)
        return -1;
    
    for (int i = 0; i < (length - _DATAHEADER_MAX_); i +=1)
    {
        memcpy(tag,buffer + i,8);
        if (strstr(tag, "LIGOGPS") && tag[0] == 'L' && tag[1] == 'I' )
        {
            return i;
        }
    }
    return -1;
}
static int FindInfoSegment(unsigned char *buffer, int length,unsigned char type)
{
	char  tag[4];
	if (!buffer)
		return -1;
	//
	for (int i = 0; i < length ; i += 1)
	{
		memcpy(tag, buffer + i, 4);
		if (strstr(tag, "####"))
		{
			if (buffer[i + 4] == type)
			{
				return i;
			}
		}
	}
	return -1;
}

static int FindTailSegment(unsigned char *buffer, int length)
{
	char  tag[4];
	if (!buffer)
		return -1;

	for (int i = 0; i < (length); i += 1)
	{
		memcpy(tag, buffer + i, 4);
		if (strstr(tag, "LIGO") && tag[0] == 'L' && tag[1] == 'I')
		{
			return i;
		}
	}
	return -1;
}

static long long GetFileLength(FILE *fp)
{
	long long  nFileLength =0L ;
	fseek(fp, 0, SEEK_END);
	nFileLength = ftell(fp);
	fseek(fp, 0, SEEK_SET);
    return nFileLength;
}

//////////////
//get type by file  
//////////////
int sunGetInfoType(const char *file)
{
	int nRet = 0;
	FILE * fp = NULL;
	int    nInfoLength = 0;
	unsigned char  buffer[128];
	if (!file)
		return 0;
	fp = fopen(file, "rb");
	if (!fp)
		return 0;
	//
	long long filesize = GetFileLength(fp);
	//tail segmet 
	memset(buffer,0,128);
	fseek(fp, filesize - _DATATAIL_MAX_,SEEK_SET);
	fread(buffer,1, _DATATAIL_MAX_,fp);
	int pos = FindTailSegment(buffer, _DATATAIL_MAX_);
	if (pos < 0)
	{
		fclose(fp);
		return 0;
	}
	char *pLength = (char *)&buffer[pos + 4];
	_UCHAR_TO_UINT(pLength, nInfoLength);
	if (nInfoLength > 128)
		return 0;
	//ligo tag 
	memset(buffer, 0, 128);
	fseek(fp, filesize - nInfoLength, SEEK_SET);
	fread(buffer, 1, nInfoLength, fp);
	fclose(fp);
	pos = FindLigoInfo(buffer, nInfoLength);
	if (pos < 0)
		return 0;
	if (buffer[15+pos] != 4)
		return 0;
	//get type segment 
	pos += FindInfoSegment(&buffer[pos + _DATAHEADER_MAX_], nInfoLength,0x01);
	if (pos > -1)
	{
		nRet = buffer[pos + _DATAHEADER_MAX_ + 7] & 0xff;
		return nRet;
	}
	return 0;
}
//
int  MakeInfoHeader(char *output, int type,int count)
{
	if (!output)
		return 0;
	sprintf(output, "LIGOGPSINFO");
	output[15] = type;
	output[16] = count & 0xff;
	output[17] =( count >>8) & 0xff;
	output[18] = (count >> 16) & 0xff;
	output[19] = (count >> 24) & 0xff;

	return _DATAHEADER_MAX_;
}
//
int  MakeInfoTail(unsigned char *output,int length)
{
	int nRet = 0;
	if (!output)
		return 0;
	char *data = (char *)output;
	//header tag
	sprintf(data, "####");
	nRet += 4;
	//type
	output[nRet] = 0x0;
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

int  MakeInfoType(unsigned char *output, int type)
{
	int nRet = 0;
	if (!output)
		return 0;
	char *data = (char *)output;
	//header tag
	sprintf(data, "####");
	nRet += 4;
	//type
	output[nRet] = 0x01;
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
//////////////
//set file type    
//////////////
void sunSetInfoType(const char *file, int type)
{
	int nRet = 0;
	FILE * fp = NULL;
	unsigned char  buffer[128];
	if (!file)
		return;
	fp = fopen(file,"ab+");
	if (!fp)
		return;
	//////////////////////
	memset(buffer,0,128);
	//segment 
	buffer[0] = 0;
	buffer[1] = 0;
	buffer[2] = 0;
	buffer[3] = 64;
	buffer[4] = 'L';
	buffer[5] = 'I';
	buffer[6] = 'G';
	buffer[7] = 'O';
	nRet = 8;
	//data header
	nRet += MakeInfoHeader((char *)&buffer[8], 4,2);
	//
	nRet += MakeInfoType(&buffer[8 + _DATAHEADER_MAX_], type);
	//
	nRet += MakeInfoTail(&buffer[nRet], (nRet + _DATATAIL_MAX_));
	//
	fseek(fp,0,SEEK_END);
	fwrite(buffer, 1, nRet, fp);
	fclose(fp);
	fp = NULL;
}



