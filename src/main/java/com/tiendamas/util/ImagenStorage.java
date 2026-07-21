package com.tiendamas.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/** Guarda y elimina las imágenes de producto subidas por el usuario en la carpeta externa "uploads/productos". */
@Component
public class ImagenStorage {

    private static final Path CARPETA = Path.of("uploads", "productos");
    private static final String RUTA_PUBLICA = "/uploads/productos/";

    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return null;
        }
        try {
            Files.createDirectories(CARPETA);
            String extension = obtenerExtension(archivo.getOriginalFilename());
            String nombreArchivo = UUID.randomUUID() + extension;
            archivo.transferTo(CARPETA.resolve(nombreArchivo));
            return RUTA_PUBLICA + nombreArchivo;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la imagen del producto", e);
        }
    }

    public void eliminar(String imagenUrl) {
        if (imagenUrl == null || !imagenUrl.startsWith(RUTA_PUBLICA)) {
            return;
        }
        try {
            Files.deleteIfExists(CARPETA.resolve(imagenUrl.substring(RUTA_PUBLICA.length())));
        } catch (IOException ignored) {
            // Si no se puede borrar el archivo viejo, no es motivo para fallar la operación.
        }
    }

    private String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null) {
            return "";
        }
        int punto = nombreOriginal.lastIndexOf('.');
        return punto >= 0 ? nombreOriginal.substring(punto) : "";
    }
}
