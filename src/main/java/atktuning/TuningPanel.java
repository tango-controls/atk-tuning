// Tuning panel class
package atktuning;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import fr.esrf.tangoatk.core.CommandList;
import fr.esrf.tangoatk.core.IAttribute;
import fr.esrf.tangoatk.core.IBooleanScalar;
import fr.esrf.tangoatk.core.INumberScalar;
import fr.esrf.tangoatk.widget.attribute.BooleanScalarCheckBoxViewer;
import fr.esrf.tangoatk.widget.attribute.NumberScalarWheelEditor;
import fr.esrf.tangoatk.widget.attribute.SimplePropertyFrame;
import fr.esrf.tangoatk.widget.attribute.SimpleScalarViewer;
import fr.esrf.tangoatk.widget.command.CommandMenuViewer;
import fr.esrf.tangoatk.widget.properties.LabelViewer;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.JAutoScrolledText;
import fr.esrf.tangoatk.widget.util.JSmoothLabel;

class TuningPanel extends JPanel implements ActionListener {

	private final JSmoothLabel title;
	private final LabelViewer[] labels;
	private final JComponent[] values;
  private NumberScalarWheelEditor[] setters = null;
	private CommandMenuViewer[] commands;
	private final JButton[] propBtn;
  private JButton[] setBtn = null;
	private int maxLabWidth = 0;
	private int height = 0;
	private final TuningConfig theCfg;
	private final JFrame parentFrame;
	private final boolean showCommand;
  private final boolean showBackground;
  private final boolean showEditor;
  private final boolean showSettingFrameButton;
  private int setterWidth = 0;
  private int setBtnWidth = 0;
  private int rowHeight = 28;

	static private SimplePropertyFrame propFrame = null;
	static private Color uColor = new Color(130, 130, 130);
  static private int viewerWidth = 120;
  static private int titleHeight = 40;
  static private Font titleFont = new Font("Dialog", Font.BOLD, 18);
  static private Font viewerFont = null;

	public TuningPanel(TuningConfig cfg, int maxH,
			boolean showCommand, boolean readOnly, boolean showEditor, boolean showBackground, boolean showSettingFrameButton, String fName, JFrame parent) {

		int i;
		int nb = cfg.getNbItem();
		theCfg = cfg;
		this.showCommand = showCommand;
    this.showBackground = showBackground;
    this.showSettingFrameButton = showSettingFrameButton;
    this.showEditor = showEditor;

		parentFrame = parent;

    if(viewerFont==null)
      viewerFont = Utils.getInstance().parseFont(fName);

		setLayout(null);
		setBorder(BorderFactory.createEtchedBorder());

    // Create title
    title = new JSmoothLabel();
    title.setFont(titleFont);
    title.setBackground(getBackground());
    title.setHorizontalAlignment(JSmoothLabel.CENTER_ALIGNMENT);
    title.setText(getConfig().getTitle());
    add(title);

		// Create Labels
		labels = new LabelViewer[nb];
		for (i = 0; i < nb; i++) {
			labels[i] = new LabelViewer();
			IAttribute m = cfg.getAtt(i);
			labels[i].setModel(m);
			// labels[i].setToolTipText(m.getName());
			labels[i].setBackground(getBackground());
			labels[i].setHorizontalAlignment(JSmoothLabel.RIGHT_ALIGNMENT);
      labels[i].setFont(viewerFont);
			Dimension d = labels[i].getPreferredSize();
			if (d.width > maxLabWidth)
				maxLabWidth = d.width;
			add(labels[i]);
		}

		// Create setters button
    if (showEditor) {

      setters = new NumberScalarWheelEditor[nb];

      for (i = 0; i < nb; i++) {

        IAttribute m = cfg.getAtt(i);
        if (m.isWritable() && m instanceof INumberScalar) {
          INumberScalar ns = (INumberScalar)m;
          setters[i] = new NumberScalarWheelEditor();
          setters[i].setBackground(getBackground());
          setters[i].setFont(viewerFont);
          setters[i].setModel(ns);
          //setters[i].setAlarmEnabled(showBackground);

          if (readOnly)
            setters[i].setEnabled(false);
          add(setters[i]);
        } else {
          setters[i] = null;
        }

      }

    }

    if( showSettingFrameButton ) {

      setBtn = new JButton[nb];

      for (i = 0; i < nb; i++) {

        IAttribute m = cfg.getAtt(i);
        if (m.isWritable() && m instanceof INumberScalar) {
          setBtn[i] = new JButton("...");
          setBtn[i].addActionListener(this);
          if (readOnly)
            setBtn[i].setEnabled(false);
          add(setBtn[i]);
        } else {
          setBtn[i] = null;
        }

      }

    }

    // Create scalar viewer
		values = new JComponent[nb];
		for (i = 0; i < nb; i++) {

      IAttribute m = cfg.getAtt(i);

      if (m instanceof INumberScalar) {

        INumberScalar ns = (INumberScalar)m;
        SimpleScalarViewer v = new SimpleScalarViewer();
        v.setHorizontalAlignment(JAutoScrolledText.RIGHT_ALIGNMENT);
        v.setMargin(new Insets(0, 0, 0, 10));
        if (!showBackground) v.setBackgroundColor(getBackground());
        v.setFont(viewerFont);
        v.setBorder(javax.swing.BorderFactory
            .createLoweredBevelBorder());
        v.setModel(ns);
        v.setBackground(uColor);
        v.setText("------");
        v.setEditable(readOnly);
        values[i] = v;

      }

      if (m instanceof IBooleanScalar) {

        IBooleanScalar bs = (IBooleanScalar)m;
        BooleanScalarCheckBoxViewer v = new BooleanScalarCheckBoxViewer();
        v.setAttModel(bs);
        v.setTrueLabel("");
        v.setFalseLabel("");
        values[i] = v;

      }

      add(values[i]);
		}

		// Create command menu viewer
		if (showCommand) {
			commands = new CommandMenuViewer[nb];
			for (i = 0; i < nb; i++) {
				commands[i] = new CommandMenuViewer();
        commands[i].setMenuTitle(" V");
				commands[i].setBackground(getBackground());
        if(cfg.hasCommand(i)) commands[i].setModel(cfg.getCmds(i));
				add(commands[i]);
			}
		}

		// Create property button
		propBtn = new JButton[nb];
		for (i = 0; i < nb; i++) {
			propBtn[i] = new JButton();
			propBtn[i].setFont(viewerFont);
		  propBtn[i].setText("?");
			propBtn[i].setMargin(new Insets(0, 0, 0, 0));
			propBtn[i].addActionListener(this);
			add(propBtn[i]);
		}

		// Done
		computeMaxWidth();
    height = maxH * rowHeight + titleHeight;
		placeComponents();

	}

	private void placeComponents() {

		int i;
		int nb = theCfg.getNbItem();
    int compH = rowHeight -1;

		if (showCommand)
			title.setBounds(2, 2, 66 + viewerWidth + setterWidth + setBtnWidth + maxLabWidth, titleHeight-2);
		else
			title.setBounds(2, 2, 36 + viewerWidth + setterWidth + setBtnWidth + maxLabWidth, titleHeight-2);

		// Place components
		for (i = 0; i < nb; i++) {

      int y = titleHeight + i*rowHeight;
      int xl = maxLabWidth + setterWidth + viewerWidth + setBtnWidth;

			labels[i].setBounds(2, y, maxLabWidth, compH);
			values[i].setBounds(maxLabWidth + 4, y+1, viewerWidth, compH);

      if( showEditor )
        if(setters[i]!=null)
          setters[i].setBounds(maxLabWidth + 4 + viewerWidth, y+1, setterWidth, compH);

      if( showSettingFrameButton )
        if(setBtn[i]!=null)
          setBtn[i].setBounds(maxLabWidth + viewerWidth + setterWidth + 6, y+1, setBtnWidth, compH);

			if (showCommand) {
				commands[i].setBounds(xl + 7, y +1, 30, compH);
				propBtn[i].setBounds(xl + 37, y +1, 30, compH);
			} else {
				propBtn[i].setBounds(xl + 7,  y +1, 30, compH);
			}

		}

	}

	private void computeMaxWidth() {

		int nb = theCfg.getNbItem();
		int i;

		maxLabWidth = 0;
    setterWidth = 0;
    setBtnWidth = 0;
    rowHeight = 28;

		for (i = 0; i < nb; i++) {
			Dimension d = labels[i].getPreferredSize();
			if (d.width > maxLabWidth)
				maxLabWidth = d.width;

      if(showEditor)
        if(setters[i]!=null) {
          setterWidth = Math.max(setters[i].getPreferredSize().width,setterWidth);
          rowHeight = 35;
        }

      if(showSettingFrameButton)
        if(setBtn[i]!=null)
         setBtnWidth = 30;

		}

	}

	public void actionPerformed(ActionEvent e) {

		int i = 0;
		boolean found = false;

		// Find source (property frame)
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
      return;
		}

    // Find source (Setting frame)
    i = 0;
    found = false;
    while (i < theCfg.getNbItem() && !found) {
      found = setBtn[i]!=null && setBtn[i].equals(e.getSource());
      if (!found)
        i++;
    }

    if (found) {
      if(theCfg.getSetFrame(i)==null)
        theCfg.createSettingFrame(i,title.getText(),(INumberScalar)theCfg.getAtt(i),showBackground);
      ATKGraphicsUtils.centerFrameOnScreen(theCfg.getSetFrame(i));
      theCfg.getSetFrame(i).setVisible(true);
      return;
    }

	}

	@Override
	public Dimension getPreferredSize() {
		if (showCommand)
			return new Dimension(70 + viewerWidth + setterWidth + setBtnWidth + maxLabWidth, height + 2);
		else
			return new Dimension(42 + viewerWidth + setterWidth + setBtnWidth + maxLabWidth, height + 2);
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public TuningConfig getConfig() {
		return theCfg;
	}

  public void clearModel() {

    for(int i=0;i<theCfg.getNbItem();i++) {
      labels[i].clearModel();
      theCfg.clearSetFrame(i);

      if(values[i] instanceof SimpleScalarViewer)
        ((SimpleScalarViewer)values[i]).clearModel();
      else if(values[i] instanceof BooleanScalarCheckBoxViewer)
        ((BooleanScalarCheckBoxViewer)values[i]).clearModel();

      if(showCommand) commands[i].setModel((CommandList) null);
    }

    if(propFrame!=null) {
      propFrame.setModel(null);
      propFrame.setVisible(false);
      propFrame.dispose();
      propFrame=null;
    }

  }

}
