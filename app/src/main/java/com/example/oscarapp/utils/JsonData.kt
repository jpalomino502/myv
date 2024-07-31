// JsonData.kt
package com.example.oscarapp.utils

object JsonData {
    val GRADOS_JSON = """
        [
            {"name": "Bajo", "value": "bajo", "checked": false},
            {"name": "Medio", "value": "medio", "checked": false},
            {"name": "Alto", "value": "alto", "checked": false}
        ]
    """.trimIndent()

    val EQUIPO_JSON = """
        [
            {"name": "BRAGA - BATA", "checked": false},
            {"name": "CARRETA - TAPABACOS", "checked": false},
            {"name": "GUANTES", "checked": false},
            {"name": "TAPAOIDOS", "checked": false},
            {"name": "CASCO - CORRO", "checked": false},
            {"name": "BOTAS", "checked": false},
            {"name": "GAFAS", "checked": false}
        ]
    """.trimIndent()

    val INFORME_OPTIONS_JSON = """
        [
            {"label": "B", "checked": false},
            {"label": "M", "checked": false},
            {"label": "NT", "checked": false},
            {"label": "O", "checked": false}
        ]
    """.trimIndent()
    
    val INFORME_SERVICIOS_JSON = """
        [
            {"label": "1. Espacio entre piso y puerta superior a 5 mm.", "options": $INFORME_OPTIONS_JSON},
            {"label": "2. Enchapes y pisos adecuados y en buen estado.", "options": $INFORME_OPTIONS_JSON},
            {"label": "3. Puertas y ventanas en óptimas condiciones.", "options": $INFORME_OPTIONS_JSON},
            {"label": "4. Ventanas o accesos con angeo o mallas.", "options": $INFORME_OPTIONS_JSON},
            {"label": "5. Paredes y techos herméticos.", "options": $INFORME_OPTIONS_JSON},
            {"label": "6. Muros lisos libres de ranuras.", "options": $INFORME_OPTIONS_JSON},
            {"label": "7. Media caña entre pisos y muros en buenas condiciones.", "options": $INFORME_OPTIONS_JSON},
            {"label": "8. Cubiertas, estructuras limpias y libres de nidación de plagas.", "options": $INFORME_OPTIONS_JSON},
            {"label": "9. Cuarto de averías en óptimas condiciones.", "options": $INFORME_OPTIONS_JSON},
            {"label": "10. Ventilación adecuada y en buen estado de limpieza.", "options": $INFORME_OPTIONS_JSON},
            {"label": "11. Drenajes indicados para industria de alimentación.", "options": $INFORME_OPTIONS_JSON},
            {"label": "12. Canaletas de cableado cerradas y limpias.", "options": $INFORME_OPTIONS_JSON},
            {"label": "13. Existe segregación en pisos.", "options": $INFORME_OPTIONS_JSON},
            {"label": "14. Control de malezas.", "options": $INFORME_OPTIONS_JSON},
            {"label": "15. Evidencia de madrigueras.", "options": $INFORME_OPTIONS_JSON},
            {"label": "16. Árboles y arbustos podados.", "options": $INFORME_OPTIONS_JSON},
            {"label": "17. Se observan plagas y su localización.", "options": $INFORME_OPTIONS_JSON},
            {"label": "18. Almacenamiento adecuado de productos tóxicos.", "options": $INFORME_OPTIONS_JSON},
            {"label": "19. Contenedores de basura en buenas condiciones y rotulados.", "options": $INFORME_OPTIONS_JSON},
            {"label": "20. Tapas de las alcantarillas herméticas y en buen estado.", "options": $INFORME_OPTIONS_JSON},
            {"label": "21. Cercas perimetrales adecuadas y cerradas.", "options": $INFORME_OPTIONS_JSON},
            {"label": "22. Se observan grietas en los muros exteriores.", "options": $INFORME_OPTIONS_JSON},
            {"label": "23. Fumigación de lotes aledaños.", "options": $INFORME_OPTIONS_JSON},
            {"label": "24. Sumideros y desagües tapados.", "options": $INFORME_OPTIONS_JSON},
            {"label": "25. Cuarto de aseo y útiles en orden.", "options": $INFORME_OPTIONS_JSON},
            {"label": "26. Limpieza y orden en las zonas comunes.", "options": $INFORME_OPTIONS_JSON},
            {"label": "27. Control de acceso al personal.", "options": $INFORME_OPTIONS_JSON},
            {"label": "28. Rótulos de seguridad e higiene visibles.", "options": $INFORME_OPTIONS_JSON},
            {"label": "29. Control de iluminación y condiciones adecuadas.", "options": $INFORME_OPTIONS_JSON}
        ]
    """.trimIndent()

    val JSON_DATA = """
[
    {
        "servicio": "Servicio 1",
        "plagas": [
            {"name": "Zancudos", "value": "zancudos", "checked": false},
            {"name": "Cucarachas", "value": "cucarachas", "checked": false},
            {"name": "Hormigas", "value": "hormigas", "checked": false},
            {"name": "Moscas", "value": "moscas", "checked": false},
            {"name": "Garrapatas", "value": "garrapatas", "checked": false},
            {"name": "Pulgas", "value": "pulgas", "checked": false},
            {"name": "Gusanos", "value": "gusanos", "checked": false},
            {"name": "Pitos", "value": "pitos", "checked": false},
            {"name": "Chinches", "value": "chinches", "checked": false},
            {"name": "Arañas", "value": "arañas", "checked": false},
            {"name": "Avispas", "value": "avispas", "checked": false},
            {"name": "Abeja", "value": "abeja", "checked": false}
        ],
        "metodos": [
            {"name": "Termonebulización", "value": "termonebulizacion", "checked": false},
            {"name": "Nebulización", "value": "nebulizacion", "checked": false},
            {"name": "Espolvoreo", "value": "espolvoreo", "checked": false},
            {"name": "Aspersión Manual", "value": "aspersion-manual", "checked": false},
            {"name": "Aplicación de Gel", "value": "aplicacion-de-gel", "checked": false},
            {"name": "Trampas Físicas C.M", "value": "trampas-fisicas-control-de-moscas", "checked": false},
            {"name": "Control Físico I.V.L.U", "value": "control-fisico-insectos-voladores-lampara-ulv", "checked": false},
            {"name": "Reubicación Abejas", "value": "reubicacion-abejas", "checked": false}
        ],
        "grados": $GRADOS_JSON
    },
    {
        "servicio": "Servicio 2",
        "plagas": [
            {"name": "Control Palometa", "value": "control-palometa", "checked": false},
            {"name": "Control Palomilla", "value": "control-palomilla", "checked": false}
        ],
        "metodos": [
            {"name": "Termonebulización", "value": "termonebulizacion", "checked": false},
            {"name": "Espolvoreo", "value": "espolvoreo", "checked": false},
            {"name": "Aspersión Manual", "value": "aspersion-manual", "checked": false},
            {"name": "Gasificación", "value": "grasificacion", "checked": false},
            {"name": "Carpado", "value": "carpado", "checked": false}
        ],
        "grados": $GRADOS_JSON
    },
    {
        "servicio": "Servicio 3",
        "plagas": [
            {"name": "Inmunización - Control de Comején", "value": "inmunizacion-control-de-comejen", "checked": false},
            {"name": "Termita", "value": "termita", "checked": false},
            {"name": "Gorgojo", "value": "gorgojo", "checked": false}
        ],
        "metodos": [
            {"name": "Carpado", "value": "carpado", "checked": false},
            {"name": "Brochado", "value": "brochado", "checked": false},
            {"name": "Aspersión Manual", "value": "aspersion-manual", "checked": false},
            {"name": "Inyección", "value": "inyeccion", "checked": false},
            {"name": "Perforación", "value": "perforacion", "checked": false},
            {"name": "Raspado", "value": "raspado", "checked": false},
            {"name": "Gasificación", "value": "gasificacion", "checked": false}
        ],
        "grados": $GRADOS_JSON
    },
    {
        "servicio": "Servicio 4",
        "plagas": [
            {"name": "Control de Maleza", "value": "control-de-maleza", "checked": false}
        ],
        "metodos": [
            {"name": "Aspersión Manual", "value": "aspersion-manual", "checked": false},
            {"name": "Espolvoreo", "value": "espolvoreo", "checked": false}
        ],
        "grados": $GRADOS_JSON
    },
    {
        "servicio": "Servicio 5",
        "plagas": [
            {"name": "Control de Caracoles", "value": "control-de-caracoles", "checked": false},
            {"name": "Control de Babosas", "value": "control-de-babosas", "checked": false}
        ],
        "metodos": [
            {"name": "Molusquicida", "value": "molusquicida", "checked": false}
        ],
        "grados": $GRADOS_JSON
    },
    {
        "servicio": "Servicio 6",
        "plagas": [
            {"name": "Control de Palomas", "value": "control-de-palomas", "checked": false}
        ],
        "metodos": [
            {"name": "Gel Repelente 4 the Birds de 10 Onzas", "value": "gel-repelente-4-the-birds-de-10-onzas", "checked": false},
            {"name": "Ultrasonidos", "value": "ultrasonidos", "checked": false}
        ],
        "grados": $GRADOS_JSON
    },
    {
        "servicio": "Servicio 7",
        "plagas": [
            {"name": "Control de Murciélagos", "value": "control-de-murcielagos", "checked": false}
        ],
        "metodos": [
            {"name": "Gel Repelente", "value": "gel-repelente", "checked": false},
            {"name": "Ultrasonidos", "value": "ultrasonidos", "checked": false},
            {"name": "Grasificación", "value": "gasificacion", "checked": false}
        ],
        "grados": $GRADOS_JSON
    },
    {
        "servicio": "Servicio 8",
        "plagas": [
            {"name": "Control de Ofidios", "value": "control-de-ofidios", "checked": false}
        ],
        "metodos": [
            {"name": "Estación Repelente Tipo Flauta", "value": "estacion-repelente-tipo-flauta", "checked": false},
            {"name": "Atrapa Culebras - Tenazas", "value": "atrapa-culebras-tenazas", "checked": false},
            {"name": "Trampa Adherente", "value": "trampa-adherente", "checked": false}
        ],
        "grados": $GRADOS_JSON
    },
    {
        "servicio": "Servicio 9",
        "plagas": [
            {"name": "Desinfección", "value": "desinfeccion", "checked": false}
        ],
        "metodos": [
            {"name": "Thermonebulización", "value": "termonebulizacion", "checked": false},
            {"name": "Nebulización", "value": "nebulizacion", "checked": false}
        ],
        "grados": $GRADOS_JSON
    },
    {
        "servicio": "Servicio 10",
        "plagas": [
            {"name": "Roedores (Ratones)", "value": "roedores", "checked": false},
            {"name": "Foto", "value": "no disponible"},
            {"name": "Cantidad", "value": "0"}
        ],
        "metodos": [
            {"name": "Trampa Tubo PVC", "value": "trampa-tubo-pvc", "checked": false},
            {"name": "Trampa Cajas C.", "value": "trampa-cajas-cebadero", "checked": false},
            {"name": "Trampas de I.C", "value": "trampas-de-impacto-cocodrilo", "checked": false},
            {"name": "Trampa Adherente B 48 R", "value": "trampa-adherente-bandeja-48-rnhp-bandeja", "checked": false},
            {"name": "Trampa Adherente Gato de Papel Neopreno", "value": "trampa-adherente-gato-de-papel-neopreno", "checked": false},
            {"name": "Jaulas para roedores", "value": "jaulas-para-roedores", "checked": false},
            {"name": "Ultrasonidos", "value": "ultrasonidos", "checked": false},
            {"name": "Gasificación", "value": "gasificacion", "checked": false},
            {"name": "Cebos", "value": "cebos", "checked": false},
            {"name": "Monitoreo", "value": "monitoreo", "checked": false}
        ],
        "grados": $GRADOS_JSON
    },
    {
        "servicio": "Servicio 11",
        "plagas": [
            {"name": "Lavado-Limpieza D.T", "value": "Lavado-Limpieza-Desinfección-de-tanques", "checked": false},
            {"name": "Vaciado-Lavado-D.L", "value": "Vaciado-Lavado-Desinfección-Llenado", "checked": false}
        ],
        "metodos": [
            {"name": "Monitoreo", "value": "monitoreo", "checked": false}
        ],
        "grados": $GRADOS_JSON
    },
    {
        "servicio": "Servicio 12",
        "metodos": [
            {"name": "Recarga", "value": "Recarga", "checked": false},
            {"name": "Mantenimiento", "value": "Mantenimiento", "checked": false},
            {"name": "Nuevo", "value": "Nuevo", "checked": false}
        ],
        "plagas": [
            {"name": "Extintores", "value": "Extintores", "checked": false}
        ],
        "grados": $GRADOS_JSON
    },
    {
        "servicio": "Servicio 13",
        "plagas": [
            {"name": "Limpieza de a", "value": "limpieza-de-archivo", "checked": false},
            {"name": "Limpieza de f", "value": "limpieza-de-fachada", "checked": false},
            {"name": "Limpieza general", "value": "limpieza-general", "checked": false},
            {"name": "Pintura y f", "value": "pintura-y-fachada", "checked": false}
        ],
        "metodos": [
            {"name": "Trabajo seguro en alturas vigente", "value": "Trabajo-seguro-en-alturas-vigente", "checked": false}
        ],
        "grados": $GRADOS_JSON
    }
]
""".trimIndent()
}

