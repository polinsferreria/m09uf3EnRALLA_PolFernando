import java.io.*;
import java.net.*;

public class Cliente {
	public static void main(String[] args) {
		try (BufferedReader lector = new BufferedReader(new InputStreamReader(System.in))) {
			String respuesta;
			while (true) {

				System.out.println("Menú Principal:");
				System.out.println("1. Crear partida");
				System.out.println("2. Unirse a partida");
				System.out.println("3. Salir");
				System.out.print("Seleccione una opción: ");

				int opcion = Integer.parseInt(lector.readLine());

				switch (opcion) {
				case 1:
					System.out.print("Ingrese el puerto para la partida: ");
					int puertoCrearPartida = Integer.parseInt(lector.readLine());

					// Enviar solicitud al Servidor Central para crear partida
					respuesta = comunicarseConServidorCentral("CREAR " + puertoCrearPartida);
					System.out.println(respuesta);
					if (respuesta.equals("OK")) {
						System.out.println("Esperando a que otro jugador se una...");
						// Aquí deberías obtener la información del Servidor Central sobre la conexión
						// No hay un puerto específico establecido aquí, ya que es aleatorio
						iniciarServidorPartida(puertoCrearPartida);
					} else {
						System.out.println("Error al crear la partida: " + respuesta);
					}
					break;
				case 2:
					respuesta = comunicarseConServidorCentral("UNIR-ME");
					System.out.println(respuesta);
					if (respuesta.equals("NO_HAY_PARTIDAS")) {
						System.out.println("no hay partidas. Crea una o intentalo mas tarde.");
					} else {
						String[] partidaInfo = respuesta.split("::");
						String ipPartida = partidaInfo[0];
						int puertoPartida = Integer.parseInt(partidaInfo[1]);
						jugarPartidaComoCliente(ipPartida, puertoPartida);

					}
					break;
				case 3:
					System.out.println("Saliendo del programa.");
					System.exit(0);
					break;
				default:
					System.out.println("Opción no válida. Inténtelo de nuevo.");
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String comunicarseConServidorCentral(String mensaje) {
		try (DatagramSocket socketUDP = new DatagramSocket()) {
			InetAddress direccionServidor = InetAddress.getByName("localhost");
			int puertoServidor = 7879;

			byte[] bufferEnvio = mensaje.getBytes();
			DatagramPacket paqueteEnvio = new DatagramPacket(bufferEnvio, bufferEnvio.length, direccionServidor,
					puertoServidor);

			socketUDP.send(paqueteEnvio);

			byte[] bufferRecepcion = new byte[1024];
			DatagramPacket paqueteRecepcion = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);
			socketUDP.receive(paqueteRecepcion);

			return new String(paqueteRecepcion.getData(), 0, paqueteRecepcion.getLength());
		} catch (IOException e) {
			e.printStackTrace();
			return "ERROR";
		}
	}

	private static void jugarPartidaComoCliente(String ipPartida, int puertoPartida) {
		try {
			System.out.println("Intentando conectar a la partida en " + ipPartida + ":" + puertoPartida);
			Socket partidaSocket = new Socket(ipPartida, puertoPartida);
			System.out.println("Conexión establecida con éxito.");
			BufferedReader recibir = new BufferedReader(new InputStreamReader(partidaSocket.getInputStream()));
			PrintWriter enviar = new PrintWriter(partidaSocket.getOutputStream(), true);

			String miSimbolos = recibir.readLine();
			char miSimbolo = miSimbolos.charAt(0);
			char simboloOponente;
			boolean soyPrimerJugador = miSimbolo == 'X';
			if (!soyPrimerJugador) {
				simboloOponente = 'X';
			} else {
				simboloOponente = 'O';
			}

			char[][] tablero = new char[3][3];
			boolean juegoEnCurso = true;

			System.out.println("soy jugador: " + miSimbolo + soyPrimerJugador);// quitar soyPrimerJugador

			if (soyPrimerJugador) {
				// Envía la señal de inicio al oponente
				enviar.println("LISTO");

			} else {
				// Espera la señal de inicio y quién comienza del oponente
				String inicio = recibir.readLine();
				if (!inicio.equals("LISTO")) {
					System.out.println("Error de inicio de la partida.");
					return;
				}
				/*
				 * String turnoOponente = recibir.readLine(); if
				 * (!turnoOponente.equals("TU_TURNO")) {
				 * System.out.println("Error en la información del turno del oponente.");
				 * return; }
				 */

			}

			while (juegoEnCurso) {
				// Imprimir tablero
				// imprimirTablero(tablero);

				// Turno del jugador
				if (soyPrimerJugador) {
					System.out.println("Es tu turno. Ingresa la fila y la columna (ej. 1 2): ");

					String movimiento;
					do {
						movimiento = ServidorPartida.leerMovimiento();
					} while (!ServidorPartida.esMovimientoValido(movimiento, tablero));
					enviar.println(movimiento);
					ServidorPartida.actualizarTablero(tablero, miSimbolo, movimiento);
				} else {
					System.out.println("Esperando el movimiento del oponente...");
					String movimientoOponente = recibir.readLine();
					ServidorPartida.actualizarTablero(tablero, simboloOponente, movimientoOponente);
				}

				// Verificar el estado del juego
				if (ServidorPartida.verificarGanador(tablero, miSimbolo)) {
					// imprimirTablero(tablero);
					System.out.println("¡Felicidades! ¡Has ganado!");
					juegoEnCurso = false;
				} else if (ServidorPartida.verificarGanador(tablero, simboloOponente)) {
					// imprimirTablero(tablero);
					System.out.println("¡Felicidades! ¡Has PERDIDO!");
					juegoEnCurso = false;
				} else if (ServidorPartida.tableroLleno(tablero)) {
					// imprimirTablero(tablero);
					System.out.println("¡El juego ha terminado en empate!");
					juegoEnCurso = false;
				}

				// -------------------------ferb imprimo aqui mejor creo
				// :0:------------------------
				ServidorPartida.imprimirTablero(tablero);

				// Cambiar de turno
				soyPrimerJugador = !soyPrimerJugador;
			}

			partidaSocket.close();
		} catch (IOException e) {
			System.err.println("Error al intentar conectar a la partida: " + e.getMessage());
			e.printStackTrace();
		}

	}

	private static void iniciarServidorPartida(int puertoPartida) {
		// inicializar al ServidorPartida
		ServidorPartida s = new ServidorPartida(puertoPartida);
		if (s.esperarJugador()) {
			s.elegirPrimerTurno();
			s.EmpezarPartida();
		}

	}
}
