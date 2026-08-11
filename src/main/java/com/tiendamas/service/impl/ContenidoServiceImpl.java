package com.tiendamas.service.impl;

import com.tiendamas.entity.ContenidoSitio;
import com.tiendamas.repository.ContenidoSitioRepository;
import com.tiendamas.service.ContenidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ContenidoServiceImpl implements ContenidoService {

    // Textos originales del sitio: se usan mientras el administrador no los haya
    // sobrescrito desde /contenido, así que el sitio nunca queda con campos vacíos.
    private static final Map<String, String> VALORES_POR_DEFECTO = new LinkedHashMap<>();

    static {
        VALORES_POR_DEFECTO.put("hero.titulo", "Moda para toda la familia");
        VALORES_POR_DEFECTO.put("hero.subtitulo", "Ropa y accesorios para hombre, mujer y niños, con envío a domicilio o recojo en tienda.");
        VALORES_POR_DEFECTO.put("hero.boton", "Ver catálogo");

        VALORES_POR_DEFECTO.put("footer.descripcion", "Ropa y accesorios para hombre, mujer y niños: prendas de calidad, atención cercana y entrega rápida, ya sea en línea o en el mostrador.");
        VALORES_POR_DEFECTO.put("footer.telefono", "+51 900 948 480");
        VALORES_POR_DEFECTO.put("footer.email", "contacto@tiendamas.com");
        VALORES_POR_DEFECTO.put("footer.mapaUrl", "https://www.google.com/maps?q=-15.492302105189488,-70.13447502049469");
        VALORES_POR_DEFECTO.put("footer.horario", "Lun. a Vie. 9:00–19:00 hs.");
        VALORES_POR_DEFECTO.put("footer.facebookUrl", "");
        VALORES_POR_DEFECTO.put("footer.instagramUrl", "");
        VALORES_POR_DEFECTO.put("footer.whatsappNumero", "");

        VALORES_POR_DEFECTO.put("info.quienesSomos", "TiendaMas es una tienda de ropa y accesorios para hombre, mujer y niños. Trabajamos con prendas de calidad, atención cercana y entrega rápida, ya sea comprando en línea o directamente en el mostrador de nuestra tienda física.");
        VALORES_POR_DEFECTO.put("info.trabajaConNosotros", "¿Te gustaría formar parte de nuestro equipo? Escríbenos contándonos un poco sobre ti y revisaremos tu postulación apenas tengamos una vacante disponible.");
        VALORES_POR_DEFECTO.put("info.libroReclamaciones", "Conforme a lo establecido en el Código de Protección y Defensa del Consumidor, este establecimiento cuenta con un Libro de Reclamaciones a tu disposición. Para registrar una queja o reclamo, contáctanos por cualquiera de los medios indicados al pie de la página y te atenderemos a la brevedad.");
        VALORES_POR_DEFECTO.put("info.preguntasFrecuentes", "¿Cómo hago un pedido?\nElige tus productos, agrégalos al carrito y sigue los pasos del checkout. Necesitarás una cuenta de cliente con tus datos de contacto y documento para poder emitir tu boleta o factura.\n\n¿Qué métodos de pago aceptan?\nEfectivo contra entrega, tarjeta de crédito/débito, Yape/Plin y transferencia bancaria.\n\n¿Puedo recoger mi pedido en tienda?\nSí, al finalizar tu compra puedes elegir \"Retiro en tienda\" sin costo adicional.");
        VALORES_POR_DEFECTO.put("info.enviosYEntregas", "Realizamos envíos a todo el país. Una vez confirmado tu pedido, te avisaremos por correo cuando salga en camino. También puedes optar por el retiro gratuito en nuestra tienda física.");
        VALORES_POR_DEFECTO.put("info.cambiosYDevoluciones", "Si una prenda no te queda como esperabas, puedes solicitar un cambio sin costo dentro de los 15 días posteriores a la entrega, siempre que el producto conserve sus etiquetas y no haya sido usado. Escríbenos indicando el número de tu pedido para coordinar el cambio.");
        VALORES_POR_DEFECTO.put("info.terminosYCondiciones", "Al comprar en TiendaMas aceptas que los precios y la disponibilidad de stock pueden variar sin previo aviso, que los datos de facturación (documento y razón social) deben ser correctos y vigentes, y que el uso de este sitio está sujeto a las leyes de protección al consumidor vigentes en Perú.");
        VALORES_POR_DEFECTO.put("info.politicaDePrivacidad", "Tus datos personales (nombre, documento, dirección y contacto) se usan únicamente para procesar tus pedidos y emitir tus comprobantes de pago. No compartimos tu información con terceros salvo que sea necesario para completar la entrega de tu pedido.");
    }

    @Autowired
    private ContenidoSitioRepository contenidoSitioRepository;

    @Override
    public Map<String, String> obtenerTodo() {
        Map<String, String> resultado = new LinkedHashMap<>(VALORES_POR_DEFECTO);
        for (ContenidoSitio c : contenidoSitioRepository.findAll()) {
            if (c.getValor() != null && !c.getValor().isBlank()) {
                resultado.put(c.getClave(), c.getValor());
            }
        }
        return resultado;
    }

    @Override
    @Transactional
    public void guardar(Map<String, String> valores) {
        for (Map.Entry<String, String> entry : valores.entrySet()) {
            if (!VALORES_POR_DEFECTO.containsKey(entry.getKey())) {
                continue;
            }
            ContenidoSitio c = contenidoSitioRepository.findById(entry.getKey())
                    .orElse(new ContenidoSitio(entry.getKey(), null));
            c.setValor(entry.getValue());
            contenidoSitioRepository.save(c);
        }
    }
}
