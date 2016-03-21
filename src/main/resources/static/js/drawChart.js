function drawPlayChart(data){

		dataArray = data.split(";");
		//0 - current play data, 1 - Minimum, 2 - Maximum
		
		playData = JSON.parse(dataArray[0]);
		
		var yMin = dataArray[1];
		var yMax = dataArray[2];

		//if there is no limitation -> min and max automatically due to values
		if(yMin == 0) yMin = null;
		if(yMax == 0) yMax = null;

		var options = {	axisLabels: {
							show:true
						},
						xaxis:{
							minTickSize: 1,
							autoscaleMargin:0.02,
							tickDecimals: 0,
							axisLabel: xAxis,
							axisLabelPadding: 10,
							axisLabelUseCanvas: true,
							axisLabelColour: "rgba(0,0,0,0.7)",
							axisLabelFontSizePixels: 18
						},
						yaxis:{
							autoscaleMargin:0.02,
							tickDecimals: 0,
							min: yMin,
							max: yMax,
							axisLabel: yAxis,
							axisLabelPadding: 10,
							axisLabelUseCanvas: true,
							axisLabelColour: "rgba(0,0,0,0.7)",
							axisLabelFontSizePixels: 18
						},
						colors: ["#000000"]
					}
		
		//draw the chart
		$.plot($("#placeholder"), [playData] , options);
	
}

function drawEvaluationChart(data){
		//Daten verarbeiten und darstellen

		dataArray = data.split(";");
		// 0 - current play data, 1 - sellerDistribution, 2 - buyerDistribution, 3 - Minimum, 4 - Maximum
		
		playData = JSON.parse(dataArray[0]);
		sellerData = JSON.parse(dataArray[1]);
		buyerData = JSON.parse(dataArray[2]);
		chartData = [{data:playData},{lines:{steps:true},data:sellerData},{lines:{steps:true},data:buyerData}];

		yMin = dataArray[3];
		yMax = dataArray[4];
		
		//if there is no limitation -> min and max automatically due to values
		if(yMin == 0) yMin = null;
		if(yMax == 0) yMax = null;
		
		var options = {	axisLabels : {
							show: true
						},
						xaxis:{
							minTickSize: 1,
							autoscaleMargin:0.02,
							tickDecimals: 0,
							axisLabel: xAxis,
							axisLabelPadding: 10,
							axisLabelUseCanvas: true,
							axisLabelColour: "rgba(0,0,0,0.7)",
							axisLabelFontSizePixels: 18
						},
						yaxis:{
							min: yMin,
							max: yMax,
							axisLabel: yAxis,
							axisLabelPadding: 10,
							axisLabelUseCanvas: true,
							axisLabelColour: "rgba(0,0,0,0.7)",
							axisLabelFontSizePixels: 18
						},
						grid:{
							backgroundColor: "white"
						},
						colors: ["#000000", "#1d3cec", "#cb4b4b"]
					}
		
		//draw the chart
		var plot = $.plot($("#placeholder"), chartData , options);
		
		//prepare export of the image of the chart
		var header = $("meta[name='_csrf_header']").attr("content");
		var token = $("meta[name='_csrf']").attr("content");
		var myCanvas = plot.getCanvas();
		console.log(myCanvas);
		var formData = new FormData();
		
		myCanvas.toBlob(
			function(image){

				formData.append("image",image,"image.png");
				
				//send image to logic/java
				$.ajax({
					beforeSend: function(request) {
						request.setRequestHeader(header, token);
					},
					url: "pdfExport.html",
					type: "POST",
					data: formData,
					processData: false,
					contentType: false,
					mimeType: "multipart/form-data",
					success:function(response){console.log(response)},
					error: function(e){console.log(e);}			
				});
			},
			"image/png");
		
}

function getData(){
	
	$.ajax({
		type: "Get",
		url: "getData.html",
		success: function(response) {drawPlayChart(response);},
		error: function(e){alert('Error' + e);}
	});
	
}

function drawEvaluation(){
	$.ajax({
		type: "Get",
		url: "getEvaluation.html",
		success: function(response) {drawEvaluationChart(response);},
		error: function(e){alert('Error' + e);}
	});
	
}


