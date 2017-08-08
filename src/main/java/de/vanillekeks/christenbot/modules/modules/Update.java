package de.vanillekeks.christenbot.modules.modules;

import de.vanillekeks.christenbot.Core;
import de.vanillekeks.christenbot.misc.AdminChecker;
import de.vanillekeks.christenbot.modules.Command;
import de.vanillekeks.christenbot.modules.IModule;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kitt3120 on 07.08.2017 at 15:16.
 */
public class Update implements IModule {

    private List<Command> commands = new ArrayList<>();

    public Update() {
        commands.add(new Command("Update", "Updates the bot", null));
    }

    @Override
    public List<Command> getCommands() {
        return commands;
    }

    @Override
    public boolean hasCommands() {
        return true;
    }

    @Override
    public void onCommand(Command command, List<String> args, User author, MessageChannel channel, Message message) {
        if (!AdminChecker.isAdmin(author)) {
            channel.sendMessage("Du darfst dies nicht tun").queue();
            return;
        }

        channel.sendMessage("Ich werde mich nun updaten...").queue();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Core.shutdown();
            }
        }).start();
    }

    @Override
    public String getName() {
        return "Update";
    }

    @Override
    public void onRegister() {
    }

    @Override
    public void unload() {
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void onEvent(Event event) {
    }

    @Override
    public void onShutdown() {
    }
}
