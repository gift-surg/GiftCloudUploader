package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.*;

import java.util.Optional;

public class QueryParams {

    private Optional<String> patientName = Optional.empty();
    private Optional<String> patientId = Optional.empty();
    private Optional<String> accessionNumber = Optional.empty();
    private Optional<String> studyDate = Optional.empty();

    public void setPatientName(String patientName) {
        this.patientName = Optional.of(patientName);
    }

    public void setPatientId(String patientId) {
        this.patientId = Optional.of(patientId);
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = Optional.of(accessionNumber);
    }

    public void setStudyDate(String studyDate) {
        this.studyDate = Optional.of(studyDate);
    }

    public AttributeList build() throws DicomException {
        SpecificCharacterSet specificCharacterSet = new SpecificCharacterSet((String[])null);
        AttributeList filter = new AttributeList();
        {
            AttributeTag t = TagFromName.PatientName; Attribute a = new PersonNameAttribute(t,specificCharacterSet);
            if (patientName.isPresent()) {
                a.addValue(patientName.get());
            }
            filter.put(t,a);
        }
        {
            AttributeTag t = TagFromName.PatientID; Attribute a = new LongStringAttribute(t,specificCharacterSet);
            if (patientId.isPresent()) {
                a.addValue(patientId.get());
            }
            filter.put(t,a);
        }
        {
            AttributeTag t = TagFromName.AccessionNumber; Attribute a = new ShortStringAttribute(t,specificCharacterSet);
            if (accessionNumber.isPresent()) {
                a.addValue(accessionNumber.get());
            }
            filter.put(t,a);
        }
        { AttributeTag t = TagFromName.PatientBirthDate; Attribute a = new DateAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.PatientSex; Attribute a = new CodeStringAttribute(t); filter.put(t,a); }

        { AttributeTag t = TagFromName.StudyID; Attribute a = new ShortStringAttribute(t,specificCharacterSet); filter.put(t,a); }
        { AttributeTag t = TagFromName.StudyDescription; Attribute a = new LongStringAttribute(t,specificCharacterSet); filter.put(t,a); }
        { AttributeTag t = TagFromName.ModalitiesInStudy; Attribute a = new CodeStringAttribute(t); filter.put(t,a); }
        {
            AttributeTag t = TagFromName.StudyDate; Attribute a = new DateAttribute(t);
            if (studyDate.isPresent()) {
                a.addValue(studyDate.get());
            }
            filter.put(t,a);
        }
        { AttributeTag t = TagFromName.StudyTime; Attribute a = new TimeAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.PatientAge; Attribute a = new AgeStringAttribute(t); filter.put(t,a); }

        { AttributeTag t = TagFromName.SeriesDescription; Attribute a = new LongStringAttribute(t,specificCharacterSet); filter.put(t,a); }
        { AttributeTag t = TagFromName.SeriesNumber; Attribute a = new IntegerStringAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.Modality; Attribute a = new CodeStringAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.SeriesDate; Attribute a = new DateAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.SeriesTime; Attribute a = new TimeAttribute(t); filter.put(t,a); }

        { AttributeTag t = TagFromName.InstanceNumber; Attribute a = new IntegerStringAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.ContentDate; Attribute a = new DateAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.ContentTime; Attribute a = new TimeAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.ImageType; Attribute a = new CodeStringAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.NumberOfFrames; Attribute a = new IntegerStringAttribute(t); filter.put(t,a); }

        { AttributeTag t = TagFromName.StudyInstanceUID; Attribute a = new UniqueIdentifierAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.SeriesInstanceUID; Attribute a = new UniqueIdentifierAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.SOPInstanceUID; Attribute a = new UniqueIdentifierAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.SOPClassUID; Attribute a = new UniqueIdentifierAttribute(t); filter.put(t,a); }
        { AttributeTag t = TagFromName.SpecificCharacterSet; Attribute a = new CodeStringAttribute(t); filter.put(t,a); a.addValue("ISO_IR 100"); }

        return filter;
    }
}
