package com.britesnow.samplesocial.feed;


import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import com.britesnow.samplesocial.dao.GmailAnalyticsDao;
import com.britesnow.samplesocial.entity.GmailAnalytics;
import com.britesnow.samplesocial.entity.User;
import com.britesnow.samplesocial.service.GmailRestService;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.inject.Inject;

public class FeedGmailAnalyticsJob implements Callable<HashMap<String,String>> {

	@Inject
	private GmailAnalyticsDao gmailAnalyticsDao;

	@Inject
	private GmailRestService gmailRestService;
	
	private User user;

	public void init(User user) {
		this.user = user;
	}

	@Override
	public HashMap<String,String> call(){
		System.out.printf("the ID %s user's task has start!\n",user.getId());
		HashMap<String,String> result = new HashMap<String,String>();
		result.put("name", user.getUsername());
		try {
			HashMap datas = gmailRestService.gmailMessageList(user.getGoogle_access_token(), null, 50);
			String start = datas.get("start").toString();
			while(!Strings.isNullOrEmpty(start)){
				storageGmailAnalytics((List<Message>)datas.get("values"));
				datas = gmailRestService.gmailMessageList(user.getGoogle_access_token(), start, 50);
				start = datas.get("start").toString();
			}
		} catch (Exception e) {
			System.out.printf("the ID %s user's task has occur an error and field!\n",user.getId());
			e.printStackTrace();
			result.put("success", "false");
			return result;
		}
		result.put("success", "true");
		return result;
	}

	private boolean storageGmailAnalytics(List<Message> messages){
		GmailAnalytics gmailAnalytics = null;
		Message message = null;
		for(int i = 0, j = messages.size(); i < j; i++){
			message = messages.get(i);
			if(message.getPayload() != null){
				gmailAnalytics = buildAnalyticsfo(message);
				gmailAnalytics.setMessageSize(Integer.toUnsignedLong(message.getSizeEstimate()));
				gmailAnalyticsDao.create(user, gmailAnalytics);
			}
		}
		return true;
	}
	
	public GmailAnalytics buildAnalyticsfo(Message message) {
		GmailAnalytics gmailAnalytics = new GmailAnalytics();
		//DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
		//DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z z", Locale.US);
        if(message.getPayload() != null){
            if(message.getPayload().getHeaders() != null){
            	StringBuilder recipientAddress = new StringBuilder();
                for(MessagePartHeader header : message.getPayload().getHeaders()){
                    if(header.getName().equals("Subject")){
                    	gmailAnalytics.setMessageSubject(header.getValue());
                    }
                    if(header.getName().equals("From")){
                    	gmailAnalytics.setSenderEmailAddress(header.getValue());
                    }
                    if(header.getName().equals("to")){
                    	String[] cc = header.getValue().split(",");
                    	for(String value : cc){
                    		recipientAddress.append(value);
                    	}
                    }
                    if(header.getName().equals("Cc")){
                    	String[] cc = header.getValue().split(",");
                    	for(String value : cc){
                    		recipientAddress.append(value);
                    	}
                    }
                    if(header.getName().equals("Bcc")){
                    	String[] cc = header.getValue().split(",");
                    	for(String value : cc){
                    		recipientAddress.append(value);
                    	}
                    }
                    if(header.getName().equals("Date")){
                        //gmailAnalytics.setRecipientTimeStamp(LocalDateTime.parse(String.valueOf(header.getValue()), DateTimeFormatter.RFC_1123_DATE_TIME));
                    	//gmailAnalytics.setRecipientTimeStamp(LocalDateTime.parse(header.getValue(), dateFormat));
                    }
                }
                gmailAnalytics.setRecipientEmailAddress(recipientAddress.toString());
            }
        }
    	return gmailAnalytics;
    }
}