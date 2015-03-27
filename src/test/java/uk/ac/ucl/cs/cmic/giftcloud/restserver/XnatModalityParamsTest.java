package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class XnatModalityParamsTest extends TestCase {

    private String dicomTag;
    private String sopClassUid;
    private String sessionTag;
    private String scanTag;
    private String formatString;
    private String collectionString;

    public XnatModalityParamsTest(final String dicomTag, final String sopClassUid, final String sessionTag, final String scanTag, final String formatString, final String collectionString) {
        this.dicomTag = dicomTag;
        this.sopClassUid = sopClassUid;
        this.sessionTag = sessionTag;
        this.scanTag = scanTag;
        this.formatString = formatString;
        this.collectionString = collectionString;
    }

    @Parameterized.Parameters
    public static Collection dicomTypes() {
        return Arrays.asList(new Object[][]{

                // Tests for MR variations
                {"MR",  "",                            "xnat:mrSessionData", "xnat:mrScanData", "DICOM", "DICOM"},
                {"MR",  "1.2.840.10008.5.1.4.1.1.4",   "xnat:mrSessionData", "xnat:mrScanData", "DICOM", "DICOM"},
                {"MR",  "1.2.840.10008.5.1.4.1.1.4.1", "xnat:mrSessionData", "xnat:mrScanData", "DICOM", "DICOM"},
                {"",    "1.2.840.10008.5.1.4.1.1.4",   "xnat:mrSessionData", "xnat:mrScanData", "DICOM", "DICOM"},
                {"",    "1.2.840.10008.5.1.4.1.1.4.1", "xnat:mrSessionData", "xnat:mrScanData", "DICOM", "DICOM"},

                // Test for CT variations
                {"CT",  "",                            "xnat:ctSessionData", "xnat:ctScanData", "DICOM", "DICOM"},
                {"CT",  "1.2.840.10008.5.1.4.1.1.2",   "xnat:ctSessionData", "xnat:ctScanData", "DICOM", "DICOM"},
                {"CT",  "1.2.840.10008.5.1.4.1.1.2.1", "xnat:ctSessionData", "xnat:ctScanData", "DICOM", "DICOM"},
                {"",    "1.2.840.10008.5.1.4.1.1.2",   "xnat:ctSessionData", "xnat:ctScanData", "DICOM", "DICOM"},
                {"",    "1.2.840.10008.5.1.4.1.1.2.1", "xnat:ctSessionData", "xnat:ctScanData", "DICOM", "DICOM"},

                // Test for US variations
                {"US",  "",                            "xnat:usSessionData", "xnat:usScanData", "DICOM", "DICOM"},
                {"US",  "1.2.840.10008.5.1.4.1.1.6",   "xnat:usSessionData", "xnat:usScanData", "DICOM", "DICOM"},
                {"US",  "1.2.840.10008.5.1.4.1.1.6.1", "xnat:usSessionData", "xnat:usScanData", "DICOM", "DICOM"},
                {"",    "1.2.840.10008.5.1.4.1.1.6",   "xnat:usSessionData", "xnat:usScanData", "DICOM", "DICOM"},
                {"",    "1.2.840.10008.5.1.4.1.1.6.1", "xnat:usSessionData", "xnat:usScanData", "DICOM", "DICOM"}

        });
    }

    @Test
    public void testCreateFromDicom() {
        final XnatModalityParams xnatModalityParams = XnatModalityParams.createFromDicom(dicomTag, sopClassUid);
        assertEquals(sessionTag, xnatModalityParams.getXnatSessionTag());
        assertEquals(scanTag, xnatModalityParams.getXnatScanTag());
        assertEquals(formatString, xnatModalityParams.getFormatString());
        assertEquals(collectionString, xnatModalityParams.getCollectionString());
    }
}