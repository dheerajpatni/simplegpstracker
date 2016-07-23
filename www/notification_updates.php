<?php
//http://localhost/notification_updates.php?userid=8975998869
include "dbconnect.php" ;

$lastUpdatedTime = $_GET["last_updated_time"];
$user = $_GET["userid"];


$JsonObj = array();
$JsonObj['NOTIFICATION_TILL'] = $timeRaw;
$JsonObj['NEW_REQ'] = array();
$JsonObj['FRIENDS'] = array();
$JsonObj['FRIENDS_POS'] = array();

$retval = Query("SELECT * from request where USER_ID_TO='$user' AND LAST_MODIFIED > '$lastUpdatedTime'" );
$outArr = GetRows( $retval );
$k = 0;
foreach($outArr as $var )
{
	if(  $var['IS_ACCEPTED'] == null || $var['IS_ACCEPTED'] == 0 )
	{
		//we got new friend request from a user.get his name(email)
		$userQueryObj = Query("SELECT * from user where USER_ID=".$var['USER_ID_FROM']);
		$userArr = GetRows( $userQueryObj );
		$JsonObj['NEW_REQ'][$k] = array();
		$JsonObj['NEW_REQ'][$k]['user'] = $userArr[0]['USER_NAME'];
		$JsonObj['NEW_REQ'][$k]['mobile'] = $userArr[0]['MOBILE'];
		$k++;
	}
}

/*
//new friend added
$retval = Query("SELECT USER_ID_FRIEND from friends where USER_ID='$user' AND LAST_MODIFIED > '$lastUpdatedTime'");
$outArr = GetRows( $retval );
$k = 0;
foreach($outArr as $var )
{
	//we got new friend request from a user.get his name(email)
	$userQueryObj = Query("SELECT * from user where USER_ID=".$var['USER_ID_FRIEND']);
	$userArr = GetRows( $userQueryObj );
	$JsonObj['FRIENDS'][$k] = array();
	$JsonObj['FRIENDS'][$k]['user'] = $userArr[0]['USER_NAME'];
	$JsonObj['FRIENDS'][$k]['mobile'] = $userArr[0]['MOBILE'];
	$JsonObj['FRIENDS'][$k]['profile_pic_checksum'] = $userArr[0]['PROFILE_PIC_CHECKSUM'];
	$k++;
}
*/

//get notification of already friends
$retval = Query("SELECT user.* from user LEFT JOIN friends ON friends.USER_ID_FRIEND=user.USER_ID ".
					"where friends.USER_ID='$user' AND ( user.LAST_MODIFIED > '$lastUpdatedTime' OR friends.LAST_MODIFIED > '$lastUpdatedTime' )" );
$outArr = GetRows( $retval );
$k = 0;
foreach($outArr as $var )
{
	//we got new friend request from a user.get his name(email)
	$JsonObj['FRIENDS'][$k] = array();
	$JsonObj['FRIENDS'][$k]['user'] = $outArr[$k]['USER_NAME'];
	$JsonObj['FRIENDS'][$k]['mobile'] = $outArr[$k]['MOBILE'];
	$JsonObj['FRIENDS'][$k]['profile_pic_checksum'] = $outArr[$k]['PROFILE_PIC_CHECKSUM'];
	$k++;
}

//get already friends location
$retval = Query("SELECT USER_ID_FRIEND from friends where USER_ID='$user'");
$outArr = GetRows( $retval );
$k = 0;
foreach($outArr as $var )
{
	$locQueryObj = Query("SELECT * from location where USER_ID=".$var['USER_ID_FRIEND'].
												" AND LAST_MODIFIED > '$lastUpdatedTime' ORDER BY LAST_MODIFIED DESC LIMIT 1");
	$locArr = GetRows($locQueryObj);
	
	if( sizeof($locArr) > 0 )
	{
		$JsonObj['FRIENDS_POS'][$k] = array();
		$JsonObj['FRIENDS_POS'][$k]['mobile'] = $var['USER_ID_FRIEND'];
		$JsonObj['FRIENDS_POS'][$k]['lat'] = $locArr[0]['LAT'];
		$JsonObj['FRIENDS_POS'][$k]['lng'] = $locArr[0]['LNG'];
		$JsonObj['FRIENDS_POS'][$k]['time'] = $locArr[0]['TIME'];
		$JsonObj['FRIENDS_POS'][$k]['accuracy'] = $locArr[0]['ACCURACY'];
		$k++;
	}
}

echo( json_encode($JsonObj));
mysql_close($conn);
?> 	