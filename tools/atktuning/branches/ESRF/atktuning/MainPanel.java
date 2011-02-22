/*
 * MainPanel.java
 */
package atktuning;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.print.*;
import java.io.*;
import java.util.*;
import fr.esrf.tangoatk.core.*;
import fr.esrf.tangoatk.widget.device.*;
import fr.esrf.tangoatk.widget.util.*;
import fr.esrf.tangoatk.widget.attribute.*;
import fr.esrf.tangoatk.widget.command.*;
import fr.esrf.tangoatk.core.command.*;

/**
 *
 * @author  pons
 */

class TuningPanel 

public class MainPanel extends JFrame implements IErrorListener,ISetErrorListener {

    // Global variables for MainPanel

    private JSmoothLabel[]            labels;
    private SimpleScalarViewer[]      values;
    private NumberScalarWheelEditor[] setters;    
    private CommandMenuViewer[]       commands;    
    
    private JPanel		      thePanel;
    private JScrollPane		      theView;
    private JTextArea		      errorText;
    private JLabel		      errorLabel;
    private JMenuBar                  mainMenu;
    
    private Font		      theFont;
    private fr.esrf.tangoatk.core.AttributeList attList;

    private String                    theTitle;	    
    private boolean                   runFromShell;
   
    // General constructor
    public MainPanel(String filename) {
      runFromShell=false;
      initComponents(filename);          
    }
    
    public MainPanel(String filename,boolean b) {    
      runFromShell = b;
      initComponents(filename);          
    }
    
    private void initComponents(String filename) {
    	
	int i;
	Splash splashScreen=null;
	    
	if( runFromShell ) {
          splashScreen = new Splash();
          splashScreen.setTitle("AtkTuning");
          splashScreen.setMessage("Reading " + filename + "...");
	  try {
	    Thread.sleep(500);
	  } catch ( Exception e) {
	  }
	}
		
	errorText = new JTextArea();
	errorText.setBorder( BorderFactory.createLoweredBevelBorder() );
	errorText.setEditable(false);
	errorText.setBackground( getBackground() );
	
	errorLabel = new JLabel();
	errorLabel.setText("Error");	

	// Read config file	
	String[] list = readConfigFile(filename);
	if( runFromShell ) splashScreen.setMessage("Creating attributes...");
	
	//Create the attribute list
	attList = new fr.esrf.tangoatk.core.AttributeList();
	//ErrorHistory errorHistory  = new ErrorHistory();
	//errorHistory.setVisible(true);
	//attList.addErrorListener( errorHistory );
	attList.addErrorListener(this);
	attList.addSetErrorListener(this);

	attList.setFilter( new IEntityFilter ()
                         {
                            public boolean keep(IEntity entity)
                            {
                               if (entity instanceof INumberScalar)
                               {
                                 return true;
                               } else {
                                 System.out.println( entity.getName() + " not supported." );
				 return false;
			       }
                            }
                         });
	
	theTitle = list[0];
	setTitle("AtkTuning ["+ theTitle +"]");
        for(i=1;i<list.length;i++) {
	  try {
	    attList.add(list[i]);
	    if( runFromShell ) splashScreen.progress((int)((double)i/(double)list.length * 50.0));
	  } catch ( Exception e ) {
	    System.out.println( list[i] + " failed :" + e.getMessage() );
	  }
	}
	attList.startRefresher();

	// Create the GUI
	getContentPane().setLayout(null);
	thePanel = new JPanel();
	thePanel.setBackground( getBackground() );
	thePanel.setLayout(null);
	thePanel.setBorder(null);
	thePanel.setSize( 470 , attList.size()*30 );	
	thePanel.setPreferredSize( new Dimension(470,attList.size()*30) );	
				
	theFont = new Font("Dialog" , Font.PLAIN , 14);
	
	// Create labels
	labels = new JSmoothLabel[attList.size()];
	for(i=0;i<attList.size();i++) {
	   labels[i] = new JSmoothLabel();
	   labels[i].setFont(theFont);
	   INumberScalar m = (INumberScalar)attList.get(i);
	   labels[i].setText( m.getLabel() );
	   labels[i].setToolTipText( m.getName() );
	   labels[i].setBackground( getBackground() );
	   thePanel.add(labels[i]);
	   labels[i].setBounds( 0 , i*30 , 200 , 28 );
	   labels[i].setHorizontalAlignment( JSmoothLabel.RIGHT_ALIGNMENT );
	}
	
	// Create scalar viewer
	values = new SimpleScalarViewer[attList.size()];
	for(i=0;i<attList.size();i++) {
	   values[i] = new SimpleScalarViewer();
           values[i].setFont(theFont);
           values[i].setBorder(javax.swing.BorderFactory.createLoweredBevelBorder());
           thePanel.add(values[i]);
	   values[i].setBounds( 202 , i*30 , 150 , 28 );
           values[i].setHorizontalAlignment(JAutoScrolledText.CENTER_ALIGNMENT);
	   INumberScalar m = (INumberScalar)attList.get(i);
	   values[i].setModel(m);
	}
	
	// Create WheelSwitchs
	setters = new NumberScalarWheelEditor[attList.size()];
	for(i=0;i<attList.size();i++) {
	   INumberScalar m = (INumberScalar)attList.get(i);
	   if( m.isWriteable() )  {
	     setters[i] = new NumberScalarWheelEditor();
             setters[i].setFont(theFont);
	     setters[i].setBackground( getBackground() );
             thePanel.add(setters[i]);
	     setters[i].setBounds( 354 , i*30 , 80 , 30 );
	     setters[i].setModel(m);
	   }
	}
	
	if( runFromShell ) splashScreen.setMessage("Creating commands...");
	
	// Create Command menu viewer
	commands = new CommandMenuViewer[attList.size()];
	for(i=0;i<attList.size();i++) {
	   INumberScalar m = (INumberScalar)attList.get(i);
	   commands[i] = new CommandMenuViewer();
	   commands[i].setBackground( getBackground() );
           thePanel.add(commands[i]);
	   commands[i].setBounds( 435 , i*30 , 30 , 28 );
	   
	   // Command list
	   fr.esrf.tangoatk.core.CommandList clist =
	     new fr.esrf.tangoatk.core.CommandList();
	   
	   clist.addErrorListener(this);
	   clist.setFilter( new IEntityFilter () {
                            public boolean keep(IEntity entity) {
                               return (entity instanceof VoidVoidCommand);
                            }
                           });
			   
           try {
	     clist.add( extractField(m.getName()) );
	   } catch (Exception e) {
	     System.out.println(e.getMessage());	   
	   }
	   commands[i].setModel(clist);
	   
	   if( runFromShell ) splashScreen.progress((int)((double)i/(double)list.length * 50.0) + 50);	   	   
	}
		
	if( runFromShell ) splashScreen.progress(100);
	
	// Create menu
        mainMenu              = new JMenuBar();
        JMenu     jMenu1      = new JMenu();
        JMenu     jMenu2      = new JMenu();
        JMenuItem jMenuItem1  = new JMenuItem();
        JMenuItem jMenuItem2  = new JMenuItem();
        JMenuItem jMenuItem3  = new JMenuItem();

        jMenu1.setText ("File");
        jMenu2.setText ("Options");
        jMenuItem1.setText ("Exit");
        jMenuItem2.setText ("Set refresh interval");
        jMenuItem3.setText ("Show trend");

        // Exit Application    
        jMenuItem1.addActionListener (new ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {
             exitForm();
          }
        } );

        jMenuItem2.addActionListener (new ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {
	    setRefreshInterval();
          }
        } );
	
        jMenuItem3.addActionListener (new ActionListener () {
          public void actionPerformed (java.awt.event.ActionEvent evt) {
	     JFrame f = new JFrame();
	     Trend graph = new Trend(f);
	     f.setTitle("Trend [" + theTitle + "]");
	     graph.setModel(attList);
	     f.setContentPane(graph);
	     f.pack();
	     f.setSize(640,480);
	     f.setVisible(true);     
          }
        } );
	
	jMenu1.add(jMenuItem1);
	jMenu2.add(jMenuItem2);
	jMenu2.add(jMenuItem3);
	mainMenu.add(jMenu1);
	mainMenu.add(jMenu2);
	setJMenuBar(mainMenu);
	
	theView = new JScrollPane( thePanel );	
	theView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	theView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
	
	getContentPane().add(theView);
	getContentPane().add(errorText);
	getContentPane().add(errorLabel);
		
        addComponentListener( new ComponentListener() {
          public void componentHidden(ComponentEvent e) {}
          public void componentMoved(ComponentEvent e) {}

          public void componentResized(ComponentEvent e) {
            placeComponents();
          }
          public void componentShown(ComponentEvent e) {
            placeComponents();
          }
        });

	addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm();
            }
        });
	
        // Set default size
	int h = attList.size()*30 + 150;
	if( h>768 ) h=768;
        setSize(500,h);
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/atktuning/icon.gif"));
        if(image!=null) setIconImage(image);
        if( runFromShell ) splashScreen.hide();
        setVisible(true);	
    }
    
    // place components
    private void placeComponents() {
      Dimension d = getContentPane().getSize();
            
      theView.setBounds( 5,5,d.width-10,d.height-90 );
      errorLabel.setBounds(5 , d.height-85 , d.width-10 , 15 );
      errorText.setBounds( 5,d.height-70,d.width-10,65 );
      theView.revalidate();

    }
    
    // Get the device name and add a "*"
    public String extractField(String s) {
      int i = s.lastIndexOf('/');
      if( i>0 ) {
        return (s.substring(0,i+1) + "*");
      } else {
        return "";
      }
    }
    
    // Set the refresh interval
    private void setRefreshInterval() {
    
      int old_it = (int)attList.getRefreshInterval();
      String i = JOptionPane.showInputDialog(this,"Enter refresh interval (ms)",(Object)new Integer(old_it));
      if( i!=null ) {
        try {
          int it = Integer.parseInt(i);
	  attList.setRefreshInterval(it);
        } catch ( NumberFormatException e ) {
          errorText.setText( i + " invalid nunber." );      
        }
      }
      
    }
    
    // Read the config file
    private String[] readConfigFile(String filename) {
    	
	FileReader f=null;
	String[] lines;
	ArrayList lst = new ArrayList();
	String s;
	
	// Read the config file
	try {
	  f = new FileReader(filename);
	} catch (FileNotFoundException e) {
	  fatalError( filename + " not found.");
	}
	
	while( (s=readLine(f))!=null ) {
	  lst.add(s);
	}
	
        try {
	  f.close();	
	} catch (IOException e) {
	  System.out.println("Warning " + e.getMessage());
	}
	
	lines = new String[lst.size()];
	for(int i=0;i<lst.size();i++)
	  lines[i] = (String)lst.get(i);
	
	return lines;
    }
    
    // Read one line if the config file
    // return null when file is eneded
    private String readLine(FileReader f) {
    
      int c=0;
      String  result="";
      boolean eor=false;
      
      while( !eor ) {
        try {
	  c = f.read();
	} catch (IOException e) {
	  fatalError( f.toString() + " " + e.getMessage() );
	}
        boolean ok = (c>=32);
	if( ok ) result += (char)c;
	eor = (c==-1) || (!ok && result.length()>0);
      }
      
      if( result.length() > 0 ) return result;
      else                      return null;
            
    }
    
    // Error listener
    public void errorChange(ErrorEvent evt) {
      Object o = evt.getSource();
      if( o instanceof IEntity ) {
        IEntity src = (IEntity)(evt.getSource());
        errorLabel.setText( "Error from " + src.getName() );
      } else {
	Device src = (Device)(evt.getSource());
        errorLabel.setText( "Error from " + src.getName() );
      }
      errorText.setText( evt.getError().getMessage() );
    }

    public void setErrorOccured(ErrorEvent evt) {
      errorChange(evt);
    }

    
    // Fatal error
    private void fatalError(String message) {
      JOptionPane.showMessageDialog(null, message , "Fatal Error", JOptionPane.ERROR_MESSAGE);
      exitForm();
    }
    
    // Exit the Application
    private void exitForm() {
        if( runFromShell ) {
	  System.exit(0);
	} else {
	  hide();
	  dispose();	 
        }
    }
    
    /* Printing stuff */
    public void printParams() {
    
      /*
      java.awt.PageAttributes pa = new java.awt.PageAttributes();
      java.awt.JobAttributes  ja = new java.awt.JobAttributes();
      pa.setPrintQuality(5);
      pa.setPrinterResolution(200);
      pa.setColor(java.awt.PageAttributes.ColorType.COLOR);
      ja.setMaxPage(1);
      ja.setMinPage(1);
      
      java.awt.PrintJob printJob = java.awt.Toolkit.getDefaultToolkit().getPrintJob(this,"Print param",ja,pa);
      
      if( printJob!=null ) {
        java.awt.Graphics g     = printJob.getGraphics();
	g.translate( 100,300 );
	g.setClip(1,1,getSize().width,getSize().height);
        Left_Panel.paint(g);
	g.dispose();
        printJob.end();        
      }
      */
      
    }

    /* main */
    public static void main(String args[]) {
    
      if( args.length < 1 ) {
        System.out.println("Usage: AtkTuning [-?] config_filename");
        System.exit(0);
      }
      
      if( "-?".equals(args[0]) ) {
        System.out.println("Usage: AtkTuning [-?] config_filename");
        System.out.println("  The config file is a list of tango attributes, each line is");
	System.out.println("  an attribute name (ex: eas/test-api/1/Long_attr).");
        System.out.println("  AtkTuning supports only number scalar attributes.");
        System.out.println("  The first line of the config file is the panel title.");	
        System.exit(0);
      }
            

      final MainPanel window = new MainPanel(args[0],true);
	

    }
    
}
