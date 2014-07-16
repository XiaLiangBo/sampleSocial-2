package com.britesnow.samplesocial.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.britesnow.snow.util.Pair;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Event.Reminders;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GoogleCalendarEventsService {

    @Inject
    GoogleAuthService authService;
    
    public Pair<String, List<Map>> listEvents(String pageIndex, int pageSize,String startDate, String endDate, String calendarId){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        com.google.api.services.calendar.Calendar.Events.List list = null;
        if(calendarId == null || calendarId.equals("")){
            calendarId = "primary";
        }
        try {
           list = getCalendarService().events().list(calendarId).setMaxResults(pageSize).setOrderBy("startTime").setSingleEvents(true);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        if(startDate != null && !startDate.equals("")){
            DateTime minTime = null;
            Date min = null;
            try {
                min = sdf.parse(startDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            minTime = new DateTime(min.getTime());
            list = list.setTimeMin(minTime);
        }
        
        if(endDate != null && !endDate.equals("")){
            Date max = null;
            try {
                max = sdf.parse(endDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            DateTime maxTime = new DateTime(max.getTime());
            list = list.setTimeMax(maxTime);
        }
        
        if(pageIndex != null && !pageIndex.equals("") && !pageIndex.equals("0")){
            list.setPageToken(pageIndex);
        }
        
        String pageToken = null;
        try {
            Events events = list.execute();
            List<Event> items = events.getItems();
            List<Map> eventList = new ArrayList();
            for (Event event : items) {
                Map eventMap = new HashMap();
                eventMap.put("summary", event.getSummary());
                eventMap.put("id", event.getId());
                eventMap.put("date", event.getStart());
                eventMap.put("location", event.getLocation());
                eventMap.put("status", event.getStatus());
                eventMap.put("calendarId", event.getOrganizer().getEmail());
                eventList.add(eventMap);
            }
            pageToken = events.getNextPageToken();
           return new Pair<String, List<Map>>(pageToken, eventList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public Map getEvent(String eventId,String calendarId){
        try {
            if(calendarId == null || calendarId.equals("")){
                calendarId = "primary";
            }
            Map eventMap = new HashMap();
            Event event = null;
            event = getCalendarService().events().get(calendarId, eventId).execute();
            
            eventMap.put("id", event.getId());
            eventMap.put("summary", event.getSummary());
            eventMap.put("reminders", event.getReminders());
            if(event.getEnd() != null && event.getEnd().getDateTime() != null){
                eventMap.put("endTime", new Date(event.getEnd().getDateTime().getValue()).getTime());
            }
            if(event.getStart() != null && event.getStart().getDateTime() != null){
                eventMap.put("startTime", new Date(event.getStart().getDateTime().getValue()).getTime());
            }
            eventMap.put("location", event.getLocation());
            eventMap.put("calendarId", event.getOrganizer().getEmail());
            StringBuilder inviters = new StringBuilder();
            int i = 0;
            if(event.getAttendees() != null){
                for(EventAttendee eventAtte : event.getAttendees()){
                    if(i != 0){
                        inviters.append(",");
                    }
                    inviters.append(eventAtte.getEmail());
                    i++;
                }
            }
            eventMap.put("inviters", inviters.toString());
            return eventMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void saveEvent(String eventId, String summary, String location, String startTime, String endTime, Integer min,String calendarId,String[] inviter){
        try {
            boolean create = false;
            Event event = null;
            if(calendarId == null || calendarId.equals("")){
                calendarId = "primary";
            }
            if(eventId == null || "".equals(eventId)){
                event = new Event();
                create = true;
            }else{
                event = getCalendarService().events().get(calendarId, eventId).execute();
                create = false;
            }
            event.setSummary(summary);
            event.setLocation(location);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if (startTime != null && !startTime.equals("")) {
                Date start = null;
                try {
                    start = sdf.parse(startTime);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                DateTime stime = new DateTime(start, TimeZone.getTimeZone("UTC"));
                event.setStart(new EventDateTime().setDateTime(stime));
            }else{
                event.setStart(new EventDateTime().setDateTime(new DateTime(new Date(), TimeZone.getTimeZone("UTC"))));
            }
            
            if (endTime != null && !endTime.equals("")) {
                Date end = null;
                try {
                    end = sdf.parse(endTime);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                DateTime etime = new DateTime(end, TimeZone.getTimeZone("UTC"));
                event.setEnd(new EventDateTime().setDateTime(etime));
            }else{
                event.setEnd(new EventDateTime().setDateTime(new DateTime(new Date(), TimeZone.getTimeZone("UTC"))));
            }
            if (min != null && min > 0){
                EventReminder eventReminder = new EventReminder();
                eventReminder.setMethod("email");
                eventReminder.setMinutes(min);
                List<EventReminder> eventReminders = new ArrayList();
                eventReminders.add(eventReminder);
                
                Reminders reminders = new Reminders();
                reminders.setOverrides(eventReminders);
                reminders.setUseDefault(false);
                
                event.setReminders(reminders);
            }
            
            if(inviter != null && inviter.length !=0){
                List<EventAttendee> attendees = new ArrayList<EventAttendee>();
                for(int i=0; i<inviter.length; i++){
                    EventAttendee eventAtte = new EventAttendee();
                    eventAtte.setEmail(inviter[i]);
                    attendees.add(eventAtte);
                }
                event.setAttendees(attendees);
            }
            
            if(create){
                getCalendarService().events().insert(calendarId, event).execute();
            }else{
                getCalendarService().events().update(calendarId, event.getId(), event).execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void deleteEvent(String eventId, String calendarId){
        try {
            if(calendarId == null || calendarId.equals("")){
                calendarId = "primary";
            }
            getCalendarService().events().delete(calendarId, eventId).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private Calendar getCalendarService(){
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        GoogleCredential credential = new GoogleCredential().setAccessToken(authService.getSocialIdEntity().getToken());
        Calendar service = new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName("Gmail Test").build();
        return service;
    }

}