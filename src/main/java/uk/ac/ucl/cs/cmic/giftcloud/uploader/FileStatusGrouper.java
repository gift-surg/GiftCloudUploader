package uk.ac.ucl.cs.cmic.giftcloud.uploader;

import uk.ac.ucl.cs.cmic.giftcloud.util.ListMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  A class for grouping files into collections, and maintaining them in a fixed order which is indexable. Files are represented by unique identifiers
 */
public class FileStatusGrouper {
    private final List<Integer> groupsChanged = new ArrayList<Integer>();
    private final int maxGroups;
    private boolean anyGroupsAddedOrRemoved = false;
    private final ListMap<String, FileStatusGroup> groupStatusMap = new ListMap<String, FileStatusGroup>();
    private static int DEFAUT_MAX_GROUPS = 1000;

    public FileStatusGrouper() {
        this(DEFAUT_MAX_GROUPS);
    }

    public FileStatusGrouper(final int maxGroups) {
        this.maxGroups = maxGroups;
    }

    /**
     * Reset the flags indicating which groups have been modified. Call this before adding or signalling done on a group of files, in order to determine afterwards which groups have changed
     */
    public void resetChanged() {
        groupsChanged.clear();
        anyGroupsAddedOrRemoved = false;
    }

    /**
     * Add a new group of files, or add files to the existing group if it already exists
     * @param groupId the unique group identifier
     * @param description the user-visible description of this group
     * @param fileUids a list of unique file identifiers
     */
    public void addFiles(final String groupId, final String date, final String modality, final String description, final List<String> fileUids) {
        if (groupStatusMap.containsKey(groupId)) {
            groupsChanged.add(groupStatusMap.getIndex(groupId));
            groupStatusMap.getFromKey(groupId).add(fileUids);
        } else {
            anyGroupsAddedOrRemoved = true;
            if (groupStatusMap.size() >= maxGroups) {
                groupStatusMap.removeFromIndex(0);
            }
            groupStatusMap.put(groupId, new FileStatusGroup(date, modality, description, fileUids));
        }
    }

    /**
     * Indicate that a given file has completed processing
     * @param groupId the unique identifier for the group containing this file
     * @param fileUid the unique file identifier
     */
    public void fileDone(final String groupId, final String fileUid) {
        if (groupStatusMap.containsKey(groupId)) {
            groupsChanged.add(groupStatusMap.getIndex(groupId));
            groupStatusMap.getFromKey(groupId).done(fileUid);
        }
    }

    /**
     * @return the number of file groups
     */
    public int numGroups() {
        return groupStatusMap.size();
    }

    /**
     * Returns the description for the specified group
     *
     * @param groupIndex zero-based group index
     * @return the description for the given group
     */
    public String getDescription(final int groupIndex) {
        return groupStatusMap.getFromIndex(groupIndex).getDescription();
    }

    /**
     * @return a string representing the total and files uploaded in a given group
     * @param groupIndex zero-based group index
     * @return a string (x/y) for the specified group, where x is the number of files uploaded and y is the total number of files
     */
    public String getFileNumbers(final int groupIndex) {
        return groupStatusMap.getFromIndex(groupIndex).getFileNumbers();
    }

    /**
     * @return a string representing the uploading status of a given group
     * @param groupIndex zero-based group index
     * @return a string e.g. "Done", "Uploading", "Waiting"
     */
    public String getStatus(final int groupIndex) {
        return groupStatusMap.getFromIndex(groupIndex).getStatus();
    }

    /**
     * @return the highest group index for groups that have changed since the last call to {@link #resetChanged}
     */
    public int getMaxGroupChanged() {
        if (groupsChanged.size() > 0) {
            return Collections.max(groupsChanged);
        } else {
            return -1;
        }
    }

    /**
     * @return the lowest group index for groups that have changed since the last call to {@link #resetChanged}
     */
    public int getMinGroupChanged() {
        if (groupsChanged.size() > 0) {
            return Collections.min(groupsChanged);
        } else {
            return -1;
        }
    }

    /**
     * @return true if any groups have been added or removed since the last call to {@link #resetChanged}
     */
    public boolean getAnyGroupsAddedOrRemoved() {
        return anyGroupsAddedOrRemoved;
    }

    /**
     * Returns a String representing the date added for the specified group
     *
     * @param groupIndex zero-based group index
     * @return the description for the given group
     */
    public String getDate(final int groupIndex) {
        return groupStatusMap.getFromIndex(groupIndex).getDate();
    }

    /**
     * Returns a String representing modality for the specified group
     *
     * @param groupIndex zero-based group index
     * @return the modality description for the given group
     */
    public Object getModality(final int groupIndex) {
        return groupStatusMap.getFromIndex(groupIndex).getModality();
    }
}