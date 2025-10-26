<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");
include_once("conexion.php");

// Crear conexión
$con = new mysqli($host, $usuario, $clave, $bd);
if ($con->connect_error) {
    echo json_encode(["status" => "error", "message" => "Error de conexión"]);
    exit();
}

// Recibir ID desde POST
$id_clase = $_REQUEST['id_clase'] ?? '';

if (empty($id_clase)) {
    echo json_encode(["status" => "error", "message" => "ID de clase no recibido"]);
    exit();
}

// Validar que sea un entero válido
if (!filter_var($id_clase, FILTER_VALIDATE_INT)) {
    echo json_encode(["status" => "error", "message" => "ID inválido"]);
    exit();
}

$id_clase = (int) $id_clase;

// Preparar y ejecutar la eliminación
$stmt = $con->prepare("DELETE FROM clases WHERE id_clase = ?");
if ($stmt === false) {
    echo json_encode(["status" => "error", "message" => "Error al preparar la consulta", "error" => $con->error]);
    exit();
}

$stmt->bind_param("i", $id_clase);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode(["status" => "success", "message" => "Clase eliminada correctamente"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Clase no encontrada"]);
    }
} else {
    // Si hay error (p. ej. restricción FK), lo devolvemos para depuración
    echo json_encode(["status" => "error", "message" => "Error al eliminar", "error" => $stmt->error]);
}

$stmt->close();
$con->close();
?>
