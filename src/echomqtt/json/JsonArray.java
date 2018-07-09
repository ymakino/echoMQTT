package echomqtt.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Noteの引数及び戻り値として利用される配列を表す。
 * @author ymakino
 */
public class JsonArray extends JsonValue {
    private static final Logger logger = Logger.getLogger(JsonArray.class.getName());
    private static final String className = JsonArray.class.getName();
    
    private List<JsonValue> valueList;
    
    /**
     * 指定された値を要素として持つ配列を生成する。
     * @param values 配列の要素の値の指定
     */
    protected JsonArray(List<? extends JsonValue> values) {
        logger.entering(className, "ValueArray", values);
        
        valueList = new ArrayList<JsonValue>(values);
        
        logger.entering(className, "ValueArray");
    }
    
    /**
     * 指定された値を要素として持つ配列を生成する。
     * @param values 配列の要素の値の指定
     */
    protected JsonArray(JsonValue... values) {
        logger.entering(className, "ValueArray", values);
        
        valueList = Arrays.asList(values);
        
        logger.entering(className, "ValueArray");
    }
    
    /**
     * 配列の要素数を返す。
     * @return 配列の要素数
     */
    public int size() {
        return valueList.size();
    }
    
    /**
     * 指定された番号に格納されている配列の要素を返す。
     * @param index 要素の番号の指定
     * @return 配列の要素
     */
    public JsonValue get(int index) {
        return valueList.get(index);
    }
    
    /**
     * 指定された番号の要素を、指定された要素の値で置き換えた配列を生成する。
     * @param index 要素の番号の指定
     * @param value 要素の値の指定
     * @return 指定された通りに要素が置き換えられた配列
     */
    public JsonArray alter(int index, JsonValue value) {
        logger.entering(className, "alter", new Object[]{index, value});
        
        ArrayList<JsonValue> newArray = new ArrayList<JsonValue>(valueList);
        newArray.add(index, value);
        JsonArray resultValue = new JsonArray(newArray);
        
        logger.exiting(className, "alter", resultValue);
        return resultValue;
    }
    
    /**
     * 指定された番号の要素を、指定された要素で置き換えた配列を生成する。
     * 番号と要素の組はMapを利用して渡される。
     * @param valueMap 要素番号と要素の値の対応
     * @return 指定された通りに要素が置き換えられた配列
     */
    public JsonArray alter(Map<Integer, ? extends JsonValue> valueMap) {
        logger.entering(className, "alter", valueMap);
        
        ArrayList<JsonValue> newArray = new ArrayList<JsonValue>(valueList);
        for (Map.Entry<Integer, ? extends JsonValue> entry: valueMap.entrySet()) {
            newArray.add(entry.getKey(), entry.getValue());
        }
        
        JsonArray resultValue = new JsonArray(newArray);
        
        logger.exiting(className, "alter", resultValue);
        return resultValue;
    }
    
    /**
     * 指定された要素を最後尾に追加した配列を生成する。
     * @param values 追加する要素の値の指定
     * @return 要素が追加された配列
     */
    public JsonArray alterAppend(JsonValue... values) {
        logger.entering(className, "alterAppend", values);
        
        JsonArray resultValue = alterAppend(Arrays.asList(values));
        
        logger.exiting(className, "alterAppend", resultValue);
        return resultValue;
    }
    
    /**
     * 指定された要素を最後尾に追加した配列を生成する。
     * @param values 追加する要素の値の指定
     * @return 要素が追加された配列
     */
    public JsonArray alterAppend(List<? extends JsonValue> values) {
        logger.entering(className, "alterAppend", values);
        
        ArrayList<JsonValue> newArray = new ArrayList<JsonValue>(valueList.size() + values.size());
        newArray.addAll(valueList);
        newArray.addAll(values);
        JsonArray resultValue = new JsonArray(newArray);
        
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
    public JsonArray alterSize(int size, JsonValue padding) {
        logger.entering(className, "alterSize", size);
        
        if (size < 0) {
            size = 0;
        }
        
        ArrayList<JsonValue> newArray = new ArrayList<JsonValue>(size);
        int minSize = Math.min(valueList.size(), size);
        for (int i=0; i<minSize; i++) {
            newArray.add(valueList.get(i));
        }
        
        for (int i=minSize; i<size; i++) {
            newArray.add(padding);
        }
        
        JsonArray resultValue = new JsonArray(newArray);
        
        logger.exiting(className, "alterSize", resultValue);
        return resultValue;
    }
    
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
        if (!(object instanceof JsonArray)) {
            return false;
        }
        
        JsonArray valueArray = (JsonArray)object;
        
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
