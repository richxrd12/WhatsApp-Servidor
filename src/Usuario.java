public class Usuario{

    private int id;
    private String nombre;
    private String estado;
    private String correo;
    private String password;
    private boolean isOnline;

    public Usuario() {

    }

    public Usuario(int id, String nombre, String estado, String correo, String password, boolean isOnline) {
        this.id = id;
        this.nombre = nombre;
        this.estado = estado;
        this.correo = correo;
        this.password = password;
        this.isOnline = isOnline;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
