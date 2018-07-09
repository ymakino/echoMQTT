package echomqtt.json;

/**
 *
 * @author ymakino
 */
public class JsonDecoderException extends Exception {

        private int index;

        public JsonDecoderException(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
}
