# Minification (R8) and resource shrinking are on for release builds.
# Compose, Coil, WorkManager and Firebase all ship their own consumer
# ProGuard rules inside their AARs, so they don't need manual rules here.
# AGP also auto-keeps anything declared in AndroidManifest.xml (activities,
# receivers), so MainActivity and the widget's AppWidgetProvider are safe
# without a rule. The two things below are the gaps that aren't covered by
# either of those mechanisms.
-dontwarn org.jetbrains.annotations.**

# WorkManager instantiates custom Worker subclasses by class name, which
# isn't a call site R8 can always trace through a generic builder
# (WorkRequestBuilder<NotificationWorker>()) with full confidence — keep
# this package whole so that lookup can never fail silently once obfuscated.
-keep class com.sriramanappindi.openwire.notify.** { *; }

# Lets Crashlytics map an obfuscated stack trace back to real file/line
# info using the mapping file it uploads on every release build.
-keepattributes SourceFile,LineNumberTable
