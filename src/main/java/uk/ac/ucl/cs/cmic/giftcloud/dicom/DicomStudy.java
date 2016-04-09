/*
 * uk.ac.ucl.cs.cmic.giftcloud.dicom.Study
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/11/14 4:28 PM
 */
package uk.ac.ucl.cs.cmic.giftcloud.dicom;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.nrg.dcm.edit.DicomUtils;
import uk.ac.ucl.cs.cmic.giftcloud.data.Study;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.Project;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.XnatModalityParams;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.UploadParameters;
import uk.ac.ucl.cs.cmic.giftcloud.util.MapRegistry;
import uk.ac.ucl.cs.cmic.giftcloud.util.Registry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DicomStudy extends MapEntity implements Entity, Study {

    public static final int MAX_TAG = Collections.max(new ArrayList<Integer>() {{
        add(Tag.AccessionNumber);
        add(Tag.StudyDate);
        add(Tag.StudyDescription);
        add(Tag.StudyID);
        add(Tag.StudyInstanceUID);
        add(Tag.StudyTime);
    }});

    private final Registry<Series> series = new MapRegistry<Series>(new TreeMap<Series, Series>());
    private final String patientId;
    private final String patientName;
    private final String studyUid;
    private final String seriesUid;

    public DicomStudy(final String uid, final Date dateTime, final String id, final String accessionNumber, final String description, final String patientId, final String patientName, final String seriesInstanceUid, final String studyInstanceUid) {
        put(Tag.StudyInstanceUID, uid);
        if (null != dateTime) {
            put(Tag.StudyDate, new SimpleDateFormat("yyyyMMdd").format(dateTime));
            put(Tag.StudyTime, new SimpleDateFormat("HHmmss").format(dateTime));
        }
        put(Tag.StudyID, id);
        put(Tag.AccessionNumber, accessionNumber);
        put(Tag.StudyDescription, description);
        this.patientId = patientId;
        this.patientName = patientName;
        this.seriesUid = seriesInstanceUid;
        this.studyUid = studyInstanceUid;
    }

    public DicomStudy(final DicomObject o) {
        this(o.getString(Tag.StudyInstanceUID),
                DicomUtils.getDateTime(o, Tag.StudyDate, Tag.StudyTime),
                o.getString(Tag.StudyID),
                o.getString(Tag.AccessionNumber),
                o.getString(Tag.StudyDescription),
                o.getString(Tag.PatientID),
                o.getString(Tag.PatientName),
                o.getString(Tag.SeriesInstanceUID),
                o.getString(Tag.StudyInstanceUID));
    }

    @Override
    public String getPatientId() {
        return patientId;
    }

    @Override
    public String getPatientName() {
        return patientName;
    }

    @Override
    public String getStudyUid() {
        return studyUid;
    }

    @Override
    public String getSeriesUid() {
        return seriesUid;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
        return o instanceof DicomStudy && Objects.equal(get(Tag.StudyInstanceUID), ((DicomStudy) o).get(Tag.StudyInstanceUID));
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.dcm.MapEntity#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(get(Tag.StudyInstanceUID));
    }

    public Series addFileAndGetSeries(final DicomObject o, final File f) {
        final Series s = series.get(new Series(o));
        s.addFile(f, o);
        return s;
    }

    /*
     * (non-Javadoc)
     * @see uk.ac.ucl.cs.cmic.giftcloud.dicom.Entity#getSeries()
     */
    public Collection<Series> getSeries() {
        return series.getAll();
    }

    /**
     * Provides a study identifier that is as unique and verbose as possible.
     *
     * @return The study identifier.
     * @see java.lang.Object#toString()
     */
    public String toString() {
        final StringBuilder builder = new StringBuilder("DICOM study ");
        final Object studyId = get(Tag.StudyID);
        builder.append(studyId);
        final Object accessionNumber = get(Tag.AccessionNumber);
        if (null != accessionNumber) {
            builder.append(" (").append(accessionNumber).append(")");
        }
        final Object description = get(Tag.StudyDescription);
        if (null != description) {
            builder.append(" ").append(description);
        }
        if (null == studyId && null == accessionNumber) {
            builder.append(" [").append(get(Tag.StudyInstanceUID)).append("]");
        }
        return builder.toString();
    }

    public List<FileCollection> getFiles() {
        final List<Series> uploads = Lists.newArrayList(Iterables.filter(series, new Predicate<Series>() {
            public boolean apply(final Series s) {
                return s.isUploadAllowed();
            }
        }));

        final List<FileCollection> fileCollections = new ArrayList<FileCollection>();
        for (final Series series : uploads) {
            fileCollections.add(series);
        }
        return fileCollections;
    }


    public XnatModalityParams getXnatModalityParams() {
        final Set<XnatModalityParams> xnatModalityParams = Sets.newLinkedHashSet();
        for (final Series s : series) {
            xnatModalityParams.add(s.getModalityParams());
        }

        // ToDo: we are only returning one modality param
        return xnatModalityParams.iterator().next();
    }

    @Override
    public SeriesZipper getSeriesZipper(final Project project, final UploadParameters uploadParameters) throws IOException {
        return new DicomSeriesZipper(project.getDicomMetaDataAnonymiser(), project.getPixelDataAnonymiser(), uploadParameters);
    }

}
