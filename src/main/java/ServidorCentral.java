import java.io.*;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ServidorCentral {
	// private static final int PUERTO_UDP = 7879;
	private static final Queue<Partida> colaPartidasNuevas = new ConcurrentLinkedQueue<>();

	public static void main(String[] args) throws SocketException {
		final int PUERTO_UDP = 7879;
		DatagramSocket socketUDP = new DatagramSocket(PUERTO_UDP);
		System.out.println("Servidor Central escuchando en el puerto " + PUERTO_UDP);

		while (true) {
			byte[] buffer = new byte[1024];
			DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);
			System.out.println("Esperant un nou paquet...");
			
			try {
				socketUDP.receive(paquete);

				String mensaje = new String(paquete.getData(), 0, paquete.getLength());
				InetAddress direccionCliente = paquete.getAddress();
				int puertoCliente = paquete.getPort();

				String[] partes = mensaje.split(" ");
				String comando = partes[0];
				
				switch (comando) {
				case "CREAR":
					int puertoJuego = Integer.parseInt(partes[1]);
					Partida p = new Partida(direccionCliente.getHostAddress(),puertoJuego);
					manejarCrear(direccionCliente, puertoCliente, p);
					break;
				case "UNIR-ME":
					manejarUnir(direccionCliente, puertoCliente);
					break;
				default:
					enviarMensajeUDP("Comando no reconocido", direccionCliente, puertoCliente);
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static void manejarCrear(InetAddress direccion, int puerto, Partida partidaNueva) {
		//int puertoJuego = Integer.parseInt(portJuego);
		System.out.println(direccion +" "+puerto+" "+partidaNueva);
		// Lógica para manejar la creación de partidas
		if (!colaPartidasNuevas.contains(partidaNueva)) {
			
			colaPartidasNuevas.add(partidaNueva);
			enviarMensajeUDP("OK", direccion, puerto);
		} else {
			enviarMensajeUDP("ERROR Partida ya registrada", direccion, puerto);
		}
	}

	private static void manejarUnir(InetAddress direccion, int puerto) {
		// Lógica para manejar unirse a una partida
		String p;
		if (!colaPartidasNuevas.isEmpty()) {
			Partida partida = colaPartidasNuevas.poll();
			
			if (partida != null) {
                p = partida.getIpJugador1() + "::" + partida.getPuertoJugador1();
            } else {
                p="NO_HAY_PARTIDAS";
            }
			enviarMensajeUDP(p, direccion, puerto);
		} else {
			// No hay partidas disponibles, esperar
			// Esto podría ser mejorado para manejar la espera de manera eficiente
			//while (colaPartidasNuevas.isEmpty()) {
				// Esperar hasta que haya una partida
				p="NO_HAY_PARTIDAS";
				enviarMensajeUDP(p, direccion, puerto);
			//}
		}
	}

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
