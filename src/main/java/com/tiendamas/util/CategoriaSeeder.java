package com.tiendamas.util;

import com.tiendamas.entity.Categoria;
import com.tiendamas.repository.CategoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Se ejecuta en cada arranque y agrega subcategorías realistas a las
 * categorías principales existentes (Hombre, Mujer, Niños, Accesorios) que
 * todavía no tengan ninguna. Es idempotente: si una categoría ya tiene
 * subcategorías, no la toca.
 */
@Component
public class CategoriaSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CategoriaSeeder.class);

    private static final Map<String, List<String>> SUBCATEGORIAS_POR_CATEGORIA = Map.of(
            "hombre", List.of("Camisas", "Polos", "Buzos", "Casacas", "Jeans", "Zapatillas"),
            "mujer", List.of("Blusas", "Vestidos", "Buzos", "Casacas", "Jeans", "Zapatillas"),
            "ninos", List.of("Polos", "Buzos", "Shorts", "Zapatillas"),
            "accesorios", List.of("Gorras", "Cinturones", "Bufandas", "Medias", "Mochilas")
    );

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public void run(String... args) {
        List<Categoria> todas = categoriaRepository.findAll();
        Set<Long> idsConSubcategorias = new HashSet<>();
        for (Categoria c : todas) {
            if (c.getCategoriaPadre() != null) {
                idsConSubcategorias.add(c.getCategoriaPadre().getId());
            }
        }

        List<Categoria> principales = todas.stream().filter(c -> c.getCategoriaPadre() == null).toList();
        for (Categoria principal : principales) {
            if (idsConSubcategorias.contains(principal.getId())) {
                continue;
            }
            String clave = normalizar(principal.getNombre());
            List<String> nombres = SUBCATEGORIAS_POR_CATEGORIA.get(clave);
            if (nombres == null) {
                continue;
            }
            for (String nombre : nombres) {
                Categoria sub = new Categoria(nombre, nombre + " para " + principal.getNombre().toLowerCase());
                sub.setCategoriaPadre(principal);
                categoriaRepository.save(sub);
            }
            log.info("Se agregaron {} subcategorías a '{}'.", nombres.size(), principal.getNombre());
        }
    }

    private String normalizar(String nombre) {
        if (nombre == null) return "";
        return nombre.trim().toLowerCase()
                .replace("ñ", "n");
    }
}
