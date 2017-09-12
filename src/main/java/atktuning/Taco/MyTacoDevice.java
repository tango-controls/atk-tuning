package atktuning.Taco;

import fr.esrf.TacoApi.*;
import fr.esrf.tangoatk.core.IDevice;

import java.util.Vector;

public class MyTacoDevice {

  TacoDevice ds;
  TacoDevice dsClass;
  String deviceClass;
  Vector<TacoSignal> signalList;
  double[] values;

  MyTacoDevice(String devName) throws TacoException {

    ds = new TacoDevice(devName);
    ds.setSource(TacoDevice.SOURCE_DEVICE);
    deviceClass = ds.getDeviceClass();
    dsClass = new TacoDevice("CLASS/"+deviceClass+"/DEFAULT");

    // Build signal list
    String[] signals = dsClass.getResource("signal_names");

    // Try from the device only if nothing specified for the class
    if(signals==null || signals.length==0)
      signals = ds.getResource("signal_names");

    signalList = new Vector<TacoSignal>();

    for(int i=0;i<signals.length;i++) {

      TacoSignal s = new TacoSignal();
      s.sigName = signals[i];
      s.label = getSignalProperty(signals[i], "label");
      s.unit = getSignalProperty(signals[i], "unit");
      s.format = getSignalProperty(signals[i], "format");
      s.desc = getSignalProperty(signals[i], "descr");
      s.min = getNumberSignalProperty(signals[i], "min");
      s.max = getNumberSignalProperty(signals[i], "max");
      s.alLow = getNumberSignalProperty(signals[i], "allow");
      s.alHigh = getNumberSignalProperty(signals[i], "alhigh");
      s.device = this;
      s.idx = i;
      signalList.add(s);

    }

    values = new double[signals.length];
    clearValues();

  }

  public double getValue(int idx) {
    return values[idx];
  }

  public void updateSigConfig() throws TacoException {

    ds.command(TacoConst.DevUpdateSigConfig);

  }

  public TacoSignal getSignal(String sigName) throws TacoException {

    int i=0;
    boolean found = false;
    while(!found && i<signalList.size()) {
      found = signalList.get(i).sigName.equalsIgnoreCase(sigName);
      if(!found) i++;
    }
    if(found) {
      return signalList.get(i);
    } else {
      for(i=0;i<signalList.size();i++) {
        System.out.println(signalList.get(i).sigName);
      }
      throw new TacoException(sigName + " signal not found");
    }

  }

  public TacoCommand getCommand(String cmdName) throws TacoException {

    TacoCommand[] cmds = ds.commandQuery();
    int i=0;
    boolean found = false;
    while(!found && i<cmds.length) {
      found = cmds[i].cmdName.equalsIgnoreCase(cmdName);
      if(!found) i++;
    }
    if(found) {
      return cmds[i];
    } else {
      throw new TacoException(cmdName + " command not found");
    }

  }

  public void clearValues() {

    for(int i=0;i<values.length;i++)
      values[i]=Double.NaN;

  }

  public void refreshValues() throws TacoException {

    TacoData data = ds.get(TacoConst.DevReadSigValues);

    switch (data.getType()) {

      case TacoConst.D_VAR_FLOATARR:
        float[] fvals = data.extractFloatArray();
        for(int i=0;i<values.length && i<fvals.length;i++)
          values[i] = fvals[i];
        break;

      case TacoConst.D_VAR_DOUBLEARR:
        double[] dvals = data.extractDoubleArray();
        for(int i=0;i<values.length && i<dvals.length;i++)
          values[i] = dvals[i];
        break;

    }


  }

  public String getName() {
    return ds.getName();
  }

  private Double getNumberSignalProperty(String signalName,String propName) throws TacoException {
    String r = getSignalProperty(signalName,propName);
    double d = Double.NaN;
    try {
      d = Double.parseDouble(r);
    } catch (NumberFormatException e) {}
    return d;
  }

  private String getSignalProperty(String signalName, String propName) throws TacoException {

    // Try from the device
    String propNameFull = signalName + "." + propName;
    String[] res = ds.getResource(propNameFull);
    if(res==null || res.length==0 || res[0].length()==0) {
      // Try from the CLASS definition
      res = dsClass.getResource(propNameFull);
    }

    if(res==null)
      return "";
    else {
      String ret = "";
      for(int i=0;i<res.length;i++) {
        ret += res[i];
        if(i<res.length-1) ret += " ";
      }
      return ret;
    }

  }

}
