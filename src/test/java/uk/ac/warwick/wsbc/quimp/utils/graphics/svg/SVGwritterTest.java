package uk.ac.warwick.wsbc.quimp.utils.graphics.svg;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static uk.ac.warwick.wsbc.quimp.utils.test.matchers.file.FileMatchers.containsExactText;

import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.junit.Ignore;
import org.junit.Test;

import uk.ac.warwick.wsbc.quimp.QColor;
import uk.ac.warwick.wsbc.quimp.geom.ExtendedVector2d;
import uk.ac.warwick.wsbc.quimp.utils.graphics.svg.SVGwritter.QPolarAxes;
import uk.ac.warwick.wsbc.quimp.utils.graphics.svg.SVGwritter.QScaleBar;
import uk.ac.warwick.wsbc.quimp.utils.graphics.svg.SVGwritter.Qcircle;
import uk.ac.warwick.wsbc.quimp.utils.graphics.svg.SVGwritter.Qline;
import uk.ac.warwick.wsbc.quimp.utils.graphics.svg.SVGwritter.Qtext;

/**
 * Tests of svg output.
 * 
 * <p>Each output is compared with previously generated reference output.
 * 
 * @author p.baniukiewicz
 *
 */
public class SVGwritterTest {

  /**
   * The tmpdir.
   */
  static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

  /**
   * Generate reference of method output.
   * 
   * <p>Use only on code change and copy to test resources. Other method compare their output with
   * this reference. After use copy outputs to Resources-static/SVGwritterTest.
   * 
   * @throws Exception on error
   */
  @Test
  @Ignore
  public void testGetReference() throws Exception {
    BufferedOutputStream out =
            new BufferedOutputStream(new FileOutputStream(tmpdir + "testWriteHeader"));
    OutputStreamWriter osw = new OutputStreamWriter(out);
    SVGwritter.writeHeader(osw, new Rectangle(-10, -10, 10, 10));
    osw.write("</svg>\n");
    osw.close();

    {
      out = new BufferedOutputStream(new FileOutputStream(tmpdir + "testQcircle"));
      osw = new OutputStreamWriter(out);
      Qcircle qc = new SVGwritter.Qcircle(1.1, 2.2, 3.1);
      qc.thickness = 3.14;
      qc.draw(osw);
      osw.close();
    }
    {
      out = new BufferedOutputStream(new FileOutputStream(tmpdir + "testQline"));
      osw = new OutputStreamWriter(out);
      Qline qc = new SVGwritter.Qline(1.1, 2.2, 3.1, 4.1);
      qc.thickness = 3.14;
      qc.colour = new QColor(0.1, 0.2, 0.3);
      qc.draw(osw);
      osw.close();
    }
    {
      out = new BufferedOutputStream(new FileOutputStream(tmpdir + "testQtext"));
      osw = new OutputStreamWriter(out);
      Qtext qc = new SVGwritter.Qtext("test", 2.2, "New Roman", new ExtendedVector2d());
      qc.colour = new QColor(0.1, 0.2, 0.3);
      qc.draw(osw);
      osw.close();
    }
    {
      out = new BufferedOutputStream(new FileOutputStream(tmpdir + "testQPolarAxes"));
      osw = new OutputStreamWriter(out);
      QPolarAxes qc = new SVGwritter.QPolarAxes(new Rectangle(0, 0, 10, 10));
      qc.colour = new QColor(0.1, 0.2, 0.3);
      qc.thickness = 3.14;
      qc.draw(osw);
      osw.close();
    }
    {
      out = new BufferedOutputStream(new FileOutputStream(tmpdir + "testQScaleBar"));
      osw = new OutputStreamWriter(out);
      QScaleBar qc = new SVGwritter.QScaleBar(new ExtendedVector2d(), "mm", 10, 2);
      qc.colour = new QColor(0.1, 0.2, 0.3);
      qc.thickness = 3.14;
      qc.draw(osw);
      osw.close();
    }
  }

  /**
   * Compare output from method with reference file.
   * 
   * @throws Exception on error
   */
  @Test
  public void testWriteHeader() throws Exception {
    File orginal = new File("src/test/Resources-static/SVGwritterTest/testWriteHeader");
    File test = File.createTempFile("testWriteHeader", "", new File(tmpdir));
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(test));
    OutputStreamWriter osw = new OutputStreamWriter(out);

    SVGwritter.writeHeader(osw, new Rectangle(-10, -10, 10, 10));
    osw.write("</svg>\n");
    osw.close();

    assertThat(test, is(containsExactText(orginal)));
    test.delete();
  }

  /**
   * Compare output from method with reference file.
   * 
   * @throws Exception on error
   */
  @Test
  public void testQcircle() throws Exception {
    File orginal = new File("src/test/Resources-static/SVGwritterTest/testQcircle");
    File test = File.createTempFile("testWriteHeader", "", new File(tmpdir));
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(test));
    OutputStreamWriter osw = new OutputStreamWriter(out);

    Qcircle qc = new SVGwritter.Qcircle(1.1, 2.2, 3.1);
    qc.thickness = 3.14;
    qc.draw(osw);
    osw.close();

    assertThat(test, is(containsExactText(orginal)));
    test.delete();
  }

  /**
   * Compare output from method with reference file.
   * 
   * @throws Exception on error
   */
  @Test
  public void testQline() throws Exception {
    File orginal = new File("src/test/Resources-static/SVGwritterTest/testQline");
    File test = File.createTempFile("testWriteHeader", "", new File(tmpdir));
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(test));
    OutputStreamWriter osw = new OutputStreamWriter(out);

    Qline qc = new SVGwritter.Qline(1.1, 2.2, 3.1, 4.1);
    qc.thickness = 3.14;
    qc.colour = new QColor(0.1, 0.2, 0.3);
    qc.draw(osw);
    osw.close();

    assertThat(test, is(containsExactText(orginal)));
    test.delete();
  }

  /**
   * Compare output from method with reference file.
   * 
   * @throws Exception on error
   */
  @Test
  public void testQtext() throws Exception {
    File orginal = new File("src/test/Resources-static/SVGwritterTest/testQtext");
    File test = File.createTempFile("testWriteHeader", "", new File(tmpdir));
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(test));
    OutputStreamWriter osw = new OutputStreamWriter(out);

    Qtext qc = new SVGwritter.Qtext("test", 2.2, "New Roman", new ExtendedVector2d());
    qc.colour = new QColor(0.1, 0.2, 0.3);
    qc.draw(osw);
    osw.close();

    assertThat(test, is(containsExactText(orginal)));
    test.delete();
  }

  /**
   * Compare output from method with reference file.
   * 
   * @throws Exception on error
   */
  @Test
  public void testQPolarAxes() throws Exception {
    File orginal = new File("src/test/Resources-static/SVGwritterTest/testQPolarAxes");
    File test = File.createTempFile("testWriteHeader", "", new File(tmpdir));
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(test));
    OutputStreamWriter osw = new OutputStreamWriter(out);

    QPolarAxes qc = new SVGwritter.QPolarAxes(new Rectangle(0, 0, 10, 10));
    qc.colour = new QColor(0.1, 0.2, 0.3);
    qc.thickness = 3.14;
    qc.draw(osw);
    osw.close();

    assertThat(test, is(containsExactText(orginal)));
    test.delete();
  }

  /**
   * Compare output from method with reference file.
   * 
   * @throws Exception on error
   */
  @Test
  public void testQScaleBar() throws Exception {
    File test = File.createTempFile("testWriteHeader", "", new File(tmpdir));
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(test));
    OutputStreamWriter osw = new OutputStreamWriter(out);

    QScaleBar qc = new SVGwritter.QScaleBar(new ExtendedVector2d(), "mm", 10, 1);
    qc.setScale(2);
    qc.colour = new QColor(0.1, 0.2, 0.3);
    qc.thickness = 3.14;
    qc.draw(osw);
    osw.close();

    File orginal = new File("src/test/Resources-static/SVGwritterTest/testQScaleBar");
    assertThat(test, is(containsExactText(orginal)));
    test.delete();
  }

}
