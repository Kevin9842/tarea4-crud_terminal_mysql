package CRUD_MySQL;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Data Access Object (DAO) para la entidad Persona.
 *
 * @author jquezada (corregido - auditoría QA)
 *
 * CORRECCIONES APLICADAS:
 *  - SINT-01: Constructor 'PersonaDAo()' → 'PersonaDAO()' (nombre incorrecto)
 *  - SINT-04: 'return nulll' → 'return null' (typo doble 'l')
 *  - SINT-05: 'executeupdate()' → 'executeUpdate()' (capitalización incorrecta)
 *  - SINT-06: 'persona.getnombre()' → 'persona.getNombre()' (capitalización incorrecta)
 *  - SINT-07: 'return null' sin punto y coma → añadido ';'
 *  - CRUD-01: 'extends Persona' eliminado (viola SRP y LSP de SOLID)
 *  - CRUD-02: getAll() retornaba null en catch → retorna Collections.emptyList()
 *  - CONN-01: Singleton no thread-safe → conexión por operación con try-with-resources
 *  - CONN-02: PreparedStatement/ResultSet no cerrados → try-with-resources en todos los métodos
 *  - CONN-03: Credenciales hardcodeadas → cargadas desde db.properties externo
 *  - VALI-03: Excepciones verificadas propagadas → encapsuladas como RuntimeException
 *  - LOGI-03: delete() ignoraba filas afectadas → retorna boolean
 */
public class PersonaDAO {   // ✅ CORREGIDO (CRUD-01): Sin 'extends Persona'

    private final String url;
    private final String username;
    private final String password;

    // ✅ CORREGIDO (SINT-01): Constructor con nombre exacto de la clase
    public PersonaDAO() {
        Properties props = loadDbProperties();
        this.url      = props.getProperty("db.url",      "jdbc:mysql://localhost:3306/mydb");
        this.username = props.getProperty("db.user",     "root");
        this.password = props.getProperty("db.password", "");
    }

    /**
     * Carga credenciales desde 'db.properties' externo.
     * Si no existe, usa valores por defecto definidos en el constructor.
     * CONN-03: Credenciales fuera del código fuente.
     */
    private Properties loadDbProperties() {
        Properties props = new Properties();
        try (InputStream is = new FileInputStream("db.properties")) {
            props.load(is);
        } catch (IOException e) {
            System.err.println("[WARN] db.properties no encontrado. Usando configuración por defecto.");
        }
        return props;
    }

    /**
     * Abre una conexión independiente a la base de datos.
     * CONN-01: Elimina singleton con estado compartido; cada operación gestiona su propia conexión.
     */
    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Inserta una nueva Persona en la base de datos.
     *
     * @param persona Objeto Persona con nombre y edad válidos (no null).
     * @throws RuntimeException si ocurre un error SQL.
     */
    public void create(Persona persona) {
        String sql = "INSERT INTO personas (nombre, edad) VALUES (?, ?)";
        // ✅ CORREGIDO (CONN-02): try-with-resources cierra automáticamente Connection y Statement
        try (Connection conn = openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, persona.getNombre()); // ✅ CORREGIDO (SINT-06): getNombre()
            stmt.setInt(2, persona.getEdad());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error al crear persona: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera una Persona por su ID.
     *
     * @param id Identificador único de la persona.
     * @return Objeto Persona o null si no existe el ID.
     * @throws RuntimeException si ocurre un error SQL.
     */
    public Persona read(int id) {
        String sql = "SELECT id, nombre, edad FROM personas WHERE id = ?";
        try (Connection conn = openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            // ✅ CORREGIDO (CONN-02): ResultSet también cerrado con try-with-resources
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Persona persona = new Persona();
                    persona.setId(rs.getInt("id"));
                    persona.setNombre(rs.getString("nombre"));
                    persona.setEdad(rs.getInt("edad"));
                    return persona;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al leer persona con ID " + id + ": " + e.getMessage(), e);
        }
        return null; // ✅ CORREGIDO (SINT-04): 'null' correcto, no 'nulll'
    }

    /**
     * Actualiza los datos de una Persona existente.
     *
     * @param persona Objeto Persona con ID válido, nombre y edad actualizados.
     * @return true si se actualizó al menos un registro.
     * @throws RuntimeException si ocurre un error SQL.
     */
    public boolean update(Persona persona) {
        String sql = "UPDATE personas SET nombre = ?, edad = ? WHERE id = ?";
        try (Connection conn = openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, persona.getNombre());
            stmt.setInt(2, persona.getEdad());
            stmt.setInt(3, persona.getId());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar persona: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina una Persona por su ID.
     *
     * @param id Identificador único de la persona a eliminar.
     * @return true si se eliminó el registro, false si el ID no existía.
     * @throws RuntimeException si ocurre un error SQL.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM personas WHERE id = ?";
        try (Connection conn = openConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0; // ✅ CORREGIDO (SINT-05): executeUpdate()

        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar persona con ID " + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Recupera todas las Personas de la base de datos, ordenadas por ID.
     *
     * @return Lista de Personas (vacía si no hay registros — nunca null). (CRUD-02)
     */
    public List<Persona> getAll() {
        String sql = "SELECT id, nombre, edad FROM personas ORDER BY id";
        List<Persona> personas = new ArrayList<>();
        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Persona persona = new Persona();
                persona.setId(rs.getInt("id"));
                persona.setNombre(rs.getString("nombre"));
                persona.setEdad(rs.getInt("edad"));
                personas.add(persona);
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] Error al obtener lista de personas: " + e.getMessage());
            return Collections.emptyList(); // ✅ CORREGIDO (CRUD-02): nunca retorna null
        }
        return personas;
    }
}
