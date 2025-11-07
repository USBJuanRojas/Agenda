<?php
include_once("conexion.php");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

$con = new mysqli($host, $usuario, $clave, $bd);

if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit;
}

// Capturar datos
$id_tarea = $_REQUEST['id_tarea'] ?? '';
$id_clase = $_REQUEST['id_clase'] ?? '';
$asunto = $_REQUEST['asunto'] ?? '';
$descripcion = $_REQUEST['descripcion'] ?? '';
$fecha_inicio = $_REQUEST['fecha_inicio'] ?? '';
$fecha_fin = $_REQUEST['fecha_fin'] ?? '';
$observaciones = $_REQUEST['observaciones'] ?? '';

// Validar ID
if (empty($id_tarea) || !filter_var($id_tarea, FILTER_VALIDATE_INT)) {
    echo json_encode(["success" => false, "message" => "ID de tarea no válido"]);
    exit;
}

$sql = $con->prepare("UPDATE tareas SET id_clase=?, asunto=?, descripcion=?, fecha_inicio=?, fecha_fin=?, observaciones=? WHERE id_tarea=?");
if (!$sql) {
    echo json_encode(["success" => false, "message" => "Error en la preparación", "error" => $con->error]);
    exit;
}

$sql->bind_param("isssssi", $id_clase, $asunto, $descripcion, $fecha_inicio, $fecha_fin, $observaciones, $id_tarea);

if ($sql->execute()) {
    echo json_encode(["success" => true, "message" => "Tarea actualizada correctamente"]);
} else {
    echo json_encode(["success" => false, "message" => "Error al actualizar", "error" => $sql->error]);
}

$sql->close();
$con->close();
?>
