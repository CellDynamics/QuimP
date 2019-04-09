package com.github.celldynamics.quimp.plugin.protanalysis;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasItemInArray;
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

import ij.ImagePlus;
import ij.WindowManager;

/**
 * Plot Map test.
 * 
 * @author p.baniukiewicz
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionPlotMapTest {
  /**
   * temp folder.
   */
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();

  @Mock
  private Prot_Analysis model;
  @Mock
  private ProtAnalysisOptions options;
  @Mock
  private ProtAnalysisUi ui;
  private ActionPlotMap actionPlotMap;
  private Path target;
  private ImagePlus curr;

  /**
   * Setup mocks.
   * 
   * @throws java.lang.Exception on error
   */
  @Before
  public void setUp() throws Exception {
    Mockito.when(ui.getModel()).thenReturn(model);
    Mockito.when(model.getOptions()).thenReturn(new ProtAnalysisOptions());
    target = Paths.get(temp.getRoot().getPath(), "fluoreszenz-test.QCONF");
    FileUtils.copyFile(
            new File("src/test/Resources-static/ProtAnalysisTest/fluoreszenz-test.QCONF"),
            target.toFile());
    Mockito.when(model.getQconfLoader()).thenReturn(new QconfLoader(target.toFile()));

  }

  /**
   * Close all windows.
   * 
   * @throws Exception on error
   */
  @After
  public void clear() throws Exception {
    if (curr != null) {
      curr.close();
      Thread.sleep(100);
    }
    WindowManager.closeAllWindows();
    Thread.sleep(700);
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.ActionPlotMap#actionPerformed(java.awt.event.ActionEvent)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testActionPerformed_MOT_Un() throws Exception {
    ActionEvent e = Mockito.mock(ActionEvent.class);
    Mockito.when(e.getModifiers()).thenReturn(ActionEvent.CTRL_MASK);
    actionPlotMap = new ActionPlotMap("", "", ui, "MOT");
    actionPlotMap.actionPerformed(e);

    assertThat(WindowManager.getImageCount(), is(1));
    String[] titles = WindowManager.getImageTitles();
    assertThat(titles, hasItemInArray("MotilityMap_cell_0"));

    curr = WindowManager.getCurrentImage();
    assertThat(curr.getWidth(), is(400));
    assertThat(curr.getHeight(), is(405));
    assertThat(curr.getBitDepth(), is(32));
    curr.close();
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.ActionPlotMap#actionPerformed(java.awt.event.ActionEvent)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testActionPerformed_MOT_Sc() throws Exception {
    ActionEvent e = Mockito.mock(ActionEvent.class);
    Mockito.when(e.getModifiers()).thenReturn(ActionEvent.SHIFT_MASK);
    actionPlotMap = new ActionPlotMap("", "", ui, "MOT");
    actionPlotMap.actionPerformed(e);

    assertThat(WindowManager.getImageCount(), is(1));
    String[] titles = WindowManager.getImageTitles();
    assertThat(titles, hasItemInArray("MotilityMap_cell_0"));

    curr = WindowManager.getCurrentImage();
    assertThat(curr.getWidth(), is(400));
    assertThat(curr.getHeight(), is(405));
    assertThat(curr.getBitDepth(), is(24)); // RGB
    curr.close();
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.ActionPlotMap#actionPerformed(java.awt.event.ActionEvent)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testActionPerformed_CON_Un() throws Exception {
    ActionEvent e = Mockito.mock(ActionEvent.class);
    Mockito.when(e.getModifiers()).thenReturn(ActionEvent.CTRL_MASK);
    actionPlotMap = new ActionPlotMap("", "", ui, "CONV");
    actionPlotMap.actionPerformed(e);

    assertThat(WindowManager.getImageCount(), is(1));
    String[] titles = WindowManager.getImageTitles();
    assertThat(titles, hasItemInArray("ConvexityMap_cell_0"));

    curr = WindowManager.getCurrentImage();
    assertThat(curr.getWidth(), is(400));
    assertThat(curr.getHeight(), is(405));
    assertThat(curr.getBitDepth(), is(32));
    curr.close();
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.ActionPlotMap#actionPerformed(java.awt.event.ActionEvent)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testActionPerformed_CON_Sc() throws Exception {
    ActionEvent e = Mockito.mock(ActionEvent.class);
    Mockito.when(e.getModifiers()).thenReturn(ActionEvent.SHIFT_MASK);
    actionPlotMap = new ActionPlotMap("", "", ui, "CONV");
    actionPlotMap.actionPerformed(e);

    assertThat(WindowManager.getImageCount(), is(1));
    String[] titles = WindowManager.getImageTitles();
    assertThat(titles, hasItemInArray("ConvexityMap_cell_0"));

    curr = WindowManager.getCurrentImage();
    assertThat(curr.getWidth(), is(400));
    assertThat(curr.getHeight(), is(405));
    assertThat(curr.getBitDepth(), is(24)); // RGB
    curr.close();
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.ActionPlotMap#actionPerformed(java.awt.event.ActionEvent)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testActionPerformed_FLU_Un() throws Exception {
    ActionEvent e = Mockito.mock(ActionEvent.class);
    Mockito.when(e.getModifiers()).thenReturn(ActionEvent.CTRL_MASK);
    actionPlotMap = new ActionPlotMap("", "", ui, "FLU");
    actionPlotMap.actionPerformed(e);

    assertThat(WindowManager.getImageCount(), is(1));
    String[] titles = WindowManager.getImageTitles();
    assertThat(titles[0].contains("fluo_map_cell_0_fluoCH"), is(true));

    curr = WindowManager.getCurrentImage();
    assertThat(curr.getWidth(), is(400));
    assertThat(curr.getHeight(), is(405));
    assertThat(curr.getBitDepth(), is(32));
    curr.close();
  }

  /**
   * Test method for
   * {@link com.github.celldynamics.quimp.plugin.protanalysis.ActionPlotMap#actionPerformed(java.awt.event.ActionEvent)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testActionPerformed_FLU_Sc() throws Exception {
    ActionEvent e = Mockito.mock(ActionEvent.class);
    Mockito.when(e.getModifiers()).thenReturn(ActionEvent.SHIFT_MASK);
    actionPlotMap = new ActionPlotMap("", "", ui, "FLU");
    actionPlotMap.actionPerformed(e);

    assertThat(WindowManager.getImageCount(), is(1));
    String[] titles = WindowManager.getImageTitles();
    assertThat(titles[0].contains("fluo_map_cell_0_fluoCH"), is(true));

    curr = WindowManager.getCurrentImage();
    assertThat(curr.getWidth(), is(400));
    assertThat(curr.getHeight(), is(405));
    assertThat(curr.getBitDepth(), is(8)); // byte map
    curr.close();
  }

}
