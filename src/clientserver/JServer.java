package clientserver;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import connection.JDBCConnection;

public class JServer extends Thread  {
    private static final int PORT = 8189;// номер порта, который будет прослушивать сервер
    // шаблоны сообщений сервера
    private final String MSG = "Клиент '%d' отправил мне сообщение:\n\r";
    private final String CONN_MESSAGE = "Клиент '%d' закрыл соединение";
    private Socket socket;// канал для связи сервера и клиента (или приложений)
    private int num;// номер клиента
    private static JDBCConnection connect;
    private boolean connOpen = false;
    private String hostIP;// IP адрес сервера базы данных
    private String serverPort;// порт сервера базы данных
    private String databaseName;// имя базы данных
    private String userName;// имя пользователя
    private String password;// пароль пользователя
    
    public void setSocket(int num, Socket socket) {
        this.num = num;
        this.socket = socket;

        start();// запуск канала сервера (запуск потока)
    }

    @Override
    public void run(){
        try {
            // входной и выходной потоки данных
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            
            // поток для чтения вводимых данных с клавиатуры
//            InputStreamReader isr = new InputStreamReader(System.in);
//            BufferedReader keyBoard = new BufferedReader(isr);
            String line;
            while(true) {
                // читаем данные пока канал не будет закрыт
                line = dis.readUTF();// читаем с поддержкой UTF-формата
                // печать информационных сообщений
                System.out.printf(MSG, num);
                System.out.println(line);
                if(line.startsWith("connect:")) {
                    // если передаётся запрос на соединение с базой данных, проверяем
                    // открывалось ли оно
                    connectToBataBase(dos, line);
                } else if(line.equalsIgnoreCase("no")) {
                    // если введено NO, закрываем соединение
                    closeConnection(dos);
                } else if(line.equalsIgnoreCase("quit")) {
                    // если введено QUIT, закрываем канал и выходим из цикла
                    // вывод информации в поток данных
                    dos.writeUTF(line);
                    dos.flush();// очищаем поток и выводим все данные
                    System.out.println();
                    closeConnection(dos);
                    socket.close();
                    System.out.printf(CONN_MESSAGE, num);
                    System.out.println();
                    break;
                } else {
                    // поступает запрос на получение данных
                    dos.writeUTF(line);
                    dos.flush();// очищаем поток и выводим все данные
                    System.out.println();
                }
                
            }
            System.exit(0);
        } catch (IOException e) {
            System.out.println("exception: " + e.getMessage());
            System.exit(0);
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(JServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws FileNotFoundException, SQLException, ClassNotFoundException {
        ServerSocket srvSocket = null;// создаём канал сервера
        int i = 0;// начальное занчение счётчика клиентов
        try {
            try {
                // получаем IP адрес локальгого компьютера (если сервер расположен на локальной машине)
                InetAddress ia = InetAddress.getByName("localhost");
                srvSocket = new ServerSocket(PORT, 0, ia);// создаём канал
                System.out.println("Ready");// сокет готов
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
    
    /**
     * Открывает соединение с базой данных
     * @return true - в случае удачи, иначе возвращает false
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SQLException
     * @throws ClassNotFoundException 
     */
    private boolean openConnection() throws FileNotFoundException, 
            IOException, SQLException, ClassNotFoundException {
        try {
            // set drivername
            String driver = "org.firebirdsql.jdbc.FBDriver";
            String url = "jdbc:firebirdsql://" + hostIP + ":" +
                serverPort + "/" + databaseName;
            System.out.println("url=" + url);
            // создаём соединение, проверяем его сосотяние
            connect = new JDBCConnection(driver, url, userName, password);
            System.out.println("connect=" + connect.isClosedConn());
            return !connect.isClosedConn();
        } catch (SQLException ex){
            return false;
        }

            
    }
    
    private void connectToBataBase (DataOutputStream dos, String line) throws 
            IOException, FileNotFoundException, SQLException,
            ClassNotFoundException {
        if(connOpen == false) {
            // если соединение на открывалось, разбираем строку на составляющие
            String[] str = line.substring(8).split(";");
            System.out.println("str:" + Arrays.toString(str));
            // для установки соединения нужен массив из 5 элементов
            if(str.length < 5) {
                // вывод информации в поток данных
                dos.writeUTF("Не хватает данных для установки соединения!");
                dos.flush();// очищаем поток и выводим все данные
                System.out.println();

            } else {
                // получаем параметры соединения
                hostIP = str[0];
                serverPort = str[1];
                databaseName = str[2];
                userName = str[3];
                password = str[4];
                // открываем соединение
                connOpen = openConnection();
            }
        }
        if(connOpen == true) {
            dos.writeUTF("Соединение установлено! Введите запрос на получение данных:");
            dos.flush();// очищаем поток и выводим все данные
            System.out.println();
        }
    }
    
    private void closeConnection(DataOutputStream dos) {
        try {
            if (connect != null && !connect.isClosedConn()) {
                JDBCConnection.getConn().close();
                if(connect.isClosedConn()){
                    System.out.println("Соединение закрыто!");
                    connOpen = false;
                }
            }
            // вывод информации в поток данных
            dos.writeUTF("Соединение закрыто");
            dos.flush();// очищаем поток и выводим все данные
            System.out.println();

        } catch (SQLException | IOException ex) {
            Logger.getLogger(JServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
