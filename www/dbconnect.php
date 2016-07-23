<?php

$dbhost = 'localhost';
$dbuser = 'root';
$dbpass = '';

/* 
// Create connection
$conn = new mysqli($dbhost, $dbuser, $dbpass, '951787');
// Check connection
if ($conn->connect_error) {
    die("SQL ERROR :Connection failed: " . $conn->connect_error);
}

function Query($query,$params) {
	$stmt = null;
	try {
		global $conn;
		$stmt = $conn->prepare( $query );
		$stmt->execute($params);
	}catch(PDOException $ex){
		die( 'SQL ERROR : '+ $ex->getMessage() );
	}
	return $stmt;
}

function PrintQuery($pdoOject) {
	try {
		while ($row = $pdoOject->fetch(PDO::FETCH_ASSOC))
		{
			
		}
	}catch(PDOException $ex){
			die( 'SQL ERROR : '+ $ex->getMessage() );
	}
} */

function addQuotes($str){
    return "'$str'";
}

date_default_timezone_set('UTC');
$timeRaw = date("Y-m-d H:i:s");
$time = addQuotes( $timeRaw );

$conn = mysql_connect($dbhost, $dbuser, $dbpass);
if(! $conn ) {
  die('Could not connect: ' . mysql_error());
}  
mysql_select_db('951787');

function Query($query) {
	global $conn;
	$retval = mysql_query( $query, $conn ) or die('SQL ERROR : '. mysql_error());
	return $retval;
}
	
function PrintQuery($sqlObj) {
	while($row = mysql_fetch_assoc($sqlObj, MYSQL_ASSOC)){	//or MYSQL_NUM
		foreach($row as $cname => $cvalue){
			print "$cname: $cvalue\t";
		}
		print "\r\n";
	}
}

function GetRows($sqlObj) {
	$rows = array();
	while($row = mysql_fetch_assoc($sqlObj)){
		$rows[] = $row;
	}
	return $rows;
}



?>