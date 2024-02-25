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
        try {
            serverSocket = new ServerSocket(PUERTO);
            System.out.println("Servidor de Partida escuchando en el puerto " + serverSocket.getLocalPort());

            clienteSocket = serverSocket.accept();
            System.out.println("Cliente conectado desde " + clienteSocket.getInetAddress() + ":" + clienteSocket.getPort());

            recibir = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
            enviar = new PrintWriter(clienteSocket.getOutputStream(), true);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void elegirPrimerTurno() {
        soyPrimerJugador = new Random().nextBoolean();
        miSimbolo = soyPrimerJugador ? 'X' : 'O';
        simboloOponente = soyPrimerJugador ? 'O' : 'X';
        enviar.println(soyPrimerJugador ? "O" : "X");
        System.out.println("Soy jugador: " + miSimbolo + " (" + (soyPrimerJugador ? "Primer" : "Segundo") + " jugador)");
    }

    public void EmpezarPartida() {
        char[][] tablero = new char[3][3];
        boolean juegoEnCurso = true;

        while (juegoEnCurso) {
            try {
                if (soyPrimerJugador) {
                    enviar.println("LISTO");
                } else {
                    String inicio = recibir.readLine();
                    if (!inicio.equals("LISTO")) {
                        System.out.println("Error de inicio de la partida.");
                        return;
                    }
                }

                if (soyPrimerJugador) {
                    jugarTurnoPropio(tablero);
                } else {
                    jugarTurnoOponente(tablero);
                }

                if (verificarGanador(tablero, miSimbolo)) {
                    System.out.println("¡Felicidades! ¡Has ganado!");
                    juegoEnCurso = false;
                } else if (verificarGanador(tablero, simboloOponente)) {
                    System.out.println("¡Felicidades! ¡Has PERDIDO!");
                    juegoEnCurso = false;
                } else if (tableroLleno(tablero)) {
                    System.out.println("¡El juego ha terminado en empate!");
                    juegoEnCurso = false;
                }

                imprimirTablero(tablero);

                soyPrimerJugador = !soyPrimerJugador;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            serverSocket.close();
            clienteSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void jugarTurnoPropio(char[][] tablero) throws IOException {
        System.out.println("Es tu turno. Ingresa la fila y la columna (ej. 1 2): ");
        String movimiento;
        do {
            movimiento = leerMovimiento();
        } while (!esMovimientoValido(movimiento, tablero));
        enviar.println(movimiento);
        actualizarTablero(tablero, miSimbolo, movimiento);
    }

    private void jugarTurnoOponente(char[][] tablero) throws IOException {
        System.out.println("Esperando el movimiento del oponente...");
        String movimientoOponente = recibir.readLine();
        actualizarTablero(tablero, simboloOponente, movimientoOponente);
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
        for (int i = 0; i < 3; i++) {
            if ((tablero[i][0] == jugador && tablero[i][1] == jugador && tablero[i][2] == jugador)
                    || (tablero[0][i] == jugador && tablero[1][i] == jugador && tablero[2][i] == jugador)) {
                return true;
            }
        }
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
}
