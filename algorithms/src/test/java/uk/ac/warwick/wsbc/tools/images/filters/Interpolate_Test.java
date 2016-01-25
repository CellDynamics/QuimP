package uk.ac.warwick.wsbc.tools.images.filters;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Suite class collecting all tests for Interpolate class
 * 
 * By default all those tests are named with small letter to prevent Maven
 * from executing them.
 * 
 * @author p.baniukiewicz
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ MeanFilter_test.class,
	InterpolateLoess_testParam.class,
	MeanFilter_testParam.class,
	IPadArray_test.class,
	HatFilter_test.class})
public class Interpolate_Test {

}
