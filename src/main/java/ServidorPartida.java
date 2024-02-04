import java.net.*;
import java.io.*;

public class ServidorPartida {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor de Partida esperant connexió...");

            Socket clientSocket = serverSocket.accept();
            System.out.println("Connexió establerta amb un jugador.");

            // Lògica del joc
            jugarPartida(clientSocket);

            System.out.println("Partida acabada. Tancant connexió...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void jugarPartida(Socket clientSocket) {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            out.println("Benvingut a la partida! Comencem el joc.");

            char[][] tauler = { { ' ', ' ', ' ' }, { ' ', ' ', ' ' }, { ' ', ' ', ' ' } };

            boolean guanyador = false;
            int jugades = 0;

            while (!guanyador && jugades < 9) {
                // Mostrar l'estat actual del tauler
                out.println("Tauler actual:");
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        out.print(tauler[i][j] + " ");
                    }
                    out.println();
                }

                // Rebre jugada del client
                out.println("Introdueix la teva jugada (fila columna): ");
                String jugada = in.readLine();
                int fila = Character.getNumericValue(jugada.charAt(0));
                int columna = Character.getNumericValue(jugada.charAt(2));

                // Validar la jugada
                if (fila >= 0 && fila < 3 && columna >= 0 && columna < 3 && tauler[fila][columna] == ' ') {
                    tauler[fila][columna] = 'X'; // Marcem la jugada del jugador
                    jugades++;
                } else {
                    out.println("Jugada no vàlida. Torna a intentar.");
                    continue;
                }

                // Comprovar si hi ha un guanyador
                guanyador = comprovarGuanyador(tauler);

                if (!guanyador && jugades < 9) {
                    // Simular la jugada de l'oponent (l'ordinador en aquest cas)
                    int[] jugadaOponent = ferJugadaOponent(tauler);
                    tauler[jugadaOponent[0]][jugadaOponent[1]] = 'O';
                    jugades++;
                }
            }

            // Mostrar resultat final
            out.println("Resultat final:");
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    out.print(tauler[i][j] + " ");
                }
                out.println();
            }

            if (guanyador) {
                out.println("Felicitats! Has guanyat!");
            } else {
                out.println("La partida ha acabat en empat.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean comprovarGuanyador(char[][] tauler) {
        // Implementar la lògica per comprovar si hi ha un guanyador
        // Pots adaptar aquesta funció segons les regles específiques del teu joc
        // En aquest exemple, només es comprova si hi ha tres 'X' consecutives en fila, columna o diagonal
        return (
            (tauler[0][0] == 'X' && tauler[0][1] == 'X' && tauler[0][2] == 'X') ||
            (tauler[1][0] == 'X' && tauler[1][1] == 'X' && tauler[1][2] == 'X') ||
            (tauler[2][0] == 'X' && tauler[2][1] == 'X' && tauler[2][2] == 'X') ||
            (tauler[0][0] == 'X' && tauler[1][0] == 'X' && tauler[2][0] == 'X') ||
            (tauler[0][1] == 'X' && tauler[1][1] == 'X' && tauler[2][1] == 'X') ||
            (tauler[0][2] == 'X' && tauler[1][2] == 'X' && tauler[2][2] == 'X') ||
            (tauler[0][0] == 'X' && tauler[1][1] == 'X' && tauler[2][2] == 'X') ||
            (tauler[0][2] == 'X' && tauler[1][1] == 'X' && tauler[2][0] == 'X')
        );
    }

    private static int[] ferJugadaOponent(char[][] tauler) {
        // Implementar la lògica per a la jugada de l'oponent (en aquest exemple, simplement omple el primer espai buit)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (tauler[i][j] == ' ') {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{0, 0}; // Retornar una jugada per defecte (aquesta situació no es donarà mai en un joc real)
    }
}
