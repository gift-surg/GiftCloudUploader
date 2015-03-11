package uk.ac.ucl.cs.cmic.giftcloud.restserver;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.Permission;
import java.util.List;
import java.util.Map;

public class HttpConnectionDebugInfo {

    private final String responseMessage;
    private final String requestMethod;
    private final Map<String, List<String>> headerFields;
    final Permission permission;
    final boolean followRedirects;
    final boolean instanceFollowRedirects;
    final int responseCode;

    public HttpConnectionDebugInfo(final HttpURLConnection connection) throws IOException {
        responseMessage = connection.getResponseMessage();   //IoException
        requestMethod = connection.getRequestMethod();
        headerFields = connection.getHeaderFields();
        connection.getErrorStream();
        followRedirects = connection.getFollowRedirects();
        instanceFollowRedirects = connection.getInstanceFollowRedirects();
        permission = connection.getPermission();  //IoException
        responseCode = connection.getResponseCode();    //IoException
    }
}
