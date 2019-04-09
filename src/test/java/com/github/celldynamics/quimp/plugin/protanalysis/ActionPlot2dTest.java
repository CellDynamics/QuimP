package com.github.celldynamics.quimp.plugin.protanalysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.celldynamics.quimp.filesystem.QconfLoader;

import ij.WindowManager;

/**
 * The Class ActionPlot2dTest.
 *
 * @author p.baniukiewicz
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionPlot2dTest {
  /**
   * temp folder.
   */
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  /** The model. */
  @Mock
  private Prot_Analysis model;

  /** The options. */
  private ProtAnalysisOptions options = new ProtAnalysisOptions();

  /** The ui. */
  @Mock
  private ProtAnalysisUi ui;

  /** The action plot 2 d. */
  private ActionPlot2d actionPlot2d;

  /** The target. */
  private Path target;
  // private ProtAnalysisOptions options = new ProtAnalysisOptions();

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    Mockito.when(ui.getModel()).thenReturn(model);
    Mockito.when(model.getOptions()).thenReturn(options);
    target = Paths.get(temp.getRoot().getPath(), "fluoreszenz-test.QCONF");
    FileUtils.copyFile(
            new File("src/test/Resources-static/ProtAnalysisTest/fluoreszenz-test.QCONF"),
            target.toFile());
    Mockito.when(model.getQconfLoader()).thenReturn(new QconfLoader(target.toFile()));
  }

  /**
   * Tear down.
   *
   * @throws Exception the exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.ActionPlot2d#actionPerformed(java.awt.event.ActionEvent)}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testActionPerformed() throws Exception {
    ActionEvent e = Mockito.mock(ActionEvent.class);
    options.chbXcentrPlot.setTrue();
    Field[] f = options.getClass().getFields();
    for (Field ff : f) {
      if (ff.getType() == MutableBoolean.class) {
        MutableBoolean val = (MutableBoolean) ff.get(options);
        val.setTrue();
      }
    }
    actionPlot2d = new ActionPlot2d("", "", ui);
    actionPlot2d.actionPerformed(e);
    String[] windows = WindowManager.getImageTitles();
    assertThat(windows.length, is(19));
    WindowManager.closeAllWindows();

  }

}
