package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomException;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.query.QueryInformationModel;
import com.pixelmed.query.QueryTreeBrowser;
import com.pixelmed.query.QueryTreeModel;
import com.pixelmed.query.QueryTreeRecord;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * A panel showing the results of a DICOM query operation
 */
public class QueryRetrievePanel extends JPanel {

    private List<QuerySelection> currentRemoteQuerySelectionList;

    QueryRetrievePanel() {
        setLayout(new GridLayout(1, 1));
    }

    public void updateQueryPanel(final QueryInformationModel queryInformationModel, final AttributeList filter, final QueryInformationModel currentRemoteQueryInformationModel) throws DicomNetworkException, DicomException, IOException {
        QueryTreeModel treeModel = queryInformationModel.performHierarchicalQuery(filter);
        new OurQueryTreeBrowser(queryInformationModel, treeModel, this, currentRemoteQueryInformationModel);

        // TD: unsure if this is required or not... for re-laying out the panel after a query operation has succeeded
        validate();
    }

    public List<QuerySelection> getCurrentRemoteQuerySelectionList() {
        return currentRemoteQuerySelectionList;
    }

    private class OurQueryTreeBrowser extends QueryTreeBrowser {
        private QueryInformationModel currentRemoteQueryInformationModel;

        /**
         * @param	q
         * @param	m
         * @param	content
         * @throws DicomException
         */
        OurQueryTreeBrowser(QueryInformationModel q,QueryTreeModel m,Container content, final QueryInformationModel currentRemoteQueryInformationModel) throws DicomException {
            super(q,m,content);
            this.currentRemoteQueryInformationModel = currentRemoteQueryInformationModel;
        }

        /***/
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
                }
            };
        }
    }

}
