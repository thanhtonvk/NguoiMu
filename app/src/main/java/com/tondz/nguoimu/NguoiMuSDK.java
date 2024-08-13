package com.tondz.nguoimu;

import android.content.res.AssetManager;
import android.view.Surface;

import java.util.List;

public class NguoiMuSDK {
    public native boolean loadModel(AssetManager mgr, int modelid, int cpugpu);
    public native boolean openCamera(int facing);
    public native boolean closeCamera();
    public native boolean setOutputWindow(Surface surface);
    public native List<String> getListResult();

    static {
        System.loadLibrary("nguoimusdk");
    }
}
