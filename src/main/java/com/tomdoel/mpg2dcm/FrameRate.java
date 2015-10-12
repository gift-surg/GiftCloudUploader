/**
 * mpg2dcm by Tom Doel
 *
 * http://github.com/tomdoel/mpg2dcm
 *
 * Distributed under the MIT License
 */



package com.tomdoel.mpg2dcm;

/**
 * Enumeration describing the frame rate of an MPEG file
 *
 * <p>Part of <a href="http://github.com/tomdoel/mpg2dcm">mpg2dcm</a>
 *
 * @author Tom Doel
 * @version 1.0
 */
enum FrameRate {
    FPS24000DIV1001("24", "41.7083"),  // 24000/1001 fps
    FPS24("24", "41.6667"),            // 24 fps
    FPS25("25", "40"),                 // 25 fps
    FPS30000DIV1001("30", "33.3667"),  // 30000/1001 fps
    FPS30("30", "33.3333"),            // 30 fps
    FPS50("50", "20"),                 // 50 fps
    FPS60000DIV1001("60", "16.6833"),  // 60000/1001 fps
    FPS60("60", "16.6667"),            // 60 fps
    UNKNOWN("", "");

    private String dicomFrameRateString;
    private String dicomFrameTimeString;

    /**
     * Creates a frame rate enumeration with the corresponding Dicom string
     * @param dicomFrameRateString
     * @param dicomFrameTimeString
     */
    FrameRate(final String dicomFrameRateString, final String dicomFrameTimeString) {
        this.dicomFrameRateString = dicomFrameRateString;
        this.dicomFrameTimeString = dicomFrameTimeString;
    }

    /**
     * Returns the frame rate corresponding to the specified MPEG code
     *
     * @param mpegCode the code from the MPEG Sequence header
     * @return the corresponding frame rate enumeration
     */
    static FrameRate getFrameRate(int mpegCode) {
        switch (mpegCode) {
            case 1:
                return FrameRate.FPS24000DIV1001;
            case 2:
                return FrameRate.FPS24;
            case 3:
                return FrameRate.FPS25;
            case 4:
                return FrameRate.FPS30000DIV1001;
            case 5:
                return FrameRate.FPS30;
            case 6:
                return FrameRate.FPS50;
            case 7:
                return FrameRate.FPS60000DIV1001;
            case 8:
                return FrameRate.FPS60;
            default:
                return FrameRate.UNKNOWN;
        }
    }

    /**
     * @return a string suitable for storing in the DICOM FrameRate tag. This string represents an integer so the frame rate is rounded to th enearest fps
     */
    public String getDicomFrameRateString() {
        return dicomFrameRateString;
    }

    /**
     * @return a string suitable for storing in the DICOM FrameTime tag. This string contains a decimal value in ms
     */
    public String getDicomFrameTimeString() {
        return dicomFrameTimeString;
    }
}