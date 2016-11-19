/*
 * MainPanel.java
 */
package atktuning;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import fr.esrf.tangoatk.core.ATKException;
import fr.esrf.tangoatk.core.AttributePolledList;
import fr.esrf.tangoatk.core.Device;
import fr.esrf.tangoatk.core.DeviceFactory;
import fr.esrf.tangoatk.core.ErrorEvent;
import fr.esrf.tangoatk.core.IEntity;
import fr.esrf.tangoatk.core.IEntityFilter;
import fr.esrf.tangoatk.core.IErrorListener;
import fr.esrf.tangoatk.core.INumberScalar;
import fr.esrf.tangoatk.core.ISetErrorListener;
import fr.esrf.tangoatk.widget.attribute.Trend;
import fr.esrf.tangoatk.widget.util.ErrorHistory;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.JSmoothLabel;
import fr.esrf.tangoatk.widget.util.Splash;

/**
 * 
 * @author pons
 */

//
public class MainPanel extends JFrame implements IErrorListener,
		ISetErrorListener {

	// Global variables for MainPanel

	private JPanel thePanel;
	private JScrollPane theView;
	private JMenuBar mainMenu;
	private Font theFont=null;
	private Font titleFont=null;
	private int nbPanel = 0;
	private TuningPanel[] panels;

	private final boolean runFromShell;
	private final boolean showCommand;
	private final boolean readOnly;
	ErrorHistory errorHistory;
	private final String appVersion = "AtkTuning 3.0";

	// Keep one attribute list for the whole application
	public AttributePolledList attList = null;

	// General constructor
	public MainPanel() {
		showCommand = false;
		readOnly = false;
		runFromShell = false;
	}

	public MainPanel(String filename) {
		this(filename, false);
	}

	public MainPanel(String filename, boolean runningFromShell) {
		this(filename, runningFromShell, true);
	}

	public MainPanel(String filename, boolean runningFromShell,
			boolean showCommand) {
		this(filename, runningFromShell, true, false);
	}

	public MainPanel(String filename, boolean runningFromShell,
			boolean showCommand, boolean readOnly) {
		this.runFromShell = runningFromShell;
		this.showCommand = showCommand;
		this.readOnly = readOnly;
		initComponents(filename);
	}

	private void initComponents(String filename) {

		int i;
		Splash splashScreen = null;

		try {

			errorHistory = new ErrorHistory();

			splashScreen = new Splash();
			splashScreen.setTitle(appVersion);
			splashScreen.setMessage("Reading " + filename + "...");

			// Let the message appears
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}

			getContentPane().setLayout(null);

			// Read config file
			setTitle(appVersion + " [" + filename + "]");
			String[][] list = readConfigFile(filename);
			nbPanel = list.length;
			theFont = new Font("Lucida Bright", Font.PLAIN, 16);
			titleFont = new Font("Dialog", Font.BOLD, 16);

			thePanel = new JPanel();
			thePanel.setBackground(getBackground());
			thePanel.setLayout(new BorderLayout());
			thePanel.setBorder(null);

			// Get the max height
			int maxRows = 0;
			for (i = 0; i < nbPanel; i++)
				if (list[i].length > maxRows)
          maxRows = list[i].length;

			// Create the global attribute list
			attList = new AttributePolledList();
			attList.addErrorListener(errorHistory);
			attList.addSetErrorListener(this);

			IEntityFilter attfilter = new IEntityFilter() {
				public boolean keep(IEntity entity) {
					if (entity instanceof INumberScalar) {
						return true;
					} else {
						System.out
								.println(entity.getName() + " not supported.");
						return false;
					}
				}
			};

			attList.setFilter(attfilter);

			// Create panels
			panels = new TuningPanel[nbPanel];

			for (i = 0; i < nbPanel; i++) {

				splashScreen.setMessage("Panel " + (i + 1) + "/" + nbPanel
						+ ":");

        // Tuning panel
				TuningConfig cfg = new TuningConfig(list[i], runFromShell,
						splashScreen, attList, this);
				panels[i] = new TuningPanel(cfg, theFont, titleFont, maxRows,
						showCommand, readOnly, this);
				thePanel.add(panels[i]);

			}

			// Start refreshers
			attList.startRefresher();

			if (runFromShell) {
				// Stops the state and status refresher
				DeviceFactory.getInstance().stopRefresher();
			}

			splashScreen.progress(100);

			// Create menu
			mainMenu = new JMenuBar();
			JMenu jMenu1 = new JMenu();
			JMenu jMenu2 = new JMenu();
			JMenu jMenu3 = new JMenu();
			JMenuItem jMenuItem1 = new JMenuItem();
			JMenuItem jMenuItem2 = new JMenuItem();
			JMenuItem jMenuItem3 = new JMenuItem();

			jMenu1.setText("File");
			jMenu2.setText("Options");
			jMenu3.setText("Trends");
			jMenuItem1.setText("Exit");
			jMenuItem2.setText("Set refresh interval");
			jMenuItem3.setText("View errors");

			// Trend menu

			JMenuItem jTrendMenuItem = new JMenuItem();
			jTrendMenuItem.setText("Show trends");
			jTrendMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					showTrendAll();
				}
			});
			jMenu3.add(jTrendMenuItem);

			// Exit Application
			jMenuItem1.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					exitForm();
				}
			});

			jMenuItem2.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					setRefreshInterval();
				}
			});

			jMenuItem3.addActionListener(new ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent evt) {
					errorHistory.setVisible(true);
				}
			});

			jMenu1.add(jMenuItem1);
			jMenu2.add(jMenuItem2);
			jMenu2.add(jMenuItem3);
			mainMenu.add(jMenu1);
			mainMenu.add(jMenu2);
			mainMenu.add(jMenu3);
			setJMenuBar(mainMenu);

			theView = new JScrollPane(thePanel);
			theView
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			theView
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

			getContentPane().add(theView);

			addComponentListener(new ComponentListener() {
				public void componentHidden(ComponentEvent e) {
				}

				public void componentMoved(ComponentEvent e) {
				}

				public void componentResized(ComponentEvent e) {
					placeComponents();
				}

				public void componentShown(ComponentEvent e) {
					placeComponents();
				}
			});

			addWindowListener(new java.awt.event.WindowAdapter() {
				@Override
				public void windowClosing(java.awt.event.WindowEvent evt) {
					exitForm();
				}
			});

			// Set default size
			pack();
			Dimension d = thePanel.getPreferredSize();

			int h = d.height + 85;
			if (h > 768)
				h = 768;
			int w = d.width + 45;
			if (w > 1024)
				w = 1024;

			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Dimension scrsize = toolkit.getScreenSize();
			Dimension appsize = new Dimension(w, h);
			int x = (scrsize.width - appsize.width) / 2;
			int y = (scrsize.height - appsize.height) / 2;
			setBounds(x, y, appsize.width, appsize.height);

			Image image = toolkit.getImage(getClass().getResource(
					"/atktuning/icon.gif"));
			if (image != null)
				setIconImage(image);
			splashScreen.hide();
			setVisible(true);

		} catch (Exception e) {

			// When exception occurs in the contructor
			// does not forget to hide the splashScreen.
			splashScreen.hide();
			ATKException ae = new ATKException(e);
			ErrorPane.showErrorMessage(null, "AtkTuning", ae);

		}

	}

	private void showTrendAll() {

		JFrame f = new JFrame();
		Trend graph = new Trend(f);
		f.setTitle("Trends");
		graph.setModel(attList);
		f.setContentPane(graph);
		f.pack();
		f.setSize(640, 480);
		f.setVisible(true);

	}

	// place components
	private void placeComponents() {
		Dimension d = getContentPane().getSize();
		theView.setBounds(5, 5, d.width - 10, d.height - 10);
		theView.revalidate();
	}

	// Set the refresh interval
	private void setRefreshInterval() {

		if (nbPanel <= 0)
			return;

		int old_it = attList.getRefreshInterval();
		String i = JOptionPane.showInputDialog(this,
				"Enter refresh interval (ms)", new Integer(old_it));
		if (i != null) {
			try {
				int it = Integer.parseInt(i);
				attList.setRefreshInterval(it);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, i + " invalid nunber.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	// Read the config file
	public String[][] readConfigFile(String filename) {

		FileReader f = null;
		String s;
		Vector items = new Vector();

		// Read the config file
		try {
			f = new FileReader(filename);
		} catch (FileNotFoundException e) {
			fatalError(filename + " not found.");
		}

		s = readLine(f);
		if (s != null) {

			if (s.startsWith("#")) {

				// ================ Version 2
				Vector attlist;
				boolean eof = false;
				while (!eof) {

					attlist = new Vector();
					// Extract Title
					String title = s.substring(1);
					attlist.add(title);

					boolean eop = false;
					while (!eop) {
						s = readLine(f);
						if (s != null) {
							eop = s.startsWith("#");
							if (!eop)
								attlist.add(s);
						} else {
							eof = true;
							eop = true;
						}
					}
					items.add(attlist);

				}

			} else {

				// =================== Version 1
				Vector attlist = new Vector();
				attlist.add(s);
				while ((s = readLine(f)) != null)
					attlist.add(s);
				items.add(attlist);

			}

		}

		try {
			f.close();
		} catch (IOException e) {
			System.out.println("Warning " + e.getMessage());
		}

		// Build the returned string array
		int i, j;
		String[][] ret = new String[items.size()][];
		for (i = 0; i < items.size(); i++) {
			Vector lst = (Vector) items.get(i);
			ret[i] = new String[lst.size()];
			for (j = 0; j < lst.size(); j++)
				ret[i][j] = (String) lst.get(j);
		}

		return ret;
	}

	// Read one line if the config file
	// return null when file is eneded
	private String readLine(FileReader f) {

		int c = 0;
		String result = "";
		boolean eor = false;

		while (!eor) {
			try {
				c = f.read();
			} catch (IOException e) {
				fatalError(f.toString() + " " + e.getMessage());
			}
			boolean ok = (c >= 32);
			if (ok)
				result += (char) c;
			eor = (c == -1) || (!ok && result.length() > 0);
		}

		if (result.length() > 0)
			return result;
		else
			return null;

	}

	public String getErrorSource(ErrorEvent evt) {
		Object o = evt.getSource();
		if (o instanceof IEntity) {
			IEntity src = (IEntity) o;
			return "Error from " + src.getName();
		} else if (o instanceof Device) {
			Device src = (Device) o;
			return "Error from " + src.getName();
		} else if (o instanceof String) {
			return "Error from " + (String) o;
		} else {
			return "Error from unkown source :" + evt.getSource();
		}
	}

	public void errorChange(ErrorEvent evt) {
		setErrorOccured(evt);
	}

	public void setErrorOccured(ErrorEvent evt) {
		evt.getError().printStackTrace();
		JOptionPane.showMessageDialog(this, evt.getError().getMessage(),
				getErrorSource(evt), JOptionPane.ERROR_MESSAGE);
	}

	// Fatal error
	private void fatalError(String message) {
		JOptionPane.showMessageDialog(null, message, "Fatal Error",
				JOptionPane.ERROR_MESSAGE);
		exitForm();
	}

	// Exit the Application
	private void exitForm() {
		if (runFromShell) {
			System.exit(0);
		} else {
			if (attList != null) {
				attList.stopRefresher();
				attList = null;
			}
			hide();
			dispose();
		}
	}

	/* Printing stuff */
	public void printParams() {

		/*
		 * java.awt.PageAttributes pa = new java.awt.PageAttributes();
		 * java.awt.JobAttributes ja = new java.awt.JobAttributes();
		 * pa.setPrintQuality(5); pa.setPrinterResolution(200);
		 * pa.setColor(java.awt.PageAttributes.ColorType.COLOR);
		 * ja.setMaxPage(1); ja.setMinPage(1);
		 * 
		 * java.awt.PrintJob printJob =
		 * java.awt.Toolkit.getDefaultToolkit().getPrintJob
		 * (this,"Print param",ja,pa);
		 * 
		 * if( printJob!=null ) { java.awt.Graphics g = printJob.getGraphics();
		 * g.translate( 100,300 );
		 * g.setClip(1,1,getSize().width,getSize().height); Left_Panel.paint(g);
		 * g.dispose(); printJob.end(); }
		 */

	}

	/* main */
	public static void main(String args[]) {

		if (args.length < 1) {
			System.out
					.println("Usage: AtkTuning [-?] [-nocmd] [-ro] config_filename");
			System.exit(0);
		}

		if ("-?".equals(args[0])) {
			System.out.println("Usage: AtkTuning [-?] config_filename");
			System.out
					.println("  The config file is a list of tango attributes, each line is");
			System.out
					.println("  an attribute name (ex: eas/test-api/1/Long_attr).");
			System.out
					.println("  AtkTuning supports only number scalar attributes.");
			System.out
					.println("  The first line of the config file is the panel title.");
			System.out
					.println("  Since the version 2.0, AtkTuning also supports multiple panels");
			System.out.println("  Configuration file examples:");
			System.out
					.println("   Version1                      Version2 (AtkTunig >2.0)");
			System.out.println("  Test panel                     #Test panel1");
			System.out
					.println("  jlp/test/1/att_un              jlp/test/1/att_un");
			System.out
					.println("  jlp/test/1/att_deux            jlp/test/1/att_deux");
			System.out
					.println("  jlp/test/1/att_trois           #Test panel 2");
			System.out
					.println("  jlp/test/1/att_quatre          jlp/test/1/att_trois");
			System.out
					.println("                                 jlp/test/2/att_un");
			System.exit(0);
		}

		// Check parameters
		int index = 0;
		boolean noCommand = "-nocmd".equals(args[index]);
		if (noCommand)
			index++;
		boolean roMode = "-ro".equals(args[index]);
		if (roMode)
			index++;

		if (args.length < index + 1) {
			System.out
					.println("Usage: AtkTuning [-?] [-nocmd] [-ro] config_filename");
			System.exit(0);
		}

		new MainPanel(args[index], true, !noCommand, roMode);

	}

}
