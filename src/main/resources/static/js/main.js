/** Filtra las filas de una tabla según el texto escrito en un buscador. */
function filtrarTabla(input, tablaId) {
    const filtro = input.value.trim().toLowerCase();
    const filas = document.querySelectorAll('#' + tablaId + ' tbody tr');
    let visibles = 0;
    filas.forEach(function (fila) {
        const coincide = fila.textContent.toLowerCase().includes(filtro);
        fila.style.display = coincide ? '' : 'none';
        if (coincide) visibles++;
    });

    const vacio = document.getElementById(tablaId + 'SinResultados');
    if (vacio) {
        vacio.style.display = (filtro && visibles === 0) ? '' : 'none';
    }
}
