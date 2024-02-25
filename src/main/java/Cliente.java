import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Scanner;

public class Cliente {
	private static String direccionServidorCentral;
	private static final int PUERTO_SERVIDOR = 7879;

	public static void main(String[] args) {
		try (BufferedReader lector = new BufferedReader(new InputStreamReader(System.in))) {
			menuServidorMaquina();

			if (obtenerOP()) {
				direccionServidorCentral = "localhost"; // Dirección del servidor en la misma máquina
			} else {
				System.out.print("Ingrese la dirección IP del servidor central: ");
				direccionServidorCentral = lector.readLine();
			}

			while (true) {
				mostrarMenuPrincipal();
				int opcion = Integer.parseInt(lector.readLine());

				switch (opcion) {
				case 1:
					int puertoCrearPartida = ingresarPuerto(lector);
		        	String ipCliente = InetAddress.getLocalHost().getHostAddress(); 
					String respuesta = comunicarseConServidorCentral("CREAR " + puertoCrearPartida+ " " + ipCliente);
					procesarRespuestaCrearPartida(respuesta, puertoCrearPartida);
					break;
				case 2:
					respuesta = comunicarseConServidorCentral("UNIR-ME");
					procesarRespuestaUnirseAPartida(respuesta, lector);
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

	private static boolean obtenerOP() {
		Scanner sc = new Scanner(System.in);
		
		do {
			System.out.print("Ingrese 1 si el servidor central está en esta máquina, 2 si no: ");
			if (sc.hasNextInt()) {
				int op = sc.nextInt();
				if (op == 1 || op == 2) {					
					return op == 1;
				}
			} else {
				System.out.println("Error, el formato no es adecuado");
				sc.next();
			}
		} while (true);
		
	}

	private static void mostrarMenuPrincipal() {
		System.out.println("Menú Principal:");
		System.out.println("1. Crear partida");
		System.out.println("2. Unirse a partida");
		System.out.println("3. Salir");
		System.out.print("Seleccione una opción: ");
	}

	private static int ingresarPuerto(BufferedReader lector) throws IOException {
		System.out.print("Ingrese el puerto para la partida: ");
		return Integer.parseInt(lector.readLine());
	}

	private static void procesarRespuestaCrearPartida(String respuesta, int puertoCrearPartida) {
		System.out.println(respuesta);
		if (respuesta.equals("OK")) {
			System.out.println("Esperando a que otro jugador se una...");
			iniciarServidorPartida(puertoCrearPartida);
		} else {
			System.out.println("Error al crear la partida: " + respuesta);
		}
	}

	private static void procesarRespuestaUnirseAPartida(String respuesta, BufferedReader lector) throws IOException {
		System.out.println(respuesta);
		if (respuesta.equals("NO_HAY_PARTIDAS")) {
			System.out.println("No hay partidas. Crea una o inténtalo más tarde.");
		} else {
			String[] partidaInfo = respuesta.split("::");
			String ipPartida = partidaInfo[0];
			int puertoPartida = Integer.parseInt(partidaInfo[1]);
			jugarPartidaComoCliente(ipPartida, puertoPartida);
		}
	}

	private static String comunicarseConServidorCentral(String mensaje) {
		try (DatagramSocket socketUDP = new DatagramSocket()) {
			InetAddress direccionServidor = InetAddress.getByName(direccionServidorCentral);
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
		// Implementar lógica para jugar como cliente
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
		} catch (SocketException e) {
			System.out.println("El otro jugador se ha desconectado o ha ocurrido un problema de conexión.");
			return;
		} catch (IOException e) {
			// Otras excepciones de E/S
			System.out.println("Error de E/S: " + e.getMessage());
			return;
		}

	}

	private static void iniciarServidorPartida(int puertoPartida) {
		// Implementar lógica para iniciar el servidor de la partida
		ServidorPartida s = new ServidorPartida(puertoPartida);
		if (s.esperarJugador()) {
			s.elegirPrimerTurno();
			s.EmpezarPartida();
		}
	}

	private static void menuServidorMaquina() {
		System.out.println("¿El servidor central está en esta máquina?");
		System.out.println("1. Sí");
		System.out.println("2. No");
		System.out.print("Seleccione una opción: ");
	}
}
