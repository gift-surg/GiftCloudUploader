/*=============================================================================

  GIFT-Cloud: A data storage and collaboration platform

  Copyright (c) University College London (UCL). All rights reserved.
  Released under the Modified BSD License
  github.com/gift-surg

  Author: Tom Doel
=============================================================================*/


package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PatientAliasMap;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.PatientListStore;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.ProjectSubjectAliasMap;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

public class ProjectSubjectPatientAliasMapTest {

    @Mock
    private GiftCloudReporter giftCloudReporter;

    @Captor
    private ArgumentCaptor<Map<String, PatientAliasMap>> mapCaptor;

    @Test
    public void testGetSubjectAliasFromEmptyListWithInitialCheck() throws Exception {
        final PatientListStore patientListStore = mock(PatientListStore.class);
        when(patientListStore.load()).thenReturn(new HashMap<String, PatientAliasMap>());
        final ProjectSubjectAliasMap projectSubjectAliasMap = new ProjectSubjectAliasMap(patientListStore);

        // Check that no alias is returned initially
        Assert.assertFalse(projectSubjectAliasMap.getSubjectAlias("PROJECT1", "HASHEDID1").isPresent());

        // Add an alias
        projectSubjectAliasMap.addAlias("PROJECT1", "HASHEDID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME1");
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT1", "HASHEDID1").get().getStringLabel(), "ALIAS1");

        // Add more aliases
        projectSubjectAliasMap.addAlias("PROJECT1", "HASHEDID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID2", "PATIENTNAME2");
        projectSubjectAliasMap.addAlias("PROJECT2", "HASHEDID3", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS3"), "PATIENTID3", "PATIENTNAME3");
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT1", "HASHEDID1").get().getStringLabel(), "ALIAS1");
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT1", "HASHEDID2").get().getStringLabel(), "ALIAS2");
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT2", "HASHEDID3").get().getStringLabel(), "ALIAS3");
    }

    @Test
    public void testGetSubjectAliasFromEmptyList() throws Exception {
        final PatientListStore patientListStore = mock(PatientListStore.class);
        when(patientListStore.load()).thenReturn(new HashMap<String, PatientAliasMap>());
        final ProjectSubjectAliasMap projectSubjectAliasMap = new ProjectSubjectAliasMap(patientListStore);

        // Add an alias
        projectSubjectAliasMap.addAlias("PROJECT1", "HASHEDID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME1");
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT1", "HASHEDID1").get().getStringLabel(), "ALIAS1");

        // Add more aliases
        projectSubjectAliasMap.addAlias("PROJECT1", "HASHEDID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID2", "PATIENTNAME2");
        projectSubjectAliasMap.addAlias("PROJECT2", "HASHEDID3", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS3"), "PATIENTID3", "PATIENTNAME3");
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT1", "HASHEDID1").get().getStringLabel(), "ALIAS1");
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT1", "HASHEDID2").get().getStringLabel(), "ALIAS2");
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT2", "HASHEDID3").get().getStringLabel(), "ALIAS3");
    }


    @Test
    public void testMultipleProjectsWithSameHashedIds() throws Exception {
        // This test is for when multiple projects have the same subject. We expect these to be treated as if they are different subjects
        final PatientListStore patientListStore = mock(PatientListStore.class);
        when(patientListStore.load()).thenReturn(new HashMap<String, PatientAliasMap>());
        final ProjectSubjectAliasMap projectSubjectAliasMap = new ProjectSubjectAliasMap(patientListStore);

        // Add an alias
        projectSubjectAliasMap.addAlias("PROJECT1", "HASHEDID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME1");
        projectSubjectAliasMap.addAlias("PROJECT2", "HASHEDID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID1", "PATIENTNAME2");

        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT1", "HASHEDID1").get().getStringLabel(), "ALIAS1");
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT2", "HASHEDID1").get().getStringLabel(), "ALIAS2");
    }

    @Test
    public void testLoadedList() throws Exception {

        // Construct an initial patient list
        Map<String, PatientAliasMap> initialPatientList = new HashMap<String, PatientAliasMap>();
        final PatientAliasMap patientAliasMap1 = new PatientAliasMap();
        patientAliasMap1.addSubjectAlias("HASHEDID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME1");
        patientAliasMap1.addSubjectAlias("HASHEDID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID2", "PATIENTNAME2");
        final PatientAliasMap patientAliasMap2 = new PatientAliasMap();
        patientAliasMap2.addSubjectAlias("HASHEDID3", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS3"), "PATIENTID3", "PATIENTNAME3");
        initialPatientList.put("PROJECT1", patientAliasMap1);
        initialPatientList.put("PROJECT2", patientAliasMap2);

        // Create a mock PatientListStore that will return this initial patient list
        final PatientListStore patientListStore = mock(PatientListStore.class);
        when(patientListStore.load()).thenReturn(initialPatientList);

        // Create the new ProjectSubjectAliasMap with this mock PatientlistStore
        final ProjectSubjectAliasMap projectSubjectAliasMap = new ProjectSubjectAliasMap(patientListStore);

        // Check the existing patients are there
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT1", "HASHEDID1").get().getStringLabel(), "ALIAS1");
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT1", "HASHEDID2").get().getStringLabel(), "ALIAS2");
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT2", "HASHEDID3").get().getStringLabel(), "ALIAS3");

        // Add another one
        projectSubjectAliasMap.addAlias("PROJECT2", "HASHEDID4", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS4"), "PATIENTID4", "PATIENTNAME4");
        Assert.assertEquals(projectSubjectAliasMap.getSubjectAlias("PROJECT2", "HASHEDID4").get().getStringLabel(), "ALIAS4");
    }

    @Test
    public void testListSaving() throws Exception {
        ArgumentCaptor<Map<String, PatientAliasMap>> mapArgumentCaptor = ArgumentCaptor.forClass((Class) Map.class);

        final PatientListStore patientListStore = mock(PatientListStore.class);
        when(patientListStore.load()).thenReturn(new HashMap<String, PatientAliasMap>());
        final ProjectSubjectAliasMap projectSubjectAliasMap = new ProjectSubjectAliasMap(patientListStore);

        // Construct a test patient list
        Map<String, PatientAliasMap> initialPatientList = new HashMap<String, PatientAliasMap>();
        final PatientAliasMap patientAliasMap1 = new PatientAliasMap();
        patientAliasMap1.addSubjectAlias("HASHEDID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME1");
        initialPatientList.put("PROJECT1", patientAliasMap1);

        // Add an alias and check the save call includes an equivalent map argument
        projectSubjectAliasMap.addAlias("PROJECT1", "HASHEDID1", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS1"), "PATIENTID1", "PATIENTNAME1");
        verify(patientListStore, times(1)).save(mapArgumentCaptor.capture());
        Assert.assertTrue(initialPatientList.equals(mapArgumentCaptor.getValue()));

        // Add another alias for the same project
        projectSubjectAliasMap.addAlias("PROJECT1", "HASHEDID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID2", "PATIENTNAME2");
        patientAliasMap1.addSubjectAlias("HASHEDID2", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS2"), "PATIENTID2", "PATIENTNAME2");
        verify(patientListStore, times(2)).save(mapArgumentCaptor.capture());
        Assert.assertTrue(initialPatientList.equals(mapArgumentCaptor.getValue()));

        // Add an alias for a different project
        projectSubjectAliasMap.addAlias("PROJECT2", "HASHEDID3", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS3"), "PATIENTID3", "PATIENTNAME3");
        final PatientAliasMap patientAliasMap2 = new PatientAliasMap();
        patientAliasMap2.addSubjectAlias("HASHEDID3", GiftCloudLabel.SubjectLabel.getFactory().create("ALIAS3"), "PATIENTID3", "PATIENTNAME3");
        initialPatientList.put("PROJECT2", patientAliasMap2);
        verify(patientListStore, times(3)).save(mapArgumentCaptor.capture());
        Assert.assertTrue(initialPatientList.equals(mapArgumentCaptor.getValue()));
    }



}