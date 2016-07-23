<?php

include "dbconnect.php" ;

$mobile =  $_REQUEST["mobile"];
//$image_size =  $_REQUEST["scale"];

$JsonObj = array();
//now, check in this in database.
$retval = Query("select * from user where MOBILE='$mobile'" );
$bSuccess = false;
if( mysql_num_rows($retval) > 0 )
{
	$user_arr = GetRows( $retval )[0];
	$pic_path = $user_arr['PROFILE_PIC'];
	$pic_checksum = $user_arr['PROFILE_PIC_CHECKSUM'];
	
	$data = file_get_contents($pic_path);
	if( $data != null && md5($data) == $pic_checksum)
	{
		
		$JsonObj['TIME_STAMP'] = $time;
		$JsonObj['USER'] = array();
		$JsonObj['USER']['MOBILE'] = $mobile;
		$JsonObj['USER']['PROFILE_PIC_CHECKSUM'] = $pic_checksum;
		$JsonObj['USER']['PROFILE_PIC'] = $data;	
		$bSuccess = true;
	}
}
$JsonObj['RESULT'] = $bSuccess;
echo( json_encode($JsonObj));
?>

	