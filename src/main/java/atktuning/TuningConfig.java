package atktuning;

import fr.esrf.tangoatk.core.IErrorListener;
import fr.esrf.tangoatk.core.INumberScalar;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.esrf.tangoatk.widget.util.Splash;

public class TuningConfig {

    private fr.esrf.tangoatk.core.AttributeList attList;
    private TuningCmdList[] cmdList;
    private String title;
    private int startIndex;
    private int nbItem=0;

    public TuningConfig(String[] list,boolean runFromShell,Splash splashScreen,fr.esrf.tangoatk.core.AttributeList globalList,Object serr) {
      int i;
      attList = globalList;
      startIndex = attList.size();
      String splashMsg = splashScreen.getMessage();

      // ----------------------- Create the attribute lists ------------------
      title=list[0];
      for (i = 1; i < list.length; i++) {
        splashScreen.setMessage(splashMsg+"Init "+list[i]);
        try {
          attList.add(list[i]);
        } catch (ConnectionException e) {
          System.out.println(e.getDescription());
        }
        splashScreen.progress((int) ((double) i / (double) list.length * 50.0));
      }

      nbItem = attList.size() - startIndex;

      // ----------------------- Create the command lists ------------------
      cmdList = new TuningCmdList[nbItem];

      for (i = 0; i < nbItem; i++) {

        String devName = extractDeviceName(getAtt(i).getName());
        splashScreen.setMessage(splashMsg+"Init "+devName);

        // Check wether the command list has already been build
        TuningCmdList newList = TuningCmdList.findList(cmdList,i,devName);
        if( newList == null ) {
          cmdList[i] = new TuningCmdList();
          cmdList[i].addErrorListener((IErrorListener)serr);
          cmdList[i].setModel(devName);
        } else {
          cmdList[i] = newList;
        }
        splashScreen.progress((int) ((double) i / (double) list.length * 50.0) + 50);
      }

      //Done

    }

    // Return number of item in this panel
    public int getNbItem() {
      return nbItem;
    }

    // Return number of item in this panel
    public String getTitle() {
      return title;
    }

    // Return the model for an attribute at a specified  index in the list
    public INumberScalar getAtt(int index) {
      return (INumberScalar) attList.get(startIndex+index);
    }

    // Return the model for a command list at a specified  index in the list
    public fr.esrf.tangoatk.core.CommandList getCmds(int index) {
      return cmdList[index];
    }

    // Get the device name and add a "*"
    public String extractDeviceName(String s) {
      int i = s.lastIndexOf('/');
      if (i > 0) {
        return s.substring(0, i);
      } else {
        return "";
      }
    }

}
