/**
 * @file Loess_Test.java
 * @date 22 Feb 2016
 */

/**
 * @author p.baniukiewicz
 * @date 22 Feb 2016
 *
 */
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import uk.ac.warwick.wsbc.QuimP.plugin.utils.RoiSaver_test;

/**
 * Suite class collecting all tests for Filters
 * 
 * By default all those tests are named with small letter to prevent Maven from
 * executing them.
 * 
 * @author p.baniukiewicz
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ InterpolateLoess_testParam.class, RoiSaver_test.class })
public class Loess_Test {

}
