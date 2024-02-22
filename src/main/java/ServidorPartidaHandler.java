
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServidorPartidaHandler implements Runnable {
    private Socket clientSocket;

    public ServidorPartidaHandler(Socket clientSocket) {
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
                ServidorCentral.agregarPartidaNueva(clientSocket.getInetAddress().getHostAddress(), puertoPartida);
                out.println("OK " + puertoPartida);
            } else if (request.equals("UNIRME")) {
                String partidaInfo = ServidorCentral.obtenerPartidaNueva();
                if (partidaInfo != null) {
                    out.println(partidaInfo);
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