// General Validation of Field "field"
function generalValidateField(field)	{
	
	var error = null;
	
	// Validate number fields
	if(field.type == "number")	{
		
		// Field must be a number
		if(isNaN(field.value))	{
			error = 1;
		}
		
		// Number must be bigger than 0 
		if(field.value <= 0)	{
			error = 2;
		}

		// Field must not be empty
		if(field.value == "")	{
			error = 0;
		}
		
		// Number must not be fractional 
		if((field.value % 1) != 0)	{
			error = 3;
		}
		
	}	else	{
		// Validate text/password fields
		
		// Field must not be empty
		if(field.value == "")	{
			error = 0;
		}
		
	}

	// If any Error occurred
	if(error != null)	{
		// Write Error
		writeError(error, field);
		return false;
	}
	
	// Everything is fine
	return true;
	
}

// Specific Validation of a form
function validateForm(form)	{
	
	var error = false;
	var globalError = false;
	
	// Get Array of input fields
	var coll = document.getElementById(form).elements;
	
	var inputs = [];
	
	for(var i = 0; i < coll.length; i++)	{
		// Relevant Fields are: number, text, password
		if((coll[i].type == "number" || coll[i].type == "text" || coll[i].type == "password") && coll[i].readOnly == false)
			inputs[inputs.length] = coll[i];
	}
	
	// Go through all input fields
	for(var i = 0; i < inputs.length && inputs[i] != null; i++)	{
		
		error = false;
		
		// Do a general Validation of the current field
		if(generalValidateField(inputs[i]))	{
			
			// Do specific Validation
			switch(form)	{
			case 'config':
				// Basic Configuration/Metadata Formular
				var players = document.getElementById('players');
				var assistants = document.getElementById('assistants');
				
				// Number of players must be even
				if(players.value % 2 != 0)	{
					writeError(4, players);
					error = true;
				}
				// Number of Players must be lower than (10000 - firstID)
				// TODO get firstID from Logic
				if(players.value > (10000 - 1001))	{
					writeError(5, players);
					error = true;
				}
				// Number of Assistants must be lower or equal 26 (Because of max 26 letters)
				if(assistants.value > 26)	{
					writeError(5, assistants);
					error = true;
				}
				break;
			case 'exclude':
				// Exclude Cards Formular
				var error;
				var field = document.getElementById(i);
				
				// Field must be a Number (type = text)
				if(isNaN(field.value))	{
					writeError(1, field);
					error = true;
				}
				// Number must be between first ID and lastID
				if(field.value < firstID || field.value > (firstID + numberOfPlayers))	{
					writeError(5, field);
					error = true;
				}
				break;
			}
			
		}	else	{
			// General Validation failed
			error = true;
		}
		
		if(!error)	{
			// remove Error if none
			removeError(inputs[i]);
		}
		
		// Set the global Error
		globalError = globalError || error;
		
	}
	
	// Special Check for distribution Form
	if(form == "arrangement" && !globalError)	{
		// Check if total relative is hundred
		if(!isHundred())	{
			alert(totalOOB);
			globalError = true;
		}	else	{
			// relative = 100
			// Check if we needed to round
			if(document.getElementById('customerTotalAbsolute').innerHTML > numberOfPlayers/2 ||
					document.getElementById('salesmanTotalAbsolute').innerHTML > numberOfPlayers/2)	{
				// Ask User if it was OK to round
				globalError = !window.confirm(roundedUp + (document.getElementById('customerTotalAbsolute').innerHTML*1 + document.getElementById('salesmanTotalAbsolute').innerHTML*1 - numberOfPlayers));
			}
		}
	}
	
	if(globalError)	{
		return false;
	}	else
		return true;
	
}

// Create Error fields if the distribution was loaded from logic
function createErrorFields(element, numberOfColumns)	{
	
	// Get inputs Array ----------------------------------
	var coll = document.getElementById(element).elements;
	
	var inputs = [];
	
	for(var i = 0; i < coll.length; i++)	{
		// Relevant inputs: number fields, that are not readonly
		// [Absolute fields are readonly for passing it with POST request, they dont need error field]
		if(coll[i].type == "number" && coll[i].readOnly == false)
			inputs[inputs.length] = coll[i];
	}
	
	// For every Error relevant field
	for(var i = 0; i <= inputs.length && inputs[i] != null; i++)	{
		
		if(document.getElementById('error' + inputs[i].name) == null)	{
			
			if(document.getElementById('errorRow' + div(i, numberOfColumns)) == null)	{
			
				// create Error Row
				var errorRow = document.createElement("div");
				errorRow.setAttribute("class", "row");
				errorRow.setAttribute("id", "errorRow" + div(i, numberOfColumns));
				
				// Append Error Row
				inputs[i].parentNode.parentNode.parentNode.insertBefore(errorRow, inputs[i].parentNode.parentNode.nextSibling);
				
			}	else	{
				// Get Error Row
				var errorRow = document.getElementById('errorRow' + div(i, numberOfColumns));
			}
			
			// Create Error Div
			var error = document.createElement("div");
			error.setAttribute("class", "error");
			error.setAttribute("id", "error" + inputs[i].id);
			
			// Append Error Div to Row
			errorRow.appendChild(error);
			
		}
		
	}
}

// Write Error "error" in field under Element "element"
function writeError(error, element)	{
	
	var errorCodes = new Array();
	
	errorCodes[0] = "Feld leer";
	errorCodes[1] = "NaN";
	errorCodes[2] = "Zahl <= 0";
	errorCodes[3] = "Zahl gebrochen";
	errorCodes[4] = "Zahl ungerade";
	errorCodes[5] = "Zahl zu groß";
	
	// Write Error Text in Field
	document.getElementById('error' + element.id).innerHTML = errorCodes[error];
	// Paint border of element red
	element.style.border = "1px solid red";
}

// Remove Errors in field under Element "element"
function removeError(element)	{
	// Clear Text
	document.getElementById('error' + element.id).innerHTML = "";
	// Paint Border white
	element.style.border = "1px solid #AAA";
}