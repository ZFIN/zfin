package org.zfin.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//
// Copied from https://stackoverflow.com/questions/1472514/convert-light-frequency-to-rgb
//
public class FluorescenceUtil {

    // color name, hex code
    public static Map<String, String> colorMap = new HashMap<>();

    static {
        colorMap.put("violet", "#6a00ff");
        colorMap.put("blue", "#00a9ff");
        colorMap.put("cyan", "#00ff92");
        colorMap.put("green", "#5eff00");
        colorMap.put("yellow", "#ffff00");
        colorMap.put("orange", "#ff7700");
        colorMap.put("red", "#ff0000");
        colorMap.put("far red", "#a10000");
    }

    // wave length in general mode
    public static Map<Color, List<Integer>> wavelengthGeneralMap = new HashMap<>();

    static {
        wavelengthGeneralMap.put(Color.VIOLET, List.of(400, 450));
        wavelengthGeneralMap.put(Color.BLUE, List.of(450, 490));
        wavelengthGeneralMap.put(Color.CYAN, List.of(490, 520));
        wavelengthGeneralMap.put(Color.GREEN, List.of(520, 560));
        wavelengthGeneralMap.put(Color.YELLOW, List.of(560, 590));
        wavelengthGeneralMap.put(Color.ORANGE, List.of(590, 635));
        wavelengthGeneralMap.put(Color.RED, List.of(635, 700));
        wavelengthGeneralMap.put(Color.FAR_RED, List.of(700, 1000));
    }

    // wave length in general mode
    public static Map<Color, List<Integer>> wavelengthFluorescentMap = new HashMap<>();

    static {
        wavelengthFluorescentMap.put(Color.VIOLET, List.of(380, 440));
        wavelengthFluorescentMap.put(Color.BLUE, List.of(440, 475));
        wavelengthFluorescentMap.put(Color.CYAN, List.of(475, 500));
        wavelengthFluorescentMap.put(Color.GREEN, List.of(500, 525));
        wavelengthFluorescentMap.put(Color.YELLOW, List.of(525, 555));
        wavelengthFluorescentMap.put(Color.ORANGE, List.of(555, 580));
        wavelengthFluorescentMap.put(Color.RED, List.of(580, 630));
        wavelengthFluorescentMap.put(Color.FAR_RED, List.of(630, 700));
    }

    public static String waveLengthToHexFixed(double wavelength) {
        return waveLengthToHex(convertFluorescentWaveLengthToGeneral(wavelength));
    }

    public static double convertFluorescentWaveLengthToGeneral(double wavelength) {
        // get color from fluorescent spectrum
        Color color = getColorFromFluorescentSpectrum(wavelength);
        // convert fluorescent wave length into general wave length
        // (d-c)/(b-a) * (lambda-a) +c
        // [a,b] is the range for the fluorescent color and [c,d] the one for the general spectrum
        double generalWaveLength = (double) (wavelengthGeneralMap.get(color).get(1) - (wavelengthGeneralMap.get(color).get(0))) /
                (wavelengthFluorescentMap.get(color).get(1) - (wavelengthFluorescentMap.get(color).get(0))) *
                (wavelength - (double) (wavelengthFluorescentMap.get(color).get(0))) + wavelengthGeneralMap.get(color).get(0);
        // beyond 750 the hex code gets into black and we don't want that to show for far-red
        return generalWaveLength < 750 ? generalWaveLength : 750;
    }

    private static Color getColorFromFluorescentSpectrum(double wavelength) {
        return wavelengthFluorescentMap.entrySet().stream().filter(colorEntry -> {
            List<Integer> limits = colorEntry.getValue();
            return wavelength >= limits.get(0) && wavelength <= limits.get(1);
        }).findFirst().map(Map.Entry::getKey).orElse(null);
    }

    public static String waveLengthToHex(double wavelength) {
        return String.format("#%02X%02X%02X", waveLengthToRGB(wavelength)[0], waveLengthToRGB(wavelength)[1], waveLengthToRGB(wavelength)[2]);
    }

    public static int[] waveLengthToRGBVals(double wavelength) {
        return waveLengthToRGB(wavelength);
    }

    public static String getReadableTextColor(int[] rgb) {
        // white
        // int[] white = {255, 255, 255};
        // int[] black = {0, 0, 0};
        double brightness = (rgb[0] * 299.0 + rgb[1] * 587.0 + rgb[2] * 114.0) / 1000;
        double brightnessWhite = 255;
        // https://web.mst.edu/~rhall/web_design/color_readability.html
        // difference in brightness should be more than 125 to be readable
        // if brightness alone does not work fully we need to include difference in hue (see above site)
        if (Math.abs(brightnessWhite - brightness) > 125)
            return "#FFF";
        return "#000";
    }

    public static int[] waveLengthToRGB(double Wavelength) {
        double factor;
        double Red, Green, Blue;

        if ((Wavelength >= 380) && (Wavelength < 440)) {
            Red = -(Wavelength - 440) / (440 - 380);
            Green = 0.0;
            Blue = 1.0;
        } else if ((Wavelength >= 440) && (Wavelength < 490)) {
            Red = 0.0;
            Green = (Wavelength - 440) / (490 - 440);
            Blue = 1.0;
        } else if ((Wavelength >= 490) && (Wavelength < 510)) {
            Red = 0.0;
            Green = 1.0;
            Blue = -(Wavelength - 510) / (510 - 490);
        } else if ((Wavelength >= 510) && (Wavelength < 580)) {
            Red = (Wavelength - 510) / (580 - 510);
            Green = 1.0;
            Blue = 0.0;
        } else if ((Wavelength >= 580) && (Wavelength < 645)) {
            Red = 1.0;
            Green = -(Wavelength - 645) / (645 - 580);
            Blue = 0.0;
        } else if ((Wavelength >= 645) && (Wavelength < 781)) {
            Red = 1.0;
            Green = 0.0;
            Blue = 0.0;
        } else {
            Red = 0.0;
            Green = 0.0;
            Blue = 0.0;
        }

        // Let the intensity fall off near the vision limits

        if ((Wavelength >= 380) && (Wavelength < 420)) {
            factor = 0.3 + 0.7 * (Wavelength - 380) / (420 - 380);
        } else if ((Wavelength >= 420) && (Wavelength < 701)) {
            factor = 1.0;
        } else if ((Wavelength >= 701) && (Wavelength < 781)) {
            factor = 0.3 + 0.7 * (780 - Wavelength) / (780 - 700);
        } else {
            factor = 0.0;
        }


        int[] rgb = new int[3];

        // Don't want 0^x = 1 for x <> 0
        rgb[0] = Red == 0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(Red * factor, Gamma));
        rgb[1] = Green == 0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(Green * factor, Gamma));
        rgb[2] = Blue == 0.0 ? 0 : (int) Math.round(IntensityMax * Math.pow(Blue * factor, Gamma));

        return rgb;
    }

    static private final double Gamma = 0.80;
    static private final double IntensityMax = 255;

    public static int[] getRGBValues(Integer wavelengthLength) {
        return waveLengthToRGB(wavelengthLength.doubleValue());
    }

    public static String getReadableTextColor(Integer emissionLength) {
        int[] rgb = waveLengthToRGBVals(convertFluorescentWaveLengthToGeneral((double) emissionLength));
        return getReadableTextColor(rgb);
    }
}
