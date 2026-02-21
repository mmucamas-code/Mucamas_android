# Estructura de la Base de Datos (Firestore)

A continuación se detalla la estructura de las colecciones utilizadas en la base de datos de la aplicación.

## Colección: `servicios`

Esta colección almacena todos los servicios ofrecidos, ya sean combos o servicios individuales. El campo `es_combo` (booleano) o el campo `tipo` se puede utilizar para diferenciar entre ellos.

---

### Documento: Combo de Servicios

Representa un paquete que incluye varias tareas.

**Ejemplo de estructura:**
```json
{
  "nombre": "Combo Básico",
  "descripcion": "Ideal para mantenimiento del hogar",
  "tipo": "combo_hogar",
  "categoria_filtro": "hogar",
  "precio": 90000,
  "duracionMinutos": 240,
  "activo": true,
  "es_combo": true,
  "caracteristicas": [
    "Sala: Barrido, trapeado y sacudida general",
    "Comedor: Limpieza de mesa y sillas",
    "Habitaciones (hasta 2): Organización básica",
    "Baño (1): Limpieza de sanitario y lavamanos",
    "General: Retiro de basuras"
  ],
  "icono": "home_clean_icon"
}
```

---

### Documento: Servicio Individual / Adicional

Representa un servicio único que se puede contratar por sí solo o como un extra.

**Ejemplo de estructura:**

```json
{
  "nombre": "Lavado de ropa",
  "descripcion": "Servicio adicional de lavandería",
  "tipo": "adicional",
  "categoria_filtro": "hogar",
  "precio": 25000,
  "duracionMinutos": 60,
  "activo": true,
  "es_combo": false,
  "caracteristicas": ["2 cargas de ropa"],
  "icono": "local_laundry_service"
}
```
