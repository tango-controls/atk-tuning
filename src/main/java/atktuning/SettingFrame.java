package atktuning;

import fr.esrf.tangoatk.core.INumberScalar;
import fr.esrf.tangoatk.widget.attribute.NumberScalarSetPanel;
import fr.esrf.tangoatk.widget.util.JSmoothLabel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Setter frame
 */
public class SettingFrame extends JFrame implements ActionListener {

  static private Font titleFont = new Font("Dialog", Font.BOLD, 18);
  static private Font setterFont = new Font("Dialog", Font.BOLD, 14);

  JSmoothLabel title;
  NumberScalarSetPanel setPanel;
  JButton dismissButton;

  public SettingFrame(String title,INumberScalar model) {

    getContentPane().setLayout(new BorderLayout());

    this.title = new JSmoothLabel();
    this.title.setFont(titleFont);
    this.title.setText(model.getDeviceName());
    this.title.setBackground(getContentPane().getBackground());
    getContentPane().add(this.title,BorderLayout.NORTH);

    setPanel = new NumberScalarSetPanel();
    //setPanel.setLabelVisible(false);
    setPanel.setAttModel(model);
    setPanel.setFont(setterFont);
    setPanel.setBorder(BorderFactory.createEmptyBorder(5,5,10,5));
    getContentPane().add(setPanel, BorderLayout.CENTER);

    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    getContentPane().add(btnPanel,BorderLayout.SOUTH);

    dismissButton = new JButton("Dismiss");
    dismissButton.addActionListener(this);
    btnPanel.add(dismissButton);

    setTitle(title);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if(e.getSource()==dismissButton) {
      setVisible(false);
    }
  }

  public void clearModel() {
    setPanel.clearModel();
  }

}
