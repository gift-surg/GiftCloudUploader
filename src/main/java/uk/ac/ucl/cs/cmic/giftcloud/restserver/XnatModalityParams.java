package uk.ac.ucl.cs.cmic.giftcloud.restserver;

import org.apache.commons.lang.StringUtils;

import java.util.EnumSet;

public class XnatModalityParams {

    private String xnatSessionTag = XnatScanType.Unknown.getXnatSessionType();
    private String xnatScanTag = XnatScanType.Unknown.getXnatScanType();
    private String formatString;
    private String collectionString;

    private XnatModalityParams(final XnatScanType primaryXnatScanType, final XnatScanType secondaryScanType, final ImageFileFormat imageFileFormat) {
        {
            this.formatString = imageFileFormat.getFormatString();
            this.collectionString = imageFileFormat.getCollectionString();

            // Get the session type from the primary scan type. If there is no corresponding session type (for example, if the scan is a secondary capture) then we use the secondary scan type
            String xnatSessionTagFromScanType = primaryXnatScanType.getXnatSessionType();
            if (StringUtils.isBlank(xnatSessionTagFromScanType)) {
                xnatSessionTagFromScanType = secondaryScanType.getXnatSessionType();
            }
            if (StringUtils.isNotBlank(xnatSessionTagFromScanType)) {
                xnatSessionTag = xnatSessionTagFromScanType;
            }

            String xnatScanTagFromScanType = primaryXnatScanType.getXnatScanType();
            if (StringUtils.isBlank(xnatScanTagFromScanType)) {
                xnatScanTagFromScanType = secondaryScanType.getXnatScanType();
            }
            if (StringUtils.isNotBlank(xnatScanTagFromScanType)) {
                xnatScanTag = xnatScanTagFromScanType;
            }
        }
    }

    public static XnatModalityParams createFromDicom(final String dicomModalityString, final String sopClassUID) {
        final XnatScanType primaryScanType = DicomSopClass.getModalityFromDicomTag(sopClassUID).getXnatScanType();
        final XnatScanType secondaryScanType = DicomModality.getModalityFromDicomTag(dicomModalityString).getXnatScanType();
        return new XnatModalityParams(primaryScanType, secondaryScanType, ImageFileFormat.DICOM);
    }

    public String getXnatSessionTag() {
        return xnatSessionTag;
    }

    public String getXnatScanTag() {
        return xnatScanTag;
    }

    public String getFormatString() {
        return formatString;
    }

    public String getCollectionString() {
        return collectionString;
    }

    public enum ImageFileFormat {
        DICOM("DICOM", "DICOM");

        private final String formatString;
        private final String collectionString;

        ImageFileFormat(final String formatString, final String collectionString) {
            this.formatString = formatString;
            this.collectionString = collectionString;
        }

        public String getFormatString() {
            return formatString;
        }

        public String getCollectionString() {
            return collectionString;
        }
    }

    public enum XnatScanType {
        MR("mrScanData", "mrSessionData"),
        PET("petScanData", "petSessionData"),
        CT("ctScanData", "ctSessionData"),
        EPS("epsScanData", "epsSessionData"),
        HD("hdScanData", "hdSessionData"),
        ECG("ecgScanData", "ecgSessionData"),
        US("usScanData", "usSessionData"),
        IO("ioScanData", "ioSessionData"),
        MG("mgScanData", "mgSessionData"),
        DX("dxScanData", "dxSessionData"),
        CR("crScanData", "crSessionData"),
        GMV("gmvScanData", "gmvSessionData"),
        GM("gmScanData", "gmSessionData"),
        ESV("esvScanData", "esvSessionData"),
        ES("esScanData", "esSessionData"),
        NM("nmScanData", "nmSessionData"),
        SR("srScanData", "srSessionData"),
        DX3DCraniofacial("dx3DCraniofacialScanData", "dx3DCraniofacialSessionData"),
        XA3D("xa3DScanData", "xa3DSessionData"),
        RF("rfScanData", "rfSessionData"),
        XA("xaScanData", "xaSessionData"),
        SM("smScanData", "smSessionData"),
        XC("xcScanData", "xcSessionData"),
        XCV("xcvScanData", "xcvSessionData"),
        OP("opScanData", "opSessionData"),
        OPT("optScanData", "optSessionData"),
        RTImage("rtImageScanData", "rtSessionData"),
        SC("scScanData", null),
        Seg("segScanData", null),
        MRS("mrsScanData", null),
        VoiceAudio("voiceAudioScanData", null),
        OtherDicom("otherDicomScanData", "otherDicomSessionData"),
        MEG("megScanData", "megSessionData"),
        EEG("eegScanData", "eegSessionData"),
        Unknown("otherDicomScanData", "otherDicomSessionData");

        private final String xnatScanType;
        private final String xnatSessionType;

        String getXnatScanType() {
            return xnatScanType == null ? null : "xnat:" + xnatScanType;
        }

        private XnatScanType(final String xnatScanType, final String xnatSessionType) {
            this.xnatScanType = xnatScanType;
            this.xnatSessionType = xnatSessionType;
        }

        public String getXnatSessionType() {
            return xnatSessionType == null ? null : "xnat:" + xnatSessionType;
        }
    }

    /**
     * Enumeration of known DICOM modalities and the equivalent XNAT scan types. In some cases a more specific XNAT
     * scan type can be found by considering the SOP class ID tag of the Dicom image.
     */
    public enum DicomModality {
        // Dicom types with XNAT support
        MR("MR", XnatScanType.MR, "Magnetic Resonance"),
        PT("PT", XnatScanType.PET, "Positron emission tomography (PET)"),
        CT("CT", XnatScanType.CT, "Computed Tomography"),
        EPS("EPS", XnatScanType.EPS, "Cardiac Electrophysiology"),
        HD("HD", XnatScanType.HD, "Hemodynamic Waveform"),
        ECG("ECG", XnatScanType.ECG, "Electrocardiography"),
        US("US", XnatScanType.US, "Ultrasound"),
        IO("IO", XnatScanType.IO, "Intra-oral Radiography"),
        MG("MG", XnatScanType.MG, "Mammography"),
        DX("DX", XnatScanType.DX, "Digital Radiography"),
        CR("CR", XnatScanType.CR, "Computed Radiography"),
        GM("GM", XnatScanType.GM, "General Microscopy"),
        ES("ES", XnatScanType.ES, "Endoscopy"),
        NM("NM", XnatScanType.NM, "Nuclear Medicine"),
        SR("SR", XnatScanType.SR, "SR Document"),
        RF("RF", XnatScanType.RF, "Radio Fluoroscopy"),
        XA("XA", XnatScanType.XA, "X-Ray Angiography"),
        SM("SM", XnatScanType.SM, "Slide Microscopy"),
        OP("OP", XnatScanType.OP, "Ophthalmic Photography"),
        OPT("OPT", XnatScanType.OPT, "Ophthalmic Tomography"),
        RTIMAGE("RTIMAGE", XnatScanType.RTImage, "Radiotherapy Image"),
        SEG("SEG", XnatScanType.Seg, "Segmentation"),
        MRS("MRS", XnatScanType.MRS, "mrsScanData"),
        AU("AU", XnatScanType.VoiceAudio, "Audio"),

        // Dicom types not supported in XNAT
        OT("OT", XnatScanType.OtherDicom, "Other"),
        BI("BI", XnatScanType.OtherDicom, "Biomagnetic imaging"),
        DG("DG", XnatScanType.OtherDicom, "Diaphanography"),
        LS("LS", XnatScanType.OtherDicom, "Laser surface scan"),
        RG("RG", XnatScanType.OtherDicom, "Radiographic imaging (conventional film/screen)"),
        TG("TG", XnatScanType.OtherDicom, "Thermography"),
        RTDOSE("RTDOSE", XnatScanType.OtherDicom, "Radiotherapy Dose"),
        RTSTRUCT("RTSTRUCT",XnatScanType.OtherDicom, "Radiotherapy Structure Set"),
        RTPLAN("RTPLAN", XnatScanType.OtherDicom, "Radiotherapy Plan"),
        RTRECORD("RTRECORD", XnatScanType.OtherDicom, "RT Treatment Record"),
        HC("HC", XnatScanType.OtherDicom, "Hard Copy"),
        PX("PX", XnatScanType.OtherDicom, "Panoramic X-Ray"),
        XC("XC", XnatScanType.OtherDicom, "External-camera Photography"),
        PR("PR", XnatScanType.OtherDicom, "Presentation State"),
        AR("AR", XnatScanType.OtherDicom, "Autorefraction"),
        VA("VA", XnatScanType.OtherDicom, "Visual Acuity"),
        OCT("OCT", XnatScanType.OtherDicom,"Optical Coherence Tomography (non-Ophthalmic)"),
        OPV("OPV", XnatScanType.OtherDicom, "Ophthalmic Visual Field"),
        OAM("OAM", XnatScanType.OtherDicom, "Ophthalmic Axial Measurements"),
        KO("KO", XnatScanType.OtherDicom, "Key Object Selection"),
        REG("REG", XnatScanType.OtherDicom, "Registration"),
        BDUS("BDUS", XnatScanType.OtherDicom, "Bone Densitometry(ultrasound)"),
        DOC("DOC", XnatScanType.OtherDicom, "Document"),
        PLAN("PLAN", XnatScanType.OtherDicom, "Plan"),
        IVOCT("IVOCT", XnatScanType.OtherDicom, "Intravascular Optical Coherence Tomography"),
        IVUS("IVUS", XnatScanType.OtherDicom, "Intravascular Ultrasound"),
        SMR("SMR", XnatScanType.OtherDicom, "Stereometric Relationship"),
        KER("KER", XnatScanType.OtherDicom, "Keratometry"),
        SRF("SRF", XnatScanType.OtherDicom, "Subjective Refraction"),
        LEN("LEN", XnatScanType.OtherDicom, "Lensometry"),
        OPM("OPM", XnatScanType.OtherDicom, "Ophthalmic Mapping"),
        RESP("RESP", XnatScanType.OtherDicom, "Respiratory Waveform"),
        BMD("BMD", XnatScanType.OtherDicom, "Bone Densitometry(X - Ray)"),
        FID("FID", XnatScanType.OtherDicom, "Fiducials"),
        IOL("IOL", XnatScanType.OtherDicom, "Intraocular Lens Data"),

        Unknown(null, XnatScanType.OtherDicom, "Unrecognised modality");

        private final String description;
        private final String dicomTag;
        private final XnatScanType xnatScanType;

        private DicomModality(final String dicomTag, final XnatScanType xnatScanType, final String description) {
            this.dicomTag = dicomTag;
            this.xnatScanType = xnatScanType;
            this.description = description;
        }

        String getDicomTag() {
            return dicomTag;
        }

        XnatScanType getXnatScanType() {
            return xnatScanType;
        }

        static DicomModality getModalityFromDicomTag(final String dicomTag) {
            for (DicomModality modality : EnumSet.allOf(DicomModality.class)) {
                if (dicomTag.equals(modality.getDicomTag())) {
                    return modality;
                }
            }
            return Unknown;
        }

    }

    public enum DicomSopClass {

        ComputedRadiographyImageStorage("1.2.840.10008.5.1.4.1.1.1", XnatScanType.CR),

        DigitalXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.1.1", XnatScanType.DX),
        DigitalXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.1.1.1", XnatScanType.DX),

        DigitalMammographyXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.1.2", XnatScanType.MG),
        DigitalMammographyXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.1.2.1", XnatScanType.MG),

        DigitalIntraoralXRayImageStorageForPresentation("1.2.840.10008.5.1.4.1.1.1.3", XnatScanType.IO),
        DigitalIntraoralXRayImageStorageForProcessing("1.2.840.10008.5.1.4.1.1.1.3.1", XnatScanType.IO),

        CTImageStorage("1.2.840.10008.5.1.4.1.1.2", XnatScanType.CT),
        EnhancedCTImageStorage("1.2.840.10008.5.1.4.1.1.2.1", XnatScanType.CT),

        MRImageStorage("1.2.840.10008.5.1.4.1.1.4", XnatScanType.MR),
        EnhancedMRImageStorage("1.2.840.10008.5.1.4.1.1.4.1", XnatScanType.MR),
        MRSpectroscopyStorage("1.2.840.10008.5.1.4.1.1.4.2", XnatScanType.MR),
        EnhancedMRColorImageStorage("1.2.840.10008.5.1.4.1.1.4.3", XnatScanType.MR),

        PositronEmissionTomographyImageStorage("1.2.840.10008.5.1.4.1.1.128", XnatScanType.PET),
        EnhancedPETImageStorage("1.2.840.10008.5.1.4.1.1.130", XnatScanType.PET),

        UltrasoundImageStorageRetired("1.2.840.10008.5.1.4.1.1.6", XnatScanType.US),
        UltrasoundMultiframeImageStorage("1.2.840.10008.5.1.4.1.1.3.1", XnatScanType.US),
        UltrasoundImageStorage("1.2.840.10008.5.1.4.1.1.6.1", XnatScanType.US),
        EnhancedUSVolumeStorage ("1.2.840.10008.5.1.4.1.1.6.2", XnatScanType.US),

        SecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7", XnatScanType.SC),
        MultiframeSingleBitSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.1", XnatScanType.SC),
        MultiframeGrayscaleByteSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.2", XnatScanType.SC),
        MultiframeGrayscaleWordSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.3", XnatScanType.SC),
        MultiframeTrueColorSecondaryCaptureImageStorage("1.2.840.10008.5.1.4.1.1.7.4", XnatScanType.SC),

        _12leadECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.1", XnatScanType.ECG),
        GeneralECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.2", XnatScanType.ECG),
        AmbulatoryECGWaveformStorage("1.2.840.10008.5.1.4.1.1.9.1.3", XnatScanType.ECG),

        HemodynamicWaveformStorage("1.2.840.10008.5.1.4.1.1.9.2.1", XnatScanType.HD),
        CardiacElectrophysiologyWaveformStorage("1.2.840.10008.5.1.4.1.1.9.3.1", XnatScanType.EPS),

        BasicVoiceAudioWaveformStorage("1.2.840.10008.5.1.4.1.1.9.4.1", XnatScanType.VoiceAudio),
        GeneralAudioWaveformStorage("1.2.840.10008.5.1.4.1.1.9.4.2", XnatScanType.VoiceAudio),

        XRayAngiographicImageStorage("1.2.840.10008.5.1.4.1.1.12.1", XnatScanType.XA),
        EnhancedXAImageStorage("1.2.840.10008.5.1.4.1.1.12.1.1", XnatScanType.XA),

        XRay3DAngiographicImageStorage("1.2.840.10008.5.1.4.1.1.13.1.1", XnatScanType.XA3D),

        XRayRadiofluoroscopicImageStorage("1.2.840.10008.5.1.4.1.1.12.2", XnatScanType.RF),
        EnhancedXRFImageStorage("1.2.840.10008.5.1.4.1.1.12.2.1", XnatScanType.RF),

        XRay3DCraniofacialImageStorage("1.2.840.10008.5.1.4.1.1.13.1.2", XnatScanType.DX3DCraniofacial),

        NuclearMedicineImageStorage("1.2.840.10008.5.1.4.1.1.20", XnatScanType.NM),

        VLEndoscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.1", XnatScanType.ESV),
        VideoEndoscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.1.1", XnatScanType.ESV),
        VideoMicroscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.2.1", XnatScanType.GMV),
        VLMicroscopicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.2	", XnatScanType.GMV),

        VideoPhotographicImageStorage("1.2.840.10008.5.1.4.1.1.77.1.4.1", XnatScanType.XCV),

        OphthalmicPhotography8BitImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.1", XnatScanType.OP),
        OphthalmicPhotography16BitImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.2", XnatScanType.OP),
        OphthalmicTomographyImageStorage("1.2.840.10008.5.1.4.1.1.77.1.5.4", XnatScanType.OPT),

        BasicTextSRStorage("1.2.840.10008.5.1.4.1.1.88.11", XnatScanType.SR),
        EnhancedSRStorage("1.2.840.10008.5.1.4.1.1.88.22", XnatScanType.SR),
        ComprehensiveSR("1.2.840.10008.5.1.4.1.1.88.33", XnatScanType.SR),

        RTImageStorage("1.2.840.10008.5.1.4.1.1.481.1", XnatScanType.RTImage),
        RTDoseStorage("1.2.840.10008.5.1.4.1.1.481.2", XnatScanType.RTImage),

        Unknown(null, XnatScanType.Unknown);


        // Dicom SOP class UIDs not supported by XNAT

//        Arterial Pulse Waveform Storage	1.2.840.10008.5.1.4.1.1.9.5.1	Arterial Pulse Waveform

//        Respiratory Waveform Storage	1.2.840.10008.5.1.4.1.1.9.6.1	Respiratory Waveform

//        Grayscale Softcopy Presentation State Storage	1.2.840.10008.5.1.4.1.1.11.1	Grayscale Softcopy Presentation State Storage
//        Color Softcopy Presentation State Storage	1.2.840.10008.5.1.4.1.1.11.2	Color Softcopy Presentation State
//        Pseudo-Color Softcopy Presentation State Storage	1.2.840.10008.5.1.4.1.1.11.3	Pseudo-Color Softcopy Presentation State
//        Blending Softcopy Presentation State Storage	1.2.840.10008.5.1.4.1.1.11.4	Blending Softcopy Presentation State
//        XA/XRF Grayscale Softcopy Presentation State Storage	1.2.840.10008.5.1.4.1.1.11.5	XA/XRF Grayscale Softcopy Presentation State

//        Breast Tomosynthesis Image Storage	1.2.840.10008.5.1.4.1.1.13.1.3	Breast Tomosynthesis Image

//        Intravascular Optical Coherence Tomography Image Storage – For Presentation	1.2.840.10008.5.1.4.1.1.14.1	IVOCT IOD (see B.5.1.13)
//        Intravascular Optical Coherence Tomography Image Storage – For Processing	1.2.840.10008.5.1.4.1.1.14.2	IVOCT IOD (see B.5.1.13)

//        Raw Data Storage	1.2.840.10008.5.1.4.1.1.66	Raw Data
//        Spatial Registration Storage	1.2.840.10008.5.1.4.1.1.66.1	Spatial Registration
//        Spatial Fiducials Storage	1.2.840.10008.5.1.4.1.1.66.2	Spatial Fiducials
//        Deformable Spatial Registration Storage	1.2.840.10008.5.1.4.1.1.66.3	Deformable Spatial Registration
//        Segmentation Storage	1.2.840.10008.5.1.4.1.1.66.4	Segmentation
//        Surface Segmentation Storage	1.2.840.10008.5.1.4.1.1.66.5	Surface Segmentation
//        Real World Value Mapping Storage	1.2.840.10008.5.1.4.1.1.67	Real World Value Mapping
//        VL Endoscopic Image Storage	1.2.840.10008.5.1.4.1.1.77.1.1	VL Endoscopic Image
//        VL Slide-Coordinates Microscopic Image Storage	1.2.840.10008.5.1.4.1.1.77.1.3	VL Slide-Coordinates Microscopic Image
//        VL Photographic Image Storage	1.2.840.10008.5.1.4.1.1.77.1.4	VL Photographic Image

//        Stereometric Relationship Storage	1.2.840.10008.5.1.4.1.1.77.1.5.3	Stereometric Relationship
//        VL Whole Slide Microscopy Image Storage	1.2.840.10008.5.1.4.1.1.77.1.6	VL Whole Slide Microscopy Image
//        Lensometry Measurements Storage	1.2.840.10008.5.1.4.1.1.78.1	Lensometry Measurements
//        Autorefraction Measurements Storage	1.2.840.10008.5.1.4.1.1.78.2	Autorefraction Measurements
//        Keratometry Measurements Storage	1.2.840.10008.5.1.4.1.1.78.3	Keratometry Measurements
//        Subjective Refraction Measurements Storage	1.2.840.10008.5.1.4.1.1.78.4	Subjective Refraction Measurements
//        Visual Acuity Measurements Storage	1.2.840.10008.5.1.4.1.1.78.5	Visual Acuity Measurements
//        Spectacle Prescription Report Storage	1.2.840.10008.5.1.4.1.1.78.6	Spectacle Prescription Report
//        Ophthalmic Axial Measurements Storage	1.2.840.10008.5.1.4.1.1.78.7	Ophthalmic Axial Measurements
//        Intraocular Lens Calculations Storage	1.2.840.10008.5.1.4.1.1.78.8	Intraocular Lens Calculations
//        Macular Grid Thickness and Volume Report	1.2.840.10008.5.1.4.1.1.79.1	Macular Grid Thickness and Volume Report
//        Ophthalmic Visual Field Static Perimetry Measurements Storage	1.2.840.10008.5.1.4.1.1.80.1	Ophthalmic Visual Field Static Perimetry Measurements

//        Procedure Log	1.2.840.10008.5.1.4.1.1.88.40	Procedure Log
//        Mammography CAD SR	1.2.840.10008.5.1.4.1.1.88.50	Mammography CAD SR IOD
//        Key Object Selection	1.2.840.10008.5.1.4.1.1.88.59	Key Object Selection Document
//        Chest CAD SR	1.2.840.10008.5.1.4.1.1.88.65	Chest CAD SR IOD
//        X-Ray Radiation Dose SR	1.2.840.10008.5.1.4.1.1.88.67	X-Ray Radiation Dose SR
//        Colon CAD SR	1.2.840.10008.5.1.4.1.1.88.69	Colon CAD SR IOD
//        Implantation Plan SR Document Storage	1.2.840.10008.5.1.4.1.1.88.70	Implantation Plan SR Document

//        Encapsulated PDF Storage	1.2.840.10008.5.1.4.1.1.104.1	Encapsulated PDF IOD
//        Encapsulated CDA Storage	1.2.840.10008.5.1.4.1.1.104.2	Encapsulated CDA IOD
//        Basic Structured Display Storage	1.2.840.10008.5.1.4.1.1.131	Basic Structured Display IOD

//        RT Structure Set Storage	1.2.840.10008.5.1.4.1.1.481.3
//        RT Beams Treatment Record Storage	1.2.840.10008.5.1.4.1.1.481.4
//        RT Plan Storage	1.2.840.10008.5.1.4.1.1.481.5
//        RT Brachy Treatment Record Storage	1.2.840.10008.5.1.4.1.1.481.6
//        RT Treatment Summary Record Storage	1.2.840.10008.5.1.4.1.1.481.7
//        RT Ion Plan Storage	1.2.840.10008.5.1.4.1.1.481.8	IOD defined in PS 3.3
//        RT Ion Beams Treatment Record Storage	1.2.840.10008.5.1.4.1.1.481.9	IOD defined in PS 3.3
//        RT Beams Delivery Instruction Storage	1.2.840.10008.5.1.4.34.7	RT Beams Delivery Instruction

//        Generic Implant Template Storage	1.2.840.10008.5.1.4.43.1	Generic Implant Template
//        Implant Assembly Template Storage	1.2.840.10008.5.1.4.44.1	Implant Assembly Template
//        Implant Template Group Storage	1.2.840.10008.5.1.4.45.1	Implant Template Group


        private final XnatScanType xnatScanType;
        private final String sopClassUid;

        String getSopClassUid() {
            return sopClassUid;
        }

        XnatScanType getXnatScanType() {
            return xnatScanType;
        }

        private DicomSopClass(final String sopClassUid, final XnatScanType xnatScanType) {
            this.sopClassUid = sopClassUid;
            this.xnatScanType = xnatScanType;
        }

        static DicomSopClass getModalityFromDicomTag(final String sopClassUid) {
            for (DicomSopClass sopClass : EnumSet.allOf(DicomSopClass.class)) {
                if (sopClassUid.equals(sopClass.getSopClassUid())) {
                    return sopClass;
                }
            }
            return Unknown;
        }

    }

}
