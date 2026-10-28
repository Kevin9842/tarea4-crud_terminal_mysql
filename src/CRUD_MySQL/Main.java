package CRUD_MySQL;

import java.util.List;
import java.util.Scanner;

/**
 * Punto de entrada de la aplicación CRUD de Personas.
 * Capa de Presentación: gestiona la interacción con el usuario por consola.
 *
 * @author jquezada (corregido - auditoría QA)
 *
 * CORRECCIONES APLICADAS:
 *  - LOGI-01: while(opcion != 5) → while(opcion != 6) alineado con opción "Salir" del menú
 *  - LOGI-02: actualizarPersona() corregido — elimina llamada recursiva a leerPersona()
 *             y consume correctamente el '\n' residual del stream
 *  - LOGI-03: eliminarPersona() ahora verifica el resultado del delete
 *  - VALI-01: Validación de nombre (no vacío, máx 50 chars) y edad (1-150)
 *  - VALI-02: leerOpcion() maneja InputMismatchException con bucle de reintento
 *  - VALI-03: main() y métodos sin 'throws' — excepciones gestionadas internamente
 *  - VALI-04: Scanner estático único compartido — evita múltiples instancias
 */
public class Main {

    // ✅ CORREGIDO (VALI-04): Scanner estático compartido — una sola instancia
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final PersonaDAO dao = new PersonaDAO();

    // ✅ CORREGIDO (VALI-03): main() sin 'throws' — excepciones gestionadas en cada capa
    public static void main(String[] args) {
        int opcion;
        do {
            mostrarMenu();
            opcion = leerOpcion();
            procesarOpcion(opcion);
        } while (opcion != 6); // ✅ CORREGIDO (LOGI-01): 6 = "Salir", no 5
        SCANNER.close();
    }

    private static void mostrarMenu() {
        System.out.println("\n══════════════════════════");
        System.out.println("       CRUD Personas      ");
        System.out.println("══════════════════════════");
        System.out.println("  1. Crear persona");
        System.out.println("  2. Listar personas");
        System.out.println("  3. Buscar persona");
        System.out.println("  4. Actualizar persona");
        System.out.println("  5. Eliminar persona");
        System.out.println("  6. Salir");
        System.out.println("══════════════════════════");
    }

    // ✅ CORREGIDO (VALI-02): Maneja InputMismatchException — no crashea con texto
    private static int leerOpcion() {
        System.out.print("Introduzca una opción [1-6]: ");
        while (!SCANNER.hasNextInt()) {
            System.out.println("  ⚠ Entrada inválida. Ingrese un número del 1 al 6.");
            SCANNER.next(); // descartar token inválido
            System.out.print("Introduzca una opción [1-6]: ");
        }
        return SCANNER.nextInt();
    }

    // ✅ CORREGIDO (VALI-03): sin 'throws' en la firma
    private static void procesarOpcion(int opcion) {
        switch (opcion) {
            case 1: crearPersona();      break;
            case 2: listarPersonas();    break;
            case 3: buscarPersona();     break;
            case 4: actualizarPersona(); break;
            case 5: eliminarPersona();   break;
            case 6: System.out.println("\n  Cerrando sesión. ¡Hasta luego!"); break;
            default: System.out.println("  ⚠ Opción no válida. Elija entre 1 y 6.");
        }
    }

    private static void crearPersona() {
        System.out.println("\n─── Crear Nueva Persona ───");
        SCANNER.nextLine(); // consumir '\n' residual del nextInt() de leerOpcion()
        // ✅ CORREGIDO (VALI-01): validación de nombre y edad
        String nombre = leerNombre();
        int    edad   = leerEdad();

        Persona persona = new Persona();
        persona.setNombre(nombre);
        persona.setEdad(edad);

        try {
            dao.create(persona);
            System.out.println("  ✔ Persona '" + nombre + "' creada correctamente.");
        } catch (RuntimeException e) {
            System.err.println("  ✘ Error al crear persona: " + e.getMessage());
        }
    }

    private static void buscarPersona() {
        System.out.println("\n─── Buscar Persona ───");
        int id = leerId();
        try {
            Persona persona = dao.read(id);
            if (persona != null) {
                System.out.println("  Resultado: " + persona);
            } else {
                System.out.println("  ⚠ No se encontró persona con ID " + id + ".");
            }
        } catch (RuntimeException e) {
            System.err.println("  ✘ Error al buscar persona: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO (LOGI-02): flujo correcto — no llama a leerPersona() ni consume stream de forma incorrecta
    private static void actualizarPersona() {
        System.out.println("\n─── Actualizar Persona ───");
        int id = leerId();

        try {
            // Verificar existencia y mostrar datos actuales antes de pedir nuevos valores
            Persona actual = dao.read(id);
            if (actual == null) {
                System.out.println("  ⚠ No se encontró persona con ID " + id + ". Actualización cancelada.");
                return;
            }
            System.out.println("  Datos actuales → " + actual);

            SCANNER.nextLine(); // consumir '\n' residual del nextInt() de leerId()
            // ✅ CORREGIDO (VALI-01): validación de nombre y edad
            String nombre = leerNombre();
            int    edad   = leerEdad();

            Persona persona = new Persona();
            persona.setId(id);
            persona.setNombre(nombre);
            persona.setEdad(edad);

            boolean actualizado = dao.update(persona);
            System.out.println(actualizado
                ? "  ✔ Persona actualizada correctamente."
                : "  ⚠ No se pudo actualizar (registro no encontrado).");
        } catch (RuntimeException e) {
            System.err.println("  ✘ Error al actualizar persona: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO (LOGI-03): verifica si realmente se eliminó algo
    private static void eliminarPersona() {
        System.out.println("\n─── Eliminar Persona ───");
        int id = leerId();
        try {
            boolean eliminado = dao.delete(id);
            System.out.println(eliminado
                ? "  ✔ Persona con ID " + id + " eliminada correctamente."
                : "  ⚠ No se encontró persona con ID " + id + ". Nada fue eliminado.");
        } catch (RuntimeException e) {
            System.err.println("  ✘ Error al eliminar persona: " + e.getMessage());
        }
    }

    private static void listarPersonas() {
        System.out.println("\n─── Lista de Personas Registradas ───");
        try {
            List<Persona> personas = dao.getAll();
            if (personas.isEmpty()) {
                System.out.println("  (No hay personas registradas en el sistema)");
            } else {
                System.out.printf("  %-6s %-22s %s%n", "ID", "Nombre", "Edad");
                System.out.println("  " + "─".repeat(36));
                personas.forEach(p -> System.out.println("  " + p));
            }
        } catch (RuntimeException e) {
            System.err.println("  ✘ Error al listar personas: " + e.getMessage());
        }
    }

    // ─── Métodos auxiliares de lectura con validación (VALI-01) ─────────────

    /** Lee y valida un ID entero positivo. */
    private static int leerId() {
        System.out.print("  ID de la persona: ");
        while (!SCANNER.hasNextInt()) {
            System.out.println("  ⚠ ID inválido. Ingrese un número entero positivo.");
            SCANNER.next();
            System.out.print("  ID de la persona: ");
        }
        int id = SCANNER.nextInt();
        if (id <= 0) {
            System.out.println("  ⚠ El ID debe ser mayor que 0.");
            return leerId();
        }
        return id;
    }

    /** Lee y valida un nombre no vacío de máximo 50 caracteres. */
    private static String leerNombre() {
        String nombre;
        do {
            System.out.print("  Nombre (máx. 50 chars): ");
            nombre = SCANNER.nextLine().trim();
            if (nombre.isEmpty())       System.out.println("  ⚠ El nombre no puede estar vacío.");
            else if (nombre.length() > 50) System.out.println("  ⚠ El nombre no puede exceder 50 caracteres.");
        } while (nombre.isEmpty() || nombre.length() > 50);
        return nombre;
    }

    /** Lee y valida una edad entre 1 y 150. */
    private static int leerEdad() {
        int edad;
        do {
            System.out.print("  Edad (1-150): ");
            while (!SCANNER.hasNextInt()) {
                System.out.println("  ⚠ Edad inválida. Ingrese un número entero.");
                SCANNER.next();
                System.out.print("  Edad (1-150): ");
            }
            edad = SCANNER.nextInt();
            if (edad <= 0 || edad > 150) System.out.println("  ⚠ La edad debe estar entre 1 y 150.");
        } while (edad <= 0 || edad > 150);
        return edad;
    }
}
