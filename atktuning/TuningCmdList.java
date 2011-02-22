package atktuning;

import fr.esrf.tangoatk.core.IEntityFilter;
import fr.esrf.tangoatk.core.IEntity;
import fr.esrf.tangoatk.core.ConnectionException;
import fr.esrf.tangoatk.core.command.VoidVoidCommand;

/** A class which extends the ATK Command list, It manages whole commands of a device */
public class TuningCmdList extends fr.esrf.tangoatk.core.CommandList {

  private String name; // The device name

  public TuningCmdList() {
    super();
  }

  // Get all the VoidVoid command from a device
  public void setModel(String devName) {

    name = devName;

    IEntityFilter cmdfilter = new IEntityFilter() {
      public boolean keep(IEntity entity) {
        return (entity instanceof VoidVoidCommand);
      }
    };

    setFilter(cmdfilter);

    try {
      add(devName+"/*");
    } catch (ConnectionException e) {
      System.out.println(e.getDescription());
    }

  }

  public String getName() {
    return name;
  }

  static TuningCmdList findList(TuningCmdList[] array,int nbItem,String name) {
    boolean found=false;
    int i=0;
    while(i<nbItem && !found) {
      found = array[i].getName().equalsIgnoreCase(name);
      if(!found) i++;
    }
    if( found )
      return array[i];
    else
      return null;
  }

}
