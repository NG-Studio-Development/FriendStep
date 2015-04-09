package com.ngstudio.friendstep.components;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import com.ngstudio.friendstep.BuildConfig;
import com.ngstudio.friendstep.utils.CommonUtils;
import com.ngstudio.friendstep.utils.QueueThread;
import com.ngstudio.friendstep.utils.SmartReference;

import org.jetbrains.annotations.NotNull;


import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NotificationManager {
    private static final String TAG = NotificationManager.class.getSimpleName();

    public interface Client {
        public void handleNotificationMessage(int what, int arg1, int arg2, Object obj);
    }

    public static final class MessageFilter {
        private final Set<Integer> clientInterests = new HashSet<>();

        public MessageFilter(Integer... interests) {
            clientInterests.addAll(Arrays.asList(interests));
        }

        public Integer[] getClientInterests() {
            return clientInterests.isEmpty() ? null
                    : clientInterests.toArray(new Integer[clientInterests.size()]);
        }


        private boolean accept(int messageWhat) {
            return clientInterests.contains(messageWhat);
        }
    }

    private static class ClientHolder {

        private final WeakReference<Client> clientRef;
        private final MessageFilter         interests;

        private final int clientHash;


        public ClientHolder(Client client, MessageFilter interests) {
            if (BuildConfig.DEBUG) {
                if (client == null)
                    throw new NullPointerException("ClientHolder: client is 'null'");
            }

            this.clientRef = new SmartReference<>(client);
            this.interests = interests;

            this.clientHash = client.hashCode();
        }


        public boolean notifyClient(int what, int arg1, int arg2, Object object) {
            final Client client = clientRef.get();

            if (client == null) {
                return false;
            }

            if (interests == null || interests.accept(what))
                client.handleNotificationMessage(what, arg1, arg2, object);

            return true;
        }


        private static final class Matcher {

            private final Client client;

            private final int clientHash;

            public Matcher(Client client) {
                this.client = client;

                this.clientHash = client != null
                        ? client.hashCode() : hashCode();
            }

            @Override
            public int hashCode() {
                return clientHash;
            }

            @Override
            public boolean equals(Object o) {
                if (o instanceof ClientHolder) {
                    ClientHolder clientHolder = (ClientHolder) o;
                    return CommonUtils.areEqual(client, clientHolder.clientRef.get());
                }
                return super.equals(o);
            }
        }


        @Override
        public int hashCode() {
            return clientHash;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ClientHolder) {
                ClientHolder clientHolder = (ClientHolder) o;
                return CommonUtils.areEqual(clientRef.get(), clientHolder.clientRef.get());
            }
            return super.equals(o);
        }
    }

    private class ClientHandler extends Handler {

        private final Set<ClientHolder> myClients = new HashSet<>();


        private ClientHandler(Looper looper) {
            super(looper);
        }


        public boolean clientAdd(Client client, MessageFilter interests) {
            synchronized (myClients) {
                return myClients.add(new ClientHolder(client, interests));
            }
        }

        public boolean clientRemove(Client client) {
            //noinspection SuspiciousMethodCalls
            final boolean removed = myClients.remove(new ClientHolder.Matcher(client));

            if (removed && myClients.isEmpty()) {
                postRemoveClientHandler(this);
            }
            return removed;
        }


        @Override
        public void handleMessage(Message msg) {
            synchronized (myClients) {
                for (Iterator<ClientHolder> entriesIt = myClients.iterator(); entriesIt.hasNext(); ) {
                    ClientHolder entry = entriesIt.next();

                    if (!entry.notifyClient(msg.what, msg.arg1, msg.arg2, msg.obj)) {
                        entriesIt.remove();
                    }
                }
            }
            if (myClients.isEmpty()) {
                postRemoveClientHandler(this);
            }
        }
    }


    private final Map<Looper, ClientHandler> myHandlerAssociation = new HashMap<>(3, 0.3f);


    private static final int MSG_REGISTER_CLIENT       = 0;
    private static final int MSG_UNREGISTER_CLIENT     = 1;
    private static final int MSG_REMOVE_CLIENT_HANDLER = 2;
    private static final int MSG_NOTIFY_CLIENTS        = 3;


    private static class ClientCarrier {

        public final Looper looperToAssociate;

        public final Client        client;
        public final MessageFilter interests;

        public ClientCarrier(Client client, MessageFilter interests) {
            looperToAssociate = Looper.myLooper();

            this.client = client;
            this.interests = interests;
        }
    }

    private static class MessageCarrier {
        public final int what;
        public final int arg1, arg2;
        public final Object obj;

        public MessageCarrier(int what, int arg1, int arg2, Object obj) {
            this.what = what;
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.obj = obj;
        }

        public void postToClientHandler(ClientHandler handler) {
            handler.obtainMessage(what, arg1, arg2, obj).sendToTarget();
        }
    }

    private final class ManagerHandler extends Handler {

        private ManagerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT: {
                    final ClientCarrier carrier = (ClientCarrier) msg.obj;

                    assert carrier != null;
                    ClientHandler handler = myHandlerAssociation.get(carrier.looperToAssociate);

                    if (handler == null) {
                        myHandlerAssociation.put(carrier.looperToAssociate,
                                handler = new ClientHandler(carrier.looperToAssociate));
                    }
                    if (!handler.clientAdd(carrier.client, carrier.interests))
                        throw new Error("Client is already registered!");
                    break;
                }

                case MSG_UNREGISTER_CLIENT: {
                    Set<Map.Entry<Looper, ClientHandler>> entries = myHandlerAssociation.entrySet();

                    if (!(entries == null || entries.isEmpty())) {
                        for (Map.Entry<Looper, ClientHandler> e : entries) {
                            if (e.getValue().clientRemove((Client) msg.obj))
                                return;
                        }
                    }
                }

                case MSG_REMOVE_CLIENT_HANDLER: {
                    myHandlerAssociation.remove(msg.obj);
                    break;
                }

                case MSG_NOTIFY_CLIENTS: {
                    Set<Map.Entry<Looper, ClientHandler>> entries = myHandlerAssociation.entrySet();

                    if (!(entries == null || entries.isEmpty())) {
                        final MessageCarrier carrier = (MessageCarrier) msg.obj;

                        assert carrier != null;
                        for (Map.Entry<Looper, ClientHandler> e : entries) {
                            carrier.postToClientHandler(e.getValue());
                        }
                    }
                    break;
                }
            }
        }
    }

    private Handler myHandler;


    public void clientRegister(@NotNull Client client, MessageFilter interests) {
        myHandler.obtainMessage(MSG_REGISTER_CLIENT, new ClientCarrier(client, interests))
                .sendToTarget();
    }

    public void clientUnregister(@NotNull Client client) {
        myHandler.obtainMessage(MSG_UNREGISTER_CLIENT, client)
                .sendToTarget();
    }

    public void clientsNotify(int what, int arg1, int arg2, Object obj) {
        myHandler.obtainMessage(MSG_NOTIFY_CLIENTS, new MessageCarrier(what, arg1, arg2, obj))
                .sendToTarget();
    }

    private void postRemoveClientHandler(@NotNull ClientHandler clientHandler) {
        myHandler.obtainMessage(MSG_REMOVE_CLIENT_HANDLER, clientHandler.getLooper())
                .sendToTarget();
    }


    private NotificationManager() {
        final QueueThread serviceThread = new QueueThread(TAG) {
            @NotNull
            @Override
            protected Handler onCreateHandler(@NotNull Looper looper) {
                return new ManagerHandler(looper);
            }
        };
        myHandler = serviceThread.getHandler();
    }


    private static class NotificationManagerSingleton {
        public static final NotificationManager INSTANCE = new NotificationManager();
    }


    public static NotificationManager getInstance() {
        return NotificationManagerSingleton.INSTANCE;
    }


    public static void registerClient(@NotNull Client client, MessageFilter filter) {
        getInstance().clientRegister(client, filter);
    }

    public static void registerClient(@NotNull Client client) {
        registerClient(client, null);
    }

    public static void unregisterClient(@NotNull Client client) {
        getInstance().clientUnregister(client);
    }


    public static void notifyClients(int what, int arg1, int arg2, Object obj) {
        getInstance().clientsNotify(what, arg1, arg2, obj);
    }

    public static void notifyClients(int what) {
        notifyClients(what, 0, 0, null);
    }

    public static void notifyClients(int what, int arg1, int arg2) {
        notifyClients(what, arg1, arg2, null);
    }

    public static void notifyClients(int what, Object obj) {
        notifyClients(what, 0, 0, obj);
    }


}