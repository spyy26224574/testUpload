package SunGps;

/*
 * SunGps
*/

/**
 * Callback interface for SunGps class
 * If you need frame data as ByteBuffer, you can use this callback interface with SunGps#sunGpsReadCallBack
 */
public interface IHttpReadCallback {
	/**
	 * buffer:保存数据
	 * pos:读文件起始地址
	 * len:读文件长度
	 * id: id 为0表示为文件长度，1 为文件内容
	 * 返回值: 分两种情况:
	 * 1 去过是获取文件长度，就直接返回长度
	 * 2 如果是获取文件内容，就直接返回读到数据长度；
	 */
	long onRead(byte[] buffer, long pos, int len, int id);
}

