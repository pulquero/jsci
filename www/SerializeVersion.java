import java.io.*;
import java.text.*;
import java.util.Date;
import JSci.Version;

public final class SerializeVersion {
        public static final double KILOBYTE=1024;
        public static final double MEGABYTE=KILOBYTE*KILOBYTE;
        /**
        * @param arg 0 - input file, 1 - output serialize file
        */
        public static void main(String arg[]) throws Exception {
                if(arg.length < 2)
                        return;
                Version current = Version.getCurrent();
                // serialize version object
                ObjectOutputStream objout=new ObjectOutputStream(new FileOutputStream(arg[1]));
                objout.writeObject(current);
                objout.close();
                // output XML data
                PrintWriter xmlout=new PrintWriter("packages.xml");
                xmlout.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                xmlout.println("<packages>");
                writeFileInfo(arg[0], current.toString(), current.platform, xmlout);
                //writeFileInfo("\\java\\jsci118\\JSci118.zip", "0.90", "JDK 1.1.8", xmlout);
                xmlout.println("</packages>");
                xmlout.close();
        }
        public static void writeFileInfo(String filename,String version,String notes,PrintWriter out) throws Exception {
                File file=new File(filename);
                out.println("<package>");
                out.print("<file-name>");
                out.print(file.getName());
                out.println("</file-name>");
                out.print("<version>");
                out.print(version);
                out.println("</version>");
                DateFormat dateFormatter=DateFormat.getInstance();
                out.print("<date>");
                out.print(dateFormatter.format(new Date()));
                out.println("</date>");
                NumberFormat numFormatter=NumberFormat.getInstance();
                numFormatter.setMinimumFractionDigits(1);
                numFormatter.setMaximumFractionDigits(2);
                out.print("<file-size units=\"MB\">");
                out.print(numFormatter.format(file.length()/MEGABYTE));
                out.println("</file-size>");
                out.print("<notes>");
                out.print(notes);
                out.println("</notes>");
                out.println("</package>");
        }
}
