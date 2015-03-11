package uk.ac.ucl.cs.cmic.giftcloud.ecat;

import com.google.common.collect.Sets;
import org.nrg.ecat.MatrixDataFile;
import uk.ac.ucl.cs.cmic.giftcloud.dicom.FileCollection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class MatrixDataFileCollection implements FileCollection {

    private final Set<MatrixDataFile> files = Sets.newLinkedHashSet();

    public MatrixDataFileCollection(final Collection<MatrixDataFile> matrixDataFileCollection) {
        for (MatrixDataFile matrixDataFile : matrixDataFileCollection) {
            files.add(matrixDataFile);
        }
    }

    @Override
    public int getFileCount() {
        return files.size();
    }

    @Override
    public Collection<File> getFiles() {
        Collection<File> fileList = new ArrayList<File>();
        for (final MatrixDataFile matrixDataFile : files) {
            fileList.add(matrixDataFile.getFile());
        }
        return fileList;
    }

    @Override
    public long getSize() {
        long size = 0;
        for (final MatrixDataFile f : files) {
            size += f.getSize();
        }
        return size;
    }
}
