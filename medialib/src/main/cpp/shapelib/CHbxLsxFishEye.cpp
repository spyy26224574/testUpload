#include "CHbxLsxFishEye.h"

static int START_ANGEL = 20;
static int END_ANGEL = 90;

static DISTORTION_TABLE lsx_table[] =
        {
                {0,   0,},
                {2,   0.03648581},
                {4,   0.07295877},
                {6,   0.10940603},
                {8,   0.1458147},
                {10,  0.18217183},
                {12,  0.21846443},
                {14,  0.2546794},
                {16,  0.29080358},
                {18,  0.32682365},
                {20,  0.36272617},
                {22,  0.39849752},
                {24,  0.43412391},
                {26,  0.46959132},
                {28,  0.50488547},
                {30,  0.53999182},
                {32,  0.57489551},
                {34,  0.60958131},
                {36,  0.64403361},
                {38,  0.67823633},
                {40,  0.71217291},
                {42,  0.74582625},
                {44,  0.77917862},
                {46,  0.81221162},
                {48,  0.84490611},
                {50,  0.87724216},
                {52,  0.90919893},
                {54,  0.94075464},
                {56,  0.97188647},
                {58,  1.00257046},
                {60,  1.0327815},
                {62,  1.06249319},
                {64,  1.09167782},
                {66,  1.12030627},
                {68,  1.14834803},
                {70,  1.1757711},
                {72,  1.20254204},
                {74,  1.22862597},
                {76,  1.25398664},
                {78,  1.27858648},
                {80,  1.30238676},
                {82,  1.32534775},
                {84,  1.34742889},
                {86,  1.36858907},
                {88,  1.38878684},
                {90,  1.40610768},
                {92,  1.42612937},
                {94,  1.4431919},
                {96,  1.45912787},
                {98,  1.47389735},
                {100, 1.4874608},
        };

CHbxLsxFishEye::CHbxLsxFishEye() {
}


CHbxLsxFishEye::~CHbxLsxFishEye() {
    DELETE_BUFFER(m_pDstVertices);
}

Vertice5f *
VerticeForSphere(float radius, float thetaAngle, float phiAngle, float x0, float y0, float inWidth,
                 float inHeight) {
    //计算网格
    float x = (float) (sin(thetaAngle) * cos(phiAngle));
    float y = (float) (sin(thetaAngle) * sin(phiAngle));
    float z = (float) (cos(thetaAngle));
    float u = 0.0f;
    float v = 0.0f;
    float theta2 = thetaAngle * 180 / HBXPI;
    float fi2 = phiAngle;

    //球面到鱼眼
    int index = int(theta2);
    float frealR = lsx_table[(index + 2) / 2].fdistortion - lsx_table[index / 2].fdistortion;
    frealR = lsx_table[index / 2].fdistortion +
             frealR * (theta2 - lsx_table[index / 2].fangle) / 2.0f;
    float radius2 = frealR * radius / 1.40610768;

    u = (radius2 * cos(fi2) + x0);
    v = (radius2 * sin(fi2) + y0);
    if (u >= 0 && u < inWidth - 1 && v >= 0 && v < inHeight - 1) {
        return new Vertice5f(-x, -y, z, u / inWidth, v / inHeight);
    } else {
        return NULL;
    }
}

Vertice5f *
VerticeForCylinder(float radius, float thetaAngle, float phiAngle, float x0, float y0,
                   float inWidth, float inHeight) {
    float x = (float) (0.75 * cos(phiAngle));
    float y = (float) (0.75 * sin(phiAngle));
    float z = (float) ((cos(thetaAngle)) -
                       (cos(START_ANGEL * HBXPI / 180.0f) + cos(END_ANGEL * HBXPI / 180.0f)) / 2);
    float u = 0.0f;
    float v = 0.0f;
    float theta2 = thetaAngle * 180 / HBXPI;
    float fi2 = phiAngle;

    //球面到鱼眼
    int index = int(theta2);
    float frealR = lsx_table[(index + 2) / 2].fdistortion - lsx_table[index / 2].fdistortion;
    frealR = lsx_table[index / 2].fdistortion +
             frealR * (theta2 - lsx_table[index / 2].fangle) / 2.0f;
    float radius2 = frealR * radius / 1.40610768;
//    float radius2 = (radius * 2.0f * 1.05f * tan(thetaAngle / 2)) / 2.251974291;
    u = (radius2 * cos(fi2) + x0);
    v = (radius2 * sin(fi2) + y0);
    if (u >= 0 && u < inWidth - 1 && v >= 0 && v < inHeight - 1) {
        return new Vertice5f(x, y, z, u / inWidth, v / inHeight);
    } else {
        return NULL;
    }
    return NULL;
}

void
CHbxLsxFishEye::MakeVertex(float x0, float y0, float radiu, float nwidth, float nheight, int type) {

    list_init(&m_VertexList);
    m_nVerticesCount = 0;
    m_preItem = &m_VertexList;
    int startAngel = 0;
    int endAngel = 90;
    if (type == 1) {
        startAngel = START_ANGEL;
    }
    for (int j = startAngel; j < endAngel; j++) {
        float theta1 = j * HBXPI / 180.0f;
        float theta2 = (j + 1.0f) * HBXPI / 180.0f;

        for (int i = 0; i < 180.0f; i++) {
            float fi1 = i * 2 * HBXPI / 180.0f;
            float fi2 = (i + 1) * 2 * HBXPI / 180.0f;
            Vertice5f *V0, *V1, *V2, *V3;
            Vertice5f *
            (*rectifyVertex)(float radius, float thetaAngle, float phiAngle, float x0, float y0,
                             float inWidth, float inHeight);
            switch (type) {
                case 0:
                    rectifyVertex = VerticeForSphere;
                    break;
                case 1:
                    rectifyVertex = VerticeForCylinder;
                    break;
                default:
                    rectifyVertex = VerticeForSphere;
                    break;
            }
            V0 = rectifyVertex(radiu, theta1, fi1, x0, y0, nwidth, nheight);
            V1 = rectifyVertex(radiu, theta2, fi1, x0, y0, nwidth, nheight);
            V2 = rectifyVertex(radiu, theta1, fi2, x0, y0, nwidth, nheight);
            V3 = rectifyVertex(radiu, theta2, fi2, x0, y0, nwidth, nheight);
            if (!V0 || !V1 || !V2 || !V3) {
                DELETE_BUFFER(V0);
                DELETE_BUFFER(V1);
                DELETE_BUFFER(V2);
                DELETE_BUFFER(V3);
                continue;
            }
            //V0 V1 V2
            TriangleVertices *pTVertices = new TriangleVertices(V0, V1, V2);
            //V1 V3 V2
            TriangleVertices *nTVertices = new TriangleVertices(V1, V3, V2);

            list_add(&(pTVertices->next), m_preItem);
            m_preItem = &(pTVertices->next);
            list_add(&(nTVertices->next), m_preItem);
            m_preItem = &(nTVertices->next);
            m_nVerticesCount += 6;
            DELETE_BUFFER(V0);
            DELETE_BUFFER(V1);
            DELETE_BUFFER(V2);
            DELETE_BUFFER(V3);
        }
    }

    int nVerticesSize = m_nVerticesCount * 5;
    //
    int offset = 0;
    m_pDstVertices = new float[nVerticesSize];
    m_nSphereVerticesCount = 0;
    if (m_VertexList.next) {
        struct list_head *head = &m_VertexList;
        TriangleVertices *Vertices = NULL;
        for (Vertices = (TriangleVertices *) head->next;
             head->next != NULL; Vertices = (TriangleVertices *) head->next) {


            m_pDstVertices[offset++] = Vertices->mV0.x;
            m_pDstVertices[offset++] = Vertices->mV0.y;
            m_pDstVertices[offset++] = Vertices->mV0.z;
            m_pDstVertices[offset++] = Vertices->mV0.u;
            m_pDstVertices[offset++] = Vertices->mV0.v;

            m_pDstVertices[offset++] = Vertices->mV1.x;
            m_pDstVertices[offset++] = Vertices->mV1.y;
            m_pDstVertices[offset++] = Vertices->mV1.z;
            m_pDstVertices[offset++] = Vertices->mV1.u;
            m_pDstVertices[offset++] = Vertices->mV1.v;

            m_pDstVertices[offset++] = Vertices->mV2.x;
            m_pDstVertices[offset++] = Vertices->mV2.y;
            m_pDstVertices[offset++] = Vertices->mV2.z;
            m_pDstVertices[offset++] = Vertices->mV2.u;
            m_pDstVertices[offset++] = Vertices->mV2.v;

            list_del(&(Vertices->next), head);
            m_nSphereVerticesCount += 3;
            DELETE_BUFFER(Vertices);
        }
    }
}

