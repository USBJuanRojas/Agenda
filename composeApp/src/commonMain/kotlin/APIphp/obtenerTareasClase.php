<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
include_once("conexion.php");

$con = new mysqli($host, $usuario, $clave, $bd);

if ($con->connect_error) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Error de conexión a la base de datos"]);
    exit;
}

$id_clase = $_REQUEST['id_clase'] ?? '';

if (empty($id_clase) || !filter_var($id_clase, FILTER_VALIDATE_INT)) {
    echo json_encode(["success" => false, "message" => "ID de clase no válido o no recibido"]);
    exit;
}

$sql = $con->prepare("
    SELECT 
        t.id_tarea,
        t.id_clase,
        t.asunto,
        t.descripcion,
        t.fecha_inicio,
        t.fecha_fin,
        t.observaciones,
        c.nombre_clase
    FROM tareas t
    INNER JOIN clases c ON t.id_clase = c.id_clase
    WHERE t.id_clase = ?
    ORDER BY t.id_tarea ASC
");

$sql->bind_param("i", $id_clase);
$sql->execute();
$result = $sql->get_result();

$tareas = [];
while ($row = $result->fetch_assoc()) {
    $tareas[] = $row;
}

if (count($tareas) > 0) {
    echo json_encode([
        "success" => true,
        "tareas" => $tareas
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
} else {
    echo json_encode([
        "success" => false,
        "message" => "No se encontraron tareas para esta clase",
        "tareas" => []
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
}

$sql->close();
$con->close();
?>
