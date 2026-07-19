package episs.unaj.com.crudpersona.entity;

public enum TipoComprobante {
    BOLETA("B001", "Boleta de Venta Electrónica"),
    FACTURA("F001", "Factura Electrónica");

    private final String serie;
    private final String etiqueta;

    TipoComprobante(String serie, String etiqueta) {
        this.serie = serie;
        this.etiqueta = etiqueta;
    }

    public String getSerie() {
        return serie;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
