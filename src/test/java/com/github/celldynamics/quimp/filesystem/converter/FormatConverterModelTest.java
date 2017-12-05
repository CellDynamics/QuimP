package com.github.celldynamics.quimp.filesystem.converter;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.celldynamics.quimp.plugin.AbstractPluginOptions;

/**
 * @author p.baniukiewicz
 *
 */
public class FormatConverterModelTest {

  /**
   * The Constant LOGGER.
   */
  static final Logger LOGGER = LoggerFactory.getLogger(FormatConverterModelTest.class.getName());

  @Test
  public void testGetStatus() throws Exception {
    FormatConverterModel model = new FormatConverterModel();
    String js = model.serialize2Macro();
    LOGGER.debug(js);
    LOGGER.debug(AbstractPluginOptions.unescapeJsonMacro(js));

  }

}
