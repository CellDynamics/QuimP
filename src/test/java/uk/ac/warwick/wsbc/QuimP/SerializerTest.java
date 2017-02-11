/**
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Since;

import uk.ac.warwick.wsbc.QuimP.filesystem.DataContainer;
import uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize;
import uk.ac.warwick.wsbc.QuimP.filesystem.versions.Converter170202;

/**
 * Test of Serializer class
 * 
 * @author p.baniukiewicz
 *
 */
public class SerializerTest {

    /**
     * The Constant LOGGER.
     */
    static final Logger LOGGER = LoggerFactory.getLogger(SerializerTest.class.getName());

    /**
     * The tmpdir.
     */
    static String tmpdir = System.getProperty("java.io.tmpdir") + File.separator;

    private TestClass testClass;
    private QuimpVersion version;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        testClass = new TestClass();
        version = new QuimpVersion("17.02.02-SNAPSHOT", "p.baniukiewicz", "QuimP");
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.save(String).
     * 
     * @throws Exception
     */
    @Test
    public void testSave() throws Exception {
        Serializer<TestClass> s = new Serializer<>(testClass, version);
        s.save(tmpdir + "serializertest.josn");
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.toString().
     * 
     * @throws Exception
     */
    @Test
    public void testToString() throws Exception {
        Serializer<TestClass> s = new Serializer<>(testClass, version);
        s.setPretty();
        LOGGER.debug(s.toString());
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.toString().
     * 
     * @throws Exception
     */
    @Test
    public void testToString_1() throws Exception {
        Serializer<TestClass> s = new Serializer<>(testClass, version);
        LOGGER.debug(s.toString());
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.fromString(final String).
     * 
     * pre: missing variable in json
     * 
     * post: exception thrown
     * 
     * @throws Exception
     */
    @Test(expected = JsonSyntaxException.class)
    public void testFromString() throws Exception {
        String json =
                "{\"className\":\"DataContainer\",\"version\":[\"0.0.1\",\"p.baniukiewicz\",\"QuimP\"],\"obj\":{\"a\":15,\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s =
                new Serializer<>(TestClass.class, new QuimpVersion("0.00.01", "baniuk", "QuimP"));
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertEquals(out.timeStamp, version);
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.fromString(final String).
     * 
     * @throws Exception
     */
    @Test
    public void testFromString_1() throws Exception {
        String json =
                "{\"className\":\"DataContainer\",\"createdOn\":\"1 2 3\",\"version\":[\"0.0.1\",\"p.baniukiewicz\",\"QuimP\"],\"obj\":{\"a\":15,\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class, version);
        s.registerConverter(new Converter170202<>(version));
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertEquals(version, out.timeStamp);
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.fromString(final String).
     * 
     * pre: Correct json in format >17.02.02
     * 
     * post: Conversion class is not run
     * 
     * @throws Exception
     */
    @Test
    public void testFromString_3() throws Exception {
        String json =
                "{\"className\":\"DataContainer\",\"createdOn\":\"1 2 3\",\"timeStamp\":{\"version\":\"17.09.02-SNAPSHOT\",\"buildstamp\":\"p.baniukiewicz\",\"name\":\"QuimP\"},\"obj\":{\"a\":15,\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class, version);
        s.registerConverter(new Converter170202<>(version));
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertNotEquals(version, out.timeStamp);
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.fromString(final String).
     * 
     * pre: empty fields in version
     * 
     * post: exception
     * 
     * @throws Exception
     */
    @Test(expected = JsonSyntaxException.class)
    public void testFromString_2() throws Exception {
        QuimpVersion version = new QuimpVersion("17.02.02", "baniuk", "QuimP");
        String json =
                "{\"className\":\"DataContainer\",\"createdOn\":\"1 2 3\",\"versionn\":[\"0.0.1\",\"QuimP\"],\"obj\":{\"a\":15,\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class, version);
        s.registerConverter(new Converter170202<>(version));
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertEquals(out.timeStamp, version);
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.load(final String)
     * 
     * @throws Exception
     */
    @Test
    public void testLoad() throws Exception {
        Serializer<TestClass> save = new Serializer<>(testClass, version);
        save.save(tmpdir + "local.josn");
        save = null;

        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class, version);
        out = s.load(tmpdir + "local.josn");
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertEquals(out.timeStamp, version);
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.load(final String)
     * 
     * Pre: Load file in older version
     * 
     * Post the same file converted to current version
     * 
     * @throws Exception
     */
    @Test
    public void testLoad_1() throws Exception {
        Serializer<DataContainer> out;
        QuimpVersion toolversion = new QuimpVersion("20.20.02", "baniuk", "QuimP");
        // provide also current version of tool as in debug there is no jar
        Serializer<DataContainer> s = new Serializer<>(DataContainer.class, toolversion);
        // now define border trigger for this converter. Start it if version in json is lower than
        // defined in class
        s.registerConverter(new Converter170202<>(toolversion));
        out = s.load("src/test/resources/ticket199/fluoreszenz-test.QCONF");
        assertEquals(toolversion, out.timeStamp);
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.fromString(final String).
     * 
     * pre: Extra data in json
     * 
     * post: It is ignored
     * 
     * @throws Exception
     */
    @Test
    public void testFromString1() throws Exception {
        String json =
                "{\"className\":\"TestClass\",\"createdOn\":\"1 2 3\",\"version\":[\"0.0.1\",\"p.baniukiewicz\",\"QuimP\"],\"obj\":{\"a\":15,\"b\":15,\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class, version);
        s.registerConverter(new Converter170202<>(version));
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertEquals(out.timeStamp, version);
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.fromString(final String).
     * 
     * pre: Lack of data
     * 
     * post: it is not initialized and has value from constructor
     * 
     * @throws Exception
     */
    @Test
    public void testFromString2() throws Exception {
        String json =
                "{\"className\":\"TestClass\",\"createdOn\":\"1 2 3\",\"version\":[\"0.0.1\",\"p.baniukiewicz\",\"QuimP\"],\"obj\":{\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class, version);
        s.registerConverter(new Converter170202<>(version));
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertEquals(out.timeStamp, version);
    }

    /**
     * @throws FileNotFoundException
     */
    @Test
    public void testDumpStatic() throws FileNotFoundException {
        TestClass tc = new TestClass();
        Serializer.Dump(tc, tmpdir + "dump.json", true);
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.Serializer#getQconfVersion(Reader)}.
     * 
     * @throws Exception
     */
    @Test
    public void testGetQconfVersion() throws Exception {
        Serializer<TestClass> s = new Serializer<>(testClass, version);
        assertEquals(
                s.getQconfVersion(new BufferedReader(
                        new FileReader("src/test/resources/ticket199/fluoreszenz-test.QCONF"))),
                17.0103, 1e-5);

        Reader ret;
        ret = giveDummyFile("17.01.03-SNAPSHOT");
        assertEquals(s.getQconfVersion(ret), 17.0103, 1e-5);
        ret.close();

        ret = giveDummyFile("17.01.03");
        assertEquals(s.getQconfVersion(ret), 17.0103, 1e-5);
        ret.close();

        ret = giveDummyFile("1.01.0-SNAPSHOT");
        assertEquals(s.getQconfVersion(ret), 1.010, 1e-5);
        ret.close();

        ret = giveDummyFile("17.01-SNAPSHOT");
        assertEquals(s.getQconfVersion(ret), 17.01, 1e-5);
        ret.close();

        ret = giveDummyFile("1701-SNAPSHOT");
        assertEquals(s.getQconfVersion(ret), 1701, 1e-5);
        ret.close();

    }

    /**
     * @throws Exception
     */
    @Test(expected = Exception.class)
    public void testGetQconfVersion_bad() throws Exception {
        Serializer<TestClass> s = new Serializer<>(testClass, version);
        Reader ret;
        // bad cases
        ret = giveDummyFile("17.01.03_SNAPSHOT");
        assertEquals(s.getQconfVersion(ret), 0.0, 1e-5);
        ret.close();

    }

    /**
     * @throws Exception
     */
    @Test(expected = Exception.class)
    public void testGetQconfVersion_bad1() throws Exception {
        Serializer<TestClass> s = new Serializer<>(testClass, version);
        Reader ret;
        // bad cases
        ret = giveDummyFile("not found");
        assertEquals(s.getQconfVersion(ret), 0.0, 1e-5);
        ret.close();

    }

    /**
     * Helper method. Write pseudo file with QCONF extension and simulated QCONF structure.
     * 
     * @param ver Version to inject
     * @return Path to created file
     * @throws IOException
     */
    private Reader giveDummyFile(String ver) throws IOException {
        String qconf = "{" + "\"className\":\"DataContainer\"," + "\"version\": [" + "\"" + ver
                + "\"," + "\"baniuk on: 2017-01-25 14:56:43\",";
        return new StringReader(qconf);
    }

    // behaviour of GSon

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.save(String).
     * 
     * @throws Exception
     */
    @Test
    public void testSave_gson() throws Exception {
        version = new QuimpVersion("1.0.0", "p.baniukiewicz", "QuimP");
        TestClass_1 obj = new TestClass_1();
        Serializer<TestClass_1> s = new Serializer<>(obj, version);
        s.save(tmpdir + "testclass1.josn");
    }

    /**
     * Gson behaviour
     * 
     * Pre: Json contains all fields
     * 
     * Post: All fields are loaded
     * 
     * @throws Exception
     */
    @Test
    public void testGson_1() throws Exception {
        version = new QuimpVersion("1.0.0", "p.baniukiewicz", "QuimP");
        //!>
        String json ="{"
                + "\"className\":\"TestClass_1\","
                + "\"timeStamp\":{"
                    + "\"version\":\"1.0.0\","
                    + "\"buildstamp\":\"p.baniukiewicz\","
                    + "\"name\":\"QuimP\""
                    + "},"
                + "\"createdOn\":\"Sun 2017.02.12 at 12:25:00 PM GMT\","
                + "\"obj\":{"
                    + "\"a\":20,"
                    + "\"b\":25,"
                    + "\"c\":30,"
                    + "\"d\":35,"
                    + "\"e\":40"
                    + "}"
                + "}";
        //!<
        Serializer<TestClass_1> out;
        TestClass_1 obj;
        Serializer<TestClass_1> s = new Serializer<>(TestClass_1.class, version);
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(20, obj.a);
        assertEquals(25, obj.b);
        assertEquals(30, obj.c);
        assertEquals(35, obj.d);
        assertEquals(40, obj.e);
    }

    /**
     * Gson behaviour
     * 
     * Pre: Json lack of b field
     * 
     * Post: b filed has vale from constructor, no error
     * 
     * @throws Exception
     */
    @Test
    public void testGson_2() throws Exception {
        version = new QuimpVersion("1.0.0", "p.baniukiewicz", "QuimP");
        //!>
        String json ="{"
                + "\"className\":\"TestClass_1\","
                + "\"timeStamp\":{"
                    + "\"version\":\"1.0.0\","
                    + "\"buildstamp\":\"p.baniukiewicz\","
                    + "\"name\":\"QuimP\""
                    + "},"
                + "\"createdOn\":\"Sun 2017.02.12 at 12:25:00 PM GMT\","
                + "\"obj\":{"
                    + "\"a\":20,"
                    + "\"c\":30,"
                    + "\"d\":35,"
                    + "\"e\":40"
                    + "}"
                + "}";
        //!<
        Serializer<TestClass_1> out;
        TestClass_1 obj;
        Serializer<TestClass_1> s = new Serializer<>(TestClass_1.class, version);
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(20, obj.a);
        assertEquals(20, obj.b); // !
        assertEquals(30, obj.c);
        assertEquals(35, obj.d);
        assertEquals(40, obj.e);
    }

    /**
     * Gson behaviour
     * 
     * Pre: Json contains field f not available in object
     * 
     * Post: Class is restored normally, f is skipped
     * 
     * @throws Exception
     */
    @Test
    public void testGson_3() throws Exception {
        version = new QuimpVersion("1.0.0", "p.baniukiewicz", "QuimP");
        //!>
        String json ="{"
                + "\"className\":\"TestClass_1\","
                + "\"timeStamp\":{"
                    + "\"version\":\"1.0.0\","
                    + "\"buildstamp\":\"p.baniukiewicz\","
                    + "\"name\":\"QuimP\""
                    + "},"
                + "\"createdOn\":\"Sun 2017.02.12 at 12:25:00 PM GMT\","
                + "\"obj\":{"
                    + "\"a\":20,"
                    + "\"b\":25,"
                    + "\"c\":30,"
                    + "\"d\":35,"
                    + "\"e\":40,"
                    + "\"f\":40"
                    + "}"
                + "}";
        //!<
        Serializer<TestClass_1> out;
        TestClass_1 obj;
        Serializer<TestClass_1> s = new Serializer<>(TestClass_1.class, version);
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(20, obj.a);
        assertEquals(25, obj.b);
        assertEquals(30, obj.c);
        assertEquals(35, obj.d);
        assertEquals(40, obj.e);
    }

    /**
     * Gson behaviour
     * 
     * Pre: Json contains field f since 1.1 but calee is in 1.0
     * 
     * Post: Class is restored normally, f has default value from constructor
     * 
     * @throws Exception
     */
    @Test
    public void testGson_4() throws Exception {
        version = new QuimpVersion("1.0.0", "p.baniukiewicz", "QuimP");
        //!>
        String json ="{"
                + "\"className\":\"TestClass_2\","
                + "\"timeStamp\":{"
                    + "\"version\":\"1.0.0\","
                    + "\"buildstamp\":\"p.baniukiewicz\","
                    + "\"name\":\"QuimP\""
                    + "},"
                + "\"createdOn\":\"Sun 2017.02.12 at 12:25:00 PM GMT\","
                + "\"obj\":{"
                    + "\"a\":20,"
                    + "\"b\":25,"
                    + "\"c\":30,"
                    + "\"d\":35,"
                    + "\"e\":40,"
                    + "\"f\":40"
                    + "}"
                + "}";
        //!<
        Serializer<TestClass_2> out;
        TestClass_2 obj;
        Serializer<TestClass_2> s = new Serializer<>(TestClass_2.class, version);
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(20, obj.a);
        assertEquals(25, obj.b);
        assertEquals(30, obj.c);
        assertEquals(35, obj.d);
        assertEquals(40, obj.e);
        assertEquals(0, obj.f); // value from constructor
    }

    /**
     * Gson behaviour
     * 
     * Pre: Json contains field f since 1.1 but calee is in 1.2 and saved json is in the same
     * version
     * 
     * Post: Class is restored normally, f has value from json
     * 
     * @throws Exception
     */
    @Test
    public void testGson_5() throws Exception {
        version = new QuimpVersion("1.2.0", "p.baniukiewicz", "QuimP");
        //!>
        String json ="{"
                + "\"className\":\"TestClass_2\","
                + "\"timeStamp\":{"
                    + "\"version\":\"1.2.0\","
                    + "\"buildstamp\":\"p.baniukiewicz\","
                    + "\"name\":\"QuimP\""
                    + "},"
                + "\"createdOn\":\"Sun 2017.02.12 at 12:25:00 PM GMT\","
                + "\"obj\":{"
                    + "\"a\":20,"
                    + "\"b\":25,"
                    + "\"c\":30,"
                    + "\"d\":35,"
                    + "\"e\":40,"
                    + "\"f\":40"
                    + "}"
                + "}";
        //!<
        Serializer<TestClass_2> out;
        TestClass_2 obj;
        Serializer<TestClass_2> s = new Serializer<>(TestClass_2.class, version);
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(20, obj.a);
        assertEquals(25, obj.b);
        assertEquals(30, obj.c);
        assertEquals(35, obj.d);
        assertEquals(40, obj.e);
        assertEquals(40, obj.f);
    }

}

/**
 * Dummy test class with support GSon annotations.
 * 
 * @author p.baniukiewicz
 *
 */
class TestClass_2 implements IQuimpSerialize {
    int a;
    int b;
    int c;
    int d;
    int e;
    @Since(1.1)
    int f;

    @Override
    public void beforeSerialize() {
        // TODO Auto-generated method stub

    }

    public TestClass_2() {
        a = 15;
        b = 20;
        c = 30;
    }

    @Override
    public void afterSerialize() throws Exception {
        // TODO Auto-generated method stub

    }

}

/**
 * Dummy test class.
 * 
 * @author p.baniukiewicz
 *
 */
class TestClass_1 implements IQuimpSerialize {
    int a;
    int b;
    int c;
    int d;
    int e;

    @Override
    public void beforeSerialize() {
        // TODO Auto-generated method stub

    }

    public TestClass_1() {
        a = 15;
        b = 20;
        c = 30;
    }

    @Override
    public void afterSerialize() throws Exception {
        // TODO Auto-generated method stub

    }

}

/**
 * Dummy test class
 * 
 * @author p.baniukiewicz
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
