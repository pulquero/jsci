package jsci.io;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;

/**
 *
 * @author Mark
 */
public class TextReader {
    private final BufferedReader reader;
    private String delimiter = ",";
    private NumberFormat format = null;

    public TextReader(Reader reader) {
        this.reader = new BufferedReader(reader);
    }
    public TextReader(String name) throws FileNotFoundException {
        this(new FileReader(name));
    }
    public TextReader(File file) throws FileNotFoundException {
        this(new FileReader(file));
    }

    public void setDelimiter(String s) {
        this.delimiter = s;
    }
    public String getDelimiter() {
        return this.delimiter;
    }

    public void setFormat(NumberFormat format) {
        this.format = format;
    }
    public NumberFormat getFormat() {
        return this.format;
    }

    public double readDouble(String str) throws IOException, ParseException {
        if(format == null)
            return Double.parseDouble(str);
        else
            return format.parse(str).doubleValue();
    }
    public int readInt(String str) {
        return Integer.parseInt(str);
    }

    public String readLine() throws IOException {
        return reader.readLine();
    }

    public double[] readRow(String line) throws IOException, ParseException {
        line = line.trim();
        // detect column count
        int colCount;
        if(line.length() == 0) {
            colCount = 0;
        } else {
            colCount = 1;
            int pos = line.indexOf(delimiter);
            while(pos != -1) {
                colCount++;
                pos = line.indexOf(delimiter, pos+1);
            }
        }
        // parse
        double[] row = new double[colCount];
        int startPos = 0;
        int endPos = line.indexOf(delimiter);
        int i;
        for(i=0; endPos != -1; i++) {
            row[i] = readDouble(line.substring(startPos, endPos).trim());
            startPos = endPos+1;
            endPos = line.indexOf(delimiter, startPos);
        }
        row[i] = readDouble(line.substring(startPos).trim());
        return row;
    }
    /**
    * Reads data to an array.
    * @exception IOException If an I/O error occurs.
    */
    public double[][] readArray() throws IOException, ParseException {
        final List<double[]> rows=new ArrayList<double[]>();
        String line = readLine();
        while(line != null) {
            double[] row = readRow(line);
            rows.add(row);
            line = readLine();
        }

        double array[][]=new double[rows.size()][];
        for(int i=0;i<array.length;i++) {
            array[i] = rows.get(i);
        }
        return array;
    }

    public void read(DoubleArray2D array) throws IOException, ParseException {
        String line = readLine();
        for(int i=0; line != null; i++) {
            line = line.trim();
            // parse
            int startPos = 0;
            int endPos = line.indexOf(delimiter);
            int k;
            for(k=0; endPos != -1; k++) {
                array.setDouble(i, k, readDouble(line.substring(startPos, endPos).trim()));
                startPos = endPos+1;
                endPos = line.indexOf(delimiter, startPos);
            }
            array.setDouble(i, k, readDouble(line.substring(startPos).trim()));
            line = readLine();
        }
    }

    public void readTable(DoubleArray2D array) throws IOException, ParseException {
            String line = readLine();
            for(int i=0; line != null && line.length() > 0; i++) {
                line = line.trim();
                // parse
                int startPos = 0;
                int endPos = line.indexOf(delimiter);
                int row = readInt(line.substring(startPos, endPos).trim());
                startPos = endPos+1;
                endPos = line.indexOf(delimiter, startPos);
                int col = readInt(line.substring(startPos, endPos).trim());
                startPos = endPos+1;
                double value = readDouble(line.substring(startPos).trim());
                array.setDouble(row, col, value);
                line = readLine();
            }
    }

    public void readColumn(DoubleArray1D array) throws IOException, ParseException {
        String line = readLine();
        for(int i=0; line != null && line.length() > 0; i++) {
            line = line.trim();
            double value = readDouble(line);
            array.setDouble(i, value);
            line = readLine();
        }
    }

    public void close() throws IOException {
        reader.close();
    }
}
