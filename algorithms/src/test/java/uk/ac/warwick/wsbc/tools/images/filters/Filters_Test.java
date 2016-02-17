package uk.ac.warwick.wsbc.tools.images.filters;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

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
@SuiteClasses({ MeanFilter_test.class, InterpolateLoess_testParam.class,
        MeanFilter_testParam.class,
        HatFilter_test.class, HatFilter_testParam.class, RoiSaver_test.class })
public class Filters_Test {

}
