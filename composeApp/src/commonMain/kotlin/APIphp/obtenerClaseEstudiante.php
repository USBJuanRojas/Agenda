<?php
header("Content-Type: application/json; charset=UTF-8");
include_once("conexion.php");
$con = new mysqli($host, $usuario, $clave, $bd);

if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit;
}

// Validar parámetro
if (!isset($_GET['id_clase'])) {
    echo json_encode(["success" => false, "message" => "Falta el parámetro id_clase"]);
    exit;
}

$id_clase = intval($_GET['id_clase']);

try {
    // Consulta uniendo clases + horario_clase + profesor
    $query = "
        SELECT 
            c.id_clase, 
            c.nombre_clase, 
            c.descripcion, 
            c.hora_inicio, 
            c.hora_fin, 
            c.lugar,
            h.dias_semana,
            u.nombre AS nombre_profesor,
            u.apellido AS apellido_profesor
        FROM clases c
        INNER JOIN horario_clase h ON c.id_clase = h.id_clase
        LEFT JOIN usuarios u ON c.id_profesor = u.id_usuario
        WHERE c.id_clase = ?
    ";

    $stmt = $con->prepare($query);
    $stmt->bind_param("i", $id_clase);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($row = $result->fetch_assoc()) {
        // Mapeo de días a texto completo
        $diasMap = [
            'L' => 'Lunes',
            'M' => 'Martes',
            'X' => 'Miércoles',
            'J' => 'Jueves',
            'V' => 'Viernes',
            'S' => 'Sábado',
            'D' => 'Domingo'
        ];

        $dias = str_split($row['dias_semana']);
        $row['dias_semana_texto'] = implode(" - ", array_map(fn($d) => $diasMap[$d] ?? $d, $dias));

        echo json_encode([
            "success" => true,
            "data" => $row
        ], JSON_UNESCAPED_UNICODE);
    } else {
        echo json_encode(["success" => false, "message" => "Clase no encontrada"]);
    }

    $stmt->close();
    $con->close();
} catch (Exception $e) {
    echo json_encode([
        "success" => false,
        "message" => "Error: " . $e->getMessage()
    ]);
}
?>
