package com.sleepyduck.pixelate4crafting.control.util;

import android.graphics.Color;
import android.net.LinkAddress;
import android.support.v4.graphics.ColorUtils;

/**
 * Created by fredrikmetcalf on 13/04/16.
 */
public class ColorUtil {
    public static final int ALPHA_CHANNEL = 0xff000000;

    private static float[] M = {
            0.5767309f, 0.1855540f, 0.1881852f,
            0.2973769f, 0.6273491f, 0.0752741f,
            0.0270343f, 0.0706872f, 0.9911085f};
    private static final float epsylon = 0.008856f;
    private static final float kappa = 903.3f;

    private static double[] XYZLeft = new double[3];
    private static double[] XYZRight = new double[3];
    private static double[] LabLeft = new double[3];
    private static double[] LabRight = new double[3];

    public static double Diff(int left, int right) {
        ColorUtils.colorToLAB(left, LabLeft);
        ColorUtils.colorToLAB(right, LabRight);
        return ColorUtils.distanceEuclidean(LabLeft, LabRight);
    }

   /* public static double Diff(int left, int right) {
        RGBToXYZ(Color.red(left), Color.green(left), Color.blue(left), XYZLeft);
        XYZToLab(XYZLeft, LabLeft);
        RGBToXYZ(Color.red(right), Color.green(right), Color.blue(right), XYZRight);
        XYZToLab(XYZRight, LabRight);
        double dL = LabLeft[0] - LabRight[0];
        double da = LabLeft[1] - LabRight[1];
        double db = LabLeft[2] - LabRight[2];
        return Math.sqrt(dL*dL + da*da + db*db);
        //XYZToLab(rgbToXYZ(left), LabLeft);
        //XYZToLab(rgbToXYZ(right), LabRight);
        /*rgb2lab(Color.red(left), Color.green(left), Color.blue(left), LabLeft);
        rgb2lab(Color.red(right), Color.green(right), Color.blue(right), LabRight);
        int dL = LabLeft[0] - LabRight[0];
        int da = LabLeft[1] - LabRight[1];
        int db = LabLeft[2] - LabRight[2];
        int diff = (int) Math.sqrt(dL * dL + da * da + db * db);
        //BetterLog.d(ColorUtil.class, "Color diff (" + dL + ", " + da + ", " + db + ") " + diff);
        return diff;*/

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
    //}

    /*private static float[] rgbToXYZ(int color) {
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

    // === Algorithm 1 from web ===
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
        xr = X / Xr;
        yr = Y / Yr;
        zr = Z / Zr;

        if (xr > eps)
            fx = (float) Math.pow(xr, 1 / 3.);
        else
            fx = (float) ((k * xr + 16.) / 116.);

        if (yr > eps)
            fy = (float) Math.pow(yr, 1 / 3.);
        else
            fy = (float) ((k * yr + 16.) / 116.);

        if (zr > eps)
            fz = (float) Math.pow(zr, 1 / 3.);
        else
            fz = (float) ((k * zr + 16.) / 116.);

        Ls = (116 * fy) - 16;
        as = 500 * (fx - fy);
        bs = 200 * (fy - fz);

        lab[0] = (int) (2.55 * Ls + .5);
        lab[1] = (int) (as + .5);
        lab[2] = (int) (bs + .5);
    }*/

    // === Algorithm 2 from http://www.easyrgb.com/index.php?X=MATH ===

    /*public static void RGBToXYZ(int R, int G, int B, double[] XYZ) {
        double var_R = (R / 255.);        //R from 0 to 255
        double var_G = (G / 255.);        //G from 0 to 255
        double var_B = (B / 255.);        //B from 0 to 255

        if (var_R > 0.04045) var_R = Math.pow((var_R + 0.055) / 1.055, 2.4);
        else var_R = var_R / 12.92;
        if (var_G > 0.04045) var_G = Math.pow((var_G + 0.055) / 1.055, 2.4);
        else var_G = var_G / 12.92;
        if (var_B > 0.04045) var_B = Math.pow((var_B + 0.055) / 1.055, 2.4);
        else var_B = var_B / 12.92;

        var_R = var_R * 100.;
        var_G = var_G * 100.;
        var_B = var_B * 100.;

        //Observer. = 2°, Illuminant = D65
        XYZ[0] = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805;
        XYZ[1] = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722;
        XYZ[2] = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505;
    }

    private static double ref_X = 95.047;
    private static double ref_Y = 100.000;
    private static double ref_Z = 108.883;

    public static void XYZToLab(double[] XYZ, double Lab[]) {
        double var_X = XYZ[0] / ref_X;          //ref_X =  95.047   Observer= 2°, Illuminant= D65
        double var_Y = XYZ[1] / ref_Y;          //ref_Y = 100.000
        double var_Z = XYZ[2] / ref_Z;          //ref_Z = 108.883

        if (var_X > 0.008856) var_X = Math.pow(var_X, 1. / 3.);
        else var_X = (7.787 * var_X) + (16. / 116.);
        if (var_Y > 0.008856) var_Y = Math.pow(var_Y, 1. / 3.);
        else var_Y = (7.787 * var_Y) + (16. / 116.);
        if (var_Z > 0.008856) var_Z = Math.pow(var_Z, 1. / 3.);
        else var_Z = (7.787 * var_Z) + (16. / 116.);

        Lab[0] = (116. * var_Y) - 16.;
        Lab[1] = 500. * (var_X - var_Y);
        Lab[2] = 200. * (var_Y - var_Z);
    }

    public static void LabToXYZ(double[] Lab, double[] XYZ) {
        double var_Y = ( Lab[0] + 16. ) / 116.;
        double var_X = Lab[1] / 500. + var_Y;
        double var_Z = var_Y - Lab[2] / 200.;

        if ( Math.pow(var_Y,3.) > 0.008856 ) var_Y = Math.pow(var_Y,3.);
        else                      var_Y = ( var_Y - 16. / 116. ) / 7.787;
        if ( Math.pow(var_X,3.) > 0.008856 ) var_X = Math.pow(var_X,3.);
        else                      var_X = ( var_X - 16. / 116. ) / 7.787;
        if ( Math.pow(var_Z,3.) > 0.008856 ) var_Z = Math.pow(var_Z,3.);
        else                      var_Z = ( var_Z - 16. / 116. ) / 7.787;

        XYZ[0] = ref_X * var_X;     //ref_X =  95.047     Observer= 2°, Illuminant= D65
        XYZ[1] = ref_Y * var_Y;     //ref_Y = 100.000
        XYZ[2] = ref_Z * var_Z;     //ref_Z = 108.883
    }

    public static void XYZToRGB(double[] XYZ, int[] RGB) {
        double var_X = XYZ[0] / 100.;        //X from 0 to  95.047      (Observer = 2°, Illuminant = D65)
        double var_Y = XYZ[1] / 100.;        //Y from 0 to 100.000
        double var_Z = XYZ[2] / 100.;        //Z from 0 to 108.883

        double var_R = var_X *  3.2406 + var_Y * -1.5372 + var_Z * -0.4986;
        double var_G = var_X * -0.9689 + var_Y *  1.8758 + var_Z *  0.0415;
        double var_B = var_X *  0.0557 + var_Y * -0.2040 + var_Z *  1.0570;

        if ( var_R > 0.0031308 ) var_R = 1.055 * ( Math.pow(var_R, 1. / 2.4 ) ) - 0.055;
        else                     var_R = 12.92 * var_R;
        if ( var_G > 0.0031308 ) var_G = 1.055 * ( Math.pow(var_G, 1. / 2.4 ) ) - 0.055;
        else                     var_G = 12.92 * var_G;
        if ( var_B > 0.0031308 ) var_B = 1.055 * ( Math.pow(var_B, 1. / 2.4 ) ) - 0.055;
        else                     var_B = 12.92 * var_B;

        RGB[0] = (int) (var_R * 255.);
        RGB[1] = (int) (var_G * 255.);
        RGB[2] = (int) (var_B * 255.);
    }*/
}
