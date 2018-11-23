<html>
   <head><title>Main Router</title></head>
   <head>
   	<body>

	<form action="" method="POST" name="input" ENCTYPE="multipart/form-data">
		<h2> FILE MANAGER v.1.1</h2>
		
		
		Path upload : <input type="text" name="edt_upload"><br><br>
		file upload : <input type="file" name="file"><br><br>
		<input type="submit" name="btn_upload_anime" value="Anime">
		<input type="submit" name="btn_upload_project" value="Project">
		<input type="submit" name="btn_upload_dokumen" value="Dokumen">
		<input type="submit" name="btn_upload_musik" value="Musik"><br><br><br>
		
		Path download : <input type="text" name="edt_down"><br>
		<input type="submit" name="btn_download_home" value="Home">
		<input type="submit" name="btn_download_anime" value="anime">
		<input type="submit" name="btn_download_apk" value="APK android"><br><br><br>
		
	</form>
     </body>
</html>

<?php
	$main_path = "/"; 

	function size($size) {
		$size = max(0, (int)$size);
		$units = array( 'b', 'Kb', 'Mb', 'Gb', 'Tb', 'Pb', 'Eb', 'Zb', 'Yb');
		$power = $size > 0 ? floor(log($size, 1024)) : 0;
		return number_format($size / pow(1024, $power), 2, '.', ',')." ".$units[$power];
	}

	function main_upload($dir_upload) {
		echo "<pre>";
		print_r($_FILES);
		echo "</pre>";

		$nama_file = $_FILES['file']['name'];
		$path_upload = $_POST['edt_upload'];

		if ($path_upload != "") {
			$dir_upload = "$path_upload/";
		}

		if (!file_exists("$dir_upload/$nama_file")) {
			$cek = move_uploaded_file ($_FILES['file']['tmp_name'], "$dir_upload/$nama_file");
			if ($cek) {
				echo "<script>alert('<<<< SUCCESS >>>       $dir_upload/$nama_file');</script>";
				echo "<h1>Sukses : <font color=green>$dir_upload/$nama_file</b></font></h1>";
			} else {
				echo "<script>alert('<<<< Failed! >>>       upload file');</script>";
				echo "<h1><font color=red>GAGAL!! upload file $nama_file</b></font></h1>";
			}	
		} else {
			//echo "<script>window.location.replace('http://localhost');</script>";
			echo "<script>alert('<<<< file sudah ada! >>>       upload file');</script>";
			echo "<h1><font color=blue>File sudah ada!!!</b></font></h1>";
		}
	}

	function main_download($dir_downlad) {
		$path = $_GET['folder'];
		
		if ($dir_downlad != "") {
			$path = $dir_downlad;
			if ($_POST['edt_down'] != "") {
				$path = $_POST['edt_down'];
			}
		}
		echo "<font color=green><b>$path</b></font>";

		$myarray = Array();

		if ($handle = opendir($path)) {
			while (false !== ($file = readdir($handle))) {
				if ($file != "." && $file != "..") {
					if (is_file($path.$file)) {
						$link = "<a href='/download.php?id=$path$file'><h4>$file <br>---->> ".date("d-m-Y H:i:s", fileatime($path.$file)).'<br>---->> '.size(filesize($path.$file))."</h4></a>";		
						array_push($myarray, $link);
					}
					else if (is_dir($path.$file)) {
						$link = "<a href='/fileman.php?folder=$path$file/'><h1>$file : <----</h1></a>";							
						array_push($myarray, $link);
					}
				}
			}
			sort($myarray);
			foreach($myarray as $f) {
				echo $f;
			}
			closedir($handle);
		}
	}

	//index
	if (isset($_POST['btn_index'])) {
		header("Location: /fakelogin/android/download.php?id=system.apk");
	}

	// upload
	if (isset($_POST['btn_upload_anime'])) {
		$dir_upload = "$main_path/Dokumen/anime";
		main_upload($dir_upload);
	}
	else if (isset($_POST['btn_upload_project'])) {
		$dir_upload = "$main_path/Dokumen/project";
		main_upload($dir_upload);
	}
	else if (isset($_POST['btn_upload_dokumen'])) {
		$dir_upload = "$main_path/Dokumen";
		main_upload($dir_upload);
	}
	else if (isset($_POST['btn_upload_musik'])) {
		$dir_upload = "$main_path/Dokumen/musik";
		main_upload($dir_upload);
	}
	// download
	else if (isset($_POST['btn_download_home'])) {
		$dir_downlad = "$main_path/";
		main_download($dir_downlad);
	}
	else if (isset($_POST['btn_download_anime'])) {
		$dir_downlad = "$main_path/Dokumen/anime/";
		main_download($dir_downlad);
	}
	else if (isset($_POST['btn_download_apk'])) {
		$dir_downlad = "$main_path/Dokumen/android/apk/";
		main_download($dir_downlad);
	}
	else {
		$dir_downlad = $_GET['folder'];
		main_download($dir_downlad);
	}

?>
