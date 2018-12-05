//
//  sunGpsPaser.h
//  SunGps
//
//  Created by bobobobobo on 2017/12/11.
//  Copyright © 2017年 bobobobobo. All rights reserved.
//
#include <stdio.h>
#include <string.h>
#include <stdlib.h>


#ifndef sunInfoPaser_h
#define sunInfoPaser_h

//
int  MakeInfoTail(unsigned char *output, int length);
//
int  MakeInfoHeader(char *output, int type, int count);
//
int  MakeInfoType(unsigned char *output, int type);
//////////////
//get type by file  
//////////////
int sunGetInfoType(const char *file);
//////////////
//set file type    
//////////////
void sunSetInfoType(const char *file,int type);

#endif /* sunGpsPaser_h */
