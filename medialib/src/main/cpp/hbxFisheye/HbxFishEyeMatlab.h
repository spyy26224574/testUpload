#pragma once

#include "HbxFishEyeParameters.h"

#ifndef _HBX_FISHEYE_M_H_
#define _HBX_FISHEYE_M_H_
///
struct vec3f {
    float x, y, z;
};

struct Vertice5f {
public:
    float x, y, z, u, v;

    Vertice5f(float pX, float pY, float pZ, float pU, float pV) {
        x = pX;
        y = pY;
        z = pZ;
        u = pU;
        v = pV;
    };

    Vertice5f() {};

    void operator=(Vertice5f Vertice) {
        x = Vertice.x;
        y = Vertice.y;
        z = Vertice.z;
        u = Vertice.u;
        v = Vertice.v;
    }
};

struct TriangleVertices {
public:
    struct list_head next;
    Vertice5f mV0;
    Vertice5f mV1;
    Vertice5f mV2;
public:
    TriangleVertices(struct Vertice5f *V0, struct Vertice5f *V1, struct Vertice5f *V2) {
        mV0 = *V0;
        mV1 = *V1;
        mV2 = *V2;
        next.next = NULL;
    };

    TriangleVertices() { next.next = NULL; };

    ~TriangleVertices() {
        //	DELETE_BUFFER(mV0);
        //	DELETE_BUFFER(mV1);
        //	DELETE_BUFFER(mV2);
    }
};


class Texture2f {
public:
    float m_fu, m_fv;
public:
    Texture2f(float u, float v) {
        m_fu = u;
        m_fv = v;
    };

    Texture2f() {};
};

class CHbxFishEyeMatlab {
public:
    CHbxFishEyeMatlab();

    ~CHbxFishEyeMatlab();

protected:
    //
    Vertice5f *
    GetOpenGLESVerticeForSphere(float r, float theta, float thetaBegin, float thetaTotal, float phi,
                                float phiShift, \
        float phiTotal, float xyaspect);

    void
    GetOpenGLESVerticeForSphereRectangle(float r, float theta, float thetaStep, float thetaBegin, \
        float thetaTotal, float phi, float phiStep, float phiShift, float phiTotal, float xyaspect);

    //
    Vertice5f *
    GetOpenGLESVerticeForCylinder(float r, float theta, float thetaBegin, float thetaTotal,
                                  float phi, float phiShift, float phiTotal, float xyaspect);

    void
    GetOpenGLESVerticeForCylinderRectangle(float r, float theta, float thetaStep, float thetaBegin,
                                           float thetaTotal, float phi, float phiStep,
                                           float phiShift, float phiTotal, float xyaspect);

    //
    Vertice5f *
    GetOpenGLESVerticeForPlane(float r, float theta, float thetaBegin, float thetaTotal, float phi,
                               float phiShift, float phiTotal, float xyaspect);

    Texture2f *
    GetDistortFisheyePointMatlab(float x, float y, float z, float rhoScale, float cx, float cy,
                                 float w, float h, float k[], int kLen);

    void
    GetOpenGLESVerticeForPlaneRectangle(float r, float theta, float thetaStep, float thetaBegin,
                                        float thetaTotal,
                                        float phi, float phiStep, float phiShift, float phiTotal,
                                        float xyaspect);

    CHbxFishEyeParameters *m_pCurParameters;

    //
    void InitVertexDataPlane();

    void InitVertexDataCylinder();

    void InitVertexDataSphere();

    //pannel
    float *m_pPannelVertices;

    struct list_head m_TriangleVerticesList;
    struct list_head *m_preItem;
    int m_nTriangleVerticesCount;

public:
    //
    void MakeCylinderVertices(CHbxFishEyeParameters *Parameters, CHbxFishEyeVertices *Vertices);

    void MakeSphereVertices(CHbxFishEyeParameters *Parameters, CHbxFishEyeVertices *Vertices);

    void MakeVertices(CHbxFishEyeParameters *Parameters, CHbxFishEyeVertices *Vertices);

    void MakePannelVertices(CHbxFishEyeParameters *Parameters, CHbxFishEyeVertices *Vertices);

public:
    CHbxFishEyeVertices *m_Vertices;

    static int TRIANGLE_VERTICES_DATA_STRIDE_BYTES;
};

#endif // !_HBX_FISHEYE_M_H_
