package com.farthestgate.android.pubnub;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.farthestgate.android.CeoApplication;
import com.farthestgate.android.helper.AppConstant;
import com.farthestgate.android.helper.DBHelper;
import com.farthestgate.android.helper.ErrorLocations;

import com.farthestgate.android.helper.fused.MyService;
import com.farthestgate.android.model.PCN;
import com.farthestgate.android.model.database.PCNTable;
import com.farthestgate.android.model.database.SyncInfoTable;
import com.farthestgate.android.ui.components.RemovalPhotoService;
import com.farthestgate.android.ui.components.Utils;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;

import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNMembershipResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNSpaceResult;
import com.pubnub.api.models.consumer.pubsub.objects.PNUserResult;
//import com.pubnub.api.Pubnub;

import org.droidparts.util.L;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class PubNubModule {

    Context context;
    String type;
    private static final int HISTORY_LIMIT = 100;

    public interface ResponseListener
    {
        void OnSuccess();
        void OnFailure();
    }
    public interface PostResponseListener
    {
        void OnPostSuccess(String pcnNumber);
        void OnPostFailure(String pcnNumber);
    }

    public interface BackOfficePcnResponse{
        void onBackOfficePcnSuccess(String pcnNumber);
        void onBackOfficePcnFailure(String pcnNumber);
    }

    ResponseListener mListener;
    PostResponseListener mPostListener;
    BackOfficePcnResponse mBackOfficePcnResponse;

    public PubNubModule(Context context, String type) {
        this.context = context;
        this.type = type;
    }

    public PubNubModule(Context context) {
        this.context = context;
        mListener = (ResponseListener) context;
        mPostListener  = (PostResponseListener) context;
        mBackOfficePcnResponse = (BackOfficePcnResponse)context;
    }

    /*public void disconnectAndResubscribe() {
        try {
            if(!CeoApplication.UsePubNub()) return;
            PubNub pubnub = CeoApplication.getPubnubInstance();
            pubnub.disconnectAndResubscribe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectAndResubscribe(String channelName) {
        try {
            if(!CeoApplication.UsePubNub()) return;
            Pubnub pubnub = CeoApplication.getPubnubInstance();
            pubnub.disconnectAndResubscribe(channelName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public void  publishError(String error, int location, String version) {
        try {
            if(!CeoApplication.UsePubNub()) return;
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("version",version);
            jsonObject.put("error",error);
            jsonObject.put("location",location);
            PubNub pubnub = CeoApplication.getPubnubInstance();
            HashMap msg = new Gson().fromJson(jsonObject.toString(), HashMap.class);
            pubnub.publish()
                    .message(msg)
                    .channel(CeoApplication.ErrorChannel())
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            if (status.isError()) {
                                Log.e("Error publishing:", result!=null?result.toString():"Failure");
                                return;
                            }
                            Log.i("Received response:", result!=null?result.toString():"Success");
                        }
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  void publish(Context context,PCN pcnInfo,Object jsonObject) {
        try {
            if(!CeoApplication.UsePubNub()) return;
            JSONObject fullPCN = (JSONObject) jsonObject;
            JSONObject pcnObject = fullPCN.getJSONObject("pcn");
            String pcnNumber = pcnObject.getString("ticketserialnumber");
            PubNub pubnub = CeoApplication.getPubnubInstance();
            Hashtable msg = new Gson().fromJson(jsonObject.toString(), Hashtable.class);
            subscribe(context,pcnInfo,CeoApplication.SubscribeChannel() + pcnNumber);
            pubnub.publish()
                    .message(msg)
                    .channel(CeoApplication.getChannel())
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            if (status!=null&&status.isError()) {
                                Log.e("Error publishing msg:", result!=null?result.toString():"Error");
                                mListener.OnFailure();
                                return;
                            }
                            Log.i("Received response:", result!=null?result.toString():"Success");
                            if(mListener!=null){
                                mListener.OnSuccess();
                            }

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void publishUnsentPCN(Context context,PCN pcnInfo,Object jsonObject) {
        try {
            JSONObject fullPCN = (JSONObject) jsonObject;
            JSONObject pcnObject = fullPCN.getJSONObject("pcn");
            final String pcnNumber = pcnObject.getString("ticketserialnumber");
            if (!CeoApplication.UsePubNub()) {
                mPostListener.OnPostFailure(pcnNumber);
                return;
            }
            PubNub pubnub = CeoApplication.getPubnubInstance();
            HashMap msg = new Gson().fromJson(jsonObject.toString(), HashMap.class);
            subscribe(context,pcnInfo,CeoApplication.SubscribeChannel() + pcnNumber);

            pubnub.publish()
                    .message(msg)
                    .channel(CeoApplication.getChannel())
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            if (status.isError()) {
                                Log.e("Error publishing PCN : ", result!=null?result.toString():"Error");
                                if(mPostListener!=null){
                                    mPostListener.OnPostFailure(pcnNumber);
                                }
                                return;
                            }
                            Log.i("Received response PCN:", result!=null?result.toString():"Success");
                            if(mPostListener!=null){
                                mPostListener.OnPostSuccess(pcnNumber);
                            }


                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



     private void subscribe(final Context context, final PCN pcnInfo, final String channelName) {

         try {
             if (!CeoApplication.UsePubNub()) return;
             final PubNub pubNub = CeoApplication.getPubnubInstance();
             pubNub.addListener(new SubscribeCallback() {
                 @Override
                 public void status(PubNub pubnub, PNStatus status) {
                     if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                     } else if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                         if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                             pubnub.publish().channel(channelName).message(channelName).async(new PNCallback<PNPublishResult>() {
                                 @Override
                                 public void onResponse(PNPublishResult result, PNStatus status) {
                                     if(status.isError()){
                                         Log.e("Error on subscribe:", result!=null?result.toString():"Error");
                                     }
                                     Log.e("Success on subscribe:", result!=null?result.toString():"Success");
                                 }
                             });
                         }
                     } else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                         Log.i("PNReconnectedCategory", "PNReconnectedCategory");
                     } else if (status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory) {
                         Log.i("PNDecryption", "PNDecryptionErrorCategory");
                     }
                 }

                 @Override
                 public void message(PubNub pubnub, PNMessageResult message) {
                     try {


                         CeoApplication.LogInfo("Ack message from back office:  "+ message.getMessage().getAsJsonObject().toString());


                         JSONObject responseObject = new JSONObject(message.getMessage().getAsJsonObject().toString());
                         if (responseObject.has("pcn")) {
                             String pcnNumber = responseObject.getString("pcn");
                             String resMessage = responseObject.getString("message");
                             mBackOfficePcnResponse.onBackOfficePcnSuccess(pcnInfo.pcnNumber);
                             boolean status1 = Boolean.parseBoolean(responseObject.getString("status"));
                             UpdatePCNWithPostResponse(pcnNumber, resMessage, status1);
                             initiatePhotoTransmission(context,pcnInfo);
                             pubNub.unsubscribe().channels(Arrays.asList(channelName))
                                     .execute();
                         } else {
                             //We need to modify the HHT to get the “Received” message and process it
                             if (responseObject.has("received")) {
                                 boolean received = Boolean.parseBoolean(responseObject.getString("received"));
                                 String reference = responseObject.getString("reference");
                                 mBackOfficePcnResponse.onBackOfficePcnFailure(pcnInfo.pcnNumber);
                                 UpdatePCNReceivedResponse(reference, received);
                             }
                         }

                     } catch (Exception ex) {
                         ex.printStackTrace();
                         String pcnNumber = channelName.split("_")[1];
                         UpdatePCNWithPostResponse(pcnNumber, "", false);
                         mBackOfficePcnResponse.onBackOfficePcnFailure(pcnInfo.pcnNumber);
                         //pubNub.unsubscribe().channels(Arrays.asList(channelName)).execute();
                     }
                 }

                 @Override
                 public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                     Log.i("presence", "pnSignapresencelResult");
                 }


                 @Override
                 public void signal(@NotNull PubNub pubNub, @NotNull PNSignalResult pnSignalResult) {
                     Log.i("pnSignalResult", "pnSignalResult");
                 }

                 @Override
                 public void user(@NotNull PubNub pubNub, @NotNull PNUserResult pnUserResult) {
                     Log.i("pnUserResult", "pnUserResult");
                 }

                 @Override
                 public void space(@NotNull PubNub pubNub, @NotNull PNSpaceResult pnSpaceResult) {
                     Log.i("pnSpaceResult", "pnSpaceResult");
                 }

                 @Override
                 public void membership(@NotNull PubNub pubNub, @NotNull PNMembershipResult pnMembershipResult) {
                     Log.i("pnMembershipResult", "pnMembershipResult");
                 }

                 @Override
                 public void messageAction(@NotNull PubNub pubNub, @NotNull PNMessageActionResult pnMessageActionResult) {
                     Log.i("pnMessageActionResult", "pnMessageActionResult");
                 }
             });

             pubNub.subscribe().channels(Arrays.asList(channelName)).execute();

         }catch (Exception e){
             e.printStackTrace();
         }


    }

    private void initiatePhotoTransmission(Context context,PCN pcnInfo) {
        if (pcnInfo.dInfo.actionToTake.equalsIgnoreCase("Remove") && !CeoApplication.SendAllPhotosRealTime()) {
            List<File> files = Utils.getFiles(pcnInfo.pcnNumber);
            for (int i = 0; i < files.size(); i++) {
                try {
                    Utils.CopyDirectory(files.get(i), Utils.getFile(AppConstant.REMOVALPHOTO_FOLDER, files.get(i).getName()));
                } catch (IOException e) {
                    try {
                        CeoApplication.LogError(e.getMessage());
                        CeoApplication.LogErrorOnChannel(context, e, ErrorLocations.location402);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        Intent intent = new Intent(context, RemovalPhotoService.class);
        intent.putExtra("removal", pcnInfo.dInfo.actionToTake);
        context.startService(intent);

    }
    public static void  publishRemovalPhotos(Object jsonObject) {
        try {
            if(!CeoApplication.UsePubNub()) return;
            final PubNub pubnub = CeoApplication.getPubnubInstance();

            Hashtable msg = new Gson().fromJson(jsonObject.toString(), Hashtable.class);
            pubnub.publish()
                    .message(msg)
                    .channel(CeoApplication.getRemovalPhotoPublishChannel())
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {

                            if (status.isError()) {
                                Log.e("Removal Photo Error:", result!=null?result.toString():"Error");
                                return;
                            }
                            Log.i("Removal Photo Success:", result!=null?result.toString():"Success");
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void subscribeRemovalPhotos() {
        try {
            if (!CeoApplication.UsePubNub()) return;
            final PubNub pubnub = CeoApplication.getPubnubInstance();
            pubnub.addListener(new SubscribeCallback() {
                @Override
                public void status(PubNub pubnub, PNStatus status) {
                    if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                    } else if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                        if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                            pubnub.publish().channel(CeoApplication.getRemovalPhotoSubscribeChannel()).message(CeoApplication.getRemovalPhotoSubscribeChannel()).async(new PNCallback<PNPublishResult>() {
                                @Override
                                public void onResponse(PNPublishResult result, PNStatus status) {
                                    if (!status.isError()) {
                                        Log.i("Removal photo success:", result != null ? result.toString() : "Success");
                                    } else {
                                        Log.i("Removal photo Failure:", result != null ? result.toString() : "Failure");
                                    }
                                }
                            });
                        }
                    } else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                        Log.i("PNReconnectedCategory", "PNReconnectedCategory");
                    } else if (status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory) {
                        Log.i("PNDecryption", "PNDecryptionErrorCategory");
                    }
                }

                @Override
                public void message(PubNub pubnub, PNMessageResult message) {
                    try {
                        JSONObject responseObject = new JSONObject(message.getMessage().getAsJsonObject().toString());

                        if (Boolean.parseBoolean(responseObject.getString("received"))) {
                            Log.i("Removal photo success:", "Success");
                            if (responseObject.getString("to-uuid").equalsIgnoreCase(CeoApplication.getUUID())) {
                                String photoName = responseObject.getString("photo");
                                //make complete photo path
                                //delete photo from device path

                                File photo = Utils.getFile(AppConstant.REMOVALPHOTO_FOLDER, photoName);
                                if (photo.exists()) {
                                    Utils.CopyDirectory(photo, Utils.getFile(AppConstant.SENT_PHOTO, photo.getName()));
                                    photo.delete();
                                } else {
                                    photo = Utils.getFile(AppConstant.PHOTO_FOLDER, photoName);
                                    if (photo.exists()) {
                                        Utils.CopyDirectory(photo, Utils.getFile(AppConstant.SENT_PHOTO, photo.getName()));
                                        photo.delete();
                                    }
                                }
                            }
                        }

                    } catch (Exception ex) {
                        //FirebaseCrashlytics.getInstance().recordException(ex);
                        ex.printStackTrace();
                    }
                }

                @Override
                public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                    Log.i("presence", "pnSignapresencelResult");
                }


                @Override
                public void signal(@NotNull PubNub pubNub, @NotNull PNSignalResult pnSignalResult) {
                    Log.i("pnSignalResult", "pnSignalResult");
                }

                @Override
                public void user(@NotNull PubNub pubNub, @NotNull PNUserResult pnUserResult) {
                    Log.i("pnUserResult", "pnUserResult");
                }

                @Override
                public void space(@NotNull PubNub pubNub, @NotNull PNSpaceResult pnSpaceResult) {
                    Log.i("pnSpaceResult", "pnSpaceResult");
                }

                @Override
                public void membership(@NotNull PubNub pubNub, @NotNull PNMembershipResult pnMembershipResult) {
                    Log.i("pnMembershipResult", "pnMembershipResult");
                }

                @Override
                public void messageAction(@NotNull PubNub pubNub, @NotNull PNMessageActionResult pnMessageActionResult) {
                    Log.i("pnMessageActionResult", "pnMessageActionResult");
                }
            });

            pubnub.subscribe().channels(Arrays.asList(CeoApplication.getRemovalPhotoSubscribeChannel())).execute();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void unSubscribeRemovalPhotoChannel(){
        final PubNub pubnub = CeoApplication.getPubnubInstance();
        //pubnub.unsubscribe(/*CeoApplication.getRemovalPhotoSubscribeChannel()*/);
        pubnub.unsubscribe()
                .channels(Arrays.asList(CeoApplication.getRemovalPhotoSubscribeChannel()))
                .execute();
    }

    public static void  publishCeoTracking(Object jsonObject) {
        try {
            if(!CeoApplication.UsePubNub()) return;

            PubNub pubnub = CeoApplication.getPubnubInstance();

            Hashtable msg = new Gson().fromJson(jsonObject.toString(), Hashtable.class);
            pubnub.publish()
                    .message(msg)
                    .channel(CeoApplication.getCeoTrackingChannel())
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            if (status.isError()) {
                                Log.e("CEO tracking error:", result!=null?result.toString():"Error");
                                return;
                            }
                            Log.i("CEO tracking success:", result!=null?result.toString():"Success");
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void  publishCodeRed(Object jsonObject) {
        try {
            if(!CeoApplication.UsePubNub()) return;

            PubNub pubnub = CeoApplication.getPubnubInstance();
            Hashtable msg = new Gson().fromJson(jsonObject.toString(), Hashtable.class);

            pubnub.publish()
                    .message(msg)
                    .channel(CeoApplication.getCodeRedChannel())
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            if (status.isError()) {
                                Log.e("CODE RED error:", result!=null?result.toString():"Error");
                                return;
                            }
                            Log.i("CODE RED success:", result!=null?result.toString():"Success");
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void  publishSyncConfirmation(final String infoStatus) {
        try {
            if(!CeoApplication.UsePubNub()) return;
            List<SyncInfoTable> syncInfoTables=DBHelper.getSyncInfo();
            if(syncInfoTables==null){
                return;
            }
            for (final SyncInfoTable syncInfo:syncInfoTables){
                PubNub pubnub = CeoApplication.getPubnubInstance();
                Hashtable msg = new Gson().fromJson(syncInfo.toJSONObject().toString(), Hashtable.class);

                pubnub.publish()
                        .message(msg)
                        .channel(CeoApplication.getSyncConfirmationChannel())
                        .async(new PNCallback<PNPublishResult>() {
                            @Override
                            public void onResponse(PNPublishResult result, PNStatus status) {
                                if (status.isError()) {
                                    Log.e("Sync error:", result!=null?result.toString():"Error");
                                    return;
                                }
                                Log.i("Sync success:", result!=null?result.toString():"Success");
                                syncInfo.setSent(infoStatus);
                                syncInfo.save();
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void publishBlackWhiteListVehicle(Object jsonObject){
        try {
            if(!CeoApplication.UsePubNub()) return;
            PubNub pubnub = CeoApplication.getPubnubInstance();

            Hashtable msg = new Gson().fromJson(jsonObject.toString(), Hashtable.class);
            pubnub.publish()
                    .message(msg)
                    .channel(CeoApplication.getBlackWhiteListVehicleChannel())
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            if (status.isError()) {
                                Log.i("Special vehicle error:", result!=null?result.toString():"Error");
                                return;
                            }
                            Log.e("SP vehicle success:", result!=null?result.toString():"Success");
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void  publishBriefingNotes(Object jsonObject) {
        try {

            if(!CeoApplication.UsePubNub()) return;
            PubNub pubnub = CeoApplication.getPubnubInstance();
            Hashtable msg = new Gson().fromJson(jsonObject.toString(), Hashtable.class);
            pubnub.publish()
                    .message(msg)
                    .channel(CeoApplication.getMessageReadChannel())
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            if (status.isError()) {
                                Log.e("Briefing notes Error:", result!=null?result.toString():"Failure");
                                return;
                            }
                            Log.i("Briefing notes Success:", result!=null?result.toString():"Success");
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void UpdatePCNWithPostResponse(String pcnNumber, String resMessage, boolean status)
    {
        PCNTable finalPcn;
        List<PCNTable> PCNs = DBHelper.GetPCN(pcnNumber);
        if (PCNs.size() > 0)
        {
            finalPcn = PCNs.get(0);
            finalPcn.setSyncOutcome(resMessage);
            finalPcn.setSyncStatus(status);
            finalPcn.save();
        }
    }

    private void UpdatePCNReceivedResponse(String pcnNumber, boolean received)
    {
        PCNTable finalPcn;
        List<PCNTable> PCNs = DBHelper.GetPCN(pcnNumber);
        if (PCNs.size() > 0)
        {
            finalPcn = PCNs.get(0);
            finalPcn.setReceivedStatus(received);
            finalPcn.save();
        }
    }

   /* public void history() {
        try {
            if(!CeoApplication.UsePubNub()) return;
            Pubnub pubnub = CeoApplication.getPubnubInstance();
            pubnub.detailedHistory(CeoApplication.getChannel(), HISTORY_LIMIT, new  com.pubnub.api.Callback() {
                public void successCallback(String channel, Object message) {

                    Log.i("Received history msg:", message.toString());
                }

                public void errorCallback(String channel, Object message) {
                    Log.e("Error history:", message.toString());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

}