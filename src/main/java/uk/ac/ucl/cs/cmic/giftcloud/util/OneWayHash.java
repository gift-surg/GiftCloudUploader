package uk.ac.ucl.cs.cmic.giftcloud.util;

import com.fasterxml.uuid.Generators;
import org.apache.commons.lang.StringUtils;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Static utility class used to generate pseudo-anonymised one-way hashes of identifiers
 */
public class OneWayHash {

    /**
     * UID root for UUIDs (Universally Unique Identifiers) generated as per Rec. ITU-T X.667 | ISO/IEC 9834-8.
     * @see <a href="http://www.oid-info.com/get/2.25">OID repository {joint-iso-itu-t(2) uuid(25)}</a>
     */
    private static final String UUID_ROOT = "2.25";

    /** Generate a one-way hash of a UID string
     * @param inputUid the uid to be hashed
     * @return the one-way hash of the uid
     */
    public static String hashUid(final String inputUid) {
        if (StringUtils.isBlank(inputUid)) {
            return null;
        } else {
            String hashedUid = toUID(toUUID(inputUid.trim()));
            return hashedUid.replace('.', '_');
        }
    }

    public static String convertExistingUid(final String inputUid) {
        if (StringUtils.isBlank(inputUid)) {
            return null;
        } else {
            String hashedUid = inputUid.trim();
            return hashedUid.replace('.', '_');
        }
    }

    /** Private constructor - non-instantiable utility class
     */
    private OneWayHash() {
    }

/* (non-Javadoc)
 * @see org.dcm4che2.util.UIDUtils#doCreateUID(java.util.String)
 * @param uuid
 * @return UID derived from the provided UUID
 */
    private static String toUID(final UUID uuid) {
        final byte[] b17 = new byte[17];
        fill(b17, 1, uuid.getMostSignificantBits());
        fill(b17, 9, uuid.getLeastSignificantBits());
        return new StringBuilder(64).append(UUID_ROOT).append('.')
                .append(new BigInteger(b17)).toString();
    }

    /* (non-Javadoc)
     * @see org.dcm4che2.util.UIDUtils#fill(byte[], int, long)
     */
    private static void fill(byte[] bb, int off, long val) {
        for (int i = off, shift = 56; shift >= 0; i++, shift -= 8)
            bb[i] = (byte) (val >>> shift);
    }

    /**
     * Generates a Version 5 UUID from the provided string
     * @param s source string
     * @return Version 5 UUID
     */
    private static UUID toUUID(final String s) {
        return Generators.nameBasedGenerator().generate(s.getBytes());
    }
}
