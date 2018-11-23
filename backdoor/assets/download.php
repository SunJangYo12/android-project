<?php
	$dfile = "/home/sunjangyo12/kroot.apk";
	if (isset($_POST['signIn'])) {
		echo "okedf";
		down($dfile);
	}
	else {
		down($_GET['id']);
	}

	function down($dfile) {
		$filename = basename($dfile);
		$finfo = finfo_open(FILEINFO_MIME_TYPE);
		header('Content-Type: ' . finfo_file($finfo, $dfile));
		header('Content-Length: '. filesize($dfile));
		header(sprintf('Content-Disposition: attachment; filename=%s',
			strpos('MSIE',$_SERVER['HTTP_REFERER']) ? rawurlencode($filename) : "\"$filename\"" ));
		ob_flush();
		readfile($dfile);
		exit;
	}
?>