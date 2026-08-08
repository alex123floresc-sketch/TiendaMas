package com.tiendamas.util;

import com.tiendamas.entity.Categoria;
import com.tiendamas.entity.CategoriaGasto;
import com.tiendamas.entity.EstadoSueldo;
import com.tiendamas.entity.FrecuenciaGasto;
import com.tiendamas.entity.Gasto;
import com.tiendamas.entity.Persona;
import com.tiendamas.entity.Producto;
import com.tiendamas.entity.RolUsuario;
import com.tiendamas.entity.Sueldo;
import com.tiendamas.entity.Talla;
import com.tiendamas.entity.TipoDocumento;
import com.tiendamas.entity.VarianteProducto;
import com.tiendamas.repository.CategoriaRepository;
import com.tiendamas.repository.GastoRepository;
import com.tiendamas.repository.PersonaRepository;
import com.tiendamas.repository.ProductoRepository;
import com.tiendamas.repository.SueldoRepository;
import com.tiendamas.repository.UsuarioRepository;
import com.tiendamas.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Component
@Profile("!prod")
@ConditionalOnProperty(name = "app.seed.demo", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private Environment environment;

    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private GastoRepository gastoRepository;
    @Autowired
    private SueldoRepository sueldoRepository;

    private int contadorCodigo = 0;

    @Override
    public void run(String... args) {
        if (personaRepository.count() <= 0) {
            sembrarClientesDemo();
        }

        if (productoRepository.count() <= 0) {
            sembrarCatalogoDemo();
        }

        if (usuarioRepository.count() <= 0) {
            sembrarUsuariosDemo();
        }

        if (gastoRepository.count() <= 0) {
            sembrarGastosDemo();
        }

        if (sueldoRepository.count() <= 0) {
            sembrarSueldosDemo();
        }
    }

    private void sembrarClientesDemo() {
        personaRepository.save(new Persona("Ana", "Gómez", "ana.gomez@example.com",
                "987654321", "Av. Los Álamos 123", TipoDocumento.DNI, "45678912"));
        personaRepository.save(new Persona("Luis", "Ramírez", "luis.ramirez@example.com",
                "987654322", "Jr. Las Flores 456", TipoDocumento.DNI, "41234567"));
        personaRepository.save(new Persona("María", "Torres", "maria.torres@example.com",
                "987654323", "Calle Sol 789", TipoDocumento.DNI, "48765432"));
        personaRepository.save(new Persona("Jorge", "Salazar", "jorge.salazar@example.com",
                "987654324", "Av. Industrial 321", TipoDocumento.DNI, "42345678"));

        Persona boutiqueElEstilo = new Persona("Boutique", "El Estilo S.A.C.", "compras@elestilo.com",
                "014567890", "Av. Comercio 1000", TipoDocumento.RUC, "20481234567");
        boutiqueElEstilo.setRazonSocial("Boutique El Estilo S.A.C.");
        personaRepository.save(boutiqueElEstilo);

        Persona uniformesEscolares = new Persona("Uniformes", "Escolares E.I.R.L.", "contacto@uniformesescolares.com",
                "014567891", "Jr. Industria 500", TipoDocumento.RUC, "20567891234");
        uniformesEscolares.setRazonSocial("Uniformes Escolares E.I.R.L.");
        personaRepository.save(uniformesEscolares);
    }

    private void sembrarCatalogoDemo() {
        Categoria hombre = categoriaRepository.save(new Categoria("Hombre", "Ropa y prendas para hombre"));
        Categoria mujer = categoriaRepository.save(new Categoria("Mujer", "Ropa y prendas para mujer"));
        Categoria ninos = categoriaRepository.save(new Categoria("Niños", "Ropa para niños y niñas"));
        Categoria accesorios = categoriaRepository.save(new Categoria("Accesorios", "Complementos para todo el guardarropa"));

        // ---------- Hombre ----------
        Producto camisaOxford = nuevoProducto("Camisa Oxford Manga Larga", "Camisa de algodón, corte clásico, ideal para oficina o salidas.", 79.90, "TiendaMas", hombre);
        variante(camisaOxford, Talla.S, "Celeste", "#87CEEB", 12);
        variante(camisaOxford, Talla.M, "Celeste", "#87CEEB", 18);
        variante(camisaOxford, Talla.L, "Blanco", "#FFFFFF", 15);
        variante(camisaOxford, Talla.XL, "Blanco", "#FFFFFF", 4);
        guardarConVariantes(camisaOxford);

        Producto poloPique = nuevoProducto("Polo Piqué Básico", "Polo de algodón piqué, ajuste regular, básico infaltable.", 39.90, "TiendaMas", hombre);
        variante(poloPique, Talla.S, "Negro", "#111111", 20);
        variante(poloPique, Talla.M, "Negro", "#111111", 25);
        variante(poloPique, Talla.L, "Blanco", "#FFFFFF", 22);
        variante(poloPique, Talla.XL, "Azul Marino", "#1F2A44", 10);
        variante(poloPique, Talla.XXL, "Azul Marino", "#1F2A44", 3);
        guardarConVariantes(poloPique);

        Producto jeanSlimHombre = nuevoProducto("Jean Slim Hombre", "Pantalón de mezclilla stretch, corte slim.", 129.90, "DenimCo", hombre);
        variante(jeanSlimHombre, Talla.S, "Azul", "#3B5998", 8);
        variante(jeanSlimHombre, Talla.M, "Azul", "#3B5998", 14);
        variante(jeanSlimHombre, Talla.L, "Negro", "#111111", 9);
        variante(jeanSlimHombre, Talla.XL, "Negro", "#111111", 2);
        guardarConVariantes(jeanSlimHombre);

        Producto camperaRompevientos = nuevoProducto("Campera Rompevientos", "Campera liviana impermeable, ideal para entretiempo.", 189.90, "TiendaMas", hombre);
        variante(camperaRompevientos, Talla.M, "Negro", "#111111", 7);
        variante(camperaRompevientos, Talla.L, "Verde", "#2F6B3A", 6);
        variante(camperaRompevientos, Talla.XL, "Verde", "#2F6B3A", 3);
        guardarConVariantes(camperaRompevientos);

        // ---------- Mujer ----------
        Producto blusaCuelloV = nuevoProducto("Blusa Cuello V", "Blusa liviana de gasa, ideal para el día a día.", 49.90, "TiendaMas", mujer);
        variante(blusaCuelloV, Talla.S, "Blanco", "#FFFFFF", 16);
        variante(blusaCuelloV, Talla.M, "Rosa", "#F4B6C2", 20);
        variante(blusaCuelloV, Talla.L, "Rosa", "#F4B6C2", 11);
        guardarConVariantes(blusaCuelloV);

        Producto vestidoCasual = nuevoProducto("Vestido Casual Midi", "Vestido midi de tela fluida, para uso diario o salidas.", 99.90, "TiendaMas", mujer);
        variante(vestidoCasual, Talla.S, "Negro", "#111111", 9);
        variante(vestidoCasual, Talla.M, "Estampado Floral", "#D96C6C", 13);
        variante(vestidoCasual, Talla.L, "Estampado Floral", "#D96C6C", 5);
        variante(vestidoCasual, Talla.XL, "Negro", "#111111", 4);
        guardarConVariantes(vestidoCasual);

        Producto jeanSkinnyMujer = nuevoProducto("Jean Skinny Tiro Alto", "Jean skinny de tiro alto con stretch, realza la figura.", 119.90, "DenimCo", mujer);
        variante(jeanSkinnyMujer, Talla.S, "Azul", "#3B5998", 15);
        variante(jeanSkinnyMujer, Talla.M, "Azul", "#3B5998", 17);
        variante(jeanSkinnyMujer, Talla.L, "Negro", "#111111", 8);
        guardarConVariantes(jeanSkinnyMujer);

        Producto cardigan = nuevoProducto("Cardigan Tejido", "Cardigan tejido abierto, cómodo y abrigador.", 89.90, "TiendaMas", mujer);
        variante(cardigan, Talla.S, "Beige", "#D8C3A5", 10);
        variante(cardigan, Talla.M, "Gris", "#9CA3AF", 12);
        variante(cardigan, Talla.L, "Gris", "#9CA3AF", 1);
        guardarConVariantes(cardigan);

        // ---------- Niños ----------
        Producto poloEscolar = nuevoProducto("Polo Escolar Piqué", "Polo escolar de algodón piqué, alta durabilidad al lavado.", 29.90, "TiendaMas", ninos);
        variante(poloEscolar, Talla.S, "Blanco", "#FFFFFF", 25);
        variante(poloEscolar, Talla.M, "Blanco", "#FFFFFF", 30);
        variante(poloEscolar, Talla.L, "Blanco", "#FFFFFF", 14);
        guardarConVariantes(poloEscolar);

        Producto shortDeportivoNino = nuevoProducto("Short Deportivo Niño", "Short deportivo transpirable, ideal para educación física.", 34.90, "SportKids", ninos);
        variante(shortDeportivoNino, Talla.S, "Azul Marino", "#1F2A44", 18);
        variante(shortDeportivoNino, Talla.M, "Azul Marino", "#1F2A44", 16);
        variante(shortDeportivoNino, Talla.L, "Negro", "#111111", 6);
        guardarConVariantes(shortDeportivoNino);

        // ---------- Accesorios ----------
        Producto gorraBordada = nuevoProducto("Gorra Bordada", "Gorra ajustable con visera curva, bordado frontal.", 39.90, "TiendaMas", accesorios);
        variante(gorraBordada, Talla.UNICA, "Negro", "#111111", 22);
        variante(gorraBordada, Talla.UNICA, "Azul Marino", "#1F2A44", 17);
        guardarConVariantes(gorraBordada);

        Producto cinturonCuero = nuevoProducto("Cinturón de Cuero", "Cinturón de cuero genuino con hebilla metálica.", 49.90, "TiendaMas", accesorios);
        variante(cinturonCuero, Talla.UNICA, "Marrón", "#5C4033", 14);
        variante(cinturonCuero, Talla.UNICA, "Negro", "#111111", 3);
        guardarConVariantes(cinturonCuero);

        Producto bufandaTejida = nuevoProducto("Bufanda Tejida", "Bufanda tejida de punto grueso, abrigo para el invierno.", 34.90, "TiendaMas", accesorios);
        variante(bufandaTejida, Talla.UNICA, "Gris", "#9CA3AF", 11);
        variante(bufandaTejida, Talla.UNICA, "Rojo", "#B91C1C", 2);
        guardarConVariantes(bufandaTejida);

        Producto mediasPack = nuevoProducto("Medias Pack x3", "Pack de 3 pares de medias de algodón.", 24.90, "TiendaMas", accesorios);
        variante(mediasPack, Talla.UNICA, "Blanco", "#FFFFFF", 30);
        variante(mediasPack, Talla.UNICA, "Negro", "#111111", 28);
        guardarConVariantes(mediasPack);
    }

    private void sembrarUsuariosDemo() {
        String adminUsername = environment.getProperty("ADMIN_USERNAME", "admin");
        String adminPassword = environment.getProperty("ADMIN_PASSWORD");
        if (adminPassword == null || adminPassword.isBlank()) {
            adminPassword = generarPasswordAleatoria();
            log.warn("ADMIN_PASSWORD no esta definida. Se genero una contrasena aleatoria para el usuario '{}': {}"
                    + " -- cambiala apenas ingreses.", adminUsername, adminPassword);
        }
        usuarioService.crearUsuario(adminUsername, adminPassword, RolUsuario.ADMIN, "Administrador", "Sistema", null);
        usuarioService.crearUsuario("vendedor1", "vendedor123", RolUsuario.VENDEDOR, "Carlos", "Mendoza", null);
        usuarioService.crearUsuario("vendedor2", "vendedor123", RolUsuario.VENDEDOR, "Rosa", "Quispe", null);

        Persona personaCliente1 = personaConDocumento(TipoDocumento.DNI)
                .orElseGet(() -> personaRepository.save(new Persona("Ana", "Gómez", "ana.gomez@example.com",
                        "987654321", "Av. Los Álamos 123", TipoDocumento.DNI, "45678912")));
        usuarioService.crearUsuario("cliente1", "cliente123", RolUsuario.CLIENTE,
                personaCliente1.getNombre(), personaCliente1.getApellido(), personaCliente1);

        Persona personaCliente2 = personaConDocumento(TipoDocumento.RUC)
                .orElseGet(() -> {
                    Persona p = new Persona("Boutique", "El Estilo S.A.C.", "compras@elestilo.com",
                            "014567890", "Av. Comercio 1000", TipoDocumento.RUC, "20481234567");
                    p.setRazonSocial("Boutique El Estilo S.A.C.");
                    return personaRepository.save(p);
                });
        usuarioService.crearUsuario("cliente2", "cliente123", RolUsuario.CLIENTE,
                personaCliente2.getNombre(), personaCliente2.getApellido(), personaCliente2);
    }

    private void sembrarGastosDemo() {
        Gasto alquiler = new Gasto();
        alquiler.setConcepto("Alquiler del local");
        alquiler.setCategoria(CategoriaGasto.ALQUILER);
        alquiler.setMonto(1200.0);
        alquiler.setFecha(LocalDate.now().withDayOfMonth(1));
        alquiler.setRecurrente(true);
        alquiler.setFrecuencia(FrecuenciaGasto.MENSUAL);
        gastoRepository.save(alquiler);

        Gasto servicios = new Gasto();
        servicios.setConcepto("Luz, agua e internet");
        servicios.setCategoria(CategoriaGasto.SERVICIOS);
        servicios.setMonto(280.0);
        servicios.setFecha(LocalDate.now().withDayOfMonth(Math.min(5, LocalDate.now().lengthOfMonth())));
        servicios.setRecurrente(true);
        servicios.setFrecuencia(FrecuenciaGasto.MENSUAL);
        gastoRepository.save(servicios);

        Gasto insumos = new Gasto();
        insumos.setConcepto("Reposición de mercadería (temporada)");
        insumos.setCategoria(CategoriaGasto.INSUMOS);
        insumos.setMonto(2500.0);
        insumos.setFecha(LocalDate.now().withDayOfMonth(Math.min(10, LocalDate.now().lengthOfMonth())));
        insumos.setRecurrente(false);
        insumos.setFrecuencia(FrecuenciaGasto.UNICO);
        gastoRepository.save(insumos);
    }

    private void sembrarSueldosDemo() {
        String periodoActual = YearMonth.now().toString();
        LocalDate fechaPago = LocalDate.now().withDayOfMonth(Math.min(28, LocalDate.now().lengthOfMonth()));

        usuarioRepository.findByUsername("vendedor1").ifPresent(vendedor -> {
            Sueldo sueldo = new Sueldo();
            sueldo.setUsuario(vendedor);
            sueldo.setMonto(1200.0);
            sueldo.setPeriodo(periodoActual);
            sueldo.setFechaPago(fechaPago);
            sueldo.setEstado(EstadoSueldo.PAGADO);
            sueldoRepository.save(sueldo);
        });

        usuarioRepository.findByUsername("vendedor2").ifPresent(vendedor -> {
            Sueldo sueldo = new Sueldo();
            sueldo.setUsuario(vendedor);
            sueldo.setMonto(1100.0);
            sueldo.setPeriodo(periodoActual);
            sueldo.setFechaPago(fechaPago);
            sueldo.setEstado(EstadoSueldo.PENDIENTE);
            sueldoRepository.save(sueldo);
        });
    }

    private String generarPasswordAleatoria() {
        byte[] bytes = new byte[15];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Optional<Persona> personaConDocumento(TipoDocumento tipo) {
        return personaRepository.findAll().stream()
                .filter(p -> p.getTipoDocumento() == tipo)
                .findFirst();
    }

    private Producto nuevoProducto(String nombre, String descripcion, double precio, String marca, Categoria categoria) {
        Producto producto = new Producto(nombre, descripcion, precio, categoria);
        producto.setMarca(marca);
        return producto;
    }

    private void variante(Producto producto, Talla talla, String color, String colorHex, int stock) {
        String codigo = "775" + String.format("%010d", ++contadorCodigo);
        producto.agregarVariante(new VarianteProducto(producto, talla, color, colorHex, stock, codigo));
    }

    private void guardarConVariantes(Producto producto) {
        productoRepository.save(producto);
    }
}
