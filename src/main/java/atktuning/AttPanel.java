package atktuning;

import java.util.Vector;

/**
 * Attribute panel
 */
public class AttPanel {

  String title;
  Vector<AttItem> items;

  public int getSize() {
    return items.size();
  }

  public AttItem get(int i) {
    return items.get(i);
  }

  public AttPanel() {
    items = new Vector<AttItem>();
  }

}
