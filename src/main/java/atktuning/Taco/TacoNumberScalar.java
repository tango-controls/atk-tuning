package atktuning.Taco;

import atktuning.Utils;
import fr.esrf.Tango.*;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.TangoApi.events.TangoChangeEvent;
import fr.esrf.TangoApi.events.TangoPeriodicEvent;
import fr.esrf.tangoatk.core.*;
import fr.esrf.tangoatk.core.Device;
import fr.esrf.tangoatk.core.attribute.NumberScalar;
import fr.esrf.tangoatk.widget.util.ErrorPane;

import java.util.Map;

public class TacoNumberScalar extends NumberScalar {

  TacoSignal tacoSignal;

  public TacoNumberScalar(TacoSignal signal) {
    this.tacoSignal = signal;
    propertyStorage = new PropertyStorage();
    updateConfiguration();
  }

  public void updateConfiguration() {

    propertyStorage.setProperty(this, "unit", tacoSignal.unit, true);
    propertyStorage.setProperty(this, "data_format", AttrDataFormat._SCALAR, true);
    propertyStorage.setProperty(this, "format", tacoSignal.format, true);
    propertyStorage.setProperty(this, "data_type", new Integer(Tango_DEV_DOUBLE), true);
    propertyStorage.setProperty(this, "description", tacoSignal.desc, true);
    propertyStorage.setProperty(this, "label", tacoSignal.label, true);
    if(tacoSignal.isWritable())
      propertyStorage.setProperty(this, "writable", AttrWriteType.READ_WRITE, true);
    else
      propertyStorage.setProperty(this, "writable", AttrWriteType.READ, true);
    propertyStorage.setProperty(this, "writable_attr_name","", false);
    propertyStorage.getProperty("writable_attr_name").setSpecified(false);
    propertyStorage.setProperty(this, "display_unit", new Double(1.0), false);
    propertyStorage.setProperty(this, "max_dim_x", new Integer(1), true);
    propertyStorage.setProperty(this, "max_dim_y", new Integer(1), true);
    propertyStorage.setProperty(this, "level", DispLevel.OPERATOR, true);
    propertyStorage.setProperty(this, "standard_unit", new Double(1.0), false);
    propertyStorage.setProperty(this, "min_value", new Double(tacoSignal.min) , true);
    propertyStorage.setProperty(this, "max_value", new Double(tacoSignal.max) , true);
    propertyStorage.setProperty(this, "min_alarm", new Double(tacoSignal.alLow) , true);
    propertyStorage.setProperty(this, "max_alarm", new Double(tacoSignal.alHigh) , true);
    propertyStorage.setProperty(this, "min_warning", new Double(Double.NaN) , true);
    propertyStorage.setProperty(this, "max_warning", new Double(Double.NaN) , true);
    propertyStorage.setProperty(this, "delta_t", new Double(Double.NaN) , true);
    propertyStorage.setProperty(this, "delta_val", new Double(Double.NaN) , true);

    propertyStorage.refreshProperties();

  }

    @Override
  public Number getNumber() {
    return tacoSignal.getValue();
  }

  @Override
  public void setNumber(Number number) {
    double v = number.doubleValue();
    setValue(v);
  }

  @Override
  public void addNumberScalarListener(INumberScalarListener l) {
    propChanges.addNumberScalarListener(l);
    addStateListener(l);
  }

  @Override
  public void removeNumberScalarListener(INumberScalarListener l) {
    propChanges.removeNumberScalarListener(l);
    removeStateListener(l);
  }

  @Override
  public double getNumberScalarValue() {
    return getNumber().doubleValue();
  }

  @Override
  public double getNumberScalarDeviceValue() {
    return getNumberScalarValue();
  }

  @Override
  public double getNumberScalarStandardValue() {
    return getNumberScalarValue();
  }

  @Override
  public double getNumberScalarSetPoint() {
    if(tacoSignal.hasWriteValue())
      return tacoSignal.getSetValue();
    else
      return Double.NaN;
  }

  @Override
  public double getNumberScalarDeviceSetPoint() {
    return getNumberScalarSetPoint();
  }

  @Override
  public double getNumberScalarStandardSetPoint() {
    return getNumberScalarSetPoint();
  }

  @Override
  public double getNumberScalarSetPointFromDevice() {
    return getNumberScalarSetPoint();
  }

  @Override
  public void setValue(double v) {
    try {
      tacoSignal.setValue(v);
    } catch (fr.esrf.TacoApi.TacoException e) {
      propChanges.fireSetErrorEvent(this, Utils.getInstance().buildAttributeSetException(e,"TacoCmdFailed", ErrSeverity.ERR,"TacoNumberScalar.setValue()"));
    }
  }

  @Override
  public INumberScalarHistory[] getNumberScalarHistory() {
    return new INumberScalarHistory[0];
  }

  @Override
  public INumberScalarHistory[] getNumberScalarDeviceHistory() {
    return new INumberScalarHistory[0];
  }

  @Override
  public void setPossibleValues(double[] doubles) {
  }

  @Override
  public double[] getPossibleValues() {
    return new double[0];
  }

  @Override
  public IScalarAttribute getWritableAttribute() {
    return null;
  }

  @Override
  public IScalarAttribute getReadableAttribute() {
    return null;
  }

  @Override
  public String getState() {
    return IAttribute.VALID;
  }

  @Override
  public String getType() {
    return "DoubleScalar";
  }

  @Override
  public boolean isWritable() {
    return tacoSignal.isWritable();
  }

  @Override
  public void dispatch(DeviceAttribute deviceAttribute) {
  }

  @Override
  public void dispatchError(DevFailed devFailed) {
  }

  @Override
  public void addStateListener(IAttributeStateListener l) {
    propChanges.addAttributeStateListener(l);
  }

  @Override
  public void removeStateListener(IAttributeStateListener l) {
    propChanges.removeAttributeStateListener(l);
  }

  @Override
  public void addSetErrorListener(ISetErrorListener l) {
    propChanges.addSetErrorListener(l);
  }

  @Override
  public void removeSetErrorListener(ISetErrorListener l) {
    propChanges.removeSetErrorListener(l);
  }

  @Override
  public int getMaxXDimension() {
    return 1;
  }

  @Override
  public int getMaxYDimension() {
    return 1;
  }

  @Override
  public int getXDimension() {
    return 1;
  }

  @Override
  public int getYDimension() {
    return 1;
  }

  @Override
  public int getHeight() {
    return 1;
  }

  @Override
  public int getWidth() {
    return 1;
  }

  @Override
  public boolean hasEvents() {
    return false;
  }

  @Override
  public boolean areAttPropertiesLoaded() {
    return false;
  }

  @Override
  public void loadAttProperties() {
  }

  @Override
  public String getName() {
    return tacoSignal.getFullName();
  }

  @Override
  public String getNameSansDevice() {
    return tacoSignal.sigName;
  }

  @Override
  public Property getProperty(String name) {
    return propertyStorage.getProperty(name);
  }

  @Override
  public Map getPropertyMap() {
    return propertyStorage.getPropertyMap();
  }

  @Override
  public void addErrorListener(IErrorListener l) {
    propChanges.addErrorListener(l);

  }

  @Override
  public void removeErrorListener(IErrorListener l) {
    propChanges.removeErrorListener(l);
  }

  @Override
  public Device getDevice() {
    return null;
  }

  private void updateNumberRes(TacoSignal sig,String resName,double resValue,double oldValue) {

    fr.esrf.TacoApi.TacoDevice ds = sig.device.ds;

    try {
      if (Double.isNaN(resValue)) {
        ds.delResource(sig.sigName + resName);
      } else if (resValue != oldValue) {
        ds.putResource(sig.sigName + resName, new String[]{Double.toString(resValue)});
      }
    } catch (fr.esrf.TacoApi.TacoException e) {
      ATKException ae = Utils.getInstance().buildATKException(e, "TacoPutResourceFail", ErrSeverity.ERR, "TacoNumberScalar.storeConfig()");
      ErrorPane.showErrorMessage(null, ds.getName(), ae);
    }

  }

  private void updateRes(TacoSignal sig,String resName,String resValue,String oldValue) {

    fr.esrf.TacoApi.TacoDevice ds = sig.device.ds;

    if(!resValue.equals(oldValue)) {
      try {
        if(resValue.isEmpty() || resValue.equalsIgnoreCase("Not specified"))
          ds.delResource(sig.sigName+resName);
        else
          ds.putResource(sig.sigName + resName, new String[]{resValue});
      } catch (fr.esrf.TacoApi.TacoException e) {
        ATKException ae = Utils.getInstance().buildATKException(e,"TacoPutResourceFail",ErrSeverity.ERR,"TacoNumberScalar.storeConfig()");
        ErrorPane.showErrorMessage(null,ds.getName(),ae);
      }
    }

  }

  @Override
  public void storeConfig() {

    // Label update
    updateRes(tacoSignal,".label",getLabel(),tacoSignal.label);
    tacoSignal.label = getLabel();

    // Unit update
    updateRes(tacoSignal,".unit",getUnit(),tacoSignal.unit);
    tacoSignal.unit = getUnit();

    // format update
    updateRes(tacoSignal,".format",getFormat(),tacoSignal.format);
    tacoSignal.format = getFormat();

    //AlMin
    updateNumberRes(tacoSignal,".alhigh",getMaxAlarm(),tacoSignal.alHigh);
    tacoSignal.alHigh = getMaxAlarm();

    //AlMax
    updateNumberRes(tacoSignal,".allow",getMinAlarm(),tacoSignal.alLow);
    tacoSignal.alLow = getMinAlarm();

    //Max (Set value and read value must be identical)
    if(tacoSignal.hasWriteValue()) {
      updateNumberRes(tacoSignal,".max",getMaxValue(),tacoSignal.max);
      tacoSignal.max = getMaxValue();
      updateNumberRes(tacoSignal.setSignal,".max",getMaxValue(),tacoSignal.setSignal.max);
      tacoSignal.setSignal.max = getMaxValue();
    }

    //Min (Set value and read value must be identical)
    if(tacoSignal.hasWriteValue()) {
      updateNumberRes(tacoSignal,".min",getMinValue(),tacoSignal.min);
      tacoSignal.min = getMinValue();
      updateNumberRes(tacoSignal.setSignal,".min",getMinValue(),tacoSignal.setSignal.min);
      tacoSignal.setSignal.min = getMinValue();
    }

    try {
      tacoSignal.device.updateSigConfig();
    } catch (fr.esrf.TacoApi.TacoException e) {
      ATKException ae = Utils.getInstance().buildATKException(e,"TacoUpdateSigConfigFail",ErrSeverity.ERR,"TacoNumberScalar.storeConfig()");
      ErrorPane.showErrorMessage(null,tacoSignal.device.getName(),ae);
    }

  }

  @Override
  public void setAlias(String s) {
  }

  @Override
  public String getAlias() {
    return "";
  }

  @Override
  public boolean isOperator() {
    return true;
  }

  @Override
  public boolean isExpert() {
    return false;
  }

  @Override
  public AtkEventListenerList getListenerList() {
    return propChanges.getListenerList();
  }

  @Override
  public void refresh() {

    refreshCount++;
    try
    {
      double value = tacoSignal.getValue();
      if(Double.isNaN(value)) {
        setState(IAttribute.INVALID);
      } else if(value==65536.0) {
        // Not valid value for Taco device
        value = Double.NaN;
        setState(IAttribute.INVALID);
      } else
        setState(IAttribute.VALID);
      propChanges.fireNumberScalarEvent(this, value, System.currentTimeMillis());
    } catch (Exception e) {
      // Code failure
      System.out.println("TacoNumberScalar.refresh() Exception caught ------------------------------");
      e.printStackTrace();
      System.out.println("TacoNumberScalar.refresh()------------------------------------------------");
    }

  }

  @Override
  public void change(TangoChangeEvent tangoChangeEvent) {
  }

  @Override
  public void periodic(TangoPeriodicEvent tangoPeriodicEvent) {
  }

}
