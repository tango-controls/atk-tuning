// Tuning panel class
package atktuning;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import fr.esrf.tangoatk.core.INumberScalar;
import fr.esrf.tangoatk.widget.attribute.NumberScalarWheelEditor;
import fr.esrf.tangoatk.widget.attribute.SimplePropertyFrame;
import fr.esrf.tangoatk.widget.attribute.SimpleScalarViewer;
import fr.esrf.tangoatk.widget.command.CommandMenuViewer;
import fr.esrf.tangoatk.widget.properties.LabelViewer;
import fr.esrf.tangoatk.widget.util.JAutoScrolledText;
import fr.esrf.tangoatk.widget.util.JSmoothLabel;

class TuningPanel extends JPanel implements ActionListener {

	private final JSmoothLabel title;
	private final LabelViewer[] labels;
	private final SimpleScalarViewer[] values;
	private final NumberScalarWheelEditor[] setters;
	private CommandMenuViewer[] commands;
	private final JButton[] propBtn;
	private int maxLabWidth = 0;
	private int maxWheelWidth = 0;
	private int height = 0;
	private final TuningConfig theCfg;
	private final JFrame parentFrame;
	private final boolean showCommand;
	private final boolean readOnly;
  private final int rowHeiht;

	static private SimplePropertyFrame propFrame = null;
	static private Color uColor = new Color(130, 130, 130);

	public TuningPanel(TuningConfig cfg, Font f, Font tf, int maxH,
			boolean showCommand, boolean readOnly, JFrame parent) {

		int i;
		int nb = cfg.getNbItem();
		theCfg = cfg;
    rowHeiht = 45;
		height = maxH * rowHeiht;
		this.showCommand = showCommand;
		this.readOnly = readOnly;

		parentFrame = parent;

		setLayout(null);
		setBorder(BorderFactory.createEtchedBorder());

		// Create Labels
		labels = new LabelViewer[nb];
		for (i = 0; i < nb; i++) {
			labels[i] = new LabelViewer();
			INumberScalar m = cfg.getAtt(i);
			labels[i].setModel(m);
			// labels[i].setToolTipText(m.getName());
			labels[i].setBackground(getBackground());
			labels[i].setHorizontalAlignment(JSmoothLabel.RIGHT_ALIGNMENT);
			Dimension d = labels[i].getPreferredSize();
			if (d.width > maxLabWidth)
				maxLabWidth = d.width;
			add(labels[i]);
		}

		// Create WheelSwitchs
		setters = new NumberScalarWheelEditor[nb];

		for (i = 0; i < nb; i++) {
			INumberScalar m = cfg.getAtt(i);
			if (m.isWritable()) {
				setters[i] = new NumberScalarWheelEditor();
				if(f!=null) setters[i].setFont(f);
				setters[i].setBackground(getBackground());
				setters[i].setModel(m);
				Dimension d = setters[i].getPreferredSize();
				if (d.width > maxWheelWidth)
					maxWheelWidth = d.width;
				if (readOnly) {
					setters[i].setEnabled(false);
				}
				add(setters[i]);
			} else {
				setters[i] = null;
			}
		}

		// Create title
		title = new JSmoothLabel();
		if(tf!=null) title.setFont(tf);
		title.setBackground(getBackground());
		title.setHorizontalAlignment(JSmoothLabel.CENTER_ALIGNMENT);
		title.setText(getConfig().getTitle());
		// title.setValueOffsets(0,-5); // deprecated
		add(title);

		// Create scalar viewer
		values = new SimpleScalarViewer[nb];
		for (i = 0; i < nb; i++) {
			values[i] = new SimpleScalarViewer();
      values[i].setHorizontalAlignment(JAutoScrolledText.RIGHT_ALIGNMENT);
      values[i].setMargin(new Insets(0,0,0,10));
      values[i].setBackgroundColor(getBackground());
			values[i].setFont(f);
			values[i].setBorder(javax.swing.BorderFactory
          .createLoweredBevelBorder());
			values[i].setBounds(maxLabWidth + 4, (i + 1) * 32, 150, 30);
			values[i].setModel(cfg.getAtt(i));
			values[i].setBackground(uColor);
			values[i].setText("------");
			values[i].setEditable(readOnly);
			add(values[i]);
		}

		// Create command menu viewer
		if (showCommand) {
			commands = new CommandMenuViewer[nb];
			for (i = 0; i < nb; i++) {
				commands[i] = new CommandMenuViewer();
				commands[i].setBackground(getBackground());
				commands[i].setModel(cfg.getCmds(i));
				add(commands[i]);
			}
		}

		// Create property button
		propBtn = new JButton[nb];
		for (i = 0; i < nb; i++) {
			propBtn[i] = new JButton();
			propBtn[i].setFont(f);
			if (showCommand)
				propBtn[i].setText("?");
			else
				propBtn[i].setText("...");
			propBtn[i].setMargin(new Insets(0, 0, 0, 0));
			propBtn[i].addActionListener(this);
			add(propBtn[i]);
		}

		// Done
		computeMaxWidth();
		placeComponents();

	}

	private void placeComponents() {

		int i;
		int nb = theCfg.getNbItem();
    int compH = rowHeiht-2;

		if (showCommand)
			title.setBounds(2, 2, 186 + maxWheelWidth + maxLabWidth, compH);
		else
			title.setBounds(2, 2, 156 + maxWheelWidth + maxLabWidth, compH);

		// Place components
		for (i = 0; i < nb; i++) {

			labels[i].setBounds(2, (i + 1) * rowHeiht, maxLabWidth, compH);

			values[i].setBounds(maxLabWidth + 4, (i + 1) * rowHeiht+3, 150, compH-6);

      if (setters[i] != null)
				setters[i].setBounds(maxLabWidth + 156, (i + 1) * rowHeiht,
						maxWheelWidth, compH);

			if (showCommand) {
				commands[i].setBounds(maxLabWidth + maxWheelWidth + 157,
						(i + 1) * rowHeiht+6, 30, compH-12);
				propBtn[i].setBounds(maxLabWidth + maxWheelWidth + 187,
						(i + 1) * rowHeiht+6, 30, compH-12);
			} else {
				propBtn[i].setBounds(maxLabWidth + maxWheelWidth + 157,
						(i + 1) * rowHeiht+6, 30, compH-12);
			}

		}

	}

	private void computeMaxWidth() {

		int nb = theCfg.getNbItem();
		int i;

		maxLabWidth = 0;
		maxWheelWidth = 0;

		for (i = 0; i < nb; i++) {
			Dimension d = labels[i].getPreferredSize();
			if (d.width > maxLabWidth)
				maxLabWidth = d.width;
		}

		for (i = 0; i < nb; i++) {
			if (setters[i] != null) {
				Dimension d = setters[i].getPreferredSize();
				if (d.width > maxWheelWidth)
					maxWheelWidth = d.width;
			}
		}

	}

	public void actionPerformed(ActionEvent e) {

		int i = 0;
		boolean found = false;
		// Find source
		while (i < theCfg.getNbItem() && !found) {
			found = propBtn[i].equals(e.getSource());
			if (!found)
				i++;
		}

		if (found) {
			if (propFrame == null)
				propFrame = new SimplePropertyFrame(parentFrame, true);
			propFrame.setModel(theCfg.getAtt(i));
			propFrame.setVisible(true);
		}

	}

	@Override
	public Dimension getPreferredSize() {
		if (showCommand)
			return new Dimension(220 + maxWheelWidth + maxLabWidth, height + 2);
		else
			return new Dimension(190 + maxWheelWidth + maxLabWidth, height + 2);
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public TuningConfig getConfig() {
		return theCfg;
	}

}
