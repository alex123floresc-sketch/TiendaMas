package com.tiendamas.service;

import com.tiendamas.dto.ResultadoPago;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/** Cobra pagos con tarjeta a través de Culqi (checkout.culqi.com/js/v4 en el front + API de cargos acá). */
@Service
public class PagoCulqiService {

    private static final Logger log = LoggerFactory.getLogger(PagoCulqiService.class);
    private static final String URL_CARGOS = "https://api.culqi.com/v2/charges";

    @Value("${app.culqui.public-key:}")
    private String publicKey;

    @Value("${app.culqui.secret-key:}")
    private String secretKey;

    @Autowired
    private ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public boolean estaConfigurado() {
        return secretKey != null && !secretKey.isBlank();
    }

    public String getPublicKey() {
        return publicKey;
    }

    /** Cobra el monto (en soles) usando el token que generó Culqi Checkout en el navegador del cliente. */
    public ResultadoPago cobrar(String tokenId, double montoSoles, String email, String descripcion) {
        if (!estaConfigurado()) {
            return ResultadoPago.fallo("Los pagos con tarjeta no están disponibles en este momento");
        }
        if (tokenId == null || tokenId.isBlank()) {
            return ResultadoPago.fallo("No se pudo leer los datos de la tarjeta, intenta de nuevo");
        }

        try {
            Map<String, Object> cuerpo = new LinkedHashMap<>();
            cuerpo.put("amount", Math.round(montoSoles * 100));
            cuerpo.put("currency_code", "PEN");
            cuerpo.put("email", email);
            cuerpo.put("source_id", tokenId);
            cuerpo.put("description", descripcion);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_CARGOS))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + secretKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(cuerpo)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode cuerpoRespuesta = objectMapper.readTree(response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return ResultadoPago.exito(cuerpoRespuesta.path("id").asString());
            }
            String mensaje = cuerpoRespuesta.path("user_message").asString(
                    cuerpoRespuesta.path("merchant_message").asString("No se pudo procesar el pago con tarjeta"));
            return ResultadoPago.fallo(mensaje);
        } catch (Exception e) {
            log.warn("Error al cobrar con Culqi: {}", e.getMessage());
            return ResultadoPago.fallo("No se pudo procesar el pago con tarjeta. Intenta de nuevo.");
        }
    }
}
