#include "public.h"

#ifndef  _HBX_THREAD_
#define  _HBX_THREAD_


typedef void (*HBXFUNCTION)(void *);

typedef void (*HBXUPDATEMEDIAINFO)(int, char *);

class CHbxMutex {
public:
    CHbxMutex();

    ~CHbxMutex();

#ifdef _IOS_
    pthread_mutex_t m_Mutex;
#endif

#ifdef _ANDROID_
    pthread_mutex_t m_Mutex;
#endif
#ifdef _WIN_
    CRITICAL_SECTION m_Mutex;
#endif

public:
    void Lock();

    void UnLock();
};


class CHbxThread {
public:
    static long long Clock();

public:
    CHbxThread();

    ~CHbxThread();

protected:
    long m_wThreadId;
    int m_ThrStatus;
    CHbxMutex m_ThreadMutex;

#ifndef _WIN_
    pthread_cond_t m_Cond;
#endif

public:
    struct MediaInfo *m_MediaInfo;
    HBXFUNCTION m_pCallback;
    void *m_pVold;

    void CreatNewThread(HBXFUNCTION callback, void *pVoid);

    virtual void CreatThread() { return; };

    virtual void ExitThread();

    virtual void Run() {};

    void Status(int status) { m_ThrStatus = status; };
public:
};


class CHbxInteractive {
public:
    CHbxInteractive() {};

    ~CHbxInteractive() {};
    //
public:
    void UpdateMediaInfo(char *tag, int size);

public:
    static HBXUPDATEMEDIAINFO m_cbUpdateMediaInfo;
};

#endif // ! _HBX_THREAD_
