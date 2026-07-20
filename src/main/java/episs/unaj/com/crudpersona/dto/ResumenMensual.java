package episs.unaj.com.crudpersona.dto;

public class ResumenMensual {

    private final String mes;
    private final double ventas;
    private final double gastos;
    private final double sueldos;

    public ResumenMensual(String mes, double ventas, double gastos, double sueldos) {
        this.mes = mes;
        this.ventas = ventas;
        this.gastos = gastos;
        this.sueldos = sueldos;
    }

    public String getMes() { return mes; }
    public double getVentas() { return ventas; }
    public double getGastos() { return gastos; }
    public double getSueldos() { return sueldos; }

    public double getUtilidad() {
        return ventas - gastos - sueldos;
    }
}
