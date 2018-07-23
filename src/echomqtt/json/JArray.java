package echomqtt.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class JArray extends JValue {
    private static final Logger logger = Logger.getLogger(JArray.class.getName());
    private static final String className = JArray.class.getName();
    
    private List<JValue> jvalueList;
    
    /**
     * 指定された値を要素として持つ配列を生成する。
     * @param jvalues 配列の要素の値の指定
     */
    protected JArray(List<? extends JValue> jvalues) {
        logger.entering(className, "JArray", jvalues);
        
        jvalueList = new ArrayList<JValue>(jvalues);
        
        logger.entering(className, "JArray");
    }
    
    /**
     * 指定された値を要素として持つ配列を生成する。
     * @param jvalues 配列の要素の値の指定
     */
    protected JArray(JValue... jvalues) {
        logger.entering(className, "JArray", jvalues);
        
        jvalueList = Arrays.asList(jvalues);
        
        logger.entering(className, "JArray");
    }
    
    /**
     * 配列の要素数を返す。
     * @return 配列の要素数
     */
    public int size() {
        return jvalueList.size();
    }
    
    /**
     * 指定された番号に格納されている配列の要素を返す。
     * @param index 要素の番号の指定
     * @return 配列の要素
     */
    public JValue get(int index) {
        return jvalueList.get(index);
    }
    
    /**
     * 指定された番号の要素を、指定された要素の値で置き換えた配列を生成する。
     * @param index 要素の番号の指定
     * @param value 要素の値の指定
     * @return 指定された通りに要素が置き換えられた配列
     */
    public JArray alter(int index, JValue value) {
        logger.entering(className, "alter", new Object[]{index, value});
        
        ArrayList<JValue> newArray = new ArrayList<JValue>(jvalueList);
        newArray.add(index, value);
        JArray resultValue = new JArray(newArray);
        
        logger.exiting(className, "alter", resultValue);
        return resultValue;
    }
    
    /**
     * 指定された番号の要素を、指定された要素で置き換えた配列を生成する。
     * 番号と要素の組はMapを利用して渡される。
     * @param valueMap 要素番号と要素の値の対応
     * @return 指定された通りに要素が置き換えられた配列
     */
    public JArray alter(Map<Integer, ? extends JValue> valueMap) {
        logger.entering(className, "alter", valueMap);
        
        ArrayList<JValue> newArray = new ArrayList<JValue>(jvalueList);
        for (Map.Entry<Integer, ? extends JValue> entry: valueMap.entrySet()) {
            newArray.add(entry.getKey(), entry.getValue());
        }
        
        JArray resultValue = new JArray(newArray);
        
        logger.exiting(className, "alter", resultValue);
        return resultValue;
    }
    
    /**
     * 指定された要素を最後尾に追加した配列を生成する。
     * @param values 追加する要素の値の指定
     * @return 要素が追加された配列
     */
    public JArray alterAppend(JValue... values) {
        logger.entering(className, "alterAppend", values);
        
        JArray resultValue = alterAppend(Arrays.asList(values));
        
        logger.exiting(className, "alterAppend", resultValue);
        return resultValue;
    }
    
    /**
     * 指定された要素を最後尾に追加した配列を生成する。
     * @param values 追加する要素の値の指定
     * @return 要素が追加された配列
     */
    public JArray alterAppend(List<? extends JValue> values) {
        logger.entering(className, "alterAppend", values);
        
        ArrayList<JValue> newArray = new ArrayList<JValue>(jvalueList.size() + values.size());
        newArray.addAll(jvalueList);
        newArray.addAll(values);
        JArray resultValue = new JArray(newArray);
        
        logger.exiting(className, "alterAppend", resultValue);
        return resultValue;
    }
    
    /**
     * サイズを変更した配列を生成する。
     * この配列よりもサイズの指定が大きい場合には、指定された値でパディングされる。
     * @param size サイズの指定
     * @param padding パディング用の値の指定
     * @return サイズが変更された配列
     */
    public JArray alterSize(int size, JValue padding) {
        logger.entering(className, "alterSize", size);
        
        if (size < 0) {
            size = 0;
        }
        
        ArrayList<JValue> newArray = new ArrayList<JValue>(size);
        int minSize = Math.min(jvalueList.size(), size);
        for (int i=0; i<minSize; i++) {
            newArray.add(jvalueList.get(i));
        }
        
        for (int i=minSize; i<size; i++) {
            newArray.add(padding);
        }
        
        JArray resultValue = new JArray(newArray);
        
        logger.exiting(className, "alterSize", resultValue);
        return resultValue;
    }
    
    @Override
    public boolean isArray() {
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(get(i).toString());
        }
        builder.append("]");
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JArray)) {
            return false;
        }
        
        JArray valueArray = (JArray)object;
        
        if (size() != valueArray.size()) {
            return false;
        }
        
        int count = size();
        for (int i=0; i<count; i++) {
            if (!get(i).equals(valueArray.get(i))) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }
}
