package CRUD_MySQL;

/**
 * Entidad de dominio que representa a una Persona.
 * Aplica convenciones JavaBeans estándar.
 *
 * @author jquezada (corregido - auditoría QA)
 *
 * CORRECCIONES APLICADAS:
 *  - SINT-02: getId() retornaba 'id2' (variable inexistente) → corregido a 'id'
 *  - SINT-03: getEdad() retornaba 'Edad' (case erróneo) → corregido a 'edad'
 */
public class Persona {

    private int id;
    private String nombre;
    private int edad;

    // ✅ CORREGIDO (SINT-02): retorna 'id', no 'id2'
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

    // ✅ CORREGIDO (SINT-03): retorna 'edad', no 'Edad'
    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    @Override
    public String toString() {
        return String.format("ID: %-4d | Nombre: %-20s | Edad: %d", id, nombre, edad);
    }
}
