package com.sleepyduck.pixelate4crafting.control.util;

import android.graphics.Color;
import android.hardware.camera2.params.ColorSpaceTransform;
import android.renderscript.Float3;
import android.renderscript.Matrix3f;

/**
 * Created by fredrikmetcalf on 13/04/16.
 */
public class ColorUtil {
    private static float[] M = {
            0.5767309f, 0.1855540f, 0.1881852f,
            0.2973769f, 0.6273491f, 0.0752741f,
            0.0270343f, 0.0706872f, 0.9911085f};
    private static final float epsylon = 0.008856f;
    private static final float kappa = 903.3f;

    private static int[] LabLeft = new int[3];
    private static int[] LabRight = new int[3];
    public static int Diff(int left, int right) {
        //XYZToLab(rgbToXYZ(left), LabLeft);
        //XYZToLab(rgbToXYZ(right), LabRight);
        rgb2lab(Color.red(left), Color.green(left), Color.blue(left), LabLeft);
        rgb2lab(Color.red(right), Color.green(right), Color.blue(right), LabRight);
        int dL = LabLeft[0] - LabRight[0];
        int da = LabLeft[1] - LabRight[1];
        int db = LabLeft[2] - LabRight[2];
        int diff = (int) Math.sqrt(dL*dL + da*da + db*db);
        //BetterLog.d(ColorUtil.class, "Color diff (" + dL + ", " + da + ", " + db + ") " + diff);
        return diff;

        /*int biggestLeft = -1, biggestRight = -1;
        int[] rgbLeft = {Color.red(left), Color.green(left), Color.blue(left)};
        int[] rgbRight = {Color.red(right), Color.green(right), Color.blue(right)};
        if (Math.abs(rgbLeft[0] - rgbRight[1]) > GRAYSCALE_THRESH
                || Math.abs(rgbLeft[0] - rgbRight[2]) < GRAYSCALE_THRESH) {
            biggestLeft = rgbLeft[0] > rgbLeft[1] ? 0 : 1;
            biggestLeft = rgbLeft[biggestLeft] > rgbLeft[2] ? biggestLeft : 2;
        }
        if (Math.abs(rgbRight[0] - rgbRight[1]) > GRAYSCALE_THRESH
                || Math.abs(rgbRight[0] - rgbRight[2]) < GRAYSCALE_THRESH) {
            biggestRight = rgbRight[0] > rgbRight[1] ? 0 : 1;
            biggestRight = rgbRight[biggestRight] > rgbRight[2] ? biggestRight : 2;
        }
        if (biggestLeft == biggestRight) {
            return Math.abs(rgbLeft[0] - rgbRight[0])
                    + Math.abs(rgbLeft[1] - rgbRight[1])
                    + Math.abs(rgbLeft[2] - rgbRight[2]);
        }
        return 768;*/
    }

    private static float[] rgbToXYZ(int color) {
        return mul(M, new float[]{Color.red(color) / 255f,
                Color.green(color) / 255f,
                Color.blue(color) / 255f});
    }

    private static void XYZToLab(float[] XYZ, float[] outLab) {
        float fx = f(XYZ[0] / Xr);
        float fy = f(XYZ[1] / Yr);
        float fz = f(XYZ[2] / Zr);
        outLab[0] = 116f * fy - 16f;
        outLab[1] = 500f * (fx - fy);
        outLab[2] = 200f * (fy - fz);
    }

    private static final float f(float x) {
        if (x > epsylon) {
            return (float) Math.pow(x, 1f / 3f);
        } else {
            return (kappa * x + 16f) / 116f;
        }
    }

    private static float[] _mul = new float[3];
    private static float[] mul(float[] m, float[] v) {
        _mul[0] = m[0] * v[0] + m[1] * v[1] + m[2] * v[2];
        _mul[1] = m[3] * v[0] + m[4] * v[1] + m[5] * v[2];
        _mul[2] = m[6] * v[0] + m[7] * v[1] + m[8] * v[2];
        return _mul;
    }

    private static float eps = 216.f / 24389.f;
    private static float k = 24389.f / 27.f;

    private static float Xr = 0.964221f;  // reference white D50
    private static float Yr = 1.0f;
    private static float Zr = 0.825211f;
    public static void rgb2lab(int R, int G, int B, int[] lab) {
        //http://www.brucelindbloom.com

        float r, g, b, X, Y, Z, fx, fy, fz, xr, yr, zr;
        float Ls, as, bs;

        // RGB to XYZ
        r = R / 255.f; //R 0..1
        g = G / 255.f; //G 0..1
        b = B / 255.f; //B 0..1

        // assuming sRGB (D65)
        if (r <= 0.04045)
            r = r / 12;
        else
            r = (float) Math.pow((r + 0.055) / 1.055, 2.4);

        if (g <= 0.04045)
            g = g / 12;
        else
            g = (float) Math.pow((g + 0.055) / 1.055, 2.4);

        if (b <= 0.04045)
            b = b / 12;
        else
            b = (float) Math.pow((b + 0.055) / 1.055, 2.4);


        X = 0.436052025f * r + 0.385081593f * g + 0.143087414f * b;
        Y = 0.222491598f * r + 0.71688606f * g + 0.060621486f * b;
        Z = 0.013929122f * r + 0.097097002f * g + 0.71418547f * b;

        // XYZ to Lab
        xr = X/Xr;
        yr = Y/Yr;
        zr = Z/Zr;

        if ( xr > eps )
            fx =  (float) Math.pow(xr, 1/3.);
        else
            fx = (float) ((k * xr + 16.) / 116.);

        if ( yr > eps )
            fy =  (float) Math.pow(yr, 1/3.);
        else
            fy = (float) ((k * yr + 16.) / 116.);

        if ( zr > eps )
            fz =  (float) Math.pow(zr, 1/3.);
        else
            fz = (float) ((k * zr + 16.) / 116.);

        Ls = ( 116 * fy ) - 16;
        as = 500*(fx-fy);
        bs = 200*(fy-fz);

        lab[0] = (int) (2.55*Ls + .5);
        lab[1] = (int) (as + .5);
        lab[2] = (int) (bs + .5);
    }
}
