package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.ac.ucl.cs.cmic.giftcloud.util.OneWayHash;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SubjectAliasMapTest {

    @Mock
    private RestServerHelper restServerHelper;

    private SubjectAliasMap subjectAliasMap;
    private String projectName;
    private Optional<String> emptyString = Optional.empty();

    private final String patientId1 = "PatientOne1";
    private final String xnatSubjectName1 = "ResearchIdPatientOne";
    private final String hashedPatientId1 = OneWayHash.hashUid(patientId1);

    private final String patientId2 = "PatientTwo2";
    private final String xnatSubjectName2 = "ResearchIdPatientTwo";
    private final String hashedPatientId2 = OneWayHash.hashUid(patientId2);

    private final String patientId3 = "PatientThree3";
    private final String xnatSubjectName3 = "ResearchIdPatientThree";
    private final String hashedPatientId3 = OneWayHash.hashUid(patientId3);

    @Before
    public void setup() {
        projectName = "MyProject1";
        subjectAliasMap = new SubjectAliasMap(projectName, restServerHelper);
    }

    @Test
    public void testNoId() throws IOException {
        {
            // Check there is no existing ID
            when(restServerHelper.getSubjectPseudonym(projectName, hashedPatientId1)).thenReturn(emptyString);
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(patientId1);
            Assert.assertFalse(subjectIdOptional.isPresent());
        }
    }

    @Test
    public void testAddSubjectAlias() throws IOException {

        {
            // Add a pseudo ID
            subjectAliasMap.addSubjectAlias(patientId1, xnatSubjectName1);
            verify(restServerHelper, times(1)).createPseudonymIfNotExisting(projectName, xnatSubjectName1, hashedPatientId1);
        }

        {
            // Check a different ID is not found
            when(restServerHelper.getSubjectPseudonym(projectName, hashedPatientId2)).thenReturn(emptyString);
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(patientId2);
            Assert.assertFalse(subjectIdOptional.isPresent());
        }

        {
            // Check the newly added pseudo ID has been found, with no server call required
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(patientId1);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName1);
        }
    }

    @Test
    public void testGettingNameFromServer() throws IOException {

        // Set up server to return a pseudonym for id1 but not id2
        when(restServerHelper.getSubjectPseudonym(projectName, hashedPatientId1)).thenReturn(Optional.of(xnatSubjectName1));
        when(restServerHelper.getSubjectPseudonym(projectName, hashedPatientId2)).thenReturn(Optional.of(xnatSubjectName2));
        when(restServerHelper.getSubjectPseudonym(projectName, hashedPatientId3)).thenReturn(emptyString);

        {
            // Check id1 returns a subject
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(patientId1);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName1);
        }

        {
            // Check id2 returns a subject
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(patientId2);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName2);
        }

        {
            // Check id3 does not return a subject
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(patientId3);
            Assert.assertFalse(subjectIdOptional.isPresent());
        }

        {
            // Now set the server response for id 3 and check this works
            when(restServerHelper.getSubjectPseudonym(projectName, hashedPatientId3)).thenReturn(Optional.of(xnatSubjectName3));
            final Optional<String> subjectIdOptional = subjectAliasMap.getSubjectAlias(patientId3);
            Assert.assertTrue(subjectIdOptional.isPresent());
            Assert.assertEquals(subjectIdOptional.get(), xnatSubjectName3);
        }
    }

    @Test
    public void testCachingOfServerValues() throws IOException {

        // Set up server to return a pseudonym for id1 but not id2
        when(restServerHelper.getSubjectPseudonym(projectName, hashedPatientId1)).thenReturn(Optional.of(xnatSubjectName1));
        when(restServerHelper.getSubjectPseudonym(projectName, hashedPatientId2)).thenReturn(Optional.of(xnatSubjectName2));
        when(restServerHelper.getSubjectPseudonym(projectName, hashedPatientId3)).thenReturn(emptyString);

        {
            // Trigger caching of ids 1 and 2, while id 3 should not cache as it has not been set
            subjectAliasMap.getSubjectAlias(patientId1);
            subjectAliasMap.getSubjectAlias(patientId2);
            subjectAliasMap.getSubjectAlias(patientId3);

            // Get all ids again
            subjectAliasMap.getSubjectAlias(patientId1);
            subjectAliasMap.getSubjectAlias(patientId2);
            subjectAliasMap.getSubjectAlias(patientId3);

            // Verify that the server has only been called once for the ids that were set
            verify(restServerHelper, times(1)).getSubjectPseudonym(projectName, hashedPatientId1);
            verify(restServerHelper, times(1)).getSubjectPseudonym(projectName, hashedPatientId2);

            // Verify that the server was called both times for the unset id
            verify(restServerHelper, times(2)).getSubjectPseudonym(projectName, hashedPatientId3);

            // Now set the response for id 3 and query it. This should trigger one extra call to the server, during which the result it cached.
            when(restServerHelper.getSubjectPseudonym(projectName, hashedPatientId3)).thenReturn(Optional.of(xnatSubjectName3));
            subjectAliasMap.getSubjectAlias(patientId3);
            subjectAliasMap.getSubjectAlias(patientId3);
            verify(restServerHelper, times(3)).getSubjectPseudonym(projectName, hashedPatientId3);
        }

    }

}