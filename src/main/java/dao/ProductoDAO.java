package dao;

import config.ConexionBD;
import model.Producto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> buscarProductos(String texto, String categoria, boolean soloExistencias) throws SQLException {
        List<Producto> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT id_producto, codigo, nombre, categoria, precio, stock FROM producto WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        if (texto != null && !texto.trim().isEmpty()) {
            sql.append(" AND (LOWER(nombre) LIKE LOWER(?) OR LOWER(codigo) LIKE LOWER(?))");
            String term = "%" + texto.trim() + "%";
            parametros.add(term);
            parametros.add(term);
        }

        if (categoria != null && !categoria.equalsIgnoreCase("Todas") && !categoria.trim().isEmpty()) {
            sql.append(" AND categoria = ?");
            parametros.add(categoria);
        }

        if (soloExistencias) {
            sql.append(" AND stock > 0");
        }

        sql.append(" ORDER BY codigo ASC");

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto(
                            rs.getInt("id_producto"),
                            rs.getString("codigo"),
                            rs.getString("nombre"),
                            rs.getString("categoria"),
                            rs.getBigDecimal("precio"),
                            rs.getInt("stock")
                    );
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    public boolean insertar(Producto p) throws SQLException {
        String sql = "INSERT INTO producto (codigo, nombre, categoria, precio, stock) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getCodigo());
            stmt.setString(2, p.getNombre());
            stmt.setString(3, p.getCategoria());
            stmt.setBigDecimal(4, p.getPrecio());
            stmt.setInt(5, p.getStock());

            int filas = stmt.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        p.setIdProducto(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean actualizar(Producto p) throws SQLException {
        String sql = "UPDATE producto SET codigo=?, nombre=?, categoria=?, precio=?, stock=? WHERE id_producto=?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getCodigo());
            stmt.setString(2, p.getNombre());
            stmt.setString(3, p.getCategoria());
            stmt.setBigDecimal(4, p.getPrecio());
            stmt.setInt(5, p.getStock());
            stmt.setInt(6, p.getIdProducto());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idProducto) throws SQLException {
        String sql = "DELETE FROM producto WHERE id_producto=?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProducto);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean existeCodigo(String codigo, Integer idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM producto WHERE UPPER(codigo) = UPPER(?)";
        if (idExcluir != null) {
            sql += " AND id_producto != ?";
        }
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, codigo.trim());
            if (idExcluir != null) {
                stmt.setInt(2, idExcluir);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}