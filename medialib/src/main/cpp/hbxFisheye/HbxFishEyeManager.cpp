#include "HbxFishEyeManager.h"

#if 0
static float OriginalvertexVertices[] = {
    -16.0f / 9.0f, -1.0f, 0.0f, 0.0f, 1.0f,
    16.0f / 9.0f,  -1.0f, 0.0f, 1.0f, 1.0f,
    -16.0f / 9.0f,  1.0f, 0.0f, 0.0f, 0.0f,
    16.0f / 9.0f,   1.0f, 0.0f, 1.0f, 0.0f,
};
#endif
static float OriginalvertexVertices[] = {
        -1.0f, -1.0f, 0.0f, 0.0f, 1.0f,
        1.0f, -1.0f, 0.0f, 1.0f, 1.0f,
        -1.0f, 1.0f, 0.0f, 0.0f, 0.0f,
        1.0f, 1.0f, 0.0f, 1.0f, 0.0f,
};

CHbxFishEyeManager *CHbxFishEyeManager::m_pInstance = NULL;

CHbxFishEyeManager::CHbxFishEyeManager() {
    m_CurParame = NULL;
    m_FishEyeVertices = NULL;

    m_nfOutputVerticesCount = 0;
    m_pfOutputVertices = NULL;
    //
    memset(m_chVerticesFileDir, 0, 256);
}

CHbxFishEyeManager::~CHbxFishEyeManager() {
    DELETE_BUFFER(m_FishEyeVertices);
    m_nfOutputVerticesCount = 0;
    DELETE_BUFFER(m_pfOutputVertices);
}

void CHbxFishEyeManager::Init(const char *dirpath) {
    m_List.Init();
    //
    if (dirpath) {
        sprintf(m_chVerticesFileDir, "%s", dirpath);
    }
    //
    char *pchFilePathName = NULL;
    if (!pchFilePathName)
        pchFilePathName = new char[1024];

    memset(pchFilePathName, 0, 1024);
    sprintf(pchFilePathName, "%s/calibration.bin", m_chVerticesFileDir);
    //
    UpdateCalibration(pchFilePathName);
    DELETE_BUFFER(pchFilePathName);
}

int CHbxFishEyeManager::GetVertices(int nId, int nWidth, int nHigh) {
    int nRet = -1;
    float fWidth = nWidth;
    float fnHigh = nHigh;
    //
    float *data = NULL;
    int nSize = 0;
    CHbxVerticesFile vfile;
    //
    char *pchFilePathName = NULL;
    CHbxFishEyeParameters *Parame = m_List.FishEyeParameters(nId, nWidth, nHigh);
    //
    if (!Parame) {
        m_preStatus = -1;
        return -1;
    }

    if (Parame == m_CurParame) {
        return m_preStatus;
    }

    CHbxFishEyeVertices *Vertices = new CHbxFishEyeVertices();
    //read vertics
    if (Vertices) {
        //make vertices data file
        MakeVerticesFile(Parame);
        //
        if (pchFilePathName == NULL) {
            pchFilePathName = new char[1024];
        }
        //
        sprintf(pchFilePathName, "%s/%d&%d&%d_pannel.bin", m_chVerticesFileDir, nId, nWidth,
                nHigh);
        int bExist = vfile.Access(pchFilePathName);
        if (bExist) {
            vfile.Open(pchFilePathName, "rb");
            vfile.Read(&data, nSize);
            vfile.Close();
            Vertices->m_nPannelVerticesCount = nSize / 5;
            Vertices->m_pPannelDstVertices = data;
        } else {
            goto End;
        }
        //
        sprintf(pchFilePathName, "%s/%d&%d&%d_cy.bin", m_chVerticesFileDir, nId, nWidth, nHigh);
        bExist = vfile.Access(pchFilePathName);
        if (bExist) {
            vfile.Open(pchFilePathName, "rb");
            vfile.Read(&data, nSize);
            vfile.Close();
            Vertices->m_nCyVerticesCount = nSize / 5;
            Vertices->m_pCyDstVertices = data;
        } else {
            goto End;
        }
        //
        sprintf(pchFilePathName, "%s/%d&%d&%d_sphere.bin", m_chVerticesFileDir, nId, nWidth,
                nHigh);
        bExist = vfile.Access(pchFilePathName);
        if (bExist) {
            vfile.Open(pchFilePathName, "rb");
            vfile.Read(&data, nSize);
            vfile.Close();
            Vertices->m_nSphereVerticesCount = nSize / 5;
            Vertices->m_pSphereDstVertices = data;
        } else {
            goto End;
        }
    }
    nRet = 0;
    End:
    CHbxFishEyeVertices *TVertice = m_FishEyeVertices;
    m_FishEyeVertices = Vertices;
    //
    m_CurParame = Parame;
    //
    OriginalvertexVertices[0] = -fWidth / fnHigh;
    OriginalvertexVertices[5] = fWidth / fnHigh;
    OriginalvertexVertices[10] = -fWidth / fnHigh;
    OriginalvertexVertices[15] = fWidth / fnHigh;
    //
    DELETE_BUFFER(TVertice);
    DELETE_BUFFER(pchFilePathName);
    m_preStatus = nRet;
    return nRet;
}


int CHbxFishEyeManager::GetVertices(int nId, int nWidth, int nHigh, int nType) {
    int nRet = -1;
    float fWidth = nWidth;
    float fnHigh = nHigh;
    //
    float *data = NULL;
    int nSize = 0;
    CHbxVerticesFile vfile;
    //
    char *pchFilePathName = NULL;
    CHbxFishEyeParameters *Parame = m_List.FishEyeParameters(nId, nWidth, nHigh);
    //
    if (!Parame) {
        m_preStatus = -1;
        return -1;
    }
    //
    m_nfOutputVerticesCount = 0;
    DELETE_BUFFER(m_pfOutputVertices);
    //
    //make vertices data file
    MakeVerticesFile(Parame);
    //
    if (pchFilePathName == NULL) {
        pchFilePathName = new char[1024];
    }
    //
    if (nType == 0)
        sprintf(pchFilePathName, "%s/%d&%d&%d_pannel.bin", m_chVerticesFileDir, nId, nWidth, nHigh);
    else if (nType == 1)
        sprintf(pchFilePathName, "%s/%d&%d&%d_cy.bin", m_chVerticesFileDir, nId, nWidth, nHigh);
    else if (nType == 2)
        sprintf(pchFilePathName, "%s/%d&%d&%d_sphere.bin", m_chVerticesFileDir, nId, nWidth, nHigh);

    int bExist = vfile.Access(pchFilePathName);
    if (bExist) {
        vfile.Open(pchFilePathName, "rb");
        vfile.Read(&data, nSize);
        vfile.Close();

        m_nfOutputVerticesCount = nSize / 5;
        m_pfOutputVertices = data;
    } else {
        goto End;
    }
    //
    nRet = 0;
    m_CurParame = Parame;
    End:
    DELETE_BUFFER(pchFilePathName);
    return nRet;
}

int CHbxFishEyeManager::Original() {
    m_pfOutputVertices = OriginalvertexVertices;
    m_nfOutputVerticesCount = 4;
    return 0;
}

int CHbxFishEyeManager::DPannel() {
    if (m_FishEyeVertices) {
        m_pfOutputVertices = m_FishEyeVertices->m_pPannelDstVertices;
        m_nfOutputVerticesCount = m_FishEyeVertices->m_nPannelVerticesCount;
    }
    return -1;
}

int CHbxFishEyeManager::Sphere() {
    if (m_FishEyeVertices) {
        m_pfOutputVertices = m_FishEyeVertices->m_pSphereDstVertices;
        m_nfOutputVerticesCount = m_FishEyeVertices->m_nSphereVerticesCount;
    }
    return -1;
}

int CHbxFishEyeManager::Pannel() {
    if (m_FishEyeVertices) {
        m_pfOutputVertices = m_FishEyeVertices->m_pPannelDstVertices;
        m_nfOutputVerticesCount = m_FishEyeVertices->m_nPannelVerticesCount;
    }
    return -1;
}

int CHbxFishEyeManager::Cylinder() {
    if (m_FishEyeVertices) {
        m_pfOutputVertices = m_FishEyeVertices->m_pCyDstVertices;
        m_nfOutputVerticesCount = m_FishEyeVertices->m_nCyVerticesCount;
    }
    return -1;
}

void CHbxFishEyeManager::UpdateCalibration(char *file) {
    // calibration
    CHbxReadFileInfo readInfo;
    //
    if (strlen(m_chVerticesFileDir) <= 5)
        return;
    //
    readInfo.Open(file);
    CHbxFishEyeParameters *parame = NULL;
    if (readInfo.m_CalibrationList.next) {
        struct list_head *head = &(readInfo.m_CalibrationList);
        for (parame = (CHbxFishEyeParameters *) head->next;
             head->next != NULL; parame = (CHbxFishEyeParameters *) head->next) {
            list_del(&(parame->next), head);
            m_List.Push(parame);
        }
    }
}

void CHbxFishEyeManager::DirVerticesFile(char *file) {
    char *pchFilePathName = NULL;
    if (file) {
        sprintf(m_chVerticesFileDir, "%s", file);
        if (!pchFilePathName)
            pchFilePathName = new char[1024];

        memset(pchFilePathName, 0, 1024);
        sprintf(pchFilePathName, "%s/calibration.bin", file);
        UpdateCalibration(pchFilePathName);
    }
    DELETE_BUFFER(pchFilePathName);
};

void CHbxFishEyeManager::UpdateVertices(char *file) {
    int nId = 0;
    int nWidth, nHigh;
    CHbxFishEyeParameters *parame = NULL;
    //
    CHbxReadFileInfo readInfo;
    //
    if (strlen(m_chVerticesFileDir) <= 5)
        return;
    //update caloibration data
    UpdateCalibration(file);
    if (m_List.m_List.next) {
        struct list_head *head = &(m_List.m_List);
        for (parame = (CHbxFishEyeParameters *) head->next;
             head->next != NULL; parame = (CHbxFishEyeParameters *) head->next) {
            //make vertice file
            MakeVerticesFile(parame);
            head = head->next;
        }
    }
}

void CHbxFishEyeManager::UpdateVertices() {
    char *pchFilePathName = NULL;
    if (!pchFilePathName)
        pchFilePathName = new char[1024];

    memset(pchFilePathName, 0, 1024);
    sprintf(pchFilePathName, "%s/calibration.bin", m_chVerticesFileDir);
    LOGE("calibration.bin = %s", pchFilePathName);
    //
    UpdateVertices(pchFilePathName);
    DELETE_BUFFER(pchFilePathName);
}

void CHbxFishEyeManager::MakeVerticesFile(CHbxFishEyeParameters *parame) {
    int nWidth, nHigh, nId;
    CHbxVerticesFile vfile;
    CHbxFishEyeVertices Vertices;
    char *pchFilePathName = NULL;
    //
    if (pchFilePathName == NULL) {
        pchFilePathName = new char[1024];
    }
    memset(pchFilePathName, 0, 1024);
    //
    nWidth = (int) parame->m_ImageWidth;
    nHigh = (int) parame->m_ImageHigh;
    nId = parame->m_nFishEyeId;
    //
    sprintf(pchFilePathName, "%s/%d&%d&%d_pannel.bin", m_chVerticesFileDir, nId, nWidth, nHigh);
    //vertices file  is exist
    int bExist = vfile.Access(pchFilePathName);
    if (bExist == 0) {
        //make vertices data
        m_FishEyeMatlab.MakePannelVertices(parame, &Vertices);
        bool bRet = vfile.Open(pchFilePathName);
        if (true == bRet) {
            vfile.Write(Vertices.m_pPannelDstVertices, Vertices.m_nPannelVerticesCount * 5);
            vfile.Close();
        }
    }
    //
    memset(pchFilePathName, 0, 1024);
    sprintf(pchFilePathName, "%s/%d&%d&%d_sphere.bin", m_chVerticesFileDir, nId, nWidth, nHigh);
    //vertices file  is exist
    bExist = vfile.Access(pchFilePathName);
    if (bExist == 0) {
        //make vertices data
        m_FishEyeMatlab.MakeSphereVertices(parame, &Vertices);
        bool bRet = vfile.Open(pchFilePathName);
        if (true == bRet) {
            vfile.Write(Vertices.m_pSphereDstVertices, Vertices.m_nSphereVerticesCount * 5);
            vfile.Close();
        }
    }
    //
    memset(pchFilePathName, 0, 1024);
    sprintf(pchFilePathName, "%s/%d&%d&%d_cy.bin", m_chVerticesFileDir, nId, nWidth, nHigh);
    bExist = vfile.Access(pchFilePathName);
    if (bExist == 0) {
        //make vertices data
        m_FishEyeMatlab.MakeCylinderVertices(parame, &Vertices);
        bool bRet = vfile.Open(pchFilePathName);
        if (true == bRet) {
            vfile.Write(Vertices.m_pCyDstVertices, Vertices.m_nCyVerticesCount * 5);
            vfile.Close();
        }
    }
    DELETE_BUFFER(pchFilePathName);
}

//
void CHbxFishEyeManager::ClearVertices() {

}

// get meidia type
int CHbxFishEyeManager::MediaFileType(char *mediafile, int *ftype, int *fisheyeid) {
    CHbxReadFileInfo file;
    file.OpenMediaFile(mediafile);
    *fisheyeid = file.m_nFishEyeId;
    *ftype = file.m_nFileType;
    return *ftype;
}

//make fisheye file
int CHbxFishEyeManager::MakeFishEyeFile(char *mediafile, int ftype, int fisheyeid) {
    CHbxWriteFileInfo file;
    file.Open(mediafile);
    file.AddFileTypeBlock(ftype);
    file.AddFishEyeIDBlock(fisheyeid);
    file.AddIndexBlock();
    file.AddTailBlock();
    file.Close();
    return 0;
}

int CHbxFishEyeManager::GetCalibrationSn() {
    char *pchFilePathName = NULL;
    if (!pchFilePathName)
        pchFilePathName = new char[1024];

    memset(pchFilePathName, 0, 1024);
    sprintf(pchFilePathName, "%s/calibration.bin", m_chVerticesFileDir);
    //

    CHbxReadFileInfo file;
    file.Open(pchFilePathName);

    DELETE_BUFFER(pchFilePathName);
    return file.m_nSn;
}