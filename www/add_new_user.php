<?php
	//http://localhost/add_new_user.php?uid=2&pass=%27helloworld%27&email=%27dheerajpatni@gmail.com%27&mobile=%279711761019%27
	include "dbconnect.php" ;
	
	$userid = $_GET["uid"];
	$pass = $_GET["pass"];
	$mobile = $_GET["mobile"];
	$email = $_GET["email"];
	
	$retval = Query("SELECT USER_ID from user where USER_ID='$userid' AND PASSWORD='$pass'" );
	if( mysql_num_rows($retval) > 0 ) {
		die( "SUCCESS" );
	}
	
	Query("INSERT INTO USER (USER_ID,USER_NAME,PASSWORD,EMAIL,MOBILE) VALUES ('$userid','$username','$pass','$mobile','$email')");
	die( "SUCCESS" );
	
	mysql_close($conn);
?> 	