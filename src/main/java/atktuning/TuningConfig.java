package atktuning;

import atktuning.Taco.TacoNumberScalar;
import fr.esrf.Tango.*;
import fr.esrf.tangoatk.core.*;
import fr.esrf.tangoatk.widget.util.ErrorHistory;
import fr.esrf.tangoatk.widget.util.ErrorPopup;
import fr.esrf.tangoatk.widget.util.Splash;

public class TuningConfig {

    private fr.esrf.tangoatk.core.AttributeList attList;
    private TuningCmdList[] cmdList = null;
    private String title;
    private int startIndex;
    private int nbItem=0;
    private SettingFrame[] setFrames;

    public TuningConfig(ErrorHistory errWin,AttPanel panel,Splash splashScreen,AttributeList globalList,boolean showCommand,int currentPanel,int nbPanel) {

      attList = globalList;
      startIndex = attList.size();
      String splashMsg = splashScreen.getMessage();

      double totalP = panel.getSize();
      double idx = 0;
      if(showCommand) totalP += panel.getSize();


      // ----------------------- Create the attribute lists ------------------
      title=panel.title;
      for (int i = 0; i < panel.getSize(); i++) {
        splashScreen.setMessage(splashMsg+panel.get(i).attName);
        try {
          if(panel.get(i).isTaco) {
            addTacoAttribute(errWin,panel.get(i).attName,panel.get(i).setCommand,panel.get(i).setName);
          } else {
            attList.add(panel.get(i).attName);
          }
        } catch (ConnectionException e) {
          System.out.println(e.getDescription());
        }
        double prg = ((double)currentPanel + idx/totalP) / (double)nbPanel;
        splashScreen.progress((int)(prg * 100.0));
        idx+=1.0;
      }

      nbItem = attList.size() - startIndex;

      setFrames = new SettingFrame[nbItem];
      for(int i=0;i<nbItem;i++)
        setFrames[i] = null;

      // ----------------------- Create the command lists ------------------
      if (showCommand) {
        cmdList = new TuningCmdList[nbItem];

        for (int i = 0; i < nbItem; i++) {

          if (!panel.get(i).isTaco) {
            String devName = getAtt(i).getDeviceName();
            splashScreen.setMessage(splashMsg + devName);

            // Check wether the command list has already been build
            TuningCmdList newList = TuningCmdList.findList(cmdList, i, devName);
            if (newList == null) {
              cmdList[i] = new TuningCmdList();
              cmdList[i].addErrorListener(ErrorPopup.getInstance());
              cmdList[i].setModel(devName);
            } else {
              cmdList[i] = newList;
            }
          } else {
            cmdList[i] = null;
          }
          double prg = ((double)currentPanel + idx/totalP) / (double)nbPanel;
          splashScreen.progress((int)(prg * 100.0));
          idx+=1.0;
        }
      }

      //Done

    }

    private void addTacoAttribute(ErrorHistory errWin,String attName,String setCommand,String setName) {

      try {
        TacoNumberScalar tacoAtt = atktuning.Taco.TacoDeviceFactory.getInstance().getTacoAttribute(attName,setCommand,setName);
        attList.add(tacoAtt);
      } catch (Exception e) {
        String srcName = Utils.getInstance().getDeviceName(attName);
        errWin.setErrorOccured(
            Utils.getInstance().buildError(srcName, (fr.esrf.TacoApi.TacoException)e, "TacoImportFail", ErrSeverity.PANIC, "TuningConfig()"));
      }

    }

    public void clearSetFrame(int index) {

      if(setFrames[index]!=null) {
        setFrames[index].setVisible(false);
        setFrames[index].clearModel();
        setFrames[index].dispose();
      }

      setFrames[index] = null;

    }

    public void createSettingFrame(int index,String title,INumberScalar model,boolean showBackground) {
      setFrames[index] = new SettingFrame(title,model,showBackground);
    }

    // Return corresponding setting frame
    public SettingFrame getSetFrame(int index) {
      return setFrames[index];
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
    public IAttribute getAtt(int index) {
      return (IAttribute) attList.get(startIndex+index);
    }

    public boolean hasCommand(int index) {
      return cmdList != null && cmdList[index]!=null;
    }

    // Return the model for a command list at a specified  index in the list
    public CommandList getCmds(int index) {
      return cmdList[index];
    }

}
