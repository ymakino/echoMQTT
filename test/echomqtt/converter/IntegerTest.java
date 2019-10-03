/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package echomqtt.converter;

import echomqtt.json.JValue;
import echowand.common.Data;
import java.math.BigInteger;
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
public class IntegerTest {
    
    public IntegerTest() {
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
     * Test of u method, of class Integer.
     */
    @Test
    public void testU() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "10");
        map.put("signed", "true");
        
        BigInteger num = new Integer(map){
            public BigInteger uu(Data data) {
                return u(data);
            }
        }.uu(new Data((byte)0xff, (byte)0xff));
        
        assertEquals(BigInteger.valueOf(0xffff), num);
    }

    /**
     * Test of s method, of class Integer.
     */
    @Test
    public void testS() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "10");
        map.put("signed", "true");
        
        BigInteger num = new Integer(map){
            public BigInteger ss(Data data) {
                return s(data);
            }
        }.ss(new Data((byte)0xff, (byte)0xff));
        
        assertEquals(BigInteger.valueOf(-1), num);
    }
    
    private Integer newConverter1(int size, boolean signed) throws ConverterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", String.valueOf(size));
        map.put("signed", String.valueOf(signed));
        return new Integer(map);
    }
    
    private Integer newConverter2(int size, boolean unsigned) throws ConverterException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", String.valueOf(size));
        map.put("unsigned", String.valueOf(unsigned));
        return new Integer(map);
    }

    /**
     * Test of isSigned method, of class Integer.
     */
    @Test
    public void testIsSigned() throws Exception {
        assertTrue(newConverter1(10, true).isSigned());
        assertFalse(newConverter1(10, false).isSigned());
        assertTrue(newConverter2(10, false).isSigned());
        assertFalse(newConverter2(10, true).isSigned());
    }

    /**
     * Test of getSize method, of class Integer.
     */
    @Test
    public void testGetSize() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "10");
        map.put("signed", "true");
        Integer c1 = new Integer(map);
        
        assertEquals(10, c1.getSize());
    }

    /**
     * Test of convert method, of class Integer.
     */
    @Test
    public void testConvert_Data() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "2");
        Integer c1 = new Integer(map);
        
        JValue jvalue = c1.convert(new Data((byte)0x04, (byte)0x00));
        
        assertEquals(JValue.parseJSON("1024"), jvalue);
    }
    
    @Test
    public void testConvert_Data_s() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "2");
        map.put("signed", "true");
        Integer c1 = new Integer(map);
        
        JValue jvalue = c1.convert(new Data((byte)0xff, (byte)0xff));
        
        assertEquals(JValue.parseJSON("-1"), jvalue);
    }
    
    @Test
    public void testConvert_Data_u() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "2");
        map.put("unsigned", "true");
        Integer c1 = new Integer(map);
        
        JValue jvalue = c1.convert(new Data((byte)0xff, (byte)0xff));
        
        assertEquals(JValue.parseJSON("65535"), jvalue);
    }

    /**
     * Test of convert method, of class Integer.
     */
    @Test
    public void testConvert_JValue() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "2");
        Integer c1 = new Integer(map);
        
        Data data = c1.convert(JValue.parseJSON("1024"));
        
        assertEquals(new Data((byte)0x04, (byte)0x00), data);
    }

    /**
     * Test of convert method, of class Integer.
     */
    @Test
    public void testConvert_JValue_s() throws Exception {
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("size", "2");
        map1.put("signed", "true");
        Integer c1 = new Integer(map1);
        
        Data data1 = c1.convert(JValue.parseJSON("65535"));
        
        assertEquals(new Data((byte)0x7f, (byte)0xff), data1);
        
        HashMap<String, String> map2 = new HashMap<String, String>();
        map2.put("size", "2");
        map2.put("signed", "true");
        Integer c2 = new Integer(map2);
        
        Data data2 = c2.convert(JValue.parseJSON("-32768"));
        
        assertEquals(new Data((byte)0x80, (byte)0x00), data2);
    }

    /**
     * Test of convert method, of class Integer.
     */
    @Test
    public void testConvert_JValue_u() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "2");
        map.put("unsigned", "true");
        Integer c1 = new Integer(map);
        
        Data data = c1.convert(JValue.parseJSON("65535"));
        
        assertEquals(new Data((byte)0xff, (byte)0xff), data);
    }
    
}
