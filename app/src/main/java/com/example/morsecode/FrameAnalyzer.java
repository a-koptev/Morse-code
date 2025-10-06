package com.example.morsecode;


import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import kotlin.Pair;

public class FrameAnalyzer {
    private static Mat ImageToMat(Image image) {

        Mat buf = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        buf.put(0, 0, bytes);

        return Imgcodecs.imdecode(buf, Imgcodecs.IMREAD_COLOR);
    }
    public static Pair< ArrayList<Float[]>, Boolean> analyze(Bitmap image, Context context) {

        if (!OpenCVLoader.initLocal()) {
            CharSequence text = "OPEN CV DIED";
            Toast Toast = null;
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }



        Mat original_mat = new Mat();
        Mat mat_img = new Mat();
        Utils.bitmapToMat(image, original_mat);

        // gamma
//        float invGamma = 1 / 0.1f;
//        Mat table = new Mat(1, 256, CvType.CV_8U);
//        for (int i = 0; i < 256; ++i) {
//            table.put(0, i, (int) (Math.pow(i / 255.0f, invGamma) * 255));
//        }
//        Mat gammaImg = new Mat();
//        Core.LUT(original_mat, table, gammaImg);


        // CLAHE
//        Mat claheImg = new Mat();
//        CLAHE clahe = Imgproc.createCLAHE(2.0, new Size(8,8));
//        clahe.apply(original_mat, claheImg);
//
//        Utils.matToBitmap(claheImg, image);
//        return new Pair<>(image, false);



        //Imgproc.cvtColor(original_mat, mat_img, Imgproc.COLOR_RGB2HSV_FULL);

        Scalar min = new Scalar(255, 255, 255, 255); // RGB
        Scalar max= new Scalar(255, 255, 255, 255); // RGB

//        min = new Scalar(0, 0, 255);  // HSV
//        max= new Scalar(360, 0, 255);  // HSV


        // Imgproc.medianBlur(mat_img, blurred, 5);


        // int[][] ker_arr = new int[][] {{0, -1, 0}, {-1, 5, -1}, {0, -1, 0}};
        // Imgproc.filter2D(blurred, blurred, -1, );

//        Mat sharp = new Mat(mat_img.rows(),mat_img.cols(), mat_img.type());
//        Imgproc.GaussianBlur(mat_img, sharp, new Size(0,0), 10);
//        Core.addWeighted(mat_img, 1.5, sharp, -0.5, 0, sharp);

        Mat blurred = new Mat();

//        int kernelSize = 7;
//        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE,
//                new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
//                new Point(kernelSize, kernelSize));
//        Imgproc.erode(mat_img, blurred, element);


        float dilation_size = 8F;
        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE,
                new Size( 2*dilation_size + 1, 2*dilation_size+1 ),
                new Point( dilation_size, dilation_size ) );
        Imgproc.dilate(original_mat, blurred, element);

//        int kernelSize = 7;
//        Mat element = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE,
//                new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
//                new Point(kernelSize, kernelSize));
//        Imgproc.erode(original_mat, blurred, element);


        Mat ranged = new Mat();
        //Imgproc.cvtColor(blurred, ranged, Imgproc.COLOR_RGB2GRAY);

        Core.inRange(original_mat, min, max, ranged);
        Imgproc.medianBlur(ranged, blurred, 5);
        Imgproc.dilate(blurred, ranged, element);

        // Imgproc.GaussianBlur(ranged, blurred, new Size(21, 21), 0);

        // Core.bitwise_and(mat_img, ranged, mat_img);
        // Imgproc.Canny(blurred, blurred, 200, 255);
        // Imgproc.cvtColor(mat_img, mat_img, Imgproc.COLOR_HSV2RGB_FULL);


        // Mat result = new Mat();

        //Mat gray = new Mat();
        //Imgproc.cvtColor(original_mat, gray, Imgproc.COLOR_RGB2GRAY);
        // Imgproc.cvtColor(blurred, gray, Imgproc.);

        //Imgproc.threshold(gray, gray, 254.7, 255, Imgproc.THRESH_BINARY);
        // Imgproc.cvtColor(blurred, blurred, Imgproc.COLOR_HSV2RGB_FULL);


        Mat circles = new Mat();
        Imgproc.HoughCircles(ranged, circles, Imgproc.HOUGH_GRADIENT, 1.15,
                50, // change this value to detect circles with different distances to each other
                230, 16 , 15, 70); // change the last two parameters
        // (min_radius & max_radius) to detect larger circles

        ArrayList<Float[]> circles_out = new ArrayList<>();


        boolean detected;
        if (circles.empty()) {
            detected = false;
        } else {
            for (int x = 0; x < circles.cols(); x++) {
                double[] c = circles.get(0, x);
//                Point center = new Point(Math.round(c[0]), Math.round(c[1]));
                Float[] values = new Float[3];
                values[0] = (float) Math.round(c[0]);
                values[1] = (float) Math.round((double) ranged.height() - c[1]);
                values[2] = (float) Math.round(c[2]);
                // circle center
                // Imgproc.circle(ranged, center, 1, new Scalar(0, 100, 100), 3, 8, 0);
                // circle outline
                // int radius = (int) Math.round(c[2]);
                // Imgproc.circle(ranged, center, radius, new Scalar(255, 0, 0), 3, 8, 0);
                circles_out.add(values);
            }
            detected = true;
        }


        // Imgproc.cvtColor(blurred, blurred, Imgproc.COLOR_HSV2RGB_FULL);
        Utils.matToBitmap(ranged, image);
        return new Pair<>(circles_out, detected);

        /*
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(mat_img, blurred, new Size(5, 5), 0);
        Utils.matToBitmap(blurred, image);
        return image;
        /*
        Mat thresh = new Mat();
        Imgproc.threshold(blurred, thresh, 200, 255, Imgproc.THRESH_BINARY);
        Imgproc.erode(thresh, thresh, new Mat(), new Point(-1, -1));
        Imgproc.dilate(thresh,thresh, new Mat(), new Point(-1, -1));

        Mat result = new Mat();
        Imgproc.medianBlur(thresh, result, 3);
        Imgproc.cvtColor(result, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, image);
        return image;
         */
    }


}
