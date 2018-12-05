package com.adai.gkd.bean.square;

import java.io.Serializable;

/**
 * 单个视频实体
 *
 * @author admin
 */
public class VideoGridBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * 文件类型(‘100’:代表视频,’200’:代表图片)
     */
    public String fileType;
    /**
     * 视频id
     */
    public int resourceId;
    /**
     * 上传视频用户id
     */
    public int userId;
    /**
     * 封面
     */
    public String coverPicture;

    public String url;

    public String des;

    public int browseCount;

    public String uploadDate;
    /**
     * 违章审核状态
     */
    public int approveState;
}
