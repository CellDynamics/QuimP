package com.github.celldynamics.quimp.plugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.filesystem.IQuimpSerialize;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This abstract class serves basic methods for processing parameters strings specified in macros.
 * 
 * <p>The principal idea is to have separate class that would hold all parameters plugin uses. Such
 * class, if derived from {@link AbstractPluginOptions} allows to produce self-representation as
 * JSon that can be displayed in macro recorder as plugin parameters string. Reverse operation -
 * creating instance of option class from JSon string is also supported.
 * 
 * <p>Additional features provided by this template are removing quotes from JSon string and
 * allowing parameters that contain spaces. In the first case such de-quoted string can be easily
 * passed as a parameter string in macro (which is also string) without the need of escaping quotes.
 * De-quoted strings can be also deserialzied. Second feature allows using {@link EscapedPath}
 * annotation for fields of String type which then are automatically enclosed in specified escape
 * character. This allows to use white spaces in these fields.
 * 
 * <p>Use {@link #beforeSerialize()} and {@link #afterSerialize()} to prepare object before
 * converting to options string and after converting it back.
 * 
 * <p>Methods {@link #serialize2Macro()} and
 * {@link #deserialize2Macro(String, AbstractPluginOptions)} are intended for creating parameter
 * strings, which can be then displayed in Macro Recorder, and creating instance of Option object
 * from such strings (specified by user in macro). Remaining two other methods:
 * {@link #serialize()} and {@link #deserialize(String, AbstractPluginOptions)} stand for normal
 * conversion to and from JSon (both take under account {@link EscapedPath} annotation).
 * 
 * <p>This abstract class by default provides {@link AbstractPluginOptions#paramFile} field that
 * holds path to the configuration file.
 * 
 * <p>There are following restriction to parameter string and concrete options class:
 * <ul>
 * <li>Quotes are not allowed in Strings (even properly escaped)
 * <li>Round brackets are not allowed in strings - they are used for escaping strings
 * <li>Arrays are allowed but only those containing primitive numbers and strings
 * <li>Concrete object should be cloneable, {@link #serialize()} makes <b>shallow</b> copy of
 * object otherwise. <b>Implement your own clone if you use arrays or collections.</b>
 * <li>If there are other objects stored in concrete implementation of this abstract class, they
 * must have default constructors for GSon.
 * </ul>
 * 
 * @author p.baniukiewicz
 * @see com.github.celldynamics.quimp.plugin.AbstractPluginOptionsTest#testSerDeser_2()
 */
public abstract class AbstractPluginOptions implements Cloneable, IQuimpSerialize {
  /**
   * The Constant logger.
   */
  public static final transient Logger LOGGER =
          LoggerFactory.getLogger(AbstractPluginOptions.class);
  /**
   * Default key used to denote options string in IJ macro recorder.
   */
  public static final transient String KEY = "opts";
  /**
   * Maximal length of parameter string.
   */
  public static final transient int MAXITER = 512;
  /**
   * Name and path of QCONF file.
   */
  @EscapedPath
  public String paramFile;

  /**
   * Create JSon string from this object.
   * 
   * <p>Fields annotated by {@link EscapedPath} will be enclosed by escape characters. If
   * {@link EscapedPath} is applied to non string data type, it is ignored.
   * 
   * <p>This method return regular JSon (but with escaped Strings if annotation is set).
   * Complementary method {@link #serialize2Macro()} return JSon file without quotes supposed to be
   * used as parameter string in macro.
   * 
   * @return JSon string from this object
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
          ; // ignore and process next field. This protects against non string fields annotated
        }
      }
    } catch (CloneNotSupportedException e1) {
      LOGGER.debug(e1.getMessage(), e1);
    } finally {
      if (cp != null) {
        ((AbstractPluginOptions) cp).beforeSerialize();
      }
      json = gson.toJson(cp);
    }
    return json;
  }

  /**
   * Serialize this class and produce JSon without spaces (except escaped strings) and without
   * quotes.
   * 
   * <p>Return is intended to show in macro recorder. Note that IJ require key to assign a parameter
   * string to it. Recommended way of use this method is:
   * 
   * <pre>
   * <code>
   * Recorder.setCommand("Generate mask");
   * Recorder.recordOption(AbstractPluginOptions.KEY, opts.serialize2Macro()); 
   * </code>
   * </pre>
   * 
   * @return JSon without spaces and quotes
   * @see EscapedPath
   * @see #escapeJsonMacro(String)
   * @see #serialize()
   * @see #deserialize2Macro(String, AbstractPluginOptions)
   */
  public String serialize2Macro() {
    return escapeJsonMacro(serialize());

  }

  /**
   * Create AbstractPluginOptions reference from JSon. Remove escaping chars.
   * 
   * <p>This method return object from regular JSon. All fields annotated with {@link EscapedPath}
   * will have escaping characters removed. Complementary method
   * {@link #deserialize2Macro(String, AbstractPluginOptions)} accept JSon file without quotes
   * and it is supposed to be used as processor of parameter string specified in macro.
   * 
   * @param json JSon string produced by {@link #serialize()}
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
   * Deserialize JSon produced by {@link #serialize2Macro()}, that is json without quotations.
   * 
   * <p>This method accepts that input string can contain a key specified by {@value #KEY}. See
   * {@link #serialize2Macro()}.
   * 
   * @param json JSon to deserialize
   * @param t type of object
   * @return object produced from JSon, fields annotated with {@link EscapedPath} does not contain
   *         escape characters.
   * @throws QuimpPluginException on deserialization error. As JSon will be produced by user in
   *         macro script this usually will be problem with escaping or forming proper JSon.
   */
  public static <T extends AbstractPluginOptions> T deserialize2Macro(String json, T t)
          throws QuimpPluginException {
    T obj = null;
    try {
      String jsonU = unescapeJsonMacro(json);
      jsonU = jsonU.replaceFirst(AbstractPluginOptions.KEY + "=", "");
      obj = deserialize(jsonU, t);
      ((AbstractPluginOptions) obj).afterSerialize();
    } catch (Exception e) {
      throw new QuimpPluginException("Malformed options string (" + e.getMessage() + ")", e);
    }

    return obj;

  }

  /**
   * Remove quotes and white characters from JSon file.
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
   * Remove white characters from string except those enclosed in ().
   * 
   * <p>String can not start with (. Integrity (number of opening and closing brackets) is not
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
      if (c == '(') {
        outRegion = false;
      }
      if (c == ')') {
        outRegion = true;
      }
    }
    return sb.toString();

  }

  /**
   * Reverse {@link #escapeJsonMacro(String)}. Add removed quotes. Do not verify integrity of
   * produced JSon.
   * 
   * @param json JSon returned by {@link #escapeJsonMacro(String)}
   * @return proper JSon string
   */
  public static String unescapeJsonMacro(String json) {
    String nospaces = removeSpacesMacro(json);

    final char toInsert = '"';
    int startIndex = 0;
    int indexOfParenthesSL = 0; // square left
    int indexOfParenthesSR = 0; // square right
    int i = 0; // iterations counter
    HashMap<String, String> map = new HashMap<>();
    // remove arrays [] and replace them with placeholders, they will be processed latter
    while (true) {
      indexOfParenthesSL = nospaces.indexOf('[', startIndex); // find opening [
      if (indexOfParenthesSL < 0) {
        break; // stop if not found
      } else {
        startIndex = indexOfParenthesSL; // start looking for ] from position of [
        indexOfParenthesSR = nospaces.indexOf(']', startIndex);
        if (indexOfParenthesSR < 0) { // closing not found
          break; // error in general
        }
        // cut text between [] inclusively
        String random = Long.toHexString(Double.doubleToLongBits(Math.random())); // random placeh
        map.put(random, nospaces.substring(indexOfParenthesSL, indexOfParenthesSR + 1)); // store it
        nospaces = nospaces.replace(map.get(random), random); // remove from sequence
        startIndex = indexOfParenthesSR - map.get(random).length() + random.length(); // next iter
      }
      if (i++ > MAXITER) {
        throw new IllegalArgumentException("Malformed options string.");
      }
    }

    // now nospaces does not contains [], they are replaced by alphanumeric strings
    // note that those string will be pu into "" after two next blocks
    startIndex = 0;
    int indexOfParenthes = 0;
    int indexOfComa = 0;
    int indexOfColon = 0;
    // detect content between : and , or { what denotes value
    // if it is not numeric put it in quotes
    while (true) {
      indexOfColon = nospaces.indexOf(':', startIndex);
      startIndex = indexOfColon + 1;
      if (indexOfColon < 0) {
        break;
      }
      // nested class, find next :
      if (nospaces.charAt(indexOfColon + 1) == '{') {
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
      if (i++ > MAXITER) {
        throw new IllegalArgumentException("Malformed options string.");
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
        if (nospaces.charAt(indexOfParenthes + 1) == '}') {
          startIndex = indexOfParenthes + 1;
          continue;
        }
      }
      startIndex = indexOfComa + 1;
      if (indexOfComa < 0) {
        break;
      }
      indexOfColon = nospaces.indexOf(':', startIndex);
      nospaces = new StringBuilder(nospaces).insert(indexOfComa + 1, toInsert).toString();
      nospaces = new StringBuilder(nospaces).insert(indexOfColon + 1, toInsert).toString();
      if (i++ > MAXITER) {
        throw new IllegalArgumentException("Malformed options string.");
      }
    }

    // process content of arrays and substitute them to string. If array was numeric do not change
    // it, otherwise put every element in quotes
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String val = entry.getValue();
      val = val.substring(1, val.length() - 1); // remove []
      if (val.isEmpty()) {
        nospaces = nospaces.replace(toInsert + entry.getKey() + toInsert, entry.getValue());
        continue;
      }
      String[] elements = val.split(",");
      // check if first is number (assume array of primitives)
      if (!NumberUtils.isCreatable(elements[0])) { // not a number - add "" to all
        for (i = 0; i < elements.length; i++) {
          elements[i] = toInsert + elements[i] + toInsert;
        }
      }
      // build proper array json
      String ret = "[";
      for (i = 0; i < elements.length; i++) {
        ret = ret.concat(elements[i]).concat(",");
      }
      ret = ret.substring(0, ret.length() - 1).concat("]");
      entry.setValue(ret);
      // now map contains proper representation of arrays as json, e.g. [0,0] or ["d","e"]
      // replace placeholders from nospaces (placeholders are already quoted after previous steps)
      nospaces = nospaces.replace(toInsert + entry.getKey() + toInsert, entry.getValue());
    }

    return nospaces;

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#beforeSerialize()
   */
  @Override
  public void beforeSerialize() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.github.celldynamics.quimp.filesystem.IQuimpSerialize#afterSerialize()
   */
  @Override
  public void afterSerialize() throws Exception {
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
