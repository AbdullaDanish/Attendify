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
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class com.abdulla.nsspda.Converters { *; }
-dontwarn androidx.room.paging.**

# Keep all data classes and entities used by Room
-keep class com.abdulla.nsspda.**.domain.model.** { *; }
-keep class com.abdulla.nsspda.**.data.** { *; }

# Keep DAO interfaces as Room needs to generate implementations for them
-keep interface com.abdulla.nsspda.**.data.**Dao { *; }

#################################################
# Reflection metadata
#################################################

-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes *Annotation*
-keepattributes AnnotationDefault

#################################################
# Apache POI
#################################################

-keep class org.apache.poi.** { *; }
-keep interface org.apache.poi.** { *; }
-keep enum org.apache.poi.** { *; }
-dontwarn org.apache.poi.**

#################################################
# XMLBeans
#################################################

-keep class org.apache.xmlbeans.** { *; }
-keep interface org.apache.xmlbeans.** { *; }
-keep enum org.apache.xmlbeans.** { *; }
-dontwarn org.apache.xmlbeans.**

#################################################
# OOXML generated schemas
#################################################

-keep class org.openxmlformats.schemas.** { *; }
-keep interface org.openxmlformats.schemas.** { *; }
-keep enum org.openxmlformats.schemas.** { *; }
-dontwarn org.openxmlformats.schemas.**

-keep class com.microsoft.schemas.** { *; }
-keep interface com.microsoft.schemas.** { *; }
-keep enum com.microsoft.schemas.** { *; }
-dontwarn com.microsoft.schemas.**

#################################################
# Log4j used internally by Apache POI
#################################################

-keep class org.apache.logging.log4j.** { *; }
-keep interface org.apache.logging.log4j.** { *; }
-keep enum org.apache.logging.log4j.** { *; }
-dontwarn org.apache.logging.log4j.**

#################################################
# Apache Commons dependencies
#################################################

-keep class org.apache.commons.compress.** { *; }
-keep interface org.apache.commons.compress.** { *; }

-keep class org.apache.commons.collections4.** { *; }
-keep interface org.apache.commons.collections4.** { *; }

-keep class org.apache.commons.io.** { *; }
-keep interface org.apache.commons.io.** { *; }

-keep class org.apache.commons.codec.** { *; }
-keep interface org.apache.commons.codec.** { *; }

-keep class org.apache.commons.math3.** { *; }
-keep interface org.apache.commons.math3.** { *; }

-dontwarn org.apache.commons.**

#################################################
# ServiceLoader metadata
#################################################

-adaptresourcefilecontents META-INF/services/**
-keepdirectories META-INF/services

#################################################
# Optional desktop-only dependencies
#################################################

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