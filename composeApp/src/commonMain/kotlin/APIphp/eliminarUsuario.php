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

$user = $_POST['user'] ?? '';

if (empty($user)) {
    echo json_encode(["status" => "error", "message" => "Usuario no recibido"]);
    exit();
}

// Preparar y ejecutar la eliminación
$stmt = $con->prepare("DELETE FROM usuarios WHERE user = ?");
$stmt->bind_param("s", $user);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode(["status" => "success", "message" => "Usuario eliminado correctamente"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Usuario no encontrado"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => $stmt->error]);
}

$stmt->close();
$con->close();
?>
