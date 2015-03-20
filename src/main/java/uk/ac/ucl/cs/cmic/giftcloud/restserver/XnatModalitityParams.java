package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class XnatModalitityParams {

    private final Optional<DicomModality> dicomModality;

    public XnatModalitityParams(final Set<String> modalities) {
        Set<DicomModality> modalitySet = new HashSet<DicomModality>();

        for (final String modality : modalities) {
            modalitySet.add(DicomModality.getModalityFromDicomTag(modality));
        }

        if (modalitySet.size() == 1) {
            dicomModality = Optional.of(modalitySet.iterator().next());
        } else {
            dicomModality = Optional.empty();
        }
    }

    public Optional<DicomModality> getModality() {
        return dicomModality;
    }


    public enum DicomModality {
        MR("MR", "ToDo"),
        UNKNOWN(null, "ToDo");

        private final String dicomTag;
        private final String xnatSessionType;

        DicomModality(final String dicomTag, final String xnatSessionType) {
            this.dicomTag = dicomTag;
            this.xnatSessionType = xnatSessionType;
        }

        String getDicomTag() {
            return dicomTag;
        }

        String getXnatSessionTag() {
            return xnatSessionType;
        }

        static DicomModality getModalityFromDicomTag(final String dicomTag) {
            for (DicomModality modality : EnumSet.allOf(DicomModality.class)) {
                if (dicomTag.equals(modality.getDicomTag())) {
                    return modality;
                }
            }
            return UNKNOWN;
        }

    }
}
