package episs.unaj.com.crudpersona.entity;

public enum FrecuenciaGasto {
    UNICO("Único"),
    SEMANAL("Semanal"),
    MENSUAL("Mensual");

    private final String etiqueta;

    FrecuenciaGasto(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
