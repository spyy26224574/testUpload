
#include "HbxFishEyePublic.h"

#ifndef _HBX_FISHEYE_P_H_
#define _HBX_FISHEYE_P_H_

class CHbxFishEyeParameters {
public:
    struct list_head next;
    int m_nFishEyeId;
public:
    CHbxFishEyeParameters(int id);

    CHbxFishEyeParameters();

    ~CHbxFishEyeParameters();

public:
    float *m_AMatlab;
    int m_KMatlabLen;
    float *m_KMatlab;
    float m_CXMatlab;
    float m_CYMatlab;
    float m_ImageWidth;
    float m_ImageHigh;
    float m_R;

    float m_fStartAngle;
    float m_fEndAngle;
};

//
class CHbxFishEyeVertices {
public:
    float *m_pPannelDstVertices;
    int m_nPannelVerticesCount;
    //cy
    float *m_pCyDstVertices;
    int m_nCyVerticesCount;
    //Sphere
    float *m_pSphereDstVertices;
    int m_nSphereVerticesCount;
public:
    CHbxFishEyeVertices() {
        m_pPannelDstVertices = NULL;
        m_nPannelVerticesCount = 0;
        //cy
        m_pCyDstVertices = NULL;
        m_nCyVerticesCount = 0;
        //Sphere
        m_pSphereDstVertices = NULL;
        m_nSphereVerticesCount = 0;
    };

    //
    ~CHbxFishEyeVertices() {
        DELETE_BUFFER(m_pPannelDstVertices);
        DELETE_BUFFER(m_pCyDstVertices);
        DELETE_BUFFER(m_pSphereDstVertices);
    }
};

#endif
