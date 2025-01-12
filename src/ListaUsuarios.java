import java.util.ArrayList;

public class ListaUsuarios {
    public ArrayList<Usuario> usuarios;

    public ListaUsuarios(ArrayList<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public ListaUsuarios() {
        this.usuarios = new ArrayList<>();
    }

    public ArrayList<Usuario> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(ArrayList<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
}
