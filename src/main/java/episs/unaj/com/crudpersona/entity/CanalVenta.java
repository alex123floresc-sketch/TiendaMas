package episs.unaj.com.crudpersona.entity;

public enum CanalVenta {
    TIENDA_FISICA("Tienda física"),
    ONLINE("Tienda en línea");

    private final String etiqueta;

    CanalVenta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
