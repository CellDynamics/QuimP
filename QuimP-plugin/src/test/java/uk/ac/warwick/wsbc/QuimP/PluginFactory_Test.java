/**
 * @file PluginFactoryTest.java
 * @date 9 Feb 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import java.nio.file.Path;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author p.baniukiewicz
 * @date 9 Feb 2016
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PluginFactory_Test {
	@Mock
	private Path path;
	@InjectMocks
	private PluginFactory pluginFactory;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link uk.ac.warwick.wsbc.QuimP.PluginFactory#PluginFactory(java.nio.file.Path)}.
	 */
	@Test
	public void test_PluginFactory() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for {@link uk.ac.warwick.wsbc.QuimP.PluginFactory#getPluginNames(int)}.
	 */
	@Test
	public void test_GetPluginNames() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for {@link uk.ac.warwick.wsbc.QuimP.PluginFactory#getInstance(java.lang.String)}.
	 */
	@Test
	public void test_GetInstance() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

}
