package com.adai.camera;

import com.adai.camera.product.INovatekCamera;
import com.adai.camera.product.ISunplusCamera;

/**
 * Created by huangxy on 2017/3/23.
 */

public abstract class AbstractCameraFactory {
    public abstract INovatekCamera getNovatekCamera();


    public abstract ISunplusCamera getSunplusCamera();
}
