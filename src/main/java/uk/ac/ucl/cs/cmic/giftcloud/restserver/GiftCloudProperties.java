package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.util.Optional;

public interface GiftCloudProperties {

    static final String propertyName_GiftCloudServerUrl = "GiftCloud_ServerUrl";
    static final String propertyName_GiftCloudLastUsername = "GiftCloud_LastUsername";
    static final String propertyName_GiftCloudLastProject = "GiftCloud_LastUploadProject";

    Optional<String> getGiftCloudUrl();

    Optional<String> getLastUserName();

    void setLastUserName(final String lastUserName);

    Optional<char[]> getLastPassword();

    void setLastPassword(final char[] text);

    Optional<String> getSessionCookie();

}
