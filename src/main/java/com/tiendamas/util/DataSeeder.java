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
import com.tiendamas.entity.TipoDocumento;
import com.tiendamas.repository.CategoriaRepository;
import com.tiendamas.repository.GastoRepository;
import com.tiendamas.repository.PersonaRepository;
import com.tiendamas.repository.ProductoRepository;
import com.tiendamas.repository.SueldoRepository;
import com.tiendamas.repository.UsuarioRepository;
import com.tiendamas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Component
@Profile("!prod")
@ConditionalOnProperty(name = "app.seed.demo", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

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

    @Override
    public void run(String... args) {
        if (personaRepository.count() <= 0) {
            sembrarCatalogoYClientesDemo();
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

    private void sembrarCatalogoYClientesDemo() {
        personaRepository.save(new Persona("Ana", "Gómez", "ana.gomez@example.com",
                "987654321", "Av. Los Álamos 123", TipoDocumento.DNI, "45678912"));
        personaRepository.save(new Persona("Luis", "Ramírez", "luis.ramirez@example.com",
                "987654322", "Jr. Las Flores 456", TipoDocumento.DNI, "41234567"));
        personaRepository.save(new Persona("María", "Torres", "maria.torres@example.com",
                "987654323", "Calle Sol 789", TipoDocumento.DNI, "48765432"));
        personaRepository.save(new Persona("Jorge", "Salazar", "jorge.salazar@example.com",
                "987654324", "Av. Industrial 321", TipoDocumento.DNI, "42345678"));

        Persona comercialLosAndes = new Persona("Comercial", "Los Andes S.A.C.", "ventas@losandes.com",
                "014567890", "Av. Comercio 1000", TipoDocumento.RUC, "20481234567");
        comercialLosAndes.setRazonSocial("Comercial Los Andes S.A.C.");
        personaRepository.save(comercialLosAndes);

        Persona distribuidoraElSol = new Persona("Distribuidora", "El Sol E.I.R.L.", "contacto@elsol.com",
                "014567891", "Jr. Industria 500", TipoDocumento.RUC, "20567891234");
        distribuidoraElSol.setRazonSocial("Distribuidora El Sol E.I.R.L.");
        personaRepository.save(distribuidoraElSol);

        Categoria abarrotes = categoriaRepository.save(new Categoria("Abarrotes", "Alimentos y productos de despensa"));
        Categoria bebidas = categoriaRepository.save(new Categoria("Bebidas", "Bebidas frías y calientes"));
        Categoria limpieza = categoriaRepository.save(new Categoria("Limpieza", "Productos de limpieza para el hogar"));
        Categoria electronica = categoriaRepository.save(new Categoria("Electrónica", "Accesorios y artículos electrónicos"));
        Categoria ropa = categoriaRepository.save(new Categoria("Ropa", "Prendas de vestir"));

        List<Producto> productos = List.of(
                conCodigo(new Producto("Arroz Extra 1kg", "Arroz blanco extra", 4.50, 80, abarrotes), "7750001000011"),
                conCodigo(new Producto("Azúcar Rubia 1kg", "Azúcar rubia doméstica", 4.20, 60, abarrotes), "7750001000028"),
                conCodigo(new Producto("Aceite Vegetal 1L", "Aceite vegetal para cocina", 9.90, 40, abarrotes), "7750001000035"),
                conCodigo(new Producto("Fideos Spaghetti 500g", "Fideos de sémola", 3.20, 70, abarrotes), "7750001000042"),
                conCodigo(new Producto("Agua Mineral 625ml", "Agua sin gas", 2.00, 120, bebidas), "7750001000059"),
                conCodigo(new Producto("Gaseosa Cola 1.5L", "Bebida gaseosa sabor cola", 6.50, 55, bebidas), "7750001000066"),
                conCodigo(new Producto("Jugo de Naranja 1L", "Néctar de naranja", 5.90, 45, bebidas), "7750001000073"),
                conCodigo(new Producto("Detergente 1kg", "Detergente en polvo", 12.90, 30, limpieza), "7750001000080"),
                conCodigo(new Producto("Lejía 1L", "Lejía desinfectante", 3.50, 50, limpieza), "7750001000097"),
                conCodigo(new Producto("Audífonos Bluetooth", "Audífonos inalámbricos", 59.90, 15, electronica), "7750001000103"),
                conCodigo(new Producto("Cargador USB-C", "Cargador rápido 20W", 34.90, 25, electronica), "7750001000110"),
                conCodigo(new Producto("Polo Algodón", "Polo básico de algodón", 24.90, 20, ropa), "7750001000127"),
                conCodigo(new Producto("Pantalón Jean", "Pantalón de mezclilla", 69.90, 3, ropa), "7750001000134")
        );
        productoRepository.saveAll(productos);
    }

    private void sembrarUsuariosDemo() {
        usuarioService.crearUsuario("admin", "admin123", RolUsuario.ADMIN, "Administrador", "Sistema", null);
        usuarioService.crearUsuario("vendedor1", "vendedor123", RolUsuario.VENDEDOR, "Carlos", "Mendoza", null);
        usuarioService.crearUsuario("vendedor2", "vendedor123", RolUsuario.VENDEDOR, "Rosa", "Quispe", null);

        Persona personaCliente1 = personaConDocumento(TipoDocumento.DNI)
                .orElseGet(() -> personaRepository.save(new Persona("Ana", "Gómez", "ana.gomez@example.com",
                        "987654321", "Av. Los Álamos 123", TipoDocumento.DNI, "45678912")));
        usuarioService.crearUsuario("cliente1", "cliente123", RolUsuario.CLIENTE,
                personaCliente1.getNombre(), personaCliente1.getApellido(), personaCliente1);

        Persona personaCliente2 = personaConDocumento(TipoDocumento.RUC)
                .orElseGet(() -> {
                    Persona p = new Persona("Comercial", "Los Andes S.A.C.", "ventas@losandes.com",
                            "014567890", "Av. Comercio 1000", TipoDocumento.RUC, "20481234567");
                    p.setRazonSocial("Comercial Los Andes S.A.C.");
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
        insumos.setConcepto("Reposición de mercadería");
        insumos.setCategoria(CategoriaGasto.INSUMOS);
        insumos.setMonto(650.0);
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

    private Optional<Persona> personaConDocumento(TipoDocumento tipo) {
        return personaRepository.findAll().stream()
                .filter(p -> p.getTipoDocumento() == tipo)
                .findFirst();
    }

    private Producto conCodigo(Producto producto, String codigo) {
        producto.setCodigoBarras(codigo);
        return producto;
    }
}
