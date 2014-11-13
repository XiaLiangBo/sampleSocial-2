var app = app || {};
(function() {
	app.liveContactApi = {
		"getUserInfo" : function() {
			var param = {};
			param.method = "Get";
			return app.getJsonData(contextPath + "/live/OutLook/getUserInfo", param);
		},
		"getUserContactlist" : function() {
			var param = {};
			param.method = "Get";
			return app.getJsonData(contextPath + "/live/OutLook/getUserContactlist", param);
		},
        "saveContact": function (contact) {
            return app.getJsonData(contextPath + "/liveContact/saveContact", contact);
        },
		"getContact" : function(id) {
			var param = {id:id};
			param.method = "Get";
			return app.getJsonData(contextPath + "/liveContact/getContact", param);
		}
	}
	
})();
