# 📋 Informe QA — CRM Hotelero "La Fogata" 
## Análisis Situacional, Hallazgos y Plan de Mejoras

**Fecha:** 05 de Octubre de 2026  
**Revisado por:** Análisis QA Integral del Código Fuente  
**Alcance:** Backend (Laravel 13), Frontend (Blade/Tailwind) y App Flutter  

---

## 1. RESUMEN EJECUTIVO

El sistema CRM Hotelero "La Fogata" es una aplicación Laravel con roles diferenciados (Admin, Recepcionista, Limpieza, Socio), un app Flutter para socios, y cobertura de módulos esenciales para la operación hotelera y de club. El sistema posee una arquitectura sólida y convenciones bien aplicadas, con auditoría de modelos y seguridad por roles. Sin embargo, se detectaron **gaps críticos de negocio**, inconsistencias en la lógica de estados, ausencia de validaciones de disponibilidad en tiempo real y funcionalidades faltantes propias del estándar del sector hotelero.

**Nivel de Madurez Actual: 62/100** — Sistema funcional con errores de lógica de negocio que afectan la operación real.

---

## 2. ARQUITECTURA Y FORTALEZAS DETECTADAS

| Aspecto | Estado | Observación |
|---------|--------|-------------|
| Separación MVC | ✅ Correcto | Controladores, Modelos y Vistas bien separados |
| Roles y Middleware | ✅ Correcto | `CheckRole` implementado, rutas protegidas por grupo |
| Uso de Transactions DB | ✅ Correcto | `DB::transaction` en operaciones críticas (reserva, checkout) |
| Auditoría de Modelos | ✅ Correcto | Trait `Auditable` en Reserva, ComprobantePago, Socio |
| Comprobantes SUNAT | ✅ Correcto | Lógica de NC electrónica con catálogo 09, IGV correcto (18%) |
| API para App Móvil | ✅ Correcto | Endpoints separados para socios con Sanctum |
| Rack de Habitaciones | ✅ Correcto | Algoritmo de solapamiento de fechas implementado correctamente |
| Validación de RUC | ✅ Correcto | Rule `ValidRuc` personalizada para facturas |

---

## 3. HALLAZGOS CRÍTICOS (PRIORIDAD ALTA 🔴)

### BUG-001 — Inconsistencia en Estado de Membresía (Enumeración Rota)
**Archivo:** `WebSocioController.php:83` vs `RegistroAccesoController.php:37`

**Problema:** El controlador de socios guarda el estado como `'activo'` (singular, masculino), pero el de acceso QR valida contra `'activa'` (femenino). El middleware de validación de `cambiarEstado` acepta `'activa'` pero al crear un socio nuevo se guarda `'activo'`.

```php
// WebSocioController.php:83 — GUARDA:
'estado_membresia' => 'activo',   // ❌ masculino

// RegistroAccesoController.php:37 — VALIDA:
if ($socio->estado_membresia !== 'activo') { // ❌ inconsistente

// WebSocioController.php:98 — ACEPTA VÍA FORMULARIO:
'estado' => 'required|in:activa,morosa,suspendida'  // ❌ femenino
```

**Impacto:** Un socio recién afiliado NO puede acceder al club mediante QR porque su estado es `'activo'` pero el validador de acceso espera `'activa'`. Todos los socios nuevos quedan bloqueados por defecto.

---

### BUG-002 — Validación de Disponibilidad Débil en Store de Reservas
**Archivo:** `WebReservaController.php:68-73`

**Problema:** Solo verifica `estado_operativo === 'disponible'` de la habitación, pero NO verifica cruzamiento de fechas contra otras reservas existentes. Una habitación puede estar "disponible" operativamente pero tener una reserva para el mismo período.

```php
// Solo verifica estado, NO fechas:
if ($hab->estado_operativo !== 'disponible') { ... }
// Falta: verificar DetalleReserva con solapamiento de fechas
```

**Impacto:** Es posible crear reservas duplicadas para la misma habitación en las mismas fechas. RIESGO OPERATIVO CRÍTICO.

---

### BUG-003 — Correlativo de Comprobantes con Condición de Carrera (Race Condition)
**Archivo:** `WebFacturacionController.php:103`

**Problema:** El correlativo se genera con `COUNT() + 1`, lo cual NO es seguro bajo concurrencia. Dos usuarios procesando pagos simultáneamente pueden obtener el mismo correlativo.

```php
$correlativoNum = ComprobantePago::where(...)..->count() + 1; // ❌ No atómico
```

**Impacto:** Comprobantes con número duplicado son inválidos ante SUNAT. Puede generar problemas legales.

---

### BUG-004 — Camas Adicionales con Costo Hardcodeado
**Archivo:** `WebReservaController.php:103`

**Problema:** El precio de cama adicional está escrito directamente en el código como `50.00`.

```php
$precioCama = 50.00; // ❌ Hardcodeado, no configurable
```

**Impacto:** Cualquier cambio de tarifa requiere modificar el código fuente y hacer un nuevo despliegue. No aceptable para operación real.

---

### BUG-005 — No se Libera Habitación al Anular una Reserva
**Archivo:** `WebReservaController.php`

**Problema:** No existe un método `destroy` ni una función de `anular` para reservas. Si una reserva está en `check_in` y necesita anularse, la habitación quedará marcada como `'ocupada'` indefinidamente, bloqueando el inventario.

**Impacto:** Las habitaciones pueden quedar bloqueadas permanentemente del inventario.

---

### BUG-006 — Discount de Socio Hardcodeado al 10%
**Archivo:** `ApiSocioReservaController.php:125`

**Problema:** El descuento de socio por reserva desde app está fijo al 10%, sin relación con el plan de membresía del socio.

```php
$totalSocio = $total * 0.90; // ❌ 10% siempre, sin importar el plan
```

**Impacto:** Todos los socios (independientemente de si tienen plan Gold, Platinum, etc.) reciben el mismo descuento. Viola la lógica de negocio de los planes de membresía.

---

## 4. HALLAZGOS MEDIOS (PRIORIDAD MEDIA 🟡)

### ISSUE-007 — Código de Reserva no Garantiza Unicidad
**Archivo:** `WebReservaController.php:56`, `ApiSocioReservaController.php:127`

El código usa `Str::random(8)` para generar el código de reserva. No hay validación de unicidad real antes del `INSERT`. Con muchas reservas, pueden existir colisiones. Debe usarse `unique:reservas,codigo_reserva` o generación secuencial.

---

### ISSUE-008 — No Existe Módulo de Registro de Cobros de Membresía
**Análisis:** El sistema tiene `PlanMembresia` con costos y `fecha_vencimiento_cuota` en `Socio`, pero NO existe un módulo de pagos periódicos de cuotas. No hay forma de registrar que un socio pagó su cuota mensual, ni historial de pagos.

**Gap:** Funcionalidad crítica para un CRM de club.

---

### ISSUE-009 — No Hay Validación de Solapamiento en Room Move
**Archivo:** `WebReservaController.php:190`

El Room Move solo valida que el estado operativo sea `'disponible'` o `'limpieza'`, sin verificar que la nueva habitación no tenga otra reserva para esas fechas.

---

### ISSUE-010 — Dashboard KPI Incompleto
**Archivo:** `DashboardController.php`

El dashboard solo muestra ocupación actual y reservas pendientes. Faltan KPIs esenciales del sector:
- Ingresos del día / semana / mes
- RevPAR (Revenue Per Available Room)
- Check-ins y Check-outs del día
- Tasa de ocupación porcentual por categoría

---

### ISSUE-011 — Tipo de Documento en Comprobantes no Dinámico
**Archivo:** `WebFacturacionController.php:109`

```php
'cliente_tipo_documento' => $request->tipo_comprobante === 'factura' ? 'RUC' : 'DNI',
```

Asume que boletas siempre son DNI. No permite CE (Carnet de Extranjería) ni Pasaporte para turistas extranjeros en boleta — violando el estándar SUNAT para operaciones de hospedaje.

---

### ISSUE-012 — Módulo de Restaurante (Reserva de Mesa) es Estático
**Archivo:** `ApiSocioServiciosController.php:22`

Las zonas y turnos del restaurante están hardcodeados como un array PHP. No existe base de datos para esto. Si el gerente quiere agregar una nueva zona, debe modificar el código.

---

### ISSUE-013 — No Existe Módulo de Housekeeping
**Análisis General:**

El sistema maneja el estado `'limpieza'` de las habitaciones pero no existe:
- Lista de tareas de limpieza por habitación
- Asignación de limpieza a personal específico
- Confirmación de habitación lista (de limpieza → disponible) por el ama de llaves
- Inspección de habitación antes de disponibilizar

**Gap:** Funcionalidad básica de estándar hotelero.

---

### ISSUE-014 — No Existe Vista de Perfil del Socio Detallada
**Análisis:** La vista `socios/show` existe pero no incluye historial de reservas, consumos históricos, historial de accesos QR al club, ni estado de cuotas.

---

### ISSUE-015 — Control de Acceso de Observaciones en Reserva no Persistente
**Archivo:** `WebReservaController.php:store`

El campo `observaciones` se recibe en el formulario y se pasa en `$datos`, pero no está en los campos `fillable` del modelo Reserva ni en la migración, por lo que se pierde silenciosamente.

---

## 5. HALLAZGOS MENORES (PRIORIDAD BAJA 🟢)

### MINOR-016 — Falta Paginación en Módulos de Huéspedes y Habitaciones desde App
Los endpoints de la API devuelven colecciones completas sin paginación (`->get()`), lo que puede causar problemas de rendimiento cuando crezca la base de datos.

### MINOR-017 — No Hay Log de Cambios de Estado de Habitación
Cuando la limpieza o mantenimiento cambia el estado de una habitación, no hay registro de quién lo hizo ni a qué hora. El trait `Auditable` no cubre el modelo `Habitacion`.

### MINOR-018 — Validación de Password Mínima muy Débil  
`min:6` caracteres es insuficiente. El estándar de seguridad de PCI-DSS para sistemas que manejan datos de clientes y pagos requiere mínimo 8 caracteres con combinación de mayúsculas, minúsculas y números.

### MINOR-019 — Falta Manejo de Estado `no_show`
El ENUM de reservas incluye `no_show` (migración), pero no existe ninguna acción en el controlador que permita marcar una reserva como No-Show. Es un estado huérfano.

### MINOR-020 — El Módulo de Socios no Verifica Membresía Vencida Automáticamente
El campo `fecha_vencimiento_cuota` existe, pero ningún proceso verifica si la fecha venció para cambiar el estado de `'activa'` a `'morosa'` automáticamente. Se requiere un Job o Command de Laravel.

---

## 6. PLAN DE MEJORAS PROPUESTO (PRIORIZACIÓN)

### FASE 1 — Correcciones Críticas (Esta semana)
| # | Tarea | Impacto |
|---|-------|---------|
| F1-01 | Unificar ENUM `estado_membresia` a valor consistente (`activa`/`inactiva`) | Bloqueo de acceso QR resuelto |
| F1-02 | Agregar validación de solapamiento de fechas en `store` de reservas | Evitar overbooking |
| F1-03 | Reemplazar correlativo de comprobante con secuencia atómica (DB `LOCK` o Sequence) | Conformidad SUNAT |
| F1-04 | Agregar función Anular Reserva con liberación de habitación | Integridad de inventario |
| F1-05 | Guardar campo `observaciones` en reserva (migration + fillable) | Datos perdidos |

### FASE 2 — Mejoras de Negocio (Próximas 2 semanas)
| # | Tarea | Impacto |
|---|-------|---------|
| F2-01 | Módulo de Cobro de Cuotas de Membresía (historial de pagos) | CRM de Club funcional |
| F2-02 | Job Scheduler para auto-vencimiento de membresías | Automatización de negocio |
| F2-03 | Descuento de reserva ligado al plan de membresía (`descuento_alojamiento %`) | Reglas de negocio correctas |
| F2-04 | Mover precio de cama adicional a configuración en DB (tabla `tarifas_extras`) | Flexible y sin redespliegues |
| F2-05 | Tipo de documento dinámico en comprobantes (CE, Pasaporte) | Conformidad SUNAT hostelería |
| F2-06 | Dashboard mejorado: ingresos del día, RevPAR, check-ins hoy | Visibilidad gerencial |

### FASE 3 — Funcionalidades Faltantes del Estándar (1 mes)
| # | Tarea | Impacto |
|---|-------|---------|
| F3-01 | Módulo Housekeeping: lista, asignación y confirmación de limpieza | Estándar operativo hotelero |
| F3-02 | Perfil 360° del Socio: historial de reservas, accesos y cuotas | CRM completo |
| F3-03 | Activar estado `no_show` con lógica de penalización | Estándar de reservas |
| F3-04 | Gestión de zonas de restaurante desde el panel (no hardcodeado) | Autonomía del operador |
| F3-05 | Parámetros de tarifa por temporada (alta/baja) en tipos de habitación | Revenue Management |
| F3-06 | Audit log en Habitaciones (quién cambió qué estado y cuándo) | Trazabilidad operativa |

---

## 7. MÉTRICAS DE CALIDAD ACTUALES

| Dimensión | Puntuación | Observación |
|-----------|-----------|-------------|
| **Lógica de Seguridad y Roles** | 85/100 | Sólida, falta política de contraseñas |
| **Integridad de Datos (BD)** | 55/100 | Bugs BUG-001, BUG-002 y BUG-003 críticos |
| **Cobertura Funcional Hotelera** | 55/100 | Faltan Housekeeping, No-Show, Temporadas |
| **Cobertura Funcional CRM Socios** | 45/100 | Falta módulo de cobro de cuotas, perfil 360° |
| **Conformidad SUNAT** | 70/100 | Base sólida, correlativo no atómico |
| **Experiencia de Usuario Web** | 80/100 | Diseño premium, feedback visual implementado |
| **App Móvil Flutter** | 65/100 | Funcional, descuentos incorrectos, sin paginación |
| **TOTAL** | **65/100** | Sistema Beta con gaps críticos |

---

> [!CAUTION]
> Los bugs BUG-001, BUG-002 y BUG-003 deben ser corregidos antes de ir a producción real. BUG-001 impide que los socios nuevos accedan al club. BUG-002 permite overbooking. BUG-003 puede generar problemas legales con SUNAT.

> [!TIP]  
> La Fase 1 puede completarse en 2-3 días de desarrollo. Las Fases 2 y 3 son mejoras de negocio que elevarán el sistema de Beta a un producto comercialmente competitivo.

