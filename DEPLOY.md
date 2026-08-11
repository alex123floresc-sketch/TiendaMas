# Desplegar TiendaMas en Render

La app ya está lista para esto (Dockerfile + `render.yaml` en la raíz del repo).
Render no ofrece MySQL como base de datos administrada, así que la base de datos
va en un proveedor externo con plan gratuito.

## 1. Base de datos MySQL (Aiven, plan gratuito)

1. Creá una cuenta en [aiven.io](https://aiven.io) (no pide tarjeta para el free tier).
2. Creá un servicio **MySQL** en el plan gratuito.
3. Cuando esté listo, copiá los datos de conexión (host, puerto, usuario, contraseña,
   nombre de la base). Aiven te da directamente una URL de conexión tipo:
   `mysql://usuario:contraseña@host:puerto/basededatos?ssl-mode=REQUIRED`
4. Armá la variable `DB_URL` en formato JDBC:
   `jdbc:mysql://host:puerto/basededatos?useSSL=true&serverTimezone=UTC&requireSSL=true`
   (`DB_USERNAME` y `DB_PASSWORD` van aparte, como variables propias.)

Otras opciones si preferís: Railway (revisá su pricing actual, ya no es 100% gratis
de por vida) o cualquier MySQL propio al que Render pueda conectarse por internet.

## 2. Desplegar en Render

1. Entrá a [render.com](https://render.com) y conectá tu cuenta de GitHub.
2. **New → Blueprint**, elegí el repo `TiendaMas`. Render va a leer `render.yaml`
   solo y va a pedirte que completes las variables marcadas como manuales:
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (del paso 1)
   - `ADMIN_USERNAME`, `ADMIN_PASSWORD` — tu cuenta de administrador real.
     **Obligatorias**: sin esto la app arranca sin ningún admin (ver
     `ProdAdminSeeder`/`WeakCredentialsGuard` en `util/`, ya preparados para esto).
   - `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` (opcional — para que salgan los
     correos de confirmación de pedido; si los dejás vacíos esa parte no funciona
     pero el resto de la tienda sí)
   - `CULQUI_PUBLIC_KEY`, `CULQUI_SECRET_KEY` (opcional — sin esto el pago con
     tarjeta se deshabilita solo, el resto del checkout funciona igual)
3. Deploy. Render construye la imagen con el `Dockerfile` y arranca el contenedor
   con `SPRING_PROFILES_ACTIVE=prod` — en ese perfil el sembrador de datos de
   demostración **no corre** (`DataSeeder` está anotado `@Profile("!prod")`), así
   que la base arranca limpia, con un solo usuario administrador.
4. Al terminar el build, Render te da una URL pública (`https://tiendamas-xxxx.onrender.com`).
   Entrá a `/login` con el `ADMIN_USERNAME`/`ADMIN_PASSWORD` que configuraste.

## Después del primer deploy

- **Cambiá la contraseña de admin** desde `/perfil` una vez adentro, aunque ya
  hayas puesto una fuerte en las variables de entorno.
- El repo de GitHub es **público** — nunca dependas de contraseñas por defecto
  del código (`admin123`, `mysql123@`, etc.) en un entorno real; esas son solo
  valores de repuesto para desarrollo local y ya quedan neutralizadas en el
  perfil `prod` mientras definas `ADMIN_PASSWORD`/`DB_PASSWORD` como variables
  de entorno reales en Render.
- El plan gratuito de Render "duerme" el servicio tras un rato sin tráfico y
  tarda unos segundos en despertar con la siguiente visita — normal en el
  free tier, no es un error.
