package GestionEmpleados;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;

public class ControEj3 implements Initializable {
    
    @FXML
    private Button importarJSON;

    @FXML
    private Button importarXML;

    @FXML
    private Button borrar;

    @FXML
    private Button actualizar;

    @FXML
    private Button exportarJSON;

    @FXML
    private Button exportarXML;

    @FXML
    private HBox hbox;

    @FXML
    private Button insertar;

    @FXML
    private TableView<Empleado> tableview;

    @FXML
    private TextField tf1, tf2, tf3, tf4;

    // ObservableList para almacenar los empleados
    private ObservableList<Empleado> lista;

    // Instancia de configuración
    private Configuracion configuracion;

    // Variable para el ID autoincremental
    private int nextId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configuracion = new Configuracion("config.properties");
        nextId = configuracion.getIdEmpleado();

        // Inicializar la lista de empleados
        lista = FXCollections.observableArrayList();
        tableview.setItems(lista);

        // Definir columnas de la tabla
        definirColumnas();

        // Listener para la selección en la tabla
        tableview.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                tf1.setText(newValue.getNombre());
                tf2.setText(newValue.getApellidos());
                tf3.setText(newValue.getDepartamento());
                tf4.setText(String.valueOf(newValue.getSueldo()));
            }
        });
    }

    private void definirColumnas() {
        TableColumn<Empleado, Integer> c1 = new TableColumn<>("Id");
        c1.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Empleado, String> c2 = new TableColumn<>("Nombre");
        c2.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        TableColumn<Empleado, String> c3 = new TableColumn<>("Apellidos");
        c3.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        TableColumn<Empleado, String> c4 = new TableColumn<>("Departamento");
        c4.setCellValueFactory(new PropertyValueFactory<>("departamento"));
        TableColumn<Empleado, Double> c5 = new TableColumn<>("Sueldo");
        c5.setCellValueFactory(new PropertyValueFactory<>("sueldo"));

        tableview.getColumns().addAll(c1, c2, c3, c4, c5);
    }

    @FXML
    void insertar(ActionEvent event) {
        String nombre = tf1.getText();
        String apellidos = tf2.getText();
        String departamento = tf3.getText();
        String sueldoStr = tf4.getText();

        if (!validarDatos(nombre, apellidos, departamento, sueldoStr)) {
            return; // Validación fallida
        }

        double sueldo = Double.parseDouble(sueldoStr);
        Empleado nuevoEmpleado = new Empleado(nextId++, nombre, apellidos, departamento, sueldo);
        lista.add(nuevoEmpleado);
        configuracion.setIdEmpleado(nextId); // Actualizar el ID en la configuración

        // Limpiar los TextFields
        tf1.clear();
        tf2.clear();
        tf3.clear();
        tf4.clear();
        tableview.getSelectionModel().clearSelection();
    }

    @FXML
    void actualizar(ActionEvent event) {
        Empleado empleadoSeleccionado = tableview.getSelectionModel().getSelectedItem();

        if (empleadoSeleccionado != null) {
            String nombre = tf1.getText();
            String apellidos = tf2.getText();
            String departamento = tf3.getText();
            String sueldoStr = tf4.getText();

            if (!validarDatos(nombre, apellidos, departamento, sueldoStr)) {
                return; // Validación fallida
            }

            double sueldo = Double.parseDouble(sueldoStr);
            empleadoSeleccionado.setNombre(nombre);
            empleadoSeleccionado.setApellidos(apellidos);
            empleadoSeleccionado.setDepartamento(departamento);
            empleadoSeleccionado.setSueldo(sueldo);
            tableview.refresh(); // Actualizar la tabla

            // Limpiar los TextFields
            tf1.clear();
            tf2.clear();
            tf3.clear();
            tf4.clear();
            tableview.getSelectionModel().clearSelection();
        } else {
            mostrarAlerta("Error", "Por favor, selecciona un empleado para actualizar.");
        }
    }

    @FXML
    void borrar(ActionEvent event) {
        Empleado empleadoSeleccionado = tableview.getSelectionModel().getSelectedItem();
        if (empleadoSeleccionado != null) {
            boolean confirmacion = mostrarConfirmacion("Confirmación de Borrado", "¿Estás seguro de que deseas eliminar este empleado?");
            if (confirmacion) {
                lista.remove(empleadoSeleccionado);
                tableview.setItems(lista); // Actualizar la tabla
            }
        } else {
            mostrarAlerta("Error", "Por favor, selecciona un empleado para borrar.");
        }
    }

    

   @FXML
    void exportarJSON(ActionEvent event) {
        Configuracion configuracion = new Configuracion("config.properties");
        String ficheroJSON = configuracion.getFicheroJSON(); // Obtener el nombre del archivo JSON desde la configuración

        // Mostrar el diálogo de selección de carpeta
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta para guardar JSON");
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File directory = directoryChooser.showDialog(stage);

        if (directory != null) {
            // Generar el archivo JSON en la carpeta seleccionada con el nombre por defecto
            File file = new File(directory, ficheroJSON); // Usar la ruta obtenida de la configuración

            try (FileWriter writer = new FileWriter(file)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(lista); // Convertir la lista a JSON
                writer.write(json);
                System.out.println("Datos exportados a JSON en: " + file.getAbsolutePath());
            } catch (IOException e) {
                mostrarAlerta("Error al exportar", "No se pudo guardar el archivo JSON: " + e.getMessage());
            }
        } else {
            System.out.println("Guardado cancelado o ubicación no seleccionada.");
        }
    }


    @FXML
    void exportarXML(ActionEvent event) {
        Configuracion configuracion = new Configuracion("config.properties");
        String ficheroXML = configuracion.getFicheroXML(); // Obtener la ruta del archivo XML desde la configuración

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Seleccionar carpeta para guardar XML");

        // Mostrar el diálogo de selección de carpeta
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File directory = directoryChooser.showDialog(stage);

        if (directory != null) {
            // Usar la ruta por defecto si no se ha especificado
            File file = new File(directory, ficheroXML); // Se guarda con el nombre por defecto

            // Comenzar la construcción del XML
            StringBuilder xmlBuilder = new StringBuilder();
            xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xmlBuilder.append("<empleados>\n");

            // Iterar sobre la lista de empleados y agregar sus datos al XML
            for (Empleado empleado : lista) { // Asegúrate de que 'lista' es tu lista de empleados
                xmlBuilder.append("\t<empleado>\n");
                xmlBuilder.append("\t\t<id>").append(empleado.getId()).append("</id>\n");
                xmlBuilder.append("\t\t<nombre>").append(empleado.getNombre()).append("</nombre>\n");
                xmlBuilder.append("\t\t<apellido>").append(empleado.getApellidos()).append("</apellido>\n");
                xmlBuilder.append("\t\t<departamento>").append(empleado.getDepartamento()).append("</departamento>\n");
                xmlBuilder.append("\t\t<sueldo>").append(empleado.getSueldo()).append("</sueldo>\n");
                xmlBuilder.append("\t</empleado>\n");
            }

            xmlBuilder.append("</empleados>");

            // Guardar el contenido XML en el archivo
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(xmlBuilder.toString());
                System.out.println("Datos exportados a XML en: " + file.getAbsolutePath());
            } catch (IOException e) {
                mostrarAlerta("Error al exportar", "Error al exportar archivo XML: " + e.getMessage());
            }
        } else {
            System.out.println("Guardado cancelado o ubicación no seleccionada.");
        }
    }

    
    @FXML
    public void importarArchivo(ActionEvent event) {
        // Tu lógica para importar archivo
        System.out.println("Archivo importado");
    }
        @FXML
    public void exportarArchivo(ActionEvent event) {
        // Tu lógica para importar archivo
        System.out.println("Archivo importado");
    }
    
     @FXML
    void importarJSON(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo JSON");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos JSON", "*.json"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try (InputStream inputStream = new FileInputStream(file)) {
                // Leer el archivo JSON
                byte[] jsonData = inputStream.readAllBytes();
                String jsonString = new String(jsonData);

                // Convertir el JSON en una lista de empleados
                Gson gson = new Gson();
                Empleado[] empleados = gson.fromJson(jsonString, Empleado[].class);

                // Agregar empleados a la lista observable
                lista.addAll(empleados);

                System.out.println("Datos importados desde JSON: " + file.getAbsolutePath());
            } catch (IOException e) {
                mostrarAlerta("Error al importar JSON", "No se pudo leer el archivo JSON: " + e.getMessage());
            } catch (Exception e) {
                mostrarAlerta("Error al procesar JSON", "El archivo no tiene un formato válido: " + e.getMessage());
            }
        } else {
            System.out.println("Importación cancelada o archivo no seleccionado.");
        }
    }

    @FXML
    void importarXML(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo XML");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos XML", "*.xml"));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                // Leer el contenido del archivo XML
                StringBuilder xmlContent = new StringBuilder();
                try (Scanner scanner = new Scanner(file)) {
                    while (scanner.hasNextLine()) {
                        xmlContent.append(scanner.nextLine()).append("\n");
                    }
                }

                // Procesar el XML manualmente
                String xmlString = xmlContent.toString();
                List<Empleado> empleadosImportados = new ArrayList<>();

                // Dividir el XML en bloques de <empleado>
                String[] empleados = xmlString.split("<empleado>");
                for (String empleadoStr : empleados) {
                    if (empleadoStr.contains("</empleado>")) {
                        // Extraer campos manualmente usando tags
                        int id = Integer.parseInt(extraerValor(empleadoStr, "id"));
                        String nombre = extraerValor(empleadoStr, "nombre");
                        String apellidos = extraerValor(empleadoStr, "apellido");
                        String departamento = extraerValor(empleadoStr, "departamento");
                        double sueldo = Double.parseDouble(extraerValor(empleadoStr, "sueldo"));

                        // Crear un objeto Empleado y agregarlo a la lista temporal
                        empleadosImportados.add(new Empleado(id, nombre, apellidos, departamento, sueldo));
                    }
                }

                // Agregar todos los empleados a la lista observable
                lista.addAll(empleadosImportados);
                System.out.println("Datos importados desde XML: " + file.getAbsolutePath());

            } catch (Exception e) {
                mostrarAlerta("Error al importar XML", "No se pudo procesar el archivo XML: " + e.getMessage());
            }
        } else {
            System.out.println("Importación cancelada o archivo no seleccionado.");
        }
    }

    // Método auxiliar para extraer valores entre etiquetas XML
    private String extraerValor(String xml, String etiqueta) {
        String inicioTag = "<" + etiqueta + ">";
        String finTag = "</" + etiqueta + ">";
        int inicio = xml.indexOf(inicioTag) + inicioTag.length();
        int fin = xml.indexOf(finTag);
        if (inicio < 0 || fin < 0 || inicio >= fin) {
            throw new IllegalArgumentException("Etiqueta <" + etiqueta + "> no encontrada o mal formada.");
        }
        return xml.substring(inicio, fin).trim();
    }
    
    
    
    private boolean validarDatos(String nombre, String apellidos, String departamento, String sueldoStr) {
        if (nombre.trim().isEmpty() || apellidos.trim().isEmpty() || departamento.trim().isEmpty() || sueldoStr.trim().isEmpty()) {
            mostrarAlerta("Error de validación", "Todos los campos deben estar llenos.");
            return false;
        }

        if (nombre.length() > 30) {
            mostrarAlerta("Error de validación", "El nombre no puede tener más de 30 caracteres.");
            return false;
        }
        if (apellidos.length() > 60) {
            mostrarAlerta("Error de validación", "Los apellidos no pueden tener más de 60 caracteres.");
            return false;
        }
        if (departamento.length() > 30) {
            mostrarAlerta("Error de validación", "El departamento no puede tener más de 30 caracteres.");
            return false;
        }

        try {
            double sueldo = Double.parseDouble(sueldoStr);
            if (sueldo < 0 || sueldo > 99999.99) {
                mostrarAlerta("Error de validación", "El sueldo debe estar entre 0 y 99,999.99.");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de validación", "El sueldo debe ser un número válido.");
            return false;
        }

        return true; // Todos los datos son válidos
    }

    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
