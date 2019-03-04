package fileclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author Manuel Espinosa Torres
 */
public class FileClient {

    private final int dstPort = 9889;
    private final String ipServer;

    public FileClient(String ipServer) {
        this.ipServer = ipServer;
    }

    public String sendCommand(String command) {
        Socket socket = new Socket();
        InetSocketAddress socketAddr = new InetSocketAddress(ipServer, dstPort);

        String serverMessage = "";
        try {
            socket.connect(socketAddr);

            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            PrintWriter pWriter = new PrintWriter(osw);

            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader bReader = new BufferedReader(isr);

            pWriter.println(command);
            pWriter.flush();

            String linia;
            while ((linia = bReader.readLine()) != null) {
                serverMessage += linia + "\n";
            }

            bReader.close();
            pWriter.close();
            isr.close();
            osw.close();
            is.close();
            os.close();

            socket.close();
        } catch (IOException ex) {
            System.out.println("S'ha produït una excepció durant la connexió. Excepció: " + ex);
        }

        return serverMessage;
    }

    public void putFile() {
        try {
            Socket socket = new Socket();
            InetSocketAddress socketAddr = new InetSocketAddress(ipServer, dstPort);

            socket.connect(socketAddr);

            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            PrintWriter pWriter = new PrintWriter(osw);

            System.out.print("Introdueix el nom del fitxer a pujar: ");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String fileName = stdin.readLine();

            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("El fitxer " + fileName + " no s'ha pogut pujar al servidor perqué no existeix.");
                return;
            }

            pWriter.println("put " + fileName);
            pWriter.flush();

            // Alternativa 1 - Comentar / descomentar la part corresponent al servidor:
            System.out.println("Pujant el fitxer " + fileName + "...");
            FileInputStream fis = new FileInputStream(fileName);
            int bytes, bytesCopied = 0;
            do {
                bytes = fis.read();
                if (bytes != -1) {
                    os.write(bytes);
                    bytesCopied++;
                }

            } while (bytes != -1);
            System.out.println("El fitxer " + fileName + " s'ha pujat al servidor correctament. Bytes copiats: " + bytesCopied + ".");

            os.close();
            fis.close();
            pWriter.close();
            osw.close();
            os.close();

            socket.close();
            // Alternativa 2 - Comentar / descomentar la part corresponent al servidor:
            /*File myFile = new File(fileName);
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

            System.out.println("Pujant el fitxer " + fileName + "...");
            dis.readFully(mybytearray, 0, mybytearray.length);

            // Enviament del nom i grandària del fitxer al servidor:
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(myFile.getName());
            dos.writeLong(mybytearray.length);

            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            System.out.println("El fitxer " + fileName + " s'ha pujat al servidor correctament. Bytes copiats: " + mybytearray.length + ".");

            dos.close();
            dis.close();
            bis.close();
            fis.close();
            osw.close();
            os.close();
            pWriter.close();

            socket.close();*/
        } catch (IOException ex) {
            System.out.println("El fitxer no s'ha pogut pujar al servidor perqué s'ha donat la següent excepció: " + ex.getMessage());
        }
    }

    public void getFile(String fileName) {
        String resposta = sendCommand("check " + fileName);

        if (resposta.replaceAll("\n", "").equals("false")) {
            System.out.println("El fitxer " + fileName + " no s'ha pogut descarregar del servidor perqué no existeix.");
            return;
        }

        Socket socket = new Socket();
        InetSocketAddress socketAddr = new InetSocketAddress(ipServer, dstPort);
        try {
            socket.connect(socketAddr);

            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            PrintWriter pWriter = new PrintWriter(osw);

            pWriter.println("get " + fileName);
            pWriter.flush();

            InputStream is = socket.getInputStream();
            FileOutputStream fos = new FileOutputStream(fileName);

            System.out.println("Descarregant el fitxer " + fileName + "...");
            int bytes, bytesCopied = 0;
            do {
                bytes = is.read();
                if (bytes != -1) {
                    fos.write(bytes);
                    bytesCopied++;
                }
            } while (bytes != -1);
            System.out.println("El fitxer " + fileName + " s'ha descarregat del servidor correctament. Bytes copiats: " + bytesCopied + ".");

            os.close();
            is.close();
            fos.close();
            pWriter.close();
            osw.close();
            os.close();

            socket.close();
        } catch (IOException ex) {
            System.out.println("El fitxer no s'ha pogut descarregar del servidor perqué s'ha donat la següent excepció: " + ex.getMessage());
        }
    }

    public void startCLI(String ipServer) {
        if (establishConnection()) {
            Scanner scanner = new Scanner(System.in);
            do {
                System.out.println("\n------ APLICACIÓ CLIENT / SERVIDOR ------");
                System.out.println("- list");
                System.out.println("- get [fitxer]");
                System.out.println("- put");
                System.out.println("- quit / exit");
                System.out.print("# FileClient(" + ipServer + ":" + dstPort + ")> ");

                String command = scanner.nextLine();
                switch (command) {
                    case "list":
                        System.out.print(sendCommand(command));
                        break;
                    case "put":
                        putFile();
                        break;
                    case "quit":
                    case "exit":
                        System.out.println("Tancant l'aplicació...");
                        System.exit(0);
                        break;
                    default:
                        String[] commandSplitted = command.split(" ");
                        if (commandSplitted.length == 2) {
                            switch (commandSplitted[0]) {
                                case "put":
                                    putFile();
                                    break;
                                case "get":
                                    getFile(commandSplitted[1]);
                                    break;
                                default:
                                    System.out.println("Has introduït una opció incorrecta.");
                            }
                        } else {
                            System.out.println("Has introduït un nombre d'arguments incorrecte. El nombre d'arguments ha de ser 2.");
                        }
                }
            } while (true);
        } else {
            System.out.println("No s'ha pogut establir la connexió amb el servidor.");
        }

    }

    /* Mètode que s'utilitza per comprovar que es podrà mantenir una connexió
    exitosa amb el servidor ubicat a la direcció IP passada com a argument
    amb la finalitat de no mostrar la CLI si dita connexió no es pot efectuar
    correctament: */
    public boolean establishConnection() {
        return sendCommand("GET connection status").replaceAll("\n", "").equals("true");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Has introduït un nombre d'arguments incorrecte. El nombre d'arguments ha de ser 1.");
            System.exit(-1);
        }

        FileClient fileClient = new FileClient(args[0]);
        fileClient.startCLI(args[0]);
    }
}
