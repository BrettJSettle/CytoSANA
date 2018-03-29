package org.cytoscape.sana.sana_app.internal;

import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.JSpinner.DefaultEditor;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.sana.sana_app.internal.rest.parameters.AlignmentParameters;
import org.cytoscape.sana.sana_app.internal.rest.parameters.SanaParameters;
import org.cytoscape.sana.sana_app.internal.rest.parameters.VisualizeParameters;
import org.cytoscape.sana.sana_app.internal.task.PerformAlignmentTaskFactory;
import org.cytoscape.sana.sana_app.internal.task.PerformVisualizeTask;
import org.cytoscape.sana.sana_app.internal.util.SanaAlignmentUtil;
import org.cytoscape.sana.sana_app.internal.util.SanaAlignmentUtil.InvalidParametersException;
import org.cytoscape.work.TaskIterator;

public class MainComponent extends Unloadable implements ActionListener, PopupMenuListener, ItemListener {

	/**
	 * Create the panel.
	 */
	private HashMap<String, CyNetwork> network_map = new HashMap<String, CyNetwork>();

	JComboBox<String> comboBox1, comboBox2;
	private JToggleButton toggleEdgesButton;
	private JToggleButton selectNetwork1Toggle, selectNetwork2Toggle;
	JTabbedPane panes;
	JPanel panel1, visualizePanel;
	JButton alignButton, visualizeButton;

	/* SANA */
	JSpinner timeSpinner, alphaSpinner, betaSpinner;
	JCheckBox nodesHaveTypesCheckbox;

	/* Visualization */
	JCheckBox overlapCheckBox;

	MainComponent() {
		super();

		createUIComponents();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				refresh();
			}
		});
		(new UpdateThread()).start();
	}

	@Override
	public void unload() {
	}

	public JPanel makeAlignmentPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Input"));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 100 };
		gridBagLayout.rowHeights = new int[] { 24, 24, 24, 24, 200, 24 };
		gridBagLayout.columnWeights = new double[] { 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 0.0 };
		panel.setLayout(gridBagLayout);

		comboBox1 = new JComboBox<String>();
		comboBox2 = new JComboBox<String>();
		comboBox1.addPopupMenuListener(this);
		comboBox2.addPopupMenuListener(this);
		comboBox1.addItemListener(this);
		comboBox2.addItemListener(this);
		updateNetworkCombos();

		addRow(panel, null, comboBox1, 0);
		addRow(panel, null, comboBox2, 1);
		addRow(panel, null, makeSANAPanel(), 2);
		addRow(panel, null, getAlignButton(), 5);

		return panel;
	}

	public JButton getAlignButton() {
		if (alignButton == null) {
			alignButton = new JButton("Start Alignment");
			alignButton.addActionListener(this);
		}
		return alignButton;
	}

	public JPanel makeSANAPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("SANA"));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 100, 150 };
		gridBagLayout.rowHeights = new int[] { 24, 24, 24, 24 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		panel.setLayout(gridBagLayout);

		timeSpinner = new JSpinner(new SpinnerNumberModel(new Integer(1), new Integer(0), null, new Integer(1)));
		DefaultEditor editor = (DefaultEditor) timeSpinner.getEditor();
		JFormattedTextField jftf = ((DefaultEditor) editor).getTextField();
		jftf.setColumns(5);

		alphaSpinner = new JSpinner(
				new SpinnerNumberModel(new Integer(1), new Integer(0), new Integer(1), new Double(.01)));
		editor = (DefaultEditor) alphaSpinner.getEditor();
		jftf = ((DefaultEditor) editor).getTextField();
		jftf.setColumns(5);

		betaSpinner = new JSpinner();
		betaSpinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), new Integer(1), new Double(.1)));
		editor = (DefaultEditor) betaSpinner.getEditor();
		jftf = ((DefaultEditor) editor).getTextField();
		jftf.setColumns(5);

		nodesHaveTypesCheckbox = new JCheckBox("Nodes have types");

		addRow(panel, "Time Limit (minutes): ", timeSpinner, 0);
		addRow(panel, "Alpha: ", alphaSpinner, 1);
		addRow(panel, "Beta: ", betaSpinner, 2);
		addRow(panel, "", nodesHaveTypesCheckbox, 3);

		return panel;
	}

	private void addRow(JPanel panel, String s, JComponent component, int row) {
		GridBagConstraints gbc = new GridBagConstraints();
		int x = 0;
		if (s != null) {
			JLabel label = new JLabel(s);
			gbc.insets = new Insets(0, 0, 5, 5);
			gbc.gridx = x++;
			gbc.gridy = row;
			panel.add(label, gbc);
		} else {
			gbc.gridwidth = 2;
		}
		if (component instanceof JButton)
			gbc.insets = new Insets(0, 10, 5, 10);
		else
			gbc.insets = new Insets(0, 0, 5, 0);
		gbc.gridx = x;
		gbc.gridy = row;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(component, gbc);
	}

	private JPanel getVisualizeDisabledPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(
				new JLabel(
						"<html><div style='text-align: center; margin=\"10px\";'>Must select an alignment result network to use the visualize panel</div></html>"),
				BorderLayout.CENTER);
		return panel;
	}

	private JPanel getVisualizationPanel() {
		if (visualizePanel == null) {
			visualizePanel = new JPanel();
			GridBagLayout gridBagLayout = new GridBagLayout();
			gridBagLayout.columnWidths = new int[] { 100, 150 };
			gridBagLayout.rowHeights = new int[] { 24, 24, 24, 100 };
			gridBagLayout.columnWeights = new double[] { 1.0, 1.0 };
			gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 100 };
			visualizePanel.setLayout(gridBagLayout);

			overlapCheckBox = new JCheckBox("Overlay networks");
			addRow(visualizePanel, "Type:", overlapCheckBox, 0);
			addRow(visualizePanel, null, getVisualizeButton(), 1);
			addRow(visualizePanel, null, getToggleVisualsPanel(), 2);

		}
		return visualizePanel;
	}

	private JPanel getSelectNetworkPanel() {
		JPanel selectPanel = new JPanel(new GridLayout(1, 2));
		selectPanel.add(getSelectNetwork1Toggle());
		selectPanel.add(getSelectNetwork2Toggle());
		return selectPanel;
	}

	private JPanel getToggleVisualsPanel() {
		JPanel togglePanel = new JPanel();
		togglePanel.setBorder(BorderFactory.createTitledBorder("Toggle Options"));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 1, 1 };
		gridBagLayout.rowHeights = new int[] { 1, 1 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0 };
		togglePanel.setLayout(gridBagLayout);

		addRow(togglePanel, null, getToggleEdgesButton(), 0);
		addRow(togglePanel, null, getSelectNetworkPanel(), 1);

		return togglePanel;
	}

	private JToggleButton getSelectNetwork1Toggle() {
		if (selectNetwork1Toggle == null) {
			selectNetwork1Toggle = new JToggleButton("(De)Select Network 1");
			selectNetwork1Toggle.addActionListener(this);
		}
		return selectNetwork1Toggle;
	}

	private JToggleButton getSelectNetwork2Toggle() {
		if (selectNetwork2Toggle == null) {
			selectNetwork2Toggle = new JToggleButton("(De)Select Network 2");
			selectNetwork2Toggle.addActionListener(this);
		}
		return selectNetwork2Toggle;
	}

	private JToggleButton getToggleEdgesButton() {
		if (toggleEdgesButton == null) {
			toggleEdgesButton = new JToggleButton("Toggle Alignment Edges");
			toggleEdgesButton.addActionListener(this);
		}
		return toggleEdgesButton;
	}

	private JButton getVisualizeButton() {
		if (visualizeButton == null) {
			visualizeButton = new JButton("Visualize Alignment");
			visualizeButton.addActionListener(this);
		}
		return visualizeButton;
	}

	public void createUIComponents() {
		panel1 = new JPanel();
		panel1.setLayout(new BorderLayout());
		panes = new JTabbedPane();
		panes.addTab("Alignment", makeAlignmentPanel());
		panes.addTab("Visualization", getVisualizationPanel());
		panes.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setVisualizeEnabled(SanaAlignmentUtil.isAlignmentNetworkSelected());
			}
		});
		panel1.add(panes, BorderLayout.CENTER);
	}

	public void setVisualizeEnabled(boolean isResult) {
		panes.setComponentAt(1, isResult ? getVisualizationPanel() : getVisualizeDisabledPanel());
	}

	private void alignPressed() {
		String name1 = (String) comboBox1.getSelectedItem();
		String name2 = (String) comboBox2.getSelectedItem();
		CyNetwork net1 = network_map.get(name1);
		CyNetwork net2 = network_map.get(name2);

		Runnable onFinish = new Runnable() {

			@Override
			public void run() {
				alignButton.setText("Start Alignment");
			}
		};
		AlignmentParameters params;
		try {
			params = getAlignmentParameters(net1, net2);
		} catch (InvalidParametersException e) {
			SanaUtil.errorbox(e.getMessage());
			onFinish.run();
			return;
		}
		PerformAlignmentTaskFactory tf = new PerformAlignmentTaskFactory(net1, net2, params);
		SanaApp.rm.taskmgr.execute(String.format("Aligning %s and %s", name1, name2), tf.createTaskIterator(),
				onFinish);

	}

	private void visualizePressed() {
		TaskIterator it = new TaskIterator();
		CyNetwork curNet = SanaApp.rm.cyappmgr.getCurrentNetwork();
		if (curNet == null || !SanaAlignmentUtil.isAlignmentNetwork(curNet)) {
			return;
		}
		CyRow row = curNet.getRow(curNet);
		List<Long> suids = row.getList(ColumnNames.SOURCE_NETWORKS_LIST, Long.class);
		CyNetwork network = SanaApp.rm.cynetmgr.getNetwork(suids.get(0));

		VisualizeParameters params = new VisualizeParameters();
		params.overlap = overlapCheckBox.isSelected();

		it.append(new PerformVisualizeTask(curNet, params, network));
		SanaApp.rm.taskmgr.execute("Visualizing ", it, null);

	}

	private void toggleEdgesPressed(boolean vis) {
		CyNetwork net = SanaApp.rm.cyappmgr.getCurrentNetwork();
		if (net != null) {
			if (vis) {
				SanaAlignmentUtil.generateMappingEdges(net);
			} else {
				SanaAlignmentUtil.removeMappingEdges(net);
			}
		}
		SanaApp.rm.cyeventmgr.flushPayloadEvents();

	}

	

	
	private AlignmentParameters getAlignmentParameters(CyNetwork net1, CyNetwork net2)
			throws InvalidParametersException {

		int t = (int) timeSpinner.getValue();
		float a = (int) alphaSpinner.getValue();
		float b = (int) betaSpinner.getValue();
		boolean nodesHaveTypes = nodesHaveTypesCheckbox.isSelected();

		for (CyNetwork net : new CyNetwork[] { net1, net2 }) {
			SanaAlignmentUtil.validateNetwork(net, nodesHaveTypes);
		}
		SanaParameters params = new SanaParameters(t, a, b, nodesHaveTypes);
		AlignmentParameters args = new AlignmentParameters(net1.getSUID(), net2.getSUID(), params);

		return args;
	}
	
	

	@Override
	public void actionPerformed(ActionEvent ev) {

		if (ev.getSource() == getAlignButton()) {
			if (alignButton.getText() == "Cancel Alignment") {
				SanaApp.rm.taskmgr.cancel();
				alignButton.setText("Start Alignment");
			} else {
				alignButton.setText("Cancel Alignment");
				alignPressed();
			}
		} else if (ev.getSource() == getVisualizeButton()) {
			visualizePressed();
		} else if (ev.getSource() == getToggleEdgesButton()) {
			toggleEdgesPressed(getToggleEdgesButton().isSelected());
		} else if (ev.getSource() == getSelectNetwork1Toggle()) {
			toggleSelectNetwork(1, getSelectNetwork1Toggle().isSelected());
		} else if (ev.getSource() == getSelectNetwork2Toggle()) {
			toggleSelectNetwork(2, getSelectNetwork2Toggle().isSelected());
		}
	}

	private void toggleSelectNetwork(int i, boolean selected) {
		CyNetwork network = SanaApp.rm.cyappmgr.getCurrentNetwork();
		for (CyRow row : network.getDefaultNodeTable().getMatchingRows(ColumnNames.SOURCE_NETWORK_ID, i)) {
			row.set(CyNetwork.SELECTED, selected);
		}
	}

	private void updateNetworkCombos() {
		String name1 = (String) comboBox1.getSelectedItem();
		String name2 = (String) comboBox2.getSelectedItem();

		comboBox1.removeAllItems();
		comboBox2.removeAllItems();
		network_map.clear();
		if (SanaApp.rm != null) {
			for (CyNetwork net : SanaApp.rm.cynetmgr.getNetworkSet()) {
				String name = net.getDefaultNetworkTable().getRow(net.getSUID()).get(CyNetwork.NAME, String.class);
				network_map.put(name, net);
				comboBox1.addItem(name);
				comboBox2.addItem(name);
			}
		}
		if (network_map.size() == 0) {
			comboBox1.addItem("Load networks first...");
			comboBox2.addItem("Load networks first...");
		}

		if (network_map.containsKey(name1))
			comboBox1.setSelectedItem(name1);
		if (network_map.containsKey(name2))
			comboBox2.setSelectedItem(name2);

	}

	public void refresh() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				_refresh();
			}
		});
	}

	private void _refresh() {
		// updateRootNetworkCombo();
		panel1.validate();
		panel1.repaint();
	}

	private class UpdateThread extends Thread implements IUnloadable {
		private volatile boolean exit = false;

		UpdateThread() {
			unloadLater(this);
		}

		public void run() {
			run2();
		}

		private void run2() {
			while (true) {
				synchronized (this) {
					if (exit)
						return;
				}
				// tickSpinner();
				try {
					sleep(100);
				} catch (InterruptedException e) {
					return;
				}
			}
		}

		@Override
		public synchronized void unload() {
			exit = true;
			interrupt();
		}
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		if (e.getSource() == comboBox1 || e.getSource() == comboBox2) {
			updateNetworkCombos();
		}

	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		getAlignButton().setEnabled(network_map.containsKey(e.getItem()));
	}

	public static void main(String[] args) {
		MainComponent c = new MainComponent();
		JPanel panel = c.getVisualizationPanel();
		JFrame f = new JFrame();
		f.add(panel);
		f.setSize(300, 600);
		f.setVisible(true);
	}

}
