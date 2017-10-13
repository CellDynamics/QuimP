package com.github.celldynamics.quimp.utils.graphics.svg;

import static com.github.baniuk.ImageJTestSuite.matchers.file.FileMatchers.containsExactText;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Rectangle;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.github.celldynamics.quimp.QColor;
import com.github.celldynamics.quimp.geom.ExtendedVector2d;
import com.github.celldynamics.quimp.utils.graphics.svg.SVGwritter.QPolarAxes;
import com.github.celldynamics.quimp.utils.graphics.svg.SVGwritter.QScaleBar;
import com.github.celldynamics.quimp.utils.graphics.svg.SVGwritter.Qcircle;
import com.github.celldynamics.quimp.utils.graphics.svg.SVGwritter.Qline;
import com.github.celldynamics.quimp.utils.graphics.svg.SVGwritter.Qtext;

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
  public void testGetReference() throws Exception {
    BufferedOutputStream out =
            new BufferedOutputStream(new FileOutputStream(tmpdir + "testWriteHeader"));
    PrintWriter osw = new PrintWriter(out);
    SVGwritter.writeHeader(osw, new Rectangle(-10, -10, 10, 10));
    osw.write("</svg>\n");
    osw.close();

    {
      out = new BufferedOutputStream(new FileOutputStream(tmpdir + "testQcircle"));
      osw = new PrintWriter(out);
      Qcircle qc = new SVGwritter.Qcircle(1.1, 2.2, 3.1);
      qc.thickness = 3.14;
      qc.draw(osw);
      osw.close();
    }
    {
      out = new BufferedOutputStream(new FileOutputStream(tmpdir + "testQline"));
      osw = new PrintWriter(out);
      Qline qc = new SVGwritter.Qline(1.1, 2.2, 3.1, 4.1);
      qc.thickness = 3.14;
      qc.colour = new QColor(0.1, 0.2, 0.3);
      qc.draw(osw);
      osw.close();
    }
    {
      out = new BufferedOutputStream(new FileOutputStream(tmpdir + "testQtext"));
      osw = new PrintWriter(out);
      Qtext qc = new SVGwritter.Qtext("test", 2.2, "New Roman", new ExtendedVector2d());
      qc.colour = new QColor(0.1, 0.2, 0.3);
      qc.draw(osw);
      osw.close();
    }
    {
      out = new BufferedOutputStream(new FileOutputStream(tmpdir + "testQPolarAxes"));
      osw = new PrintWriter(out);
      QPolarAxes qc = new SVGwritter.QPolarAxes(new Rectangle(0, 0, 10, 10));
      qc.colour = new QColor(0.1, 0.2, 0.3);
      qc.thickness = 3.14;
      qc.draw(osw);
      osw.close();
    }
    {
      out = new BufferedOutputStream(new FileOutputStream(tmpdir + "testQScaleBar"));
      osw = new PrintWriter(out);
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
    PrintWriter osw = new PrintWriter(out);

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
    PrintWriter osw = new PrintWriter(out);

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
    PrintWriter osw = new PrintWriter(out);

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
    PrintWriter osw = new PrintWriter(out);

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
    PrintWriter osw = new PrintWriter(out);

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
    PrintWriter osw = new PrintWriter(out);

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
