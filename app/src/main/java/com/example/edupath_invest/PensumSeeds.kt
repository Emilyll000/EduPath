package com.example.edupath_invest

data class PlanSeed(
    val id: Int,
    val nombre: String
)

data class MateriaSeed(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val prerequisito: String,
    val uv: Int,
    val anio: String,
    val ciclo: String,
    val planId: Int
)

object PensumSeeds {

    val planes = listOf(
        PlanSeed(id = 1, nombre = "Plan 2016"),
        PlanSeed(id = 2, nombre = "Plan 2017"),
        PlanSeed(id = 3, nombre = "Plan 2023"),
        PlanSeed(id = 4, nombre = "Plan 2024")
    )

    val materias = listOf(
        MateriaSeed(1, "CAD501", "Cálculo Diferencial", "Bachillerato", 4, "AÑO 1", "CICLO I", 4),
        MateriaSeed(2, "QUG501", "Química General", "Bachillerato", 4, "AÑO 1", "CICLO I", 4),
        MateriaSeed(3, "ANF231", "Antropología filosófica", "Bachillerato", 3, "AÑO 1", "CICLO I", 4),
        MateriaSeed(4, "PRE104", "Programación Estructurada", "Bachillerato", 4, "AÑO 1", "CICLO I", 4),

        MateriaSeed(5, "ALG501", "Álgebra Vectorial y Matrices", "Bachillerato", 4, "AÑO 1", "CICLO II", 4),
        MateriaSeed(6, "CAI501", "Cálculo Integral", "1", 4, "AÑO 1", "CICLO II", 4),
        MateriaSeed(7, "MDB104", "Modelamiento y Diseño de Base de Datos", "4", 4, "AÑO 1", "CICLO II", 4),
        MateriaSeed(8, "POO104", "Programación Orientada a Objetos", "4", 4, "AÑO 1", "CICLO II", 4),

        MateriaSeed(9, "CVV501", "Cálculo de Varias Variables", "6", 4, "AÑO 2", "CICLO III", 4),
        MateriaSeed(10, "CDP501", "Cinemática y Dinámica de Partículas", "1", 4, "AÑO 2", "CICLO III", 4),
        MateriaSeed(11, "ADS104", "Análisis y Diseño de Sistemas Informáticos", "7", 4, "AÑO 2", "CICLO III", 4),
        MateriaSeed(12, "PRD104", "Programación con Estructuras de Datos", "8", 4, "AÑO 2", "CICLO III", 4),

        MateriaSeed(13, "EDI501", "Ecuaciones Diferenciales", "9", 4, "AÑO 2", "CICLO IV", 4),
        MateriaSeed(14, "EYM501", "Electricidad y Magnetismo", "10", 4, "AÑO 2", "CICLO IV", 4),
        MateriaSeed(15, "DMD104", "Datawarehouse y Minería de Datos", "7", 4, "AÑO 2", "CICLO IV", 4),
        MateriaSeed(16, "ESA501", "Estadística Aplicada", "6", 4, "AÑO 2", "CICLO IV", 4),

        MateriaSeed(17, "ACE102", "Análisis de Circuitos Eléctricos", "14", 4, "AÑO 3", "CICLO V", 4),
        MateriaSeed(18, "GEA106", "Gestión Ambiental", "2", 4, "AÑO 3", "CICLO V", 4),
        MateriaSeed(19, "AEE106", "Análisis y Evaluación Económica", "16", 4, "AÑO 3", "CICLO V", 4),
        MateriaSeed(20, "OFC501", "Oscilaciones, Fluidos y Calor", "10", 4, "AÑO 3", "CICLO V", 4),

        MateriaSeed(21, "DDP106", "Dirección de Proyectos", "19", 4, "AÑO 3", "CICLO VI", 4),
        MateriaSeed(22, "ACO101", "Arquitectura de Computadoras", "17", 4, "AÑO 3", "CICLO VI", 4),
        MateriaSeed(23, "LIC104", "Lenguajes Interpretados en el Cliente", "15", 4, "AÑO 3", "CICLO VI", 4),
        MateriaSeed(24, "DRD101", "Diseño de Redes de Datos", "Bachillerato", 4, "AÑO 3", "CICLO VI", 4),

        MateriaSeed(25, "APN501", "Aplicación de Métodos Numéricos", "13", 4, "AÑO 4", "CICLO VII", 4),
        MateriaSeed(26, "SIO104", "Sistemas Operativos", "22", 4, "AÑO 4", "CICLO VII", 4),
        MateriaSeed(27, "LIS104", "Lenguajes Interpretados en el Servidor", "23", 4, "AÑO 4", "CICLO VII", 4),
        MateriaSeed(28, "IRD101", "Interconexión de Redes de Datos", "24", 4, "AÑO 4", "CICLO VII", 4),

        MateriaSeed(29, "PSC231", "Pensamiento Social Cristiano", "Bachillerato", 3, "AÑO 4", "CICLO VIII", 4),
        MateriaSeed(30, "INS104", "Ingeniería de Software", "21", 5, "AÑO 4", "CICLO VIII", 4),
        MateriaSeed(31, "DPS104", "Diseño y Programación de Software Multiplataforma", "27", 4, "AÑO 4", "CICLO VIII", 4),
        MateriaSeed(32, "DSS101", "Diseño de Sistemas de Seguridad para Redes de Datos", "28", 4, "AÑO 4", "CICLO VIII", 4),

        MateriaSeed(33, "NTI104", "Normalización de Tecnologías de la Información", "30", 4, "AÑO 5", "CICLO IX", 4),
        MateriaSeed(34, "CSD104", "Gestión de la Calidad del Software", "30", 5, "AÑO 5", "CICLO IX", 4),
        MateriaSeed(35, "DSM104", "Desarrollo de Software para Dispositivos Móviles", "31", 4, "AÑO 5", "CICLO IX", 4),
        MateriaSeed(36, "ASR104", "Administración e Implementación de Redes con Sistemas Operativos Propietarios", "28", 4, "AÑO 5", "CICLO IX", 4),

        MateriaSeed(37, "AUS104", "Auditoría de Sistemas", "30", 4, "AÑO 5", "CICLO X", 4),
        MateriaSeed(38, "-", "Electiva", "-", 4, "AÑO 5", "CICLO X", 4),
        MateriaSeed(39, "DES104", "Desarrollo de Software Empresarial", "31", 5, "AÑO 5", "CICLO X", 4),
        MateriaSeed(40, "IASI104", "Administración e Implementación de Redes con Sistemas Operativos Libres", "28", 4, "AÑO 5", "CICLO X", 4),
        
        MateriaSeed(41, "CAD501", "Cálculo Diferencial", "Bachillerato", 4, "AÑO 1", "CICLO I", 3),
        MateriaSeed(42, "QUG501", "Química General", "Bachillerato", 4, "AÑO 1", "CICLO I", 3),
        MateriaSeed(43, "ANF231", "Antropología filosófica", "Bachillerato", 3, "AÑO 1", "CICLO I", 3),
        MateriaSeed(44, "PRE104", "Programación Estructurada", "Bachillerato", 4, "AÑO 1", "CICLO I", 3),

        MateriaSeed(45, "ALG501", "Álgebra Vectorial y Matrices", "Bachillerato", 4, "AÑO 1", "CICLO II", 3),
        MateriaSeed(46, "CAI501", "Cálculo Integral", "1", 4, "AÑO 1", "CICLO II", 3),
        MateriaSeed(47, "MDB104", "Modelamiento y Diseño de Base de Datos", "4", 4, "AÑO 1", "CICLO II", 3),
        MateriaSeed(48, "POO104", "Programación Orientada a Objetos", "4", 4, "AÑO 1", "CICLO II", 3),

        MateriaSeed(49, "CVV501", "Cálculo de Varias Variables", "6", 4, "AÑO 2", "CICLO III", 3),
        MateriaSeed(50, "CDP501", "Cinemática y Dinámica de Partículas", "1", 4, "AÑO 2", "CICLO III", 3),
        MateriaSeed(51, "ADS104", "Análisis y Diseño de Sistemas Informáticos", "7", 4, "AÑO 2", "CICLO III", 3),
        MateriaSeed(52, "PRD104", "Programación con Estructuras de Datos", "8", 4, "AÑO 2", "CICLO III", 3),

        MateriaSeed(53, "EDI501", "Ecuaciones Diferenciales", "9", 4, "AÑO 2", "CICLO IV", 3),
        MateriaSeed(54, "EYM501", "Electricidad y Magnetismo", "10", 4, "AÑO 2", "CICLO IV", 3),
        MateriaSeed(55, "DMD104", "Datawarehouse y Minería de Datos", "7", 4, "AÑO 2", "CICLO IV", 3),
        MateriaSeed(56, "ESA501", "Estadística Aplicada", "6", 4, "AÑO 2", "CICLO IV", 3),

        MateriaSeed(57, "ACE102", "Análisis de Circuitos Eléctricos", "14", 4, "AÑO 3", "CICLO V", 3),
        MateriaSeed(58, "GEA106", "Gestión Ambiental", "2", 4, "AÑO 3", "CICLO V", 3),
        MateriaSeed(59, "AEE106", "Análisis y Evaluación Económica", "16", 4, "AÑO 3", "CICLO V", 3),
        MateriaSeed(60, "OFC501", "Oscilaciones, Fluidos y Calor", "10", 4, "AÑO 3", "CICLO V", 3),

        MateriaSeed(61, "DDP106", "Dirección de Proyectos", "19", 4, "AÑO 3", "CICLO VI", 3),
        MateriaSeed(62, "ACO101", "Arquitectura de Computadoras", "17", 4, "AÑO 3", "CICLO VI", 3),
        MateriaSeed(63, "LIC104", "Lenguajes Interpretados en el Cliente", "15", 4, "AÑO 3", "CICLO VI", 3),
        MateriaSeed(64, "DRD101", "Diseño de Redes de Datos", "Bachillerato", 4, "AÑO 3", "CICLO VI", 3),

        MateriaSeed(65, "APN501", "Aplicación de Métodos Numéricos", "13", 4, "AÑO 4", "CICLO VII", 3),
        MateriaSeed(66, "SIO104", "Sistemas Operativos", "22", 4, "AÑO 4", "CICLO VII", 3),
        MateriaSeed(67, "LIS104", "Lenguajes Interpretados en el Servidor", "23", 4, "AÑO 4", "CICLO VII", 3),
        MateriaSeed(68, "IRD101", "Interconexión de Redes de Datos", "24", 4, "AÑO 4", "CICLO VII", 3),

        MateriaSeed(69, "PSC231", "Pensamiento Social Cristiano", "Bachillerato", 3, "AÑO 4", "CICLO VIII", 3),
        MateriaSeed(70, "INS104", "Ingeniería de Software", "21", 5, "AÑO 4", "CICLO VIII", 3),
        MateriaSeed(71, "DPS104", "Diseño y Programación de Software Multiplataforma", "27", 4, "AÑO 4", "CICLO VIII", 3),
        MateriaSeed(72, "DSS101", "Diseño de Sistemas de Seguridad para Redes de Datos", "28", 4, "AÑO 4", "CICLO VIII", 3),

        MateriaSeed(73, "NTI104", "Normalización de Tecnologías de la Información", "30", 4, "AÑO 5", "CICLO IX", 3),
        MateriaSeed(74, "CSD104", "Gestión de la Calidad del Software", "30", 5, "AÑO 5", "CICLO IX", 3),
        MateriaSeed(75, "DSM104", "Desarrollo de Software para Dispositivos Móviles", "31", 4, "AÑO 5", "CICLO IX", 3),
        MateriaSeed(76, "ASR104", "Administración e Implementación de Redes con Sistemas Operativos Propietarios", "28", 4, "AÑO 5", "CICLO IX", 3),

        MateriaSeed(77, "AUS104", "Auditoría de Sistemas", "30", 4, "AÑO 5", "CICLO X", 3),
        MateriaSeed(78, "-", "Electiva", "-", 4, "AÑO 5", "CICLO X", 3),
        MateriaSeed(79, "DES104", "Desarrollo de Software Empresarial", "31", 5, "AÑO 5", "CICLO X", 3),
        MateriaSeed(80, "IASI104", "Administración e Implementación de Redes con Sistemas Operativos Libres", "28", 4, "AÑO 5", "CICLO X", 3),

        MateriaSeed(81, "CAD501", "Cálculo Diferencial", "Bach.", 4, "I AÑO", "CICLO I", 2),
        MateriaSeed(82, "QUG501", "Química General", "Bach.", 4, "I AÑO", "CICLO I", 2),
        MateriaSeed(83, "COE201", "Comunicación Oral y Escrita", "Bach.", 3, "I AÑO", "CICLO I", 2),
        MateriaSeed(84, "PRE104", "Programación Estructurada", "Bach.", 4, "I AÑO", "CICLO I", 2),

        MateriaSeed(85, "AVM501", "Álgebra Vectorial y Matrices", "Bach.", 3, "I AÑO", "CICLO II", 2),
        MateriaSeed(86, "CAI501", "Cálculo Integral", "1", 4, "I AÑO", "CICLO II", 2),
        MateriaSeed(87, "CDP501", "Cinemática y Dinámica de Partículas", "1", 4, "I AÑO", "CICLO II", 2),
        MateriaSeed(88, "POO104", "Programación Orientada a Objetos", "4", 4, "I AÑO", "CICLO II", 2),
        MateriaSeed(89, "MDB104", "Modelamiento y Diseño de Base de Datos", "4", 4, "I AÑO", "CICLO II", 2),

        MateriaSeed(90, "CVV501", "Cálculo de Varias Variables", "5, 6", 4, "II AÑO", "CICLO III", 2),
        MateriaSeed(91, "EYM501", "Electricidad y Magnetismo", "2, 6, 7", 4, "II AÑO", "CICLO III", 2),
        MateriaSeed(92, "ESA501", "Estadística Aplicada", "6", 4, "II AÑO", "CICLO III", 2),
        MateriaSeed(93, "PED104", "Programación con Estructuras de Datos", "8", 4, "II AÑO", "CICLO III", 2),
        MateriaSeed(94, "ADS104", "Análisis y Diseño de Sistemas Informáticos", "8, 9", 4, "II AÑO", "CICLO III", 2),

        MateriaSeed(95, "EDI501", "Ecuaciones Diferenciales", "10", 4, "II AÑO", "CICLO IV", 2),
        MateriaSeed(96, "CAA501", "Cálculo Avanzado", "10", 4, "II AÑO", "CICLO IV", 2),
        MateriaSeed(97, "OFC501", "Oscilaciones, Fluidos y Calor", "6, 7", 4, "II AÑO", "CICLO IV", 2),
        MateriaSeed(98, "DMD104", "Datawarehouse y Minería de Datos", "9", 4, "II AÑO", "CICLO IV", 2),
        MateriaSeed(99, "LIC104", "Lenguajes Interpretados en el Cliente", "8, 9", 4, "II AÑO", "CICLO IV", 2),

        MateriaSeed(100, "ACE102", "Análisis de Circuitos Eléctricos", "11", 4, "III AÑO", "CICLO V", 2),
        MateriaSeed(101, "GEA106", "Gestión Ambiental", "2", 4, "III AÑO", "CICLO V", 2),
        MateriaSeed(102, "AEE106", "Análisis y Evaluación Económica", "12", 4, "III AÑO", "CICLO V", 2),
        MateriaSeed(103, "ANF231", "Antropología Filosófica", "Bach.", 3, "III AÑO", "CICLO V", 2),
        MateriaSeed(104, "ACO101", "Arquitectura de Computadoras", "4, 11", 4, "III AÑO", "CICLO V", 2),

        MateriaSeed(105, "DDP106", "Dirección de Proyectos", "21, 22", 4, "III AÑO", "CICLO VI", 2),
        MateriaSeed(106, "SIO104", "Sistemas Operativos", "24", 4, "III AÑO", "CICLO VI", 2),
        MateriaSeed(107, "PSC231", "Pensamiento Social Cristiano", "Bach.", 3, "III AÑO", "CICLO VI", 2),
        MateriaSeed(108, "DRD101", "Diseño de Redes de Datos", "Bach.", 4, "III AÑO", "CICLO VI", 2),

        MateriaSeed(109, "AMN501", "Aplicación de Métodos Numéricos", "4, 15, 20", 3, "IV AÑO", "CICLO VII", 2),
        MateriaSeed(110, "ISO104", "Ingeniería de Software", "14, 25", 3, "IV AÑO", "CICLO VII", 2),
        MateriaSeed(111, "LIS104", "Lenguajes Interpretados en el Servidor", "19", 4, "IV AÑO", "CICLO VII", 2),
        MateriaSeed(112, "IRD101", "Interconexión de Redes de Datos", "28", 4, "IV AÑO", "CICLO VII", 2),

        MateriaSeed(113, "AYC104", "Autómatas y Compiladores", "13, 29", 3, "IV AÑO", "CICLO VIII", 2),
        MateriaSeed(114, "GCS104", "Gestión de la Calidad de Software", "30", 3, "IV AÑO", "CICLO VIII", 2),
        MateriaSeed(115, "DPS104", "Diseño y Programación de Software Multiplataforma", "13, 18", 4, "IV AÑO", "CICLO VIII", 2),
        MateriaSeed(116, "DSS101", "Diseño de Sistemas de Seguridad para Redes de Datos", "28, 32", 4, "IV AÑO", "CICLO VIII", 2),

        MateriaSeed(117, "NTI104", "Normalización de Tecnologías de Información", "30", 4, "V AÑO", "CICLO IX", 2),
        MateriaSeed(118, "-", "Técnica Electiva I", "**", 4, "V AÑO", "CICLO IX", 2),
        MateriaSeed(119, "DSM104", "Desarrollo de Software para Móviles", "35", 4, "V AÑO", "CICLO IX", 2),
        MateriaSeed(120, "ASR104", "Administración e Implementación de Servicios de Red con Sistemas Operativos Propietarios", "28, 32", 4, "V AÑO", "CICLO IX", 2),

        MateriaSeed(121, "AUS104", "Auditoría de Sistemas", "30", 4, "V AÑO", "CICLO X", 2),
        MateriaSeed(122, "-", "Técnica Electiva II", "**", 4, "V AÑO", "CICLO X", 2),
        MateriaSeed(123, "DSE104", "Desarrollo de Software Empresarial", "18, 31", 4, "V AÑO", "CICLO X", 2),
        MateriaSeed(124, "IASI104", "Administración e Implementación de Servicios de Red con Sistemas Operativos Libres", "28, 32", 4, "V AÑO", "CICLO X", 2)
    )
}