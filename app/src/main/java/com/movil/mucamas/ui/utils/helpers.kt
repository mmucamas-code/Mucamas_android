package com.movil.mucamas.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

import com.google.firebase.firestore.FirebaseFirestore
import com.movil.mucamas.ui.models.Service

fun uploadServicesToFirestore(
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val services = getSampleServices()

    services.forEach { service ->
        db.collection("services")
            .add(service)
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    onSuccess()
}



fun getSampleServices() : List<Service> {
    val servicesSeed = listOf(
        Service(
            nombre = "Limpieza general del hogar",
            icono = "cleaning",
            descripcion = "Servicio de limpieza básica para mantener tu hogar en orden. Incluye barrido, trapeado, limpieza de polvo, baños y cocina. Se espera que la vivienda esté en condiciones normales, sin acumulación extrema de suciedad.",
            precio = 60000,
            activo = true
        ),
        Service(
            nombre = "Limpieza profunda",
            icono = "deep_clean",
            descripcion = "Limpieza detallada y a fondo de todo el hogar. Incluye paredes, zócalos, baños profundos y cocina completa. Puede requerir mayor tiempo dependiendo del estado del lugar.",
            precio = 120000,
            activo = true
        ),
        Service(
            nombre = "Limpieza post-obra",
            icono = "construction_clean",
            descripcion = "Servicio especializado para eliminar residuos de construcción. Incluye limpieza de polvo, restos de pintura, vidrios y pisos. Se espera que el espacio esté desocupado para una mejor ejecución.",
            precio = 150000,
            activo = true
        ),
        Service(
            nombre = "Lavado y planchado de ropa",
            icono = "laundry",
            descripcion = "Servicio completo de lavado, secado y planchado de ropa. Incluye prendas de uso diario. Se espera que la ropa esté separada previamente.",
            precio = 50000,
            activo = true
        ),
        Service(
            nombre = "Planchado de ropa",
            icono = "iron",
            descripcion = "Planchado profesional de prendas limpias como camisas, pantalones y camisetas. No incluye lavado.",
            precio = 40000,
            activo = true
        ),
        Service(
            nombre = "Limpieza de cocina",
            icono = "kitchen",
            descripcion = "Limpieza y desinfección de la cocina. Incluye estufa, mesones, lavaplatos y gabinetes externos. No incluye eliminación de grasa extrema acumulada por largos periodos.",
            precio = 55000,
            activo = true
        ),
        Service(
            nombre = "Limpieza de baños",
            icono = "bathroom",
            descripcion = "Baños limpios y desinfectados. Incluye inodoro, lavamanos, ducha y espejos. Se requiere acceso libre al baño.",
            precio = 45000,
            activo = true
        ),
        Service(
            nombre = "Limpieza de ventanas y vidrios",
            icono = "window",
            descripcion = "Limpieza de ventanas internas y externas accesibles. No incluye trabajos en alturas peligrosas o fachadas complejas.",
            precio = 50000,
            activo = true
        ),
        Service(
            nombre = "Organización de espacios",
            icono = "organize",
            descripcion = "Organización y optimización de espacios como closets, habitaciones y alacenas. Se requiere acompañamiento del cliente para decisiones de descarte.",
            precio = 70000,
            activo = true
        ),
        Service(
            nombre = "Organización de closet",
            icono = "closet",
            descripcion = "Ordenamiento de prendas por categorías y funcionalidad. Incluye doblado y clasificación. Las decisiones finales de descarte dependen del cliente.",
            precio = 60000,
            activo = true
        ),
        Service(
            nombre = "Cuidado de adultos mayores",
            icono = "elderly",
            descripcion = "Acompañamiento y apoyo en el hogar. Incluye ayuda en aseo personal, alimentación básica y compañía. No incluye atención médica especializada.",
            precio = 90000,
            activo = true
        ),
        Service(
            nombre = "Cuidado de niños",
            icono = "child_care",
            descripcion = "Supervisión responsable y acompañamiento infantil. Incluye juegos y alimentación básica. No incluye actividades educativas formales.",
            precio = 80000,
            activo = true
        ),
        Service(
            nombre = "Cuidado de mascotas",
            icono = "pet",
            descripcion = "Atención básica para mascotas. Incluye alimentación, paseo y limpieza ligera del área. Las mascotas deben ser sociables.",
            precio = 40000,
            activo = true
        ),
        Service(
            nombre = "Paseo de perros",
            icono = "dog_walk",
            descripcion = "Paseo supervisado de 30 minutos para perros. Incluye control con correa y cuidado responsable.",
            precio = 30000,
            activo = true
        ),
        Service(
            nombre = "Cocina básica en el hogar",
            icono = "cooking",
            descripcion = "Preparación de comidas caseras sencillas como almuerzos. Los ingredientes deben ser proporcionados por el cliente.",
            precio = 70000,
            activo = true
        ),
        Service(
            nombre = "Ayuda doméstica por horas",
            icono = "hour_service",
            descripcion = "Apoyo doméstico flexible según la necesidad. Incluye limpieza, orden o ayuda general. Las tareas deben estar claramente definidas.",
            precio = 25000,
            activo = true
        ),
        Service(
            nombre = "Desinfección del hogar",
            icono = "disinfect",
            descripcion = "Desinfección de superficies, baños y cocina para un ambiente más seguro. No sustituye una limpieza profunda.",
            precio = 65000,
            activo = true
        ),
        Service(
            nombre = "Limpieza de electrodomésticos",
            icono = "appliance",
            descripcion = "Limpieza externa de electrodomésticos como nevera, horno y microondas. Deben estar desconectados previamente.",
            precio = 50000,
            activo = true
        ),
        Service(
            nombre = "Limpieza de colchones y muebles",
            icono = "sofa",
            descripcion = "Aspirado y limpieza superficial de colchones y muebles. No garantiza eliminación total de manchas profundas.",
            precio = 90000,
            activo = true
        ),
        Service(
            nombre = "Servicio integral del hogar",
            icono = "home_service",
            descripcion = "Solución completa para el hogar. Incluye limpieza, organización y apoyo general durante una jornada completa.",
            precio = 180000,
            activo = true
        ),
        Service(
            nombre = "Lavado de vehículo a domicilio",
            icono = "car_wash",
            descripcion = "Lavado exterior e interior básico de vehículo en casa del cliente. Requiere acceso a agua.",
            precio = 60000,
            activo = true
        ),
        Service(
            nombre = "Jardinería básica",
            icono = "garden",
            descripcion = "Corte de césped, riego y mantenimiento básico de plantas. No incluye poda de árboles grandes.",
            precio = 70000,
            activo = true
        ),
        Service(
            nombre = "Poda de plantas",
            icono = "plant",
            descripcion = "Poda y mantenimiento de plantas ornamentales y arbustos pequeños. No incluye retiro de desechos voluminosos.",
            precio = 60000,
            activo = true
        ),
        Service(
            nombre = "Armado de muebles",
            icono = "furniture",
            descripcion = "Armado de muebles domésticos como mesas, camas o estanterías. El mueble debe incluir manual y piezas completas.",
            precio = 80000,
            activo = true
        ),
        Service(
            nombre = "Mudanza ligera",
            icono = "moving",
            descripcion = "Apoyo en traslado de cajas y objetos livianos dentro de la misma vivienda o edificio. No incluye transporte.",
            precio = 70000,
            activo = true
        ),
        Service(
            nombre = "Acompañamiento para diligencias",
            icono = "assistant",
            descripcion = "Acompañamiento a citas, compras o diligencias personales. No incluye gastos adicionales.",
            precio = 50000,
            activo = true
        )
    )
    return servicesSeed
}

/*
fun getServiceIcon(iconName: String): ImageVector {
    return when (iconName.lowercase()) {

        // Limpieza
        "cleaning" -> Icons.Default.CleaningServices
        "deep_clean" -> Icons.Default.
        "construction_clean" -> Icons.Default.Construction
        "disinfect" -> Icons.Default.Sanitizer

        // Ropa
        "laundry" -> Icons.Default.LocalLaundryService
        "iron" -> Icons.Default.Iron

        // Cocina y baños
        "kitchen" -> Icons.Default.Kitchen
        "bathroom" -> Icons.Default.Bathtub
        "cooking" -> Icons.Default.Restaurant

        // Ventanas y electrodomésticos
        "window" -> Icons.Default.Window
        "appliance" -> Icons.Default.Microwave

        // Organización
        "organize" -> Icons.Default.Folder
        "closet" -> Icons.Default.Checkroom

        // Personas
        "elderly" -> Icons.Default.Elderly
        "child_care" -> Icons.Default.ChildCare
        "assistant" -> Icons.Default.SupportAgent

        // Mascotas
        "pet" -> Icons.Default.Pets
        "dog_walk" -> Icons.Default.DirectionsWalk

        // Tiempo / horas
        "hour_service" -> Icons.Default.AccessTime

        // Muebles y hogar
        "sofa" -> Icons.Default.Weekend
        "furniture" -> Icons.Default.Chair
        "home_service" -> Icons.Default.Home

        // Vehículos
        "car_wash" -> Icons.Default.LocalCarWash

        // Jardín
        "garden" -> Icons.Default.Yard
        "plant" -> Icons.Default.LocalFlorist

        // Mudanza
        "moving" -> Icons.Default.MoveUp

        // Fallback seguro
        else -> Icons.Default.Home
    }
}
 */