/**
 * mpg2dcm by Tom Doel
 *
 * http://github.com/tomdoel/mpg2dcm
 *
 * Distributed under the MIT License
 */

package com.tomdoel.mpg2dcm;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Stores metadata derived from an MPEG file
 *
 * <p>Part of <a href="http://github.com/tomdoel/mpg2dcm">mpg2dcm</a>
 *
 * @author Tom Doel
 * @version 1.0
 */
public class MpegMetaData {
    private Optional<AspectRatio> aspectRatio = Optional.empty();
    private Optional<FrameRate> frameRate = Optional.empty();
    private Optional<Integer> Rows = Optional.empty();
    private Optional<Integer> Columns = Optional.empty();

    /**
     * @return an Optional containing the aspect ratio, or empty if it is not specified in the file
     */
    public Optional<AspectRatio> getAspectRatio() {
        return aspectRatio;
    }

    /**
     * @return an Optional containing the frame rate ratio, or empty if it is not specified in the file
     */
    public Optional<FrameRate> getFrameRate() {
        return frameRate;
    }

    /**
     * @return an Optional containing the number of rows, or empty if it is not set
     */
    public Optional<Integer> getRows() {
        return Rows;
    }

    /**
     * @return an Optional containing the number of columns, or empty if it is not set
     */
    public Optional<Integer> getColumns() {
        return Columns;
    }

    /**
     * Reads in MPEG metadata from an MPEG file provided as an InputStream
     *
     * @param inputStream a stream to the MPEG file
     * @return MpegMetaData object containing parsed header information
     * @throws IOException if a failure occurred when reading from the stream
     */
    public static MpegMetaData getMetaDataFromMpegStream(final InputStream inputStream) throws IOException {
        final MpegMetaData mpegMetaData= new MpegMetaData();
        final BufferedInputStream mpegStream = new BufferedInputStream(inputStream);
        byte[] byteArray = new byte[4];
        mpegStream.mark(0);

        while (true) {
            final int bytesRead = mpegStream.read(byteArray, 0, 4);
            if (bytesRead < 3) {
                return mpegMetaData;
            }
            if (byteArray[0] == 0 && byteArray[1] == 0 && byteArray[2] == 0x01 && (byteArray[3] & 0xFF) == 0xB3) {
                int size0 = mpegStream.read();
                int size1 = mpegStream.read();
                int size2 = mpegStream.read();
                int aspectframe = mpegStream.read();
                int cols = (size0 << 4) + (size1 & 0xF0);
                int rows = ((size1 & 0x0F) << 8) + size2;
                int aspectRatioType = (aspectframe & 0xF0) >> 4;
                int frameRateType = (aspectframe & 0x0F);
                final FrameRate frameRate = FrameRate.getFrameRate(frameRateType);
                final AspectRatio aspectRatio = AspectRatio.getAspectRatioFromMpegCode(aspectRatioType);
                mpegMetaData.setAspectRatio(aspectRatio);
                mpegMetaData.setFrameRate(frameRate);
                mpegMetaData.setColumns(cols);
                mpegMetaData.setRows(rows);
                return mpegMetaData;
            } else {
                mpegStream.reset();
                mpegStream.read();
                mpegStream.mark(0);
            }
        }
    }

    private void setAspectRatio(AspectRatio aspectRatio) {
        this.aspectRatio = Optional.of(aspectRatio);
    }

    private void setFrameRate(FrameRate frameRate) {
        this.frameRate = Optional.of(frameRate);
    }

    private void setRows(int rows) {
        Rows = Optional.of(rows);
    }

    private void setColumns(int columns) {
        Columns = Optional.of(columns);
    }
}
