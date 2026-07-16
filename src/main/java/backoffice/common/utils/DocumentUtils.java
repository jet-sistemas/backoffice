package backoffice.common.utils;

public final class DocumentUtils {

  private DocumentUtils() {
  }

  public static String normalize(String document) {
    if (document == null) {
      return "";
    }
    return document.replaceAll("\\D", "");
  }

  public static boolean matches(String storedDocument, String informedDocument) {
    return normalize(storedDocument).equals(normalize(informedDocument));
  }
}
