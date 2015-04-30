//the document ready function
try	{
	$(function() {
		init();
	});
} catch (e) {
	alert("*** jQuery not loaded. ***");
}

function init() {
	///////////////////////////////////////////////////////
	/////////////////// DIALOG HANDLERS////////////////////
	///////////////////////////////////////////////////////
	//make dialog box
	$("#listItemDialog").dialog({ modal: true,			//modal dialog to disable parent when dialog is active
							autoOpen: false,		//set autoOpen to false, hidding dialog after creation
							title: "List An Item for Auction",	//set title of dialog box
							minWidth: 600,
							minHeight: 400
						});

	$("#itemDetails").dialog({ modal: true,			//modal dialog to disable parent when dialog is active
							autoOpen: false,		//set autoOpen to false, hidding dialog after creation
							title: "Bid an Item",	//set title of dialog box
							minWidth: 600,
							minHeight: 400
						});

	///////////////////////////////////////////////////////
	/////////////////// DIALOG HANDLERS/////////////////////
	///////////////////////////////////////////////////////

	//set click handler of "List An Item" button
	$("#listItemButton").click(function() {
						$("#itemSeller").val("");
						$("#itemTitle").val("");
						$("#itemDesc").val("");
						$("#itemPrice").val("");
						$("#noBidsDetails").val("");
						$("#winnerDetails").val("");
						$("#itemDeadline").val("");
						$("#listItemDialog").dialog("open",true); //open dialog box
					});
	//set click handler of "Refresh" button
	//refresh items, trigger populateItems()
	$("#refreshItemsList").click(function() {
							populateItems();
					  	  });
	//set click handler of "List An Item" button in List An Item dialog
	$("#listItem").click(function() {
					listItem();	//save item to web service
					$("#listItemDialog").dialog("close");	
			 	  });
	//set click handler of "Cancel" button in List An Item dialog
	$("#cancelItem").click(function() {
					$("#listItemDialog").dialog("close");
				  });

	//set click handler of "Cancel" button in List An Item dialog, close details dialog
	$("#closeDetails").click(function() {
						$("#itemDetails").dialog("close");
						$("#items tr").removeClass("selected");
				 	});

	//set click handler of Place Bid button in List Item dialog, close details dialog
	//place a bid
	$("#placeBid").click(function() {
					placeBid();	//save item to web service
					$("#itemDetails").dialog("close");
					$("#items tr").removeClass("selected");
			 	 });

	//set click handler to display an item details
	$("#items").on("click", "tr", function() {
				var id = $(this).attr("id");
				itemSelected(id);
				$("#itemDetails")
					.attr("data", id) //save id as an attribute "data" of the dialog
					.dialog("open",true);	//open dialog box
	});
	//populate list of saved items
	populateItems();	
}

/*
	Save an item using the Item service, given its position
*/
function listItem() {
	var seller = $("#itemSeller").val(),
		title = $("#itemTitle").val(),
		desc = $("#itemDesc").val(),
		price = $("#itemPrice").val(),
		deadlineString = $("#itemDeadline").val(),
		deadline = Date.parse(deadlineString),
		url="/item"; //URL of web service

	
	//request parameters as a map
	var data = { "seller": seller,
				 "title": title,
				 "desc": desc,
				 "price": price,
				 "deadline": deadline
				};

	//use jQuery shorthand Ajax POST function
	$.post(	url, data, function() { // on success, callback function
		console.log(url);
		alert("Item saved: " + seller + " (" + title + ", £" + price + ")");
	}).done(function() {
		populateItems();
	}).fail(function(){
		alert("Item cannot be listed. Please, fill in all the fields.");
	});
} 

/*
	Save a bid using the Bid service
*/
function placeBid() {
	var bidder = $("#bidder").val(),
		itemId = $("#itemDetails").attr("data"),
		amount = $("#amount").val();
		url="/bid"; //URL of web service

		//request parameters as a map
		var data = { "bidder":bidder,
					 "itemId": itemId,
				 	 "amount":amount
		};
		
		//use jQuery shorthand Ajax POST function
		$.post(	url, data, function() { // on success, callback function
			alert("Bid placed: " + bidder + " (£" + amount + ")");
		}).done(function() {
			populateItems();
		}).fail(function(){
			alert("This bid cannot be placed!")
		});
		recalculateBids(itemId);		
} 
/*
	Retrieve details of an item in the items list
*/
function itemSelected(id) {
	$("#items tr").removeClass("selected"); //remove all list items from the class "selected, thus clearing previous selection

	// 	Find the selected item (i.e. list item) and add the class "selected" to it.
	// This will highlight it according to the "selected" class.
	$("#" + id).addClass("selected");

	//retrieve item details from item service
	var itemUrl="/item/" + id, // url of service listing particular item details
		winningBidUrl = "/bid/" + id, // url of service displaying winning bid for this item
		allBidsUrl = "/bid",
		winner = "---", //auction winner name
		bidsCount = 0; //count of the bids placed on this item

	$.getJSON(winningBidUrl)
    .then(function(jsonData) {
    	if (jsonData == null) {
    		winner = "---";
    	} else {
    		winner = jsonData["bidder"];
    	}
    })
    .fail(function() {
        // didn't work
    })
    .done(function() {
    	$("#winnerDetails").html( "(winner: " + winner + ")" );
    });
	
	$.getJSON(allBidsUrl)
    .then(function(jsonData) {
      $.grep( jsonData, function( n, i ) {
  			if ( n['itemId'] == id ) bidsCount++;
		});
	}).done(function(){
		$("#noBidsDetails").html(bidsCount);
    }).fail(function() {
        // ...didn't work, handle it
    });	

	//use jQuery shorthand AJAX function to get JSON data
	$.getJSON(itemUrl, function(jsonData) {
		seller = jsonData["seller"];
		title = jsonData["title"];
		deadline = new Date(jsonData["deadline"]);
		currentPrice = jsonData["currentPrice"];
		desc = jsonData["description"];

		$("#itemSellerDetails").html(seller);
		$("#itemTitleDetails").html(title);
		$("#itemDeadlineDetails").html(+ (deadline.getMonth() + 1) + "/" + deadline.getDate() + "/" + deadline.getFullYear() + "\t"
						+ deadline.getHours() + ":" + ('0'+deadline.getMinutes()).slice(-2) + ":00");
		$("#itemPriceDetails").html("£" + currentPrice);
		$("#itemDescDetails").html(desc);
			
	});

} 

/*
/ Retrieve all items from Item service and populate the items list
*/
function populateItems() {
	var url="/item";		//URL of items service

	//use jQuery shorthand Ajax function to get JSON data
	$.getJSON(url,				//URL of service
		function(items) {
			$("#items").empty();	//clear the table before populating it
			
			// sort array of items previously to outputing it to browser
			items.sort(function(a,b){
    			var a = new Date(a.deadline),
    				b = new Date(b.deadline);
    				// Compare the 2 dates
    				if(a < b) return -1;
    				if(a > b) return 1;
    				return 0;
			});

			/* Loop through the items */
				for (var i in items) {
					var item = items[i];		//get 1 item from the JSON list
						id = item["id"],		//get item id from JSON data
						title = item["title"],	//get item title from JSON data
						deadline = new Date(item["deadline"]),
						price = item["currentPrice"];

					//date format is: 11/21/1987 16:00:00
				

					//compose HTML of a table row representing an item
					var htmlCode = "<tr id='" 
								+ id 
								+"'><td class='wide sort'>" 
								+ (deadline.getMonth() + 1) + "/" + deadline.getDate() + "/" + deadline.getFullYear() + " "
								+ deadline.getHours() + ":" + ('0'+deadline.getMinutes()).slice(-2) + ":00"
								+ "</td><td class='narrow item'>" + title + "</td><td class='narrow'>£"
								+ price
								+"</td></tr>";
					
						$("#items").append(htmlCode);	//add a child to the city list
				}
					
		}); //end Ajax call
}

/*
	Update the winning bid by using the service Bid
*/
function recalculateBids(itemId) {
		var url= "/bid/" + itemId,
			settings = {type:"GET"};

		$.ajax(url,settings);
}

/*
	Helper sorter function
*/
function sortNum(a, b) {
    array.sort(function(a,b){
		var c = new Date(a.date);
		var d = new Date(b.date);
		return c-d;
	});
}