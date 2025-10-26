<?php
include_once("conexion.php");
header("Content-Type: application/json");

try {
    $con = new mysqli($host, $usuario, $clave, $bd);

    if ($con->connect_error) {
        http_response_code(500);
        echo json_encode(["success" => false, "message" => "Error de conexión"]);
        exit;
    }

    $nombre = $_REQUEST['nombre'] ?? null;
    $apellido = $_REQUEST['apellido'] ?? null;
    $correo = $_REQUEST['correo'] ?? null;
    $user = $_REQUEST['user'] ?? null;
    $password = $_REQUEST['password'] ?? null;
    $id_rol = $_REQUEST['id_rol'] ?? 3; // Asignar rol por defecto si no se proporciona

    if (!$nombre || !$apellido || !$correo || !$user || !$password) {
        echo json_encode(["success" => false, "message" => "Faltan datos: {\"nombre\":\"$nombre\", \"apellido\":\"$apellido\", \"correo\":\"$correo\", \"user\":\"$user\", \"password\":\"$password\"}"]);
        exit;
    }

    // Verificar si el usuario ya existe
    $check = $con->prepare("SELECT user FROM usuarios WHERE user = ?");
    $check->bind_param("s", $user);
    $check->execute();
    $result = $check->get_result();

    if ($result->num_rows > 0) {
        echo json_encode(["success" => false, "message" => "El usuario ya existe"]);
        exit;
    }

    // Insertar nuevo usuario
    $sql = $con->prepare("INSERT INTO usuarios (nombre, apellido, correo, user, password, id_rol) VALUES (?, ?, ?, ?, ?, ?)");
    $sql->bind_param("sssssi", $nombre, $apellido, $correo, $user, $password, $id_rol);

    if ($sql->execute()) {
        echo json_encode(["success" => true, "message" => "Registro exitoso"]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al registrar"]);
    }

} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>