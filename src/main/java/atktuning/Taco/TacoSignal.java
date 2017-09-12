package atktuning.Taco;

import fr.esrf.TacoApi.TacoCommand;
import fr.esrf.TacoApi.TacoConst;
import fr.esrf.TacoApi.TacoData;
import fr.esrf.TacoApi.TacoException;
import fr.esrf.tangoatk.core.attribute.NumberScalar;
import fr.esrf.tangoatk.widget.attribute.NumberScalarViewer;

/**
 * Taco signal class
 */
public class TacoSignal {

    String sigName;
    String unit;
    String format;
    String label;
    String desc;
    double min;
    double max;
    double alLow;
    double alHigh;
    int idx;
    MyTacoDevice device;

    // For READ/WRITE signal
    TacoSignal setSignal;
    String setCommandName;
    private TacoCommand setCommand;

    public TacoSignal() {
      setSignal = null;
      setCommandName = null;
      setCommand = null;
    }

    public double getValue() {
      return device.getValue(idx);
    }

    public void setValue(double set) throws TacoException {

      if(setCommandName!=null) {

        if(setCommand==null)
          setCommand = device.getCommand(setCommandName);

        TacoData argin = new TacoData();
        switch(setCommand.inType) {
          case TacoConst.D_FLOAT_TYPE:
            argin.insert((float)set);
            break;
          case TacoConst.D_DOUBLE_TYPE:
            argin.insert(set);
            break;
          case TacoConst.D_LONG_TYPE:
            argin.insert((int)set);
            break;
          case TacoConst.D_SHORT_TYPE:
            argin.insert((short)set);
            break;
          default:
            throw new TacoException("Type not supported for " + setCommandName);
        }

        device.ds.put(setCommand.cmdCode,argin);

        // Update value in cache
        if( setSignal!=null )
          device.values[setSignal.idx]=set;

      }

    }

    public String getFullName() {
      return device.getName() + "/" + sigName;
    }

    public double getSetValue() {
      if(setSignal!=null)
        return device.getValue(setSignal.idx);
      else
        return Double.NaN;
    }

    public boolean isWritable() {
      return setCommandName != null;
    }

    public boolean hasWriteValue() {
      return setSignal!=null;
    }

}
