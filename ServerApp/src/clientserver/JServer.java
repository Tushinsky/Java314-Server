package clientserver;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class JServer extends Thread  {
    private static final int PORT = 5555;// номер порта, который будет прослушивать сервер
    // шаблоны сообщений сервера
    private final String MSG = "Клиент '%d' отправил мне сообщение:\n\r";
    private final String CONN = "Клиент '%d' закрыл соединение";
    private Socket socket;// канал для связи сервера и клиента (или приложений)
    private int num;// номер клиента
    private static String[] string;// массив цитат

    public void setSocket(int num, Socket socket) {
        this.num = num;
        this.socket = socket;

        start();// запуска канала сервера (запуск потока)
    }

    @Override
    public void run(){
        try {
            // входной и выходной потоки данных
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            String line;
            while(true) {
                // читаем данные пока канал не будет закрыт
                line = dis.readUTF();// читаем с поддержкой UTF-формата
                
                if(line.equalsIgnoreCase("yes")) {
                    // если введено YES, выбираем цитату из массива случайным образом
                    Random random = new Random();
                    // печать информационных сообщений
                    System.out.printf(MSG, num);
                    System.out.println(line);
                    int i = random.nextInt(5);
                    // вывод информации в поток данных
                    System.out.println("Отправляю обратно...\n\t" + string[i]);
                    dos.writeUTF(string[i]);
                    dos.flush();// очищаем поток и выводим все данные
                    System.out.println();
                    
                }
                
                if(line.endsWith("no") || line.equalsIgnoreCase("quit")) {
                    // если введено quit, закрываем канал и выходим из цикла
                    socket.close();
                    System.out.printf(CONN, num);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("exception: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ServerSocket srvSocket = null;// создаём канал сервера
        int i = 0;// начальное занчение счётчика клиентов
        try {
            try {
                // получаем IP адрес локальгого компьютера (если сервер расположен на локальной машине)
                InetAddress ia = InetAddress.getByName("localhost");
                srvSocket = new ServerSocket(PORT, 0, ia);// создаём канал
                System.out.println("Сервер запущен");
                string = new String[]{"Если вы можете мечтать об этом, вы можете это сделать.",
                    "Не считай дни, извлекай из них пользу.",
                    "Не ждите. Время никогда не будет подходящим.",
                    "Неисследованная жизнь не стоит того, чтобы ее жить.",
                    "Усердно работайте, мечтайте по-крупному.",
                    "Я не потерпел неудачу. Я просто нашел 10 000 способов, которые не работают."};
                // запускается бесконечный цикл ожидания подключения клиентов
                while (true) {
                    Socket socket = srvSocket.accept();// создаём канал для принятия данных
                    System.err.println("\n\nКлиент принят");
                    new JServer().setSocket(i++, socket);// создание нашего класса - сервера
                }
            } catch (IOException ex) {
                System.out.println("Исключение: " + ex);
            }


        } finally {
            // в случае ошибки закрываем созданный канал
            try{
                if(srvSocket != null) {
                    srvSocket.close();
                }
            } catch (IOException ex) {
                System.out.println("Исключение: " + ex);
            }
        }
    }
}
