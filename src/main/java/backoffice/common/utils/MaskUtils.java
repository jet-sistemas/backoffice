package backoffice.common.utils;

public class MaskUtils {
  public static String removeNumbericMask(String data) {
    if (data == null) {
      return "";
    }
    return data.replaceAll("[^\\d]", "");
  }

  /** Máscara parcial de CPF: ***.XXX.XXX-** */
  public static String maskCpfPartial(String document) {
    String digits = removeNumbericMask(document);
    if (digits.length() != 11) {
      return "***.***.***-**";
    }
    return "***." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-**";
  }
}