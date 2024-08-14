package com.tondz.nguoimu;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.view.Surface;

import java.util.List;

public class NguoiMuSDK {
    public native boolean loadModel(AssetManager mgr, int modelid, int cpugpu);
    public native boolean openCamera(int facing);
    public native boolean closeCamera();
    public native boolean setOutputWindow(Surface surface);
    public native List<String> getListResult();
    public native String getEmbedding();
    public native Bitmap getFaceAlign();
    static {
        System.loadLibrary("nguoimusdk");
    }
}
