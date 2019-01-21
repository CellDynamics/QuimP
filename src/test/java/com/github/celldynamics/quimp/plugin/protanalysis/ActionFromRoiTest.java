package com.github.celldynamics.quimp.plugin.protanalysis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test of {@link ActionFromRoi}.
 * 
 * @author p.baniukiewicz
 *
 */
public class ActionFromRoiTest {

  /**
   * Test method for
   * {@link ActionFromRoi#stripCellNo(java.lang.String)}.
   * 
   * @throws Exception on error
   */
  @Test
  public void testStripCellNo() throws Exception {
    CustomStackWindow cs = Mockito.mock(CustomStackWindow.class);
    Prot_Analysis model = Mockito.mock(Prot_Analysis.class);
    Mockito.when(model.getOptions()).thenReturn(new ProtAnalysisOptions());
    Mockito.when(cs.getModel()).thenReturn(Mockito.mock(Prot_Analysis.class));
    ActionFromRoi obj = new ActionFromRoi("test", "test", cs);

    int s1 = obj.stripCellNo(ProtAnalysisOptions.roiPrefix + "0_1");
    assertEquals(0, s1);

    int s2 = obj.stripCellNo(ProtAnalysisOptions.roiPrefix + "12.1");
    assertEquals(12, s2);

    try {
      obj.stripCellNo(ProtAnalysisOptions.roiPrefix + "d12.1");
      fail("Expected exception");
    } catch (NumberFormatException e) {
      ;
    }

  }

}
