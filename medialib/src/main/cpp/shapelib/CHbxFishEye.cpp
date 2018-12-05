#include "CHbxFishEye.h"


static float OriginalvertexVertices[] = {
	-1.0f, -1.0f, 0.0f, 0.0f, 1.0f,
	1.0f,  -1.0f, 0.0f, 1.0f, 1.0f,
	-1.0f,  1.0f, 0.0f, 0.0f, 0.0f,
	1.0f,   1.0f, 0.0f, 1.0f, 0.0f,
};

CHbxFishEye::CHbxFishEye()
{
	m_pBaseVertices = OriginalvertexVertices;
	m_nBaseVerticesCount = 4;
}


CHbxFishEye::~CHbxFishEye()
{
}
