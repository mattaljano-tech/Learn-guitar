# Lägger in det appen behöver i Android-projektet efter "cap add android":
# behörigheter, förgrundstjänsten (spela med skärmen släckt), widgetarna och vår egen MainActivity.
import os, re, shutil, sys
A = 'android/app/src/main'
pkg = 'se/mattaljano/nylongitarr'
for f in os.listdir('native/java'):
    shutil.copy(os.path.join('native/java', f), os.path.join(A, 'java', pkg, f))
for sub in ('layout', 'xml', 'drawable', 'drawable-nodpi'):
    dst = os.path.join(A, 'res', sub); os.makedirs(dst, exist_ok=True)
    for f in os.listdir(os.path.join('native/res', sub)): shutil.copy(os.path.join('native/res', sub, f), os.path.join(dst, f))
m = os.path.join(A, 'AndroidManifest.xml'); s = open(m).read()
perms = ['RECORD_AUDIO', 'MODIFY_AUDIO_SETTINGS', 'VIBRATE', 'WAKE_LOCK', 'FOREGROUND_SERVICE', 'FOREGROUND_SERVICE_MEDIA_PLAYBACK', 'FOREGROUND_SERVICE_MICROPHONE', 'POST_NOTIFICATIONS', 'CAMERA']
add = ''.join('    <uses-permission android:name="android.permission.%s" />\n' % p for p in perms if ('android.permission.' + p + '"') not in s)
if 'android.hardware.camera' not in s: add += '    <uses-feature android:name="android.hardware.camera" android:required="false" />\n'
s = s.replace('</manifest>', add + '</manifest>')
app = '''
        <receiver android:name="io.capawesome.capacitorjs.plugins.foregroundservice.NotificationActionBroadcastReceiver" />
        <service android:name="io.capawesome.capacitorjs.plugins.foregroundservice.AndroidForegroundService" android:foregroundServiceType="mediaPlayback" />
        <receiver android:name=".NgWidget" android:exported="true" android:label="NylonGitarr">
            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE" /></intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/ng_widget_info" />
        </receiver>
        <receiver android:name=".NgWidgetSmall" android:exported="true" android:label="NylonGitarr streak">
            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE" /></intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/ng_widget_small_info" />
        </receiver>
        <service android:name=".NgJamService" android:exported="false" android:foregroundServiceType="microphone" />
        <receiver android:name=".NgWidgetJam" android:exported="true" android:label="NylonGitarr Jamma">
            <intent-filter><action android:name="android.appwidget.action.APPWIDGET_UPDATE" /></intent-filter>
            <meta-data android:name="android.appwidget.provider" android:resource="@xml/ng_widget_jam_info" />
        </receiver>
    </application>'''
assert s.count('</application>') == 1
s = s.replace('</application>', app, 1)
open(m, 'w').write(s)
print(s)
