/* Copyright (c) 2001-2013, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.test;

import com.pixelmed.dicom.*;

import junit.framework.*;

public class TestRemoveIdentifyingAttributes extends TestCase {
	
	// constructor to support adding tests to suite ...
	
	public TestRemoveIdentifyingAttributes(String name) {
		super(name);
	}
	
	// add tests to suite manually, rather than depending on default of all test...() methods
	// in order to allow adding TestRemoveIdentifyingAttributes.suite() in AllTests.suite()
	// see Johannes Link. Unit Testing in Java pp36-47
	
	public static Test suite() {
		TestSuite suite = new TestSuite("TestRemoveIdentifyingAttributes");
		
		suite.addTest(new TestRemoveIdentifyingAttributes("TestRemoveIdentifyingAttributes_FromList"));
		
		return suite;
	}
		
	protected void setUp() {
	}
	
	protected void tearDown() {
	}
	
	private AttributeTag[] toBeRetained = {
		TagFromName.ProtocolName,
		TagFromName.SeriesDescription,
		TagFromName.StudyDescription
	};
	
	private AttributeTag[] toBeDummied = {
		TagFromName.DetectorID,
		TagFromName.DeviceSerialNumber,
		TagFromName.VerifyingObserverName
	};
	
	private AttributeTag[] toBeZeroed = {
		TagFromName.AccessionNumber,
		TagFromName.ContentCreatorName,
		TagFromName.FillerOrderNumberImagingServiceRequest,
		TagFromName.PatientBirthDate,
		TagFromName.PatientID,
		TagFromName.PatientName,
		TagFromName.PlacerOrderNumberImagingServiceRequest,
		TagFromName.ReferringPhysicianName,
		TagFromName.StudyID,
		TagFromName.VerifyingObserverIdentificationCodeSequence
	};
	
	private AttributeTag[] toBeRemoved = {
		TagFromName.ActualHumanPerformersSequence,
		TagFromName.ActualHumanPerformersSequence,
		TagFromName.AdditionalPatientHistory,
		TagFromName.AdmissionID,
		TagFromName.AdmittingDate,
		TagFromName.AdmittingDiagnosesCodeSequence,
		TagFromName.AdmittingDiagnosesDescription,
		TagFromName.AdmittingTime,
		TagFromName.Allergies,
		TagFromName.Arbitrary,
		TagFromName.AuthorObserverSequence,
		TagFromName.BranchOfService,
		TagFromName.CassetteID,
		TagFromName.ConfidentialityConstraintOnPatientDataDescription,
		TagFromName.ContentCreatorIdentificationCodeSequence,
		TagFromName.ContributionDescription,
		TagFromName.CountryOfResidence,
		TagFromName.CurrentPatientLocation,
		TagFromName.CustodialOrganizationSequence,
		TagFromName.DataSetTrailingPadding,
		TagFromName.DerivationDescription,
		TagFromName.DeviceUID,
		TagFromName.DistributionAddress,
		TagFromName.DistributionName,
		TagFromName.GantryID,
		TagFromName.GeneratorID,
		TagFromName.HumanPerformerName,
		TagFromName.HumanPerformerOrganization,
		TagFromName.IconImageSequence,
		TagFromName.IdentifyingComments,
		TagFromName.InstitutionAddress,
		TagFromName.InstitutionalDepartmentName,
		TagFromName.InstitutionName,
		TagFromName.InsurancePlanIdentification,
		TagFromName.IntendedRecipientsOfResultsIdentificationSequence,
		TagFromName.InterpretationApproverSequence,
		TagFromName.InterpretationAuthor,
		TagFromName.InterpretationIDIssuer,
		TagFromName.InterpretationRecorder,
		TagFromName.InterpretationTranscriber,
		TagFromName.IssuerOfAccessionNumberSequence,
		TagFromName.IssuerOfAdmissionID,
		TagFromName.IssuerOfAdmissionIDSequence,
		TagFromName.IssuerOfPatientID,
		TagFromName.IssuerOfPatientIDQualifiersSequence,
		TagFromName.IssuerOfServiceEpisodeID,
		TagFromName.IssuerOfServiceEpisodeIDSequence,
		TagFromName.LastMenstrualDate,
		TagFromName.MedicalAlerts,
		TagFromName.MedicalRecordLocator,
		TagFromName.MilitaryRank,
		TagFromName.ModifyingDeviceID,
		TagFromName.ModifyingDeviceManufacturer,
		TagFromName.NameOfPhysiciansReadingStudy,
		TagFromName.NamesOfIntendedRecipientsOfResults,
		TagFromName.Occupation,
		TagFromName.OperatorIdentificationSequence,
		TagFromName.OperatorsName,
		TagFromName.OrderCallbackPhoneNumber,
		TagFromName.OrderEnteredBy,
		TagFromName.OrderEntererLocation,
		TagFromName.OtherPatientIDs,
		TagFromName.OtherPatientIDsSequence,
		TagFromName.OtherPatientNames,
		TagFromName.ParticipantSequence,
		TagFromName.PatientAddress,
		TagFromName.PatientBirthName,
		TagFromName.PatientBirthTime,
		TagFromName.PatientComments,
		TagFromName.PatientInsurancePlanCodeSequence,
		TagFromName.PatientMotherBirthName,
		TagFromName.PatientPrimaryLanguageCodeSequence,
		TagFromName.PatientReligiousPreference,
		TagFromName.PatientState,
		TagFromName.PatientTelephoneNumbers,
		TagFromName.PerformedLocation,
		TagFromName.PerformedProcedureStepID,
		TagFromName.PerformedStationAETitle,
		TagFromName.PerformedStationGeographicLocationCodeSequence,
		TagFromName.PerformedStationName,
		TagFromName.PerformedStationNameCodeSequence,
		TagFromName.PerformingPhysicianIdentificationSequence,
		TagFromName.PerformingPhysicianName,
		TagFromName.PersonAddress,
		TagFromName.PersonIdentificationCodeSequence,
		TagFromName.PersonName,
		TagFromName.PersonTelephoneNumbers,
		TagFromName.PhysicianApprovingInterpretation,
		TagFromName.PhysiciansOfRecord,
		TagFromName.PhysiciansOfRecordIdentificationSequence,
		TagFromName.PhysiciansReadingStudyIdentificationSequence,
		TagFromName.PlateID,
		TagFromName.PreMedication,
		TagFromName.ReferencedPatientAliasSequence,
		TagFromName.ReferencedPatientSequence,
		TagFromName.ReferencedStudySequence,
		TagFromName.ReferringPhysicianAddress,
		TagFromName.ReferringPhysicianIdentificationSequence,
		TagFromName.ReferringPhysicianTelephoneNumbers,
		TagFromName.RegionOfResidence,
		TagFromName.RequestAttributesSequence,
		TagFromName.RequestedProcedureID,
		TagFromName.RequestedProcedureLocation,
		TagFromName.RequestingPhysician,
		TagFromName.RequestingService,
		TagFromName.ResponsibleOrganization,
		TagFromName.ResponsiblePerson,
		TagFromName.ResultsDistributionListSequence,
		TagFromName.ResultsIDIssuer,
		TagFromName.ScheduledHumanPerformersSequence,
		TagFromName.ScheduledPatientInstitutionResidence,
		TagFromName.ScheduledPerformingPhysicianIdentificationSequence,
		TagFromName.ScheduledPerformingPhysicianName,
		TagFromName.ScheduledProcedureStepLocation,
		TagFromName.ScheduledStationAETitle,
		TagFromName.ScheduledStationGeographicLocationCodeSequence,
		TagFromName.ScheduledStationName,
		TagFromName.ScheduledStationNameCodeSequence,
		TagFromName.ScheduledStudyLocation,
		TagFromName.ScheduledStudyLocationAETitle,
		TagFromName.ServiceEpisodeID,
		TagFromName.SpecialNeeds,
		TagFromName.StationName,
		TagFromName.StudyIDIssuer,
		TagFromName.StudyPriorityID,
		TagFromName.StudyStatusID,
		TagFromName.TextComments,
		TagFromName.TextString,
		TagFromName.TopicAuthor,
		TagFromName.TopicKeywords,
		TagFromName.TopicSubject,
		TagFromName.TopicTitle,
		TagFromName.VerifyingOrganization
	};
	
	public void TestRemoveIdentifyingAttributes_FromList() throws Exception {
		String originalValueToBeReplaced = "REPLACEMEPLEASE";
		AttributeList list = new AttributeList();
		for (AttributeTag t : toBeDummied) {
			Attribute a = AttributeFactory.newAttribute(t);
			list.put(a);
			a.addValue(originalValueToBeReplaced);
		}
		for (AttributeTag t : toBeZeroed) {
			Attribute a = AttributeFactory.newAttribute(t);
			list.put(a);
		}
		for (AttributeTag t : toBeRemoved) {
			Attribute a = AttributeFactory.newAttribute(t);
			list.put(a);
		}
		for (AttributeTag t : toBeRetained) {
			Attribute a = AttributeFactory.newAttribute(t);
			list.put(a);
		}
		ClinicalTrialsAttributes.removeOrNullIdentifyingAttributes(list,true/*keepUIDs*/,true/*keepDescriptors*/,true/*keepPatientCharacteristics*/);
		
		DicomDictionary dictionary = AttributeList.getDictionary();
		for (AttributeTag t : toBeDummied) {
			Attribute a = list.get(t);
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" is not removed",a != null);
			String replacedValue = a.getSingleStringValueOrNull();
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" is not null value",replacedValue != null);
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" has been replaced ",!originalValueToBeReplaced.equals(replacedValue));
		}
		for (AttributeTag t : toBeZeroed) {
			Attribute a = list.get(t);
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" is not removed",a != null);
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" is zero length",(a instanceof SequenceAttribute ? ((SequenceAttribute)a).getNumberOfItems() == 0 : a.getVL() == 0));
		}
		for (AttributeTag t : toBeRemoved) {
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" is removed",list.get(t) == null);
		}
		for (AttributeTag t : toBeRetained) {
			assertTrue("Checking "+dictionary.getNameFromTag(t)+" is retained",list.get(t) != null);
		}


	}
	
}
