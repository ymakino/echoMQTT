package echomqtt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;

/**
 *
 * @author ymakino
 */
public class NetworkInterfaceSelector {
    public static NetworkInterface select() throws SocketException, IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        for (;;) {
            LinkedList<NetworkInterface> networkInterfaces = new LinkedList<NetworkInterface>();
        
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                if (ni.isUp()) {
                    networkInterfaces.add(ni);
                }
            }

            System.out.println("0) default");

            for (int i = 0; i < networkInterfaces.size(); i++) {
                NetworkInterface nif = networkInterfaces.get(i);
                String name = nif.getName();
                String displayName = nif.getDisplayName();
                System.out.print("" + (i + 1) + ") " + name);
                
                if (!name.equals(displayName)) {
                    System.out.print(" - " + displayName);
                }
                
                System.out.println();
            }

            System.out.print("Select network interface(0-" + networkInterfaces.size() + "): ");
            System.out.flush();

            String line = reader.readLine();
            try {
                int num = Integer.parseInt(line.trim());
                if (0 <= num && num <= networkInterfaces.size()) {
                    if (num == 0) {
                        return null;
                    } else {
                        return networkInterfaces.get(num-1);
                    }
                }
            } catch (NumberFormatException ex) {
                // The exception can be ignored safely.
            }
            
            if (!line.trim().isEmpty()) {
                System.out.println("Invalid input: " + line);
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println(NetworkInterfaceSelector.select());
    }
}
