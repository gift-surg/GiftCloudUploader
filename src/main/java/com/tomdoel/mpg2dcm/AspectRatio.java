/**
 * mpg2dcm by Tom Doel
 *
 * http://github.com/tomdoel/mpg2dcm
 *
 * Distributed under the MIT License
 */


package com.tomdoel.mpg2dcm;

/**
 * Enumeration for MPEG aspect ratios
 *
 * <p>Part of <a href="http://github.com/tomdoel/mpg2dcm">mpg2dcm</a>
 *
 * @author Tom Doel
 * @version 1.0
 * */
enum AspectRatio {
    AR1_1("1\\1"),        // 1:1
    AR4_3("4\\3"),        // 4:3
    AR16_9("16\\9"),      // 16:9
    AR2_21_1("221\\100"), // 2.21:1
    UNKNOWN("");

    private final String aspectRatioDicomString;

    AspectRatio(final String aspectRatioDicomString) {
        this.aspectRatioDicomString = aspectRatioDicomString;
    }

    /**
     * Returns the enumeration corresponding to an MPEG aspect ratio code
     *
     * @param mpegCode the code specified in the MPEG sequence header
     * @return the corresponding enumeration
     */
    static AspectRatio getAspectRatioFromMpegCode(int mpegCode) {
        switch (mpegCode) {
            case 1:
                return AspectRatio.AR1_1;
            case 2:
                return AspectRatio.AR4_3;
            case 3:
                return AspectRatio.AR16_9;
            case 4:
                return AspectRatio.AR2_21_1;
            default:
                return AspectRatio.UNKNOWN;
        }
    }

    /**
     * Returns a string representation of the aspect ratio suitable for setting the DICOM tag for PixelAspectRatio
     *
     * @return a string containg the aspect ratio in the format x\y
     */
    public String getAspectRatioDicomString() {
        return aspectRatioDicomString;
    }
}


