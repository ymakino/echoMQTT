/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package echomqtt.converter;

import echomqtt.json.JValue;
import echowand.common.Data;
import java.util.HashMap;
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
public class TextTest {
    
    public TextTest() {
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
     * Test of getSize method, of class Text.
     */
    @Test
    public void testGetSize() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        
        Text c0 = new Text(map);
        assertEquals(-1, c0.getSize());
        
        map.put("size", "10");
        Text c1 = new Text(map);
        
        assertEquals(10, c1.getSize());
    }

    /**
     * Test of convert method, of class Text.
     */
    @Test
    public void testConvert_Data() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        Text c1 = new Text(map);
        
        JValue jvalue = c1.convert(new Data((byte)0x41, (byte)0x42, (byte)0x43));
        
        assertEquals(JValue.parseJSON("\"ABC\""), jvalue);
    }
    
    @Test
    public void testConvert_Data_with_size() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "4");
        Text c1 = new Text(map);
        
        JValue jvalue = c1.convert(new Data((byte)0x41, (byte)0x42, (byte)0x43));
        
        assertEquals(JValue.parseJSON("\"ABC\""), jvalue);
    }
    
    @Test
    public void testConvert_Data_smaller() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "2");
        Text c1 = new Text(map);
        
        JValue jvalue = c1.convert(new Data((byte)0x41, (byte)0x42, (byte)0x43));
        
        assertEquals(JValue.parseJSON("\"ABC\""), jvalue);
    }
    
    @Test
    public void testConvert_Data_bigger() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "400");
        Text c1 = new Text(map);
        
        JValue jvalue = c1.convert(new Data((byte)0x41, (byte)0x42, (byte)0x43));
        
        assertEquals(JValue.parseJSON("\"ABC\""), jvalue);
    }
    
    @Test
    public void testConvert_Data_eucjp() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "4");
        map.put("encoding", "EUC-JP");
        Text c1 = new Text(map);
        
        JValue jvalue = c1.convert(new Data((byte)0xa4, (byte)0xa2, (byte)0xa4, (byte)0xa4));
        
        assertEquals(JValue.parseJSON("\"あい\""), jvalue);
    }

    /**
     * Test of convert method, of class Text.
     */
    @Test
    public void testConvert_JValue() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        Text c1 = new Text(map);
        
        Data data = c1.convert(JValue.parseJSON("\"ABC\""));
        
        assertEquals(new Data((byte)0x41, (byte)0x42, (byte)0x43), data);
    }
    
    @Test
    public void testConvert_JValue_with_size() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "4");
        Text c1 = new Text(map);
        
        Data data = c1.convert(JValue.parseJSON("\"ABC\""));
        
        assertEquals(new Data((byte)0x41, (byte)0x42, (byte)0x43, (byte)0x00), data);
    }
    
    @Test(expected = ConverterException.class)
    public void testConvert_JValue_smaller() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "2");
        Text c1 = new Text(map);
        
        c1.convert(JValue.parseJSON("\"ABC\""));
    }
    
    @Test
    public void testConvert_JValue_bigger() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "6");
        Text c1 = new Text(map);
        
        Data data = c1.convert(JValue.parseJSON("\"ABC\""));
        
        assertEquals(new Data((byte)0x41, (byte)0x42, (byte)0x43, (byte)0x00, (byte)0x00, (byte)0x00), data);
    }
    @Test
    public void testConvert_JValue_eucjp() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "4");
        map.put("encoding", "EUC-JP");
        Text c1 = new Text(map);
        
        Data data = c1.convert(JValue.parseJSON("\"あい\""));
        
        assertEquals(new Data((byte)0xa4, (byte)0xa2, (byte)0xa4, (byte)0xa4), data);
    }
}
