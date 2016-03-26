import java.io.*;
import java.net.*;
import JSci.Version;

public final class AutoUpdate {
        private final static String zipfile="JSci.zip";
        private final static String email="support@jsci.org.uk";
        private URL url;

        public static void main(String arg[]) {
                Version current = Version.getCurrent();
                System.out.println("Current version: "+current.toString());
                System.out.println("Checking for a later version...");
                try {
                        Version latest = Version.getLatest();
                        System.out.println("Latest version: "+latest.toString());
                        if(latest.isLater(current)) {
                                System.out.print("Downloading latest version...");
                                AutoUpdate app=new AutoUpdate(current.home);
                                app.download();
                                System.out.println("done.");
                        }
                } catch(IOException e) {
                        System.out.println("\nError transfering data - try again or contact "+email+" directly.");
                }
        }
        private AutoUpdate(String home) {
                try {
                        url = new URL(home+zipfile);
                } catch(MalformedURLException e) {}
        }
        private void download() throws IOException {
                final OutputStream out = new FileOutputStream(zipfile);
                final InputStream in = url.openStream();
                byte buf[] = new byte[in.available()];
                while(buf.length>0) {
                        in.read(buf);
                        out.write(buf);
                        buf = new byte[in.available()];
                }
                in.close();
                out.close();
        }
}

