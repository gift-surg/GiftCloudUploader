package uk.ac.ucl.cs.cmic.giftcloud.uploadapplet;


import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SessionParameters;
import uk.ac.ucl.cs.cmic.giftcloud.data.Project;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionParams;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariable;
import uk.ac.ucl.cs.cmic.giftcloud.data.SessionVariableNames;
import uk.ac.ucl.cs.cmic.giftcloud.util.AutoArchive;

import java.net.URL;
import java.util.Collection;
import java.util.Map;

public class WizardSessionParameters implements SessionParameters {
    final Map<?, ?> sessionParameters;

    public WizardSessionParameters(final Map<?, ?> sessionParameters) {
        this.sessionParameters = sessionParameters;
    }

    @Override
    public GiftCloudLabel.ExperimentLabel getExperimentLabel() {
        return GiftCloudLabel.ExperimentLabel.getFactory().create((sessionParameters.get(SessionVariableNames.SESSION_LABEL)).toString());
    }

    @Override
    public String getVisit() {
        if (sessionParameters.get(SessionVariableNames.VISIT_LABEL) != null) {
            return ((SessionVariable) sessionParameters.get(SessionVariableNames.VISIT_LABEL)).getValue();
        } else {
            return null;
        }
    }

    @Override
    public String getProtocol() {
        if (sessionParameters.get(SessionVariableNames.PROTOCOL_LABEL) != null) {
            return ((SessionVariable) sessionParameters.get(SessionVariableNames.PROTOCOL_LABEL)).getValue();
        } else {
            return null;
        }
    }

    @Override
    public String getAdminEmail() {
        return (String)sessionParameters.get(SessionParams.XNAT_ADMIN_EMAIL_WIZ_PARAM);
    }

    @Override
    public URL getBaseURL() {
        return (URL)sessionParameters.get(SessionParams.XNAT_URL_WIZ_PARAM);
    }

    @Override
    public boolean getUseFixedSize() {
        return (Boolean)sessionParameters.get(SessionParams.FIXED_SIZE_STREAMING_WIZ_PARAM);
    }

    @Override
    public int getNumberOfThreads() {
        return (Integer)sessionParameters.get(SessionParams.N_UPLOAD_THREADS_WIZ_PARAM);
    }

    @Override
    public AutoArchive getAutoArchive() {
        if (sessionParameters.get(Project.AUTO_ARCHIVE) != null) {
            return (AutoArchive)sessionParameters.get(Project.AUTO_ARCHIVE);
        } else {
            return null;
        }
    }

    @Override
    public Collection<?> getSessionVariables() {
        return (Collection<?>) sessionParameters.get(AssignSessionVariablesPage.PRODUCT_NAME);
    }

    @Override
    public GiftCloudLabel.ScanLabel getScanLabel() {
        return null;
    }
}
