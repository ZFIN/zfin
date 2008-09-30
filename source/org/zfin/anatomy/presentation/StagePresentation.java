package org.zfin.anatomy.presentation;

import org.zfin.anatomy.DevelopmentStage;
import org.apache.commons.lang.StringUtils;

/**
 * This class contains methods being used for display purposes.
 */
public class StagePresentation {
    /**
     * This is the upper number for hours above which
     * the presentation switches from an hourly to a
     * daily display.
     */
    public static final int SEVEN_DAYS = 168;
    /**
     * The short term string used for hour.
     */
    public static final String HOUR = "h";
    /**
     * The short term string used for day
     */
    public static final String DAY = "d";

    /**
     * Formats an hour into a display string:
     * If the time <t> is smaller than or equal 7 days then print:
     * th, e.g. 120h
     * If the time <t> is greater than 7 days then print:
     * xd, e.g. 20d
     * where x is the number of days the hour t corresponds.
     * Note: The conversion of the hours into days truncates
     * any fractional day, i.e. 168 hours converts into 7 days
     * and so does 170 hours.
     *
     * @param hours
     * @return String formatted time string
     */
    public static String formatHourToHourOrDay(float hours) {
        if (hours < 0) {
            throw new RuntimeException("Hours have to be positive!");
        }
        String formattedTime;
        if (hours > SEVEN_DAYS) {
            int startTimeInt = (int) (hours / 24);
            formattedTime = String.valueOf(startTimeInt) + DAY;
        } else {
            formattedTime = String.valueOf(hours) + HOUR;
        }
        return formattedTime;
    }

    /**
     * Create a formatted string for the developmental stage name.
     * It is a concatination of the name, start and end and other feature attribute:
     * <name> (<startFF>-<endFF>, <otherFeatures>)
     * startFF and endFF is the formatted time string converted via the formatHourToHourOrDay() method.
     * If the stage name is 'Unknown' only 'Unknown' is returned.
     *
     * @param stage
     * @return String
     */
    public static String createDisplayEntry(DevelopmentStage stage) {
        if (stage == null) {
            return null;
        }
        String stageName = stage.getName();
        if (stageName == null)
            return null;
        StringBuilder displayName = new StringBuilder(stageName);
        if (stageName.equals(DevelopmentStage.UNKNOWN)) {
            return stageName;
        }
        String time = formatHourToHourOrDay(stage.getHoursStart());
        displayName.append(" (").append(time);
        time = formatHourToHourOrDay(stage.getHoursEnd());
        displayName.append("-").append(time);
        if (!StringUtils.isEmpty(stage.getOtherFeature()))
            displayName.append(", ").append(stage.getOtherFeature());
        displayName.append(")");
        return displayName.toString();
    }

    public static String createDisplayEntryShort(DevelopmentStage stage) {
        if (stage == null) {
            return null;
        }
        String stageName = stage.getName();
        if (stageName == null)
            return null;
        StringBuilder displayName = new StringBuilder(stageName);
        if (stageName.equals(DevelopmentStage.UNKNOWN)) {
            return stageName;
        }
        return displayName.toString();
    }
}
