/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package echomqtt.json;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ymakino
 */
public class JsonDecoderTest {
    
    public JsonDecoderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of parsePartial method, of class JsonDecoder.
     */
    @Test
    public void testParsePartial() throws Exception {
        System.out.println("parsePartial");
        JsonDecoder instance = new JsonDecoder("[-1,0,1, \"hoge\", {}, null]");
        JArray result = instance.parsePartial().asArray();
        assertEquals(6, result.size());
        assertEquals(-1, result.get(0).asNumber().getValue().intValue());
        assertEquals(0, result.get(1).asNumber().getValue().intValue());
        assertEquals(1, result.get(2).asNumber().getValue().intValue());
        assertEquals("hoge", result.get(3).asString().getValue());
        assertEquals(0, result.get(4).asObject().getMembers().size());
        assertTrue(result.get(5).isNull());
    }

    /**
     * Test of parse method, of class JsonDecoder.
     */
    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        JsonDecoder instance = new JsonDecoder("[-1,0,1, \"hoge\", {}, null]");
        JArray result = instance.parsePartial().asArray();
        assertEquals(6, result.size());
        assertEquals(-1, result.get(0).asNumber().getValue().intValue());
        assertEquals(0, result.get(1).asNumber().getValue().intValue());
        assertEquals(1, result.get(2).asNumber().getValue().intValue());
        assertEquals("hoge", result.get(3).asString().getValue());
        assertEquals(0, result.get(4).asObject().getMembers().size());
        assertTrue(result.get(5).isNull());
    }

    /**
     * Test of decode method, of class JsonDecoder.
     */
    @Test
    public void testDecode() throws Exception {
        System.out.println("decode");
        JArray result = JsonDecoder.decode("[-1,0,1, \"hoge\", {}, null]").asArray();
        assertEquals(6, result.size());
        assertEquals(-1, result.get(0).asNumber().getValue().intValue());
        assertEquals(0, result.get(1).asNumber().getValue().intValue());
        assertEquals(1, result.get(2).asNumber().getValue().intValue());
        assertEquals("hoge", result.get(3).asString().getValue());
        assertEquals(0, result.get(4).asObject().getMembers().size());
        assertTrue(result.get(5).isNull());
    }
}
