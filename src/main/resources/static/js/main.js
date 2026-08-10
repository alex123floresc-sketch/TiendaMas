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

/** Menú de "capas" (arriba a la izquierda): en escritorio se abre solo al pasar el mouse (CSS :hover);
 *  este script añade el equivalente por clic para pantallas táctiles, que no tienen hover. */
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.capas-menu-toggle').forEach(function (boton) {
        const contenedor = boton.closest('.capas-menu');
        if (!contenedor) return;
        boton.addEventListener('click', function (ev) {
            ev.preventDefault();
            ev.stopPropagation();
            const abierto = contenedor.classList.toggle('open');
            boton.setAttribute('aria-expanded', abierto ? 'true' : 'false');
        });
    });
    document.addEventListener('click', function (ev) {
        document.querySelectorAll('.capas-menu.open').forEach(function (contenedor) {
            if (contenedor.contains(ev.target)) return;
            contenedor.classList.remove('open');
            const boton = contenedor.querySelector('.capas-menu-toggle');
            if (boton) boton.setAttribute('aria-expanded', 'false');
        });
    });
    document.addEventListener('keydown', function (ev) {
        if (ev.key !== 'Escape') return;
        document.querySelectorAll('.capas-menu.open').forEach(function (contenedor) {
            contenedor.classList.remove('open');
        });
    });
});
