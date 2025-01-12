public class Mensaje {

    private int id;
    private int idRemitente;
    private int idDestinatario;
    private String mensaje;
    private String fecha;

    public Mensaje() {

    }

    public Mensaje(int id, int idRemitente, int idDestinatario, String mensaje, String fecha) {
        this.id = id;
        this.idRemitente = idRemitente;
        this.idDestinatario = idDestinatario;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRemitente() {
        return idRemitente;
    }

    public void setRemitente(int remitente) {
        this.idRemitente = remitente;
    }

    public int getDestinatario() {
        return idDestinatario;
    }

    public void setDestinatario(int destinatario) {
        this.idDestinatario = destinatario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

}
