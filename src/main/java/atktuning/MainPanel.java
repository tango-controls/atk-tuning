/*
 * MainPanel.java
 */
package atktuning;

import java.awt.*;
import java.awt.event.ActionListener;
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

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.ApiUtil;
import fr.esrf.TangoApi.Database;
import fr.esrf.tangoatk.core.AttributePolledList;
import fr.esrf.tangoatk.core.DeviceFactory;
import fr.esrf.tangoatk.core.IEntity;
import fr.esrf.tangoatk.core.IEntityFilter;
import fr.esrf.tangoatk.core.INumberScalar;
import fr.esrf.tangoatk.widget.attribute.Trend;
import fr.esrf.tangoatk.widget.util.*;

/**
 * 
 * @author pons
 */

//
public class MainPanel extends JFrame {

	// Global variables for MainPanel
  final static int MAX_WIDTH = 1400;

	private JPanel thePanel;
	private JScrollPane theView;
	private JMenuBar mainMenu;
	private int nbPanel = 0;
	private TuningPanel[] panels;

	private boolean runFromShell = false;
	private boolean showCommand = false;
  private boolean showSetter = false;
  private boolean showBackground = false;
	private boolean readOnly = false;
  private String fName = "Dialog,1,14";
	private ErrorHistory errWin;
	private final String appVersion = "ATKTuning " + getVersion();
  private Splash splashScreen = null;

	// Keep one attribute list for the whole application
	public AttributePolledList attList = null;

  private atktuning.Taco.TacoDeviceFactory TacoFactory;

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

  public MainPanel(String filename, boolean runningFromShell,
                   boolean showCommand, boolean readOnly, boolean showSetter) {
    this.runFromShell = runningFromShell;
    this.showCommand = showCommand;
    this.readOnly = readOnly;
    this.showSetter = showSetter;
    initComponents(filename);
  }

  public MainPanel(String filename, boolean runningFromShell,
                   boolean showCommand, boolean readOnly, boolean showSetter,String fName,boolean showBackground) {
    this.runFromShell = runningFromShell;
    this.showCommand = showCommand;
    this.readOnly = readOnly;
    this.showSetter = showSetter;
    if(fName!=null) this.fName = fName;
    this.showBackground = showBackground;
    initComponents(filename);
  }

  private void initComponents(String filename) {

    int i;

    errWin = new ErrorHistory();

    TacoFactory = atktuning.Taco.TacoDeviceFactory.getInstance();
    TacoFactory.setErrorWin(errWin);

    splashScreen = new Splash();
    splashScreen.setTitle(appVersion);
    splashScreen.setMessage("Reading " + filename + "...");

    // Let the message appears
    try {
      Thread.sleep(10);
    } catch (InterruptedException e) {
    }

    // Read config file
    setTitle(appVersion + " [" + filename + "]");
    AttPanel[] list = readConfigFile(filename);
    if(list==null) {
      // Fatal error
      return;
    }
    nbPanel = list.length;

    thePanel = new JPanel();
    thePanel.setBackground(getBackground());
    thePanel.setLayout(new GridBagLayout());
    thePanel.setBorder(null);

    // Get the max height
    int maxRows = 0;
    for (i = 0; i < nbPanel; i++)
      if (list[i].getSize() > maxRows)
        maxRows = list[i].getSize();

    // Create the global attribute list
    attList = new AttributePolledList();
    attList.setForceRefresh(true); // We cannot use multiple read if Taco device are present
    attList.addErrorListener(errWin);
    attList.addSetErrorListener(ErrorPopup.getInstance());

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
    int totalWidth = 0;
    boolean cropped = false;
    panels = new TuningPanel[nbPanel];
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridy = 0;

    for (i = 0; i < nbPanel; i++) {

      splashScreen.setMessage("Panel " + (i + 1) + "/" + nbPanel
          + ":");

      // Tuning panel
      TuningConfig cfg = new TuningConfig(errWin,list[i], splashScreen, attList, showCommand,i,nbPanel);
      panels[i] = new TuningPanel(cfg, maxRows, showCommand, readOnly, showSetter, showBackground, fName, this);
      int pWidth = panels[i].getPreferredSize().width;
      if(!cropped && totalWidth + pWidth < MAX_WIDTH) {
        totalWidth += pWidth;
      } else {
        cropped = true;
      }
      gbc.gridx = i;
      thePanel.add(panels[i], gbc);

    }

    attList.setRefreshInterval(2000);
    if(TacoFactory.getDeviceNumber()>0)
      TacoFactory.setRefreshInterval(2000);

    // Start refreshers
    attList.startRefresher();

    if (runFromShell) {
      // Stops the state and status refresher
      DeviceFactory.getInstance().stopRefresher();
    }

    splashScreen.progress(100);

    // Start Taco device refresher
    if(TacoFactory.getDeviceNumber()>0) {

      TacoFactory.getInstance().startRefresher();
      // Wait a bit that the Taco refresher starts
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {}

    }

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
        ATKGraphicsUtils.centerFrameOnScreen(errWin);
        errWin.setVisible(true);
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
    theView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    theView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

    Dimension viewD = theView.getPreferredSize();
    if (cropped) {
      // We need to crop
      theView.setPreferredSize(new Dimension(totalWidth,viewD.height));
    }

    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());
    innerPanel.add(theView, BorderLayout.CENTER);
    setContentPane(innerPanel);

    addWindowListener(new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(java.awt.event.WindowEvent evt) {
        exitForm();
      }
    });

    // Set default size
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Image image = toolkit.getImage(getClass().getResource(
        "/atktuning/icon.gif"));
    if (image != null) setIconImage(image);
    splashScreen.setVisible(false);
    ATKGraphicsUtils.centerFrameOnScreen(this);
    setVisible(true);

  }

  private void showTrendAll() {

		JFrame f = new JFrame();
		Trend graph = new Trend(f);
		f.setTitle("Trends");
		graph.setModel(attList);
		f.setContentPane(graph);
		f.pack();
		f.setPreferredSize(new Dimension(640, 480));
    ATKGraphicsUtils.centerFrameOnScreen(f);
		f.setVisible(true);

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
        if(TacoFactory.getDeviceNumber()>0)
          TacoFactory.setRefreshInterval(it);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, i + " invalid nunber.",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		}

	}

  private AttItem getItem(String s) {

    AttItem item = new AttItem();
    if(s.startsWith("taco:")) {
      item.isTaco = true;
      String tacoDef = s.substring(5).trim();
      String[] fields = tacoDef.split(",");
      if(fields.length<=1) {
        item.attName = tacoDef;
      } else {
        item.attName = fields[0];
        item.isSettable = true;
        item.setCommand = fields[1];
        item.setName = fields[2];
      }
    } else {
      item.attName = s;
    }

    return item;

  }

	// Read the config file
	public AttPanel[] readConfigFile(String filename) {

		FileReader f = null;
		String s;
		Vector<AttPanel> items = new Vector<AttPanel>();

		// Read the config file
		try {

			f = new FileReader(filename);

		} catch (FileNotFoundException e) {

      // Try from default directory
      Database db = null;
      String path = null;
      try {
        db = ApiUtil.get_db_obj();
        path = db.get_property("AtkTuning","path").extractString();
      } catch (DevFailed e1) {
        splashScreen.setVisible(false);
        fatalError(e1.errors[0].desc);
        return null;
      }

      try {
        f = new FileReader(path+"/"+filename);
      } catch (FileNotFoundException e2) {
        splashScreen.setVisible(false);
        fatalError(filename + " not found.");
        return null;
      }

		}

		s = readLine(f);
		if (s != null) {

			if (s.startsWith("#")) {

				// ================ Version 2
				boolean eof = false;
				while (!eof) {
          AttPanel panel = new AttPanel();

					// Extract Title
					String title = s.substring(1);
          panel.title = title;

					boolean eop = false;
					while (!eop) {
						s = readLine(f);
						if (s != null) {
							eop = s.startsWith("#");
							if (!eop) {
                AttItem item = getItem(s);
                panel.items.add(item);
              }
						} else {
							eof = true;
							eop = true;
						}
					}
					items.add(panel);

				}

			} else {

				// =================== Version 1
        AttPanel panel = new AttPanel();
				panel.title=s;
				while ((s = readLine(f)) != null) {
          AttItem item = getItem(s);
          panel.items.add(item);
        }
				items.add(panel);

			}

		}

		try {
			f.close();
		} catch (IOException e) {
			System.out.println("Warning " + e.getMessage());
		}

    if(items.size()==0) {
      splashScreen.setVisible(false);
      fatalError(filename + " is not an atktuning configuration file.");
      return null;
    }

		// Build the returned array
		int i;
		AttPanel[] ret = new AttPanel[items.size()];
		for (i = 0; i < items.size(); i++) {
			ret[i] = items.get(i);
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

	// Fatal error
	private void fatalError(String message) {
    splashScreen.setVisible(false);
		JOptionPane.showMessageDialog(null, message, "Fatal Error",
				JOptionPane.ERROR_MESSAGE);
		exitForm();
	}

	// Exit the Application
	private void exitForm() {
		if (runFromShell) {
			System.exit(0);
		} else {

      System.out.println("Clear model");
      for(int i=0;i<nbPanel;i++)
        panels[i].clearModel();

      System.out.println("Clear attList");
			if (attList != null) {
				attList.stopRefresher();
        attList.removeErrorListener(errWin);
        attList = null;
			}

      System.out.println("Clear Taco");
      atktuning.Taco.TacoDeviceFactory.getInstance().stopRefresher();
			setVisible(false);

      System.out.println("Done");
			dispose();
		}

	}

  public static String getVersion() {
    Package p = MainPanel.class.getPackage();

    //if version is set in MANIFEST.mf
    if(p.getImplementationVersion() != null) return p.getImplementationVersion();

    return "*.*";
  }

  private static void printUsage() {
    System.out.println("Usage: atktuning [-conv res_tag] [-h] [-cmd] [-ro] [-w] [-f fontname] [-sb] config_filename");
  }

  private static void printHelp() {

    System.out
        .println("  -conv res_tag : Build a configuration file from TACO resource");
    System.out
        .println("  -cmd : Show command menu");
    System.out
        .println("  -ro : Read only mode");
    System.out
        .println("  -w : Display editor in the panel");
    System.out
        .println("  -f fontname : Font used by viewer Name,style,size (ex -f Dialog,1,14) 0=PLAIN 1=BOLD 2=ITALIC");
    System.out
        .println("  -sb : Show background when attribute is valid");
    System.out
        .println("");
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

  }

	/* main */
	public static void main(String args[]) {

    String fName = null;

		if (args.length < 1) {
      printUsage();
			System.exit(0);
		}

    if("-conv".equals(args[0])) {
      if(args.length<2) {
        printUsage();
        System.out.println("Resource TAG expected.");
        System.exit(0);
      }
      try {
        Utils.getInstance().printXtuningConf(args[1]);
      } catch (Exception e) {
        System.out.print(e.getMessage());
      }
      System.exit(0);
    }

		if ("-h".equals(args[0])) {
      printUsage();
      printHelp();
			System.exit(0);
		}

		// Check parameters
		int index = 0;
		boolean showCommand = "-cmd".equals(args[index]);
		if (showCommand) index++;
		boolean roMode = "-ro".equals(args[index]);
		if (roMode)	index++;
    boolean showEditor = "-w".equals(args[index]);
    if (showEditor)	index++;
    if("-f".equals(args[index])) {
      index++;
      if(index>=args.length){
        printUsage();
        System.exit(0);
      }
      fName = args[index];
      index++;
    }
    boolean showBackground = "-sb".equals(args[index]);
    if (showBackground)	index++;

		if (args.length < index + 1) {
      printUsage();
			System.exit(0);
		}

		new MainPanel(args[index], true, showCommand, roMode, showEditor,fName,showBackground);

	}

}
