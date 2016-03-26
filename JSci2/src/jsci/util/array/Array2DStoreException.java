package jsci.util.array;

import java.util.Arrays;

/**
 *
 * @author Mark
 */
public class Array2DStoreException extends RuntimeException {
    public Array2DStoreException(int i, int j, Object value) {
        super("Cannot store "+value+" at ("+i+", "+j+")");
    }
}
