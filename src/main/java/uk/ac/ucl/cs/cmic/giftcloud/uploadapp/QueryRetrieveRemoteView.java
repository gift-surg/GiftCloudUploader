package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.query.QueryInformationModel;
import com.pixelmed.query.QueryTreeBrowser;
import com.pixelmed.query.QueryTreeModel;
import com.pixelmed.query.QueryTreeRecord;
import uk.ac.ucl.cs.cmic.giftcloud.uploader.GiftCloudUncheckedException;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel showing the results of a DICOM query operation
 */
public class QueryRetrieveRemoteView extends JPanel {

    private List<QuerySelection> currentRemoteQuerySelectionList;
    private QueryRetrieveDialog callback;

    QueryRetrieveRemoteView(final QueryRetrieveDialog callback) {
        this.callback = callback;
        setLayout(new GridLayout(1, 1));
    }

    public void updateQueryPanel(final QueryInformationModel queryInformationModel, final AttributeList filter, final QueryInformationModel currentRemoteQueryInformationModel) throws IOException, DicomException, DicomNetworkException {
        try {
            QueryTreeModel treeModel = queryInformationModel.performHierarchicalQuery(filter);
            new OurQueryTreeBrowser(queryInformationModel, treeModel, this, currentRemoteQueryInformationModel);
        } catch (GiftCloudUncheckedException e) {
            throw e.getWrappedException();
        }

        // TD: unsure if this is required or not... for re-laying out the panel after a query operation has succeeded
        validate();
        callback.pack();
    }

    public List<QuerySelection> getCurrentRemoteQuerySelectionList() {
        return currentRemoteQuerySelectionList;
    }

    private void updateListStatus(final boolean nonEmpty) {
        callback.updateListStatus(nonEmpty);
    }

    private class OurQueryTreeBrowser extends QueryTreeBrowser {
        private QueryInformationModel currentRemoteQueryInformationModel;

        OurQueryTreeBrowser(QueryInformationModel q,QueryTreeModel m,Container content, final QueryInformationModel currentRemoteQueryInformationModel) throws DicomException {
            super(q,m,content);
            this.currentRemoteQueryInformationModel = currentRemoteQueryInformationModel;
        }

        protected TreeSelectionListener buildTreeSelectionListenerToDoSomethingWithSelectedLevel() {
            return new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent tse) {

                    // Store all the selected paths
                    QueryTreeRecord[] records = getSelectionPaths();
                    java.util.List<QuerySelection> remoteQuerySelectionList = new ArrayList<QuerySelection>();
                    if (records != null) {
                        for (QueryTreeRecord record : records) {
                            remoteQuerySelectionList.add(new QuerySelection(record, currentRemoteQueryInformationModel));
                        }
                    }
                    currentRemoteQuerySelectionList = remoteQuerySelectionList;
                    updateListStatus(!remoteQuerySelectionList.isEmpty());
                }
            };
        }
    }

}
