<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
include_once("conexion.php");

$con = new mysqli($host, $usuario, $clave, $bd);

if ($con->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Conexión fallida: " . $con->connect_error]);
    exit();
}

$sql = "
SELECT
    c.id_clase,
    c.nombre_clase,
    c.descripcion,
    c.hora_inicio,
    c.hora_fin,
    c.lugar,
    c.id_profesor,
    u.nombre AS profesor_nombre,
    u.apellido AS profesor_apellido
FROM clases c
LEFT JOIN usuarios u ON c.id_profesor = u.id_usuario
ORDER BY c.nombre_clase ASC
";

$result = $con->query($sql);

$clases = [];

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $clases[] = $row;
    }

    echo json_encode([
        "success" => true,
        "message" => "Clases obtenidas correctamente",
        "clases" => $clases
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
} else {
    echo json_encode([
        "success" => false,
        "message" => "No se encontraron clases",
        "clases" => []
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
}

$con->close();
?>
