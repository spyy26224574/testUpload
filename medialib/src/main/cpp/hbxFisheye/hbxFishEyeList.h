#pragma once

#include "HbxFishEyeParameters.h"

#ifndef _HBX_FISHEYE_LIST_H_
#define _HBX_FISHEYE_LIST_H_

class CHbxFishEyeList
{
public:
	CHbxFishEyeList();
	~CHbxFishEyeList();
protected:
	struct list_head *m_preItem;
	void              Clean();

public:
	struct list_head  m_List;
	void              Push(CHbxFishEyeParameters *parame);
	//
	CHbxFishEyeParameters *FishEyeParameters(int id, int width, int high);
	void                   Init();
	//
};
#endif