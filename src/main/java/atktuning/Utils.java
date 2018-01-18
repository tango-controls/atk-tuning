package atktuning;

import fr.esrf.Tango.DevError;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.ErrSeverity;
import fr.esrf.tangoatk.core.ATKException;
import fr.esrf.tangoatk.core.AttributeSetException;
import fr.esrf.tangoatk.core.ErrorEvent;

import javax.swing.*;
import java.awt.*;

/**
 * Utility class
 */
public class Utils {

  private static Utils object = null;

  public static Utils getInstance() {
    if(object==null)
      object = new Utils();
    return object;
  }

  public ATKException buildATKException(fr.esrf.TacoApi.TacoException e, String reason, ErrSeverity severity, String where) {

    DevError[] err = new DevError[]{new DevError(reason, severity, e.getErrorString(), where)};
    DevFailed df = new DevFailed(err);
    return new ATKException(df);

  }

  public AttributeSetException buildAttributeSetException(fr.esrf.TacoApi.TacoException e, String reason, ErrSeverity severity, String where) {

    DevError[] err = new DevError[]{new DevError(reason, severity, e.getErrorString(), where)};
    DevFailed df = new DevFailed(err);
    return new AttributeSetException(df);

  }

  public ErrorEvent buildError(String devName, fr.esrf.TacoApi.TacoException e, String reason, ErrSeverity severity, String where) {

    devName = "taco:" + devName;
    ATKException ae = buildATKException(e,reason,severity,where);
    return new ErrorEvent(devName, ae, System.currentTimeMillis());

  }

  public String getAttributeName(String attName) {

    int idx = attName.lastIndexOf('/');
    if (idx != -1) {
      return attName.substring(idx+1);
    } else {
      return "";
    }

  }

  public String getDeviceName(String attName) {

    int idx = attName.lastIndexOf('/');
    if (idx != -1) {
      return attName.substring(0, idx);
    } else {
      return "";
    }

  }

  public String[] getResource(String resName) throws fr.esrf.TacoApi.TacoException {
    fr.esrf.TacoApi.TacoDevice ds = new fr.esrf.TacoApi.TacoDevice(getDeviceName(resName));
    return ds.getResource(getAttributeName(resName));
  }

  public Font parseFont(String name) {

    String fName = "Dialog";
    int style = Font.BOLD;
    int size = 14;

    String[] fields = name.split(",");
    if(fields.length!=3) {
      JOptionPane.showMessageDialog(null,"Invalid font definition\n"+name,"Error",JOptionPane.ERROR_MESSAGE);
    } else {
      fName = fields[0];
      style = Integer.parseInt(fields[1]);
      size = Integer.parseInt(fields[2]);
    }

    return new Font(fName,style,size);

  }

  public void printXtuningConf(String tag) throws Exception {

    fr.esrf.TacoApi.TacoDevice ds = new fr.esrf.TacoApi.TacoDevice("sr/tapp-"+tag+"/config");
    String[] nStr = ds.getResource("NU_OF_PANELS");
    if(nStr == null || nStr.length==0) {
      System.out.println("No tapp application defined for " + tag);
      return;
    }
    if(nStr[0].isEmpty()) {
      System.out.println("No tapp application defined for " + tag);
      return;
    }

    int nbPanel = Integer.parseInt(nStr[0]);

    for(int i=0;i<nbPanel;i++) {
       String pName = "sr/tapp-"+tag+"/panel"+Integer.toString(i + 1);
       System.out.println("#"+getResource(pName+"/label")[0]);
       String[] signals = getResource(pName+"/signals");
       for(int j=0;j<signals.length;j+=4) {
         if(signals[j+2].equalsIgnoreCase("null")) {
           System.out.println("taco:"+signals[j+0]+"/"+signals[j+1]);
         } else {
           System.out.println("taco:"+signals[j+0]+"/"+signals[j+1]+","+signals[j+2]+","+signals[j+3]);
         }
       }
    }

  }


}
