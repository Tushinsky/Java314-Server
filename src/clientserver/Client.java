package clientserver;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends Thread {
    private static final int SERVER_PORT = 8192;// номер канала для связи с сервером
    private static final String LOCAL_HOST = "127.0.0.1";// IP адрес компьютера

    public static void main(String[] args) {
        Socket socket = null;
        try {
            try {
                System.out.println("Добро пожаловать на клиентскую сторону\n" +
                        "Подключение к серверу\n\t(IP адрес " + LOCAL_HOST + ")" +
                        " порт" + SERVER_PORT + ")");
                InetAddress ipAddress = InetAddress.getByName(LOCAL_HOST);
                socket = new Socket(ipAddress, SERVER_PORT);
                System.out.println("Соединение установлено");
                System.out.println("\tАдрес хоста = " + socket.getInetAddress().getHostAddress() +
                        "\tРазмер буфера = " + socket.getReceiveBufferSize());
                // объекты для чтения и передачи данных на сервер
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                InputStreamReader isr = new InputStreamReader(System.in);
                // объект для чтения с клавиатуры
                BufferedReader keyBoard = new BufferedReader(isr);
                /*
                connect:10.29.66.235;3050;consumer?encoding=win1251;Tushinsky_S;12092072
                */
                String line;
                while (true) {
                    System.out.println("Напишите и нажмите Enter");
                    line = keyBoard.readLine();
                    dos.writeUTF(line);
                    dos.flush();
                    line = dis.readUTF();

                    if(line.endsWith("quit")) {
                        break;
                    } else {
                        System.out.println("\nСервер отправил мне эту строку:\n\t" + line);
                        System.out.println();
                    }
                }
                System.exit(0);
            } catch (IOException e) {
                System.out.println("error=" + e.getMessage());
            }
        } finally {

            try {
                if(socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("error=" + e.getMessage());
            }
        }

    }
}
