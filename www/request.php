<?php
	//http://localhost/request.php?from=1&to=2
	include "dbconnect.php" ;
	
	$uid_from = $_GET["from"];
	$uid_to = $_GET["to"];
	$action =$_GET["action"];
	
	
	$retval = Query('SELECT USER_ID from user where USER_ID='.$uid_from.' OR USER_ID='.$uid_to );
	if( mysql_num_rows($retval) < 2 )
		die('Error : User dont exist.');
	
	$retval = Query( 'SELECT ID from friends where USER_ID='.$uid_from.' AND USER_ID_FRIEND='.$uid_to );
	if( mysql_num_rows($retval) > 0 )
	{
		//die('Error : Already Friends.');
		die("SUCCESS");
	}
	
	$retval = Query( 'SELECT USER_ID_FROM from request where USER_ID_FROM='.$uid_from
																.' AND USER_ID_TO='.$uid_to );
	$is_exist = mysql_num_rows($retval);
	
	//0= friend request
	//1=friend request accepted
	//2=friend request rejected
	if( $action == 0 )
	{
		if( $is_exist > 0 ) {
			die('Error : Request is already sent.');
		}
		
		$retval = Query( 'SELECT USER_ID_FROM from request where USER_ID_FROM='.$uid_to.' AND USER_ID_TO='.$uid_from );
		if( mysql_num_rows($retval) > 0 )
			die('Error : You already have a friend request from this user.Please accept that in friend section.');
		
		$retval = Query( 'INSERT INTO request (USER_ID_FROM,USER_ID_TO,LAST_MODIFIED) VALUES ('.$uid_from.','
																.$uid_to.','
																.$time.')');
		
		die('SUCCESS');
	}
	else if( $action == 1 || $action == 2 )
	{
		if( $is_exist == 0 ) {
			die('Error : Request is not exist.');
		}
		$retval = Query('UPDATE request SET IS_ACCEPTED='. $action. ',LAST_MODIFIED='.$time
									.' WHERE USER_ID_FROM='.$uid_from.' AND USER_ID_TO='.$uid_to);
		if( $action == 1 )
		{		
			$retval = Query('INSERT INTO friends (USER_ID,USER_ID_FRIEND,LAST_MODIFIED) VALUES ('.$uid_from.','
																.$uid_to.','
																.$time.')');			
			$retval = Query('INSERT INTO friends (USER_ID,USER_ID_FRIEND,LAST_MODIFIED) VALUES ('.$uid_to.','
																.$uid_from.','
																.$time.')');			
			
			//we got new friend request from a user.get his name(email)
			$userQueryObj = Query('SELECT * from user where USER_ID='.$uid_from );
			$userArr = GetRows( $userQueryObj );
			$JsonObj['USER'] = array();
			$JsonObj['NEW_FRIEND']['user'] = $userArr[0]['USER_NAME'];
			$JsonObj['NEW_FRIEND']['mobile'] = $userArr[0]['MOBILE'];
			$JsonObj['NEW_FRIEND']['email'] = $userArr[0]['EMAIL'];
			
			print("SUCCESS ");
			echo( json_encode($JsonObj));
			

		}	
	}
	else
	{
		die( "Some unhandeled error occured");
	}
	mysql_close($conn);
?> 	