package backoffice.common.utils;

public class MaskUtils {
  public static String removeNumbericMask(String data) {
    return data.replaceAll("[^\\d]", "");
  }
}