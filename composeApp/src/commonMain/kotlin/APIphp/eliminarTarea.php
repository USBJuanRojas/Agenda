<?php
include_once("conexion.php");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

$con = new mysqli($host, $usuario, $clave, $bd);

if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit;
}

$id_tarea = $_REQUEST['id_tarea'] ?? '';

if (empty($id_tarea) || !filter_var($id_tarea, FILTER_VALIDATE_INT)) {
    echo json_encode(["success" => false, "message" => "ID de tarea no válido"]);
    exit;
}

$stmt = $con->prepare("DELETE FROM tareas WHERE id_tarea = ?");
if (!$stmt) {
    echo json_encode(["success" => false, "message" => "Error en la preparación", "error" => $con->error]);
    exit;
}

$stmt->bind_param("i", $id_tarea);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode(["success" => true, "message" => "Tarea eliminada correctamente"]);
    } else {
        echo json_encode(["success" => false, "message" => "Tarea no encontrada"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Error al eliminar", "error" => $stmt->error]);
}

$stmt->close();
$con->close();
?>
