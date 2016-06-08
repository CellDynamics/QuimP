/**
 * @file SerializerTest.java
 * @date 31 Mar 2016
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonSyntaxException;

/**
 * Test of Serializer class 
 * 
 * @author p.baniukiewicz
 * @date 31 Mar 2016
 *
 */
public class SerializerTest {
    static {
        System.setProperty("log4j.configurationFile", "qlog4j2.xml");
    }
    private static final Logger LOGGER = LogManager.getLogger(SerializerTest.class.getName());
    private TestClass testClass;
    private String[] version;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        testClass = new TestClass();
        version = new String[3];
        version[0] = "0.0.1";
        version[1] = "baniuk";
        version[2] = "QuimP";
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.save(String).
     */
    @Test
    public void testSave() throws Exception {
        Serializer<TestClass> s = new Serializer<>(testClass, version);
        s.save("/tmp/serializertest.josn");
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.toString().
     */
    @Test
    public void testToString() throws Exception {
        Serializer<TestClass> s = new Serializer<>(testClass, version);
        s.setPretty();
        LOGGER.debug(s.toString());
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.toString().
     */
    @Test
    public void testToString_1() throws Exception {
        Serializer<TestClass> s = new Serializer<>(testClass, version);
        LOGGER.debug(s.toString());
    }

    /**
     * @test Test method for uk.ac.warwick.wsbc.QuimP.Serializer.fromString(final String).
     * @pre missing important field
     * @post exception thrown
     * @throws Exception
     */
    @Test(expected = JsonSyntaxException.class)
    public void testFromString() throws Exception {
        String json =
                "{\"className\":\"TestClass\",\"version\":[\"0.0.1\",\"baniuk\",\"QuimP\"],\"obj\":{\"a\":15,\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class);
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertArrayEquals(out.version, version);
    }

    /**
     * @test Test method for uk.ac.warwick.wsbc.QuimP.Serializer.fromString(final String).
     * @throws Exception
     */
    @Test
    public void testFromString_1() throws Exception {
        String json =
                "{\"className\":\"TestClass\",\"createdOn\":\"1 2 3\",\"version\":[\"0.0.1\",\"baniuk\",\"QuimP\"],\"obj\":{\"a\":15,\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class);
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertArrayEquals(out.version, version);
    }

    /**
     * @test Test method for uk.ac.warwick.wsbc.QuimP.Serializer.fromString(final String).
     * @pre empty fields in version
     * @post exception
     * @throws Exception
     */
    @Test(expected = JsonSyntaxException.class)
    public void testFromString_2() throws Exception {
        String json =
                "{\"className\":\"TestClass\",\"createdOn\":\"1 2 3\",\"version\":[\"0.0.1\",\"QuimP\"],\"obj\":{\"a\":15,\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class);
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertArrayEquals(out.version, version);
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.load(final String)
     * @throws Exception
     */
    @Test
    public void testLoad() throws Exception {
        Serializer<TestClass> save = new Serializer<>(testClass, version);
        save.save("/tmp/local.josn");
        save = null;

        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class);
        out = s.load("/tmp/local.josn");
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertArrayEquals(out.version, version);
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.fromString(final String).
     * @pre Extra data in json
     * @post It is ignored
     * @throws Exception
     */
    @Test
    public void testFromString1() throws Exception {
        String json =
                "{\"className\":\"TestClass\",\"createdOn\":\"1 2 3\",\"version\":[\"0.0.1\",\"baniuk\",\"QuimP\"],\"obj\":{\"a\":15,\"b\":15,\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class);
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertArrayEquals(out.version, version);
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.fromString(final String).
     * @pre Lack of data
     * @post it is not initialized and has value from constructor
     * @throws Exception
     */
    @Test
    public void testFromString2() throws Exception {
        String json =
                "{\"className\":\"TestClass\",\"createdOn\":\"1 2 3\",\"version\":[\"0.0.1\",\"baniuk\",\"QuimP\"],\"obj\":{\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class);
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertArrayEquals(out.version, version);
    }

    @Test
    public void testDumpStatic() throws FileNotFoundException {
        TestClass tc = new TestClass();
        Serializer.Dump(tc, "/tmp/dump.json");
    }

}

/**
 * Dummy test class
 * 
 * @author p.baniukiewicz
 * @date 1 Apr 2016
 *
 */
class TestClass implements IQuimpSerialize {
    int a;
    int z;
    ArrayList<Integer> al;

    @Override
    public void beforeSerialize() {
        // TODO Auto-generated method stub

    }

    public TestClass() {
        a = 15;
        al = new ArrayList<>();
        al.add(4);
        al.add(56);
    }

    @Override
    public void afterSerialize() throws Exception {
        // TODO Auto-generated method stub

    }

}
