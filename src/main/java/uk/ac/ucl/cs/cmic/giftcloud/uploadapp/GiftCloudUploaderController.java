package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public interface GiftCloudUploaderController {
    void showConfigureDialog() throws IOException, DicomNode.DicomNodeStartException;
    void showAboutDialog();

    void hide();

    void show();

    void upload(Vector<String> filePaths);

    void retrieve(List<QuerySelection> currentRemoteQuerySelectionList);

    void query(QueryParams queryParams);

    void export(String exportDirectory, Vector<String> filesToExport);

    void runImport(String filePath, JProgressBar progressBar);
}
