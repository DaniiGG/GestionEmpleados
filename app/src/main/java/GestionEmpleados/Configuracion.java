package GestionEmpleados;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Configuracion {
    private Properties propiedades;

    public Configuracion(String configproperties) {
        propiedades = new Properties();
        cargarConfiguracion();
    }

    private void cargarConfiguracion() {
        // Cargar propiedades desde el archivo en el classpath
        try (InputStream input = getClass().getResourceAsStream("config.properties")) { // Asegúrate de que la ruta sea correcta
            if (input == null) {
                System.out.println("Error: no se pudo encontrar el archivo de configuración.");
                return;
            }
            propiedades.load(input);
        } catch (IOException e) {
            System.out.println("Error al cargar el archivo de configuración: " + e.getMessage());
        }
    }

    public String getFicheroBinario() {
        return propiedades.getProperty("fichero_binario");
    }

    public String getFicheroXML() {
        return propiedades.getProperty("fichero_xml");
    }

    public String getFicheroJSON() {
        return propiedades.getProperty("fichero_json");
    }

    public int getIdEmpleado() {
        return Integer.parseInt(propiedades.getProperty("id_empleado", "0")); // Valor por defecto en caso de que no se encuentre
    }

    public void setIdEmpleado(int id) {
        propiedades.setProperty("id_empleado", String.valueOf(id));
        guardarConfiguracion();
    }

    private void guardarConfiguracion() {
        // Guardar propiedades en el archivo de configuración en el classpath
        try (OutputStream output = getClass().getResourceAsStream("/config.properties") != null ? 
                new FileOutputStream("config.properties") : new FileOutputStream("config.properties")) {
            propiedades.store(output, "Configuración de la aplicación");
        } catch (IOException e) {
            System.out.println("Error al guardar el archivo de configuración: " + e.getMessage());
        }
    }
}
