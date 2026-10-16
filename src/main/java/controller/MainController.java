package controller;

import dao.ProductoDAO;
import model.Producto;
import util.ValidadorProducto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MainController {

    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    @FXML private TextField txtId;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cbCategoriaForm;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cbCategoriaFiltro;
    @FXML private CheckBox chkExistencias;
    @FXML private Label lblContador;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ObservableList<Producto> listaProductos = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        tablaProductos.setItems(listaProductos);

        cbCategoriaForm.setItems(FXCollections.observableArrayList("Papelería", "Tecnología", "Accesorios"));
        cbCategoriaFiltro.setItems(FXCollections.observableArrayList("Todas", "Papelería", "Tecnología", "Accesorios"));
        cbCategoriaFiltro.setValue("Todas");

        tablaProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                txtId.setText(String.valueOf(newSel.getIdProducto()));
                txtCodigo.setText(newSel.getCodigo());
                txtNombre.setText(newSel.getNombre());
                cbCategoriaForm.setValue(newSel.getCategoria());
                txtPrecio.setText(newSel.getPrecio().toPlainString());
                txtStock.setText(String.valueOf(newSel.getStock()));
            }
        });

        recargarTabla();
    }

    @FXML
    public void recargarTabla() {
        try {
            String texto = txtBuscar.getText();
            String cat = cbCategoriaFiltro.getValue();
            boolean soloExistencias = chkExistencias.isSelected();

            List<Producto> resultados = productoDAO.buscarProductos(texto, cat, soloExistencias);
            listaProductos.setAll(resultados);

            lblContador.setText("Registros mostrados: " + resultados.size());

            if (resultados.isEmpty() && texto != null && !texto.trim().isEmpty()) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Aviso", "Sin coincidencias.");
            }
        } catch (Exception e) {
            // Imprime la pila completa del error en la consola
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión / SQL",
                    "Detalle: " + e.getMessage());
        }
    }

    @FXML
    public void limpiarFiltros() {
        txtBuscar.clear();
        cbCategoriaFiltro.setValue("Todas");
        chkExistencias.setSelected(false);
        recargarTabla();
    }

    @FXML
    public void limpiarFormulario() {
        txtId.clear();
        txtCodigo.clear();
        txtNombre.clear();
        cbCategoriaForm.setValue(null);
        txtPrecio.clear();
        txtStock.clear();
        tablaProductos.getSelectionModel().clearSelection();
    }

    @FXML
    public void guardarProducto() {
        try {
            ValidadorProducto.validar(
                    txtCodigo.getText(), txtNombre.getText(),
                    cbCategoriaForm.getValue(), txtPrecio.getText(), txtStock.getText()
            );

            String codigoNorm = txtCodigo.getText().trim().toUpperCase();
            String nombreNorm = txtNombre.getText().trim();

            if (productoDAO.existeCodigo(codigoNorm, null)) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación V9", "El código '" + codigoNorm + "' ya existe.");
                return;
            }

            Producto p = new Producto(
                    codigoNorm,
                    nombreNorm,
                    cbCategoriaForm.getValue(),
                    new BigDecimal(txtPrecio.getText().trim()),
                    Integer.parseInt(txtStock.getText().trim())
            );

            if (productoDAO.insertar(p)) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto guardado correctamente.");
                limpiarFormulario();
                recargarTabla();
            }
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error de Validación", e.getMessage());
        } catch (SQLException e) {
            System.err.println("SQLState: " + e.getSQLState() + " - " + e.getMessage());
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "Error al guardar en PostgreSQL.");
        }
    }

    @FXML
    public void actualizarProducto() {
        if (txtId.getText().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación V10", "Debe seleccionar un producto de la tabla para actualizar.");
            return;
        }

        try {
            int id = Integer.parseInt(txtId.getText());

            ValidadorProducto.validar(
                    txtCodigo.getText(), txtNombre.getText(),
                    cbCategoriaForm.getValue(), txtPrecio.getText(), txtStock.getText()
            );

            String codigoNorm = txtCodigo.getText().trim().toUpperCase();
            String nombreNorm = txtNombre.getText().trim();

            if (productoDAO.existeCodigo(codigoNorm, id)) {
                mostrarAlerta(Alert.AlertType.WARNING, "Validación V9", "El código '" + codigoNorm + "' ya lo utiliza otro producto.");
                return;
            }

            Producto p = new Producto(
                    id,
                    codigoNorm,
                    nombreNorm,
                    cbCategoriaForm.getValue(),
                    new BigDecimal(txtPrecio.getText().trim()),
                    Integer.parseInt(txtStock.getText().trim())
            );

            boolean exito = productoDAO.actualizar(p);

            if (!exito) {
                mostrarAlerta(Alert.AlertType.WARNING, "Error E3", "El producto ya no existe en la base de datos.");
                recargarTabla();
                limpiarFormulario();
            } else {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente.");
                limpiarFormulario();
                recargarTabla();
            }
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Error de Validación", e.getMessage());
        } catch (SQLException e) {
            System.err.println("SQLState: " + e.getSQLState() + " - " + e.getMessage());
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "Error al actualizar en PostgreSQL.");
        }
    }

    @FXML
    public void eliminarProducto() {
        if (txtId.getText().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación V10", "Debe seleccionar un producto de la tabla para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar el producto seleccionado?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                int id = Integer.parseInt(txtId.getText());
                boolean exito = productoDAO.eliminar(id);

                if (!exito) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Error E3", "El producto ya no existe en la base de datos.");
                } else {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Producto eliminado correctamente.");
                }
                limpiarFormulario();
                recargarTabla();
            } catch (SQLException e) {
                System.err.println("SQLState: " + e.getSQLState() + " - " + e.getMessage());
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "No se pudo eliminar el registro.");
            }
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}