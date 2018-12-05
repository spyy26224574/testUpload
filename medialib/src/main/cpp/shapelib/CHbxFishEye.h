#pragma once

#include <stdio.h>

#define HBXPI  (3.141592653589793f)

#ifdef __cplusplus
extern "C" {
#endif
#include "hbxList.h"
#include <math.h>
#ifdef __cplusplus
}
#endif
#define DELETE_BUFFER(buffer) if(buffer){ \
        delete buffer; \
        buffer = NULL; \
    }

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
    }
};


class CHbxFishEye {
public:
    CHbxFishEye();

    ~CHbxFishEye();

protected:
    float *m_pBaseVertices;
    int m_nBaseVerticesCount;
public:
    virtual float *GetVertexData() { return m_pBaseVertices; };

    virtual int GetVertexCount() { return m_nBaseVerticesCount; };
};

