package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;
import uk.ac.ucl.cs.cmic.giftcloud.util.Optional;

import java.util.HashSet;
import java.util.Set;

public class NameGeneratorTest {

    @Test
    public void testSubjectNameGenerator() throws Exception {
        final NameGenerator.SubjectNameGenerator subjectNameGenerator = new NameGenerator.SubjectNameGenerator(Optional.of("AutoUploadSubject"));
        final GiftCloudLabel.LabelFactory<GiftCloudLabel.SubjectLabel> subjectLabelFactory = GiftCloudLabel.SubjectLabel.getFactory();
        commonNameGeneratorTests(subjectNameGenerator, subjectLabelFactory, "AutoUploadSubject", "0000");

        final NameGenerator.ExperimentNameGenerator experimentNameGenerator1 = subjectNameGenerator.getExperimentNameGenerator(subjectLabelFactory.create("Subject001"));
        final NameGenerator.ExperimentNameGenerator experimentNameGenerator2 = subjectNameGenerator.getExperimentNameGenerator(subjectLabelFactory.create("Subject002"));

        commonNameGeneratorTests(experimentNameGenerator1, GiftCloudLabel.ScanLabel.getFactory(), "Subject001-Study", "");
        commonNameGeneratorTests(experimentNameGenerator2, GiftCloudLabel.ScanLabel.getFactory(), "Subject002-Study", "");

        GiftCloudLabel label1 = subjectLabelFactory.create("Study1");
        GiftCloudLabel label2 = subjectLabelFactory.create("Study1");

        // Check that the same experiment id returns the same name generator
        final NameGenerator.ExperimentNameGenerator experimentNameGenerator1again = subjectNameGenerator.getExperimentNameGenerator(subjectLabelFactory.create("Subject001"));
        Assert.assertSame(experimentNameGenerator1again, experimentNameGenerator1);
        final Set<String> knownNames = new HashSet<String>();
        Assert.assertEquals(experimentNameGenerator1again.getNewName(knownNames), GiftCloudLabel.ExperimentLabel.getFactory().create("Subject001-Study9"));


        final String newSubjectPrefix = "SubjectTest";

        // Updating with a different prefix will change the prefix and reset the index number
        subjectNameGenerator.updateSubjectNamePrefix(Optional.of(newSubjectPrefix));
        Assert.assertEquals(subjectNameGenerator.getNewName(knownNames), subjectLabelFactory.create(newSubjectPrefix + "00001"));
        Assert.assertEquals(subjectNameGenerator.getNewName(knownNames), subjectLabelFactory.create(newSubjectPrefix + "00002"));

        // Updating with the same prefix will not update the index number
        subjectNameGenerator.updateNamePrefix(newSubjectPrefix, 1);
        Assert.assertEquals(subjectNameGenerator.getNewName(knownNames), subjectLabelFactory.create(newSubjectPrefix + "00003"));
    }
    @Test
    public void testExperimentNameGenerator() throws Exception {
        final NameGenerator.ExperimentNameGenerator experimentNameGenerator = new NameGenerator.ExperimentNameGenerator("Study");
        final GiftCloudLabel.LabelFactory<GiftCloudLabel.ExperimentLabel> experimentLabelFactory = GiftCloudLabel.ExperimentLabel.getFactory();
        commonNameGeneratorTests(experimentNameGenerator, experimentLabelFactory, "Study", "");

        final NameGenerator.ScanNameGenerator scanNameGenerator1 = experimentNameGenerator.getScanNameGenerator(experimentLabelFactory.create("Study1"));
        final NameGenerator.ScanNameGenerator scanNameGenerator2 = experimentNameGenerator.getScanNameGenerator(experimentLabelFactory.create("Study2"));

        commonNameGeneratorTests(scanNameGenerator1, GiftCloudLabel.ScanLabel.getFactory(), "Series", "");
        commonNameGeneratorTests(scanNameGenerator2, GiftCloudLabel.ScanLabel.getFactory(), "Series", "");

        GiftCloudLabel label1 = experimentLabelFactory.create("Study1");
        GiftCloudLabel label2 = experimentLabelFactory.create("Study1");

        // Check that the same experiment id returns the same name generator
        final NameGenerator.ScanNameGenerator scanNameGenerator1again = experimentNameGenerator.getScanNameGenerator(experimentLabelFactory.create("Study1"));
        Assert.assertSame(scanNameGenerator1again, scanNameGenerator1);
        final Set<String> knownNames = new HashSet<String>();
        Assert.assertEquals(scanNameGenerator1again.getNewName(knownNames), GiftCloudLabel.ScanLabel.getFactory().create("Series9"));
    }


    @Test
    public void testScanNameGenerator() throws Exception {
        final NameGenerator.ScanNameGenerator scanNameGenerator = new NameGenerator.ScanNameGenerator();
        commonNameGeneratorTests(scanNameGenerator, GiftCloudLabel.ScanLabel.getFactory(), "Series", "");
    }

    /**
     * General tests for name generation
     */
    private void commonNameGeneratorTests(final NameGenerator nameGenerator, final GiftCloudLabel.LabelFactory labelFactory, final String prefix, final String leadingZeros) {
        final Set<String> knownNames = new HashSet<String>();
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + leadingZeros + "1"));
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + leadingZeros + "2"));
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + leadingZeros + "3"));

        knownNames.add(prefix + "4");   // Legacy name format

        if (leadingZeros.equals("0000")) {
            knownNames.add(prefix + "00006"); // New name format
        } else {
            knownNames.add(prefix + "6"); // New name format
        }

        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + leadingZeros + "5"));
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + leadingZeros + "7"));
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + leadingZeros + "8"));

        if (leadingZeros.equals("0000")) {
            knownNames.add(prefix + "00010"); // New name format
        } else {
            knownNames.add(prefix + "10"); // New name format
        }

        knownNames.add(prefix + "12");  // Legacy name format

        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + leadingZeros + "9"));
        if (leadingZeros.equals("0000")) {
            Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + "00011"));
            Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + "00013"));
        } else {
            Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + "11"));
            Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + "13"));
        }

        final String newPrefix = "TestPrefix";
        // Updating with a different prefix will change the prefix and reset the index number
        nameGenerator.updateNamePrefix(newPrefix, 1);
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(newPrefix + leadingZeros + "1"));
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(newPrefix + leadingZeros + "2"));

        // Updating with the same prefix will not update the index number
        nameGenerator.updateNamePrefix(newPrefix, 1);
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(newPrefix + leadingZeros + "3"));

        // Now switch to the original prefix and check it is set according to our supplied index
        nameGenerator.updateNamePrefix(prefix, 8);
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + leadingZeros + "8"));
    }
}