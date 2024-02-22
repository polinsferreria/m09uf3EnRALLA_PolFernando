import java.io.*;
import java.net.*;
import java.util.Random;

public class Cliente {
	public static void main(String[] args) {
		try {
			BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

			System.out.println("Bienvenido al Juego en Red");
			System.out.println("1. Crear partida");
			System.out.println("2. Unirse a una partida");
			System.out.println("3. Salir");

			System.out.print("Ingrese su opción: ");
			int opcion = Integer.parseInt(userInput.readLine());

			if (opcion == 1 || opcion == 2) {
				String servidorCentralIP = "localhost";
				int servidorCentralPort = 7879;

				Socket serverCentralSocket = new Socket(servidorCentralIP, servidorCentralPort);
				BufferedReader in = new BufferedReader(new InputStreamReader(serverCentralSocket.getInputStream()));
				PrintWriter out = new PrintWriter(serverCentralSocket.getOutputStream(), true);

				if (opcion == 1) {
					// Crear partida
					out.println("CREAR");
					String response = in.readLine();

					if (response.startsWith("OK")) {
						int puertoPartida = Integer.parseInt(response.split(" ")[1]);
						System.out.println("Partida creada. Esperando a un oponente en el puerto " + puertoPartida);

						// Lógica del juego
						jugarPartida(puertoPartida, true);

					} else {
						System.out.println("Error al crear la partida: " + response);
					}
				} else if (opcion == 2) {
					// Unirse a una partida
					out.println("UNIRME");
					String response = in.readLine();

					if (response.equals("NO_HAY_PARTIDAS")) {
						System.out.println("No hay partidas disponibles. Inténtelo más tarde.");
					} else {
						String[] partidaInfo = response.split("::");
						String ipPartida = partidaInfo[0];
						int puertoPartida = Integer.parseInt(partidaInfo[1]);

						System.out.println(
								"Unido a una partida. Esperando al oponente en " + ipPartida + ":" + puertoPartida);

						// Lógica del juego
						jugarPartidaComoCliente(ipPartida, puertoPartida);
					}
				}

				serverCentralSocket.close();
			} else {
				System.out.println("Hasta luego.");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void jugarPartidaComoCliente(String ipPartida, int puertoPartida) {
    try {
        System.out.println("Intentando conectar a la partida en " + ipPartida + ":" + puertoPartida);
        Socket partidaSocket = new Socket(ipPartida, puertoPartida);
        System.out.println("Conexión establecida con éxito.");
        BufferedReader partidaIn = new BufferedReader(new InputStreamReader(partidaSocket.getInputStream()));
        PrintWriter partidaOut = new PrintWriter(partidaSocket.getOutputStream(), true);

        // Determinar aleatoriamente quién empieza
        boolean soyPrimerJugador = new Random().nextBoolean();
        char miSimbolo = soyPrimerJugador ? 'X' : 'O';
        char simboloOponente = soyPrimerJugador ? 'O' : 'X';

        char[][] tablero = new char[3][3];
        boolean juegoEnCurso = true;

        if (soyPrimerJugador) {
            // Envía la señal de inicio al oponente
            partidaOut.println("LISTO");
            // Envía la información sobre quién comienza
            partidaOut.println("TU_TURNO");
        } else {
            // Espera la señal de inicio y quién comienza del oponente
            String inicio = partidaIn.readLine();
            if (!inicio.equals("LISTO")) {
                System.out.println("Error de inicio de la partida.");
                return;
            }
            String turnoOponente = partidaIn.readLine();
            if (!turnoOponente.equals("TU_TURNO")) {
                System.out.println("Error en la información del turno del oponente.");
                return;
            }
        }

        while (juegoEnCurso) {
            // Imprimir tablero
            imprimirTablero(tablero);

            // Turno del jugador
            if (soyPrimerJugador) {
                System.out.println("Es tu turno. Ingresa la fila y la columna (ej. 1 2): ");
                String movimiento = leerMovimiento();
                partidaOut.println(movimiento);
                actualizarTablero(tablero, miSimbolo, movimiento);
            } else {
                System.out.println("Esperando el movimiento del oponente...");
                String movimientoOponente = partidaIn.readLine();
                actualizarTablero(tablero, simboloOponente, movimientoOponente);
            }

            // Verificar el estado del juego
            if (verificarGanador(tablero, miSimbolo)) {
                imprimirTablero(tablero);
                System.out.println("¡Felicidades! ¡Has ganado!");
                juegoEnCurso = false;
            } else if (tableroLleno(tablero)) {
                imprimirTablero(tablero);
                System.out.println("¡El juego ha terminado en empate!");
                juegoEnCurso = false;
            }

            // Cambiar de turno
            soyPrimerJugador = !soyPrimerJugador;
        }

        partidaSocket.close();
    } catch (IOException e) {
        System.err.println("Error al intentar conectar a la partida: " + e.getMessage());
        e.printStackTrace();
    }
}

	private static void jugarPartida(int puertoPartida, boolean primerJugador) {
		try {
			ServerSocket serverSocket = new ServerSocket(puertoPartida);
			System.out.println("Esperando a que un oponente se una en el puerto " + puertoPartida + "...");

			Socket oponenteSocket = serverSocket.accept();
			System.out.println("Oponente conectado. Comienza el juego.");

			BufferedReader oponenteIn = new BufferedReader(new InputStreamReader(oponenteSocket.getInputStream()));
			PrintWriter oponenteOut = new PrintWriter(oponenteSocket.getOutputStream(), true);

			// Determinar aleatoriamente quién empieza
			boolean soyPrimerJugador = primerJugador;
			char miSimbolo = soyPrimerJugador ? 'X' : 'O';
			char simboloOponente = soyPrimerJugador ? 'O' : 'X';

			// Envía la señal de inicio al oponente
			oponenteOut.println("LISTO");
			// Envía la información sobre quién comienza
			oponenteOut.println(soyPrimerJugador ? "TU_TURNO" : "TURNO_OPONENTE");

			char[][] tablero = new char[3][3];
			boolean juegoEnCurso = true;

			while (juegoEnCurso) {
				// Imprimir tablero
				imprimirTablero(tablero);

				// Turno del jugador
				if (soyPrimerJugador) {
					System.out.println("Es tu turno. Ingresa la fila y la columna (ej. 1 2): ");
					String movimiento = leerMovimiento();
					oponenteOut.println(movimiento);
					actualizarTablero(tablero, miSimbolo, movimiento);
				} else {
					System.out.println("Esperando el movimiento del oponente...");
					String movimientoOponente = oponenteIn.readLine();
					actualizarTablero(tablero, simboloOponente, movimientoOponente);
				}

				// Verificar el estado del juego
				if (verificarGanador(tablero, miSimbolo)) {
					imprimirTablero(tablero);
					System.out.println("¡Felicidades! ¡Has ganado!");
					juegoEnCurso = false;
				} else if (tableroLleno(tablero)) {
					imprimirTablero(tablero);
					System.out.println("¡El juego ha terminado en empate!");
					juegoEnCurso = false;
				}

				// Cambiar de turno
				soyPrimerJugador = !soyPrimerJugador;
			}

			serverSocket.close();
			oponenteSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String leerMovimiento() {
		try {
			BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
			return userInput.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	private static void imprimirTablero(char[][] tablero) {
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

	private static void actualizarTablero(char[][] tablero, char jugador, String movimiento) {
		String[] partes = movimiento.split(" ");
		int fila = Integer.parseInt(partes[0]) - 1;
		int columna = Integer.parseInt(partes[1]) - 1;

		tablero[fila][columna] = jugador;
	}

	private static boolean verificarGanador(char[][] tablero, char jugador) {
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

	private static boolean tableroLleno(char[][] tablero) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (tablero[i][j] == 0) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean esMovimientoValido(String movimiento, char[][] tablero) {
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
}
