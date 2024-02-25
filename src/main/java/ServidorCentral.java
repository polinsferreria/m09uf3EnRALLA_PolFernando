import java.io.*;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServidorCentral {
    private static final Queue<Partida> colaPartidasNuevas = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        final int PUERTO_UDP = 7879;
        try (DatagramSocket socketUDP = new DatagramSocket(PUERTO_UDP)) {
            System.out.println("Servidor Central escuchando en el puerto " + PUERTO_UDP);

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);

                // Espera la recepción de un paquete
                System.out.println("Esperando un nuevo paquete...");
                socketUDP.receive(paquete);

                // Obtiene la información del paquete recibido
                String mensaje = new String(paquete.getData(), 0, paquete.getLength());
                InetAddress direccionCliente = paquete.getAddress();
                int puertoCliente = paquete.getPort();

                // Divide el mensaje en partes
                String[] partes = mensaje.split(" ");
                String comando = partes[0];

                switch (comando) {
                    case "CREAR":
                        // Crea una nueva partida y maneja la solicitud de creación
                        int puertoJuego = Integer.parseInt(partes[1]);
                        Partida nuevaPartida = new Partida(direccionCliente.getHostAddress(), puertoJuego);
                        manejarCrear(direccionCliente, puertoCliente, nuevaPartida);
                        break;
                    case "UNIR-ME":
                        // Maneja la solicitud de unirse a una partida
                        manejarUnir(direccionCliente, puertoCliente);
                        break;
                    default:
                        // Envía un mensaje de error si el comando no es reconocido
                        enviarMensajeUDP("Comando no reconocido", direccionCliente, puertoCliente);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Maneja la creación de una nueva partida
    private static void manejarCrear(InetAddress direccion, int puerto, Partida partidaNueva) {
        System.out.println("Creando partida: " + direccion + " " + puerto + " " + partidaNueva);
        if (!colaPartidasNuevas.contains(partidaNueva)) {
            // Agrega la nueva partida a la cola y envía un mensaje de confirmación
            colaPartidasNuevas.add(partidaNueva);
            enviarMensajeUDP("OK", direccion, puerto);
        } else {
            // Envía un mensaje de error si la partida ya está registrada
            enviarMensajeUDP("ERROR: Partida ya registrada", direccion, puerto);
        }
    }

    // Maneja la solicitud de unirse a una partida
    private static void manejarUnir(InetAddress direccion, int puerto) {
        String respuesta;
        if (!colaPartidasNuevas.isEmpty()) {
            // Si hay partidas disponibles, obtiene la primera de la cola y envía su información
            Partida partida = colaPartidasNuevas.poll();
            if (partida != null) {
                respuesta = partida.getIpJugador1() + "::" + partida.getPuertoJugador1();
            } else {
                respuesta = "NO_HAY_PARTIDAS";
            }
        } else {
            // Si no hay partidas disponibles, envía un mensaje indicando eso
            respuesta = "NO_HAY_PARTIDAS";
        }
        enviarMensajeUDP(respuesta, direccion, puerto);
    }

    // Envía un mensaje UDP al cliente
    private static void enviarMensajeUDP(String mensaje, InetAddress direccion, int puerto) {
        try (DatagramSocket socketUDP = new DatagramSocket()) {
            byte[] buffer = mensaje.getBytes();
            DatagramPacket paquete = new DatagramPacket(buffer, buffer.length, direccion, puerto);
            socketUDP.send(paquete);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
