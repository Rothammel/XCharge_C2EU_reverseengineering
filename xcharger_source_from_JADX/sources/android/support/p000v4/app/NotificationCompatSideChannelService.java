package android.support.p000v4.app;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.p000v4.app.INotificationSideChannel;

/* renamed from: android.support.v4.app.NotificationCompatSideChannelService */
public abstract class NotificationCompatSideChannelService extends Service {
    private static final int BUILD_VERSION_CODE_KITKAT_WATCH = 20;

    public abstract void cancel(String str, int i, String str2);

    public abstract void cancelAll(String str);

    public abstract void notify(String str, int i, String str2, Notification notification);

    public IBinder onBind(Intent intent) {
        if (!intent.getAction().equals(NotificationManagerCompat.ACTION_BIND_SIDE_CHANNEL) || Build.VERSION.SDK_INT >= 20) {
            return null;
        }
        return new NotificationSideChannelStub();
    }

    /* renamed from: android.support.v4.app.NotificationCompatSideChannelService$NotificationSideChannelStub */
    private class NotificationSideChannelStub extends INotificationSideChannel.Stub {
        private NotificationSideChannelStub() {
        }

        public void notify(String packageName, int id, String tag, Notification notification) throws RemoteException {
            NotificationCompatSideChannelService.this.checkPermission(getCallingUid(), packageName);
            long idToken = clearCallingIdentity();
            try {
                NotificationCompatSideChannelService.this.notify(packageName, id, tag, notification);
            } finally {
                restoreCallingIdentity(idToken);
            }
        }

        public void cancel(String packageName, int id, String tag) throws RemoteException {
            NotificationCompatSideChannelService.this.checkPermission(getCallingUid(), packageName);
            long idToken = clearCallingIdentity();
            try {
                NotificationCompatSideChannelService.this.cancel(packageName, id, tag);
            } finally {
                restoreCallingIdentity(idToken);
            }
        }

        public void cancelAll(String packageName) {
            NotificationCompatSideChannelService.this.checkPermission(getCallingUid(), packageName);
            long idToken = clearCallingIdentity();
            try {
                NotificationCompatSideChannelService.this.cancelAll(packageName);
            } finally {
                restoreCallingIdentity(idToken);
            }
        }
    }

    /* access modifiers changed from: private */
    public void checkPermission(int callingUid, String packageName) {
        String[] arr$ = getPackageManager().getPackagesForUid(callingUid);
        int len$ = arr$.length;
        int i$ = 0;
        while (i$ < len$) {
            if (!arr$[i$].equals(packageName)) {
                i$++;
            } else {
                return;
            }
        }
        throw new SecurityException("NotificationSideChannelService: Uid " + callingUid + " is not authorized for package " + packageName);
    }
}
