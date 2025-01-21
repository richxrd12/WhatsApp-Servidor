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

            new Thread(() -> Main.manejarCliente(cliente, conectados)).start();
        }
    }

    public static void manejarCliente(Socket cliente, Map<String, Socket> conectados) {
        try {

            // Crea los flujos de entrada y salida una sola vez por cliente
            ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());
            ObjectOutputStream salida = new ObjectOutputStream(cliente.getOutputStream());

            int idCliente = 0;
            int idContacto = 0;
            while (true) {

                // Escuchar y procesar solicitudes
                Map<String, String> datos = (Map<String, String>) entrada.readObject();
                String peticion = datos.get("peticion");

                switch (peticion) {
                    case "login":
                        idCliente = comprobarLogin(datos.get("correo"), datos.get("password"));

                        salida.writeObject(String.valueOf(idCliente));
                        salida.flush();

                        conectados.put(String.valueOf(idCliente), cliente);

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

                    case "contactos":
                        // Procesa los contactos
                        ListaUsuarios listaUsuarios = new ListaUsuarios(obtenerContactos());

                        Gson gsonContactos = new GsonBuilder().setPrettyPrinting().create();
                        String json = gsonContactos.toJson(listaUsuarios);

                        salida.writeObject(json);
                        salida.flush();

                        break;

                    case "peticion-mensajes":
                        idContacto = Integer.parseInt(datos.get("idContacto"));

                        ArrayList<Mensaje> mensajes = obtenerMensajes(idCliente, idContacto);

                        Gson gsonMensajes = new GsonBuilder().setPrettyPrinting().create();
                        String jsonMensajes = gsonMensajes.toJson(mensajes);

                        salida.writeObject(jsonMensajes);

                        salida.flush();

                        break;

                    case "envio-mensaje":

                        String mensajeEnviado = datos.get("mensaje");
                        String fecha = datos.get("fecha");

                        Mensaje mensaje = new Mensaje(0, idCliente, idContacto, mensajeEnviado, fecha);

                        guardarMensaje(mensaje);

                        Socket contacto = conectados.get(String.valueOf(idContacto));

                        if (contacto != null){

                            OutputStream outputStream = contacto.getOutputStream();
                            ObjectOutputStream streamContacto = new ObjectOutputStream(outputStream);

                            String respuesta = "mensaje-recibido";

                            streamContacto.writeObject(respuesta);

                            streamContacto.flush();
                        }
                        salida.flush();

                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static int comprobarLogin(String correo, String password){
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
                int id = resultSet.getInt("id");
                connection.close();
                return id;
            }else{
                connection.close();
                return 0;
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
            preparedStatement.setInt(2, mensaje.getDestinatario());
            preparedStatement.setInt(3, mensaje.getRemitente());
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

    public static ArrayList<Mensaje> obtenerMensajes(int idCliente, int idContacto){
        ArrayList<Mensaje> mensajes = new ArrayList<>();

        String bd = "whatsapp";
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "1234";
        String driver = "com.mysql.cj.jdbc.Driver";
        Connection connection;

        final String SELECT_QUERY = "SELECT id, mensaje, idDestinatario, idRemitente, fecha FROM mensajes " +
                "WHERE idDestinatario = ? AND idRemitente = ? OR idDestinatario = ? AND idRemitente = ? " +
                "ORDER BY fecha ASC;";

        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url+bd, user, pass);

            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_QUERY);

            preparedStatement.setInt(1, idCliente);
            preparedStatement.setInt(2, idContacto);
            preparedStatement.setInt(3, idContacto);
            preparedStatement.setInt(4, idCliente);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                mensajes.add(new Mensaje(resultSet.getInt("id"),
                        resultSet.getInt("idRemitente"), resultSet.getInt("idDestinatario"),
                        resultSet.getString("mensaje"), resultSet.getString("fecha")));
            }

            return mensajes;

        }catch (Exception e){
            System.out.println(e);
            return null;
        }
    }
}