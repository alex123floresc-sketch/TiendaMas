package com.tiendamas.service.impl;

import com.tiendamas.entity.DetallePedido;
import com.tiendamas.entity.Pedido;
import com.tiendamas.entity.Persona;
import com.tiendamas.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.remitente}")
    private String remitente;

    @Override
    @Async
    public void enviarCambioEstadoPedido(Pedido pedido) {
        if (pedido == null || pedido.getPersona() == null) {
            return;
        }
        String destinatario = pedido.getPersona().getEmail();
        if (destinatario == null || destinatario.isBlank()) {
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject("TiendaMas - Actualización de tu pedido " + pedido.getNumeroCompleto());
            mensaje.setText(construirCuerpo(pedido));
            mailSender.send(mensaje);
        } catch (MailAuthenticationException e) {
            logFalloAutenticacion("cambio de estado", pedido.getId(), e);
        } catch (MailException e) {
            log.warn("No se pudo enviar el correo de cambio de estado del pedido {}: {}", pedido.getId(), e.getMessage());
        }
    }

    @Override
    @Async
    public void enviarConfirmacionPedido(Pedido pedido, byte[] pdfComprobante) {
        if (pedido == null || pedido.getPersona() == null) {
            return;
        }
        String destinatario = pedido.getPersona().getEmail();
        if (destinatario == null || destinatario.isBlank()) {
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, pdfComprobante != null);
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("TiendaMas - Confirmación de tu pedido " + pedido.getNumeroCompleto());
            helper.setText(construirCuerpoConfirmacion(pedido, pdfComprobante != null));
            if (pdfComprobante != null) {
                helper.addAttachment("Boleta-" + pedido.getNumeroCompleto() + ".pdf",
                        new org.springframework.core.io.ByteArrayResource(pdfComprobante));
            }
            mailSender.send(mimeMessage);
        } catch (MailAuthenticationException e) {
            logFalloAutenticacion("confirmación de pedido", pedido.getId(), e);
        } catch (MailException | MessagingException e) {
            log.warn("No se pudo enviar el correo de confirmación del pedido {}: {}", pedido.getId(), e.getMessage());
        }
    }

    @Override
    public boolean enviarComprobantePorCorreo(Pedido pedido, byte[] pdfBoleta) {
        if (pedido == null || pedido.getPersona() == null) {
            return false;
        }
        String destinatario = pedido.getPersona().getEmail();
        if (destinatario == null || destinatario.isBlank()) {
            return false;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("TiendaMas - Tu comprobante " + pedido.getNumeroCompleto());
            helper.setText("Hola " + pedido.getPersona().getNombre() + ",\n\n"
                    + "Adjuntamos el comprobante de tu pedido " + pedido.getNumeroCompleto() + ".\n\n"
                    + "Gracias por tu compra.\nTiendaMas");
            helper.addAttachment("Boleta-" + pedido.getNumeroCompleto() + ".pdf",
                    new org.springframework.core.io.ByteArrayResource(pdfBoleta));
            mailSender.send(mimeMessage);
            return true;
        } catch (MailAuthenticationException e) {
            logFalloAutenticacion("comprobante", pedido.getId(), e);
            return false;
        } catch (MailException | MessagingException e) {
            log.warn("No se pudo enviar el comprobante por correo del pedido {}: {}", pedido.getId(), e.getMessage());
            return false;
        }
    }

    private void logFalloAutenticacion(String contexto, Long pedidoId, MailAuthenticationException e) {
        log.error("No se pudo enviar el correo de {} del pedido {}: fallo la autenticacion SMTP. "
                        + "Revisa las variables de entorno MAIL_USERNAME/MAIL_PASSWORD: Gmail exige una "
                        + "'contrasena de aplicacion' de 16 caracteres (no la contrasena normal de la cuenta), "
                        + "generada en https://myaccount.google.com/apppasswords con la verificacion en dos pasos activada. "
                        + "Detalle: {}", contexto, pedidoId, e.getMessage());
    }

    private String construirCuerpo(Pedido pedido) {
        Persona persona = pedido.getPersona();
        String estado = pedido.getEstado() != null ? pedido.getEstado().getEtiqueta() : "actualizado";
        return "Hola " + persona.getNombre() + ",\n\n"
                + "El estado de tu pedido " + pedido.getNumeroCompleto() + " cambió a: " + estado + ".\n\n"
                + "Podés ver el detalle completo ingresando a la tienda, en la sección \"Mis pedidos\".\n\n"
                + "Gracias por tu compra.\nTiendaMas";
    }

    private String construirCuerpoConfirmacion(Pedido pedido, boolean conComprobanteAdjunto) {
        Persona persona = pedido.getPersona();
        StringBuilder texto = new StringBuilder();
        texto.append("Hola ").append(persona.getNombre()).append(",\n\n")
                .append("Registramos tu pedido ").append(pedido.getNumeroCompleto()).append(".\n\n")
                .append("Detalle:\n");

        for (DetallePedido d : pedido.getDetalles()) {
            texto.append("- ").append(d.getCantidad()).append(" x ").append(d.getProducto().getNombre());
            if (d.getTalla() != null && !d.getTalla().isBlank()) {
                texto.append(" (talla ").append(d.getTalla());
                if (d.getColor() != null && !d.getColor().isBlank()) {
                    texto.append(", ").append(d.getColor());
                }
                texto.append(")");
            }
            texto.append(" — S/ ").append(d.getSubtotal()).append("\n");
        }

        texto.append("\nTotal: S/ ").append(pedido.getTotal()).append("\n")
                .append("Estado: ").append(pedido.getEstado() != null ? pedido.getEstado().getEtiqueta() : "").append("\n\n");
        if (conComprobanteAdjunto) {
            texto.append("Te adjuntamos tu comprobante de pago en PDF.\n\n");
        }
        texto.append("Podés seguir tu pedido ingresando a la tienda, en la sección \"Mis pedidos\".\n\n")
                .append("Gracias por tu compra.\nTiendaMas");
        return texto.toString();
    }
}
