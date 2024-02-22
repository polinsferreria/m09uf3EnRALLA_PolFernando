import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            // Conectar con el Servidor Central
            Socket servidorCentralSocket = new Socket("localhost", 7879);
            BufferedReader in = new BufferedReader(new InputStreamReader(servidorCentralSocket.getInputStream()));
            PrintWriter out = new PrintWriter(servidorCentralSocket.getOutputStream(), true);

            // Implementar la lógica del menú y la interacción con el usuario
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String opcion;

            do {
                System.out.println("1. Crear una partida nueva");
                System.out.println("2. Unirse a una partida");
                System.out.println("3. Salir");
                System.out.print("Selecciona una opción: ");
                opcion = userInput.readLine();

                if (opcion.equals("1")) {
                    out.println("CREAR");
                    String respuesta = in.readLine();
                    if (respuesta.startsWith("OK")) {
                        int puertoPartida = Integer.parseInt(respuesta.split(" ")[1]);
                        System.out.println("Partida creada con éxito. Puerto de juego: " + puertoPartida);
                    } else {
                        System.out.println("Error al crear la partida: " + respuesta);
                    }
                } else if (opcion.equals("2")) {
                    out.println("UNIRME");
                    String respuesta = in.readLine();
                    if (respuesta.equals("NO_HAY_PARTIDAS")) {
                        System.out.println("No hay partidas disponibles para unirse.");
                    } else {
                        String[] partidaInfo = respuesta.split("::");
                        String ipPartida = partidaInfo[0];
                        int puertoPartida = Integer.parseInt(partidaInfo[1]);
                        System.out.println("Unido a la partida en la dirección IP: " + ipPartida + ", Puerto: " + puertoPartida);
                    }
                }

            } while (!opcion.equals("3"));

            // Cerrar conexiones
            servidorCentralSocket.close();
            userInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
