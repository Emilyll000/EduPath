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
    val planId: Int,
    val correlativo: Int
)

object PensumSeeds {

    val planes = listOf(
        PlanSeed(id = 1, nombre = "Plan 2016"),
        PlanSeed(id = 2, nombre = "Plan 2017"),
        PlanSeed(id = 3, nombre = "Plan 2023"),
        PlanSeed(id = 4, nombre = "Plan 2024")
    )

    val materias = listOf(
        MateriaSeed(1, "CAD501", "Cálculo Diferencial", "Bachillerato", 4, "AÑO 1", "CICLO I", 4,1),
        MateriaSeed(2, "QUG501", "Química General", "Bachillerato", 4, "AÑO 1", "CICLO I", 4,2),
        MateriaSeed(3, "ANF231", "Antropología filosófica", "Bachillerato", 3, "AÑO 1", "CICLO I", 4,3),
        MateriaSeed(4, "PRE104", "Programación Estructurada", "Bachillerato", 4, "AÑO 1", "CICLO I", 4,4),

        MateriaSeed(5, "ALG501", "Álgebra Vectorial y Matrices", "Bachillerato", 4, "AÑO 1", "CICLO II", 4,5),
        MateriaSeed(6, "CAI501", "Cálculo Integral", "1", 4, "AÑO 1", "CICLO II", 4,6),
        MateriaSeed(7, "MDB104", "Modelamiento y Diseño de Base de Datos", "4", 4, "AÑO 1", "CICLO II", 4,7),
        MateriaSeed(8, "POO104", "Programación Orientada a Objetos", "4", 4, "AÑO 1", "CICLO II", 4,8),

        MateriaSeed(9, "CVV501", "Cálculo de Varias Variables", "6", 4, "AÑO 2", "CICLO III", 4,9),
        MateriaSeed(10, "CDP501", "Cinemática y Dinámica de Partículas", "1", 4, "AÑO 2", "CICLO III", 4,10),
        MateriaSeed(11, "ADS104", "Análisis y Diseño de Sistemas Informáticos", "7", 4, "AÑO 2", "CICLO III", 4,11),
        MateriaSeed(12, "PRD104", "Programación con Estructuras de Datos", "8", 4, "AÑO 2", "CICLO III", 4,12),

        MateriaSeed(13, "EDI501", "Ecuaciones Diferenciales", "9", 4, "AÑO 2", "CICLO IV", 4,13),
        MateriaSeed(14, "EYM501", "Electricidad y Magnetismo", "10", 4, "AÑO 2", "CICLO IV", 4,14),
        MateriaSeed(15, "DMD104", "Datawarehouse y Minería de Datos", "7", 4, "AÑO 2", "CICLO IV", 4,15),
        MateriaSeed(16, "ESA501", "Estadística Aplicada", "6", 4, "AÑO 2", "CICLO IV", 4,16),

        MateriaSeed(17, "ACE102", "Análisis de Circuitos Eléctricos", "14", 4, "AÑO 3", "CICLO V", 4,17),
        MateriaSeed(18, "GEA106", "Gestión Ambiental", "2", 4, "AÑO 3", "CICLO V", 4,18),
        MateriaSeed(19, "AEE106", "Análisis y Evaluación Económica", "16", 4, "AÑO 3", "CICLO V", 4,19),
        MateriaSeed(20, "OFC501", "Oscilaciones, Fluidos y Calor", "10", 4, "AÑO 3", "CICLO V", 4,20),

        MateriaSeed(21, "DDP106", "Dirección de Proyectos", "19", 4, "AÑO 3", "CICLO VI", 4,21),
        MateriaSeed(22, "ACO101", "Arquitectura de Computadoras", "17", 4, "AÑO 3", "CICLO VI", 4,22),
        MateriaSeed(23, "LIC104", "Lenguajes Interpretados en el Cliente", "15", 4, "AÑO 3", "CICLO VI", 4,23),
        MateriaSeed(24, "DRD101", "Diseño de Redes de Datos", "Bachillerato", 4, "AÑO 3", "CICLO VI", 4,24),

        MateriaSeed(25, "APN501", "Aplicación de Métodos Numéricos", "13", 4, "AÑO 4", "CICLO VII", 4,25),
        MateriaSeed(26, "SIO104", "Sistemas Operativos", "22", 4, "AÑO 4", "CICLO VII", 4,26),
        MateriaSeed(27, "LIS104", "Lenguajes Interpretados en el Servidor", "23", 4, "AÑO 4", "CICLO VII", 4,27),
        MateriaSeed(28, "IRD101", "Interconexión de Redes de Datos", "24", 4, "AÑO 4", "CICLO VII", 4,28),

        MateriaSeed(29, "PSC231", "Pensamiento Social Cristiano", "Bachillerato", 3, "AÑO 4", "CICLO VIII", 4,29),
        MateriaSeed(30, "INS104", "Ingeniería de Software", "21", 5, "AÑO 4", "CICLO VIII", 4,30),
        MateriaSeed(31, "DPS104", "Diseño y Programación de Software Multiplataforma", "27", 4, "AÑO 4", "CICLO VIII", 4,31),
        MateriaSeed(32, "DSS101", "Diseño de Sistemas de Seguridad para Redes de Datos", "28", 4, "AÑO 4", "CICLO VIII", 4,32),

        MateriaSeed(33, "NTI104", "Normalización de Tecnologías de la Información", "30", 4, "AÑO 5", "CICLO IX", 4,33),
        MateriaSeed(34, "CSD104", "Gestión de la Calidad del Software", "30", 5, "AÑO 5", "CICLO IX", 4,34),
        MateriaSeed(35, "DSM104", "Desarrollo de Software para Dispositivos Móviles", "31", 4, "AÑO 5", "CICLO IX", 4,35),
        MateriaSeed(36, "ASR104", "Administración e Implementación de Redes con Sistemas Operativos Propietarios", "28", 4, "AÑO 5", "CICLO IX", 4,36),

        MateriaSeed(37, "AUS104", "Auditoría de Sistemas", "30", 4, "AÑO 5", "CICLO X", 4,37),
        MateriaSeed(38, "-", "Electiva", "33", 4, "AÑO 5", "CICLO X", 4,38),
        MateriaSeed(39, "DES104", "Desarrollo de Software Empresarial", "31", 5, "AÑO 5", "CICLO X", 4,39),
        MateriaSeed(40, "IASI104", "Administración e Implementación de Redes con Sistemas Operativos Libres", "28", 4, "AÑO 5", "CICLO X", 4,40),
        
        MateriaSeed(41, "CAD501", "Cálculo Diferencial", "Bachillerato", 4, "AÑO 1", "CICLO I", 3,1),
        MateriaSeed(42, "QUG501", "Química General", "Bachillerato", 4, "AÑO 1", "CICLO I", 3,2),
        MateriaSeed(43, "ANF231", "Antropología filosófica", "Bachillerato", 3, "AÑO 1", "CICLO I", 3,3),
        MateriaSeed(44, "PRE104", "Programación Estructurada", "Bachillerato", 4, "AÑO 1", "CICLO I", 3,4),

        MateriaSeed(45, "ALG501", "Álgebra Vectorial y Matrices", "Bachillerato", 4, "AÑO 1", "CICLO II", 3,5),
        MateriaSeed(46, "CAI501", "Cálculo Integral", "1", 4, "AÑO 1", "CICLO II", 3,6),
        MateriaSeed(47, "MDB104", "Modelamiento y Diseño de Base de Datos", "4", 4, "AÑO 1", "CICLO II", 3,7),
        MateriaSeed(48, "POO104", "Programación Orientada a Objetos", "4", 4, "AÑO 1", "CICLO II", 3,8),

        MateriaSeed(49, "CVV501", "Cálculo de Varias Variables", "6", 4, "AÑO 2", "CICLO III", 3,9),
        MateriaSeed(50, "CDP501", "Cinemática y Dinámica de Partículas", "1", 4, "AÑO 2", "CICLO III", 3,10),
        MateriaSeed(51, "ADS104", "Análisis y Diseño de Sistemas Informáticos", "7", 4, "AÑO 2", "CICLO III", 3,11),
        MateriaSeed(52, "PRD104", "Programación con Estructuras de Datos", "8", 4, "AÑO 2", "CICLO III", 3,12),

        MateriaSeed(53, "EDI501", "Ecuaciones Diferenciales", "9", 4, "AÑO 2", "CICLO IV", 3,13),
        MateriaSeed(54, "EYM501", "Electricidad y Magnetismo", "10", 4, "AÑO 2", "CICLO IV", 3,14),
        MateriaSeed(55, "DMD104", "Datawarehouse y Minería de Datos", "7", 4, "AÑO 2", "CICLO IV", 3,15),
        MateriaSeed(56, "ESA501", "Estadística Aplicada", "6", 4, "AÑO 2", "CICLO IV", 3,16),

        MateriaSeed(57, "ACE102", "Análisis de Circuitos Eléctricos", "14", 4, "AÑO 3", "CICLO V", 3,17),
        MateriaSeed(58, "GEA106", "Gestión Ambiental", "2", 4, "AÑO 3", "CICLO V", 3,18),
        MateriaSeed(59, "AEE106", "Análisis y Evaluación Económica", "16", 4, "AÑO 3", "CICLO V", 3,19),
        MateriaSeed(60, "OFC501", "Oscilaciones, Fluidos y Calor", "10", 4, "AÑO 3", "CICLO V", 3,20),

        MateriaSeed(61, "DDP106", "Dirección de Proyectos", "19", 4, "AÑO 3", "CICLO VI", 3,21),
        MateriaSeed(62, "ACO101", "Arquitectura de Computadoras", "17", 4, "AÑO 3", "CICLO VI", 3,22),
        MateriaSeed(63, "LIC104", "Lenguajes Interpretados en el Cliente", "15", 4, "AÑO 3", "CICLO VI", 3,23),
        MateriaSeed(64, "DRD101", "Diseño de Redes de Datos", "Bachillerato", 4, "AÑO 3", "CICLO VI", 3,24),

        MateriaSeed(65, "APN501", "Aplicación de Métodos Numéricos", "13", 4, "AÑO 4", "CICLO VII", 3,25),
        MateriaSeed(66, "SIO104", "Sistemas Operativos", "22", 4, "AÑO 4", "CICLO VII", 3,26),
        MateriaSeed(67, "LIS104", "Lenguajes Interpretados en el Servidor", "23", 4, "AÑO 4", "CICLO VII", 3,27),
        MateriaSeed(68, "IRD101", "Interconexión de Redes de Datos", "24", 4, "AÑO 4", "CICLO VII", 3,28),

        MateriaSeed(69, "PSC231", "Pensamiento Social Cristiano", "Bachillerato", 3, "AÑO 4", "CICLO VIII", 3,29),
        MateriaSeed(70, "INS104", "Ingeniería de Software", "21", 5, "AÑO 4", "CICLO VIII", 3,30),
        MateriaSeed(71, "DPS104", "Diseño y Programación de Software Multiplataforma", "27", 4, "AÑO 4", "CICLO VIII", 3,31),
        MateriaSeed(72, "DSS101", "Diseño de Sistemas de Seguridad para Redes de Datos", "28", 4, "AÑO 4", "CICLO VIII", 3,32),

        MateriaSeed(73, "NTI104", "Normalización de Tecnologías de la Información", "30", 4, "AÑO 5", "CICLO IX", 3,33),
        MateriaSeed(74, "CSD104", "Gestión de la Calidad del Software", "30", 5, "AÑO 5", "CICLO IX", 3,34),
        MateriaSeed(75, "DSM104", "Desarrollo de Software para Dispositivos Móviles", "31", 4, "AÑO 5", "CICLO IX", 3,35),
        MateriaSeed(76, "ASR104", "Administración e Implementación de Redes con Sistemas Operativos Propietarios", "28", 4, "AÑO 5", "CICLO IX", 3,36),

        MateriaSeed(77, "AUS104", "Auditoría de Sistemas", "30", 4, "AÑO 5", "CICLO X", 3,37),
        MateriaSeed(78, "-", "Electiva", "33", 4, "AÑO 5", "CICLO X", 3,38),
        MateriaSeed(79, "DES104", "Desarrollo de Software Empresarial", "31", 5, "AÑO 5", "CICLO X", 3,39),
        MateriaSeed(80, "IASI104", "Administración e Implementación de Redes con Sistemas Operativos Libres", "28", 4, "AÑO 5", "CICLO X", 3,40),

        MateriaSeed(81, "CAD501", "Cálculo Diferencial", "Bach.", 4, "I AÑO", "CICLO I", 2,1),
        MateriaSeed(82, "QUG501", "Química General", "Bach.", 4, "I AÑO", "CICLO I", 2,2),
        MateriaSeed(83, "COE201", "Comunicación Oral y Escrita", "Bach.", 3, "I AÑO", "CICLO I", 2,3),
        MateriaSeed(84, "PRE104", "Programación Estructurada", "Bach.", 4, "I AÑO", "CICLO I", 2,4),

        MateriaSeed(85, "AVM501", "Álgebra Vectorial y Matrices", "Bach.", 3, "I AÑO", "CICLO II", 2,5),
        MateriaSeed(86, "CAI501", "Cálculo Integral", "1", 4, "I AÑO", "CICLO II", 2,6),
        MateriaSeed(87, "CDP501", "Cinemática y Dinámica de Partículas", "1", 4, "I AÑO", "CICLO II", 2,7),
        MateriaSeed(88, "POO104", "Programación Orientada a Objetos", "4", 4, "I AÑO", "CICLO II", 2,8),
        MateriaSeed(89, "MDB104", "Modelamiento y Diseño de Base de Datos", "4", 4, "I AÑO", "CICLO II", 2,9),

        MateriaSeed(90, "CVV501", "Cálculo de Varias Variables", "5, 6", 4, "II AÑO", "CICLO III", 2,10),
        MateriaSeed(91, "EYM501", "Electricidad y Magnetismo", "2, 6, 7", 4, "II AÑO", "CICLO III", 2,11),
        MateriaSeed(92, "ESA501", "Estadística Aplicada", "6", 4, "II AÑO", "CICLO III", 2,12),
        MateriaSeed(93, "PED104", "Programación con Estructuras de Datos", "8", 4, "II AÑO", "CICLO III", 2,13),
        MateriaSeed(94, "ADS104", "Análisis y Diseño de Sistemas Informáticos", "8, 9", 4, "II AÑO", "CICLO III", 2,14),

        MateriaSeed(95, "EDI501", "Ecuaciones Diferenciales", "10", 4, "II AÑO", "CICLO IV", 2,15),
        MateriaSeed(96, "CAA501", "Cálculo Avanzado", "10", 4, "II AÑO", "CICLO IV", 2,16),
        MateriaSeed(97, "OFC501", "Oscilaciones, Fluidos y Calor", "6, 7", 4, "II AÑO", "CICLO IV", 2,17),
        MateriaSeed(98, "DMD104", "Datawarehouse y Minería de Datos", "9", 4, "II AÑO", "CICLO IV", 2,18),
        MateriaSeed(99, "LIC104", "Lenguajes Interpretados en el Cliente", "8, 9", 4, "II AÑO", "CICLO IV", 2,19),

        MateriaSeed(100, "ACE102", "Análisis de Circuitos Eléctricos", "11", 4, "III AÑO", "CICLO V", 2,20),
        MateriaSeed(101, "GEA106", "Gestión Ambiental", "2", 4, "III AÑO", "CICLO V", 2,21),
        MateriaSeed(102, "AEE106", "Análisis y Evaluación Económica", "12", 4, "III AÑO", "CICLO V", 2,22),
        MateriaSeed(103, "ANF231", "Antropología Filosófica", "Bach.", 3, "III AÑO", "CICLO V", 2,23),
        MateriaSeed(104, "ACO101", "Arquitectura de Computadoras", "4, 11", 4, "III AÑO", "CICLO V", 2,24),

        MateriaSeed(105, "DDP106", "Dirección de Proyectos", "21, 22", 4, "III AÑO", "CICLO VI", 2,25),
        MateriaSeed(106, "SIO104", "Sistemas Operativos", "24", 4, "III AÑO", "CICLO VI", 2,26),
        MateriaSeed(107, "PSC231", "Pensamiento Social Cristiano", "Bach.", 3, "III AÑO", "CICLO VI", 2,27),
        MateriaSeed(108, "DRD101", "Diseño de Redes de Datos", "Bach.", 4, "III AÑO", "CICLO VI", 2,28),

        MateriaSeed(109, "AMN501", "Aplicación de Métodos Numéricos", "4, 15, 20", 3, "IV AÑO", "CICLO VII", 2,29),
        MateriaSeed(110, "ISO104", "Ingeniería de Software", "14, 25", 3, "IV AÑO", "CICLO VII", 2,30),
        MateriaSeed(111, "LIS104", "Lenguajes Interpretados en el Servidor", "19", 4, "IV AÑO", "CICLO VII", 2,31),
        MateriaSeed(112, "IRD101", "Interconexión de Redes de Datos", "28", 4, "IV AÑO", "CICLO VII", 2,32),

        MateriaSeed(113, "AYC104", "Autómatas y Compiladores", "13, 29", 3, "IV AÑO", "CICLO VIII", 2,33),
        MateriaSeed(114, "GCS104", "Gestión de la Calidad de Software", "30", 3, "IV AÑO", "CICLO VIII", 2,34),
        MateriaSeed(115, "DPS104", "Diseño y Programación de Software Multiplataforma", "13, 18", 4, "IV AÑO", "CICLO VIII", 2,35),
        MateriaSeed(116, "DSS101", "Diseño de Sistemas de Seguridad para Redes de Datos", "28, 32", 4, "IV AÑO", "CICLO VIII", 2,36),

        MateriaSeed(117, "NTI104", "Normalización de Tecnologías de Información", "30", 4, "V AÑO", "CICLO IX", 2,37),
        MateriaSeed(118, "-", "Técnica Electiva I", "33", 4, "V AÑO", "CICLO IX", 2,38),
        MateriaSeed(119, "DSM104", "Desarrollo de Software para Móviles", "35", 4, "V AÑO", "CICLO IX", 2,39),
        MateriaSeed(120, "ASR104", "Administración e Implementación de Servicios de Red con Sistemas Operativos Propietarios", "28, 32", 4, "V AÑO", "CICLO IX", 2,40),

        MateriaSeed(121, "AUS104", "Auditoría de Sistemas", "30", 4, "V AÑO", "CICLO X", 2,41),
        MateriaSeed(122, "-", "Técnica Electiva II", "38", 4, "V AÑO", "CICLO X", 2,42),
        MateriaSeed(123, "DSE104", "Desarrollo de Software Empresarial", "18, 31", 4, "V AÑO", "CICLO X", 2,43),
        MateriaSeed(124, "IASI104", "Administración e Implementación de Servicios de Red con Sistemas Operativos Libres", "28, 32", 4, "V AÑO", "CICLO X", 2,44)
    )
}