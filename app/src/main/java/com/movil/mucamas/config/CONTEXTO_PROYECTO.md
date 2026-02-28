# 🧹 Proyecto Mucamas - Documentación de Contexto Técnico

Este documento sirve como referencia técnica para el análisis y desarrollo del proyecto **Mucamas**, una plataforma móvil para la gestión de servicios de limpieza a domicilio.

---

## 1. Resumen Ejecutivo
**Mucamas** es una solución integral que conecta clientes con colaboradores calificados. El sistema gestiona todo el ciclo de vida del servicio: desde la solicitud inicial y la validación de identidad hasta la asignación de personal, seguimiento en tiempo real y calificación final.

**Roles del Sistema:**
* **Cliente:** Solicita servicios, realiza pagos y califica.
* **Colaborador (Mucama):** Gestiona su disponibilidad y marca el progreso de las tareas.
* **Administrador:** Supervisa la plataforma y realiza asignaciones manuales cuando es necesario.

---

## 2. Stack Tecnológico
* **Lenguaje:** `Kotlin 2.0.x`
* **UI Framework:** `Jetpack Compose` (Modern Toolkit)
* **Arquitectura:** `MVVM` + Principios de `Clean Architecture`
* **Backend & Storage:**
    * **Firebase Firestore:** Base de datos NoSQL para persistencia en tiempo real.
    * **Firebase Storage:** Almacenamiento de comprobantes de pago y multimedia.
* **Librerías Críticas:**
    * `Navigation Compose`: Gestión de rutas y argumentos.
    * `Retrofit & OkHttp`: Integración con API externa (**EmailJS**) para flujo OTP.
    * `DataStore Preferences`: Persistencia local de sesión y preferencias.
    * `Coroutines & Flow`: Manejo de concurrencia y flujos reactivos.

---

## 3. Arquitectura y Capas
El proyecto mantiene una separación de responsabilidades estricta:

* **UI Layer:** Composables para la vista y `ViewModels`. Los ViewModels exponen un `UiState` (vía `StateFlow`) y emiten `UiEvent` (vía `SharedFlow`) para eventos de un solo disparo.
* **Domain Layer:** Contiene la lógica de negocio pura encapsulada en `UseCases` (ej. `StartOtpFlowUseCase`).
* **Data Layer:** Repositorios que funcionan como "Single Source of Truth".
    * `SessionProvider`: Singleton para estado global de la sesión.
    * `ReservationRepository`: Implementación del CRUD y transacciones en Firestore.

---

## 4. Modelo de Datos (Firestore)
### Colección: `users/`
* **Campos:** `idNumber`, `fullName`, `role` (`CLIENT`, `COLLABORATOR`, `ADMIN`), `email`, `phone`.
* **Sub-objeto:** `otp` (datos temporales para login).

### Colección: `reservations/`
* **Campos:** `clientId`, `collaboratorId`, `serviceId`.
* **Estados (`status`):** `PENDING_ASSIGNMENT`, `CONFIRMED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`.
* **Extras:** Objeto `address` embebido y lista de `ratings`.

### Colección: `collaborators/`
* **Campos:** `isAvailable` (Boolean), `availableAt` (Timestamp), estadísticas de desempeño.

---

## 5. Configuración Técnica
* **SDK:** `minSdk: 26` | `targetSdk: 36` (Android 15 Preview).
* **Build System:** `Gradle (Kotlin DSL)` con `libs.versions.toml` (Version Catalog).
* **Networking:** Requiere permisos de `INTERNET` y `ACCESS_NETWORK_STATE`.

---

## 6. Flujos Críticos de Lógica
### A. Autenticación (OTP sin contraseña)
1. El usuario solicita acceso con email.
2. Se dispara un flujo vía `EmailJS` para enviar un código único.
3. El código se valida contra el registro temporal en Firestore.
4. Éxito: Se persiste el token/sesión en `DataStore`.

### B. Ciclo de Vida de Reserva
1. **Solicitud:** El sistema filtra colaboradores por `isAvailable == true`.
2. **Asignación:** * *Automática:* Si hay match inmediato.
    * *Manual:* El Admin interviene si la reserva queda en `PENDING_ASSIGNMENT`.
3. **Ejecución:** Cambio de estados mediante triggers en la UI del Colaborador.
4. **Cierre:** Mutación de documentos para incluir `ratings` y actualizar disponibilidad del colaborador.

---

## 7. Convenciones de Desarrollo
* **Naming:** `PascalCase` para Componentes UI; `camelCase` para lógica y variables.
* **Theming:** Uso de `CompositionLocal` personalizado (`MucamasTheme`) y soporte para diseños adaptativos (`LocalAdaptiveSpecs`).
* **Reactividad:** Los repositorios **deben** exponer `Flow` o funciones `suspend` para garantizar que la UI siempre esté sincronizada.