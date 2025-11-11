<?php
include_once("conexion.php");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

$con = new mysqli($host, $usuario, $clave, $bd);

// Verificar conexión
if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión a la base de datos"]);
    exit;
}

// Capturar datos (acepta POST o GET)
$id_clase     = $_REQUEST['id_clase']     ?? '';
$nombre_clase = $_REQUEST['nombre_clase'] ?? '';
$descripcion  = $_REQUEST['descripcion']  ?? '';
$hora_inicio  = $_REQUEST['hora_inicio']  ?? '';
$hora_fin     = $_REQUEST['hora_fin']     ?? '';
$lugar        = $_REQUEST['lugar']        ?? '';
$id_profesor  = $_REQUEST['id_profesor']  ?? '';

// Validar ID
if (empty($id_clase) || !filter_var($id_clase, FILTER_VALIDATE_INT)) {
    echo json_encode(["success" => false, "message" => "ID de clase no recibido o no válido"]);
    exit;
}

// Preparar sentencia
$sql = $con->prepare("UPDATE clases SET nombre_clase=?, descripcion=?, hora_inicio=?, hora_fin=?, lugar=?, id_profesor=? WHERE id_clase=?");
if (!$sql) {
    echo json_encode(["success" => false, "message" => "Error en la preparación de la consulta", "error" => $con->error]);
    exit;
}

// Asociar parámetros
$sql->bind_param("sssssii", $nombre_clase, $descripcion, $hora_inicio, $hora_fin, $lugar, $id_profesor, $id_clase);

// Ejecutar actualización
if ($sql->execute()) {
    echo json_encode(["success" => true, "message" => "Clase modificada correctamente"]);
} else {
    echo json_encode(["success" => false, "message" => "Error al actualizar la clase", "error" => $sql->error]);
}

$sql->close();
$con->close();
?>
