package util;

import java.math.BigDecimal;

public class ValidadorProducto {

    public static void validar(String codigo, String nombre, String categoria, String precioTexto, String stockTexto) throws IllegalArgumentException {
        if (codigo == null || codigo.trim().isEmpty() || codigo.trim().length() > 10) {
            throw new IllegalArgumentException("El código es obligatorio y debe tener entre 1 y 10 caracteres.");
        }

        if (nombre == null || nombre.trim().length() < 3 || nombre.trim().length() > 80) {
            throw new IllegalArgumentException("El nombre es obligatorio y debe tener entre 3 y 80 caracteres.");
        }

        if (categoria == null || categoria.trim().isEmpty() ||
                (!categoria.equals("Papelería") && !categoria.equals("Tecnología") && !categoria.equals("Accesorios"))) {
            throw new IllegalArgumentException("Debe seleccionar una categoría válida (Papelería, Tecnología o Accesorios).");
        }

        if (precioTexto == null || precioTexto.trim().isEmpty()) {
            throw new IllegalArgumentException("El precio es obligatorio.");
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(precioTexto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El precio debe ser un valor numérico válido (ej. 15.50).");
        }

        if (precio.scale() > 2) {
            throw new IllegalArgumentException("El precio no puede tener más de 2 decimales.");
        }

        if (precio.compareTo(new BigDecimal("0.01")) < 0 || precio.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException("El precio debe estar entre 0.01 y 99,999,999.99.");
        }

        if (stockTexto == null || stockTexto.trim().isEmpty()) {
            throw new IllegalArgumentException("El stock es obligatorio.");
        }

        long stock;
        try {
            stock = Long.parseLong(stockTexto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El stock debe ser un número entero válido (sin decimales ni letras).");
        }

        if (stock < 0 || stock > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("El stock debe ser un entero entre 0 y 2,147,483,647.");
        }
    }
}