<html>
<head>
	<meta charset="utf-8" />
	<title>Demo</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0" />
	<link  href="/css/bootstrap.css" rel="stylesheet"></link>
	<script type="text/javascript" src="http://code.jquery.com/jquery.min.js"></script>
</head>

<body>
	<div class="container">
		<h1>Test image</h1>
		<div>
			<img id="icon" src="http://placehold.it/64x64"/>
		</div>
		<button id="computer" onclick="requestImage('computer')">Computer</button>
		<button id="code"  onclick="requestImage('code')">Code </button>
		<button id="cloud" onclick="requestImage('cloud')">Cloud</button>
		<button id="phone" onclick="requestImage('phone')">Phone</button>

	</div>
	<hr/>
</body>
<script  type="text/javascript">

	function requestImage (imageId) {
		var requestUrl = "/viewImage/" + imageId + ".json";

		var icon =  $("#icon");
		$.ajax({
		 url : requestUrl,
		 type: "GET",
		 dataType :"json",
		 success:function(image){
			 icon.attr('src', image.src);
			 icon.attr('height',image.height);
			 icon.attr('width',image.width);			 
		 },
		 error:function(jqXHR, textStatus, errorThrown){
		  alert("Error");
		 }
		});
	}
</script>
</html>