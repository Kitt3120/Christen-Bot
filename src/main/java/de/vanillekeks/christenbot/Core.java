package de.vanillekeks.christenbot;

import java.io.IOException;

import javax.security.auth.login.LoginException;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

import de.vanillekeks.christenbot.frames.GUIFrame;
import de.vanillekeks.christenbot.modules.modulemanager.ModuleManager;
import de.vanillekeks.christenbot.modules.modules.audio.AudioSystem;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class Core {

    private static Core instance;

    private static JDA bot;
    
    private static AudioSystem audioSystem;

    private static ModuleManager moduleManager;
    
    //Youtube API
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private static YouTube youtube;

    public static void main(String[] args) {
    	try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("Google HTTP_TRANSPORT could not be created. (Audio Module) - Exiting...");
            System.exit(1);
        }
    	
    	try {
    		youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest httpRequest) throws IOException {
                }
            }).setApplicationName("Christen-Bot").build();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("There was an error setting up the Youtube-API - " + e.getMessage() + "\nExiting...");
			System.exit(1);
		}
        System.out.println("Youtube-API set up. Starting AudioSystem...");
        audioSystem = new AudioSystem();
        System.out.println("AudioSystem set up. Starting bot...");
    	
        instance = new Core();
        System.out.println("Started");
    }

    public Core() {
        try {
            bot = new JDABuilder(AccountType.BOT).setToken("MzQyMzA0MDQyOTAwNTIwOTYw.DGN6bA.-NwrG5x4Qt1ZLQbVlSJy_zQXvmo").setAutoReconnect(true).buildBlocking();
        } catch (LoginException | IllegalArgumentException | InterruptedException | RateLimitedException e) {
            e.printStackTrace();
            System.err.println("Could not start the bot: " + e.getMessage());
            System.exit(0);
        }

        moduleManager = new ModuleManager();

        try {
            new GUIFrame();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not start the GUI: " + e.getMessage());
        }
    }

    public static Core getInstance() {
        return instance;
    }

    public static JDA getBot() {
        return bot;
    }

    public static ModuleManager getModuleManager() {
        return moduleManager;
    }
    
    public static YouTube getYoutube() {
		return youtube;
	}
    
    public static AudioSystem getAudioSystem() {
		return audioSystem;
	}
}
