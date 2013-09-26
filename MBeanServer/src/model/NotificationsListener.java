package model;

import java.util.ArrayList;

import javax.management.Notification;

public interface NotificationsListener extends java.util.EventListener   {
    public void handleNotifications(ArrayList<Notification> notifications, Object handback) ;
}