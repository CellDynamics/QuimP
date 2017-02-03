/**
 */
package uk.ac.warwick.wsbc.QuimP;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

import uk.ac.warwick.wsbc.QuimP.filesystem.IQuimpSerialize;

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
        version = new QuimpVersion("0.0.1", "p.baniukiewicz", "QuimP");
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
    @Ignore
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
    @Ignore
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
    @Ignore
    public void testToString_1() throws Exception {
        Serializer<TestClass> s = new Serializer<>(testClass, version);
        LOGGER.debug(s.toString());
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.fromString(final String).
     * 
     * pre: missing important field
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
        assertEquals(out.version, version);
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
        Serializer<TestClass> s =
                new Serializer<>(TestClass.class, new QuimpVersion("0.00.01", "baniuk", "QuimP"));
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertEquals(out.version, version);
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
        String json =
                "{\"className\":\"DataContainer\",\"createdOn\":\"1 2 3\",\"version\":[\"0.0.1\",\"QuimP\"],\"obj\":{\"a\":15,\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s =
                new Serializer<>(TestClass.class, new QuimpVersion("0.00.01", "baniuk", "QuimP"));
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertEquals(out.version, version);
    }

    /**
     * Test method for uk.ac.warwick.wsbc.QuimP.Serializer.load(final String)
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void testLoad() throws Exception {
        Serializer<TestClass> save = new Serializer<>(testClass, version);
        save.save(tmpdir + "local.josn");
        save = null;

        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class);
        out = s.load(tmpdir + "local.josn");
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertEquals(out.version, version);
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
    @Ignore
    public void testFromString1() throws Exception {
        String json =
                "{\"className\":\"TestClass\",\"createdOn\":\"1 2 3\",\"version\":[\"0.0.1\",\"p.baniukiewicz\",\"QuimP\"],\"obj\":{\"a\":15,\"b\":15,\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class);
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertEquals(out.version, version);
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
    @Ignore
    public void testFromString2() throws Exception {
        String json =
                "{\"className\":\"TestClass\",\"createdOn\":\"1 2 3\",\"version\":[\"0.0.1\",\"p.baniukiewicz\",\"QuimP\"],\"obj\":{\"al\":[4,56]}}";
        Serializer<TestClass> out;
        TestClass obj;
        Serializer<TestClass> s = new Serializer<>(TestClass.class);
        out = s.fromString(json);
        obj = out.obj;
        assertEquals(testClass.al, obj.al);
        assertEquals(testClass.a, obj.a);
        assertEquals(out.version, version);
    }

    /**
     * @throws FileNotFoundException
     */
    @Test
    @Ignore
    public void testDumpStatic() throws FileNotFoundException {
        TestClass tc = new TestClass();
        Serializer.Dump(tc, tmpdir + "dump.json", true);
    }

    /**
     * Test method for {@link uk.ac.warwick.wsbc.QuimP.Serializer#getVersion(java.lang.String)}.
     * 
     * @throws Exception
     */
    @Test
    @Ignore
    public void testGetVersion() throws Exception {
        Path ret;
        assertEquals(Serializer.getVersion("src/test/resources/ticket199/fluoreszenz-test.QCONF"),
                17.0103, 1e-5);

        ret = writeDummyFile("17.01.03-SNAPSHOT");
        assertEquals(Serializer.getVersion(ret.toString()), 17.0103, 1e-5);

        ret = writeDummyFile("17.01.03");
        assertEquals(Serializer.getVersion(ret.toString()), 17.0103, 1e-5);

        ret = writeDummyFile("1.01.0-SNAPSHOT");
        assertEquals(Serializer.getVersion(ret.toString()), 1.010, 1e-5);

        ret = writeDummyFile("17.01-SNAPSHOT");
        assertEquals(Serializer.getVersion(ret.toString()), 17.01, 1e-5);

        ret = writeDummyFile("1701-SNAPSHOT");
        assertEquals(Serializer.getVersion(ret.toString()), 1701, 1e-5);

        ret = writeDummyFile("17.01.03_SNAPSHOT");
        assertEquals(Serializer.getVersion(ret.toString()), 0.0, 1e-5);

        ret = writeDummyFile("not found");
        assertEquals(Serializer.getVersion(ret.toString()), 0.0, 1e-5);

    }

    /**
     * Helper method. Write pseudo file with QCONF extension and simulated QCONF structure.
     * 
     * @param ver Version to inject
     * @return Path to created file
     * @throws IOException
     */
    private Path writeDummyFile(String ver) throws IOException {
        File filename = File.createTempFile("serializer", ".QCONF");
        PrintWriter pw = new PrintWriter(filename);
        pw.write("{\n");
        pw.write("  \"className\": \"DataContainer\",\n");
        pw.write("  \"version\": [\n");
        pw.write("    \"" + ver + "\",\n");
        pw.write("    \"baniuk on: 2017-01-25 14:56:43\",\n");
        pw.close();
        return filename.toPath();
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
