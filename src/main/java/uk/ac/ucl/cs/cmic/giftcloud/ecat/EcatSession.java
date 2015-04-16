/*
 * uk.ac.ucl.cs.cmic.giftcloud.ecat.EcatSession
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 12:40 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.ecat;

import com.google.common.collect.Lists;
import netscape.javascript.JSObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.nrg.ecat.MatrixDataFile;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;
import org.nrg.ecat.edit.ScriptApplicator;
import org.nrg.ecat.edit.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.SessionParameters;
import uk.ac.ucl.cs.cmic.giftcloud.util.MultiUploadReporter;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.RestServerHelper;
import uk.ac.ucl.cs.cmic.giftcloud.data.*;

import javax.swing.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class EcatSession implements Session {
    private static final String FORMAT = "ECAT";
    private static final String MODALITY = "PET";

    private final Logger logger = LoggerFactory.getLogger(EcatSession.class);
    private final MatrixDataFile first;
    private final Collection<MatrixDataFile> data;
    private long size = 0;
    
    private TimeZone timeZone = null;
    
    private static final String [] timezones = new String[] {
		System.getProperty("user.timezone"),
		"America/New_York", 
		"America/Chicago", 
		"America/Los_Angeles",
		"Pacific/Midway",
	    "US/Hawaii",
	    "US/Alaska",
	    "US/Pacific",
	    "America/Tijuana",
	    "US/Arizona",
	    "America/Chihuahua",
	    "US/Mountain",
	    "America/Guatemala",
	    "US/Central",
	    "America/Mexico_City",
	    "Canada/Saskatchewan",
	    "America/Bogota",
	    "US/Eastern",
	    "US/East-Indiana",
	    "Canada/Eastern",
	    "America/Caracas",
	    "America/Manaus",
	    "America/Santiago",
	    "Canada/Newfoundland",
	    "Brazil/East",
	    "America/Buenos_Aires",
	    "America/Godthab",
	    "America/Montevideo",
	    "Atlantic/South_Georgia",
	    "Atlantic/Azores",
	    "Atlantic/Cape_Verde",
	    "Africa/Casablanca",
	    "Europe/London",
	    "Europe/Berlin",
	    "Europe/Belgrade",
	    "Europe/Brussels",
	    "Europe/Warsaw",
	    "Africa/Algiers",
	    "Asia/Amman",
	    "Europe/Athens",
	    "Asia/Beirut",
	    "Africa/Cairo",
	    "Africa/Harare",
	    "Europe/Helsinki",
	    "Asia/Jerusalem",
	    "Europe/Minsk",
	    "Africa/Windhoek",
	    "Asia/Baghdad",
	    "Asia/Kuwait",
	    "Europe/Moscow",
	    "Africa/Nairobi",
	    "Asia/Tbilisi",
	    "Asia/Tehran",
	    "Asia/Muscat",
	    "Asia/Baku",
	    "Asia/Yerevan",
	    "Asia/Kabul",
	    "Asia/Yekaterinburg",
	    "Asia/Karachi",
	    "Asia/Calcutta",
	    "Asia/Colombo",
	    "Asia/Katmandu",
	    "Asia/Novosibirsk",
	    "Asia/Dhaka",
	    "Asia/Rangoon",
	    "Asia/Bangkok",
	    "Asia/Krasnoyarsk",
	    "Asia/Hong_Kong",
	    "Asia/Irkutsk",
	    "Asia/Kuala_Lumpur",
	    "Australia/Perth",
	    "Asia/Taipei",
	    "Asia/Tokyo",
	    "Asia/Seoul",
	    "Asia/Yakutsk",
	    "Australia/Adelaide",
	    "Australia/Darwin",
	    "Australia/Brisbane",
	    "Australia/Sydney",
	    "Pacific/Guam",
	    "Australia/Hobart",
	    "Asia/Vladivostok",
	    "Asia/Magadan",
	    "Pacific/Auckland",
	    "Pacific/Fiji",
	    "Pacific/Tongatapu"		
    };
    
    @SuppressWarnings("unchecked")
    public EcatSession(final Collection<MatrixDataFile> files) {
        final List<MatrixDataFile> list = Lists.newArrayList(files);
        Collections.sort(list);
        this.first = files.isEmpty() ? null : list.get(0);
        this.data = Collections.unmodifiableCollection(list);
    }

    @Override
    public String getPatientId() {
        return null;  // ToDo: we don't currently support ecat in the multi upload
    }

    @Override
    public String getStudyUid() {
        return null;  // ToDo: we don't currently support ecat in the multi upload
    }

    @Override
    public String getSeriesUid() {
        return null;  // ToDo: we don't currently support ecat in the multi upload
    }

    /* (non-Javadoc)
     * @see Session#getAccession()
     */
    public String getAccession() { return null; }

    
    /* (non-Javadoc)
     * @see Session#getDateTime()
     */
    public Date getDateTime() {
    	
    	// The first time this is called, the user is prompted to enter the timezone in which this
    	// session was acquired. Subsequent calls re-use the timezone that was selected.
    	
    	if(timeZone == null){
    		JComboBox list = new JComboBox(timezones);
    		JPanel panel = new JPanel();
    		JLabel l = new JLabel("Select the Time Zone in which this scan was acquired");
    	    
    		panel.add(l);
    	    panel.add(list);
    	    
    		JOptionPane.showMessageDialog(null, panel, "Select the Time Zone in which this scan was acquired", JOptionPane.PLAIN_MESSAGE);
    		timeZone = TimeZone.getTimeZone(timezones[list.getSelectedIndex()]);
    	}
        DateTime original = new DateTime(first.getDate().getTime());
        DateTime zoned = original.withZone(DateTimeZone.forTimeZone(timeZone));
        return zoned.toDate();    	  
    }

    /* (non-Javadoc)
     * @see Session#getDescription()
     */
    public String getDescription() { return first.getDescription(); }

    /* (non-Javadoc)
     * @see Session#getFileCount()
     */
    public int getFileCount() { return data.size(); }

    /* (non-Javadoc)
     * @see Session#getFormat()
     */
    public String getFormat() { return FORMAT; }

    /* (non-Javadoc)
     * @see Session#getID()
     */
    public String getID() { return first.getPatientID(); }

    /* (non-Javadoc)
     * @see Session#getModalities()
     */
    public Set<String> getModalities() { return Collections.singleton(MODALITY); }

    /* (non-Javadoc)
     * @see Session#getScanCount()
     */
    public int getScanCount() { return data.size(); }

    /* (non-Javadoc)
     * @see Session#getTimeZone()
     */
    public TimeZone getTimeZone() { return timeZone; }
    
    /* (non-Javadoc)
     * @see Session#getSize()
     */
    public long getSize() {
        if (0 == size) {
            for (final MatrixDataFile f : data){
                size += f.getSize();
            }
        }
        return size;
    }

    /* (non-Javadoc)
     * @see Session#getVariables(java.util.Map)
     */
    public List<SessionVariable> getVariables(final Project project, final Session session) {
        final List<Variable> evs;
        try {
            final ScriptApplicator applicator = project.getEcatScriptApplicator(session);
            if (null == applicator) {
                logger.info("no script available");
                return Collections.emptyList();
            } else {
                evs = applicator.getSortedVariables();
            }
        } catch (Throwable t) {
            logger.warn("unable to load script applicator", t);
            return Collections.emptyList();
        }
        final List<SessionVariable> sessionVars = Lists.newArrayList();
        sessionVars.add(new AssignedSessionVariable(SessionVariableNames.MODALITY_LABEL, MODALITY, true));
        for (final Variable ev : evs) {
            sessionVars.add(EcatSessionVariable.getSessionVariable(ev));
        }
        return sessionVars;
    }

    /**
     * Provides a study identifier that is as unique and verbose as possible.
     * @return The study identifier.
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	Date sessionDate = getDateTime();
    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm z");
    	if(timeZone != null){
    		sdf.setTimeZone(timeZone);
    	}
    		
        final StringBuilder builder = new StringBuilder("ECAT study ");
        final String studyId = getID();
        if (null != studyId) {
            builder.append(studyId);
        }
        final Object accessionNumber = getAccession();
        if (null != accessionNumber) {
            builder.append(" (").append(accessionNumber).append(")");
        }
        if (null != sessionDate) {
            builder.append(" ").append(sdf.format(sessionDate));
        }
        final Object description = getDescription();
        if (null != description) {
            builder.append(" ").append(description);
        }
        if (null == studyId && null == accessionNumber) {
            builder.append(" [NO ECAT ID]");
        }
        return builder.toString();
    }


    /* (non-Javadoc)
     * @see Session#uploadTo(java.util.Map, org.netbeans.spi.wizard.ResultProgressHandle)
     */
    public boolean uploadTo(final String projectLabel, final String subjectLabel, final RestServerHelper restServerHelper, final SessionParameters sessionParameters, final Project project, final ResultProgressHandle progress, final Optional<String> windowName, final Optional<JSObject> jsContext, final UploadFailureHandler failureHandler, final MultiUploadReporter logger) {
        return restServerHelper.uploadToEcat(new MatrixDataFileCollection(data), projectLabel, subjectLabel, sessionParameters, progress, windowName, jsContext, failureHandler, timeZone, logger);
    }

    @Override
    public boolean appendTo(String projectLabel, String subjectLabel, RestServerHelper restServerHelper, SessionParameters sessionParameters, Project project, ResultProgressHandle progress, Optional<String> windowName, Optional<JSObject> jsContext, UploadFailureHandler failureHandler, MultiUploadReporter logger) throws IOException {
        return restServerHelper.uploadToEcat(new MatrixDataFileCollection(data), projectLabel, subjectLabel, sessionParameters, progress, windowName, jsContext, failureHandler, timeZone, logger);
    }

    public List<FileCollection> getFiles() {
        final List<FileCollection> fileCollections = new ArrayList<FileCollection>();
        fileCollections.add(new MatrixDataFileCollection(data));
        return fileCollections;
    }


}
