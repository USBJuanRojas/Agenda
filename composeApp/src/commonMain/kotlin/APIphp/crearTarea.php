<?php
include_once("conexion.php");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

try {
    $con = new mysqli($host, $usuario, $clave, $bd);

    if ($con->connect_error) {
        throw new Exception("Error de conexión: " . $con->connect_error);
    }

    // Capturar datos
    $id_clase = $_REQUEST['id_clase'] ?? null;
    $asunto = $_REQUEST['asunto'] ?? null;
    $descripcion = $_REQUEST['descripcion'] ?? null;
    $fecha_inicio = $_REQUEST['fecha_inicio'] ?? null;
    $fecha_fin = $_REQUEST['fecha_fin'] ?? null;
    $observaciones = $_REQUEST['observaciones'] ?? null;

    // Validar campos requeridos
    if (!$id_clase || !$asunto || !$descripcion || !$fecha_inicio || !$fecha_fin) {
        echo json_encode(["success" => false, "message" => "Faltan datos obligatorios"]);
        exit;
    }

    // Insertar tarea
    $sql = $con->prepare("INSERT INTO tareas (id_clase, asunto, descripcion, fecha_inicio, fecha_fin, observaciones) VALUES (?, ?, ?, ?, ?, ?)");
    $sql->bind_param("isssss", $id_clase, $asunto, $descripcion, $fecha_inicio, $fecha_fin, $observaciones);

    if ($sql->execute()) {
        echo json_encode([
            "success" => true,
            "message" => "Tarea creada exitosamente",
            "id_tarea" => $con->insert_id
        ]);
    } else {
        throw new Exception("Error al insertar: " . $sql->error);
    }

} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => $e->getMessage()]);
}
?>
