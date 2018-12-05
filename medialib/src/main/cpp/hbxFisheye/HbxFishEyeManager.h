#pragma once

#include "hbxFishEyeList.h"
#include "HbxFishEyeMatlab.h"
#include "HbxReadFileInfo.h"
#include "HbxVerticesFile.h"
#include "HbxWriteFileInfo.h"
#include "utilbase.h"

#ifndef  _HBX_FISHEYE_MANNAGER_H_
#define  _HBX_FISHEYE_MANNAGER_H_


class CHbxFishEyeManager {
public:
    CHbxFishEyeManager();

    ~CHbxFishEyeManager();

public:
    static CHbxFishEyeManager *GetInstance() {
        if (m_pInstance == NULL)
            m_pInstance = new CHbxFishEyeManager();
        return m_pInstance;
    }

    void Release() {
        if (m_pInstance)
            delete m_pInstance;
    }

private:
    static CHbxFishEyeManager *m_pInstance;
    //
    CHbxFishEyeList m_List;
    CHbxFishEyeMatlab m_FishEyeMatlab;
protected:
    char m_chVerticesFileDir[256];

    void MakeVerticesFile(CHbxFishEyeParameters *parame);

    void UpdateCalibration(char *file);

    int m_preStatus;
public:
    CHbxFishEyeParameters *m_CurParame;
    //
    CHbxFishEyeVertices *m_FishEyeVertices;

    //
    void Init(const char *dirpath);

    //
    int GetVertices(int nId, int nWidth, int nHigh);

    //
    int GetVertices(int nId, int nWidth, int nHigh, int ntype);

    // get meidia type
    int MediaFileType(char *mediafile, int *ftype, int *fisheyeid);

    //make fisheye file
    int MakeFishEyeFile(char *mediafile, int ftype, int fisheyeid);

    // update calibration and vertices,file is calibration
    void UpdateVertices(char *file);

    //
    void UpdateVertices();

    //
    void ClearVertices();

    //
    int GetCalibrationSn();
    //

    // setting calibration and vertices dir path ,when app startup
    void DirVerticesFile(char *file);

    //
    int Original();

    int DPannel();

    int QPannel() {};

    int Sphere();

    int Pannel();

    int Cylinder();
    //
public:
    int m_nfOutputVerticesCount;
    float *m_pfOutputVertices;
};

#endif // ! 
