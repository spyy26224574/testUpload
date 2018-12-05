package SunGps;


public class SunGpsInterface {
    static {
        System.loadLibrary("sungps");
    }

    //设置加密方式
    public native int SunSetEncType(int type);

    //获取加密方式
    public native int SunGetEncType();

    //编解码一段字符串,如果成功返回0，如果失败返回-1
    public native int SunEncrypt(byte[] inbuf, int inlen, byte[] outbuf, byte[] outlen);

    //解码一段字符串,如果成功返回0，如果失败返回-1
    public native int SunDecrypt(byte[] inbuf, int inlen, byte[] outbuf, byte[] outlen, int type);

    //生成数据头,如果成功返回0，如果失败返回-1
    public native int MakeHeader(byte output[], int count);

    //生成非加密数据块,如果成功返回0，如果失败返回-1
    public native int MakeRawDataBlock(int nframe, byte chInfomation[], int chInfolength, byte chOutput[]);

    //加密数据，使用不带nframe的chInfomation数据加密,如果成功返回0，如果失败返回-1
    public native int MakeEncryptDataBlock(int nframe, byte chInfomation[], int chInfolength, byte chOutput[]);

    //加密数据，使用带nframe的chInfomation数据加密,如果成功返回0，如果失败返回-1
    public native int MakeEncryptDataBlockII(byte chInfomation[], int chInfolength, byte chOutput[]);

    //
    public native void SunDecodeInfo(byte[] file); //解码

    public native int SunGetFileType();   //获取文件类型

    public native int SunGetEyeFishID();  //获取解码类型

    public native void SunSetFileInfo(byte[] file, int type, int eyefishid); //写入全景文件类型

    public native String sunGpsDecode(String filepath, int ntype);

    public native void sunGpsReadCallBack(IHttpReadCallback callback);

    public native void sunGpsInit();

    public native void sunGpsExit();
}
