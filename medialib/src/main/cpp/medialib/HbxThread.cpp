
#include "HbxThread.h"

HBXUPDATEMEDIAINFO CHbxInteractive::m_cbUpdateMediaInfo = NULL;
CHbxMutex::CHbxMutex()
{
#ifdef _IOS_
    pthread_mutex_init(&m_Mutex, NULL);
#endif
#ifdef _ANDROID_  
	pthread_mutex_init(&m_Mutex, NULL);
#endif
#ifdef _WIN_
	InitializeCriticalSection(&m_Mutex);
#endif
}
CHbxMutex::~CHbxMutex()
{
#ifdef _IOS_
    pthread_mutex_destroy(&m_Mutex);
#endif
#ifdef _ANDROID_  
	pthread_mutex_destroy(&m_Mutex);
#endif
#ifdef _WIN_
	DeleteCriticalSection(&m_Mutex);
#endif
}
void CHbxMutex::Lock()
{
#ifdef _IOS_
    pthread_mutex_lock(&m_Mutex);
#endif
#ifdef _ANDROID_  
	pthread_mutex_lock(&m_Mutex);
#endif
#ifdef _WIN_  
	EnterCriticalSection(&m_Mutex);
#endif
}

void CHbxMutex::UnLock()
{
#ifdef _IOS_
    pthread_mutex_unlock(&m_Mutex);
#endif
#ifdef _ANDROID_ 
	pthread_mutex_unlock(&m_Mutex);
#endif
#ifdef _WIN_  
	LeaveCriticalSection(&m_Mutex);
#endif
}

#ifdef _ANDROID_ 
static void * FuncTionThread(void *pVold)
#endif
#ifdef _WIN_ 
static DWORD WINAPI FuncTionThread(void *pVold)
#endif
#ifdef _IOS_
static void * FuncTionThread(void *pVold)
#endif
{
	CHbxThread *pThread = (CHbxThread *)pVold;
	if (pThread->m_pCallback)
		pThread->m_pCallback(pThread->m_pVold);
	else {
		if (pThread)
			pThread->Run();
	}
	return 0;
}

long long CHbxThread::Clock()
{
#ifdef _ANDROID_
    struct timespec tTemp ={0,0};
    clock_gettime(CLOCK_MONOTONIC,&tTemp);
	return  (tTemp.tv_sec * 1000 + tTemp.tv_nsec/1000000);
#endif
#ifdef _WIN_ 
	return GetTickCount();
#endif
#ifdef _IOS_
    struct timespec tTemp ={0,0};
    if (__builtin_available(iOS 10.0, *)) {
        clock_gettime(CLOCK_MONOTONIC,&tTemp);
    } else {
        // Fallback on earlier versions
        struct timeval tms;
        char tstr[100];
        timerclear(&tms);
        gettimeofday(&tms,NULL);
        strftime(tstr,100,"%X",localtime(&tms.tv_sec));
        return tms.tv_usec/1000; /*tv_usec��΢�룬����1000ת��Ϊ����*/
    }
    return  (tTemp.tv_sec * 1000 + tTemp.tv_nsec/1000000);
#endif
}

CHbxThread::CHbxThread()
{
	m_wThreadId = 0;
	m_pCallback = NULL;
	m_pVold = NULL;
	m_ThrStatus = _STATUS_THREAD_NEW_;
	m_MediaInfo = NULL;
}

CHbxThread::~CHbxThread()
{
	m_wThreadId = 0;
}

void CHbxThread::CreatNewThread(HBXFUNCTION callback,void *pVoid)
{
	m_ThrStatus = _STATUS_THREAD_NEW_;
	m_pCallback  = callback;
	m_pVold = pVoid;
#ifdef _IOS_
    pthread_create((pthread_t *)&m_wThreadId, NULL, FuncTionThread, pVoid);
#endif
#ifdef _ANDROID_ 
	pthread_create(&m_wThreadId, NULL, FuncTionThread, pVoid);
#endif
#ifdef _WIN_ 
	CreateThread(NULL, 0, FuncTionThread, pVoid, 0, (LPDWORD)&m_wThreadId);
#endif
}


void CHbxThread::ExitThread()
{
	m_ThrStatus = _STATUS_THREAD_EXIT_;
	m_ThreadMutex.Lock();
	m_ThreadMutex.UnLock();
	m_wThreadId = 0;
}

void CHbxInteractive::UpdateMediaInfo(char *tag,int size)
{
    if(m_cbUpdateMediaInfo)
        m_cbUpdateMediaInfo(size,tag);
}
