#include "HbxFishEyeList.h"

enum {
    EFISHP1 = 1, EFISHP2
};

CHbxFishEyeList::CHbxFishEyeList() {
    list_init(&m_List);
}

CHbxFishEyeList::~CHbxFishEyeList() {
    Clean();
}

void CHbxFishEyeList::Push(CHbxFishEyeParameters *Parameters) {
    CHbxFishEyeParameters *parame = NULL;
    int id = Parameters->m_nFishEyeId;
    int width = (int) Parameters->m_ImageWidth;
    int high = (int) Parameters->m_ImageHigh;

    if (m_List.next) {
        struct list_head *head = &m_List;
        for (parame = (CHbxFishEyeParameters *) head->next;
             head->next != NULL; parame = (CHbxFishEyeParameters *) head->next) {
            if (parame->m_nFishEyeId == id) {
                int nWidth = (int) parame->m_ImageWidth;
                int nHeight = (int) parame->m_ImageHigh;
                if ((nWidth == width) && (nHeight == high)) {
                    //old parame
                    return;
                }
            }
            head = head->next;
        }
    }
    //
    //new parame
    list_add_tail(&(Parameters->next), &m_List);
}

void CHbxFishEyeList::Init() {
}


CHbxFishEyeParameters *CHbxFishEyeList::FishEyeParameters(int id, int width, int high) {
    CHbxFishEyeParameters *parame = NULL;
    if (m_List.next) {
        struct list_head *head = &m_List;
        for (parame = (CHbxFishEyeParameters *) head->next;
             head->next != NULL; parame = (CHbxFishEyeParameters *) head->next) {
            if (parame->m_nFishEyeId == id) {
                int nWidth = (int) parame->m_ImageWidth;
                int nHeight = (int) parame->m_ImageHigh;
                if ((nWidth == width) && (nHeight == high)) {
                    return parame;
                }
            }
            head = head->next;
        }
    }
    return NULL;
}

void CHbxFishEyeList::Clean() {
    CHbxFishEyeParameters *parame = NULL;
    if (m_List.next) {
        struct list_head *head = &m_List;
        for (parame = (CHbxFishEyeParameters *) head->next;
             head->next != NULL; parame = (CHbxFishEyeParameters *) head->next) {
            list_del(&(parame->next), head);
            DELETE_BUFFER(parame);
        }
    }
}