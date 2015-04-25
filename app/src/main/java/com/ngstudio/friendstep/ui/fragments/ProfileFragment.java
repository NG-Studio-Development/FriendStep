package com.ngstudio.friendstep.ui.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alexutils.helpers.BitmapUtils;
import com.alexutils.utility.FileUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ngstudio.friendstep.R;
import com.ngstudio.friendstep.WhereAreYouApplication;
import com.ngstudio.friendstep.components.NotificationManager;
import com.ngstudio.friendstep.model.connectivity.BaseResponseCallback;
import com.ngstudio.friendstep.model.connectivity.HttpServer;
import com.ngstudio.friendstep.model.connectivity.requests.BaseAvatarRequest;
import com.ngstudio.friendstep.model.connectivity.requests.BaseContactRequest;
import com.ngstudio.friendstep.model.entity.Contact;
import com.ngstudio.friendstep.model.entity.ContactLocation;
import com.ngstudio.friendstep.model.entity.step.ContactStep;
import com.ngstudio.friendstep.ui.activities.MapForPushActivity;
import com.ngstudio.friendstep.ui.activities.ProfileActivity;
import com.ngstudio.friendstep.ui.adapters.ItemsAdapterOLD;
import com.ngstudio.friendstep.ui.dialogs.AlertDialogBase;
import com.ngstudio.friendstep.ui.widgets.AdapterLinearLayout;
import com.ngstudio.friendstep.ui.widgets.SearchView;
import com.ngstudio.friendstep.utils.CommonUtils;
import com.ngstudio.friendstep.utils.WhereAreYouAppConstants;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ProfileFragment extends BaseFragment<ProfileActivity> implements NotificationManager.Client {

    private static final int REQUEST_CODE_CHOOSE_IMAGE = 2;
    private static final int REQUEST_CODE_CAPTURE_IMAGE = 3;
    private static final String TEMPLATE_RESULT_REQUEST = "[{\"username\"";

    //private static final String TEMP_PHOTO_NAME = "qaza.jpg";
    //private static final String TEMP_MOBILE_NUMBER = "0998889988";

    ImageButton avatarMenu, delete, capture, gallery;
    AdapterLinearLayout photoControls;
    LinearLayout profileButtons;
    RelativeLayout contactData;

    ImageView avatar;
    TextView profileName;
    TextView contactMobile;
    TextView contactStatus;
    AlertDialogBase dialogSendLocation;
    ContactStep currentContact;
    ItemsAdapterOLD adapter;
    File file;

    // ---- TEMP VARIABLES ---- //
    private double temp_lat = 0.0;
    private double temp_lng = 0.0;
    private final String TEMP_DIALOG_MESSAGE = "temp hard dialog message";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationManager.registerClient(this);
        if(getArguments() != null) {
            currentContact = (ContactStep) getArguments().getSerializable(WhereAreYouAppConstants.KEY_CONTACT);
        }
        getHostActivity().initActionBar(currentContact == null);
        //getActivity().getActionBar().hide();



    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NotificationManager.unregisterClient(this);
    }


    @Override
    public int getLayoutResID() {
        return R.layout.fragment_profile;
    }

    @Override
    public void findChildViews(@NotNull View view) {
        avatarMenu = (ImageButton) view.findViewById(R.id.ibAvatarMenu);
        avatar = (ImageView) view.findViewById(R.id.ivAvatar);
        delete = (ImageButton) view.findViewById(R.id.ibDelete);
        capture = (ImageButton) view.findViewById(R.id.ibCapture);
        gallery = (ImageButton) view.findViewById(R.id.ibGallery);
        photoControls = (AdapterLinearLayout) view.findViewById(R.id.lnOptions);
        profileButtons = (LinearLayout) view.findViewById(R.id.lnProfileButtons);
        profileName = (TextView) view.findViewById(R.id.tvProfileName);
        contactData = (RelativeLayout) view.findViewById(R.id.rlContactData);
        contactMobile = (TextView) view.findViewById(R.id.tvContactMobile);
        contactStatus = (TextView) view.findViewById(R.id.tvContactStatus);

        if(currentContact != null) {
            photoControls.setAdapter(adapter = ItemsAdapterOLD.getProfileItemsAdapter(getActivity()));
            photoControls.setOnItemClickListener(new AdapterLinearLayout.OnItemClickListener() {
                @Override
                public void onItemClick(Adapter adapter, int pos, View v) {
                    selectItem(pos);
                }
            });
            avatarMenu.setImageResource(R.drawable.drawable_ic_chat);
            avatarMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //ChatActivity.startActivty(getActivity(), currentContact);
                }
            });
            //profileName.setText(currentContact.getContactname());

            /*contactData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentContact.getContact_status().equals(Contact.Status.pending.name()))
                        dialogSendLocation.show();
                }
            });*/

            contactData.setVisibility(View.VISIBLE);
            //contactMobile.setText("+" + currentContact.getMobilenumber());
            //contactStatus.setText(String.format(getString(R.string.text_contact_status),currentContact.getContact_status()));

            dialogSendLocation = new AlertDialogBase(getActivity());
            dialogSendLocation.setCustomView(R.layout.dialog_profile_approve_contact);
            dialogSendLocation.addPositiveButton(R.string.dialog_button_positive_generic,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendStatus(Contact.Status.approve.name(),TEMP_DIALOG_MESSAGE,new LatLng(temp_lat,temp_lng));
                }
            });
            dialogSendLocation.addNegativeButton(R.string.dialog_button_negative_generic, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendStatus(Contact.Status.decline.name(),TEMP_DIALOG_MESSAGE,new LatLng(temp_lat,temp_lng));
                }
            });

            getHostActivity().getActionBarHolder().setMenuItemClickListener(R.id.ivDelete, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*final BaseContactRequest request = BaseContactRequest.createDeleteContactRequest(WhereAreYouApplication.getInstance().getUuid(),currentContact.getMobilenumber());
                    HttpServer.submitToServer(request,new BaseResponseCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            if(!TextUtils.isEmpty(result) && result.contains("Contact Deleted")) {
                                getActivity().finish();
                            }
                        }

                        @Override
                        public void onError(Exception error) {}
                    });*/
                }
            });

            getHostActivity().getActionBarHolder().setMenuItemClickListener(R.id.ivEdit, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getHostActivity().getActionBarHolder().expandSearchField(v);
                }
            });

            getHostActivity().getActionBarHolder().setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public void onQueryTextSubmit(final String query) {
                    if (TextUtils.isEmpty(query)) {
                        Toast.makeText(getActivity(), R.string.toast_empty_text, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    getHostActivity().showProgressDialog();
                    //BaseContactRequest renameRequest = BaseContactRequest.createRenameContactRequest(WhereAreYouApplication.getInstance().getUuid(), currentContact.getMobilenumber(), query);
                    /*HttpServer.submitToServer(renameRequest, new BaseResponseCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            getHostActivity().hideProgressDialog();
                            if (!TextUtils.isEmpty(result) && result.contains("Contact renamed sucessfully")) {
                                profileName.setText(query);
                                getHostActivity().getActionBarHolder().collapseSearchField(getHostActivity().getActionBarHolder().findViewById(R.id.ivEdit));
                            }
                        }

                        @Override
                        public void onError(Exception error) {
                            getHostActivity().hideProgressDialog();
                        }
                    }); */
                }

                @Override
                public void onQueryTextChange(String newText) {

                }
            });

            getHostActivity().getActionBarHolder().setSearchField(R.drawable.drawable_ic_edit, null);

        } else {
            adapter = new ItemsAdapterOLD(getActivity(),R.layout.item_menu);
            ItemsAdapterOLD.MenuItem[] items = {new ItemsAdapterOLD.MenuItem(R.drawable.drawable_item_profile, WhereAreYouApplication.getPrefString(WhereAreYouAppConstants.PREF_KEY_NAME,"")),
                    new ItemsAdapterOLD.MenuItem(R.drawable.drawable_item_phone, "+" + WhereAreYouApplication.getInstance().getCurrentMobile())};
            adapter.addAll(Arrays.asList(items));
            photoControls.setAdapter(adapter);

            avatarMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (profileButtons.getVisibility() == View.VISIBLE) {
                        profileButtons.setVisibility(View.GONE);
                        avatarMenu.setSelected(false);
                    } else {
                        profileButtons.setVisibility(View.VISIBLE);
                        avatarMenu.setSelected(true);
                    }
                }
            });

            avatarMenu.setImageResource(R.drawable.drawable_ic_camera);
            profileName.setText(R.string.text_my_profile);
            contactData.setVisibility(View.GONE);
        }

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePhoto(Calendar.getInstance().getTimeInMillis()+"jpg");
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getHostActivity().showProgressDialog();
                HttpServer.submitToServer(BaseAvatarRequest.getImagePostRequest(WhereAreYouApplication.getInstance().getUuid(), System.currentTimeMillis(), " "), new BaseResponseCallback<String>() {
                    @Override
                    public void onSuccess(String result) {
                        getHostActivity().hideProgressDialog();

                        WhereAreYouApplication.removeAvatarFromCache(WhereAreYouApplication.getInstance().getCurrentMobile());
                    }

                    @Override
                    public void onError(Exception error) {

                    }
                });
            }
        });

        /*WhereAreYouApplication.getInstance().getAvatarCache().displayImage(
                AvatarBase64ImageDownloader.getImageUriFor(currentContact == null ? WhereAreYouApplication.getInstance().getCurrentMobile() : currentContact.getMobilenumber()), avatar);*/
    }

    private void sendStatus(String status, String message, LatLng latLng){
       /* BaseContactRequest getContacts = BaseContactRequest
                .createLocationContactReplyRequest(WhereAreYouApplication.getInstance().getUuid(),
                        latLng.latitude,
                        latLng.longitude,
                        currentContact.getMobilenumber(),
                        message,
                        status);

        HttpServer.submitToServer(getContacts, new BaseResponseCallback<String>() {
            @Override
            public void onSuccess(String result) {
                NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_CONTACTS_NEARBY, result );
            }
            @Override
            public void onError(Exception error) {
                Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    public void capturePhoto(String targetFilename) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(FileUtils.getFilesDir(), Calendar.getInstance().getTimeInMillis()+"jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_CAPTURE_IMAGE);
        }
    }

    public void selectItem(int position) {
        /*ItemsAdapter.MenuItem item = adapter.getItem(position);
        switch(item.getIconId()) {
            case R.drawable.drawable_item_requests:
                queryGetContactsLocation(currentContact.getMobilenumber());
                break;
            case R.drawable.drawable_item_phone:
                Intent intentCall = new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:"+currentContact.getMobilenumber()));
                startActivity(intentCall);
                break;
            case R.drawable.drawable_item_message:
                Intent intentSendSMS = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("sms:"+currentContact.getMobilenumber()));
                startActivity(intentSendSMS);
                break;
        }*/
    }

    public void queryGetContactsLocation(String mobile) {
        BaseContactRequest getContacts = BaseContactRequest.createGetContactLocationsRequest(WhereAreYouApplication.getInstance().getUuid(), mobile);
        if( CommonUtils.isConnected(getActivity()) ) {
            HttpServer.submitToServer(getContacts, new BaseResponseCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    //Test json array
                    //result = "[{\"username\":\"art\",\"mobilenumber\":\"07999999999\",\"yourstatus\":\"approve\",\"contact_status\":\"approve\",\"contactname\":\"Test\",\"latitude\":\"54.9230370\",\"longitude\":\"73.4118280\",\"last_updated\":\"1393390884\"}]";
                    Gson gson = new Gson();
                    try {
                        if (!result.contains(TEMPLATE_RESULT_REQUEST))
                            throw new Exception();
                        List<ContactLocation> contactLocations = gson.fromJson(result, new TypeToken<List<ContactLocation>>() {
                        }.getType());
                        NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_CONTACTS_LOCATION, contactLocations);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception error) {
                    Toast.makeText(getActivity(), R.string.toast_unknown_error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void handleNotificationMessage(int what, int arg1, int arg2, Object obj) {
        if(obj == null)
            return;

        ContactLocation contactLocation;
        if (what == WhereAreYouAppConstants.NOTIFICATION_CONTACTS_LOCATION) {
            List<ContactLocation> loaded = (List<ContactLocation>) obj;
            contactLocation = loaded.get(0);
            MapForPushActivity.startMapProfile(getActivity(),contactLocation);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("CAM_WAY","yes");
        Log.d("CAM_WAY","resultCode = "+resultCode);
        if (resultCode == Activity.RESULT_OK) {
            final Uri uri;
            if (requestCode == REQUEST_CODE_CHOOSE_IMAGE) {
                uri = data.getData();
            } else {
                //file = new File(FileUtils.getFilesDir(), TEMP_PHOTO_NAME);
                uri = Uri.fromFile(file);
                //uri = data.getData();
            }

            if (uri == null)
                return;

            getHostActivity().showProgressDialog();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Bitmap avatarImage = BitmapUtils.decodeUri(getActivity(), uri, BitmapUtils.DESIRED_SIZE, BitmapUtils.DESIRED_SIZE, BitmapUtils.DecodeType.BOTH_SHOULD_BE_EQUAL_CUT);
                        final String base64Image = BitmapUtils.convertBitmapToBase64(avatarImage,false);
                        WhereAreYouApplication.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                HttpServer.submitToServer(BaseAvatarRequest.getImagePostRequest(WhereAreYouApplication.getInstance().getUuid(),System.currentTimeMillis(),base64Image),new BaseResponseCallback<String>() {
                                    @Override
                                    public void onSuccess(String result) {
                                        getHostActivity().hideProgressDialog();
                                        //String avatarUri = AvatarBase64ImageDownloader.getImageUriFor(WhereAreYouApplication.getInstance().getCurrentMobile());
                                        String avatarUri = uri.toString();
                                        WhereAreYouApplication.removeAvatarFromCache(WhereAreYouApplication.getInstance().getCurrentMobile());

                                        WhereAreYouApplication.getInstance().getAvatarCache().displayImage(avatarUri,avatar, new ImageLoadingListener() {
                                            @Override
                                            public void onLoadingStarted(String imageUri, View view) {}

                                            @Override
                                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {}

                                            @Override
                                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                                NotificationManager.notifyClients(WhereAreYouAppConstants.NOTIFICATION_USER_AVATAR_LOADED);
                                            }

                                            @Override
                                            public void onLoadingCancelled(String imageUri, View view) {}
                                        });
                                    }

                                    @Override
                                    public void onError(Exception error) {
                                        getHostActivity().hideProgressDialog();
                                    }
                                });
                            }
                        },true);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }
}
