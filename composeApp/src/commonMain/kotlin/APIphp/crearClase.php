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

    // Capturar datos desde POST o GET
    $nombre_clase = $_REQUEST['nombre_clase'] ?? null;
    $descripcion = $_REQUEST['descripcion'] ?? null;
    $hora_inicio = $_REQUEST['hora_inicio'] ?? null;
    $hora_fin = $_REQUEST['hora_fin'] ?? null;
    $lugar = $_REQUEST['lugar'] ?? null;
    $id_profesor = $_REQUEST['id_profesor'] ?? null;

    // Validar campos vacíos
    if (!$nombre_clase || !$hora_inicio || !$hora_fin || !$lugar || !$id_profesor || !$descripcion) {
        echo json_encode([
            "success" => false,
            "message" => "Faltan datos",
            "data" => [
                "nombre_clase" => $nombre_clase,
                "descripcion" => $descripcion,
                "hora_inicio" => $hora_inicio,
                "hora_fin" => $hora_fin,
                "lugar" => $lugar,
                "id_profesor" => $id_profesor
            ]
        ]);
        exit;
    }

    // Verificar si ya existe una clase con el mismo nombre y profesor en ese horario
    $check = $con->prepare("SELECT id_clase FROM clases WHERE nombre_clase = ? AND id_profesor = ? AND hora_inicio = ? AND hora_fin = ?");
    $check->bind_param("siss", $nombre_clase, $id_profesor, $hora_inicio, $hora_fin);
    $check->execute();
    $result = $check->get_result();

    if ($result->num_rows > 0) {
        echo json_encode(["success" => false, "message" => "Ya existe una clase con los mismos datos"]);
        exit;
    }

    // Insertar nueva clase
    $sql = $con->prepare("INSERT INTO clases (nombre_clase, descripcion, hora_inicio, hora_fin, lugar, id_profesor) VALUES (?, ?, ?, ?, ?, ?)");
    $sql->bind_param("sssssi", $nombre_clase, $descripcion, $hora_inicio, $hora_fin, $lugar, $id_profesor);

    if ($sql->execute()) {
        echo json_encode([
            "success" => true,
            "message" => "Clase creada exitosamente",
            "id_clase" => $con->insert_id
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Error al registrar la clase"]);
    }

} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>