package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.TagFromName;
import com.pixelmed.query.QueryInformationModel;
import com.pixelmed.query.QueryTreeRecord;

public class QuerySelection {
    private AttributeList currentRemoteQuerySelectionUniqueKeys;
    private Attribute currentRemoteQuerySelectionUniqueKey;
    private String currentRemoteQuerySelectionRetrieveAE;
    private String currentRemoteQuerySelectionLevel;
    private QueryTreeRecord currentRemoteQuerySelectionQueryTreeRecord;


    public QuerySelection(QueryTreeRecord r, final QueryInformationModel queryInformationModel) {
        AttributeList uniqueKeys = r == null ? null : r.getUniqueKeys();
        Attribute uniqueKey = r == null ? null : r.getUniqueKey();
        AttributeList identifier = r == null ? null : r.getAllAttributesReturnedInIdentifier();

        currentRemoteQuerySelectionQueryTreeRecord = r;
        currentRemoteQuerySelectionUniqueKeys=uniqueKeys;
        currentRemoteQuerySelectionUniqueKey=uniqueKey;
        currentRemoteQuerySelectionRetrieveAE=null;
        if (identifier != null) {
            Attribute aRetrieveAETitle=identifier.get(TagFromName.RetrieveAETitle);
            if (aRetrieveAETitle != null) currentRemoteQuerySelectionRetrieveAE=aRetrieveAETitle.getSingleStringValueOrNull();
        }
        if (currentRemoteQuerySelectionRetrieveAE == null) {
            // it is legal for RetrieveAETitle to be zero length at all but the lowest levels of
            // the query model :( (See PS 3.4 C.4.1.1.3.2)
            // (so far the Leonardo is the only one that doesn't send it at all levels)
            // we could recurse down to the lower levels and get the union of the value there
            // but lets just keep it simple and ...
            // default to whoever it was we queried in the first place ...
            if (queryInformationModel != null) {
                currentRemoteQuerySelectionRetrieveAE = queryInformationModel.getCalledAETitle();
            }
        }
        currentRemoteQuerySelectionLevel = null;
        if (identifier != null) {
            Attribute a = identifier.get(TagFromName.QueryRetrieveLevel);
            if (a != null) {
                currentRemoteQuerySelectionLevel = a.getSingleStringValueOrNull();
            }
        }
        if (currentRemoteQuerySelectionLevel == null) {
            // QueryRetrieveLevel must have been (erroneously) missing in query response ... see with Dave Harvey's code on public server
            // so try to guess it from unique key in tree record
            // Fixes [bugs.mrmf] (000224) Missing query/retrieve level in C-FIND response causes tree select and retrieve to fail
            if (uniqueKey != null) {
                AttributeTag tag = uniqueKey.getTag();
                if (tag != null) {
                    if (tag.equals(TagFromName.PatientID)) {
                        currentRemoteQuerySelectionLevel="PATIENT";
                    }
                    else if (tag.equals(TagFromName.StudyInstanceUID)) {
                        currentRemoteQuerySelectionLevel="STUDY";
                    }
                    else if (tag.equals(TagFromName.SeriesInstanceUID)) {
                        currentRemoteQuerySelectionLevel="SERIES";
                    }
                    else if (tag.equals(TagFromName.SOPInstanceUID)) {
                        currentRemoteQuerySelectionLevel="IMAGE";
                    }
                }
            }
        }
    }

    public AttributeList getCurrentRemoteQuerySelectionUniqueKeys() {
        return currentRemoteQuerySelectionUniqueKeys;
    }

    public Attribute getCurrentRemoteQuerySelectionUniqueKey() {
        return currentRemoteQuerySelectionUniqueKey;
    }

    public String getCurrentRemoteQuerySelectionRetrieveAE() {
        return currentRemoteQuerySelectionRetrieveAE;
    }

    public String getCurrentRemoteQuerySelectionLevel() {
        return currentRemoteQuerySelectionLevel;
    }

    public QueryTreeRecord getCurrentRemoteQuerySelectionQueryTreeRecord() {
        return currentRemoteQuerySelectionQueryTreeRecord;
    }
}
