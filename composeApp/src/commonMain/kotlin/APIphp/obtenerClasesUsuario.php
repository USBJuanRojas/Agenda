<?php
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

include_once("conexion.php");
$con = new mysqli($host, $usuario, $clave, $bd);

if ($con->connect_error) {
    echo json_encode(["success" => false, "message" => "Error de conexión"]);
    exit;
}

$id_usuario = $_GET['id_usuario'] ?? '';

if (empty($id_usuario) || !filter_var($id_usuario, FILTER_VALIDATE_INT)) {
    echo json_encode(["success" => false, "message" => "ID de usuario no válido"]);
    exit;
}

/*
 * - Se usa LEFT JOIN para traer las clases incluso si el profesor fue eliminado (id_profesor = NULL)
 * - Se añaden validaciones para mostrar "Sin asignar" si no hay profesor.
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
    COALESCE(u.nombre, 'Sin asignar') AS profesor_nombre,
    COALESCE(u.apellido, '') AS profesor_apellido
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
    // Manejo seguro del nombre del profesor
    $row['profesor_completo'] = trim($row['profesor_nombre'] . ' ' . $row['profesor_apellido']);
    $clases[] = $row;
}

if (!empty($clases)) {
    echo json_encode(["success" => true, "clases" => $clases], JSON_UNESCAPED_UNICODE);
} else {
    echo json_encode(["success" => false, "message" => "No se encontraron clases asociadas a este usuario"]);
}

$stmt->close();
$con->close();
?>
