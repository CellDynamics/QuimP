package uk.ac.warwick.wsbc.quimp.utils.test.matchers.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Hamcrest extension.
 * 
 * <p>Matches two text files.
 * 
 * @author p.baniukiewicz
 *
 */
public class FileMatcher extends TypeSafeDiagnosingMatcher<File> {

  private final File expected;

  /**
   * Main constructor.
   * 
   * @param expected expected file.
   */
  public FileMatcher(File expected) {
    this.expected = expected;
  }

  @Override
  public void describeTo(Description arg0) {
    arg0.appendText("Same content of file");

  }

  @Override
  protected boolean matchesSafely(File compared, Description arg1) {
    String line;
    int lineNo = 0;
    BufferedReader expectedReader = null;
    BufferedReader comparedReader = null;
    boolean status = true;
    try {
      expectedReader = getReader(compared);
      comparedReader = getReader(expected);
      // go through expected lines
      while ((line = expectedReader.readLine()) != null) {
        lineNo++;
        Matcher<?> equalsMatcher = CoreMatchers.equalTo(line);
        String actualLine = comparedReader.readLine();
        if (!equalsMatcher.matches(actualLine)) {
          arg1.appendText("was: ").appendDescriptionOf(equalsMatcher).appendText(" at line ")
                  .appendValue(lineNo).appendText(" of " + compared.getName());
          status = false;
          break;
        }
      }
      // check if compared has any line left if not broke previous loop
      if (status && comparedReader.readLine() != null) {
        arg1.appendText("was: ").appendText(" longer than " + expected.getName());
        status = false;
      }
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    // close all buffers
    if (expectedReader != null) {
      try {
        expectedReader.close();
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }
    if (comparedReader != null) {
      try {
        comparedReader.close();
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }

    return status;
  }

  private BufferedReader getReader(File fileToRead) throws FileNotFoundException {
    BufferedReader expectedReader;
    InputStream ins = new FileInputStream(fileToRead);
    InputStreamReader isr = new InputStreamReader(ins, Charset.forName("UTF-8"));
    expectedReader = new BufferedReader(isr);
    return expectedReader;
  }
}
