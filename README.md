# 🍽️ Restaurant Backend - Spring Boot

Backend REST API para aplicación de restaurantes con Flutter.

## 🚀 Tecnologías

- **Spring Boot 3.5.7**
- **Java 22**
- **PostgreSQL 18.0**
- **JWT Authentication**
- **Stripe Payments**

## 📦 Requisitos

- Java 22+
- PostgreSQL 18.0
- Maven 3.9+

## ⚙️ Configuración

### 1. Variables de Entorno

Copia el archivo `.env.example` y configura tus claves de Stripe:

```bash
# Linux/Mac
export STRIPE_SECRET_KEY=sk_test_YOUR_SECRET_KEY_HERE
export STRIPE_PUBLISHABLE_KEY=pk_test_YOUR_PUBLISHABLE_KEY_HERE

# Windows PowerShell
$env:STRIPE_SECRET_KEY="sk_test_YOUR_SECRET_KEY_HERE"
$env:STRIPE_PUBLISHABLE_KEY="pk_test_YOUR_PUBLISHABLE_KEY_HERE"
```

### 2. Base de Datos

Configura PostgreSQL en `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/restaurant_db
spring.datasource.username=postgres
spring.datasource.password=123
```

### 3. Ejecutar Scripts SQL

Ejecuta los scripts en orden:

```bash
psql -U postgres -d restaurant_db -p 5433 -f UPDATE_ORDERS_PAYMENT.sql
psql -U postgres -d restaurant_db -p 5433 -f SETUP_ADMIN.sql
```

## 🏃 Ejecutar

```bash
# Compilar
./mvnw clean compile

# Ejecutar
./mvnw spring-boot:run
```

Servidor corriendo en: **http://localhost:8080**

## 📚 Endpoints

### Autenticación
- `POST /api/auth/register` - Registrar usuario
- `POST /api/auth/login` - Login
- `GET /api/auth/profile` - Perfil usuario

### Admin (requiere rol ADMIN)
- `GET /api/admin/stats` - Estadísticas generales
- `GET /api/admin/recent-orders` - Órdenes recientes
- `GET /api/admin/active-coupons` - Cupones activos

### Pagos
- `POST /api/payments` - Crear Payment Intent (Stripe)

### Órdenes
- `POST /api/orders` - Crear orden
- `GET /api/orders` - Listar órdenes del usuario
- `PUT /api/orders/{id}/status` - Actualizar estado
- `POST /api/orders/{id}/deliver` - Marcar como entregada
- `POST /api/orders/{id}/cancel` - Cancelar orden

### Restaurantes
- `GET /api/restaurants` - Listar restaurantes
- `GET /api/restaurants/{id}` - Detalle restaurante

### Productos
- `GET /api/products` - Listar productos
- `GET /api/products/{id}` - Detalle producto

## 👨‍💼 Usuario Admin por Defecto

Email: `juan@gmail.com`
Rol: `ADMIN`

## 📖 Documentación

Ver archivos markdown para más detalles:
- `ESTADOS_ORDENES_IMPLEMENTADO.md` - Estados de órdenes
- `TESTING_ADMIN_ENDPOINTS.md` - Testing endpoints admin
- `ENDPOINTS_ADMIN_BACKEND.md` - Documentación completa

## 🔐 Seguridad

- JWT para autenticación
- CORS configurado para Flutter
- Roles: USER, ADMIN
- Password encryption con BCrypt

## 📄 Licencia

MIT
