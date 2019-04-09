package com.github.celldynamics.quimp.plugin.protanalysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
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

/**
 * The Class ActionPolarPlotTest.
 *
 * @author p.baniukiewicz
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionPolarPlotTest {
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

  /** The action polar plot. */
  private ActionPolarPlot actionPolarPlot;

  /** The target. */
  private Path target;

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
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.ActionPolarPlot#actionPerformed(java.awt.event.ActionEvent)}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testActionPerformed() throws Exception {
    ActionEvent e = Mockito.mock(ActionEvent.class);
    actionPolarPlot = new ActionPolarPlot("", "", ui);
    actionPolarPlot.actionPerformed(e);
    // fluoreszenz-test_1_polar.svg
    assertThat(
            Paths.get(temp.getRoot().getPath(), "fluoreszenz-test_0_polar.svg").toFile().exists(),
            is(true));
    assertThat(
            Paths.get(temp.getRoot().getPath(), "fluoreszenz-test_1_polar.svg").toFile().exists(),
            is(true));
  }

}
