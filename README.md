SysLogger
=======
 
Android Logging app to gather various Logs
-----------
 
### Loging net

**FILE - HEADERS**   
net_stat_app.csv - TYPE;Time;UID;Rxbytes;Txbytes;<br />
net_stat.csv - Time;Interface;Rxbytes;Rxpackets;Rerrs;Rdrop;Txbytes;Txpackets;Txerrs;Txdrop<br />

### Loging apps

**FILE - HEADERS**   
app/installed_apps.csv - TimeUTC;UID;Name;ClasName;Permission;lastUpdateTime<br />
app/per_info.csv - TimeUTC;UID;packadgeName;name;group<br />
app/receiver_info.csv - TimeUTC;UID;packadgeName;name;permission;TargetActivity;taskAffinity;processName<br />
app/activity_info.csv - TimeUTC;UID;name<br />
app/service_info.csv - TimeUTC;UID;name<br />
<br />			
app/running_app#_list.csv - Time;CountRunning;UID1#UID2#...#UIDn<br />
app/removed_apps.csv - Time;UID;DataRemoved<br />

*per app logging*  
app/[uid].csv -
Time;Name;Pid;State;ppid;uTime;sTime;cutime;cstime;starttime;virtualmem;rss;
MemoryInfo-dalvikPrivateDirty;dalvikPss;dalvikSharedDirty;
nativePrivateDirty;nativePss;nativeSharedDirty;otherPrivateDirty;otherPss;otherSharedDirty
TotalPrivateDirty;TotalSharedDirty

### Loging cpu

**FILE - HEADERS**   
cpu_info.csv - cpuid;BogoMIPS
