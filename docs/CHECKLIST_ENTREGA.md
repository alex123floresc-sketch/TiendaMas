# Checklist para la presentación del proyecto final — TiendaMas

Lista de verificación genérica para un proyecto integrador de carrera. Como no me pasaste una rúbrica formal de tu institución, marcá/tachá lo que no aplique en tu caso y **contrastala con lo que realmente pida tu cátedra** antes de entregar.

## 1. Documentación escrita

- [ ] Informe técnico completo (`docs/INFORME_TECNICO.md`), con todos los `[COMPLETAR: ...]` resueltos.
- [ ] Portada con datos institucionales correctos (institución, carrera, autor/es, docente, fecha).
- [ ] Índice actualizado (si lo pasás a Word, regenerar la tabla de contenidos automática).
- [ ] Objetivos generales y específicos claros y verificables.
- [ ] Capturas de pantalla reales de la aplicación funcionando (mínimo: login, panel admin, POS, tienda online, un reporte).
- [ ] Diagrama entidad-relación exportado como imagen.
- [ ] Diagrama de casos de uso (si la cátedra lo pide) exportado como imagen.
- [ ] Conclusiones personales escritas por vos (no genéricas).
- [ ] Bibliografía con las fuentes que realmente consultaste.
- [ ] Ortografía y formato revisados (títulos consistentes, numeración de páginas, mismo tipo de letra).

## 2. Código fuente

- [ ] Repositorio Git accesible para quien evalúe (público o con acceso otorgado).
- [ ] `README.md` en la raíz del proyecto con: descripción breve, cómo instalar/ejecutar, tecnologías usadas, credenciales de demo si aplica.
- [ ] Sin credenciales reales ni contraseñas en texto plano commiteadas (revisar `application.yml`: actualmente tiene usuario/contraseña de MySQL fijos — ver nota de seguridad en el informe, sección 11.2).
- [ ] Historial de commits coherente (evitar un único commit gigante si podés evitarlo).
- [ ] Código compila y arranca desde cero siguiendo el manual de instalación del informe (probarlo en limpio, idealmente en otra máquina o con la base de datos vacía).

## 3. Funcionamiento del sistema

- [ ] Datos de demostración cargan correctamente al iniciar (`DataSeeder`).
- [ ] Usuario de prueba para cada rol (ADMIN, VENDEDOR, CLIENTE) documentado y funcionando.
- [ ] Flujo completo de venta por POS probado de punta a punta.
- [ ] Flujo completo de compra en la tienda online probado de punta a punta.
- [ ] Reportes muestran datos coherentes con las ventas cargadas.
- [ ] Restricciones de acceso por rol verificadas manualmente (por ejemplo, que un CLIENTE no pueda entrar a `/pos` ni a `/productos`).

## 4. Material de presentación (defensa oral)

- [ ] Diapositivas o material de apoyo para la exposición (si se requiere defensa oral).
- [ ] Demo en vivo preparada y probada de antemano (o video de respaldo por si falla algo el día de la defensa).
- [ ] Guion breve de qué mostrar y en qué orden (por ejemplo: login → panel admin → alta de producto → venta por POS → compra online → reportes).
- [ ] Anticipar preguntas típicas del jurado: por qué elegiste esta arquitectura, qué harías distinto, cómo manejás la seguridad, qué pruebas hiciste.

## 5. Trámites administrativos

- [ ] Confirmar fecha, horario y modalidad de entrega/defensa con la institución.
- [ ] Confirmar formato de entrega exigido (PDF, impreso, plataforma virtual, etc.) y respetarlo.
- [ ] Verificar si se requiere firma de tutor/a o aval previo antes de la entrega.
- [ ] Verificar si hay un formulario o carátula oficial de la institución que reemplace la portada libre del informe.

## 6. Pendientes técnicos detectados en el relevamiento

Estos son gaps concretos que noté al leer el código y que conviene que decidas si los resolvés antes de entregar o los dejás documentados como "trabajo futuro" en el informe (sección 15):

- [ ] No hay pruebas automatizadas más allá del test de contexto por defecto (`TiendaMasApplicationTests`). Si tu cátedra evalúa testing, considerá agregar al menos algunos tests de `PedidoServiceImpl`.
- [ ] `application.yml` tiene usuario y contraseña de MySQL en texto plano dentro del repositorio.
- [ ] `ddl-auto: update` no es apto para producción real (está bien para un proyecto académico, pero conviene mencionarlo como limitación conocida en el informe).

---

**Sugerencia de uso:** and cuando termines de completar el informe, decime y te ayudo a convertirlo a Word o a generarte una versión en PDF/HTML lista para imprimir.
