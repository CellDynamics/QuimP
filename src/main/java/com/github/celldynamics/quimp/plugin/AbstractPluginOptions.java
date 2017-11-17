package com.github.celldynamics.quimp.plugin;

import java.lang.reflect.Field;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This abstract class serves basic methods for processing parameters strings specified in macros.
 * 
 * <p>The principal idea is to have separate class that would hold all parameters plugin uses. Such
 * class, if derived from {@link AbstractPluginOptions} allows to produce its json representation
 * that can be given in macro recorder as plugin parameters string. Reverse operation - creating
 * instance of option class from json string is also supported.
 * 
 * <p>Additional features provided by this template are removing quotes from json string and
 * allowing for parameters with spaces. In the first case such de-quoted string can be easily passed
 * as a parameter string in macro (which is also string) without the need of escaping quotes.
 * De-quoted strings can be also deserialzied. Second feature allows using {@link EscapedPath}
 * annotation for fields of String type which then are automatically enclosed in specified escape
 * character. This allows to use white spaces in these fields.
 * 
 * <p>Methods {@link #serialize2Macro()} and
 * {@link #deserialize2Macro(String, AbstractPluginOptions)} are intended for processing parameter
 * strings displayed in Macro Recorder and collected collected from macro. Remaining two:
 * {@link #serialize()} and {@link #deserialize(String, AbstractPluginOptions)} stand for normal
 * conversion to and from json (both take under account {@link EscapedPath} annotation).
 * 
 * <p>There are following restriction to parameter string:
 * <ul>
 * <li>Quotes are not allowed in Strings (even properly escaped)
 * <li>Arrays are not allowed in parameter class (derived from {@link AbstractPluginOptions}) due to
 * json representation of array that uses square brackets (same as default string escaping
 * characters).
 * </ul>
 * 
 * @author p.baniukiewicz
 *
 */
public abstract class AbstractPluginOptions implements Cloneable {
  /**
   * The Constant logger.
   */
  public static final Logger LOGGER = LoggerFactory.getLogger(AbstractPluginOptions.class);

  /**
   * Maximal length of parameter string.
   */
  public static final int MAXLEN = 512;
  /**
   * Name and path of QCONF file.
   */
  @EscapedPath
  public String paramFile;

  /**
   * Create json string from this object.
   * 
   * <p>Fields annotated by {@link EscapedPath} will be enclosed by escape characters. If
   * {@link EscapedPath} is applied to non string data type, it is ignored.
   * 
   * <p>This method return regular json (but with escaped Strings if annotation is set).
   * Complementary method {@link #serialize2Macro()} return json file without quotes supposed to be
   * used as parameter string in macro.
   * 
   * @return json string from this object
   * @see EscapedPath
   * @see #serialize2Macro()
   */
  public String serialize() {
    GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    String json = null;
    Object cp = null;
    try {
      cp = this.clone();
      for (Field f : FieldUtils.getFieldsListWithAnnotation(this.getClass(), EscapedPath.class)) {
        try {
          String s = (String) f.get(this);
          EscapedPath annotation = (EscapedPath) f.getAnnotation(EscapedPath.class);
          s = annotation.left() + s + annotation.right();
          Field cpf = getField(cp.getClass(), f.getName());
          if (cpf != null) {
            cpf.set(cp, s);
          }
        } catch (IllegalArgumentException | IllegalAccessException | SecurityException
                | ClassCastException e) {
          ; // ignore and process next field. This protects against non string fields annotaed
        }
      }
    } catch (CloneNotSupportedException e1) {
      LOGGER.debug(e1.getMessage(), e1);
    } finally {
      json = gson.toJson(cp);
    }
    return json;
  }

  /**
   * Serialize this class and produce json without spaces (except escaped strings) and without
   * quotes.
   * 
   * <p>Return is intended to show in macro recorder.
   * 
   * @return json without spaces and quotes
   * @see EscapedPath
   * @see #escapeJsonMacro(String)
   * @see #serialize()
   */
  public String serialize2Macro() {
    return escapeJsonMacro(serialize());

  }

  /**
   * Create AbstractPluginOptions reference from json. Remove escaping chars.
   * 
   * <p>This method return object from regular json. All fields annotated with {@link EscapedPath}
   * will have escaping characters removed. Complementary method
   * {@link #deserialize2Macro(String, AbstractPluginOptions)} accepr json file without quotes
   * and it is supposed to be used as processor of parameter string specified in macro.
   * 
   * @param json json string produced by {@link #serialize()}
   * @param t type of restored object
   * @return instance of T class
   * @see EscapedPath
   * @see #deserialize2Macro(String, AbstractPluginOptions)
   */
  @SuppressWarnings("unchecked")
  public static <T extends AbstractPluginOptions> T deserialize(String json, T t) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    T obj = null;

    obj = (T) gson.fromJson(json, t.getClass());
    for (Field f : FieldUtils.getFieldsListWithAnnotation(obj.getClass(), EscapedPath.class)) {
      try {
        String s = (String) f.get(obj);
        EscapedPath annotation = (EscapedPath) f.getAnnotation(EscapedPath.class);
        s = StringUtils.removeStart(s, Character.toString(annotation.left()));
        s = StringUtils.removeEnd(s, Character.toString(annotation.right()));
        f.set(obj, s);
      } catch (IllegalArgumentException | IllegalAccessException | SecurityException
              | ClassCastException e) {
        ; // ignore and process next field. This protects against non string fields annotaed
      }
    }
    return obj;
  }

  /**
   * Deserialize json produced by {@link #serialize2Macro()}, that is json without quotations.
   * 
   * @param json json to deserialize
   * @param t type of object
   * @return object produced from json, fields annotated with {@link EscapedPath} does not contain
   *         escape characters.
   * @throws QuimpPluginException on deserialization error. As json will be produced by user in
   *         macro script this usually will be problem with escaping or forming proper json.
   */
  public static <T extends AbstractPluginOptions> T deserialize2Macro(String json, T t)
          throws QuimpPluginException {
    T obj = null;
    try {
      String jsonU = unescapeJsonMacro(json);
      obj = deserialize(jsonU, t);
    } catch (Exception e) {
      throw new QuimpPluginException("Malformed Json string (" + e.getMessage() + ")", e);
    }

    return obj;

  }

  /**
   * Remove quotes and white characters from json file.
   * 
   * <p>Note that none of value can contain quote. Spaces in Strings for fields annotated by
   * {@link EscapedPath} are preserved.
   * 
   * @param json file to be processed
   * @return json string without spaces and quotes (except annotated fields).
   */
  public String escapeJsonMacro(String json) {
    String nospaces = removeSpacesMacro(json);
    return nospaces.replaceAll("\\\"+", "");
  }

  /**
   * Remove white characters from string except those enclosed in [].
   * 
   * <p>String can not start with [. Integrity (number of opening and closing brackets) is not
   * checked.
   * 
   * <p>TODO This should accept chars set in {@link EscapedPath}. (defined in class annotation)
   * 
   * @param param string to process
   * @return processed string.
   */
  public static String removeSpacesMacro(String param) {
    StringBuilder sb = new StringBuilder(param.length());
    boolean outRegion = true; // determine that we are outside brackets
    for (int i = 0; i < param.length(); i++) {
      char c = param.charAt(i);
      if (outRegion == true && Character.isWhitespace(c)) {
        continue;
      } else {
        sb.append(c);
      }
      if (c == '[') {
        outRegion = false;
      }
      if (c == ']') {
        outRegion = true;
      }
    }
    return sb.toString();

  }

  /**
   * Reverse {@link #escapeJsonMacro(String)}. Add removed quotes. Do not verify integrity of
   * produced json.
   * 
   * @param json json returned by {@link #escapeJsonMacro(String)}
   * @return proper json string
   */
  public static String unescapeJsonMacro(String json) {
    String nospaces = removeSpacesMacro(json);

    final char toInsert = '"';
    int startIndex = 0;
    int indexOfComa = 0;
    int indexOfColon = 0;
    int indexOfParenthes = 0;
    int i = 0; // iterations counter
    // detect content between : and , or { what denotes value
    // if it is not numeric put it in quotes
    while (true) {
      indexOfColon = nospaces.indexOf(':', startIndex);
      startIndex = indexOfColon + 1;
      if (indexOfColon < 0) {
        break;
      }
      if (nospaces.charAt(indexOfColon + 1) == '{') { // nested class, find next :
        continue;
      }
      indexOfComa = nospaces.indexOf(',', indexOfColon);
      indexOfParenthes = nospaces.indexOf('}', indexOfColon);
      if (indexOfComa < 0 || indexOfParenthes < indexOfComa) { // whatev first, detect end of nested
        indexOfComa = indexOfParenthes;
      }
      String sub = nospaces.substring(indexOfColon + 1, indexOfComa);
      if (!NumberUtils.isCreatable(sub)) { // only in not numeric
        nospaces = new StringBuilder(nospaces).insert(indexOfColon + 1, toInsert).toString();
        nospaces = new StringBuilder(nospaces).insert(indexOfComa + 1, toInsert).toString();
      }
      if (i++ > MAXLEN) {
        throw new IllegalArgumentException("Malformed Json string.");
      }
    }
    // detect keys between { or , and :
    startIndex = 0;
    indexOfComa = 0;
    indexOfColon = 0;
    indexOfParenthes = 0;
    i = 0;
    while (true) {
      indexOfComa = nospaces.indexOf(',', startIndex);
      indexOfParenthes = nospaces.indexOf('{', startIndex);
      if (indexOfParenthes >= 0 && indexOfParenthes < indexOfComa) { // beginning of file or nested
        indexOfComa = indexOfParenthes;
      }
      startIndex = indexOfComa + 1;
      if (indexOfComa < 0) {
        break;
      }
      indexOfColon = nospaces.indexOf(':', startIndex);
      nospaces = new StringBuilder(nospaces).insert(indexOfComa + 1, toInsert).toString();
      nospaces = new StringBuilder(nospaces).insert(indexOfColon + 1, toInsert).toString();
      if (i++ > MAXLEN) {
        throw new IllegalArgumentException("Malformed Json string.");
      }
    }

    return nospaces;

  }

  /**
   * Return the first {@link Field} in the hierarchy for the specified name.
   * 
   * <p>Taken from stackoverflow.com/questions/16966629
   * 
   * @param clazz class name to search
   * @param name field name
   * @return field instance
   * 
   */
  private static Field getField(Class<?> clazz, String name) {
    Field field = null;
    while (clazz != null && field == null) {
      try {
        field = clazz.getDeclaredField(name);
      } catch (Exception e) {
        ;
      }
      clazz = clazz.getSuperclass();
    }
    return field;
  }
}
