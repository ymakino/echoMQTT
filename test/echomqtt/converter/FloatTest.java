/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package echomqtt.converter;

import echomqtt.json.JValue;
import echomqtt.json.JsonDecoderException;
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
public class FloatTest {
    
    public FloatTest() {
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
     * Test of convertData method, of class Float.
     */
    @Test
    public void testConvert_JValue() throws ConverterException, JsonDecoderException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "2");
        map.put("divide", "10");
        Float c1 = new Float(map);
        
        Data data = c1.convert(JValue.parseJSON("1024.6"));
        
        assertEquals(new Data((byte)0x28, (byte)0x06), data);
    }

    /**
     * Test of convertJValue method, of class Float.
     */
    @Test
    public void testConvert_Data() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "2");
        map.put("divide", "10");
        Float c1 = new Float(map);
        
        JValue jvalue = c1.convert(new Data((byte)0x28, (byte)0x06));
        
        assertEquals(JValue.parseJSON("1024.6"), jvalue);
    }
    
}
