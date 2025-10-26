<?php 
include_once("conexion.php");

header("Content-Type: application/json; charset=UTF-8");

try {
    $con = new mysqli($host, $usuario, $clave, $bd);

    if ($con->connect_error) {
        echo json_encode(["success" => false, "message" => "Conexión fallida: " . $con->connect_error]);
        exit;
    }

    // Leer parámetros JSON o GET
    $input = json_decode(file_get_contents("php://input"), true);
    $user = $input['user'] ?? $_GET['user'] ?? '';
    $password = $input['password'] ?? $_GET['password'] ?? '';

    if (empty($user) || empty($password)) {
        echo json_encode(["success" => false, "message" => "Faltan datos."]);
        exit;
    }

    // Consulta corregida según tu estructura actual
    $sql = "SELECT 
                u.id_usuario AS idUsu,
                u.nombre AS nomUsu,
                u.apellido AS apeUsu,
                r.nombre_rol AS perfil
            FROM usuarios u
            INNER JOIN roles r ON u.id_rol = r.id_rol
            WHERE u.user = ? AND u.password = ?
            LIMIT 1";

    $stmt = $con->prepare($sql);
    $stmt->bind_param("ss", $user, $password);
    $stmt->execute();
    $result = $stmt->get_result();
    $usuario = $result->fetch_assoc();

    if ($usuario) {
        echo json_encode(["success" => true] + $usuario);
    } else {
        echo json_encode(["success" => false, "message" => "Usuario o contraseña incorrectos."]);
    }

    $stmt->close();
    $con->close();

} catch (Exception $e) {
    echo json_encode(["success" => false, "message" => "Error: " . $e->getMessage()]);
}
?>