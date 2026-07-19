# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Apache POI
-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**
-dontwarn com.microsoft.schemas.office.drawing.x2008.diagram.**
-dontwarn org.apache.xmlbeans.impl.store.Locale
-dontwarn org.apache.xmlbeans.impl.xpath.saxon.SaxonXPath
-dontwarn org.apache.poi.xslf.draw.SVGImageRenderer
-dontwarn org.apache.logging.log4j.util.OsgiServiceLocator

# Hilt/Dagger
-keep class dagger.hilt.android.internal.managers.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Keep models if they are used for serialization (e.g. Room, although Room handles it mostly)
-keep class com.abdulla.nsspda.**.domain.model.** { *; }
-keep class com.abdulla.nsspda.**.data.entity.** { *; }
