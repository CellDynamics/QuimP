package uk.ac.warwick.wsbc.quimp.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.warwick.wsbc.quimp.plugin.ParamList;

// TODO: Auto-generated Javadoc
/**
 * Test class for ParamList
 * 
 * @author p.baniukiewicz
 *
 */
public class ParamList_Test {

    private ParamList l;

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        l = new ParamList();
    }

    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        l = null;
    }

    /**
     * Test set int value.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSetIntValue() throws Exception {
        l.setIntValue("KEY", 10);
        assertEquals(10, l.getIntValue("key"));
    }

    /**
     * Test set double value.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSetDoubleValue() throws Exception {
        l.setDoubleValue("KEY", 10.1);
        assertEquals(10.1, l.getDoubleValue("key"), 1e-5);
    }

    /**
     * Test set string value.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSetStringValue() throws Exception {
        l.setStringValue("key", "v");
        assertEquals("v", l.getStringValue("Key"));
    }

    /**
     * Test set boolean value.
     *
     * @throws Exception the exception
     */
    @Test
    public void testSetBooleanValue() throws Exception {
        l.setBooleanValue("key", true);
        assertTrue(l.getBooleanValue("KEY"));
    }

    /**
     * Test put.
     *
     * @throws Exception the exception
     */
    @Test
    public void testPut() throws Exception {
        l.put("key", "v");
        assertEquals("v", l.get("Key"));
    }

    /**
     * Test contains key.
     *
     * @throws Exception the exception
     */
    @Test
    public void testContainsKey() throws Exception {
        l.put("Key", "1.1");
        assertTrue(l.containsKey("KEY"));

    }

    /**
     * Test put all.
     *
     * @throws Exception the exception
     */
    @Test
    public void testPutAll() throws Exception {
        HashMap<String, String> s = new HashMap<>();
        s.put("key1", "1");
        s.put("key2", "2");
        l.putAll(s);
        assertTrue(l.containsKey("KEY1") && l.containsKey("KEY2"));
    }

    /**
     * Test remove object.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRemoveObject() throws Exception {
        l.put("Key", "1.1");
        l.put("rem", "2");
        l.remove("REM");
        assertFalse(l.containsKey("rem"));
    }

    /**
     * Test remove object object.
     *
     * @throws Exception the exception
     */
    @Test
    public void testRemoveObjectObject() throws Exception {
        l.put("Key", "1.1");
        l.put("rem", "2");
        l.remove("REM", "2");
        assertFalse(l.containsKey("rem"));
    }

    /**
     * Test shallow copy.
     */
    @Test
    public void testShallowCopy() {
        HashMap<String, String> s = new HashMap<>();
        s.put("key1", "df");
        s.put("key2", "BH");
        l.putAll(s);
        assertTrue(l.containsKey("KEY1") && l.containsKey("KEY2"));

        ParamList copy = new ParamList(l); // make shallow copy
        assertTrue(copy.containsKey("KEY1") && copy.containsKey("KEY2"));
        assertEquals("df", copy.get("KEY1"));
        assertEquals("BH", copy.get("KEY2"));

    }
}
