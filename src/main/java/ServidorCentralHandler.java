
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServidorCentralHandler implements Runnable {
    private Socket clientSocket;

    public ServidorCentralHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String request = in.readLine();

            if (request.equals("CREAR")) {
                int puertoPartida = (int) (Math.random() * (60000 - 49153 + 1) + 49153);
                Partida nuevaPartida = new Partida(clientSocket.getInetAddress().getHostAddress(), puertoPartida);
                ServidorCentral.agregarPartidaEnEspera(nuevaPartida);

                out.println("OK " + puertoPartida);
            } else if (request.equals("UNIRME")) {
                Partida partida = ServidorCentral.obtenerPartidaEnEspera();

                if (partida != null) {
                    out.println(partida.getIpJugador1() + "::" + partida.getPuertoJugador1());
                } else {
                    out.println("NO_HAY_PARTIDAS");
                }
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}