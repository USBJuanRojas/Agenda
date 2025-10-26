<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

include_once("conexion.php");
$con = new mysqli($host, $usuario, $clave, $bd);

// Verificar conexión
if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit;
}

// Capturar id del usuario
$id_usuario = $_GET['id_usuario'] ?? '';

if (empty($id_usuario) || !filter_var($id_usuario, FILTER_VALIDATE_INT)) {
    echo json_encode(["success" => false, "message" => "ID de usuario no válido"]);
    exit;
}

/*
 * Consulta que obtiene:
 * - Clases dictadas por el usuario (si es profesor)
 * - Clases en las que el usuario está inscrito (si es estudiante)
 * Evita duplicados usando DISTINCT
 */
$sql = "
SELECT DISTINCT 
    c.id_clase,
    c.nombre_clase,
    c.descripcion,
    c.hora_inicio,
    c.hora_fin,
    c.lugar,
    c.id_profesor,
    u.nombre AS profesor_nombre,
    u.apellido AS profesor_apellido
FROM clases c
LEFT JOIN usuarios u ON c.id_profesor = u.id_usuario
LEFT JOIN gestor_clases g ON c.id_clase = g.id_clase
WHERE c.id_profesor = ? OR g.id_usuario = ?
ORDER BY c.nombre_clase ASC
";

$stmt = $con->prepare($sql);
$stmt->bind_param("ii", $id_usuario, $id_usuario);
$stmt->execute();
$result = $stmt->get_result();

$clases = [];
while ($row = $result->fetch_assoc()) {
    $clases[] = $row;
}

if (count($clases) > 0) {
    echo json_encode(["success" => true, "clases" => $clases]);
} else {
    echo json_encode(["success" => false, "message" => "No se encontraron clases asociadas a este usuario"]);
}

$stmt->close();
$con->close();
?>
