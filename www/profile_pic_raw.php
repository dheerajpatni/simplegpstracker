<?php

include "dbconnect.php" ;

$mobile =  $_REQUEST["mobile"];
//now, check in this in database.
$retval = Query("select * from user where MOBILE='$mobile'" );
$bSuccess = false;
if( mysql_num_rows($retval) > 0 )
{
	$user_arr = GetRows( $retval )[0];
	$pic_path = $user_arr['PROFILE_PIC'];
	$pic_checksum = $user_arr['PROFILE_PIC_CHECKSUM'];
	
	$newSize = 0;
	if( isset($_REQUEST['size']) && !empty($_REQUEST['size']) )
	{
		$newSize = $_REQUEST["size"];
	}
	
	$data = file_get_contents($pic_path);
	
	if( $data != null && md5($data) == $pic_checksum)
	{
		$binary = base64_decode($data);	
		$src = imagecreatefromstring($binary);
		$imgSize = imagesx($src);
		
		$dst = $src;
		if( $newSize > 0 && $newSize < $imgSize )
		{
			$dst = imagecreatetruecolor($newSize, $newSize);
			imagecopyresampled($dst, $src, 0, 0, 0, 0, $newSize, $newSize, $imgSize, $imgSize);
		}
		
		header('Content-Type: bitmap; charset=utf-8');
		imagejpeg ( $dst );
		
		/*$file = fopen( $filename , 'wb');
		if( $file == null )
			die( "Some error occured" );
		fwrite($file, $binary  );
		fclose($file);*/
		//echo ('<img src="'.$filename.'" />');
		
		//echo( $binary );
	}
}
?>

	