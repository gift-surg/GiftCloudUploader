package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import junit.framework.Assert;
import org.junit.Test;
import uk.ac.ucl.cs.cmic.giftcloud.restserver.GiftCloudLabel;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class NameGeneratorTest {

    @Test
    public void testSubjectNameGenerator() throws Exception {
        final NameGenerator.SubjectNameGenerator subjectNameGenerator = new NameGenerator.SubjectNameGenerator(Optional.of("AutoUploadSubject"));
        final GiftCloudLabel.LabelFactory<GiftCloudLabel.SubjectLabel> subjectLabelFactory = GiftCloudLabel.SubjectLabel.getFactory();
        commonNameGeneratorTests(subjectNameGenerator, subjectLabelFactory, "AutoUploadSubject");

        final NameGenerator.ExperimentNameGenerator experimentNameGenerator1 = subjectNameGenerator.getExperimentNameGenerator(subjectLabelFactory.create("Subject1"));
        final NameGenerator.ExperimentNameGenerator experimentNameGenerator2 = subjectNameGenerator.getExperimentNameGenerator(subjectLabelFactory.create("Subject2"));

        commonNameGeneratorTests(experimentNameGenerator1, GiftCloudLabel.ScanLabel.getFactory(), "Study");
        commonNameGeneratorTests(experimentNameGenerator2, GiftCloudLabel.ScanLabel.getFactory(), "Study");

        GiftCloudLabel label1 = subjectLabelFactory.create("Study1");
        GiftCloudLabel label2 = subjectLabelFactory.create("Study1");

        // Check that the same experiment id returns the same name generator
        final NameGenerator.ExperimentNameGenerator experimentNameGenerator1again = subjectNameGenerator.getExperimentNameGenerator(subjectLabelFactory.create("Subject1"));
        Assert.assertSame(experimentNameGenerator1again, experimentNameGenerator1);
        final Set<String> knownNames = new HashSet<String>();
        Assert.assertEquals(experimentNameGenerator1again.getNewName(knownNames), GiftCloudLabel.ExperimentLabel.getFactory().create("Study9"));


        final String newSubjectPrefix = "SubjectTest";

        // Updating with a different prefix will change the prefix and reset the index number
        subjectNameGenerator.updateSubjectNamePrefix(Optional.of(newSubjectPrefix));
        Assert.assertEquals(subjectNameGenerator.getNewName(knownNames), subjectLabelFactory.create(newSubjectPrefix + "1"));
        Assert.assertEquals(subjectNameGenerator.getNewName(knownNames), subjectLabelFactory.create(newSubjectPrefix + "2"));

        // Updating with the same prefix will not update the index number
        subjectNameGenerator.updateNamePrefix(newSubjectPrefix, 1);
        Assert.assertEquals(subjectNameGenerator.getNewName(knownNames), subjectLabelFactory.create(newSubjectPrefix + "3"));
    }
    @Test
    public void testExperimentNameGenerator() throws Exception {
        final NameGenerator.ExperimentNameGenerator experimentNameGenerator = new NameGenerator.ExperimentNameGenerator();
        final GiftCloudLabel.LabelFactory<GiftCloudLabel.ExperimentLabel> experimentLabelFactory = GiftCloudLabel.ExperimentLabel.getFactory();
        commonNameGeneratorTests(experimentNameGenerator, experimentLabelFactory, "Study");

        final NameGenerator.ScanNameGenerator scanNameGenerator1 = experimentNameGenerator.getScanNameGenerator(experimentLabelFactory.create("Study1"));
        final NameGenerator.ScanNameGenerator scanNameGenerator2 = experimentNameGenerator.getScanNameGenerator(experimentLabelFactory.create("Study2"));

        commonNameGeneratorTests(scanNameGenerator1, GiftCloudLabel.ScanLabel.getFactory(), "Series");
        commonNameGeneratorTests(scanNameGenerator2, GiftCloudLabel.ScanLabel.getFactory(), "Series");

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
        commonNameGeneratorTests(scanNameGenerator, GiftCloudLabel.ScanLabel.getFactory(), "Series");
    }

    /**
     * General tests for name generation
     */
    private void commonNameGeneratorTests(final NameGenerator nameGenerator, final GiftCloudLabel.LabelFactory labelFactory, final String prefix) {
        final Set<String> knownNames = new HashSet<String>();
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + "1"));
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + "2"));
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + "3"));

        knownNames.add(prefix + "4");
        knownNames.add(prefix + "6");

        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + "5"));
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + "7"));
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + "8"));

        final String newPrefix = "TestPrefix";
        // Updating with a different prefix will change the prefix and reset the index number
        nameGenerator.updateNamePrefix(newPrefix, 1);
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(newPrefix + "1"));
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(newPrefix + "2"));

        // Updating with the same prefix will not update the index number
        nameGenerator.updateNamePrefix(newPrefix, 1);
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(newPrefix + "3"));

        // Now switch to the original prefix and check it is set according to our supplied index
        nameGenerator.updateNamePrefix(prefix, 8);
        Assert.assertEquals(nameGenerator.getNewName(knownNames), labelFactory.create(prefix + "8"));
    }
}