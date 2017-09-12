package atktuning.Taco;

import fr.esrf.TacoApi.*;
import fr.esrf.Tango.ErrSeverity;
import fr.esrf.tangoatk.widget.util.ErrorHistory;

import java.util.Vector;

/**
 * Taco device factory (Singleton class)
 */

public class TacoDeviceFactory {

  private static TacoDeviceFactory factory = null;
  private Vector<MyTacoDevice> ds;
  private long  refreshInterval = 2000;
  private  boolean isRefresherRunning;
  private Thread refresher;
  private ErrorHistory errWin;

  private TacoDeviceFactory() {
    ds = new Vector<MyTacoDevice>();
    isRefresherRunning = false;
    refresher = null;
    errWin = null;
  }

  public static TacoDeviceFactory getInstance() {
    if(factory==null) factory = new TacoDeviceFactory();
    return factory;
  }

  private int getDeviceIndex(String name) {

    boolean found = false;
    int i = 0;
    while(!found && i<ds.size()) {
      found = ds.get(i).getName().equalsIgnoreCase(name);
      if(!found) i++;
    }

    if(found)
      return i;
    else
      return -1;

  }

  // Import a taco device a return the handle
  public MyTacoDevice getDevice(String devName) throws TacoException {

    int idx = getDeviceIndex(devName);
    if(idx<0) {
      // Import
      MyTacoDevice d = new MyTacoDevice(devName);
      ds.add(d);
      return d;
    } else {
      return ds.get(idx);
    }

  }

  public int getDeviceNumber() {
    return ds.size();
  }

  public TacoNumberScalar getTacoAttribute(String attName) throws TacoException {
    return getTacoAttribute(attName,null,null);
  }

  public TacoNumberScalar getTacoAttribute(String attName,String setCommand,String setAttName) throws TacoException {

    String devName = atktuning.Utils.getInstance().getDeviceName(attName);
    MyTacoDevice ds = getInstance().getDevice(devName);
    String att = atktuning.Utils.getInstance().getAttributeName(attName);
    TacoSignal sig = ds.getSignal(att);
    sig.setCommandName = setCommand;
    if( setAttName!=null ) sig.setSignal = ds.getSignal(setAttName);
    return new TacoNumberScalar(sig);

  }

  public void setErrorWin(ErrorHistory w) {
    errWin = w;
  }

  public void setRefreshInterval(long ms) {
    refreshInterval = ms;
  }

  public void stopRefresher() {

    if(isRefresherRunning) {
      isRefresherRunning = false;
      try {
        refresher.join();
      } catch (InterruptedException e) {}
      refresher = null;
    }

  }

  public void startRefresher() {

    if (isRefresherRunning) return;
    isRefresherRunning = true;

    refresher = new Thread(new Runnable() {
      @Override
      public void run() {

        while(isRefresherRunning) {

          // Refresh all devices
          long t0 = System.currentTimeMillis();
          try {
            for (int i = 0; i < ds.size() && isRefresherRunning; i++) {
              MyTacoDevice d = ds.get(i);
              try {
                d.refreshValues();
              } catch (TacoException e) {
                d.clearValues();
                errWin.setErrorOccured(
                    atktuning.Utils.getInstance().buildError(d.getName(), e, "TacoReadFail", ErrSeverity.ERR, "TacoDevice.refreshValues()"));
              }
            }
          } catch (Exception e2) {
            System.out.println("TacoDeviceFactory.refresher(): Unexpected exception : " + e2.getMessage());
          }
          long t1 = System.currentTimeMillis();

          // Sleep
          long toSleep = refreshInterval - (t1-t0);
          if(toSleep>0) {
            try {
              Thread.sleep(toSleep);
            } catch (InterruptedException e) {}
          }

        }

      }
    });
    refresher.start();

  }


}
