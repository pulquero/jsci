package jsci.io;

import java.io.*;
import java.text.NumberFormat;
import jsci.util.array.DoubleArray1D;
import jsci.util.array.DoubleArray2D;

/**
 *
 * @author Mark
 */
public class TextWriter {
    private final BufferedWriter writer;
    private String delimiter = ",";
    private NumberFormat format = null;

    public TextWriter(Writer writer) {
        this.writer = new BufferedWriter(writer);
    }
    public TextWriter(String name) throws IOException {
        this(new FileWriter(name));
    }
    public TextWriter(File file) throws IOException {
        this(new FileWriter(file));
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

    public void write(String str) throws IOException {
        writer.write(str);
    }
    public void newLine() throws IOException {
        writer.newLine();
    }

    public void write(double x) throws IOException {
        if(format == null)
            write(Double.toString(x));
        else
            write(format.format(x));
    }
    public void write(int x) throws IOException {
        write(Integer.toString(x));
    }

    public void writeRow(double data[]) throws IOException {
        if(data.length > 0) {
            int i;
            for(i=0;i<data.length-1;i++) {
                write(data[i]);
                write(delimiter);
            }
            write(data[i]);
        }
        newLine();
    }
    public void writeColumn(double data[]) throws IOException {
        for(int i=0;i<data.length;i++) {
            write(data[i]);
            newLine();
        }
    }
    public void write(double data[][]) throws IOException {
        for(int i=0;i<data.length;i++) {
            writeRow(data[i]);
        }
    }

    public void writeTable(DoubleArray2D array) throws IOException {
        for(int i=0;i<array.rows();i++) {
            for(int j=0;j<array.columns();j++) {
                    double value = array.getDouble(i, j);
                    if(value != 0.0) {
                        write(i);
                        write(delimiter);
                        write(j);
                        write(delimiter);
                        write(value);
                        newLine();
                    }
            }
        }
    }

    public void write(DoubleArray2D array) throws IOException {
        int cols = array.columns();
        for(int i=0;i<array.rows();i++) {
            if(cols > 0) {
                int j;
                for(j=0;j<cols-1;j++) {
                        write(array.getDouble(i,j));
                        write(delimiter);
                }
                write(array.getDouble(i,j));
            }
            newLine();
        }
    }

    public void writeRow(DoubleArray1D array) throws IOException {
        int size = array.size();
        if(size > 0) {
            int i;
            for(i=0;i<size-1;i++) {
                write(array.getDouble(i));
                write(delimiter);
            }
            write(array.getDouble(i));
        }
        newLine();
    }
    public void writeColumn(DoubleArray1D array) throws IOException {
        int size = array.size();
        for(int i=0;i<size;i++) {
            write(array.getDouble(i));
            newLine();
        }
    }

    public void close() throws IOException {
        writer.close();
    }
}
