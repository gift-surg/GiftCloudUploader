package uk.ac.ucl.cs.cmic.giftcloud.data;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import uk.ac.ucl.cs.cmic.giftcloud.util.GiftCloudReporter;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.MultiUploadParameters;
import uk.ac.ucl.cs.cmic.giftcloud.uploadapplet.UploadSelector;

import java.util.HashMap;
import java.util.Map;

public class SessionParams {

    public static final String XNAT_URL_WIZ_PARAM = "*xnat-url*";
    public static final String XNAT_ADMIN_EMAIL_WIZ_PARAM = "*xnat-admin-email*";
    public static final String FIXED_SIZE_STREAMING_WIZ_PARAM = "*fixed-size-streaming*";
    public static final String N_UPLOAD_THREADS_WIZ_PARAM = "*n-upload-threads*";

    private Map<String, Object> params = new HashMap<String, Object>();

    public SessionParams(Map<String, Object> params) {
        this.params = params;
    }

    /**
     * Builds a map of the incoming parameters for managing the wizard.
     *
     * @return The map of parameters for the wizard.
     */
    public static SessionParams fromRestServer(final MultiUploadParameters multiUploadParameters, final UploadSelector uploadSelector, final GiftCloudReporter reporter) {
        final Map<String, Object> params = SessionParams.fromMultiUploadParameters(multiUploadParameters, reporter);
        params.put(UploadSelector.UPLOAD_SELECTOR_WIZARD_ID, uploadSelector);
        params.put(XNAT_URL_WIZ_PARAM, multiUploadParameters.getStrippedXnatUrl().get());
        return new SessionParams(params);
    }

    public static Map<String, Object> fromMultiUploadParameters(final MultiUploadParameters multiUploadParameters, final GiftCloudReporter reporter)
    {
        final Map<String, Object> params = Maps.newLinkedHashMap();
        params.put(XNAT_ADMIN_EMAIL_WIZ_PARAM, multiUploadParameters.getParameter(MultiUploadParameters.XNAT_ADMIN_EMAIL));
        params.put(FIXED_SIZE_STREAMING_WIZ_PARAM, Boolean.valueOf(multiUploadParameters.getParameter(MultiUploadParameters.USE_FIXED_SIZE_STREAMING, "false")));
        params.put(N_UPLOAD_THREADS_WIZ_PARAM, Integer.valueOf(multiUploadParameters.getParameter(MultiUploadParameters.N_UPLOAD_THREADS, "1")));

        final String visitLabel = multiUploadParameters.getParameter(MultiUploadParameters.XNAT_VISIT);
        //TODO: we want to verify this visit... and if there isn't a visit, we want to check the project to see if it has a protocol
        //so we can get a list of visits and let the user associate the session with a visit.
        //For now, we'll just assume the visit is a valid, existing label for a pVisitdata and pass it through to the importer.
        if (StringUtils.isNotBlank(visitLabel)) {
            reporter.trace("visit: {}", visitLabel);
            params.put(SessionVariableNames.VISIT_LABEL, new AssignedSessionVariable(SessionVariableNames.VISIT_LABEL, visitLabel));
        }

        final String protocolLabel = multiUploadParameters.getParameter(MultiUploadParameters.XNAT_PROTOCOL);
        //TODO: we want to verify this experiment's protocol is valid for this visit... and if there isn't a visit, we want to check the project to see
        //if it has a protocol so we can get a list of visits and let the user associate the session with a visit.
        //For now, we'll just assume the protocol is valid and pass it through to the importer.
        if (StringUtils.isNotBlank(protocolLabel)) {
            reporter.trace("protocol: {}", protocolLabel);
            params.put(SessionVariableNames.PROTOCOL_LABEL, new AssignedSessionVariable(SessionVariableNames.PROTOCOL_LABEL, protocolLabel));
        }

        final String expectedModality = multiUploadParameters.getParameter(MultiUploadParameters.EXPECTED_MODALITY);
        if (StringUtils.isNotBlank(expectedModality)) {
            reporter.trace("expected modality: {}", expectedModality);
            params.put(MultiUploadParameters.EXPECTED_MODALITY_LABEL, expectedModality);

        }

        final String sessionLabel = multiUploadParameters.getParameter(MultiUploadParameters.XNAT_SESSION);
        if (StringUtils.isNotBlank(sessionLabel)) {
            reporter.trace("session: {}", sessionLabel);
            params.put(SessionVariableNames.PREDEF_SESSION, new AssignedSessionVariable(SessionVariableNames.SESSION_LABEL, sessionLabel));
        }

        final String warnOnDupeSessionLabels = multiUploadParameters.getParameter(SessionVariableNames.WARN_ON_DUPE_SESSION_LABELS);
        if (StringUtils.isNotBlank(warnOnDupeSessionLabels)) {
            reporter.trace("Warn on dupe session labels: {}", warnOnDupeSessionLabels);
            params.put(SessionVariableNames.WARN_ON_DUPE_SESSION_LABELS, new AssignedSessionVariable(SessionVariableNames.WARN_ON_DUPE_SESSION_LABELS, warnOnDupeSessionLabels));
        }

        final String allowOverwriteOnDupeSessionLabels = multiUploadParameters.getParameter(SessionVariableNames.ALLOW_OVERWRITE_ON_DUPE_SESSION_LABELS);
        if (StringUtils.isNotBlank(allowOverwriteOnDupeSessionLabels)) {
            reporter.trace("Allow overwrite on dupe session labels: {}", allowOverwriteOnDupeSessionLabels);
            params.put(SessionVariableNames.ALLOW_OVERWRITE_ON_DUPE_SESSION_LABELS, new AssignedSessionVariable(SessionVariableNames.ALLOW_OVERWRITE_ON_DUPE_SESSION_LABELS, allowOverwriteOnDupeSessionLabels));
        }
        final String allowAppendOnDupeSessionLabels = multiUploadParameters.getParameter(SessionVariableNames.ALLOW_APPEND_ON_DUPE_SESSION_LABELS);
        if (StringUtils.isNotBlank(allowAppendOnDupeSessionLabels)) {
            reporter.trace("Allow append on dupe session labels: {}", allowAppendOnDupeSessionLabels);
            params.put(SessionVariableNames.ALLOW_APPEND_ON_DUPE_SESSION_LABELS, new AssignedSessionVariable(SessionVariableNames.ALLOW_APPEND_ON_DUPE_SESSION_LABELS, allowAppendOnDupeSessionLabels));
        }

        return params;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
