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
public class MapTest {
    
    public MapTest() {
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
     * Test of convert method, of class Map.
     */
    @Test
    public void testConvert_Data() throws Exception {        
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("mapping", "{\"30\":\"ON\", \"31\":\"OFF\"}");
        params.put("case-insensitive", "true");
        Map map = new Map(params);
        
        Data data1 = new Data((byte)0x30);
        JValue result1 = map.convert(data1);
        assertEquals(JValue.newString("ON"), result1);
        
        Data data2 = new Data((byte)0x31);
        JValue result2 = map.convert(data2);
        assertEquals(JValue.newString("OFF"), result2);
    }
    
    @Test(expected=ConverterException.class)
    public void testConvert_Data_NotExists() throws Exception {        
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("mapping", "{\"30\":\"ON\", \"31\":\"OFF\"}");
        params.put("case-insensitive", "true");
        Map map = new Map(params);
        
        Data data = new Data((byte)0x32);
        JValue result = map.convert(data);
        assertEquals(JValue.newString("OFF"), result);
    }

    /**
     * Test of convert method, of class Map.
     */
    @Test
    public void testConvert_JValue_String() throws Exception {
        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("mapping", "{\"ON\":\"30\", \"OFF\":\"31\"}");
        params1.put("case-insensitive", "true");
        Map map1 = new Map(params1);
        
        JValue value1 = JValue.newString("ON");
        Data result1 = map1.convert(value1);
        assertEquals(new Data(new byte[]{0x30}), result1);
        
        JValue value2 = JValue.newString("On");
        Data result2 = map1.convert(value2);
        assertEquals(new Data(new byte[]{0x30}), result2);
    }
    
    @Test(expected=ConverterException.class)
    public void testConvert_JValue_String_NotExists() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("mapping", "{\"ON\":\"30\", \"OFF\":\"31\"}");
        params.put("case-insensitive", "true");
        Map map = new Map(params);
        
        JValue value = JValue.newString("Invalid");
        map.convert(value);
    }
    
    @Test(expected=ConverterException.class)
    public void testConvert_JValue_String_CaseSensitive() throws Exception {
        HashMap<String, String> params1 = new HashMap<String, String>();
        params1.put("mapping", "{\"ON\":\"30\", \"OFF\":\"31\"}");
        params1.put("case-sensitive", "true");
        Map map1 = new Map(params1);
        
        JValue value1 = JValue.newString("ON");
        Data result1 = map1.convert(value1);
        assertEquals(new Data(new byte[]{0x30}), result1);
        
        JValue value2 = JValue.newString("On");
        Data result2 = map1.convert(value2);
        assertEquals(new Data(new byte[]{0x30}), result2);
    }
    
    @Test
    public void testConvert_JValue_Number() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("mapping", "{\"30\":\"40\", \"31\":\"ff\"}");
        Map map = new Map(params);
        
        JValue value1 = JValue.newNumber(30);
        Data result1 = map.convert(value1);
        assertEquals(new Data(new byte[]{0x40}), result1);
        
        JValue value2 = JValue.newNumber(31);
        Data result2 = map.convert(value2);
        assertEquals(new Data(new byte[]{(byte)0xff}), result2);
    }
    
    @Test
    public void testConvert_JValue_Boolean() throws Exception {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("mapping", "{\"true\":\"7f\", \"false\":\"80\"}");
        Map map = new Map(params);
        
        JValue value1 = JValue.newBoolean(true);
        Data result1 = map.convert(value1);
        assertEquals(new Data(new byte[]{0x7f}), result1);
        
        JValue value2 = JValue.newBoolean(false);
        Data result2 = map.convert(value2);
        assertEquals(new Data(new byte[]{(byte)0x80}), result2);
    }
    
}
