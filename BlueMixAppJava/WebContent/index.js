// index.js
var REST_DBENV = 'api/dbinfo';
var REST_TABLELIST = 'api/tablelist';
var REST_TWITTERENV = 'api/twitterinfo';
var REST_COUNT = 'api/twittercount';
var REST_LOAD = 'api/load';
var REST_SENTI='api/loadsentiment';
var REST_SEARCH='api/search';
var REST_EVENTS='api/events'
var REST_COORDDINATES='api/coordinates'
var numtweets = 0;
function countTweets(){
	document.getElementById('numtweets').innerHTML = '<br/>Please wait...<br/><br/>';
//	document.getElementById('numtweets').className = 'greyArea';
//	document.getElementById('tweetquery').disabled = true;
//	document.getElementById('countbutton').disabled = true;
	
	var countURL = REST_COUNT + '?q=' + encodeURIComponent(document.getElementById('tweetquery').value);
	xhrGet(countURL, function(count){
		
				console.log(count);
				var returnString=JSON.parse(count);
				numtweets = returnString.search.results;
				console.log(numtweets);
				if (numtweets > 0){
					document.getElementById('numtweets').innerHTML = '<br/>' + numtweets + ' tweets available...<br/><br/>';
//					document.getElementById('numtweets').className = 'greenArea';
				}else{
					document.getElementById('numtweets').innerHTML = '<br/>No tweets available...<br/><br/>';
//					document.getElementById('numtweets').className = 'redArea';
				}
//				toggleLoadButton(document.getElementById('tablename'));
				
		}, function(err){
			document.getElementById('numtweets').innerHTML = '<br/>No tweets error available...<br/><br/>';
//			document.getElementById('numtweets').className = 'redArea';
			console.error(err);
	});

//	document.getElementById('tweetquery').disabled = false;
//	document.getElementById('countbutton').disabled = false;
}
function getColumns(){
	var coltable=document.getElementById('columns').children[1];
	var colnames = '';
	var colidx = 0;
	var colmax=coltable.childElementCount;
	while (colidx < colmax) {
		var colchecked = coltable.children[colidx].children[0].children[0].checked;
		if (colchecked == true) {
			var colname = coltable.children[colidx].children[1].children[0].value;
			var coltype = coltable.children[colidx].children[2].innerHTML;
			var colloc = coltable.children[colidx].children[3].innerHTML;
			if (colnames.length > 0 ) {
				colnames = colnames + "|";
			}
			colnames = colnames + colname + "|" + coltype + "|" + colloc;
		}
		colidx = colidx + 1;
	}
	return colnames;
}
function startAnalysis(){
	loadData();
	getSentiments();
	getCoordinates();
}
function loadData(){
//	var loadarea = document.getElementById('analysis');
//	var progressarea = document.getElementById('progress');
//	var phase = progressarea.getElementsByTagName('p')[0];
//	var progress = progressarea.getElementsByTagName('progress')[0];
	var columns = document.getElementById('columns');
//	var togglecolumns = document.getElementById('togglecolumns');
	var formmap = {
		q: document.getElementById('tweetquery').value,
		table: document.getElementById('tweetquery').value,
		columns: getColumns()
	};

	// deactivate all clickable form elements
	document.getElementById('tweetquery').disabled = true;
//	document.getElementById('countbutton').disabled = true;
//	document.getElementById('tables').disabled = true;
//	document.getElementById('tablename').disabled = true;
//	columns.style.display = 'none';
//	togglecolumns.innerHTML = '>';
//	togglecolumns.disabled = true;
//	loadarea.style.display = 'none';
//	progressarea.style.display = '';
	
	// create the table and load tweets
//	phase.innerHTML = 'Starting...';
//	progress.max = 1.0;
//	progress.value = 0.0;
	xhrPost(REST_LOAD, formmap, function(loadstatus){

				console.log(loadstatus);

	}, function(err){
		console.error(err);
	});
//	getLoadProgress();
	
}

function getSentiments()
{
//	var tweetquery=document.getElementById("tweetquery").value;
	var formmap = {
			q: document.getElementById("tweetquery").value,
			table: document.getElementById('tweetquery').value,
			columns: getColumns()
		};
	xhrPost(REST_SENTI,formmap, function(result){
		var data=result;
		var visualization = d3plus.viz()
	    .container("#vis")
	    .data(data)
	    .type("bar")
	    .id("name")
	    .x("name")
	    .y("value")
	    .draw()
	}, function(err){
		console.error(err);
	});
	}
var data;
function getData(){
	return data;
}
function getCoordinates(){
	var formmap = {
			q: document.getElementById("tweetquery").value,
			table: document.getElementById('tweetquery').value,
			columns: getColumns()
		};
	xhrPost(REST_COORDDINATES,formmap, function(result){
		 data=result;
	
		
	}, function(err){
		console.error(err);
	});
}
//function getLoadProgress(){
//	xhrGet(REST_LOAD, function(loadstatus){
//				var status=JSON.parse(loadstatus);
//				var progressarea = document.getElementById('progress');
//				var phase = progressarea.getElementsByTagName('p')[0];
//				var progress = progressarea.getElementsByTagName('progress')[0];
//				if (status.status == 'idle') {
//					phase.innerHTML = status.phase;
//					setTimeout(getLoadProgress(), 1000);
//				} else if (status.status == 'running') {
//					phase.innerHTML = status.phase;
//					progress.max = status.expected;
//					progress.value = status.actual;
//					setTimeout(getLoadProgress(), 1000);
//				} else if (status.status == 'error') {
//					phase.innerHTML = 'ERROR: ' + status.phase;					
//					setTimeout(stopLoad(), 5000);
//				} else {
//					phase.innerHTML = status.phase;
//					setTimeout(stopLoad(), 5000);					
//				}
//
//	}, function(err){
//		console.error(err);
//		setTimeout(getLoadProgress(), 1000);
//	});
//}
//
//
//function stopLoad(){
//	var loadarea = document.getElementById('analysis');
//	var progressarea = document.getElementById('progress');
//	var phase = progressarea.getElementsByTagName('p')[0];
//
//	// save the latest phase message to the log
//	var logarea = document.getElementById('log');
//	var currentTime = new Date();
//	var month = currentTime.getMonth() + 1;
//	var day = currentTime.getDate();
//	var year = currentTime.getFullYear();
//	var hrs = currentTime.getHours();
//	var mins = currentTime.getMinutes();
//	var secs = currentTime.getSeconds();
//	var curts = '' + year + '-'
//					+ ((month < 10) ? '0' : '') + month + '-'
//					+ ((day < 10) ? '0' : '') + day + ' '
//					+ ((hrs < 10) ? '0' : '') + hrs + ':'
//					+ ((mins < 10) ? '0' : '') + mins + ':'
//					+ ((secs < 10) ? '0' : '') + secs;
//	logarea.innerHTML = '<p>(' + curts + ') ' + phase.innerHTML + '</p>' + logarea.innerHTML;
//	
//	// activate the form for the next load
//	loadarea.style.display = '';
//	progressarea.style.display = 'none';
////	document.getElementById('tweetquery').disabled = false;
////	document.getElementById('countbutton').disabled = false;
////	document.getElementById('tables').disabled = false;
////	document.getElementById('tablename').disabled = false;
//	document.getElementById('startanalysis').disabled = true;
////	document.getElementById('togglecolumns').disabled = false;
////	refreshTableList();
//}



function getTweets()
{
// request message on server
xhrGet("api/hello", function(responseText){
	// add to document
	var mytitle = document.getElementById('message');
	mytitle.innerHTML = responseText;

}, function(err){
	console.log(err);
});
}
function getEvents(){


	xhrGet(REST_EVENTS, function(responseText){
		// add to document
		console.log(responseText);
		var json_data=JSON.parse(responseText);
		var events=json_data.events.event;
		for (var i = 0; i < events.length; i++) {
		    var event_title = events[i].title;
		    var event_desc=events[i].description;
		    addRow(event_title,event_desc);
		}
	
	}, function(err){
		console.log(err);
	});
}
function addRow(name,desc) {
	var table = document.getElementById("tableCheck");

    var rowCount = table.rows.length;
	var rowIndex=$('#tableCheck').dataTable().fnAddData( [
	                                         rowCount,
	                             		    name,
	                             		    desc ]
	                             		  );
	var row = $('#tableCheck').dataTable().fnGetNodes(rowIndex);
	$(row).attr( 'data-name', name );
	$(row).attr( 'data-toggle', 'modal' );
	$(row).attr( 'data-target', '#confirmModal' );
		
}
function searchCity()
{
	xhrGet(REST_SEARCH, function(responseText){
		// add to document
		var mytitle = document.getElementById('message');
		mytitle.innerHTML = responseText;

	}, function(err){
		console.log(err);
	});
}

//utilities
function createXHR(){
	if(typeof XMLHttpRequest != 'undefined'){
		return new XMLHttpRequest();
	}else{
		try{
			return new ActiveXObject('Msxml2.XMLHTTP');
		}catch(e){
			try{
				return new ActiveXObject('Microsoft.XMLHTTP');
			}catch(e){}
		}
	}
	return null;
}
function xhrGet(url, callback, errback){
	var xhr = new createXHR();
	xhr.open("GET", url, true);
	xhr.onreadystatechange = function(){
		if(xhr.readyState == 4){
			if(xhr.status == 200){
				callback(xhr.responseText);
			}else{
				errback('service not available');
			}
		}
	};
	xhr.timeout = 3000;
	xhr.ontimeout = errback;
	xhr.send();
}

function xhrPost(url, data, callback, errback){
	var xhr = new createXHR();
	xhr.open("POST", url, true);
	xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xhr.onreadystatechange = function(){
		if(xhr.readyState == 4){
			if(xhr.status == 200){
				callback(parseJson(xhr.responseText));
			}else{
				errback('service not available');
			}
		}
	};
//	xhr.timeout = 3000;
//	xhr.ontimeout = errback;
	xhr.send(objectToQuery(data));
}
function objectToQuery(map){
	var enc = encodeURIComponent, pairs = [];
	for(var name in map){
		var value = map[name];
		var assign = enc(name) + "=";
		if(value && (value instanceof Array || typeof value == 'array')){
			for(var i = 0, len = value.length; i < len; ++i){
				pairs.push(assign + enc(value[i]));
			}
		}else{
			pairs.push(assign + enc(value));
		}
	}
	return pairs.join("&");
}
function parseJson(str){
	return window.JSON ? JSON.parse(str) : eval('(' + str + ')');
}
function prettyJson(str){
	// If browser does not have JSON utilities, just print the raw string value.
	return window.JSON ? JSON.stringify(JSON.parse(str), null, '  ') : str;
}

window.onresize = function(){ location.reload(); }
