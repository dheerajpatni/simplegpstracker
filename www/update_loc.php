
<?php
	//http://localhost/update_loc.php?uid=1&lat=1&lng=1&accuracy=10&time=%272016-04-03%2023:38:20%27
	include "dbconnect.php" ;
	
	$userid = $_GET["uid"];
	$lat = $_GET["lat"];
	$lng = $_GET["lng"];
	$accuracy = $_GET["accuracy"];
	$data_time = $_GET["time"];
	
	$sql = "INSERT INTO LOCATION (USER_ID,LAT,LNG,TIME,ACCURACY,LAST_MODIFIED) VALUES ('$userid'
																		,$lat
																		,$lng
																		,$data_time
																		,$accuracy
																		,$time)";
	
	$retval = mysql_query( $sql, $conn );
	if(! $retval ) {
	  die('Could not enter data: ' . mysql_error());
	} 
	echo "Entered data successfully\n";
	mysql_close($conn);
?>