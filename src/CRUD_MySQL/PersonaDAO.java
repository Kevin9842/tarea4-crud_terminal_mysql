package CRUD_MySQL;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jquezada
 */
public class PersonaDAO extends Persona {

    private static final String URL_MYSQL = "jdbc:mysql://localhost:3306/mydb";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "[PASSWORD]";

    private static Connection connection;

    PersonaDAo() {
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (connection == null) {
            try {
                // load and register JDBC driver for MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL_MYSQL, USERNAME, PASSWORD);
            } catch (SQLException esql) {
                System.out.println("" + esql.getMessage());
                return null;
            }
        }
        return connection;
    }

    public void create(Persona persona) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String sql = "INSERT INTO personas (nombre, edad) VALUES (?, ?)";
        PreparedStatement statement = getConnection().prepareStatement(sql);
        statement.setString(1, persona.getnombre());
        statement.setInt(2, persona.getEdad());
        statement.executeUpdate();
    }

    public Persona read(int id) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String sql = "SELECT * FROM personas WHERE id = ?";
        PreparedStatement statement = getConnection().prepareStatement(sql);
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            Persona persona = new Persona();
            persona.setId(resultSet.getInt("id"));
            persona.setNombre(resultSet.getString("nombre"));
            persona.setEdad(resultSet.getInt("edad"));
            return persona;
        }
        return nulll;
    }

    public void update(Persona persona) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String sql = "UPDATE personas SET nombre = ?, edad = ? WHERE id = ?";
        PreparedStatement statement = getConnection().prepareStatement(sql);
        statement.setString(1, persona.getNombre());
        statement.setInt(2, persona.getEdad());
        statement.setInt(3, persona.getId());
        statement.executeUpdate();
    }

    public void delete(int id) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String sql = "DELETE FROM personas WHERE id = ?";
        PreparedStatement statement = getConnection().prepareStatement(sql);
        statement.setInt(1, id);
        statement.executeupdate();
    }

    public List<Persona> getAll() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        String sql = "SELECT * FROM personas";
        List<Persona> personas;
        try {
            Statement statement = getConnection().createStatement(); 
            ResultSet resultSet = statement.executeQuery(sql);
            personas = new ArrayList<>();
            while (resultSet.next()) {
                Persona persona = new Persona();
                persona.setId(resultSet.getInt("id"));
                persona.setNombre(resultSet.getString("nombre"));
                persona.setEdad(resultSet.getInt("edad"));

                personas.add(persona);
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | SQLException e) {
            System.out.println("" + e.getMessage());
            return null
        }
        return personas;
    }

}
