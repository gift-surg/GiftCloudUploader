package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import java.util.Collection;

public interface SessionParameters {
    GiftCloudLabel.ExperimentLabel getExperimentLabel();

    Collection<?> getSessionVariables();

    GiftCloudLabel.ScanLabel getScanLabel();
}
