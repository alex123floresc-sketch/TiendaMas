# TiendaMas — contexto del proyecto

TiendaMas es una app de e-commerce + POS + panel administrativo para una tienda de
ropa en Perú, hecha en Spring Boot + Thymeleaf. Este archivo existe para que cualquier
sesión de Claude Code retome el trabajo con el mismo contexto que se fue acumulando
en conversaciones anteriores, sin tener que redescubrirlo a golpes.

## Stack técnico

- **Java 17**, **Spring Boot 4.0.6** (`spring-boot-starter-parent`)
- **Thymeleaf** como motor de vistas (`spring.thymeleaf.cache: false` — los cambios de
  plantilla se ven en caliente sin reiniciar)
- **Spring Security** con login form-based, roles `ADMIN` / `VENDEDOR` / `CLIENTE`
  (`entity/RolUsuario.java`)
- **Spring Data JPA + Hibernate** sobre **MySQL** (`ddl-auto: update` — el esquema se
  autoactualiza, no hay migraciones formales tipo Flyway)
- **Semantic UI 2.5.0** vía CDN como base de componentes, con un design system propio
  encima en `src/main/resources/static/css/estilos.css` (~4000 líneas)
- Librerías puntuales: `openhtmltopdf` (boletas/reportes PDF), `jfreechart` (gráficos en
  PDF), `jsoup`, `html5-qrcode` (escaneo de código de barras vía cámara en POS/productos)
- Pasarela de pago real: **Culqi** (Checkout.js v4 + Charges API), opcional — sin
  `CULQUI_SECRET_KEY` el pago con tarjeta se deshabilita solo (ver `application.yml`)

## Cómo correr la app

- Puerto por defecto `9096` (`PORT` env var lo cambia). El usuario corre su propia
  instancia desde IntelliJ — **nunca la toques ni la reinicies**; si necesitás probar
  algo mientras esa instancia no está disponible, levantá una temporal en otro puerto
  (`PORT=9103 ./mvnw.cmd -o spring-boot:run`) y **apagala vos mismo** cuando termines
  de verificar.
- **Los cambios de Java (.java) NO se recargan en caliente** — hay que recompilar y
  reiniciar el proceso para que se reflejen. Los cambios de plantillas Thymeleaf y de
  CSS/JS estático sí se ven en vivo (cache de Thymeleaf desactivada). Esto ya causó
  confusión varias veces: si algo "no cambió" después de editar un `.java`, sospechá
  primero de esto antes de asumir que el código está mal.
- `app.seed.demo` (env `SEED_DEMO`, default `true`) genera datos y usuarios demo al
  arrancar fuera de perfil `prod`. Credenciales demo (`util/DataSeeder.java`):
  admin/`ADMIN_PASSWORD` o una fija de desarrollo si no está seteada, `vendedor1` /
  `vendedor123`, `vendedor2` / `vendedor123`, `cliente1` / `cliente123`, `cliente2` /
  `cliente123`.
- DB local esperada: MySQL en `localhost:3306/TiendaMas`, usuario `root` (ver
  `application.yml` para overrides por variable de entorno).
- **Para probar login/sesión/roles sin arriesgar la sesión real del usuario ni sus
  datos**: levantar una instancia temporal apuntando a una base de datos AISLADA con
  `createDatabaseIfNotExist=true` en la URL, ej.:
  `DB_URL="jdbc:mysql://localhost:3306/tiendamas_diag?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true" PORT=9111 ./mvnw.cmd -o spring-boot:run`
  — con la base vacía, `AdminPasswordSync` crea `admin`/`admin123` automáticamente
  (perfil no-prod), y `DataSeeder` siembra catálogo/clientes de demo. Después se puede
  loguear con `curl` (jar de cookies propio, `-b`/`-c`) y navegar todas las rutas
  admin/POS con esa sesión — así se verifica de verdad si un problema de login/sesión
  es un bug real de la app o no, sin usar el navegador real del usuario. Esto fue
  clave para descartar (dos veces) que el "me saca de la aplicación" fuera un bug de
  código: login + navegación por ~20 rutas admin/tienda/POS funcionó perfecto por
  `curl`, confirmando que la causa real era la contaminación de cookies entre puertos
  (ver más abajo), no la app. No hace falta borrar esa base de diagnóstico después
  (queda aislada de `TiendaMas`, no afecta nada), pero se puede si se quiere prolijo.

## Despliegue (Render)

- `Dockerfile`, `docker-compose.yml`, `render.yaml` y `DEPLOY.md` ya están en la raíz
  del repo. `render.yaml` define el Web Service (`runtime: docker`) con las env vars
  necesarias marcadas `sync: false` (las completa el usuario al crear el Blueprint
  en Render).
- **Render no ofrece MySQL administrado** (solo Postgres/Redis nativo) — decisión ya
  tomada con el usuario (2026-08-11): usar un proveedor externo de MySQL con plan
  gratuito. PlanetScale ya NO tiene free tier (lo sacaron); Aiven sí tiene un free
  tier real (1GB, sin tarjeta) — es la opción sugerida en `DEPLOY.md`. Railway es
  alternativa pero verificar su pricing vigente antes de recomendarlo, cambia seguido.
  **No migrar a PostgreSQL** salvo que el usuario lo pida explícitamente — ya se le
  preguntó y prefirió mantener MySQL con proveedor externo antes que arriesgar romper
  algo cambiando de dialecto/driver.
- La app YA está lista para "prod" sin tocar nada más: `ProdAdminSeeder` (perfil
  `prod`) crea el admin inicial solo desde `ADMIN_USERNAME`/`ADMIN_PASSWORD` (falla
  silenciosamente con un warning si no están, no inventa credenciales), y
  `WeakCredentialsGuard` (perfil `prod`) rota automáticamente cualquier cuenta
  ADMIN/VENDEDOR que todavía tenga una contraseña de fábrica conocida (`admin123`,
  `vendedor123`). `DataSeeder` (todo el catálogo/clientes de demo) está anotado
  `@Profile("!prod")`, así que **nunca corre en producción** — una base de datos
  nueva en Render arranca limpia automáticamente, sin necesidad de borrar nada a
  mano. El usuario decidió explícitamente NO tocar su base de datos LOCAL de
  desarrollo (la sigue usando tal cual, con sus datos de prueba).
- El repo es **público** en GitHub. Los valores por defecto hardcodeados en el código
  (`admin123`, `mysql123@` como fallback de `DB_PASSWORD`) son visibles para
  cualquiera — nunca asumir que son secretos; en Render hay que definir
  `ADMIN_PASSWORD`/`DB_PASSWORD` reales como variables de entorno, nunca confiar en
  los defaults.

## Estructura de paquetes (ya reorganizada, mantener este patrón)

```
controller/   — un controller por área (Tienda, Pos, Producto, Pedido, Reporte, ...)
dto/          — formularios y objetos de transferencia (XxxForm, XxxDto, ResultadoXxx)
entity/       — entidades JPA + enums de dominio (EstadoPedido, TipoDescuentoCupon, ...)
repository/   — Spring Data JPA repositories
service/      — interfaces de servicio (y alguna clase concreta utilitaria sin interfaz,
                 ej. EnvioService, PagoCulqiService, ReportePdfService, ReporteChartService,
                 ComprobantePdfService — quedan directo en service/, no en service/impl/)
service/impl/ — XxxServiceImpl implementando XxxService
config/       — seguridad, model attributes globales, async, mvc config
util/         — seeders de datos demo, utilidades (ImagenStorage, WeakCredentialsGuard...)
web/          — Carrito y CarritoItem (estado de sesión del carrito de compra)
```

Las plantillas siguen la misma lógica: `templates/<área>/index.html` +
`templates/<área>/form.html`, con `templates/layout/base.html` (admin/POS) y
`templates/layout/tienda.html` (storefront público) como layouts base vía
`th:replace`. `templates/auth/` tiene login y registro (separados del resto).

**No "limpiar" la convención de resolución de errores de Spring Boot**:
`error/404.html` + `error.html` como fallback es el mecanismo estándar, no basura.

## Seguridad / roles (SecurityConfig.java)

- `/tienda/**` público, salvo `/tienda/checkout/**`, `/tienda/pedidos/**`,
  `/tienda/perfil/**`, `/tienda/productos/*/resenas`, `/tienda/fidelidad/**` → rol
  `CLIENTE`
- `/pos/**` → `VENDEDOR` o `ADMIN`
- `/dashboard`, `/personas/**`, `/categorias/**`, `/productos/**`, `/reportes/**`,
  `/gastos/**`, `/sueldos/**`, `/usuarios/**`, `/suscriptores/**`, `/devoluciones/**`,
  y gestión de pedidos (crear/eliminar/cambiar estado) → `ADMIN`
- Login en `/login` (vista `auth/login`), registro público de clientes en `/registro`
  (vista `auth/registro`)

## Funcionalidades construidas (en orden cronológico aproximado)

Catálogo con variantes (talla/color) → POS con escaneo de código de barras → reportes
con gráficos y export a PDF → cupones de descuento → **reseñas y calificaciones** de
producto → **devoluciones y cambios (RMA)**, cliente solicita / admin resuelve →
**programa de puntos de fidelización**, canjeables por cupones personales → **pasarela
de pago real (Culqi)** en el checkout → limpieza de bugs visuales (estadísticas
amontonadas en mobile) → reorganización de paquetes/carpetas → rediseño visual completo
(login/registro, tienda, panel admin, detalle de producto) — este último todavía en
curso, ver "Estado actual" más abajo.

Ver `git log --oneline` para el detalle real; los mensajes desde
"Corrige acceso de administrador..." en adelante son descriptivos y confiables. Los
commits anteriores a esa etapa tienen mensajes poco descriptivos (`"1234317"`, `"cel"`,
etc.) — no sirven como referencia histórica, hay que mirar el diff si hace falta.

## Design system (estilos.css)

- Tokens en `:root` al inicio del archivo: `--color-navy`, `--color-navy-dark`,
  `--color-blue`, `--color-blue-light`, `--color-cream`, `--color-beige`,
  `--color-beige-dark`, `--color-white`, `--color-text`, `--color-text-muted`,
  `--color-success`. Usar estos tokens, no colores literales nuevos, salvo casos
  puntuales ya existentes (ej. rojo de "sin stock", naranja de "stock bajo").
- Prefijos de clase por área: `tienda-*` (storefront), `admin-*` (panel admin),
  `pos-*` (punto de venta), `login-*` (auth). Mantener esta convención al agregar CSS.
- Patrón de carrusel reutilizable: `.tienda-carrusel-seccion` > `.tienda-carrusel-header`
  (título + `.tienda-carrusel-flechas`) > `.tienda-carrusel` (scrollable, con
  `.tienda-carrusel-compacto` si tiene ≤5 items) > `.tienda-carrusel-card` por producto,
  usando el fragment `tienda/fragments :: tarjetaProductoContenido(${p})`. La función JS
  `desplazarCarrusel(id, direccion)` vive en `layout/tienda.html` y está disponible en
  cualquier página que use ese layout. **Usar siempre este patrón para cualquier listado
  horizontal de productos** — ya se unificó "también te puede interesar" del detalle de
  producto para no tener un grid estático distinto al resto.
- Patrón de pestañas para páginas admin largas (usado en `reportes/index.html`):
  `.reporte-tabs` (botones `.reporte-tab[data-tab]`, uno `.activo`) +
  `.reporte-tab-panel#panel-<tab>` por sección, ocultos con `style="display:none"`
  salvo el primero. El JS de cada página arma el listener de click que alterna
  `.activo`/`display`. **Si un panel tiene un `<canvas>` de Chart.js, hay que crear
  ese gráfico recién cuando el panel se muestra por primera vez** (lazy init, con una
  bandera tipo `ventasGraficosListos`), nunca en `DOMContentLoaded` a ciegas: un
  canvas dentro de un contenedor `display:none` mide 0×0 en el momento de crear el
  `Chart`, y aunque el panel se muestre después el gráfico queda roto/en blanco.
- **"tshirt icon" (y variantes) NO es un ícono válido en Semantic UI 2.5.0** — no
  existe en su hoja de íconos (verificado contra el CSS real del CDN), así que se
  renderiza vacío/invisible. Se usó por error en varios lugares para representar
  "Productos" (sidebar admin, header de /productos, accesos rápidos, tarjeta de
  reportes) y ya se corrigió a `boxes icon` (o `shopping bag icon` donde `boxes` ya
  estaba tomado por otra tarjeta en la misma grilla). Antes de usar un ícono nuevo
  que no esté ya probado en este proyecto, conviene verificarlo contra
  `https://cdnjs.cloudflare.com/ajax/libs/semantic-ui/2.5.0/components/icon.min.css`
  en vez de asumir que existe por analogía con Font Awesome.

## Trampas ya encontradas (no las vuelvas a pisar)

- **`th:if` + `th:replace` en la misma etiqueta no oculta el contenido de forma
  confiable.** Si necesitás condicionar un `th:replace`, envolvelo en
  `<th:block th:if="...">` y poné el `th:replace` en una etiqueta interna separada.
- **Semantic UI `stackable` NO existe para `.ui.statistics`** (solo Grid y Menu). Si ves
  números de estadísticas amontonados en mobile, no es un bug de tu CSS, es que Semantic
  no soporta ese modificador ahí. Fix aplicado:
  `.ui.statistics .statistic, .ui[class*="statistics"] .statistic { width: 40% !important; min-width: 0 !important; }`
  — necesita `!important` porque el selector numerado de Semantic
  (`.ui.three.statistics .statistic`) tiene más especificidad.
- **`em` como unidad de `font-size` puede heredar `font-size: 0`** de un `.ui.segment`
  padre en este proyecto (pasó con las estrellas de reseñas) — usar `px` para iconos que
  dependen de tamaño de fuente cuando estén dentro de segments.
- **`scroll-behavior: smooth` en `html`** hace que `window.scrollY` leído justo después
  de un `scrollTo()` esté desactualizado (el scroll es async/animado). Usar
  `{behavior:'instant'}` o esperar con `setTimeout` antes de leer la posición.
- **Ocultar un `<input>` con `display:none` dentro de un `<label>` sigue funcionando**
  (el click en el label activa el input igual) — patrón usado tanto en las estrellas de
  reseña como en el selector de variante de producto para evitar mostrar controles
  nativos feos sin perder accesibilidad/semántica de formulario.
- **CSRF**: los `th:action` con Spring Security lo inyectan solo; para llamadas AJAX hay
  que usar el meta `<meta name="_csrf">` / `<meta name="_csrf_header">` ya agregado en
  los layouts.

## Convenciones de colaboración con este usuario

- **Regla fija: todo cambio hecho durante una conversación se sube a GitHub** (commit +
  push a `main`) antes de cerrar el trabajo, salvo que el usuario diga lo contrario.
  Mensajes de commit descriptivos, en español, explicando el "por qué" cuando no sea
  obvio.
- El usuario prefiere que se avance de forma autónoma cuando da directivas abiertas
  ("continua mejorandolo", "sigue"). No hace falta preguntar en cada paso — sí conviene
  dar updates cortos de qué se hizo y qué sigue.
- Toda la UI y los commits van en español (la app es para un negocio peruano — moneda
  en soles "S/", textos, roles, etc.).
- Verificación preferida cuando el navegador automatizado falla repetidamente (ver
  abajo): recompilar + probar con `curl` contra una instancia temporal en otro puerto,
  en vez de insistir con la automatización de browser.

## Automatización de navegador — problemas conocidos en este entorno

- `resize_window` y el redimensionado real del viewport no son confiables; para probar
  estilos responsive, mejor inyectar el bloque `@media (max-width:Npx)` relevante vía JS
  en vez de confiar en el resize real.
- Las pestañas del navegador se "corrompen" con cierta frecuencia (screenshots que dan
  timeout, `Page.captureScreenshot` que cuelga, scroll que no responde). Cuando pasa:
  cerrar la pestaña (`tabs_close_mcp`) y abrir una nueva (`tabs_context_mcp` con
  `createIfEmpty`) en vez de insistir en la misma.
- El login automatizado por formulario es flaky de forma intermitente (el `type` no
  siempre registra las teclas en inputs con ícono de Semantic UI). Si falla repetidas
  veces tras varios intentos, no es necesariamente un bug de la app — verificar por
  `curl` que las credenciales funcionan server-side antes de asumir que algo está roto.
- **CUIDADO: las cookies de sesión NO distinguen por puerto, solo por host.** El
  navegador que controla `claude-in-chrome` es el navegador real del usuario. Si se
  levanta una instancia temporal en otro puerto (ej. 9105) y se la visita en ese mismo
  navegador mientras el usuario tiene una sesión real abierta en el puerto 9096, el
  `Set-Cookie` de la instancia temporal PISA la cookie `JSESSIONID` de "localhost" —
  y la próxima vez que el usuario use su app real, esa cookie ya no es válida ahí, así
  que Spring Security lo trata como anónimo y lo manda a `/login` sin ningún error de
  por medio. Esto pasó de verdad (2026-08-11) y el usuario lo reportó como "la página
  me saca de la aplicación". Mitigación: preferir verificación por `curl` (no toca
  cookies del navegador real) para lo que no requiera JS/visual; si hace falta
  navegador, avisar al usuario que puede necesitar volver a loguearse después, o que
  borre cookies de "localhost" si le pasa.

## Estado actual / trabajo en curso

Directiva abierta y vigente del usuario: **"cambia todo de las vistas, rediseña para
que se vea aun mas limpio y mejor posible, hacé un análisis completo de la app y
continua mejorandolo"** — no tiene un punto de fin definido, es una directiva de mejora
continua hasta que el usuario redirija explícitamente hacia otra cosa.

Ya hecho en esta pasada de rediseño:
- Menú "capas" hover en la esquina superior izquierda para saltar entre tienda/POS/admin
- App usable en mobile en las vistas de los distintos tipos de usuario
- Reorganización de paquetes .java y carpetas de templates
- Login/registro con layout de panel dividido (marca + formulario)
- Panel admin: tarjeta de KPI de ingresos y accesos rápidos con iconos
- Detalle de producto: selector de variante sin radio+swatch redundante (el swatch de
  color es ahora el propio indicador de selección), columna de imagen más alta y
  centrada para balancear el layout de dos columnas, "también te puede interesar"
  unificado al carrusel, y caja de reseñas reorganizada (título+resumen en la misma
  fila en vez de contenido apretado a la izquierda con la caja vacía a la derecha)
- Tarjetas de producto: se sacó el masonry con `columns:` (rompía el orden de lectura
  izq-a-der) y se reemplazó por `.tienda-grid` como CSS Grid real. La variedad de
  tamaños de imagen (estilo Pinterest) pasó por dos versiones: primero aleatoria por
  posición (`nth-child`) — el usuario la rechazó porque generaba alturas distintas
  *dentro de una misma categoría*, que seguía viéndose asimétrico — y terminó siendo
  **por categoría**: `Categoria.tamanoTarjeta` (enum `TamanoTarjetaCategoria`: NORMAL/
  ANCHO/ALTO/COMPACTO, editable desde `/categorias`) controla la altura de imagen de
  *todas* las tarjetas de esa categoría a la vez, vía una clase `tienda-tam-<valor>`
  puesta en el `.tienda-carrusel`/`.tienda-grid` de esa sección (ver `tienda/index.html`,
  loops de `carruseles` y `catalogoAgrupado`). Así cada categoría es internamente
  simétrica, y la variedad queda entre categorías (ej. Zapatillas ancha vs. Camisas
  alta), no dentro de una. Getter de la entidad nunca devuelve null (default NORMAL)
  para no romper categorías creadas antes de que este campo existiera.
  También se cambió `.tienda-card-img img` de `object-fit: cover` a `contain` +
  `object-position: center`, para que la foto completa quede centrada en la caja en
  vez de recortada — con fotos de producto de proporciones mixtas (retrato/paisaje,
  como las de los datos de demo) `cover` las recortaba de forma dispareja y se veía
  descentrado.
  Mismo criterio de "no dejar hueco" aplicado al collage de fotos del hero: con 1-2
  fotos disponibles (en vez de las 4 que la grilla espera), la(s) foto(s) ocupan toda
  la caja en lugar de dejar celdas vacías.
- **Panel de contenido editable** (`/contenido`, solo ADMIN): entidad `ContenidoSitio`
  (tabla `contenido_sitio`, clave/valor libre) + `ContenidoService` (devuelve un
  `Map<String,String>` con defaults hardcodeados en `ContenidoServiceImpl` para toda
  clave sin fila en la DB, así nunca queda vacío) + `ContenidoController`. El mapa se
  expone como atributo de modelo `contenido` vía `CarritoModelAttributes` (ya corre en
  toda vista de `TiendaController`/`PosController`). Cubre: banner principal (título/
  subtítulo/botón), pie de página (descripción, teléfono, email, horario, link de
  mapa, redes sociales — antes hardcodeados y algunos ni siquiera eran enlaces reales)
  y las 8 secciones de `/tienda/info`. Simplificación consciente: las secciones de
  info que antes tenían texto con links/listas embebidas (trabaja-con-nosotros,
  libro-de-reclamaciones, preguntas-frecuentes) pasaron a texto plano editable con
  `white-space:pre-line` (clase `.tienda-info-texto`) — se perdió el auto-link de
  email/teléfono dentro del cuerpo y el estilo distintivo de cada pregunta del FAQ,
  a cambio de que el admin pueda editar todo sin tocar código. Si en el futuro se
  quiere más fidelidad ahí, habría que volver a estructurar esas claves en vez de
  texto libre.

Pendiente (próximos pasos naturales si no hay redirección):
- Pasada visual sobre el resto del panel admin: listados/formularios de productos,
  pedidos, categorías, personas, usuarios, gastos, sueldos, reportes
- Pasada visual sobre POS (bloqueada hasta ahora por flakiness del login automatizado
  en esa pantalla puntual — probar primero con curl/verificación server-side)
- Revisar consistencia de `border-radius` y `box-shadow` en `estilos.css` (deuda de
  diseño detectada: valores literales inconsistentes en vez de una escala definida)
- El pedido de "todo lo que se muestra en la página web tiene que estar disponible
  para editar" se cubrió para el contenido de marketing/textos fijos (arriba). La
  gestión de productos/categorías/pedidos/cupones/etc. ya existía de antes en el
  admin — no se tocó, solo se confirmó que ya cumple ese pedido.
