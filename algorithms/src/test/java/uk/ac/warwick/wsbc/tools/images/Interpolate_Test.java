package uk.ac.warwick.wsbc.tools.images;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Suite class collecting all tests for Interpolate class
 * 
 * By default all those tests are named with small letter to prevent Maven
 * from executing them.
 * 
 * @author baniuk
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ Interpolate_test.class, InterpolateLoess_testParam.class, InterpolateMean_testParam.class })
public class Interpolate_Test {

}
