import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        //Esto es la parte del servidor, hay que mirar como pasarle a x persona
        //exactamente el mensaje que le vamos a enviar
        final int PORT = 5000;

        //Aquí se guardarán los conectados
        Map<String, Socket> conectados = new HashMap<>();

        try (ServerSocket serverSocket = new ServerSocket(PORT)){

            System.out.println("Servidor escuchando en el puerto " + PORT);

            while (true){
                Socket cliente =  serverSocket.accept();

                //Para recibir el Map con el correo y la contraseña
                ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
                Map<String, String> datos = (Map<String, String>) entrada.readObject();

                //Se comprueba si se va a logear o si se va a registrar
                if (datos.get("estado").equals("login")){
                    boolean loginSuccess = comprobarLogin(datos.get("correo"), datos.get("password"));

                    if (loginSuccess) {
                        //Si el login es correcto le manda el estado true al usuario y empieza a escuchar para recibir mensajes
                        System.out.println("El usuario se ha logeado correctamente");
                        ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
                        salida.writeObject("true");

                        //Lo añadimos a la lista de usuarios conectados
                        conectados.put("Usuario", cliente); //Hardcodeado
                        System.out.println("Cliente conectado: " + cliente.getInetAddress());

                        while (true){
                            //Lógica para guardar los mensajes en el servidor
                            BufferedReader entradaMensaje = new BufferedReader(new InputStreamReader(cliente.getInputStream()));

                            //Leemos el JSON que nos envía el Remitente
                            String packetJson;
                            while((packetJson = entradaMensaje.readLine()) != null){
                                Gson gson = new Gson();
                                Mensaje mensaje = gson.fromJson(packetJson, Mensaje.class);

                                //Guardamos el mensaje en la base de datos
                                guardarMensaje(mensaje);
                            }
                        }

                    } else{
                        System.out.println("No se ha podido logear");
                        ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
                        salida.writeObject("false");
                    }
                } else {
                    //Comprobar primero si el correo no está utilizado por ningún usuario
                    boolean correoUsed = comprobarCorreo(datos.get("correo"));

                    if (!correoUsed){
                        //Se registra al usuario y vuelve a esperar a que se logee
                        boolean registeredUser = registrarUsuario(datos.get("nombre"), datos.get("correo"), datos.get("password"));

                        if (registeredUser) {
                            ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
                            salida.writeObject("true");
                        } else {
                            ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
                            salida.writeObject("false");
                        }
                    } else {
                        ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());
                        salida.writeObject("false");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean comprobarLogin(String correo, String password){
        String bd = "whatsapp";
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "1234";
        String driver = "com.mysql.cj.jdbc.Driver";
        Connection connection;

        final String SELECT_QUERY = "SELECT * FROM usuarios where correo = ? and password = ?";

        try{
            Class.forName(driver);
            connection = DriverManager.getConnection(url+bd, user, pass);

            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_QUERY);

            preparedStatement.setString(1, correo);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                connection.close();
                return true;
            }else{
                connection.close();
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void guardarMensaje(Mensaje mensaje){
        String bd = "whatsapp";
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "1234";
        String driver = "com.mysql.cj.jdbc.Driver";

        String insertQuery = "INSERT INTO mensajes (mensaje, idDestinatario, idRemitente, fecha) VALUES (?, ?, ?, ?)";
        try{
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url+bd, user, pass);

            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

            preparedStatement.setString(1, mensaje.getMensaje());
            preparedStatement.setInt(2, mensaje.getDestinatario().getId());
            preparedStatement.setInt(3, mensaje.getRemitente().getId());
            preparedStatement.setString(4, mensaje.getFecha());

            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0){
                System.out.println("Se ha insertado el mensaje correctamente");
            }else{
                System.out.println("Prueba otra cosa ihte");
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean registrarUsuario(String nombre, String correo, String password){
        String bd = "whatsapp";
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "1234";
        String driver = "com.mysql.cj.jdbc.Driver";

        String insertQuery = "INSERT INTO usuarios (nombre, correo, password) VALUES (?, ?, ?)";
        try{
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url+bd, user, pass);

            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

            preparedStatement.setString(1, nombre);
            preparedStatement.setString(2, correo);
            preparedStatement.setString(3, password);

            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0){
                System.out.println("El usuario se ha registrado correctamente");
                return true;
            }else{
                System.out.println("Prueba otra cosa ihte");
                return false;
            }

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean comprobarCorreo(String correo){
        String bd = "whatsapp";
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "1234";
        String driver = "com.mysql.cj.jdbc.Driver";
        Connection connection;

        final String SELECT_QUERY = "SELECT * FROM usuarios where correo = ?";

        try{
            Class.forName(driver);
            connection = DriverManager.getConnection(url+bd, user, pass);

            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_QUERY);

            preparedStatement.setString(1, correo);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()){
                connection.close();
                return true;
            }else{
                connection.close();
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}