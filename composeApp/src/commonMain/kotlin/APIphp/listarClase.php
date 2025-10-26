<?php
// Encabezados
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
include_once("conexion.php");

// Crear conexión
$con = new mysqli($host, $usuario, $clave, $bd);

// Verificar conexión
if ($con->connect_error) {
    http_response_code(500);
    echo json_encode(["status" => "error", "message" => "Conexión fallida: " . $con->connect_error]);
    exit();
}

// Consulta de clases
$sql = "SELECT id_clase, nombre_clase, hora_inicio, hora_fin, lugar, id_profesor FROM clases";
$result = $con->query($sql);

$clases = [];

if ($result && $result->num_rows > 0) {
    while ($row = $result->fetch_assoc()) {
        $clases[] = $row;
    }

    echo json_encode([
        "status" => "success",
        "count" => count($clases),
        "data" => $clases
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
} else {
    echo json_encode([
        "status" => "empty",
        "message" => "No se encontraron clases"
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
}

// Cerrar conexión
$con->close();
?>
