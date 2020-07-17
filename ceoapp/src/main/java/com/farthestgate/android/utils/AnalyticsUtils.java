package com.farthestgate.android.utils;

import android.os.Bundle;

public class AnalyticsUtils {

    public static void trackLogin(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "LoginScreen");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.LOGIN, parameters);
    }


    public static void trackDeviceSync(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "SuperVisorScreen");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.SYNC, parameters);
    }

    public static void trackStartOfDay(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "StartOfDayScreen");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.STARTOFDAYCONTINUE, parameters);
    }
    public static void trackLocationPopUp(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "Liberator");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.LOCATIONPOPUP, parameters);
    }

    public static void trackPCNButton(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "Liberator");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.PCNBUTTONCLICKED, parameters);
    }


    public static void trackLiberatorContinue(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "Liberator");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.LIBERATORCONTINUE, parameters);
    }

    public static void trackParkingSessionDialog(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "Liberator");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.PARKINGSESSIONDIALOG, parameters);
    }

    public static void trackANPRButton(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "Liberator");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.ANPRBUTTON, parameters);
    }
    public static void trackMessagesButton(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "Liberator");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.MESSAGESBUTTON, parameters);
    }
    public static void trackDetectsButton(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "Liberator");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.DETECTSSBUTTON, parameters);
    }
    public static void trackVRMLookUpButton(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "Liberator");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.VRMLOOKUPBUTTON, parameters);
    }

    public static void trackGalleryButton(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "Liberator");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.GALLERYBUTTON, parameters);
    }

    public static void trackCodeRedButton(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "Liberator");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.CODREDBUTTON, parameters);
    }
    public static void trackTakePhotoButton(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "FinalScreen");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.TAKEPHOTOBUTTON, parameters);
    }
    public static void trackNoteskButton(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "FinalScreen");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.NOTESBUTTON, parameters);
    }

    public static void trackFinalPrintButton(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "FinalScreen");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.FINALPRINTBUTTON, parameters);
    }
    public static void trackMenuPrinter(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "MenuBar");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.MENUPRINTOR, parameters);
    }
    public static void trackMenuEndOfDay(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "MenuBar");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.MENUENDOFDAY, parameters);
    }
    public static void trackMenuBreak(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "MenuBar");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.MENUBREAK, parameters);
    }

    public static void trackMenuNotesList(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "MenuBar");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.MENUNOTESLIST, parameters);
    }
    public static void trackMenuInTransit(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "MenuBar");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.MENUINTRANSIT, parameters);
    }


    public static void trackDestinaitonDialog(){
        Bundle parameters = new Bundle();
        parameters.putString("screen_name", "DestinationDialog");
        FirebaseAnalyticsController.getInstance().firebaseTrack(EventName.DESTINATIONDIALOG, parameters);
    }

    public static final class EventName {
        public static final String LOGIN = "LoginClicked";
        public static final String SYNC = "StartSync";
        public static final String STARTOFDAYCONTINUE = "StartOfDayContinue";
        public static final String LOCATIONPOPUP = "LocationPopUp";
        public static final String PCNBUTTONCLICKED = "PCNButtonClicked";
        public static final String LIBERATORCONTINUE = "LiberatorContinue";
        public static final String PARKINGSESSIONDIALOG = "ParkingSessionDialog";
        public static final String ANPRBUTTON = "ANPRButtonClicked";
        public static final String MESSAGESBUTTON = "MessagesButtonClicked";
        public static final String DETECTSSBUTTON = "DetectsButtonClicked";
        public static final String VRMLOOKUPBUTTON = "VRMLookUpButtonClicked";
        public static final String GALLERYBUTTON = "GalleryButtonClicked";
        public static final String CODREDBUTTON = "CodeRedButtonClicked";
        public static final String TAKEPHOTOBUTTON = "TakePhotoButtonClicked";
        public static final String NOTESBUTTON = "NotesButtonClicked";
        public static final String FINALPRINTBUTTON = "FinalprintButtonClicked";
        public static final String MENUPRINTOR = "MenuPrinter";
        public static final String MENUENDOFDAY = "MenuEndOfDay";
        public static final String MENUBREAK = "MenuBreak";
        public static final String MENUNOTESLIST = "MenuNotesList";
        public static final String MENUINTRANSIT = "MenuInTransit";
        public static final String DESTINATIONDIALOG = "DestinationDialog";



    }


}
