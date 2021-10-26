package org.zfin.util;

//
// Copied from https://stackoverflow.com/questions/1472514/convert-light-frequency-to-rgb
//
public class FluorescenceUtil {

    public static String waveLengthToHex(double wavelength) {
        return String.format("#%02X%02X%02X", waveLengthToRGB(wavelength)[0], waveLengthToRGB(wavelength)[1], waveLengthToRGB(wavelength)[2]);
    }

    public static int[] waveLengthToRGB(double Wavelength) {
        double factor;
        double Red, Green, Blue;

        if((Wavelength >= 380) && (Wavelength < 440)) {
            Red = -(Wavelength - 440) / (440 - 380);
            Green = 0.0;
            Blue = 1.0;
        } else if((Wavelength >= 440) && (Wavelength < 490)) {
            Red = 0.0;
            Green = (Wavelength - 440) / (490 - 440);
            Blue = 1.0;
        } else if((Wavelength >= 490) && (Wavelength < 510)) {
            Red = 0.0;
            Green = 1.0;
            Blue = -(Wavelength - 510) / (510 - 490);
        } else if((Wavelength >= 510) && (Wavelength < 580)) {
            Red = (Wavelength - 510) / (580 - 510);
            Green = 1.0;
            Blue = 0.0;
        } else if((Wavelength >= 580) && (Wavelength < 645)) {
            Red = 1.0;
            Green = -(Wavelength - 645) / (645 - 580);
            Blue = 0.0;
        } else if((Wavelength >= 645) && (Wavelength < 781)) {
            Red = 1.0;
            Green = 0.0;
            Blue = 0.0;
        } else {
            Red = 0.0;
            Green = 0.0;
            Blue = 0.0;
        }

        // Let the intensity fall off near the vision limits

        if((Wavelength >= 380) && (Wavelength < 420)) {
            factor = 0.3 + 0.7 * (Wavelength - 380) / (420 - 380);
        } else if((Wavelength >= 420) && (Wavelength < 701)) {
            factor = 1.0;
        } else if((Wavelength >= 701) && (Wavelength < 781)) {
            factor = 0.3 + 0.7 * (780 - Wavelength) / (780 - 700);
        } else {
            factor = 0.0;
        }


        int[] rgb = new int[3];

        // Don't want 0^x = 1 for x <> 0
        rgb[0] = Red == 0.0 ? 0 : (int)Math.round(IntensityMax * Math.pow(Red * factor, Gamma));
        rgb[1] = Green == 0.0 ? 0 : (int)Math.round(IntensityMax * Math.pow(Green * factor, Gamma));
        rgb[2] = Blue == 0.0 ? 0 : (int)Math.round(IntensityMax * Math.pow(Blue * factor, Gamma));

        return rgb;
    }

    static private final double Gamma = 0.80;
    static private final double IntensityMax = 255;

    public static int[] getRGBValues(Integer wavelengthLength) {
        return waveLengthToRGB(wavelengthLength.doubleValue());
    }
}
