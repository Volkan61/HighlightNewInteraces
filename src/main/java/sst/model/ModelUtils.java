package sst.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * @author CapGemini, Volkan Hacimüftüoglu
 * @version 05.02.2018
 */
public class ModelUtils {

  public static boolean checkTest(LinkedList<Interface> list, Interface pInterface) {

    boolean result = false;
    for (Iterator iterator = list.iterator(); iterator.hasNext();) {
      Interface interface1 = (Interface) iterator.next();

      String pInterfaceName = pInterface.getName();
      String pInterfaceVersion = pInterface.getVersion();

      String interfaceName = interface1.getName();
      String interfaceVersion = interface1.getVersion();

      if (pInterfaceName.equals(interfaceName) && pInterfaceVersion.equals(interfaceVersion)) {
        result = true;
        break;
      }
    }
    return result;
  }

  public static String getKey(Map<String, LinkedList<Interface>> mapPrefixToInterfaces, Interface pInterface) {

    Set<String> keySetHashMap = mapPrefixToInterfaces.keySet();

    String result = null;
    for (Iterator iterator = keySetHashMap.iterator(); iterator.hasNext();) {
      String string = (String) iterator.next();
      LinkedList<Interface> list = mapPrefixToInterfaces.get(string);

      boolean asdsa = checkTest(list, pInterface);

      if (asdsa) {
        result = string;
        break;
      }
    }
    return result;
  }

}
