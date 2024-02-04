import java.net.*;
import java.io.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 7879;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (DatagramSocket udpSocket = new DatagramSocket();
             Socket tcpSocket = new Socket(SERVER_IP, SERVER_PORT)) {

            while (true) {
                mostrarMenu();
                int opcio = scanner.nextInt();
                scanner.nextLine(); // Consumir la nova línia

                switch (opcio) {
                    case 1:
                        crearPartida(udpSocket);
                        break;
                    case 2:
                        connectarPartida(tcpSocket);
                        break;
                    case 3:
                        System.out.println("Adéu!");
                        return;
                    default:
                        System.out.println("Opció no vàlida. Torna a intentar.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static void mostrarMenu() {
        System.out.println("Menú:");
        System.out.println("1. Crear una partida nova");
        System.out.println("2. Connectar-se a una partida");
        System.out.println("3. Sortir");
        System.out.print("Selecciona una opció: ");
    }

    private static void crearPartida(DatagramSocket udpSocket) {
        try {
            System.out.print("Introdueix el port de la nova partida: ");
            int portPartida = new Scanner(System.in).nextInt();
            String missatge = "CREAR " + portPartida;

            byte[] data = missatge.getBytes();
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, SERVER_PORT);

            udpSocket.send(packet);

            byte[] responseBuffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
            udpSocket.receive(responsePacket);

            String resposta = new String(responsePacket.getData(), 0, responsePacket.getLength());
            System.out.println("Resposta del servidor: " + resposta);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void connectarPartida(Socket tcpSocket) {
        try {
            System.out.println("Introdueix l'adreça IP del servidor de partida:");
            String serverIP = new Scanner(System.in).nextLine();

            System.out.println("Introdueix el port del servidor de partida:");
            int serverPort = new Scanner(System.in).nextInt();

            tcpSocket.connect(new InetSocketAddress(serverIP, serverPort));
            System.out.println("Connexió amb el servidor de partida establerta.");

            // Lògica de la partida
            jugarPartida(tcpSocket);

            // Tancar connexió quan la partida ha acabat
            tcpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void jugarPartida(Socket tcpSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(tcpSocket.getInputStream()));
            PrintWriter out = new PrintWriter(tcpSocket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            String respostaServidor;

            do {
                respostaServidor = in.readLine();
                System.out.println("Servidor: " + respostaServidor);

                if (!respostaServidor.equals("Partida acabada")) {
                    System.out.print("El teu torn. Introdueix la teva jugada (ex. fila columna): ");
                    String jugada = scanner.nextLine();
                    out.println(jugada);
                }
            } while (!respostaServidor.equals("Partida acabada"));

            System.out.println("Partida acabada. Gràcies per jugar!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
