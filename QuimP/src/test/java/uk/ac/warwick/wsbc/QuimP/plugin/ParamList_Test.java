package uk.ac.warwick.wsbc.QuimP.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for ParamList
 * 
 * @author p.baniukiewicz
 * @date 24 Feb 2016
 *
 */
public class ParamList_Test {
    // http://stackoverflow.com/questions/21083834/load-log4j2-configuration-file-programmatically
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private ParamList l;

    @Before
    public void setUp() throws Exception {
        l = new ParamList();
    }

    @After
    public void tearDown() throws Exception {
        l = null;
    }

    @Test
    public void testSetIntValue() throws Exception {
        l.setIntValue("KEY", 10);
        assertEquals(10, l.getIntValue("key"));
    }

    @Test
    public void testSetDoubleValue() throws Exception {
        l.setDoubleValue("KEY", 10.1);
        assertEquals(10.1, l.getDoubleValue("key"), 1e-5);
    }

    @Test
    public void testSetStringValue() throws Exception {
        l.setStringValue("key", "v");
        assertEquals("v", l.getStringValue("Key"));
    }

    @Test
    public void testSetBooleanValue() throws Exception {
        l.setBooleanValue("key", true);
        assertTrue(l.getBooleanValue("KEY"));
    }

    @Test
    public void testPut() throws Exception {
        l.put("key", "v");
        assertEquals("v", l.get("Key"));
    }

    @Test
    public void testContainsKey() throws Exception {
        l.put("Key", "1.1");
        assertTrue(l.containsKey("KEY"));

    }

    @Test
    public void testPutAll() throws Exception {
        HashMap<String, String> s = new HashMap<>();
        s.put("key1", "1");
        s.put("key2", "2");
        l.putAll(s);
        assertTrue(l.containsKey("KEY1") && l.containsKey("KEY2"));
    }

    @Test
    public void testRemoveObject() throws Exception {
        l.put("Key", "1.1");
        l.put("rem", "2");
        l.remove("REM");
        assertFalse(l.containsKey("rem"));
    }

    @Test
    public void testRemoveObjectObject() throws Exception {
        l.put("Key", "1.1");
        l.put("rem", "2");
        l.remove("REM", "2");
        assertFalse(l.containsKey("rem"));
    }
}
