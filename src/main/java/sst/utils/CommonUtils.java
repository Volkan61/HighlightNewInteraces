package sst.utils;

/**
 * @author CapGemini, Volkan Hacimüftüoglu
 * @version 05.02.2018
 */
public class CommonUtils {

  /*
   * Bestimmt das größtmögliche Präfix von einem String-Array
   */
  public static String longestCommonPrefix(String[] strings) {

    if (strings.length == 0) {
      return ""; // Or maybe return null?
    }

    for (int prefixLen = 0; prefixLen < strings[0].length(); prefixLen++) {
      char c = strings[0].charAt(prefixLen);
      for (int i = 1; i < strings.length; i++) {
        if (prefixLen >= strings[i].length() || strings[i].charAt(prefixLen) != c) {
          // Mismatch found
          return strings[i].substring(0, prefixLen);
        }
      }
    }
    return strings[0];
  }
}
