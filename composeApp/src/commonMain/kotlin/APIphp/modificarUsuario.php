<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

include_once("conexion.php");
$con = new mysqli($host, $usuario, $clave, $bd);

if ($con->connect_error) {
    echo json_encode(["error" => "Error de conexión"]);
    exit();
}

$nombre = $_POST['nombre'] ?? '';
$apellido = $_POST['apellido'] ?? '';
$correo = $_POST['correo'] ?? '';
$user = $_POST['user'] ?? '';
$id_rol = $_POST['id_rol'] ?? '';

if ($user == '') {
    echo json_encode(["error" => "Usuario no especificado"]);
    exit();
}

$sql = "UPDATE usuarios SET nombre=?, apellido=?, correo=?, id_rol=? WHERE user=?";
$stmt = $con->prepare($sql);
$stmt->bind_param("sssds", $nombre, $apellido, $correo, $id_rol, $user);

if ($stmt->execute()) {
    echo json_encode(["exito" => true]);
} else {
    echo json_encode(["error" => "No se pudo actualizar"]);
}

$stmt->close();
$con->close();
?>
