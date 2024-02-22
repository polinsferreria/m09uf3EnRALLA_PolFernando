import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class ServidorCentral {
    private static final int PORT = 7879;
    private static final Queue<Partida> partidasEnEspera = new LinkedList<>();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor Central esperando conexiones en el puerto " + PORT + "...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ServidorCentralHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static synchronized void agregarPartidaEnEspera(Partida partida) {
        partidasEnEspera.add(partida);
    }

    static synchronized Partida obtenerPartidaEnEspera() {
        return partidasEnEspera.poll();
    }
}