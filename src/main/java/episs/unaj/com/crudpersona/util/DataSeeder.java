package episs.unaj.com.crudpersona.util;

import episs.unaj.com.crudpersona.entity.Categoria;
import episs.unaj.com.crudpersona.entity.Persona;
import episs.unaj.com.crudpersona.entity.Producto;
import episs.unaj.com.crudpersona.repository.CategoriaRepository;
import episs.unaj.com.crudpersona.repository.PersonaRepository;
import episs.unaj.com.crudpersona.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Override
    public void run(String... args) throws Exception {
        System.out.println("AQUI INICIANDO");

        if (personaRepository.count()<=0){
            List<Persona> personas = List.of(new Persona("Noe Wilber","Tipo Mamani","noe.tipo@gmail.com", "99999999","SIN DIRECCION"),
                    new Persona("Noe Wilber","Tipo Mamani","noe.tipo@gmail.com", "99999999","SIN DIRECCION"),
                    new Persona("Noe Wilber","Tipo Mamani","noe.tipo@gmail.com", "99999999","SIN DIRECCION"),
                    new Persona("Noe Wilber","Tipo Mamani","noe.tipo@gmail.com", "99999999","SIN DIRECCION"),
                    new Persona("Noe Wilber","Tipo Mamani","noe.tipo@gmail.com", "99999999","SIN DIRECCION"),
                    new Persona("Noe Wilber","Tipo Mamani","noe.tipo@gmail.com", "99999999","SIN DIRECCION"));
        personaRepository.saveAll(personas);

            Categoria categoria1 = categoriaRepository.save(new Categoria("Ropa","ROPA"));
            Categoria categoria2 = categoriaRepository.save(new Categoria("Ropa1","ROPA"));
            Categoria categoria3 = categoriaRepository.save(new Categoria("Ropa2","ROPA"));
            Categoria categoria4 = categoriaRepository.save(new Categoria("Ropa3","ROPA"));
            Categoria categoria5 = categoriaRepository.save(new Categoria("Ropa4","ROPA"));
            Categoria categoria6 = categoriaRepository.save(new Categoria("Ropa5","ROPA"));
            Categoria categoria7 = categoriaRepository.save(new Categoria("Ropa6","ROPA"));
            Categoria categoria8 = categoriaRepository.save(new Categoria("Ropa7","ROPA"));
            Categoria categoria9 = categoriaRepository.save(new Categoria("Ropa8","ROPA"));


            List<Producto> productoList = List.of(new Producto("Pan","pan",12.0,12,categoria1),
                    new Producto("Pan","pan",12.0,12,categoria2),
                    new Producto("Pan","pan",12.0,12,categoria3),
                    new Producto("Pan","pan",12.0,12,categoria4),
                    new Producto("Pan","pan",12.0,12,categoria5),
                    new Producto("Pan","pan",12.0,12,categoria6));
            productoRepository.saveAll(productoList);
        }

    }
}
