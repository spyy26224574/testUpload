#include "HbxFishEyeParameters.h"

CHbxFishEyeParameters::CHbxFishEyeParameters(int id)
{
	m_nFishEyeId = id;
	m_AMatlab = NULL;
	m_KMatlab = NULL;
}

CHbxFishEyeParameters::~CHbxFishEyeParameters()
{
	DELETE_BUFFER(m_AMatlab);
	DELETE_BUFFER(m_KMatlab);
	m_nFishEyeId = 0;
	m_KMatlabLen = 0;
}

CHbxFishEyeParameters::CHbxFishEyeParameters()
{
	m_nFishEyeId = 0;
	m_AMatlab = new float[5];
	m_KMatlabLen = 9;
	m_KMatlab = new float[m_KMatlabLen];
	m_CXMatlab = 947.1886854513804f;
	m_CYMatlab = 939.8795157766352f;

	m_AMatlab[0] = 533.7996342269008f;
	m_AMatlab[1] = 0.0f;
	m_AMatlab[2] = -0.0008278378152f;
	m_AMatlab[3] = 0.0000005448740f;
	m_AMatlab[4] = -0.0000000010049f;

	m_KMatlab[0] = 7.0772601389409f;
	m_KMatlab[1] = -29.6522818822685f;
	m_KMatlab[2] = 31.1812684772714f;
	m_KMatlab[3] = 8.4841842321290f;
	m_KMatlab[4] = 11.1618129673944f;
	m_KMatlab[5] = -72.3612715035643f;
	m_KMatlab[6] = -18.7423650388933f;
	m_KMatlab[7] = -377.7571135440194f;
	m_KMatlab[8] = 739.9475975130548f;

	/////////////////////////////////
	m_ImageWidth = 1920.0f;
	m_ImageHigh = 1080.0f;
	m_R = 740;
	//
	m_fStartAngle = 25.0f;
	m_fEndAngle = 120.0f;
}

