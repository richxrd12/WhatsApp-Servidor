import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        final int PORT = 5000;
        final int PORT_ESCUCHA = 5001;

        Map<String, ObjectOutputStream> conectados = new HashMap<>();

        ServerSocket serverSocket = new ServerSocket(PORT);
        ServerSocket serverEscucha = new ServerSocket(PORT_ESCUCHA);

        while (true){
            Socket cliente = serverSocket.accept();

            //Le mandamos un código de éxito para que no se quede el hilo bloqueado
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(cliente.getOutputStream());
            objectOutputStream.writeObject("Conexión establecida");
            objectOutputStream.flush();

            Socket clienteEscucha = serverEscucha.accept();

            //Le mandamos un código de éxito para que no se quede bloqueado el hilo por culpa del otro Socket
            ObjectOutputStream outputStreamEscucha = new ObjectOutputStream(clienteEscucha.getOutputStream());
            outputStreamEscucha.writeObject("Conexión establecida");
            outputStreamEscucha.flush();

            System.out.println("Cliente conectado: " + cliente.getInetAddress());

            new Thread(() -> Main.manejarCliente(cliente, clienteEscucha, conectados, objectOutputStream,
                    outputStreamEscucha)).start();
        }
    }

    public static void manejarCliente(Socket cliente, Socket clienteEscucha, Map<String, ObjectOutputStream> conectados,
                                      ObjectOutputStream salida, ObjectOutputStream salidaEscucha) {

        int idCliente = 0;
        int idContacto = 0;

        try {

            // Crea los flujos de entrada y salida una sola vez por cliente
            ObjectInputStream entrada = new ObjectInputStream(cliente.getInputStream());

            //Crea los flujos de entrada y salida de la escucha una vez por cliente
            ObjectInputStream entradaEscucha = new ObjectInputStream(clienteEscucha.getInputStream());

            while (true) {

                // Escuchar y procesar solicitudes
                Map<String, String> datos = (Map<String, String>) entrada.readObject();
                String peticion = datos.get("peticion");

                switch (peticion) {
                    /**
                     * Si la petición es login, borra el posible id del antiguo login,
                     * nos comprueba si el id y la contraseña son correctas y nos devuelve el id del
                     * cliente directamente.
                     */
                    case "login":
                        conectados.remove(String.valueOf(idCliente));

                        idCliente = comprobarLogin(datos.get("usuario"), datos.get("password"));

                        conectados.put(String.valueOf(idCliente), salidaEscucha);

                        salida.writeObject(String.valueOf(idCliente));
                        salida.flush();

                        break;
                    /**
                     * Si la petición es register nos comprueba si el usuario está en uso, si no está en uso,
                     * lo registra y nos manda el código "true", si no, nos manda el código de error "false"
                      */
                    case "register":
                        boolean usuarioUsed = comprobarUsuario(datos.get("usuario"));
                        if (!usuarioUsed) {
                            boolean registeredUser = registrarUsuario(datos.get("nombre"), datos.get("estado"),
                                    datos.get("usuario"), datos.get("password"));
                            salida.writeObject(registeredUser ? "true" : "false");
                        } else {
                            salida.writeObject("false");
                        }

                        salida.flush();

                        break;

                    /**
                     * Si la petición es contactos, este nos crea un objeto de ListaUsuarios con una lista de usuarios
                     * para serializar en JSON, lo serializa y lo manda al cliente.
                      */
                    case "contactos":
                        // Procesa los contactos
                        ListaUsuarios listaUsuarios = new ListaUsuarios(obtenerContactos(conectados));

                        Gson gsonContactos = new GsonBuilder().setPrettyPrinting().create();
                        String json = gsonContactos.toJson(listaUsuarios);

                        salida.writeObject(json);
                        salida.flush();

                        break;

                    /**
                     * Si nos pide los mensajes, tenemos que pedir los mensajes del contacto al que está entrando en el
                     * chat, los pide, los serializa a JSON y los manda.
                      */
                    case "peticion-mensajes":
                        idContacto = Integer.parseInt(datos.get("idContacto"));

                        ArrayList<Mensaje> mensajes = obtenerMensajes(idCliente, idContacto);

                        Gson gsonMensajes = new GsonBuilder().setPrettyPrinting().create();
                        String jsonMensajes = gsonMensajes.toJson(mensajes);

                        salida.writeObject(jsonMensajes);

                        salida.flush();

                        break;

                    /**
                     * Si la petición es envío mensaje, obtiene el mensaje, la fecha y con el idCliente (guardado al
                     * hacer el login) y el idContacto (guardado al entrar en la conversación, es decir, cuando hacen la
                     * petición de mensajes), guarda el mensaje en la base de datos y si el usuario está conectado,
                     * le manda un código de "mensaje-recibido" de escucha para que se actualice la interfaz.
                     */
                    case "envio-mensaje":

                        String mensajeEnviado = datos.get("mensaje");
                        String fecha = datos.get("fecha");

                        Mensaje mensaje = new Mensaje(0, idCliente, idContacto, mensajeEnviado, fecha);

                        guardarMensaje(mensaje);

                        ObjectOutputStream contactoEscucha = conectados.get(String.valueOf(idContacto));

                        if (contactoEscucha != null){
                            contactoEscucha.writeObject("mensaje-recibido");

                            contactoEscucha.flush();
                        }

                        salida.flush();

                        break;
                }
            }
            /**
             * Si el cliente sale de la app o da error, se desconecta (se borra de la lista de conectados)
              */
        } catch (Exception e) {
            conectados.remove(String.valueOf(idCliente));
            System.out.println(e);
        }
    }

    public static int comprobarLogin(String usuario, String password){
        String bd = "whatsapp";
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "1234";
        String driver = "com.mysql.cj.jdbc.Driver";
        Connection connection;

        final String SELECT_QUERY = "SELECT * FROM usuarios where usuario = ? and password = ?";

        try{
            Class.forName(driver);
            connection = DriverManager.getConnection(url+bd, user, pass);

            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_QUERY);

            preparedStatement.setString(1, usuario);
            preparedStatement.setString(2, encriptarMd5(password));

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

    public static boolean registrarUsuario(String nombre, String estado, String usuario, String password){
        String bd = "whatsapp";
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "1234";
        String driver = "com.mysql.cj.jdbc.Driver";

        String insertQuery = "INSERT INTO usuarios (nombre, estado, usuario, password) VALUES (?, ?, ?, ?)";
        try{
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url+bd, user, pass);

            PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

            preparedStatement.setString(1, nombre);
            preparedStatement.setString(2, estado);
            preparedStatement.setString(3, usuario);
            preparedStatement.setString(4, encriptarMd5(password));

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

    public static boolean comprobarUsuario(String correo){
        String bd = "whatsapp";
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "1234";
        String driver = "com.mysql.cj.jdbc.Driver";
        Connection connection;

        final String SELECT_QUERY = "SELECT * FROM usuarios where usuario = ?";

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

    public static ArrayList<Usuario> obtenerContactos(Map<String, ObjectOutputStream> conectados){
        ArrayList<Usuario> usuarios = new ArrayList<>();

        String bd = "whatsapp";
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String pass = "1234";
        String driver = "com.mysql.cj.jdbc.Driver";
        boolean isOnline;
        Connection connection;

        final String SELECT_QUERY = "SELECT id, nombre, estado, usuario, password FROM usuarios";

        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url+bd, user, pass);

            PreparedStatement preparedStatement = connection.prepareStatement(SELECT_QUERY);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                if (conectados.get(String.valueOf(resultSet.getInt("id"))) != null){
                    isOnline = true;
                } else{
                    isOnline = false;
                }
                usuarios.add(new Usuario(resultSet.getInt("id"), resultSet.getString("nombre"),
                        resultSet.getString("estado"), resultSet.getString("usuario"),
                        resultSet.getString("password"), isOnline));
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
                "ORDER BY id ASC;";

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

    public static String encriptarMd5(String password){
        String output = "";
        StringBuilder stringBuilder = new StringBuilder();

        try{
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            byte[] digest = md5.digest(password.getBytes());

            for (byte bit : digest){
                stringBuilder.append(String.format("%02x", bit));
            }

            output = stringBuilder.toString();

            return output;

        }catch (Exception e){
            System.out.println(e);
            return null;
        }
    }
}