import java.io.*;
import java.net.*;
import java.util.Random;

public class ServidorPartida {
	private int PUERTO = 0; // Puerto aleatorio, generado o proporcionado por el servidor central
	private ServerSocket serverSocket;
	private Socket clienteSocket;
	private BufferedReader recibir;
	private PrintWriter enviar;
	private boolean soyPrimerJugador;
	private char miSimbolo;
	private char simboloOponente;

	public ServidorPartida(int puerto) {
		this.PUERTO = puerto;

	}

	public boolean esperarJugador() {
		try (ServerSocket servidorSocket = new ServerSocket(PUERTO)) {
			System.out.println("Servidor de Partida escuchando en el puerto " + servidorSocket.getLocalPort());

			clienteSocket = servidorSocket.accept();
			System.out.println(
					"Cliente conectado desde " + clienteSocket.getInetAddress() + ":" + clienteSocket.getPort());

			serverSocket = servidorSocket;
			recibir = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
			enviar = new PrintWriter(clienteSocket.getOutputStream(), true);

			// Lógica para la comunicación con el cliente
			/*
			 * String mensajeCliente;
			 * 
			 * while ((mensajeCliente = entrada.readLine()) != null) {
			 * System.out.println("Mensaje del cliente: " + mensajeCliente);
			 * 
			 * // Lógica para procesar los mensajes del cliente if
			 * (mensajeCliente.equals("ADEU")) { break; } else { // Lógica para procesar el
			 * movimiento del cliente y enviar el resumen String resumen =
			 * procesarMovimiento(mensajeCliente); salida.println(resumen); } }
			 */
			return true;

		} catch (IOException e) {

			e.printStackTrace();
			return false;
		}
	}

	public void elegirPrimerTurno() {
		// Determinar aleatoriamente quién empieza
		soyPrimerJugador = new Random().nextBoolean();
		miSimbolo = soyPrimerJugador ? 'X' : 'O';
		simboloOponente = soyPrimerJugador ? 'O' : 'X';
		if (soyPrimerJugador) {
			enviar.println("O");
		} else {
			enviar.println("X");
		}
		System.out.println("soy jugador: " + miSimbolo + soyPrimerJugador);// quitar soyPrimerJugador
	}

	public void EmpezarPartida() {
		char[][] tablero = new char[3][3];
		boolean juegoEnCurso = true;
		if (soyPrimerJugador) {
			// Envía la señal de inicio al oponente
			enviar.println("LISTO");

		} else {
			// Espera la señal de inicio y quién comienza del oponente
			String inicio;
			try {
				inicio = recibir.readLine();

				if (!inicio.equals("LISTO")) {
					System.out.println("Error de inicio de la partida.");
					return;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
					movimiento = leerMovimiento();
				} while (!esMovimientoValido(movimiento, tablero));
				enviar.println(movimiento);
				actualizarTablero(tablero, miSimbolo, movimiento);
			} else {
				System.out.println("Esperando el movimiento del oponente...");
				String movimientoOponente;
				try {
					movimientoOponente = recibir.readLine();

					actualizarTablero(tablero, simboloOponente, movimientoOponente);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// Verificar el estado del juego
			if (verificarGanador(tablero, miSimbolo)) {
				// imprimirTablero(tablero);
				System.out.println("¡Felicidades! ¡Has ganado!");
				juegoEnCurso = false;
			} else if (verificarGanador(tablero, simboloOponente)) {
				// imprimirTablero(tablero);
				System.out.println("¡Felicidades! ¡Has PERDIDO!");
				juegoEnCurso = false;
			} else if (tableroLleno(tablero)) {
				// imprimirTablero(tablero);
				System.out.println("¡El juego ha terminado en empate!");
				juegoEnCurso = false;
			}

			// -------------------------ferb imprimo aqui mejor creo
			// :0:------------------------
			imprimirTablero(tablero);

			// Cambiar de turno
			soyPrimerJugador = !soyPrimerJugador;
		}

		try {
			serverSocket.close();
			clienteSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String leerMovimiento() {
		try {
			BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
			return userInput.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static boolean esMovimientoValido(String movimiento, char[][] tablero) {
		String[] partes = movimiento.split(" ");
		if (partes.length != 2) {
			System.out.println("Formato incorrecto. Debe ingresar dos valores separados por espacio.");
			return false;
		}
		int fila, columna;
		try {
			fila = Integer.parseInt(partes[0]);
			columna = Integer.parseInt(partes[1]);
		} catch (NumberFormatException e) {
			System.out.println("Formato incorrecto. Debe ingresar valores numéricos.");
			return false;
		}
		if (fila < 1 || fila > 3 || columna < 1 || columna > 3) {
			System.out.println("Valores fuera de rango. Debe ingresar valores entre 1 y 3.");
			return false;
		}
		if (tablero[fila - 1][columna - 1] != 0) {
			System.out.println("La celda seleccionada ya está ocupada. Por favor, elige otra celda.");
			return false;
		}
		return true;
	}

	public static void actualizarTablero(char[][] tablero, char jugador, String movimiento) {
		/*
		 * if (movimiento.equals("LISTO")) {
		 * System.out.println("El oponente está listo para comenzar."); return; // No es
		 * un movimiento válido, no actualizamos el tablero }
		 */

		String[] partes = movimiento.split(" ");
		int fila = Integer.parseInt(partes[0]) - 1;
		int columna = Integer.parseInt(partes[1]) - 1;

		tablero[fila][columna] = jugador;
	}

	public static boolean tableroLleno(char[][] tablero) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (tablero[i][j] == 0) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean verificarGanador(char[][] tablero, char jugador) {
		// Verificar filas y columnas
		for (int i = 0; i < 3; i++) {
			if ((tablero[i][0] == jugador && tablero[i][1] == jugador && tablero[i][2] == jugador)
					|| (tablero[0][i] == jugador && tablero[1][i] == jugador && tablero[2][i] == jugador)) {
				return true;
			}
		}

		// Verificar diagonales
		return (tablero[0][0] == jugador && tablero[1][1] == jugador && tablero[2][2] == jugador)
				|| (tablero[0][2] == jugador && tablero[1][1] == jugador && tablero[2][0] == jugador);
	}

	public static void imprimirTablero(char[][] tablero) {
		System.out.println("Tablero:");

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (j > 0) {
					System.out.print(" | ");
				}
				System.out.print(tablero[i][j] == 0 ? " " : tablero[i][j]);
			}
			System.out.println();

			if (i < 2) {
				System.out.println("---------");
			}
		}
	}

	/*
	 * private static String procesarMovimiento(String movimiento) { // Lógica para
	 * procesar el movimiento del cliente y actualizar el estado del juego //
	 * Devolver el resumen del juego return
	 * "RESUM\n1 |  O  |  o  | x\n2 |  o  | x |\n3 |  x  |    |   x\n"; }
	 */
}

/*
 * private static void jugarTresEnRaya(BufferedReader entrada, PrintWriter
 * salida) throws IOException { char[][] tablero = { {' ', ' ', ' '}, {' ', ' ',
 * ' '}, {' ', ' ', ' '} };
 * 
 * salida.println("RESUM"); enviarTablero(salida, tablero);
 * 
 * boolean juegoTerminado = false; char jugadorActual = 'X';
 * 
 * while (!juegoTerminado) { salida.println("JUEGO Introduce fila columna:");
 * 
 * String movimiento = entrada.readLine(); String[] partes =
 * movimiento.split(" "); int fila = Integer.parseInt(partes[0]); int columna =
 * Integer.parseInt(partes[1]);
 * 
 * if (esMovimientoValido(tablero, fila, columna)) { tablero[fila][columna] =
 * jugadorActual; enviarTablero(salida, tablero);
 * 
 * if (hayGanador(tablero, jugadorActual)) { salida.println("Guanya " +
 * jugadorActual); juegoTerminado = true; } else if (hayEmpate(tablero)) {
 * salida.println("Empat"); juegoTerminado = true; } else { jugadorActual =
 * (jugadorActual == 'X') ? 'O' : 'X'; } } else {
 * salida.println("ERROR Movimiento inválido. Inténtalo de nuevo."); } }
 * 
 * salida.println("ADEU"); }
 * 
 * private static void enviarTablero(PrintWriter salida, char[][] tablero) {
 * salida.println("RESUM"); for (int i = 0; i < tablero.length; i++) { for (int
 * j = 0; j < tablero[i].length; j++) { salida.print(tablero[i][j] + " | "); }
 * salida.println(); } salida.println(); }
 * 
 * private static boolean esMovimientoValido(char[][] tablero, int fila, int
 * columna) { return fila >= 0 && fila < 3 && columna >= 0 && columna < 3 &&
 * tablero[fila][columna] == ' '; }
 * 
 * private static boolean hayGanador(char[][] tablero, char jugador) { //
 * Comprobar filas, columnas y diagonales for (int i = 0; i < 3; i++) { if
 * ((tablero[i][0] == jugador && tablero[i][1] == jugador && tablero[i][2] ==
 * jugador) || (tablero[0][i] == jugador && tablero[1][i] == jugador &&
 * tablero[2][i] == jugador)) { return true; } } return (tablero[0][0] ==
 * jugador && tablero[1][1] == jugador && tablero[2][2] == jugador) ||
 * (tablero[0][2] == jugador && tablero[1][1] == jugador && tablero[2][0] ==
 * jugador); }
 * 
 * private static boolean hayEmpate(char[][] tablero) { for (int i = 0; i < 3;
 * i++) { for (int j = 0; j < 3; j++) { if (tablero[i][j] == ' ') { return
 * false; // Todavía hay casillas vacías } } } return true; // Todas las
 * casillas están ocupadas, es un empate } }
 */
