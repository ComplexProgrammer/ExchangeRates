package exchangerates.complexprogrammer.uz.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

import java.util.Locale;

import exchangerates.complexprogrammer.uz.MainActivity;

public class ChangeLanguage {
    private static SharedPreferences preferences;

    public static void setLocal(Context context, String langCode){
        Locale locale;
        Configuration config;
        locale = new Locale(langCode);
        Locale.setDefault(locale);
        config = new Configuration();
        config.locale = locale;
        context.createConfigurationContext(config);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

//        Locale[] locales = Locale.getAvailableLocales();
//        switch (langCode){
//
//            case "uz":
//                locale = new Locale.Builder().setLanguage("uz").setRegion("UZ").setScript("Latn").build();
//                Log.e("locale uz ",locale.getScript());
//
//                config = new Configuration();
//                config.setLocale(locale);
//                context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
////             case "uzc":
////                 locale = new Locale.Builder().setLanguage("uz").setRegion("UZ").setScript("Cyrl").build();
////                 Log.e("locale uzc ",locale.getScript());
////                 config = new Configuration();
////                 config.setLocale(locale);
////                 context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
////                for(Locale locale : locales){
////                    if(locale.getCountry().equalsIgnoreCase("UZ")
////                            && locale.getScript().equalsIgnoreCase("Cyrl"))
////                    {
////                        Configuration config = new Configuration();
////                        config.setLocale(locale);
////                        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
////                    }
////                }
//                case "en":
//                    locale = new Locale(langCode);
//                    Locale.setDefault(locale);
//                    config = new Configuration();
//                    config.locale = locale;
//                    context.createConfigurationContext(config);
//                    context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
//                case "ru":
//                    locale = new Locale(langCode);
//                    Locale.setDefault(locale);
//                    config = new Configuration();
//                    config.locale = locale;
//                    context.createConfigurationContext(config);
//                    context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
//
//        }




        preferences=context.getSharedPreferences("C0mplexLanguage", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("langCode",langCode);
        editor.apply();
    }

    public static String getLangCode(Context context){
        preferences=context.getSharedPreferences("C0mplexLanguage", Context.MODE_PRIVATE);
        String langCode=preferences.getString("langCode","en");
        return langCode;
    }
}
