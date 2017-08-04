package de.vanillekeks.christenbot;

import de.vanillekeks.christenbot.frames.GUIFrame;
import de.vanillekeks.christenbot.modules.modulemanager.ModuleManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;

public class Core {

    private static Core instance;

    private JDA bot;

    private ModuleManager moduleManager;

    public static void main(String[] args) {
        instance = new Core();
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

    public JDA getBot() {
        return bot;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

}
