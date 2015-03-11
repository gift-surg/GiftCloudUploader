/* Copyright (c) 2001-2014, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package com.pixelmed.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.swing.tree.TreeNode;
import java.io.IOException;

import com.pixelmed.dicom.Attribute;
import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.AttributeTag;
import com.pixelmed.dicom.InformationEntity;
import com.pixelmed.dicom.DicomException;

import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.network.IdentifierHandler;

import com.pixelmed.utils.StringUtilities;

/**
 * <p>Instances of the {@link com.pixelmed.query.QueryTreeRecord QueryTreeRecord} class represent
 * nodes in a tree of the {@link com.pixelmed.query.QueryTreeModel QueryTreeModel} class, which in
 * turn is used by the {@link com.pixelmed.query.QueryTreeBrowser QueryTreeBrowser} class.</p>
 *
 * <p>This class is publically visible primarily so that selection change listeners can be
 * constructed for {@link com.pixelmed.query.QueryTreeBrowser QueryTreeBrowser}, since
 * the user's selection is returned as a path of {@link com.pixelmed.query.QueryTreeRecord QueryTreeRecord}
 * instances, which need to be cast accordingly.</p>
 *
 * @author	dclunie
 */
public class QueryTreeRecord implements Comparable, TreeNode {

	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/query/QueryTreeRecord.java,v 1.13 2014/09/09 20:34:09 dclunie Exp $";

	protected QueryInformationModel q;
	protected AttributeList filter;
	protected int debugLevel;

	protected QueryTreeRecord parent;
	protected List children;
	protected InformationEntity ie;
	protected Attribute uniqueKey;					// the unique key for this level
	protected AttributeList uniqueKeys;				// the unique key for parents AND this level
	protected AttributeList allAttributesReturnedInIdentifier;
	protected String value;

	protected boolean childrenPopulated;
	protected int numberOfChildren;				// -1 is a flag that the count is unknown at node creation, and a subsidiary query has to be performed

	/**
	 * <p>Dump the string value of the node.</p>
	 *
	 * @return	the string value of this node
	 */
	public String toString() {
		return value == null ? "" : value;
	}
	
	// Methods to implement Comparable (allows parent to sort)

	/**
	 * <p>Compare nodes based on the lexicographic order of their string values.</p>
	 *
	 * <p>Note that the comparison is more complex than a simple lexicographic comparison
	 * of strings (as described in the definition of {@link java.lang.String#compareTo(String) java.lang.String.compareTo(String)}
	 * but rather accounts for embedded non-zero padded integers. See {@link com.pixelmed.utils.StringUtilities#compareStringsWithEmbeddedNonZeroPaddedIntegers(String,String) com.pixelmed.utils.compareStringsWithEmbeddedNonZeroPaddedIntegers(String,String)}
	 * </p>
	 *
	 * <p>If the string values are equal but they do not have the same unique key, then an arbitrary but consistent order is return.</p>
	 *
	 * @param	o	the {@link com.pixelmed.query.QueryTreeRecord QueryTreeRecord}
	 *			to compare this {@link com.pixelmed.query.QueryTreeRecord QueryTreeRecord} against
	 * @return		the value 0 if the argument is equal to this object; a value less than 0 if this object
	 *			is lexicographically less than the argument; and a value greater than 0 if this object
	 *			is lexicographically greater than the argument
	 */
	public int compareTo(Object o) {
		QueryTreeRecord otherRecord = (QueryTreeRecord)o;
		Attribute otherUniqueKey = otherRecord.getUniqueKey();
		String otherUniqueKeyValue = otherUniqueKey == null ? null : otherUniqueKey.getSingleStringValueOrNull();
		String uniqueKeyValue = getUniqueKey() == null ? null : getUniqueKey().getSingleStringValueOrNull();
//System.err.println("QueryTreeRecord.compareTo(): our   unique key = <"+uniqueKeyValue+"> for <"+getValue()+">");
//System.err.println("QueryTreeRecord.compareTo(): other unique key = <"+otherUniqueKeyValue+"> for <"+otherRecord.getValue()+">");
		if (uniqueKeyValue == null && otherUniqueKeyValue == null) {
			return 0;	// only occurs for top level node
		}
		int uniqueKeyComparison = uniqueKeyValue.compareTo(otherUniqueKeyValue);
		//if (uniqueKeyComparison == 0) {
		//	return 0;				// always equal if same unique key
		//}
		//else {
		{
			int strComparison = StringUtilities.compareStringsWithEmbeddedNonZeroPaddedIntegers(getValue(),otherRecord.getValue());
			if (strComparison == 0) {
				return uniqueKeyComparison;	// same string but different primary key; distinguish and order consistently
			}
			else {
				return strComparison;
			}
		}
	}

	/**
	 * @param	o
	 */
	public boolean equals(Object o) {
		return compareTo(o) == 0;
	}

	// Methods to implement TreeNode ...

	/**
	 * <p>Returns the parent node of this node.</p>
	 *
	 * @return	the parent node, or null if the root
	 */
	public TreeNode getParent() {
		return parent;
	}

	/**
	 * <p>Returns the child at the specified index.</p>
	 *
	 * @param	index	the index of the child to be returned, numbered from 0
	 * @return		the child <code>TreeNode</code> at the specified index
	 */
	public TreeNode getChildAt(int index) {
if (debugLevel > 1) System.err.println("QueryTreeRecord.getChildAt(): "+index);
		populateChildrenIfRequired();
if (debugLevel > 1) System.err.println("QueryTreeRecord.getChildAt(): back from populateChildrenIfRequired(), childrenPopulated="+childrenPopulated+", numberOfChildren="+numberOfChildren);
		TreeNode node = index < getChildCount() ? (TreeNode)(children.get(index)) : null;
		if (node == null) {
			 System.err.println("QueryTreeRecord.getChildAt(): there is no such child as "+index+", probably because query failed to return any children at "+getQueryLevelToPopulateChildren()+" level - javax.swing.tree.TreePath.pathByAddingChild will now throw a NullPointerException");
		}
		return node;
	}

	/**
	 * <p>Returns the index of the specified child from amongst this node's children, if present.</p>
	 *
	 * @param	child	the child to search for amongst this node's children
	 * @return		the index of the child, or -1 if not present
	 */
	public int getIndex(TreeNode child) {
if (debugLevel > 1) System.err.println("getIndexOfChild: looking for "+child);
		populateChildrenIfRequired();
		int n=getChildCount();
		for (int i=0; i<n; ++i) {
			if (children.get(i).equals(child)) {	// expensive comparison ? :(
if (debugLevel > 1) System.err.println("getIndexOfChild: found "+child);
				return i;
			}
		}
		return -1;
	}

	/**
	 * <p> Always returns true, since children may always be added.</p>
	 *
	 * @return	always true
	 */
	public boolean getAllowsChildren() {
		return true;
	}

	/**
	 * <p> Returns true if the receiver is a leaf (has no children).</p>
	 *
	 * @return	true if the receiver is a leaf
	 */
	public boolean isLeaf() {
if (debugLevel > 1) System.err.println("QueryTreeRecord.populateChildrenIfRequired(): isLeaf()");
		int count = getChildCount();	// don't call more than once, since has side effects
		boolean leaf = count == 0;
if (debugLevel > 1) System.err.println("QueryTreeRecord.populateChildrenIfRequired(): isLeaf() returns "+leaf+" because getChildCount() is "+count);
		return getChildCount() == 0;
	}
	
	protected void populateChildrenIfRequired() {
if (debugLevel > 1) System.err.println("QueryTreeRecord.populateChildrenIfRequired(): childrenPopulated="+childrenPopulated);
		if (!childrenPopulated) {
			populateChildren();
			childrenPopulated = true;
			numberOfChildren = children == null ? 0 : children.size();
		}
	}

	/**
	 * <p>Return the number of children that this node contains.</p>
	 *
	 * @return	the number of children, 0 if none
	 */
	public int getChildCount() {
if (debugLevel > 1) System.err.println("QueryTreeRecord.getChildCount(): when called numberOfChildren="+numberOfChildren);
		if (numberOfChildren == -1) {
			populateChildrenIfRequired();
if (debugLevel > 1) System.err.println("QueryTreeRecord.getChildCount(): after populateChildrenIfRequired() numberOfChildren="+numberOfChildren);
		}
		return numberOfChildren;
	}

	/**
	 * <p>Returns the children of this node as an {@link java.util.Enumeration Enumeration}.</p>
	 *
	 * @return	the children of this node
	 */
	public Enumeration children() {
if (debugLevel > 1) System.err.println("QueryTreeRecord.children(): "+ (children == null ? "null" : "not null"));
		populateChildrenIfRequired();
		return children == null ? null : new Vector(children).elements();
	}

	// Methods specific to this kind of node ...

	/***/
	protected class OurResponseIdentifierHandler extends IdentifierHandler {
		protected InformationEntity ie;
		protected AttributeTag uniqueKeyTagFromThisLevel;
		protected QueryTreeRecord parentNode;
		
		/**
		 * @param	parentNode
		 * @param	ie
		 * @param	uniqueKeyTagFromThisLevel
		 */
		public OurResponseIdentifierHandler(QueryTreeRecord parentNode,InformationEntity ie,AttributeTag uniqueKeyTagFromThisLevel) {
			this.parentNode=parentNode;
			this.ie=ie;
			this.uniqueKeyTagFromThisLevel=uniqueKeyTagFromThisLevel;
		}
		
		/**
		 * @param	responseIdentifier
		 */
		public void doSomethingWithIdentifier(AttributeList responseIdentifier) throws DicomException {
if (debugLevel > 0) System.err.println("QueryTreeRecord.populateChildren.doSomethingWithIdentifier():");
if (debugLevel > 0) System.err.print(responseIdentifier);
			String value = q.getStringValueForTreeFromResponseIdentifier(ie,responseIdentifier);
			Attribute uniqueKey = responseIdentifier.get(uniqueKeyTagFromThisLevel);
			if (uniqueKey == null || uniqueKey.getVL() == 0) {
				throw new DicomException("Invalid query response for "+ie+" without unique key value in "+uniqueKeyTagFromThisLevel+" from "+q);
			}
			else {
				QueryTreeRecord node = new QueryTreeRecord(q,filter,parentNode,value,ie,uniqueKey,responseIdentifier,debugLevel);
				addChild(node);
			}
		}
	}
	
	protected InformationEntity getQueryLevelToPopulateChildren() {
		return ie == null ? q.getRoot() : q.getChildTypeForParent(ie);
	}
	
	protected void populateChildren() {
if (debugLevel > 0) System.err.println("QueryTreeRecord.populateChildren() for "+this);
		InformationEntity queryLevel = getQueryLevelToPopulateChildren();
		if (queryLevel != null) {
if (debugLevel > 0) System.err.println("QueryTreeRecord.populateChildren(): queryLevel="+queryLevel);
			AttributeTag uniqueKeyTagFromThisLevel = q.getUniqueKeyForInformationEntity(queryLevel);
if (debugLevel > 0) System.err.println("QueryTreeRecord.populateChildren(): uniqueKeyTagFromThisLevel="+uniqueKeyTagFromThisLevel);
			OurResponseIdentifierHandler ourResponseIdentifierHandler = new OurResponseIdentifierHandler(this,queryLevel,uniqueKeyTagFromThisLevel);
			try {
				q.performQuery(filter,uniqueKeys,queryLevel,ourResponseIdentifierHandler);
			}
			catch (IOException e) {
				e.printStackTrace(System.err);
			}
			catch (DicomException e) {
				e.printStackTrace(System.err);
			}
			catch (DicomNetworkException e) {
				e.printStackTrace(System.err);
			}
		}
	}

	/**
	 * <p>Make a new node in a tree.</p>
	 *
	 * @param	q					the query information model to build the tree from
	 * @param	filter					the query request identifier as a list of DICOM attributes
	 * @param	parent					the parent of this node
	 * @param	value					a string value which is used primarily to sort siblings into lexicographic order
	 * @param	ie					the entity in the DICOM information model that the constructed node is an instance of
	 * @param	uniqueKey				the DICOM attribute which is the unique key at the level of this record
	 * @param	allAttributesReturnedInIdentifier	a list of all the DICOM attributes from the query response for this level of a query
	 * @param	debugLevel				0 is no debugging (silent), &gt; 0 more verbose levels of debugging
	 */
	public QueryTreeRecord(QueryInformationModel q,AttributeList filter,QueryTreeRecord parent,String value,InformationEntity ie,
			Attribute uniqueKey,AttributeList allAttributesReturnedInIdentifier,int debugLevel) {
		this.q=q;
		this.filter=filter;
		this.parent=parent;
		this.value=value;
		this.ie=ie;
		this.uniqueKey=uniqueKey;
		this.allAttributesReturnedInIdentifier=allAttributesReturnedInIdentifier;
		this.debugLevel=debugLevel;
		childrenPopulated = false;
		{
			numberOfChildren = -1;
			AttributeTag t = q.getAttributeTagOfCountOfChildren(ie);
			if (allAttributesReturnedInIdentifier != null && t != null && allAttributesReturnedInIdentifier.containsKey(t)) {
				numberOfChildren = Attribute.getSingleIntegerValueOrDefault(allAttributesReturnedInIdentifier,t,-1);
//System.err.println("QueryTreeRecord(): "+this+" numberOfChildren = "+numberOfChildren);
			}
		}
		{
			uniqueKeys = new AttributeList();
			QueryTreeRecord testParent = parent;
			while (testParent != null) {
				Attribute parentUniqueKey = testParent.getUniqueKey();
				if (parentUniqueKey != null) {
					uniqueKeys.put(parentUniqueKey.getTag(),parentUniqueKey);
				}
				testParent = (QueryTreeRecord)(testParent.getParent());
			}
			if (uniqueKey != null) {
				uniqueKeys.put(uniqueKey.getTag(),uniqueKey);
			}
		}
//System.err.println("QueryTreeRecord(): created="+this);
//System.err.println("QueryTreeRecord(): uniqueKey="+uniqueKey);
//System.err.println("QueryTreeRecord(): uniqueKeys="+uniqueKeys);
	}

	/**
	 * <p>Add a child to this nodes sorted collection of children.</p>
	 *
	 * @param	child	the child node to be added
	 */
	public void addChild(QueryTreeRecord child) {
if (debugLevel > 1) System.err.println("QueryTreeRecord.addChild(): child="+child);
		if (children == null) {
			children=new ArrayList();
		}
		// Next is from "http://javaalmanac.com/egs/java.util/coll_InsertInList.html?l=rel"
		// and is way faster than children.add(child) followed by Collections.sort(children)
		int index = Collections.binarySearch(children,child);
		if (index < 0) {
			children.add(-index-1,child);
		}
	}

	/**
	 * <p>Add a sibling to this node,
	 * that is add a child to this
	 * node's parent's sorted collection of children.</p>
	 *
	 * @param	sibling		the sibling node to be added
	 * @throws	DicomException	thrown if this node has no parent
	 */
	public void addSibling(QueryTreeRecord sibling) throws DicomException {
if (debugLevel > 1) System.err.println("QueryTreeRecord.addSibling(): sibling="+sibling);
		if (parent == null) {
			throw new DicomException("Internal error - root node with sibling");
		}
		else {
			parent.addChild(sibling);
		}
	}

	/**
	 * <p>Get the string value of the node which is used for sorting and human-readable rendering.</p>
	 *
	 * @return	the string value of this node
	 */
	public String getValue() { return value; }
	
	/**
	 * <p>Get the information entity that this node represents.</p>
	 *
	 * @return	information entity that this node represents
	 */
	public InformationEntity getInformationEntity() { return ie; }
	
	/**
	 * <p>Get the list of DICOM attributes, one for each unique key of each parent of this level as well as this level itself.</p>
	 *
	 * @return	the list of unique keys
	 */
	public AttributeList getUniqueKeys() { return uniqueKeys; }
	
	/**
	 * <p>Get the DICOM attribute that is the unique key at the level of this record.</p>
	 *
	 * @return	the unique key
	 */
	public Attribute getUniqueKey() { return uniqueKey; }
	
	/**
	 * <p>Get the list of all the DICOM attributes from the query response for this level of the query.</p>
	 *
	 * @return	the list of all response attributes for this level
	 */
	public AttributeList getAllAttributesReturnedInIdentifier() { return allAttributesReturnedInIdentifier; }
}



