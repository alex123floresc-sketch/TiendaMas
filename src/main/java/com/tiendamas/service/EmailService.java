package com.tiendamas.service;

import com.tiendamas.entity.Pedido;

public interface EmailService {

    /** Notifica al cliente dueño del pedido que su estado cambió. No lanza excepción si falla el envío. */
    void enviarCambioEstadoPedido(Pedido pedido);

    /** Confirma al cliente que su pedido fue registrado. No lanza excepción si falla el envío. */
    void enviarConfirmacionPedido(Pedido pedido);
}
