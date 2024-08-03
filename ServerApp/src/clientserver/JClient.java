package clientserver;


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class JClient extends Thread {
    private static final int SERVER_PORT = 5555;// номер канала для связи с сервером
    private static final String LOCAL_HOST = "127.0.0.1";// IP адрес компьютера

    public static void main(String[] args) {
        Socket socket = null;
        try {
            try {
                System.out.println("Добро пожаловать на клиентскую сторону\n" +
                        ">> Подключение к серверу\n\t(IP адрес " + LOCAL_HOST + ")" +
                        " порт" + SERVER_PORT + ")");
                InetAddress ipAddress = InetAddress.getByName(LOCAL_HOST);
                socket = new Socket(ipAddress, SERVER_PORT);
                System.out.println(">> Соединение установлено");
                System.out.println("\tАдрес хоста = " + socket.getInetAddress().getHostAddress() +
                        "\tРазмер буфера = " + socket.getReceiveBufferSize());
                
                // создаём потоки для чтения и записи данных
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                
                // поток для чтения вводимых данных с клавиатуры
                InputStreamReader isr = new InputStreamReader(System.in);
                BufferedReader keyBoard = new BufferedReader(isr);
                
                String line;
                while (true) {
                    System.out.println("Хотите получить цитату дня: yes/no");
                    line = keyBoard.readLine();// читаем с клавиатуры
                    if(line.endsWith("yes")) {
                        dos.writeUTF(line);// записываем в поток вывода и отправляем на сервер
                        dos.flush();
                        line = dis.readUTF();// читаем из потока ввода ответ сервера
                    }
                    if(line.endsWith("no") || line.endsWith("quit")) {
                        System.out.println("Спасибо за использование ресурса");
                        break;
                    } else {
                        System.out.println("\nСервер отправил мне эту строку:\n\t" + line);
                        System.out.println();
                    }
                }
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
