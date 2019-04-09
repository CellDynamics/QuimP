package com.github.celldynamics.quimp.plugin.protanalysis;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.celldynamics.quimp.filesystem.QconfLoader;

import ij.IJ;

/**
 * The Class ActionSaveTracksTest.
 *
 * @author p.baniukiewicz
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionSaveTracksTest {
  /**
   * temp folder.
   */
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  /** The model. */
  @Spy
  private Prot_Analysis model;

  /** The options. */
  private ProtAnalysisOptions options = new ProtAnalysisOptions();

  /** The ui. */
  @Mock
  private ProtAnalysisUi ui;

  /** The action save tracks. */
  private ActionSaveTracks actionSaveTracks;
  /** The target. */
  private Path target;

  /** The targetf. */
  private Path targetf;

  /**
   * Sets the up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    model.selected.add(new PointCoords(new Point(20, 20), 0));
    model.selected.add(new PointCoords(new Point(300, 300), 0));
    Mockito.when(ui.getModel()).thenReturn(model);
    Mockito.when(model.getOptions()).thenReturn(options);
    target = Paths.get(temp.getRoot().getPath(), "fluoreszenz-test.QCONF");
    targetf = Paths.get(temp.getRoot().getPath(), "fluoreszenz-test.tif kept stack.tif");
    FileUtils.copyFile(
            new File("src/test/Resources-static/ProtAnalysisTest/fluoreszenz-test.QCONF"),
            target.toFile());
    FileUtils.copyFile(new File(
            "src/test/Resources-static/ProtAnalysisTest/fluoreszenz-test.tif kept stack.tif"),
            targetf.toFile());
    Mockito.when(model.getQconfLoader()).thenReturn(new QconfLoader(target.toFile()));
    Mockito.when(ui.getImagePlus()).thenReturn(IJ.openImage(targetf.toString()));
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
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.ActionSaveTracks#actionPerformed(java.awt.event.ActionEvent)}.
   *
   * @throws Exception the exception
   */
  @Test
  public void testActionPerformed() throws Exception {
    ActionEvent e = Mockito.mock(ActionEvent.class);
    options.saveTracks.setTrue();
    actionSaveTracks = new ActionSaveTracks(ui);
    actionSaveTracks.actionPerformed(e);
    assertThat(Paths.get(temp.getRoot().getPath(), "tracks_0.csv").toFile().exists(), is(true));
    int lc = 0; // line count
    BufferedReader reader = new BufferedReader(
            new FileReader(Paths.get(temp.getRoot().getPath(), "tracks_0.csv").toFile()));
    String line;
    while ((line = reader.readLine()) != null) {
      if (!line.startsWith("#")) {
        lc++;
      }
    }
    reader.close();
    assertThat(lc, is(8));
  }

}
