<?php
header('Content-Type: application/json');
error_reporting(E_ALL);
ini_set('display_errors', 1);

include_once("conexion.php");
$con = new mysqli($host, $usuario, $clave, $bd);

// Verificar conexión
if ($con->connect_error) {
    die(json_encode([
        "success" => false,
        "message" => "Error de conexión a la base de datos: " . $con->connect_error
    ]));
}

if (!isset($_GET['id_usuario'])) {
    echo json_encode(["success" => false, "message" => "ID de usuario no proporcionado"]);
    exit;
}

$id_usuario = intval($_GET['id_usuario']);

// Consulta: obtener las tareas de las clases en las que el usuario está inscrito
$query = "
    SELECT 
        t.id_tarea,
        t.asunto,
        t.descripcion,
        t.fecha_inicio,
        t.fecha_fin,
        c.id_clase,
        c.nombre_clase
    FROM tareas t
    INNER JOIN clases c ON c.id_clase = t.id_clase
    INNER JOIN gestor_clases g ON g.id_clase = c.id_clase
    WHERE g.id_usuario = $id_usuario
";

$result = $con->query($query);

if (!$result) {
    echo json_encode(["success" => false, "message" => "Error en consulta SQL: " . $con->error]);
    exit;
}

$tareas = [];
while ($row = $result->fetch_assoc()) {
    $tareas[] = $row;
}

if (count($tareas) > 0) {
    echo json_encode(["success" => true, "tareas" => $tareas]);
} else {
    echo json_encode(["success" => false, "message" => "No se encontraron tareas"]);
}

$con->close();
?>
