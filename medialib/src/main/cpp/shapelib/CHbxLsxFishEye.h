#pragma once

#include "CHbxFishEye.h"
typedef struct _DISTORTION_TABLE_
{
    float fangle;
    float fdistortion;
}DISTORTION_TABLE;

class CHbxLsxFishEye : public CHbxFishEye {
public:
    CHbxLsxFishEye();

    ~CHbxLsxFishEye();

protected:
    struct list_head m_VertexList;
    struct list_head *m_preItem;
public:
    float *m_pDstVertices;
    int m_nSphereVerticesCount;
    int m_nVerticesCount;

public:
    void MakeVertex(float x0, float y0, float radiu, float nwidth, float nheight, int type);

};

