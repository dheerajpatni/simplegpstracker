


<?php
    /*$base=$_REQUEST['image'];
    $binary=base64_decode($base);
    header('Content-Type: bitmap; charset=utf-8');
    $file = fopen('uploaded_image.jpg', 'wb');
    fwrite($file, $binary);
    fclose($file);
    echo 'Image upload complete!!, Please check your php file directory……';
	*/
?>



<?php

include "dbconnect.php" ;

$userid =  $_REQUEST["uid"];
$encoded_img = $_REQUEST['image'];
$checksum = $_REQUEST['imgchecksum'];

if( md5($encoded_img) != $checksum)
	die( "Some error in uploading file. PHP:".md5($encoded_img) ."ANDROID:".$checksum);

// image is saved
$filename = "$userid". date("Y-m-d H_i_s") .".jpg";
//$binary = base64_decode($encoded_img);
//header('Content-Type: bitmap; charset=utf-8');
$file = fopen( "$filename" , 'wb');
if( $file == null )
	die( "Some error occured" );
fwrite($file, /*$binary*/ $encoded_img  );
fclose($file);

//now, check in this in database.
$retval = Query("select * from user where USER_ID='$userid'" );
$old_pic_path = GetRows( $retval )[0]['PROFILE_PIC'];

//update new profile pic path
$retval = Query("UPDATE user SET PROFILE_PIC='$filename' ,PROFILE_PIC_CHECKSUM='$checksum',LAST_MODIFIED=$time WHERE USER_ID='$userid'");

//remove old profile pic
//unlink($old_pic_path);

die( "SUCCESS" );
?>