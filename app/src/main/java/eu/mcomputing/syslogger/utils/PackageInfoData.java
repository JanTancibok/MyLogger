package eu.mcomputing.syslogger.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

    /**
     * Small structure to hold an application info
     */
    public final class PackageInfoData {
        /** linux user id */
        public int uid;
        /** application names belonging to this user id */
        public List<String> names;
        /** rules saving & load **/
        public  String pkgName;
        /** indicates if this application is selected for wifi */
        public  boolean selected_wifi;
        /** indicates if this application is selected for 3g */
        boolean selected_3g;
        /** indicates if this application is selected for roam */
        boolean selected_roam;
        /** indicates if this application is selected for vpn */
        boolean selected_vpn;
        /** indicates if this application is selected for lan */
        boolean selected_lan;
        /** toString cache */
        public String tostr;
        /** application info */
        public ApplicationInfo appinfo;
        /** cached application icon */
        public Drawable cached_icon;
        /** indicates if the icon has been loaded already */
        public boolean icon_loaded;
        /** first time seen? */
        public boolean firstseen;

        public PackageInfoData() {
        }
        public PackageInfoData(int uid, String name, String pkgNameStr) {
            this.uid = uid;
            this.names = new ArrayList<String>();
            this.names.add(name);
            this.pkgName = pkgNameStr;
        }
        public PackageInfoData(String user, String name, String pkgNameStr) {
            this(android.os.Process.getUidForName(user), name, pkgNameStr);
        }

        /**
         * Screen representation of this application
         */
        @Override
        public String toString() {
            if (tostr == null) {
                final StringBuilder s = new StringBuilder();
                //if (uid > 0) s.append(uid + ": ");
                for (int i = 0; i < names.size(); i++) {
                    if (i != 0) s.append(", ");
                    s.append(names.get(i));
                }
                s.append("\n");
                tostr = s.toString();
            }
            return tostr;
        }

        public String toStringWithUID() {
            if (tostr == null) {
                final StringBuilder s = new StringBuilder();
                s.append(uid + ": ");
                for (int i=0; i<names.size(); i++) {
                    if (i != 0) s.append(", ");
                    s.append(names.get(i));
                }
                s.append("\n");
                tostr = s.toString();
            }
            return tostr;
        }

        public static List<PackageInfoData> getApps(Context ctx) {
            final PackageManager pkgmanager = ctx.getPackageManager();
            final List<ApplicationInfo> installed = pkgmanager.getInstalledApplications(PackageManager.GET_META_DATA);

            List<PackageInfoData> list = new ArrayList<PackageInfoData>();

            //int i = 0;
            for(ApplicationInfo akt : installed){
                if(PackageManager.PERMISSION_GRANTED == pkgmanager.checkPermission(Manifest.permission.INTERNET, akt.packageName)){ //INTERNET
                    if((akt.flags & ApplicationInfo.FLAG_SYSTEM) == 0){ //no system
                        PackageInfoData app = new PackageInfoData();
                        app.uid = akt.uid;
                        app.appinfo = akt;
                        app.names = new ArrayList<String>();
                        app.names.add(pkgmanager.getApplicationLabel(akt).toString());
                        app.pkgName = akt.packageName;
                        list.add(app);
                    }
                }
            }

            return list;
        }
}
