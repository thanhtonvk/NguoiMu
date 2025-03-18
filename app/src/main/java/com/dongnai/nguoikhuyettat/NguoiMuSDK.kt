package com.dongnai.nguoikhuyettat

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.view.Surface

class NguoiMuSDK {
    external fun loadModel(
        assetManager: AssetManager?,
        yoloDetect: Int,
        faceDectector: Int,
        trafficLight: Int,
        camDiec: Int,
        money: Int,
        chuCai: Int
    ): Boolean

    external fun openCamera(facing: Int): Boolean

    external fun closeCamera(): Boolean

    external fun setOutputWindow(surface: Surface?): Boolean

    val listResult: List<String?>?
        external get

    val embedding: String?
        external get

    val faceAlign: Bitmap?
        external get

    external fun getEmbeddingFromPath(path: String?): String?

    external fun getFaceAlignFromPath(path: String?): Bitmap?

    val lightTraffic: String?
        external get

    val emotion: String?
        external get

    val deaf: String?
        external get
    val chuCai: String?
        external get

    val listMoneyResult: List<String?>?
        external get

    val image: Bitmap?
        external get

    companion object {
        init {
            System.loadLibrary("nguoimusdk")
        }
    }
}
