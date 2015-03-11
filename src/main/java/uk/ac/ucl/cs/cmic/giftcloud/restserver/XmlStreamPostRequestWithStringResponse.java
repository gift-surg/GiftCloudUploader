package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.nrg.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



class XmlStreamPostRequestWithStringResponse extends HttpRequestWithOutput<String> {

    private final Logger logger = LoggerFactory.getLogger(XmlStreamPostRequestWithStringResponse.class);
    private final InputStream in;

    XmlStreamPostRequestWithStringResponse(final String urlString, final InputStream in) {
        super(HttpConnectionWrapper.ConnectionType.POST, urlString, new HttpStringResponseProcessor());

        this.in = in;
    }

    protected void prepareConnection(final HttpConnectionBuilder connectionBuilder) throws IOException {
        super.prepareConnection(connectionBuilder);
        connectionBuilder.setContentType("text/xml");
    }

    protected void streamToConnection(final OutputStream outputStream) throws IOException {
        try {
            try {
                IOUtils.copy(outputStream, in, null);
                outputStream.flush();
            } catch (Throwable t) {
                System.out.println("failure");
                t.printStackTrace();
                logger.debug("stream copy failed", t);
            } finally {
                outputStream.close();
            }
        } finally {
            in.close();
        }
    }
}
