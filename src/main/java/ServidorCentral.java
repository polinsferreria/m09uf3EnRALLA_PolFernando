import java.net.*;
import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

public class ServidorCentral {
    private static final int PORT = 7879;
    private static Queue<String> cuaPartidesNoves = new LinkedList<>();

    public static void main(String[] args) {
        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket(PORT);

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String request = new String(packet.getData(), 0, packet.getLength());
                String[] parts = request.split(" ");

                InetAddress clientAddress = packet.getAddress();
                int clientPort = packet.getPort();

                if (parts[0].equals("CREAR")) {
                    String resposta = processarCrear(clientAddress, clientPort, parts);
                    enviarResposta(resposta, socket, clientAddress, clientPort);
                } else if (parts[0].equals("UNIR-ME")) {
                    String resposta = processarUnirMe();
                    enviarResposta(resposta, socket, clientAddress, clientPort);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    private static String processarCrear(InetAddress clientAddress, int clientPort, String[] parts) {
        // Verificar si ja hi ha una partida registrada amb aquesta adreça i port
        // Si no hi ha, afegir a la cua de partides noves
        // Retornar resposta adequada
        return "OK"; // o "ERROR missatge"
    }

    private static String processarUnirMe() {
        if (!cuaPartidesNoves.isEmpty()) {
            String partida = cuaPartidesNoves.poll();
            return partida;
        } else {
            // Esperar fins que hi hagi partides noves a la cua
            return "WAIT";
        }
    }

    private static void enviarResposta(String resposta, DatagramSocket socket, InetAddress clientAddress, int clientPort) {
        try {
            byte[] responseBytes = resposta.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseBytes, responseBytes.length, clientAddress, clientPort);
            socket.send(responsePacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
