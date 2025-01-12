import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        final int PORT = 5000;
        Map<String, Socket> conectados = new HashMap<>();

        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true){
            Socket cliente = serverSocket.accept(); // acepta cada nueva conexión
            System.out.println("Cliente conectado: " + cliente.getInetAddress());

            new Thread(() -> Main.manejarCliente(cliente)).start();
        }
    }

    public static void manejarCliente(Socket cliente) {
        try {

            // Crea los flujos de entrada y salida una sola vez por cliente
            ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
            ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());

            while (true) {

                // Escuchar y procesar solicitudes
                Map<String, String> datos = (Map<String, String>) entrada.readObject();
                String peticion = datos.get("peticion");

                switch (peticion) {
                    case "login":
                        boolean loginSuccess = comprobarLogin(datos.get("correo"), datos.get("password"));

                        if (loginSuccess) {
                            salida.writeObject("true");
                        } else {
                            salida.writeObject("false");
                        }

                        salida.flush();

                        break;

                    case "contactos":
                        // Procesa los contactos
                        ListaUsuarios listaUsuarios = new ListaUsuarios(obtenerContactos());

                        Gson gsonContactos = new GsonBuilder().setPrettyPrinting().create();
                        String json = gsonContactos.toJson(listaUsuarios);

                        salida.writeObject(json);
                        salida.flush();

                        break;

                    case "register":
                        boolean correoUsed = comprobarCorreo(datos.get("correo"));
                        if (!correoUsed) {
                            boolean registeredUser = registrarUsuario(datos.get("nombre"), datos.get("estado"),
                                    datos.get("correo"), datos.get("password"));
                            salida.writeObject(registeredUser ? "true" : "false");
                        } else {
                            salida.writeObject("false");
                        }

                        salida.flush();

                        break;

                    case "mensaje":
                        // Procesa los mensajes
                        BufferedReader entradaMensaje = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
                        String packetJson;
                        while ((packetJson = entradaMensaje.readLine()) != null) {
                            Gson gson = new Gson();
                            Mensaje mensaje = gson.fromJson(packetJson, Mensaje.class);
                            guardarMensaje(mensaje);
                        }

                        salida.flush();

                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
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

    public static boolean registrarUsuario(String nombre, String estado, String correo, String password){
        String bd = "whatsapp";
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "1234";
        String driver = "com.mysql.cj.jdbc.Driver";

        String insertQuery = "INSERT INTO usuarios (nombre, estado, correo, password) VALUES (?, ?, ?, ?)";
        try{
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url+bd, user, pass);

            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

            preparedStatement.setString(1, nombre);
            preparedStatement.setString(2, estado);
            preparedStatement.setString(3, correo);
            preparedStatement.setString(4, password);

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

    public static ArrayList<Usuario> obtenerContactos(){
        ArrayList<Usuario> usuarios = new ArrayList<>();

        String bd = "whatsapp";
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "1234";
        String driver = "com.mysql.cj.jdbc.Driver";
        Connection connection;

        final String SELECT_QUERY = "SELECT id, nombre, estado, correo, password FROM usuarios";

        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url+bd, user, pass);

            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_QUERY);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                usuarios.add(new Usuario(resultSet.getInt("id"), resultSet.getString("nombre"),
                        resultSet.getString("estado"), resultSet.getString("correo"),
                        resultSet.getString("password")));
            }
            return usuarios;

        }catch (Exception e){
            System.out.println(e);
            return null;
        }
    }
}