package com.example.morsecode

import android.content.Context
import android.graphics.Bitmap
import android.media.Image
import android.media.Image.Plane
import android.renderscript.*
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer

class YUVtoMAT {
    private val TAG = "YuvToMatConverter"
    private var rs: RenderScript? = null
    private var rgba = Mat()
    private var yMat = Mat()
    private var uMat = Mat()
    private var yuvMat = Mat()
    private var yuvBuffer: ByteBuffer? = null
    private var tempBitmap = Bitmap.createBitmap(1,1,Bitmap.Config.ARGB_8888)

    fun init(context: Context){
        rs = RenderScript.create(context)
    }

    fun convert(mImage: Image): Long? {
        checkYuvBufferAllocation(mImage)

        return yuvBuffer?.let { tunedOldConvert(mImage, it) }
    }

    private fun checkYuvBufferAllocation(mImage: Image) {
        yuvBuffer ?: run { yuvBuffer = ByteBuffer.allocateDirect(mImage.height * mImage.width * 2)}
    }

    private fun tunedOldConvert(image: Image, yuvBuffer: ByteBuffer): Long{
        val start = System.currentTimeMillis()

        yuvBuffer.run {
            rewind()
            position(0)
            image.planes.forEach { put(it.buffer) }
        }

        yuvMat = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1, yuvBuffer)
        Imgproc.cvtColor(yuvMat, rgba, Imgproc.COLOR_YUV2RGB_NV12)

        //Log.d(TAG, "tunedOldConvert: Time to convert img = ${System.currentTimeMillis() - start}")
        return rgba.nativeObjAddr
    }

    private fun oldConvert(mImage: Image): Long
    {
        mImage.run {
            //Only for NV21
            //Todo: move to c++ side
            val start = System.currentTimeMillis()
            // https://github.com/opencv/opencv/blob/master/modules/java/generator/android-21/java/org/opencv/android/JavaCamera2View.java#L344
            yMat = Mat(height, width, CvType.CV_8UC1, planes[0].buffer)
            uMat = Mat(height / 2, width / 2, CvType.CV_8UC2, planes[2].buffer)

            Imgproc.cvtColorTwoPlane(yMat, uMat, rgba, Imgproc.COLOR_YUV2RGB_NV12)

            Log.d(TAG, "oldConvert: Time to convert img = ${System.currentTimeMillis() - start}")
            return rgba.nativeObjAddr
        }
    }

    private fun interesticConvert(mImage: Image): Long? {
        //Reuse existing buffer - https://github.com/gordinmitya/yuv2buf
        rs?.run {
            val start = System.currentTimeMillis()
            tempBitmap = yuvBuffer?.let {
                it.rewind()
                it.position(0)
                mImage.planes.forEach { plane-> it.put(plane.buffer) }
                //https://stackoverflow.com/questions/17390289/convert-bitmap-to-mat-after-capture-image-using-android-camera
                yuvByteArrayToBitmap(this, it.array(), mImage.width, mImage.height, Bitmap.Config.RGB_565)
            } ?: return null
            Utils.bitmapToMat(tempBitmap, rgba)
            //Imgproc.cvtColor(rgba, rgba, Imgproc.COLOR_RGBA2RGB)

            Log.d(TAG, "interesticConvert: Time to convert img = ${System.currentTimeMillis() - start}")
            return rgba.nativeObj
        }

        return null
    }

    private fun yuvByteArrayToBitmap(rs: RenderScript, bytes: ByteArray, width: Int, height: Int, bitmapConfig: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
        val yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        val yuvType = Type.Builder(rs, Element.U8(rs)).setX(bytes.size);
        val input = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

        val rgbaType = Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
        val output = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

        input.copyFrom(bytes);

        yuvToRgbIntrinsic.setInput(input);
        yuvToRgbIntrinsic.forEach(output);

        val bitmap = Bitmap.createBitmap(width, height, bitmapConfig)
        output.copyTo(bitmap)

        input.destroy()
        output.destroy()
        yuvToRgbIntrinsic.destroy()

        return bitmap
    }

    fun destroy(){
        rs?.destroy()
        rs = null
    }

    private fun cvOfficialYuvToRgbaMat(mImage: Image): Mat? {
        val planes: Array<Plane> = mImage.planes
        val w: Int = mImage.width
        val h: Int = mImage.height
        val chromaPixelStride = planes[1].pixelStride
        mImage.run {

        }
        return if (chromaPixelStride == 2) { // Chroma channels are interleaved
            val y_plane = planes[0].buffer
            val y_plane_step = planes[0].rowStride
            val uv_plane1 = planes[1].buffer
            val uv_plane1_step = planes[1].rowStride
            val uv_plane2 = planes[2].buffer
            val uv_plane2_step = planes[2].rowStride
            val y_mat = Mat(h, w, CvType.CV_8UC1, y_plane, y_plane_step.toLong())
            val uv_mat1 = Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane1, uv_plane1_step.toLong())
            val uv_mat2 = Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane2, uv_plane2_step.toLong())
            val addr_diff = uv_mat2.dataAddr() - uv_mat1.dataAddr()
            if (addr_diff > 0) {
                Imgproc.cvtColorTwoPlane(y_mat, uv_mat1, rgba, Imgproc.COLOR_YUV2RGBA_NV12)
            } else {
                Imgproc.cvtColorTwoPlane(y_mat, uv_mat2, rgba, Imgproc.COLOR_YUV2RGBA_NV21)
            }
            rgba
        } else { // Chroma channels are not interleaved
            val yuv_bytes = ByteArray(w * (h + h / 2))
            val y_plane = planes[0].buffer
            val u_plane = planes[1].buffer
            val v_plane = planes[2].buffer
            var yuv_bytes_offset = 0
            val y_plane_step = planes[0].rowStride
            if (y_plane_step == w) {
                y_plane[yuv_bytes, 0, w * h]
                yuv_bytes_offset = w * h
            } else {
                val padding = y_plane_step - w
                for (i in 0 until h) {
                    y_plane[yuv_bytes, yuv_bytes_offset, w]
                    yuv_bytes_offset += w
                    if (i < h - 1) {
                        y_plane.position(y_plane.position() + padding)
                    }
                }
            }
            val chromaRowStride = planes[1].rowStride
            val chromaRowPadding = chromaRowStride - w / 2
            if (chromaRowPadding == 0) {
                // When the row stride of the chroma channels equals their width, we can copy
                // the entire channels in one go
                u_plane[yuv_bytes, yuv_bytes_offset, w * h / 4]
                yuv_bytes_offset += w * h / 4
                v_plane[yuv_bytes, yuv_bytes_offset, w * h / 4]
            } else {
                // When not equal, we need to copy the channels row by row
                for (i in 0 until h / 2) {
                    u_plane[yuv_bytes, yuv_bytes_offset, w / 2]
                    yuv_bytes_offset += w / 2
                    if (i < h / 2 - 1) {
                        u_plane.position(u_plane.position() + chromaRowPadding)
                    }
                }
                for (i in 0 until h / 2) {
                    v_plane[yuv_bytes, yuv_bytes_offset, w / 2]
                    yuv_bytes_offset += w / 2
                    if (i < h / 2 - 1) {
                        v_plane.position(v_plane.position() + chromaRowPadding)
                    }
                }
            }
            val yuv_mat = Mat(h + h / 2, w, CvType.CV_8UC1)
            yuv_mat.put(0, 0, yuv_bytes)
            Imgproc.cvtColor(yuv_mat, rgba, Imgproc.COLOR_YUV2RGBA_I420, 4)
            rgba
        }
    }
}