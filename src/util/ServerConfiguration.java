package util;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class ServerConfiguration {
    private static JSONObject configuration;

    static {
        try {
            String configFile = System.getProperty("config-file");
            if(configFile != null) {
                loadConfiguration(configFile);
                System.out.println(String.format("Loading configuration file: %s", configFile));
            } else {
                loadConfiguration("configuration.json");
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read configuration file");
        } catch (ParseException e) {
            throw new RuntimeException("Could not parse configuration file");
        }
    }

    private static void loadConfiguration(String configurationPath) throws IOException, ParseException {
        JSONParser p = new JSONParser();
        configuration = (JSONObject) p.parse(new FileReader(configurationPath));
    }


    @NotNull public static String getConfigurationString(@NotNull String optionName) {
        return (String)getConfigurationOption(optionName);
    }

    @NotNull public static String getConfigurationString(@NotNull String nameSpace, @NotNull String optionName) {
        return (String)getConfigurationOption(nameSpace, optionName);
    }

    @NotNull public static Integer getConfigurationInt(@NotNull String optionName) {
        return (int)(long)getConfigurationOption(optionName);
    }

    @NotNull public static Integer getConfigurationInt(@NotNull String nameSpace, @NotNull String optionName) {
        return (int)((long)getConfigurationOption(nameSpace, optionName));
    }


    @NotNull public static Object getConfigurationOption(@NotNull String nameSpace, @NotNull String optionName) {
        try {
            if (configuration.containsKey(nameSpace) && ((JSONObject) configuration.get(nameSpace)).containsKey(optionName)) {
                return ((JSONObject) configuration.get(nameSpace)).get(optionName);
            }
        } catch (ClassCastException e) {
            throw new RuntimeException("Invalid option type");
        }
        throw new RuntimeException("Configuration Option not found");
    }

    @NotNull public static Object getConfigurationOption(@NotNull String optionName) {
        if(configuration.containsKey(optionName)) {
            return configuration.get(optionName);
        }
        throw new RuntimeException("Configuration Option not found");
    }

}
