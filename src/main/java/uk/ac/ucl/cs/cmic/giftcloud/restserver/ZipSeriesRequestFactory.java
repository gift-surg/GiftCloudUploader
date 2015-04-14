package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.nrg.dcm.edit.ScriptApplicator;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;

/**
 * Used to create the appropriate HttpRequest for uploading one or more files as a zip file
 */
class ZipSeriesRequestFactory {

    /**
     * The constuctor is private because there is no need to instantiate this class
     */
    private ZipSeriesRequestFactory() {
    }

    /**
     * The transfer encoding for uploading the zipped file to the server
     */
    enum ZipStreaming {
        /**
         * Fixed Length Streaming
         */
        FixedSize,

        /**
         * Chunked Streaming
         */
        Chunked
    }

    /**
     * Creates a HttpRequest class for uploading one or more files as a zip file.
     *
     * @param zipStreaming the type of streaming that will be used to upload the files after they are zipped
     * @param url the location to which the files will be uploaded
     * @param fileCollection the set of file paths to be zipped and uploaded
     * @param applicators Dicom anonymisation scripts to be applied to the files
     * @param progress a callback for providing upload progress feedback
     * @return an HttpRequest object which can be used to perform the zipping and uploading
     */
    static HttpRequestWithOutput build(
            final HttpConnectionWrapper.ConnectionType connectionType,
            final ZipStreaming zipStreaming,
            final String url,
            final FileCollection fileCollection,
            final Iterable<ScriptApplicator> applicators,
            final UploadStatisticsReporter progress,
            final HttpResponseProcessor responseProcessor,
            final MultiUploadReporter reporter) {
        switch (zipStreaming) {
            case Chunked:
                return new ZipSeriesRequestChunked(connectionType, url, fileCollection, applicators, progress, responseProcessor, reporter);
            case FixedSize:
                return new ZipSeriesRequestFixedSize(connectionType, url, fileCollection, applicators, progress, responseProcessor, reporter);
            default:
                throw new RuntimeException("Unknown enum value");
        }
    }
}
