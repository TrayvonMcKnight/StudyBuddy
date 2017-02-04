<?php
error_reporting(E_ALL); ini_set('display_errors', '1');

switch ($_SERVER["REQUEST_METHOD"]) {
    case "GET":  // This code is executed if extra code is being passed in through the URL or the script is called directly from a browser by name.
        DisplayPage();
        break;
    case "POST":  // This code is executed if the script is called by clicking a button or posting to the script. 
        break;
    default:  // This code is executed if neither of the above are true.
        echo $_SERVER["REQUEST_METHOD"];
}

function DisplayPage(){
	$pageContent = file_get_contents('default.html');
	echo $pageContent;
}