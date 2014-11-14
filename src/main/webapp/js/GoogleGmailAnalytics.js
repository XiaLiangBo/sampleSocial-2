(function(){
	
	brite.registerView("GoogleGmailAnalytics",{parent:".GoogleScreen-content",emptyParent:true},{
		create: function(data, config){
			return render("tmpl-GoogleGmailAnalytics");
		},

		postDisplay: function (data, config) {
			var view = this;
			showGmailAnalytics.call(view);
		}
		
	});

	function showGmailAnalytics() {
        return brite.display("DataTable", ".GoogleGmailAnalytics", {
            dataProvider: {list: function(params){
                return app.googleApi.getGmailAnalytics(params);
            }},
            rowAttrs: function(obj){ 
            	return "data-id='{0}'".format(obj.id)
            },
            columnDef:[
                {
                    text:"#",
                    render: function(obj, idx){return idx + 1},
                    attrs:"style='width: 5%'"
                },
                {
                    text:"Message Subject",
                    attrs: "style='width: 10%;'",
                    render:function(obj){
                    	return obj.messageSubject
                    }

                },
                {
                    text:"Convetsation Name",
                    attrs: "style='width: 10%;'",
                    render:function(obj){
                    	return obj.convetsationName
                    }

                },
                {
                    text:"Sender Email Address",
                    attrs: "style='width: 10%;'",
                    render:function(obj){
                    	return obj.senderEmailAddress
                    }
                },
                {
                    text:"Recipient Email Address",
                    attrs: "style='width: 10%;'",
                    render:function(obj){
                    	return obj.recipientEmailAddress
                    }
                },
                {
                    text:"Message Type",
                    attrs: "style='width: 10%;'",
                    render:function(obj){
                    	return obj.messageType
                    }
                },
                {
                    text:"Recipient Type",
                    attrs: "style='width: 10%;'",
                    render:function(obj){
                    	return obj.recipientType
                    }
                },
                {
                    text:"Attachments Number",
                    attrs: "style='width: 10%;'",
                    render:function(obj){
                    	return obj.countOfAttachments
                    }
                },
                {
                    text:"Message Size",
                    attrs: "style='width: 10%;'",
                    render:function(obj){
                    	return obj.messageSize
                    }
                },
                {
                    text:"Message Length",
                    attrs: "style='width: 10%;'",
                    render:function(obj){
                    	return obj.messageLength
                    }
                }
            ],
            opts:{
                htmlIfEmpty: "Not Gmail Analytics found!",
                withPaging: true,
                withCmdDelete:false
            }
        });
    }
})();