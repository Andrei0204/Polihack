<?php
// Setează informațiile de conectare la baza de date
$servername = "localhost"; // Numele serverului MySQL (pentru XAMPP este "localhost")
$username = "root"; // Numele de utilizator MySQL (implicit pentru XAMPP)
$password = ""; // Parola MySQL (implicit pentru XAMPP este goală)
$dbname = "profi"; // Numele bazei de date

// Creează conexiunea la baza de date
$conn = new mysqli($servername, $username, $password, $dbname);

// Verifică conexiunea
if ($conn->connect_error) {
    die("Conexiune eșuată: " . $conn->connect_error);
}

// Preia datele trimise prin formular
$name = $_POST['name'];  // Numele utilizatorului
$email = $_POST['email'];  // Emailul utilizatorului

// Pregătește interogarea SQL pentru inserarea datelor în tabel
$sql = "INSERT INTO users (name, email) VALUES ('$name', '$email')";

// Execută interogarea și verifică dacă a avut succes
if ($conn->query($sql) === TRUE) {
    echo "Datele au fost adăugate cu succes!";
} else {
    echo "Eroare: " . $sql . "<br>" . $conn->error;
}

// Închide conexiunea la baza de date
$conn->close();
?>