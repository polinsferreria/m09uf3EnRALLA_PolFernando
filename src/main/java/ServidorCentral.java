import java.net.*;
import java.io.*;
import java.util.*;

public class ServidorCentral {
    private static final int PORT = 7879;
    private static List<String> partidasNuevas = new ArrayList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor Central esperando conexiones en el puerto " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ServidorPartidaHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static synchronized void agregarPartidaNueva(String ip, int puerto) {
        partidasNuevas.add(ip + "::" + puerto);
    }

    static synchronized String obtenerPartidaNueva() {
        if (!partidasNuevas.isEmpty()) {
            return partidasNuevas.remove(0);
        }
        return null;
    }
}