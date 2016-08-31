var contractsStat = false, distributionsStat = false, hypBenefitsStat = false, benefitGraph = false;
var chartContent = null;

function setButtonState(button, state)	{
	//rgba(80,80,80,0.9) rgba(110,210,0,1);
	//rgba(110,210,0,1); rgba(50,50,50,1);
	if(state)	{
		document.getElementById(button).style.background = "rgba(110,210,0,1)";
		document.getElementById(button).style.color = "rgba(50,50,50,1)";
	}	else	{
		document.getElementById(button).style.background = "rgba(80,80,80,0.9)";
		document.getElementById(button).style.color = "rgba(110,210,0,1)";
	}
	switch(button)	{
	case "contract":
		contractsStat = state;
		break;
	case "distribution":
		distributionsStat = state;
		break;
	case "hypBenefit":
		hypBenefitsStat = state;
		break;
	case "benefitGraph":
		benefitGraph = state;
		break;
	}
}

function drawPlayChart(data)	{
	drawChart(data, 0, true, false, false, false);
}


/**
 * @param data
 * @param type	Set Chart type: 0 - play, 1 - Evaluation, 2 - Evaluation with "silent" PDF export, 3 - Evaluation with download
 */
function drawChart(data, type, contracts, distributions, hypBenefits, benefitGraph)	{
	//Daten verarbeiten und darstellen
	dataArray = data.split(";");
	// 0 - current play data, 1 - Minimum, 2 - Maximum, 
	// 3 - sellerDistribution, 4 - buyerDistribution, 
	// 5 - realPosBenefits, 6 - realNegBenefits
	// 7 - benefitData
	var chartData = [];
	yMin = dataArray[1];
	yMax = dataArray[2];
	if(contracts)	{
		playData = JSON.parse(dataArray[0]);
		chartData = [{color:"#000",data:playData}];
	}
	if(type != 0)	{
		setButtonState('contract', contracts);
		setButtonState('distribution', distributions);
		setButtonState('hypBenefit', hypBenefits);
		setButtonState('benefitGraph', benefitGraph);
		if(distributions)	{
			sellerData = JSON.parse(dataArray[3]);
			buyerData = JSON.parse(dataArray[4]);
			chartData = chartData.concat({lines:{steps:true},data:sellerData,color:"#1d3cec"},{lines:{steps:true},data:buyerData,color:"#cb4b4b"});
		}
		if(hypBenefits && dataArray[5].length > 0 && dataArray[6].length > 0)	{
			posBenefits = JSON.parse(dataArray[5]);
			negBenefits = JSON.parse(dataArray[6]);
			chartData = chartData.concat(	{lines:{show:false,steps:true},data:posBenefits,id:"pos",fillBelowTo:"neg",color:"#6ECD00"},
											{lines:{show:false,steps:true},data:negBenefits,id:"neg",fillBelowTo:"pos",color:"#6ECD00"});
		}
		if(benefitGraph)	{
			benefits = JSON.parse(dataArray[7]);
			chartData = chartData.concat(	{bars:{align: "center",show:true,barWidth:0.2},data:benefits,color:"#e09200"});
		}
	}
	
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
					}
				}

	//draw the chart
	var plot = $.plot($("#placeholder"), chartData , options);
	
	if(type >= 2)	{
		//prepare export of the image of the chart
		var header = $("meta[name='_csrf_header']").attr("content");
		var token = $("meta[name='_csrf']").attr("content");
		var myCanvas = plot.getCanvas();
		console.log(myCanvas);
		var formData = new FormData();
		myCanvas.toBlob(
			function(image){
	
				formData.append("image",image,"image.png");
				formData.append("se", (type == 2));
				
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
					success:function(response){console.log(response); if(type == 3) window.location.href='pdfDownload.html';},
					error: function(e){console.log(e);}			
				});
			},
			"image/png");
	}
	
}

function getData(){
	$.ajax({
		type: "Get",
		url: "getData.html",
		success: function(response) {drawPlayChart(response);},
		error: function(e){alert('Error' + e);}
	});
	
}

function drawEvaluation(contracts, distributions, hypBenefits, benefitGraph){
	if(chartContent == null)	{
		$.ajax({
			type: "Get",
			url: "getEvaluation.html",
			success: function(response) {chartContent = response; drawChart(response, 2, contracts, distributions, hypBenefits, benefitGraph);},
			error: function(e){alert('Error' + e);}
		});
	} else 
		drawChart(chartContent, 1, contracts, distributions, hypBenefits, benefitGraph);
}

function toggleEvaluation(which)	{
	if(which == "contracts")
		drawEvaluation(!contractsStat, distributionsStat, hypBenefitsStat, benefitGraph);
	else if(which == "distributions")
		drawEvaluation(contractsStat, !distributionsStat, hypBenefitsStat, benefitGraph);
	else if(which == "hypBenefits")
		drawEvaluation(contractsStat, distributionsStat, !hypBenefitsStat, benefitGraph);
	else if(which == "benefitGraph")
		drawEvaluation(contractsStat, distributionsStat, hypBenefitsStat, !benefitGraph);
}
