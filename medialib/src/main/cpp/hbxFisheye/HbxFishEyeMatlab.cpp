#include <math.h>
#include "HbxFishEyeMatlab.h"

//
int CHbxFishEyeMatlab::TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * sizeof(float);

CHbxFishEyeMatlab::CHbxFishEyeMatlab() {
    m_pCurParameters = NULL;
    //pannel
    m_pPannelVertices = NULL;
}

CHbxFishEyeMatlab::~CHbxFishEyeMatlab() {
    m_pCurParameters = NULL;
    DELETE_BUFFER(m_pPannelVertices);

}

void
CHbxFishEyeMatlab::MakeVertices(CHbxFishEyeParameters *Parameters, CHbxFishEyeVertices *Vertices) {
    //
    m_Vertices = Vertices;
    //
    m_pCurParameters = NULL;
    DELETE_BUFFER(m_pPannelVertices);
    //
    m_pCurParameters = NULL;
    //pannel
    m_pPannelVertices = NULL;
    //
    m_pCurParameters = Parameters;
    //
    InitVertexDataPlane();
    InitVertexDataCylinder();
    InitVertexDataSphere();
}

void CHbxFishEyeMatlab::MakePannelVertices(CHbxFishEyeParameters *Parameters,
                                           CHbxFishEyeVertices *Vertices) {
    //
    m_Vertices = Vertices;
    //
    DELETE_BUFFER(m_pPannelVertices);
    //pannel
    m_pPannelVertices = NULL;
    //
    m_pCurParameters = Parameters;
    //
    InitVertexDataPlane();
}

void CHbxFishEyeMatlab::MakeCylinderVertices(CHbxFishEyeParameters *Parameters,
                                             CHbxFishEyeVertices *Vertices) {
    //
    m_Vertices = Vertices;
    //
    m_pCurParameters = NULL;
    DELETE_BUFFER(m_pPannelVertices);
    //
    m_pCurParameters = NULL;
    //pannel
    m_pPannelVertices = NULL;
    //
    m_pCurParameters = Parameters;
    //
    InitVertexDataCylinder();
}

void CHbxFishEyeMatlab::MakeSphereVertices(CHbxFishEyeParameters *Parameters,
                                           CHbxFishEyeVertices *Vertices) {
    //
    m_Vertices = Vertices;
    //
    m_pCurParameters = NULL;
    DELETE_BUFFER(m_pPannelVertices);
    //
    m_pCurParameters = NULL;
    //pannel
    m_pPannelVertices = NULL;
    //
    m_pCurParameters = Parameters;
    //
    InitVertexDataSphere();
}

Texture2f *
CHbxFishEyeMatlab::GetDistortFisheyePointMatlab(float x, float y, float z, float rhoScale, float cx,
                                                float cy, float w, float h, float k[], int kLen) {
    float xc = x / fabsf(z);
    float yc = y / fabsf(z);
    float lambdaRho = (float) (sqrt(xc * xc + yc * yc));
    if (z < 0) {
        lambdaRho = -lambdaRho;
    }
    float theta = (float) (atan(1 / lambdaRho));
    float rho = k[0];
    for (int i = 1; i < kLen; i++) {
        rho = theta * rho + k[i];
    }
    if (theta >= 0) {
        rho = rho * rhoScale;
    } else {
        rho = -rho * rhoScale;
    }
    float u = (xc / lambdaRho) * rho + cx;
    float v = (yc / lambdaRho) * rho + cy;

    u = u / h;
    v = v / w;
    return new Texture2f(u, v);
}

///////////////
//r,theta:25 ~ 90,thetaBegin:50,thetaTotal:90-25
//phi:0~360,phiShift;-90.0f,phiTotal:360,
///
Vertice5f *CHbxFishEyeMatlab::GetOpenGLESVerticeForPlane(float r, float theta, float thetaBegin,
                                                         float thetaTotal, float phi,
                                                         float phiShift, float phiTotal,
                                                         float xyaspect) {
    float x = (phi / phiTotal) * 2 - 1;

    float y = (((theta - thetaBegin) / thetaTotal) * 2 - 1);//* abs(thetaTotal) / abs(phiTotal);
    y = y / xyaspect;
    float z = 0;
    float thetaRadian = (float) (theta * PI / 180.0f);
    float phiRadian = (float) ((phi + phiShift) * PI / 180.0f);
    //
    float xx = (float) (r * sin(thetaRadian) * cos(phiRadian));
    float yy = (float) (r * sin(thetaRadian) * sin(phiRadian));
    float zz = (float) (r * cos(thetaRadian));
    //

    Texture2f *p = GetDistortFisheyePointMatlab(xx, yy, zz, 1.0f, m_pCurParameters->m_CXMatlab,
                                                m_pCurParameters->m_CYMatlab, 1.0f, 1.0f,
                                                m_pCurParameters->m_KMatlab,
                                                m_pCurParameters->m_KMatlabLen);
    //
    p->m_fu = p->m_fu - (m_pCurParameters->m_ImageWidth - m_pCurParameters->m_ImageHigh) / 2;
    p->m_fu = p->m_fu / m_pCurParameters->m_ImageHigh;
    p->m_fv = p->m_fv / m_pCurParameters->m_ImageWidth;
    //
    float u = p->m_fv;
    float v = p->m_fu;

    //
//	p.x = p.x - 420;
//	p.x = p.x / 1080f;
//	p.y = p.y / 1920.f;
    //
//	float u = p.y;
//	float v = p.x;
    //
    DELETE_BUFFER(p);
    return new Vertice5f(x, y, z, u, v);
}


//////////////////
// r ,theta 25 ~ 90;thetaStep:0.5;thetaBegin:25,thetaTotal= 90-25
//phi,0~360,phiStep:0.1;phiShift-90.0f;phiTotal=360
// V0 V2
// V1 V3
void CHbxFishEyeMatlab::GetOpenGLESVerticeForPlaneRectangle(float r, float theta, float thetaStep,
                                                            float thetaBegin, float thetaTotal,
                                                            float phi, float phiStep,
                                                            float phiShift, float phiTotal,
                                                            float xyaspect) {
    float theta0, theta1, phi0, phi1;
    Vertice5f *V0, *V1, *V2, *V3;

    phi0 = phi;
    phi1 = phi0 + phiStep;

    theta0 = theta;
    theta1 = theta0 + thetaStep;

    V0 = GetOpenGLESVerticeForPlane(r, theta0, thetaBegin, thetaTotal, phi0, phiShift, phiTotal,
                                    xyaspect);
    V1 = GetOpenGLESVerticeForPlane(r, theta1, thetaBegin, thetaTotal, phi0, phiShift, phiTotal,
                                    xyaspect);
    V2 = GetOpenGLESVerticeForPlane(r, theta0, thetaBegin, thetaTotal, phi1, phiShift, phiTotal,
                                    xyaspect);
    V3 = GetOpenGLESVerticeForPlane(r, theta1, thetaBegin, thetaTotal, phi1, phiShift, phiTotal,
                                    xyaspect);

    //V0 V1 V2
    /*
    triangles.add(new Triangle3f(V0, V1, V2));
    //V1 V3 V2
    triangles.add(new Triangle3f(V1, V3, V2));
    */
    if ((V0->u > 0) && (V0->v > 0) && (V1->u > 0) && (V1->v > 0) && (V2->u > 0) && (V2->v > 0) &&
        (V3->u > 0) && (V3->v > 0) &&
        (V0->u < 1) && (V0->v < 1) && (V1->u < 1) && (V1->v < 1) && (V2->u < 1) && (V2->v < 1) &&
        (V3->u < 1) && (V3->v < 1)) {
        //V0 V1 V2
        //  V0  -  V2
        //  |   /  |
        //  V1  -  V3
        TriangleVertices *pTVertices = new TriangleVertices(V0, V1, V2);
        TriangleVertices *nTVertices = new TriangleVertices(V1, V3, V2);

        list_add(&(pTVertices->next), m_preItem);
        m_preItem = &(pTVertices->next);
        list_add(&(nTVertices->next), m_preItem);
        m_preItem = &(nTVertices->next);
        m_nTriangleVerticesCount += 2;
    } else if ((V0->u > 0) || (V0->v > 0) || (V1->u > 0) || (V1->v > 0) || (V2->u > 0) ||
               (V2->v > 0) || (V3->u > 0) || (V3->v > 0) ||
               (V0->u < 1) || (V0->v < 1) || (V1->u < 1) || (V1->v < 1) || (V2->u < 1) ||
               (V2->v < 1) || (V3->u < 1) || (V3->v < 1)) {
        float thinningMultiple = 5.0f;
        float phiStepThin = phiStep / thinningMultiple;
        float thetaStepThin = thetaStep / thinningMultiple;
        for (phi0 = phi; phi0 < phi + phiStep; phi0 = phi0 + phiStepThin) {
            for (theta0 = theta; theta0 < theta + thetaStep; theta0 = theta0 + thetaStepThin) {
                phi1 = phi0 + phiStepThin;
                theta1 = theta0 + thetaStepThin;
                V0 = GetOpenGLESVerticeForPlane(r, theta0, thetaBegin, thetaTotal, phi0, phiShift,
                                                phiTotal, xyaspect);
                V1 = GetOpenGLESVerticeForPlane(r, theta1, thetaBegin, thetaTotal, phi0, phiShift,
                                                phiTotal, xyaspect);
                V2 = GetOpenGLESVerticeForPlane(r, theta0, thetaBegin, thetaTotal, phi1, phiShift,
                                                phiTotal, xyaspect);
                V3 = GetOpenGLESVerticeForPlane(r, theta1, thetaBegin, thetaTotal, phi1, phiShift,
                                                phiTotal, xyaspect);
                if ((V0->u > 0) && (V0->v > 0) && (V1->u > 0) && (V1->v > 0) && (V2->u > 0) &&
                    (V2->v > 0) && (V3->u > 0) && (V3->v > 0) &&
                    (V0->u < 1) && (V0->v < 1) && (V1->u < 1) && (V1->v < 1) && (V2->u < 1) &&
                    (V2->v < 1) && (V3->u < 1) && (V3->v < 1)) {
                    //V0 V1 V2
                    //  V0  -  V2
                    //  |   /  |
                    //  V1  -  V3
                    TriangleVertices *pTVertices = new TriangleVertices(V0, V1, V2);
                    TriangleVertices *nTVertices = new TriangleVertices(V1, V3, V2);

                    list_add(&(pTVertices->next), m_preItem);
                    m_preItem = &(pTVertices->next);
                    list_add(&(nTVertices->next), m_preItem);
                    m_preItem = &(nTVertices->next);
                    m_nTriangleVerticesCount += 2;
                    //triangles.add(new Triangle3f(V0, V1, V2));
                    //V1 V3 V2
                    //triangles.add(new Triangle3f(V1, V3, V2));
                }
            }
        }
    }
    DELETE_BUFFER(V0);
    DELETE_BUFFER(V1);
    DELETE_BUFFER(V2);
    DELETE_BUFFER(V3);
}

void CHbxFishEyeMatlab::InitVertexDataPlane() {
    float planeThetaBegin = m_pCurParameters->m_fStartAngle;
    float planeThetaEnd = m_pCurParameters->m_fEndAngle;
    float planeThetaTotal = planeThetaEnd - planeThetaBegin;
    float planeThetaStep = 1.0f;
    int planeThetaCount = (int) (planeThetaTotal / planeThetaStep);
    float planePhiBegin = 0;
    float planePhiEnd = 360;
    float planePhiTotal = planePhiEnd - planePhiBegin;
    float planePhiStep = 1.0f;
    int planePhiCount = (int) (planePhiTotal / planePhiStep);

    float r = m_pCurParameters->m_R;
    float phiShift = -90.0f;
    float xyaspect = planePhiTotal / planeThetaTotal;

    float phi;
    float theta;

    list_init(&m_TriangleVerticesList);
    m_nTriangleVerticesCount = 0;
    m_preItem = &m_TriangleVerticesList;
    //  V0  -  V2
    //  |   /  |
    //  V1  -  V3
    for (int phiIndex = 0; phiIndex < planePhiCount; phiIndex++) {
        //phiIndex cricl 0~360,
        for (int thetaIndex = 0; thetaIndex < planeThetaCount; thetaIndex++) {
            //25 ~ 95
            phi = phiIndex * planePhiStep;//0~360 jingdu
            theta = thetaIndex * planeThetaStep + planeThetaBegin;//25~50 weidu
            GetOpenGLESVerticeForPlaneRectangle(r, theta, planeThetaStep, planeThetaBegin,
                                                planeThetaTotal, phi, planePhiStep, phiShift,
                                                planePhiTotal, xyaspect);
        }
    }
    int nPannelVerticesSize = m_nTriangleVerticesCount * 3 * 5;
    //
    if (!m_pPannelVertices)
        m_pPannelVertices = new float[nPannelVerticesSize];

    if (!m_Vertices->m_pPannelDstVertices)
        m_Vertices->m_pPannelDstVertices = new float[nPannelVerticesSize * 2];

    m_Vertices->m_nPannelVerticesCount = 0;
    int offset = 0;
    if (m_TriangleVerticesList.next) {
        struct list_head *head = &m_TriangleVerticesList;
        TriangleVertices *Vertices = NULL;
        for (Vertices = (TriangleVertices *) head->next;
             head->next != NULL; Vertices = (TriangleVertices *) head->next) {

            m_pPannelVertices[offset++] = Vertices->mV0.x;
            m_pPannelVertices[offset++] = Vertices->mV0.y;
            m_pPannelVertices[offset++] = 0;
            m_pPannelVertices[offset++] = Vertices->mV0.u;
            m_pPannelVertices[offset++] = Vertices->mV0.v;

            m_pPannelVertices[offset++] = Vertices->mV1.x;
            m_pPannelVertices[offset++] = Vertices->mV1.y;
            m_pPannelVertices[offset++] = 0;
            m_pPannelVertices[offset++] = Vertices->mV1.u;
            m_pPannelVertices[offset++] = Vertices->mV1.v;

            m_pPannelVertices[offset++] = Vertices->mV2.x;
            m_pPannelVertices[offset++] = Vertices->mV2.y;
            m_pPannelVertices[offset++] = 0;
            m_pPannelVertices[offset++] = Vertices->mV2.u;
            m_pPannelVertices[offset++] = Vertices->mV2.v;

            list_del(&(Vertices->next), head);
            m_Vertices->m_nPannelVerticesCount += 3;
            DELETE_BUFFER(Vertices);
        }
    }
    offset = 0;
    memcpy((unsigned char *) m_Vertices->m_pPannelDstVertices, (unsigned char *) m_pPannelVertices,
           nPannelVerticesSize * sizeof(float));
    memcpy((unsigned char *) (&(m_Vertices->m_pPannelDstVertices[nPannelVerticesSize])),
           (unsigned char *) m_pPannelVertices, nPannelVerticesSize * sizeof(float));
    for (int i = 0; i < m_nTriangleVerticesCount; i++) {
        m_Vertices->m_pPannelDstVertices[offset + nPannelVerticesSize] =
                m_pPannelVertices[offset] + 2.0;
        offset += 5;
        m_Vertices->m_pPannelDstVertices[offset + nPannelVerticesSize] =
                m_pPannelVertices[offset] + 2.0;
        offset += 5;
        m_Vertices->m_pPannelDstVertices[offset + nPannelVerticesSize] =
                m_pPannelVertices[offset] + 2.0;
        offset += 5;
        m_Vertices->m_nPannelVerticesCount += 3;
    }
    DELETE_BUFFER(m_pPannelVertices);
}

Vertice5f *CHbxFishEyeMatlab::GetOpenGLESVerticeForCylinder(float r, float theta, float thetaBegin,
                                                            float thetaTotal, \
      float phi, float phiShift, float phiTotal, float xyaspect) {
    float phiRadian = (float) ((phi) * PI / 180.0f);
    float thetaRadian = (float) (theta * PI / 180.0f);

    float x = (float) (cos(phiRadian));
    float y = (float) (sin(phiRadian));
    float z = (float) (cos(thetaRadian));
    phiRadian = (float) ((phi + phiShift) * PI / 180.0f);
    //
    float xx = (float) (r * sin(thetaRadian) * cos(phiRadian));
    float yy = (float) (r * sin(thetaRadian) * sin(phiRadian));
    float zz = (float) ((r * cos(thetaRadian)) -
                        ((cos(m_pCurParameters->m_fStartAngle * PI / 180.0f) +
                          cos(m_pCurParameters->m_fEndAngle * PI / 180.0f)) / 2));
    //
    Texture2f *p = GetDistortFisheyePointMatlab(xx, yy, zz, 1.0f, m_pCurParameters->m_CXMatlab,
                                                m_pCurParameters->m_CYMatlab, 1.0f, 1.0f,
                                                m_pCurParameters->m_KMatlab,
                                                m_pCurParameters->m_KMatlabLen);
    //
    p->m_fu = p->m_fu - (m_pCurParameters->m_ImageWidth - m_pCurParameters->m_ImageHigh) / 2;
    p->m_fu = p->m_fu / m_pCurParameters->m_ImageHigh;
    p->m_fv = p->m_fv / m_pCurParameters->m_ImageWidth;
    //
    float u = p->m_fv;
    float v = p->m_fu;
    //
    DELETE_BUFFER(p);
    //
    return new Vertice5f(x, y, z, u, v);
}

void
CHbxFishEyeMatlab::GetOpenGLESVerticeForCylinderRectangle(float r, float theta, float thetaStep,
                                                          float thetaBegin, \
       float thetaTotal, float phi, float phiStep, float phiShift, float phiTotal, float xyaspect) {
    float theta0, theta1, phi0, phi1;
    Vertice5f *V0, *V1, *V2, *V3;

    phi0 = phi;
    phi1 = phi0 + phiStep;

    theta0 = theta;
    theta1 = theta0 + thetaStep;

    V0 = GetOpenGLESVerticeForCylinder(r, theta0, thetaBegin, thetaTotal, phi0, phiShift, phiTotal,
                                       xyaspect);
    V1 = GetOpenGLESVerticeForCylinder(r, theta1, thetaBegin, thetaTotal, phi0, phiShift, phiTotal,
                                       xyaspect);
    V2 = GetOpenGLESVerticeForCylinder(r, theta0, thetaBegin, thetaTotal, phi1, phiShift, phiTotal,
                                       xyaspect);
    V3 = GetOpenGLESVerticeForCylinder(r, theta1, thetaBegin, thetaTotal, phi1, phiShift, phiTotal,
                                       xyaspect);
    if ((V0->u > 0) && (V0->v > 0) && (V1->u > 0) && (V1->v > 0) && (V2->u > 0) && (V2->v > 0) &&
        (V3->u > 0) && (V3->v > 0) &&
        (V0->u < 1) && (V0->v < 1) && (V1->u < 1) && (V1->v < 1) && (V2->u < 1) && (V2->v < 1) &&
        (V3->u < 1) && (V3->v < 1)) {
        //V0 V1 V2
        TriangleVertices *pTVertices = new TriangleVertices(V0, V1, V2);
        //V1 V3 V2
        TriangleVertices *nTVertices = new TriangleVertices(V1, V3, V2);

        list_add(&(pTVertices->next), m_preItem);
        m_preItem = &(pTVertices->next);
        list_add(&(nTVertices->next), m_preItem);
        m_preItem = &(nTVertices->next);
        m_nTriangleVerticesCount += 2;
    } else if ((V0->u > 0) || (V0->v > 0) || (V1->u > 0) || (V1->v > 0) || (V2->u > 0) ||
               (V2->v > 0) || (V3->u > 0) || (V3->v > 0) ||
               (V0->u < 1) || (V0->v < 1) || (V1->u < 1) || (V1->v < 1) || (V2->u < 1) ||
               (V2->v < 1) || (V3->u < 1) || (V3->v < 1)) {

        //ϸ��
        float thinningMultiple = 5.0f;
        float phiStepThin = phiStep / thinningMultiple;
        float thetaStepThin = thetaStep / thinningMultiple;
        for (phi0 = phi; phi0 < phi + phiStep; phi0 = phi0 + phiStepThin) {
            for (theta0 = theta; theta0 < theta + thetaStep; theta0 = theta0 + thetaStepThin) {
                phi1 = phi0 + phiStepThin;
                theta1 = theta0 + thetaStepThin;
                V0 = GetOpenGLESVerticeForCylinder(r, theta0, thetaBegin, thetaTotal, phi0,
                                                   phiShift, phiTotal, xyaspect);
                V1 = GetOpenGLESVerticeForCylinder(r, theta1, thetaBegin, thetaTotal, phi0,
                                                   phiShift, phiTotal, xyaspect);
                V2 = GetOpenGLESVerticeForCylinder(r, theta0, thetaBegin, thetaTotal, phi1,
                                                   phiShift, phiTotal, xyaspect);
                V3 = GetOpenGLESVerticeForCylinder(r, theta1, thetaBegin, thetaTotal, phi1,
                                                   phiShift, phiTotal, xyaspect);
                if ((V0->u > 0) && (V0->v > 0) && (V1->u > 0) && (V1->v > 0) && (V2->u > 0) &&
                    (V2->v > 0) && (V3->u > 0) && (V3->v > 0) &&
                    (V0->u < 1) && (V0->v < 1) && (V1->u < 1) && (V1->v < 1) && (V2->u < 1) &&
                    (V2->v < 1) && (V3->u < 1) && (V3->v < 1)) {
                    //V0 V1 V2
                    TriangleVertices *pTVertices = new TriangleVertices(V0, V1, V2);
                    //V1 V3 V2
                    TriangleVertices *nTVertices = new TriangleVertices(V1, V3, V2);

                    list_add(&(pTVertices->next), m_preItem);
                    m_preItem = &(pTVertices->next);
                    list_add(&(nTVertices->next), m_preItem);
                    m_preItem = &(nTVertices->next);
                    m_nTriangleVerticesCount += 2;
                }
            }
        }
    }
    DELETE_BUFFER(V0);
    DELETE_BUFFER(V1);
    DELETE_BUFFER(V2);
    DELETE_BUFFER(V3);
}

void CHbxFishEyeMatlab::InitVertexDataCylinder() {
    float cylinderThetaBegin = m_pCurParameters->m_fStartAngle;
    float cylinderThetaEnd = m_pCurParameters->m_fEndAngle;
    float cylinderThetaTotal = cylinderThetaEnd - cylinderThetaBegin;
    float cylinderThetaStep = 1.0f;
    int cylinderThetaCount = (int) (cylinderThetaTotal / cylinderThetaStep);
    float cylinderPhiBegin = 0;
    float cylinderPhiEnd = 360;
    float cylinderPhiTotal = cylinderPhiEnd - cylinderPhiBegin;
    float cylinderPhiStep = 1.0f;
    int cylinderPhiCount = (int) (cylinderPhiTotal / cylinderPhiStep);

    float r = m_pCurParameters->m_R;
    float phiShift = -90.0f;
    float xyaspect = cylinderPhiTotal / cylinderThetaTotal;

    float phi;
    float theta;
    list_init(&m_TriangleVerticesList);
    m_nTriangleVerticesCount = 0;
    m_preItem = &m_TriangleVerticesList;
    //          V0  -  V2
    //          |   /  |
    //          V1  -  V3
    for (int phiIndex = 0; phiIndex < cylinderPhiCount; phiIndex++) {
        for (int thetaIndex = 0; thetaIndex < cylinderThetaCount; thetaIndex++) {
            phi = phiIndex * cylinderPhiStep;
            theta = thetaIndex * cylinderThetaStep + cylinderThetaBegin;
            GetOpenGLESVerticeForCylinderRectangle(r, theta, cylinderThetaStep, cylinderThetaBegin,
                                                   cylinderThetaTotal, phi, cylinderPhiStep,
                                                   phiShift, cylinderPhiTotal, xyaspect);
        }
    }
    int nVerticesSize = m_nTriangleVerticesCount * 3 * 5;
    //
    if (!m_Vertices->m_pCyDstVertices)
        m_Vertices->m_pCyDstVertices = new float[nVerticesSize];

    m_Vertices->m_nCyVerticesCount = 0;
    int offset = 0;
    if (m_TriangleVerticesList.next) {
        struct list_head *head = &m_TriangleVerticesList;
        TriangleVertices *Vertices = NULL;
        for (Vertices = (TriangleVertices *) head->next;
             head->next != NULL; Vertices = (TriangleVertices *) head->next) {

            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV0.x;
            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV0.z;
            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV0.y;
            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV0.u;
            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV0.v;

            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV1.x;
            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV1.z;
            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV1.y;
            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV1.u;
            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV1.v;

            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV2.x;
            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV2.z;
            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV2.y;
            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV2.u;
            m_Vertices->m_pCyDstVertices[offset++] = Vertices->mV2.v;

            list_del(&(Vertices->next), head);
            m_Vertices->m_nCyVerticesCount += 3;
            DELETE_BUFFER(Vertices);
        }
    }
}

#if 1

Vertice5f *CHbxFishEyeMatlab::GetOpenGLESVerticeForSphere(float r, float theta, float thetaBegin,
                                                          float thetaTotal, float phi,
                                                          float phiShift, \
             float phiTotal, float xyaspect) {
    //��������
    float phiRadian = (float) ((phi) * PI / 180.0f);
    float thetaRadian = (float) (theta * PI / 180.0f);

    float x = (float) (sin(thetaRadian) * cos(phiRadian));
    float y = (float) (sin(thetaRadian) * sin(phiRadian));
    float z = (float) (cos(thetaRadian));
    //��������
    phiRadian = (float) ((phi + phiShift) * PI / 180.0f);
    //
    float xx = (float) (r * sin(thetaRadian) * cos(phiRadian));
    float yy = (float) (r * sin(thetaRadian) * sin(phiRadian));
    float zz = (float) (r * cos(thetaRadian));
    //
    Texture2f *p = GetDistortFisheyePointMatlab(xx, yy, zz, 1.0f, m_pCurParameters->m_CXMatlab,
                                                m_pCurParameters->m_CYMatlab, \
        1.0f, 1.0f, m_pCurParameters->m_KMatlab, m_pCurParameters->m_KMatlabLen);
    //
    //ת��Ϊw1920 * hW_IMAGEͼ�������ϵĵ�
    p->m_fu = p->m_fu - (m_pCurParameters->m_ImageWidth - m_pCurParameters->m_ImageHigh) / 2;
    //��һ����[0 1]
    p->m_fu = p->m_fu / m_pCurParameters->m_ImageHigh;
    p->m_fv = p->m_fv / m_pCurParameters->m_ImageWidth;
    //
    float u = p->m_fv;
    float v = p->m_fu;
    //
    DELETE_BUFFER(p);
    return new Vertice5f(x, y, z, u, v);
}

#else
void CHbxFishEyeMatlab::GetOpenGLESVerticeForSphere(float r, float theta, float thetaBegin, float thetaTotal, float phi, float phiShift, \
    float phiTotal, float xyaspect, Vertice5f &v)
{
    //��������
    float phiRadian = (float)((phi)*PI / 180.0f);
    float thetaRadian = (float)(theta*PI / 180.0f);

    float x = (float)(sin(thetaRadian)*cos(phiRadian));
    float y = (float)(sin(thetaRadian)*sin(phiRadian));
    float z = (float)(cos(thetaRadian));
    //��������
    phiRadian = (float)((phi + phiShift)*PI / 180.0f);
    //
    float xx = (float)(r*sin(thetaRadian)*cos(phiRadian));
    float yy = (float)(r*sin(thetaRadian)*sin(phiRadian));
    float zz = (float)(r*cos(thetaRadian));
    //
    Texture2f* p = GetDistortFisheyePointMatlab(xx, yy, zz, 1.0f, m_pCurParameters->m_CXMatlab, m_pCurParameters->m_CYMatlab, \
        1.0f, 1.0f, m_pCurParameters->m_KMatlab, m_pCurParameters->m_KMatlabLen);
    //
    //ת��Ϊw1920 * hW_IMAGEͼ�������ϵĵ�
    p->m_fu = p->m_fu - (m_pCurParameters->m_ImageWidth - m_pCurParameters->m_ImageHigh) / 2;
    //��һ����[0 1]
    p->m_fu = p->m_fu / m_pCurParameters->m_ImageHigh;
    p->m_fv = p->m_fv / m_pCurParameters->m_ImageWidth;
    //
    float u = p->m_fv;
    float v = p->m_fu;
    //
    DELETE_BUFFER(p);
//	return new Vertice5f(x, y, z, u, v);
}
#endif

void CHbxFishEyeMatlab::GetOpenGLESVerticeForSphereRectangle(float r, float theta, float thetaStep,
                                                             float thetaBegin, \
    float thetaTotal, float phi, float phiStep, float phiShift, float phiTotal, float xyaspect) {
    float theta0, theta1, phi0, phi1;
    Vertice5f *V0, *V1, *V2, *V3;

    phi0 = phi;
    phi1 = phi0 + phiStep;

    theta0 = theta;
    theta1 = theta0 + thetaStep;

    V0 = GetOpenGLESVerticeForSphere(r, theta0, thetaBegin, thetaTotal, phi0, phiShift, phiTotal,
                                     xyaspect);
    V1 = GetOpenGLESVerticeForSphere(r, theta1, thetaBegin, thetaTotal, phi0, phiShift, phiTotal,
                                     xyaspect);
    V2 = GetOpenGLESVerticeForSphere(r, theta0, thetaBegin, thetaTotal, phi1, phiShift, phiTotal,
                                     xyaspect);
    V3 = GetOpenGLESVerticeForSphere(r, theta1, thetaBegin, thetaTotal, phi1, phiShift, phiTotal,
                                     xyaspect);

    if ((V0->u > 0) && (V0->v > 0) && (V1->u > 0) && (V1->v > 0) && (V2->u > 0) && (V2->v > 0) &&
        (V3->u > 0) && (V3->v > 0) &&
        (V0->u < 1) && (V0->v < 1) && (V1->u < 1) && (V1->v < 1) && (V2->u < 1) && (V2->v < 1) &&
        (V3->u < 1) && (V3->v < 1)) {
        //��һ������
        //��һ������
        //V0 V1 V2
        TriangleVertices *pTVertices = new TriangleVertices(V0, V1, V2);
        //��һ������
        //V1 V3 V2
        TriangleVertices *nTVertices = new TriangleVertices(V1, V3, V2);
        list_add(&(pTVertices->next), m_preItem);
        m_preItem = &(pTVertices->next);
        list_add(&(nTVertices->next), m_preItem);
        m_preItem = &(nTVertices->next);
        m_nTriangleVerticesCount += 2;
    } else if ((V0->u > 0) || (V0->v > 0) || (V1->u > 0) || (V1->v > 0) || (V2->u > 0) ||
               (V2->v > 0) || (V3->u > 0) || (V3->v > 0) ||
               (V0->u < 1) || (V0->v < 1) || (V1->u < 1) || (V1->v < 1) || (V2->u < 1) ||
               (V2->v < 1) || (V3->u < 1) || (V3->v < 1)) {
        //ϸ��
        float thinningMultiple = 5.0f;
        float phiStepThin = phiStep / thinningMultiple;
        float thetaStepThin = thetaStep / thinningMultiple;
        for (phi0 = phi; phi0 < phi + phiStep; phi0 = phi0 + phiStepThin) {
            for (theta0 = theta; theta0 < theta + thetaStep; theta0 = theta0 + thetaStepThin) {
                phi1 = phi0 + phiStepThin;
                theta1 = theta0 + thetaStepThin;
                V0 = GetOpenGLESVerticeForSphere(r, theta0, thetaBegin, thetaTotal, phi0, phiShift,
                                                 phiTotal, xyaspect);
                V1 = GetOpenGLESVerticeForSphere(r, theta1, thetaBegin, thetaTotal, phi0, phiShift,
                                                 phiTotal, xyaspect);
                V2 = GetOpenGLESVerticeForSphere(r, theta0, thetaBegin, thetaTotal, phi1, phiShift,
                                                 phiTotal, xyaspect);
                V3 = GetOpenGLESVerticeForSphere(r, theta1, thetaBegin, thetaTotal, phi1, phiShift,
                                                 phiTotal, xyaspect);
                if ((V0->u > 0) && (V0->v > 0) && (V1->u > 0) && (V1->v > 0) && (V2->u > 0) &&
                    (V2->v > 0) && (V3->u > 0) && (V3->v > 0) &&
                    (V0->u < 1) && (V0->v < 1) && (V1->u < 1) && (V1->v < 1) && (V2->u < 1) &&
                    (V2->v < 1) && (V3->u < 1) && (V3->v < 1)) {
                    //��һ������
                    //V0 V1 V2
                    TriangleVertices *pTVertices = new TriangleVertices(V0, V1, V2);
                    //��һ������
                    //V1 V3 V2
                    TriangleVertices *nTVertices = new TriangleVertices(V1, V3, V2);

                    list_add(&(pTVertices->next), m_preItem);
                    m_preItem = &(pTVertices->next);
                    list_add(&(nTVertices->next), m_preItem);
                    m_preItem = &(nTVertices->next);
                    m_nTriangleVerticesCount += 2;
                }
            }
        }
    }
    DELETE_BUFFER(V0);
    DELETE_BUFFER(V1);
    DELETE_BUFFER(V2);
    DELETE_BUFFER(V3);
}

void CHbxFishEyeMatlab::InitVertexDataSphere() {
    float sphereThetaBegin = 0.0;//��ʼ�Ƕ�
    float sphereThetaEnd = 89.0;//�����Ƕ�
    float sphereThetaTotal = sphereThetaEnd - sphereThetaBegin;
    float sphereThetaStep = 1.0f;
    int sphereThetaCount = (int) (sphereThetaTotal / sphereThetaStep);
    float spherePhiBegin = 0;//��ʼ�Ƕ�
    float spherePhiEnd = 360;//�����Ƕ�
    float spherePhiTotal = spherePhiEnd - spherePhiBegin;
    float spherePhiStep = 1.0f;
    int spherePhiCount = (int) (spherePhiTotal / spherePhiStep);
    float r = m_pCurParameters->m_R;
    float phiShift = 90.0f;//���ÿ�ʼ�Ƕ�
    float xyaspect = spherePhiTotal / sphereThetaTotal;

    float phi;
    float theta;
    //����
    list_init(&m_TriangleVerticesList);
    m_nTriangleVerticesCount = 0;
    m_preItem = &m_TriangleVerticesList;
    //  V0  -  V2
    //  |   /  |
    //  V1  -  V3
    for (int phiIndex = 0; phiIndex < spherePhiCount; phiIndex++) {
        for (int thetaIndex = 0; thetaIndex < sphereThetaCount; thetaIndex++) {
            phi = phiIndex * spherePhiStep;
            theta = thetaIndex * sphereThetaStep + sphereThetaBegin;
            GetOpenGLESVerticeForSphereRectangle(r, theta, sphereThetaStep, sphereThetaBegin,
                                                 sphereThetaTotal, phi, spherePhiStep, phiShift,
                                                 spherePhiTotal, xyaspect);
        }
    }
    //
    int nVerticesSize = m_nTriangleVerticesCount * 3 * 5;
    //

    if (!m_Vertices->m_pSphereDstVertices)
        m_Vertices->m_pSphereDstVertices = new float[nVerticesSize];

    m_Vertices->m_nSphereVerticesCount = 0;
    int offset = 0;

    if (m_TriangleVerticesList.next) {
        struct list_head *head = &m_TriangleVerticesList;
        TriangleVertices *Vertices = NULL;
        for (Vertices = (TriangleVertices *) head->next;
             head->next != NULL; Vertices = (TriangleVertices *) head->next) {

            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV0.x;
            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV0.y;
            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV0.z;
            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV0.u;
            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV0.v;

            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV1.x;
            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV1.y;
            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV1.z;
            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV1.u;
            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV1.v;

            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV2.x;
            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV2.y;
            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV2.z;
            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV2.u;
            m_Vertices->m_pSphereDstVertices[offset++] = Vertices->mV2.v;

            list_del(&(Vertices->next), head);
            m_Vertices->m_nSphereVerticesCount += 3;
            DELETE_BUFFER(Vertices);
        }
    }
}