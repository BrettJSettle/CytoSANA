package org.cytoscape.sana.sana_app.internal;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.sana.sana_app.internal.resources.*;
import javax.swing.*;

import java.awt.*;
import java.util.Properties;

public class SanaMainPanel extends Unloadable implements CytoPanelComponent {
	public MainComponent ui;

	public SanaMainPanel() {
		getUI();
		SanaApp.rm.cyreg.registerService(this, CytoPanelComponent.class, new Properties());
	}

	public void refresh() {

		ui.refresh();
	}

	public MainComponent getUI() {
		if (ui == null) {
			ui = new MainComponent();
		}
		return ui;
	}

	@Override
	public Component getComponent() {
		return ui.panel1;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		return "Sana";
	}

	@Override
	public Icon getIcon() {
		return Resources.getTabIcon();
	}

	@Override
	public void unload() {
		SanaApp.rm.cyreg.unregisterService(this, CytoPanelComponent.class);

		ui = null;
	}

}
