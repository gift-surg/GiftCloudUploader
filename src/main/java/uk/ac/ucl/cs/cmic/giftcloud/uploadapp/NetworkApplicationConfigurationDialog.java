/* Copyright (c) 2001-2011, David A. Clunie DBA Pixelmed Publishing. All rights reserved. */

package uk.ac.ucl.cs.cmic.giftcloud.uploadapp;

import com.pixelmed.network.*;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;

/**
 * <p>This class provides user interface for network applications to configure and test properties related to DICOM network services.</p>
  *
 * @author	dclunie
 */
public class NetworkApplicationConfigurationDialog {

//	private static final String identString = "@(#) $Header: /userland/cvs/pixelmed/imgbook/com/pixelmed/network/NetworkApplicationConfigurationDialog.java,v 1.3 2011/02/21 17:27:35 dclunie Exp $";

	protected NetworkApplicationInformation networkApplicationInformation;
	protected GiftCloudPropertiesFromApplication networkApplicationProperties;
	
	protected JTextField calledAETitleField;
	protected JTextField callingAETitleField;
	protected JTextField listeningPortField;

	Component componentToCenterDialogOver;
    private GiftCloudDialogs giftCloudDialogs;
    JDialog dialog;

	protected String showInputDialogToSelectNetworkTargetByLocalApplicationEntityName(NetworkApplicationInformation infoFromProperties, String message, String title, String defaultSelection) {
		String ae = defaultSelection;
		if (infoFromProperties != null) {
			Set localNamesOfRemoteAEs = infoFromProperties.getListOfLocalNamesOfApplicationEntities();
			if (localNamesOfRemoteAEs != null) {
				String sta[] = new String[localNamesOfRemoteAEs.size()];
				int i=0;
				Iterator it = localNamesOfRemoteAEs.iterator();
				while (it.hasNext()) {
					sta[i++]=(String)(it.next());
				}
                ae = giftCloudDialogs.getSelection(message, title, sta, ae);
			}
		}
		return ae;
	}

    protected class ChooseRemoteAEActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            try {
                NetworkApplicationInformation infoFromProperties = networkApplicationProperties.getNetworkApplicationInformation();
                String ae = networkApplicationProperties.getCurrentlySelectedQueryTargetAE();
                ae = showInputDialogToSelectNetworkTargetByLocalApplicationEntityName(infoFromProperties, "Select remote system to query", "GIFT-Cloud", ae);
                networkApplicationProperties.setCurrentlySelectedQueryTargetAE(ae);
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }


	protected class AddRemoteAEActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			try {
				NetworkApplicationInformation infoFromProperties = networkApplicationProperties.getNetworkApplicationInformation();
				ApplicationEntityConfigurationDialog aed = new ApplicationEntityConfigurationDialog(componentToCenterDialogOver);
				String localName = aed.getLocalName();
				infoFromProperties.remove(localName);	// in case it already existed, in which case we replace it (sliently :()
				infoFromProperties.add(localName,aed);
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}
	
	protected class EditRemoteAEActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			try {
				NetworkApplicationInformation infoFromProperties = networkApplicationProperties.getNetworkApplicationInformation();
                String defaultAe = networkApplicationProperties.getCurrentlySelectedQueryTargetAE();
				String localName = showInputDialogToSelectNetworkTargetByLocalApplicationEntityName(infoFromProperties, "Select AE to edit", "GIFT-Cloud", defaultAe);
				if (localName != null && localName.length() > 0) {
					Set localNamesFromProperties = infoFromProperties.getListOfLocalNamesOfApplicationEntities();
					ApplicationEntityMap aesFromProperties = infoFromProperties.getApplicationEntityMap();
					String aeTitle = infoFromProperties.getApplicationEntityTitleFromLocalName(localName);
					if (aesFromProperties != null && aeTitle != null && aeTitle.length() > 0) {
						ApplicationEntity ae = (ApplicationEntity)(aesFromProperties.get(aeTitle));
						if (ae != null) {
							infoFromProperties.remove(localName);	// remove it BEFORE editing, since may have different local name on return
							ApplicationEntityConfigurationDialog aed = new ApplicationEntityConfigurationDialog(componentToCenterDialogOver,localName,ae);
							localName = aed.getLocalName();			// may have been edited
							infoFromProperties.add(localName,aed);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}
	
	protected class RemoveRemoteAEActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			try {
                String ae = networkApplicationProperties.getCurrentlySelectedQueryTargetAE();
				NetworkApplicationInformation infoFromProperties = networkApplicationProperties.getNetworkApplicationInformation();
				String localName = showInputDialogToSelectNetworkTargetByLocalApplicationEntityName(infoFromProperties, "Select AE to remove", "GIFT-Cloud", ae);
				if (localName != null && localName.length() > 0) {
					infoFromProperties.remove(localName);
				}

                // Ensure that if we have deleted the current selection, then it is no longer stored as the defauls
                if (StringUtils.isNotBlank(ae) && ae.equals(localName)) {
                    networkApplicationProperties.setCurrentlySelectedQueryTargetAE("");
                }
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
	}
	
	protected class DoneActionListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			boolean good = true;
			// check and update properties for our listener ...
			{
				String calledAETitle = calledAETitleField.getText();
				if (!ApplicationEntityConfigurationDialog.isValidAETitle(calledAETitle)) {
					good=false;
					calledAETitleField.setText("\\\\\\BAD\\\\\\");		// use backslash character here (which is illegal in AE's) to make sure this field is edited
				}
			}
			{
				String callingAETitle = callingAETitleField.getText();
				if (!ApplicationEntityConfigurationDialog.isValidAETitle(callingAETitle)) {
					good=false;
					callingAETitleField.setText("\\\\\\BAD\\\\\\");		// use backslash character here (which is illegal in AE's) to make sure this field is edited
				}
			}
			{
				int listeningPort=0;
				try {
					listeningPort = Integer.parseInt(listeningPortField.getText());
					if (listeningPort < 1024) {
						good=false;
						listeningPortField.setText("want >= 1024");
					}
				}
				catch (NumberFormatException e) {
					good=false;
					listeningPortField.setText("\\\\\\BAD\\\\\\");
				}
			}
			if (good) {
				networkApplicationProperties.setListeningPort(Integer.parseInt(listeningPortField.getText()));
				networkApplicationProperties.setCalledAETitle(calledAETitleField.getText());
				networkApplicationProperties.setCallingAETitle(callingAETitleField.getText());
				
				Cursor was = dialog.getCursor();
				dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					if (networkApplicationInformation instanceof NetworkApplicationInformationFederated) {
						((NetworkApplicationInformationFederated)networkApplicationInformation).removeAllSources();
						((NetworkApplicationInformationFederated)networkApplicationInformation).startupAllKnownSourcesAndRegister(networkApplicationProperties);
					}
					// else do nothing ... we have been modifying the NetworkApplicationInformation inside the existing NetworkApplicationProperties all along
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
				dialog.setCursor(was);
				dialog.dispose();
			}
			// else if not good, do not dispose ... make user fix the problems
		}
	}
	
	/**
	 * <p>Configure and test network information.</p>
	 *
	 * @param	parent							the parent component on which the new dialog is centered, may be null in which case centered on the screen
	 * @param	networkApplicationInformation	the current information whose contents are to be replaced with updated information
	 * @param	networkApplicationProperties	the static properties that are to be edited
	 */
	public NetworkApplicationConfigurationDialog(Component parent,NetworkApplicationInformation networkApplicationInformation, GiftCloudPropertiesFromApplication networkApplicationProperties, GiftCloudDialogs giftCloudDialogs) throws DicomNetworkException {
		this.networkApplicationInformation = networkApplicationInformation;
		this.networkApplicationProperties = networkApplicationProperties;
		
		componentToCenterDialogOver = parent;
        this.giftCloudDialogs = giftCloudDialogs;

        dialog = new JDialog();
		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(componentToCenterDialogOver);	// without this, appears at TLHC rather then center of parent or screen

		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		//dialog.addWindowListener(new WindowAdapter() {
		//	public void windowClosing(WindowEvent we) {
		//		System.err.println("Thwarted user attempt to close window.");
		//	}
		//});
		
		Border panelBorder = BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),BorderFactory.createEmptyBorder(5,20,5,20));

		JPanel listenerPanel = new JPanel(new GridLayout(0,2));	// "Specifying the number of columns affects the layout only when the number of rows is set to zero."
		listenerPanel.setBorder(panelBorder);
		{
			JLabel listenerHeaderJLabel = new JLabel("Our DICOM network properties:");
			listenerPanel.add(listenerHeaderJLabel);
			listenerPanel.add(new JLabel(""));
		}
		try {
			java.util.Enumeration<java.net.NetworkInterface> nifs = java.net.NetworkInterface.getNetworkInterfaces();
			while (nifs.hasMoreElements()) {
				java.net.NetworkInterface nif = nifs.nextElement();
				java.util.Enumeration<java.net.InetAddress> inetas = nif.getInetAddresses();
				while (inetas.hasMoreElements()) {
					java.net.InetAddress ineta = inetas.nextElement();
					if (!ineta.isLoopbackAddress()) {
						String hostname = ineta.getCanonicalHostName();
						listenerPanel.add(new JLabel(hostname+": ",SwingConstants.RIGHT));
						String hostaddress = ineta.getHostAddress();
						listenerPanel.add(new JLabel(hostaddress));
					}
				}
			}
		}
		catch (java.net.SocketException e) {
			// don't care if there were no interfaces
			System.err.println(e);
		}
		{
			JLabel callingAETitleJLabel = new JLabel("Calling AE Title: ",SwingConstants.RIGHT);
			callingAETitleJLabel.setToolTipText("The AE Title we use for ourselves when calling (initiating an association to) another AE");
			listenerPanel.add(callingAETitleJLabel);
			callingAETitleField = new JTextField();
			callingAETitleField.setText(networkApplicationProperties.getCallingAETitle());
        	listenerPanel.add(callingAETitleField);
			callingAETitleField.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(java.awt.event.FocusEvent event) {
					JTextComponent textComponent = (JTextComponent)(event.getSource());
					textComponent.selectAll();
				}
			});
		}
		{
			JLabel calledAETitleJLabel = new JLabel("Called AE Title: ",SwingConstants.RIGHT);
			calledAETitleJLabel.setToolTipText("The AE Title we expect to be called by (when accepting an association from) another AE; usually the same as the Calling AE Title");
			listenerPanel.add(calledAETitleJLabel);
			calledAETitleField = new JTextField();
			calledAETitleField.setText(networkApplicationProperties.getCalledAETitle());
        	listenerPanel.add(calledAETitleField);
			calledAETitleField.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(java.awt.event.FocusEvent event) {
					JTextComponent textComponent = (JTextComponent)(event.getSource());
					textComponent.selectAll();
				}
			});
		}
		{
			JLabel listeningPortJLabel = new JLabel("Listening port: ",SwingConstants.RIGHT);
			listeningPortJLabel.setToolTipText("The port on which we listen for inbound connections from another AE");
			listenerPanel.add(listeningPortJLabel);
			listeningPortField = new JTextField();
			listeningPortField.setText(Integer.toString(networkApplicationProperties.getListeningPort()));
        	listenerPanel.add(listeningPortField);
			listeningPortField.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusGained(java.awt.event.FocusEvent event) {
					JTextComponent textComponent = (JTextComponent)(event.getSource());
					textComponent.selectAll();
				}
			});
		}
// * <p><code>Dicom.ListeningPort</code> - the port that an association acceptor will listen on for incoming connections</p>
// * <p><code>Dicom.CalledAETitle</code> - what the AE expects to be called when accepting an association</p>
// * <p><code>Dicom.CallingAETitle</code> - what the AE will call itself when initiating an association</p>
// * <p><code>Dicom.PrimaryDeviceType</code> - what our own primary device type is</p>
		
		
		JPanel remoteAEPanel = new JPanel();
		{
			remoteAEPanel.setBorder(panelBorder);
			JPanel remoteHeaderPanel = new JPanel(new GridLayout(1,1));
			{
				remoteHeaderPanel.add(new JLabel("Remote AE DICOM network properties:"));
			}
		
			JPanel remoteButtonPanel = new JPanel();
			{
				remoteButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

                JButton selectRemoteAEButton = new JButton("Select");
                selectRemoteAEButton.setToolTipText("Selects the remote Application Entity to use for query");
                remoteButtonPanel.add(selectRemoteAEButton);
                selectRemoteAEButton.addActionListener(new ChooseRemoteAEActionListener());

				JButton addRemoteAEButton = new JButton("Add");
				addRemoteAEButton.setToolTipText("Add an AE to the list of remote Application Entities");
				remoteButtonPanel.add(addRemoteAEButton);
				addRemoteAEButton.addActionListener(new AddRemoteAEActionListener());

				JButton editRemoteAEButton = new JButton("Edit");
				editRemoteAEButton.setToolTipText("Edit an AE in the list of remote Application Entities");
				remoteButtonPanel.add(editRemoteAEButton);
				editRemoteAEButton.addActionListener(new EditRemoteAEActionListener());
		
				JButton removeRemoteAEButton = new JButton("Remove");
				removeRemoteAEButton.setToolTipText("Remove an AE from the list of remote Application Entities");
				remoteButtonPanel.add(removeRemoteAEButton);
				removeRemoteAEButton.addActionListener(new RemoveRemoteAEActionListener());



			}
			
			GridBagLayout layout = new GridBagLayout();
			remoteAEPanel.setLayout(layout);
			{
				GridBagConstraints constraints = new GridBagConstraints();
				constraints.gridx = 0;
				constraints.gridy = 0;
				constraints.weightx = 1;
				constraints.weighty = 1;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				layout.setConstraints(remoteHeaderPanel,constraints);
				remoteAEPanel.add(remoteHeaderPanel);
			}
			{
				GridBagConstraints constraints = new GridBagConstraints();
				constraints.gridx = 0;
				constraints.gridy = 1;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				layout.setConstraints(remoteButtonPanel,constraints);
				remoteAEPanel.add(remoteButtonPanel);
			}

		}
		
		JPanel commonButtonPanel = new JPanel();
		{
			commonButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			commonButtonPanel.setBorder(panelBorder);

			JButton doneButton = new JButton("Done");
			doneButton.setToolTipText("Finished edits, so reset application to use new configuration");
			commonButtonPanel.add(doneButton);
			doneButton.addActionListener(new DoneActionListener());
		}
		
		JPanel mainPanel = new JPanel();
		{
			GridBagLayout mainPanelLayout = new GridBagLayout();
			mainPanel.setLayout(mainPanelLayout);
			{
				GridBagConstraints constraints = new GridBagConstraints();
				constraints.gridx = 0;
				constraints.gridy = 0;
				constraints.weightx = 1;
				constraints.weighty = 1;
				constraints.fill = GridBagConstraints.BOTH;
				mainPanelLayout.setConstraints(listenerPanel,constraints);
				mainPanel.add(listenerPanel);
			}
			{
				GridBagConstraints constraints = new GridBagConstraints();
				constraints.gridx = 0;
				constraints.gridy = 1;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				mainPanelLayout.setConstraints(remoteAEPanel,constraints);
				mainPanel.add(remoteAEPanel);
			}
			{
				GridBagConstraints constraints = new GridBagConstraints();
				constraints.gridx = 0;
				constraints.gridy = 2;
				constraints.fill = GridBagConstraints.HORIZONTAL;
				mainPanelLayout.setConstraints(commonButtonPanel,constraints);
				mainPanel.add(commonButtonPanel);
			}
		}
		Container content = dialog.getContentPane();
		content.add(mainPanel);
		dialog.pack();
		dialog.setVisible(true);
	}
}

