package jsci.util.array;

/**
 *
 * @author Mark
 */
public final class ArrayUtilities {
    public static double[] createArray(int size, double value) {
        double array[] = new double[size];
        for(int i=0; i<size; i++) {
            array[i] = value;
        }
        return array;
    }
    public static double[][] createRandomArray(int rows, int cols) {
        double[][] arr = new double[rows][cols];
        for(int i=0; i<rows; i++) {
            for(int j=0; j<cols; j++) {
                arr[i][j] = random();
            }
        }
        return arr;
    }
    private static double random() {
        return 2.0*Math.random()-1.0;
    }
}
