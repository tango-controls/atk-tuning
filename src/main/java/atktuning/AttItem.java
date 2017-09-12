package atktuning;

/**
 * Attribute config
 */
public class AttItem {

  String  attName;     // Attribute name (4 fields)
  boolean isTaco;      // true is attName is a taco signal
  String  setCommand;  // Taco set command
  boolean isSettable;  // tue is settable (taco only)
  String  setName;     // Name of the set signal

  public AttItem() {
    attName = "";
    isTaco = false;
    setCommand = null;
    isSettable = false;
    setName = null;
  }

}
