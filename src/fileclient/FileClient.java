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
            System.out.println("There was an exception during the connection. Exception: " + ex);
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

            System.out.print("Enter the name of the file to upload: ");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String fileName = stdin.readLine();

            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("The file " + fileName + " could not be uploaded to the server because it does not exist.");
                return;
            }

            pWriter.println("put " + fileName);
            pWriter.flush();

            // Alternative 1 - Comment / uncomment the part corresponding to the server:
            System.out.println("Uploading file " + fileName + "...");
            FileInputStream fis = new FileInputStream(fileName);
            int bytes, bytesCopied = 0;
            do {
                bytes = fis.read();
                if (bytes != -1) {
                    os.write(bytes);
                    bytesCopied++;
                }

            } while (bytes != -1);
            System.out.println("The file " + fileName + " has uploaded to the server correctly. Copied bytes: " + bytesCopied + ".");

            os.close();
            fis.close();
            pWriter.close();
            osw.close();
            os.close();

            socket.close();

            // Alternative 2 - Comment / uncomment the part corresponding to the server:
            /*File myFile = new File(fileName);
            byte[] mybytearray = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            DataInputStream dis = new DataInputStream(bis);

            System.out.println("Uploading file " + fileName + "...");
            dis.readFully(mybytearray, 0, mybytearray.length);

            // Sending the name and size of the file to the server:
            DataOutputStream dos = new DataOutputStream(os);
            dos.writeUTF(myFile.getName());
            dos.writeLong(mybytearray.length);

            dos.write(mybytearray, 0, mybytearray.length);
            dos.flush();
            System.out.println("The file " + fileName + "  was successfully uploaded to the server. Copied bytes: " + mybytearray.length + ".");

            dos.close();
            dis.close();
            bis.close();
            fis.close();
            osw.close();
            os.close();
            pWriter.close();

            socket.close();*/
        } catch (IOException ex) {
            System.out.println("The file could not be uploaded to the server because the following exception was given: " + ex.getMessage());
        }
    }

    public void getFile(String fileName) {
        String resposta = sendCommand("check " + fileName);

        if (resposta.replaceAll("\n", "").equals("false")) {
            System.out.println("The file " + fileName + " could not be downloaded from the server because it does not exist.");
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

            System.out.println("Downloading the file " + fileName + "...");
            int bytes, bytesCopied = 0;
            do {
                bytes = is.read();
                if (bytes != -1) {
                    fos.write(bytes);
                    bytesCopied++;
                }
            } while (bytes != -1);
            System.out.println("The file " + fileName + " was downloaded successfully from the server. Copied bytes: " + bytesCopied + ".");

            os.close();
            is.close();
            fos.close();
            pWriter.close();
            osw.close();
            os.close();

            socket.close();
        } catch (IOException ex) {
            System.out.println("The file could not be downloaded from the server because the following exception was given: " + ex.getMessage());
        }
    }

    public void startCLI(String ipServer) {
        if (establishConnection()) {
            Scanner scanner = new Scanner(System.in);
            do {
                System.out.println("\n------ SERVER CLIENT APPLICATION ------");
                System.out.println("- list");
                System.out.println("- get [file]");
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
                        System.out.println("Closing the application...");
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
                                    System.out.println("You have entered an incorrect option.");
                            }
                        } else {
                            System.out.println("You have entered a number of incorrect arguments. The number of arguments must be 2.");
                        }
                }
            } while (true);
        } else {
            System.out.println("The connection to the server could not be established.");
        }

    }

    /* Method used to verify that a connection can be maintained successful with
    the server located in the IP address passed as an argument in order not to
    display the CLI if this connection can not be performed correctly: */
    public boolean establishConnection() {
        return sendCommand("GET connection status").replaceAll("\n", "").equals("true");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("You have entered a number of incorrect arguments. The number of arguments must be 1.");
            System.exit(-1);
        }

        FileClient fileClient = new FileClient(args[0]);
        fileClient.startCLI(args[0]);
    }
}
